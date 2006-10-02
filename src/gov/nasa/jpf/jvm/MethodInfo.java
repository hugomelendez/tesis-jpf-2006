//
// Copyright (C) 2006 United States Government as represented by the
// Administrator of the National Aeronautics and Space Administration
// (NASA).  All Rights Reserved.
// 
// This software is distributed under the NASA Open Source Agreement
// (NOSA), version 1.3.  The NOSA has been approved by the Open Source
// Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
// directory tree for the complete NOSA document.
// 
// THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
// KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
// LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
// SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
// A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
// THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
// DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
//
package gov.nasa.jpf.jvm;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.jvm.bytecode.Instruction;
import gov.nasa.jpf.util.Debug;
import gov.nasa.jpf.util.IntTable;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.CodeException;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.LineNumberTable;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;

import gov.nasa.jpf.jvm.bytecode.*;


/**
 * information associated with a method. Each method in JPF
 * is represented by a MethodInfo object
 */
public class MethodInfo implements Cloneable {
  private static final IntTable<String> globalIdTable = new IntTable<String>();
  
  static final String[] EMPTY = new String[0];
  
  /**
   * Used to warn about local variable information.
   */
  protected static boolean warnedLocalInfo = false;
  static final int         MJI_NONE = 0;
  static final int         MJI_NATIVE = 0x1;

  /**
   * scheduling relevance (used for on-the-fly POR)
   */
  public static final int         SR_NEVER = 0;       // never relevant, excl. sync blocks in body
  public static final int         SR_ALWAYS = 0x1;    // always relevant
  public static final int         SR_RUNNABLES = 0x2; // only relevant if there are other runnables
  public static final int         SR_SYNC = 0x4;      // only relevant if top lock level
  
  
  /**
   * Name of the method.
   */
  protected String name;

  /**
   * Signature of the method.
   */
  protected String signature;

  /**
   * Class the method belongs to.
   */
  protected ClassInfo ci;

  /**
   * Instructions associated with the method.
   */
  protected Instruction[] code;

  /**
   * Exception handlers.
   */
  protected ExceptionHandler[] exceptions;

  /**
   * Table used for line numbers.
   */
  protected int[] lineNumbers;

  /**
   * Local variables names.
   */
  protected String[] localVariableNames;

  /**
   * Local variables types.
   */
  protected String[] localVariableTypes;

  /**
   * Static method.
   */
  protected boolean isStatic;

  /**
   * Synchronized method.
   */
  protected boolean isSynchronized;

  /** cached for optimization purposes */
  protected boolean isClinit;
  
  /**
   * that's our own attribute - execute this method atomically
   */
  protected boolean isAtomic;
  
  /**
   * Native method.
   */
  protected boolean isNative;

  /**
   * is this a scheduling relevant method (step boundary)
   */
  protected int schedulingRelevance;
  
  /**
   * Maximum number of local variables.
   */
  protected int maxLocals;

  /**
   * Maximum number of elements on the stack.
   */
  protected int maxStack;

  // <2do> pcm - turn this into a derived class, it's only required for MJI methods

  /**
   * the number of stack slots for the arguments (incl. 'this'), lazy eval
   */
  private int argSize = -1;

  /**
   * number of arguments (excl. 'this'), lazy eval
   */
  private int nArgs = -1;

  /**
   * what return type do we have (again, lazy evaluated)
   */
  private byte returnType = -1;

  /**
   * used for native method parameter conversion (lazy evaluated)
   */
  private byte[] argTypes = null;

  /**
   * the various MJI method attrs (we don't want to burn 12 bytes for them
   */
  int mjiAttrs = MJI_NONE;

  /**
   * this is a lazy evaluated mangled name consisting of the name and
   * arg type signature
   */
  private String uniqueName;
  
  /**
   * a unique int assigned to this method.
   */
  private int globalId = -1;
  
  static InstructionFactory insnFactory;
  
  static boolean init (Config config) throws Config.Exception {
    insnFactory = config.getEssentialInstance("vm.insn_factory.class", InstructionFactory.class);
    return true;
  }
  
  /**
   * Creates a new method info.
   */
  protected MethodInfo (Method m, ClassInfo c) {
    name = m.getName();
    signature = m.getSignature();
    ci = c;

    code = loadCode(m);
    exceptions = loadExceptions(m);
    lineNumbers = loadLineNumbers(m);
    maxLocals = getMaxLocals(m);
    maxStack = getMaxStack(m);
    localVariableNames = loadLocalVariableNames(m);
    localVariableTypes = loadLocalVariableTypes(m);
    isStatic = m.isStatic();
    isNative = m.isNative();
    
    // clinits are automatically synchronized on the class object
    if (name.equals("<clinit>")) {
      isSynchronized = true;
      isClinit = true;
    } else {
      isSynchronized = m.isSynchronized();      
    }
    
    // since that's used to store the method in the ClassInfo, and to
    // identify it in tne InvokeInstruction, we can set it here
    uniqueName = getUniqueName(name, signature);
    
    JVM.getVM().annotations.loadAnnotations(this, m.getAnnotationEntries());
  }

  protected MethodInfo () {
    // for explicit construction only
  }
  
  void setAtomic (boolean isAtomic) {
    this.isAtomic = isAtomic;
  }
  
  public boolean isAtomic () {
    return isAtomic;
  }
  
  void setSchedulingRelevance (int sr) {
    schedulingRelevance = sr;
  }
        
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException cnx) {
      return null;
    }
  }
  
  public int getGlobalId() {
    if (globalId < 0) {
      globalId = globalIdTable.poolIndex(getFullName());
    }
    return globalId;
  }
  
  /**
   * NOTE - this only works in conjunction with a special StackFrame
   */
  public MethodInfo createDirectCallStub (String originator) {
    MethodInfo mi = new MethodInfo();
    String cname = ci.getName();
    InvokeInstruction insn;

    mi.name = originator + cname; // could maybe also include the called method, but keep it fast
    mi.signature = "()V";
    mi.maxLocals = isStatic ? 0 : 1;
    mi.maxStack = getNumberOfCallerStackSlots();  // <2do> cache for optimization
    mi.localVariableNames = EMPTY;
    mi.localVariableTypes = EMPTY;
    mi.isStatic = isStatic;
    mi.isSynchronized = false;
    mi.isNative = false;
    
    if (isStatic) {
      if (isClinit()) {
        insn = new INVOKECLINIT(mi, cname, 0, 0);
      } else {
        insn = new INVOKESTATIC(mi, cname, name, signature, 0, 0);
      }
    } else if (name.equals("<init>")){
      insn = new INVOKESPECIAL(mi, cname, name, signature, 0, 0);
    } else {
      insn = new INVOKEVIRTUAL(mi, cname, name, signature, 0, 0);
    }
    
    mi.code = new Instruction[2];
    mi.code[0] = insn;
    mi.code[1] = new RETURN(mi, 1, 4); // we don't return values from this stackframe!
    
    mi.lineNumbers = null;
    mi.exceptions = null;

    mi.uniqueName = mi.name;
    
    return mi;
  }
  
  public boolean isSyncRelevant () {
    return (name.charAt(0) != '<');
  }
  
  public boolean isClinit () {
    return isClinit;
  }

  public boolean isClinit (ClassInfo ci) {
    return (isClinit && (this.ci == ci));
  }

  /**
   * return the minimal name that has to be unique for overloading
   * used as a lookup key
   * NOTE: with the silent introduction of covariant return types
   * in Java 5.0, we have to use the full signature to be unique
   */
  public static String getUniqueName (String mname, String signature) {
    return (mname + signature);
  }

  public byte[] getArgumentTypes () {
    if (argTypes == null) {
      argTypes = Types.getArgumentTypes(signature);
    }

    return argTypes;
  }

  public int getArgumentsSize () {
    if (argSize < 0) {
      argSize = Types.getArgumentsSize(signature);

      if (!isStatic) {
        argSize++;
      }
    }

    return argSize;
  }

  public String getSourceFileName () {
    if (ci != null) {
      return ci.getSourceFileName();
    } else {
      return "<VM>";
    }
  }

  public String getClassName () {
    if (ci != null) {
      return ci.getName();
    } else {
      return "<VM>";
    }
  }
  
  /**
   * Returns the class the method belongs to.
   */
  public ClassInfo getClassInfo () {
    return ci;
  }

  /**
   * Return the complete name of the method, including the class name.
   */
  public String getCompleteName () {
    return getClassName() + '.' + name + signature;
  }

  public boolean isExecutable (ThreadInfo ti) {
    // <2do> well, that doesn't take into account if executability depends on values
    // but 'isExecutable' is going away anyways
    return canEnter(ti);
  }

    
  public boolean isCtor () {
    return (name.equals("<init>"));
  }
  
  public boolean isInternalMethod () {
    // <2do> pcm - should turn this into an attribute for efficiency reasons
    return (name.equals("<clinit>") || uniqueName.equals("finalize()V"));
  }
  
  public boolean isThreadEntry (ThreadInfo ti) {
    return (uniqueName.equals("run()V") && (ti.countStackFrames() == 1));
  }
  
  /**
   * Returns the full name of the method, name and signature.
   */
  public String getFullName () {
    if (ci != null) {
      return ci.getName() + '.' + getUniqueName();
    } else {
      return (name + signature);
    }
  }

  /**
   * Returns a specific instruction.
   */
  public Instruction getInstruction (int i) {
    if (code == null) {
      return null;
    }

    if ((i < 0) || (i >= code.length)) {
      return null;
    }

    return code[i];
  }

  /**
   * Returns the instruction at a certain position.
   */
  public Instruction getInstructionAt (int position) {
    if (code == null) {
      return null;
    }

    for (int i = 0, l = code.length; i < l; i++) {
      if ((code[i] != null) && (code[i].getPosition() == position)) {
        return code[i];
      }
    }

    throw new JPFException("instruction not found");
  }

  /**
   * Returns the instructions of the method.
   */
  public Instruction[] getInstructions () {
    return code;
  }

  /**
   * Returns the line number for a given position.
   */
  public int getLineNumber (Instruction pc) {
    if (lineNumbers == null) {
      return pc.getPosition();
    }
    int idx = pc.getOffset();
    if (idx < 0) idx = 0;
    return lineNumbers[idx];
  }

  /**
   * Returns a table to translate positions into line numbers.
   */
  public int[] getLineNumbers () {
    return lineNumbers;
  }

  public boolean isMJI () {
    return ((mjiAttrs & MJI_NATIVE) != 0);
  }

  public int getMaxLocals () {
    return maxLocals;
  }

  public static int getMaxLocals (Method m) {
    Code c = m.getCode();

    if (c == null) {
      return 0;
    }

    return c.getMaxLocals();
  }

  public int getMaxStack () {
    return maxStack;
  }

  public static int getMaxStack (Method m) {
    Code c = m.getCode();

    if (c == null) {
      return 0;
    }

    return c.getMaxStack();
  }

  public ExceptionHandler[] getExceptions () {
    return exceptions;
  }

  public String[] getLocalVariableNames () {
    return localVariableNames;
  }

  public String[] getLocalVariableTypes () {
    return localVariableTypes;
  }

  /**
   * Returns the name of the method.
   */
  public String getName () {
    return name;
  }

  /**
   * Returns true if the method is native
   */
  public boolean isNative () {
    return isNative;
  }

  public int getNumberOfArguments () {
    if (nArgs < 0) {
      nArgs = Types.getNumberOfArguments(signature);
    }

    return nArgs;
  }

  /**
   * Returns the size of the arguments.
   * This returns the number of parameters passed on the stack, incl. 'this'
   */
  public int getNumberOfStackArguments () {
    int n = getNumberOfArguments();

    return isStatic ? n : n + 1;
  }

  public int getNumberOfCallerStackSlots () {
    int n = Types.getNumberOfStackSlots(signature);
    if ((n == 0) && !isStatic) { // we need at least a 'this'
      n++;
    }
    return n;
  }
  
  /**
   * do we return Object references?
   */
  public boolean isReferenceReturnType () {
    int r = getReturnType();

    return ((r == Types.T_REFERENCE) || (r == Types.T_ARRAY));
  }

  public byte getReturnType () {
    if (returnType < 0) {
      returnType = Types.getReturnType(signature);
    }

    return returnType;
  }

  /**
   * Returns the signature of the method.
   */
  public String getSignature () {
    return signature;
  }

  /**
   * Returns true if the method is static.
   */
  public boolean isStatic () {
    return isStatic;
  }

  /**
   * Returns true if the method is synchronized.
   */
  public boolean isSynchronized () {
    return isSynchronized;
  }
  
  public String getUniqueName () {
    return uniqueName;
  }

  public boolean canEnter (ThreadInfo th) {
    if (isSynchronized) {
      ElementInfo ei = getBlockedObject(th, true);

      // <?> pcm - the other way round would be intuitive
      return ei.canLock(th);
    }

    return true;
  }

  public ElementInfo getBlockedObject (ThreadInfo th, boolean isBeforeCall) {
    int         objref;
    ElementInfo ei = null;

    if (isSynchronized) {
      if (isStatic) {
        objref = ci.getClassObjectRef();
      } else {
        // NOTE 'inMethod' doesn't work for natives, because th.getThis()
        // pulls 'this' from the stack frame, which we don't have (and don't need)
        // for natives
        objref = isBeforeCall ? th.getCalleeThis(this) : th.getThis();
      }

      DynamicArea da = JVM.getVM().getDynamicArea();
      ei = da.get(objref);

      assert (ei != null) : ("inconsistent stack, no object or class ref: " +
                               getCompleteName() + " (" + objref +")");
    }

    return ei;
  }

  public void enter (ThreadInfo ti) {
    if (isSynchronized) {
      ElementInfo ei = getBlockedObject(ti, false);
      ei.lock(ti);
      
      if (isStatic && isClinit()) {
        ci.setInitializing(ti);
      }
    }
  }

  public void leave (ThreadInfo ti) {
    
    // <2do> - that's not really enough, we might have suspicious bytecode that fails
    // to release locks acquired by monitor_enter (e.g. by not having a handler that
    // monitor_exits & re-throws). That's probably shifted into the bytecode verifier
    // in the future (i.e. outside JPF), but maybe we should add an explicit test here
    // and report an error if the code does asymmetric locking (according to the specs,
    // VMs are allowed to silently fix this, so it might run on some and fail on others)
    
    if (isSynchronized) {
      ElementInfo ei = getBlockedObject(ti, false);
      ei.unlock(ti);
      
      if (isStatic && isClinit()) {
        // we just released the lock on the class object, returning from a clinit
        // now we can consider this class to be initialized.
        // NOTE this is still part of the RETURN insn of clinit, so ClassInfo.isInitialized
        // is protected
        ci.setInitialized();
      }
    }
  }
  
  /**
   * execute this method, which might be either bytecode or native.
   */
  public Instruction execute (ThreadInfo ti) {
    if (((mjiAttrs & MJI_NATIVE) != 0) || isNative) {
      NativePeer nativePeer = ci.getNativePeer();
      if (nativePeer != null) {
        return  nativePeer.executeMethod(ti, this);
      } else {
        return ti.createAndThrowException("java.lang.UnsatisfiedLinkError",
                                          ci.getName() + '.' + getUniqueName() + " (no peer)");
      }
      
    } else {
      ti.pushFrame( new StackFrame(this, ti.top()));
      enter(ti);

      return ti.getPC();
    }
  }

  /**
   * Loads the code of the method.
   */
  protected Instruction[] loadCode (Method m) {
    Code c = m.getCode();

    if (c == null) {
      return null;
    }

    InstructionList     il = new InstructionList(c.getCode());

    InstructionHandle[] hs = il.getInstructionHandles();
    
    int                 length = hs.length;

    Instruction[]       is = new Instruction[length];

    for (int i = 0; i < length; i++) {
      is[i] = insnFactory.create(hs[i], i, this, m.getConstantPool());

      if (c.getLineNumberTable() != null) {
        // annoying bug when BCEL don't seem to find linenumber - pos match
        // also sometimes linenumber tables are not available
        is[i].setContext(ci.getName(), name,
                         c.getLineNumberTable()
                          .getSourceLine(is[i].getPosition()),
                         is[i].getPosition());
      }
    }

    return is;
  }

  /**
   * Returns the exceptions of the method.
   */
  protected ExceptionHandler[] loadExceptions (Method m) {
    Code c = m.getCode();

    if (c == null) {
      return null;
    }

    CodeException[] ce = c.getExceptionTable();

    if (ce.length == 0) {
      return null;
    }

    int                length = ce.length;
    ExceptionHandler[] eh = new ExceptionHandler[length];

    ConstantPool       cp = m.getConstantPool();

    for (int i = 0; i < length; i++) {
      int ct = ce[i].getCatchType();
      eh[i] = new ExceptionHandler(((ct == 0)
                                    ? null
                                    : cp.getConstantString(ct,
                                                           Constants.CONSTANT_Class)
                                        .replace('/', '.')), ce[i].getStartPC(),
                                   ce[i].getEndPC(), ce[i].getHandlerPC());
    }

    return eh;
  }

  /**
   * Loads the line numbers for the method.
   */
  protected int[] loadLineNumbers (Method m) {
    Code c = m.getCode();

    if (c == null) {
      return null;
    }

    LineNumberTable lnt = c.getLineNumberTable();

    int             length = code.length;
    int[]           ln = new int[length];

    if (lnt == null) {
      // no line information
      return null;
    } else {
      for (int i = 0; i < length; i++) {
        try { //annoying bug when BCEL don't seem to find linenumber - pos match
          ln[i] = lnt.getSourceLine(code[i].getPosition());
        } catch (RuntimeException e) {
          System.out.print("^");
        }
      }
    }

    return ln;
  }

  /**
   * Loads the names of the local variables.
   *
   * NOTE: BCEL only gives us a list of all *named* locals, which might not
   * include all local vars (temporaries, like StringBuffer). Note that we have
   * to fill this with "?" inorder to make the returned array correspond with
   * slot numbers
   */
  protected String[] loadLocalVariableNames (Method m) {
    Code c = m.getCode();

    if (c == null) {
      return null;
    }

    LocalVariableTable lvt = c.getLocalVariableTable();

    if (lvt == null) {
      if (!warnedLocalInfo && !ci.isSystemClass()) {
        Debug.println(Debug.WARNING);
        Debug.println(Debug.WARNING, "No local variable information available");
        Debug.println(Debug.WARNING, "for " + getCompleteName());
        Debug.println(Debug.WARNING,
                      "Recompile with -g to include this information");
        Debug.println(Debug.WARNING);
        warnedLocalInfo = true;
      }

      return null;
    }

    LocalVariable[] lv = lvt.getLocalVariableTable();
    int             length = lv.length;
    String[]        v = new String[c.getMaxLocals()];

    for (int i = 0; i < length; i++) {
      v[lv[i].getIndex()] = lv[i].getName();
    }

    for (int i=0; i<v.length; i++) {
      if (v[i] == null) {
        v[i] = "?";
      }
    }

    return v;
  }

  /**
   * Loads the types of the local variables.
   * see loadLocalVariableNames for the problem with temporaries and
   * why we can't copy the types 1:1
   */
  protected String[] loadLocalVariableTypes (Method m) {
    Code c = m.getCode();

    if (c == null) {
      return null;
    }

    LocalVariableTable lvt = c.getLocalVariableTable();

    if (lvt == null) {
      if (!warnedLocalInfo && !ci.isSystemClass()) {
        Debug.println(Debug.WARNING, "No local variable information available");
        Debug.println(Debug.WARNING, "for " + getCompleteName());
        Debug.println(Debug.WARNING,
                      "Recompile with -g to include this information");
        Debug.println(Debug.WARNING);
        warnedLocalInfo = true;
      }

      return null;
    }

    LocalVariable[] lv = lvt.getLocalVariableTable();
    int             length = lv.length;
    String[]        v = new String[c.getMaxLocals()];

    for (int i = 0; i < length; i++) {
      v[lv[i].getIndex()] = lv[i].getSignature();
    }

    for (int i=0; i<v.length; i++) {
      if (v[i] == null) {
        v[i] = "?";
      }
    }

    return v;
  }

  void setMJI (boolean isMJI) {
    if (isMJI) {
      mjiAttrs |= MJI_NATIVE;
    } else {
      mjiAttrs &= ~MJI_NATIVE;
    }
  }
  
  public String toString() {
    return "MethodInfo[" + getFullName() + ']';
  }
}
