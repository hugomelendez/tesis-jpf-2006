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
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.jvm.bytecode.INVOKESTATIC;
import gov.nasa.jpf.jvm.bytecode.Instruction;
import gov.nasa.jpf.jvm.bytecode.InvokeInstruction;
import gov.nasa.jpf.jvm.bytecode.ReturnInstruction;
import gov.nasa.jpf.util.Debug;
import gov.nasa.jpf.util.HashData;
import gov.nasa.jpf.util.IntVector;
import gov.nasa.jpf.util.SparseObjVector;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Represents a thread. It contains the state of the thread, static
 * information about the thread, and the stack frames.
 * Race detection and lock order also store some information
 * in this data structure.
 */
public class ThreadInfo implements Iterable<StackFrame>, Comparable<ThreadInfo> {
  
  static Logger log = JPF.getLogger("gov.nasa.jpf.jvm.ThreadInfo");
    
  public static final int NEW = 0;
  public static final int RUNNING = 1;
  public static final int BLOCKED = 2;
  public static final int UNBLOCKED = 3;
  public static final int WAITING = 4;
  public static final int TIMEOUT_WAITING = 5;
  public static final int NOTIFIED = 6;
  public static final int STOPPED = 7;
  public static final int INTERRUPTED = 8;
  public static final int UNBLOCKED_INTERRUPTED = 9;
  public static final int TIMEDOUT = 10;
  public static final int TERMINATED = 11;
  
  public static final String[] statusName = {
    "NEW", "RUNNING", "BLOCKED", "UNBLOCKED", "WAITING", "TIMEOUT_WAITING", "NOTIFIED",
    "STOPPED", "INTERRUPTED", "UNBLOCKED_INTERRUPTED", "TIMEDOUT", "TERMINATED"
  };
  
  static ThreadInfo currentThread;
  static ThreadInfo mainThread;
  
  /** do we want a forced reschedule */
  boolean yield;
  
  protected ExceptionInfo pendingException;
  
  /** backtrack-relevant Information about the thread */
  protected ThreadData threadData;
    
  /**
   * The stack frames of the JVM.
   */
  protected final ArrayList<StackFrame> stack = new ArrayList<StackFrame>();
  
  /**
   * The top stack frame.
   * INVARIANTS:
   * topIdx == topIdx 
   *    top == (stack.isEmpty() ? null : stack.get(topIdx)) 
   */
  protected StackFrame top = null;
  protected int topIdx = -1;
  
  /**
   * Reference of the thread list it is in.
   * <2do> - bad cross ref (ThreadList should know about ThreadInfo, but not vice versa)
   */
  public ThreadList list;
  
  /** thread list index */
  public int index;
  
  /**
   * <2do> pcm - BAD, if it doesn't get set after changing ThreadData fields
   * that result in a new hashvalue, we get terribly out of sync. Move this logic
   * into ThreadData, where it belongs!
   */
  public boolean tdChanged;
  
  /** which stackframes have changed */
  protected final BitSet hasChanged = new BitSet();

  /** the first insn in the current transition */
  protected boolean isFirstStepInsn;
  
  /** shall we skip the next insn */
  boolean skipInstruction;
  
  /** store the last executed insn in the path */
  boolean logInstruction;
  
  
  DirectCallStackFrame returnedDirectCall;
  
  /** the next insn to execute */
  Instruction nextPc;

  /**
   * not so nice we cross-couple the NativePeers with ThreadInfo,
   * but to carry on with the JNI analogy, a MJIEnv is clearly
   * owned by a thread (even though we don't have much ThreadInfo
   * state dependency in here (yet), hence the simplistic init)
   */
  MJIEnv env;
  
  /**
   * the VM we are running on. Bad backlink, but then again, we can't really
   * test a ThreadInfo outside a VM context anyways.
   * <2do> If we keep 'list' as a field, 'vm' might be moved over there
   * (all threads in the list share the same VM) 
   */
  JVM vm;
  
  /**
   * !! this is a volatile object, i.e. has to be reset and restored
   * !! after each backtrack (we don't want to duplicate state storage)
   * list of lock objects currently held by this thread.
   * unfortunately, we cannot organize this as a stack, since it might get
   * restored (from the heap) in random order
   */
  LinkedList<ElementInfo> lockedObjects;
  
  /**
   * !! this is also volatile -> has to be reset after backtrack
   * the reference of the object if this thread is blocked or waiting for
   */
  int lockRef = -1;
  
  /**
   * this is where we keep ThreadInfos, indexed by their java.lang.Thread objRef, to
   * enable us to keep ThreadInfo identities across backtracked and restored states
   */
  static SparseObjVector<ThreadInfo> threadInfos;
  
  // the following parameters are configurable. Would be nice if we could keep
  // them on a per-instance basis, but there are a few locations
  // (e.g. ThreadList) where we loose the init context, and it's questionable
  // if we want to change this at runtime, or POR might make sense on a per-thread
  // basis
  
  /** do we halt on each throw, i.e. don't look for an exception handler?
   * Useful to find empty handler blocks, or misusd exceptions
   */
  static boolean haltOnThrow;
  
  /** is on-the-fly partial order in effect? */
  static boolean porInEffect;
  
  /** do we treat access of fields referring to objects that are reachable
   * from different threads as boundary steps (i.e. starting a new Transition)?
   */
  static boolean porFieldBoundaries;
  
  /** detect field synchronization (find locks which are used to synchronize
   * field access - if we have viable candidates, and we find the locks taken,
   * we don't treat access of the corresponding field as a boundary step 
   */
  static boolean porSyncDetection;
    
  static boolean init (Config config) {
    currentThread = null;
    mainThread = null;
    
    haltOnThrow = config.getBoolean("vm.halt_on_throw");
    porInEffect = config.getBoolean("vm.por");
    porFieldBoundaries = config.getBoolean("vm.por.field_boundaries");
    porSyncDetection = config.getBoolean("vm.por.sync_detection");
    
    threadInfos = new SparseObjVector<ThreadInfo>();
    
    return true;
  }
  
  /**
   * Creates a new thread info. It is associated with the object
   * passed and sets the target object as well.
   */
  public ThreadInfo (JVM vm, int objRef) {    
    init( vm, objRef);
    
    env = new MJIEnv(this);
    threadInfos.set(objRef, this); // our ThreadInfo repository
    
    // there can only be one
    if (mainThread == null) {
      mainThread = this;
      currentThread = this;
    }
  }
 
  public static ThreadInfo getMainThread () {
    return mainThread;
  }
  
  private void init (JVM vm, int objRef) {
    
    DynamicArea da = vm.getDynamicArea();
    ElementInfo ei = da.get(objRef);

    this.vm = vm;
    
    threadData = new ThreadData();
    threadData.status = NEW;
    threadData.ci = ei.getClassInfo();    
    threadData.objref = objRef;
    threadData.target = MJIEnv.NULL;
    threadData.lockCount = 0;
    // this is nasty - 'priority', 'name', 'target' and 'group' are not taken
    // from the object, but set within the java.lang.Thread ctors
    
    stack.clear();
    top = null;
    topIdx = -1;
    
    lockedObjects = new LinkedList<ElementInfo>();

    markUnchanged();
    tdChanged = true;
  }
  
  /**
   * if we already had a ThreadInfo object for this java.lang.Thread object, make
   * sure we reset it. It will be restored to proper state afterwards
   */
  static ThreadInfo createThreadInfo (JVM vm, int objRef) {
    ThreadInfo ti = threadInfos.get(objRef);
    
    if (ti == null) {
      ti = new ThreadInfo(vm, objRef);
    } else {
      ti.init(vm, objRef);
    }

    vm.addThread(ti);
    
    return ti;
  }
  
  /**
   * just retrieve the ThreadInfo object for this java.lang.Thread object. This method is
   * only valid after the thread got created 
   */
  static ThreadInfo getThreadInfo (JVM vm, int objRef) {
    return threadInfos.get(objRef);
  }
  

  
  public static ThreadInfo getCurrent() {
    return currentThread;
  }

  
  public boolean isExecutingAtomically () {
    return vm.getSystemState().isAtomic();
  }
  
  public boolean holdsLock (ElementInfo ei) {
    return lockedObjects.contains(ei);
  }
    
  public JVM getVM () {
    return vm;
  }
  
  public boolean isFirstStepInsn() {
    return isFirstStepInsn;
  }
    
  public boolean usePor () {
    return porInEffect;
  }
  
  public boolean usePorFieldBoundaries () {
    return porFieldBoundaries;
  }
  
  public boolean usePorSyncDetection () {
    return porSyncDetection;
  }
  
  void setListInfo (ThreadList tl, int idx) {
    list = tl;
    index = idx;
  }
  
  /**
   * Checks if the thread is waiting to execute a nondeterministic choice
   * due to an abstraction, i.e. due to a Bandera.choose() call
   * 
   * <2do> that's probably deprecated
   */
  public boolean isAbstractionNonDeterministic () {
    if (getPC() == null) {
      return false;
    }
    
    return getPC().examineAbstraction(vm.getSystemState(), vm.getKernelState(), this);
  }
  
  /**
   * An alive thread is anything but TERMINATED or NEW
   */
  public boolean isAlive () {
    return (threadData.status != TERMINATED && threadData.status != NEW);
  }
  
  public boolean isWaiting () {
    int state = threadData.status;
    return (state == WAITING) || (state == TIMEOUT_WAITING);
  }
  
  public boolean isNotified () {
    return (threadData.status == NOTIFIED);
  }

  public boolean isTimedout () {
    return (threadData.status == TIMEDOUT);
  }
  
  public boolean isInterrupted () {
    return (threadData.status == INTERRUPTED);
  }
  
  public boolean isUnblocked () {
    int state = threadData.status;
    return (state == UNBLOCKED) || (state == UNBLOCKED_INTERRUPTED) || (state == TIMEDOUT);
  }
  
  public boolean isBlocked () {
    return (threadData.status == BLOCKED);
  }
  
  public boolean isBlockedOrNotified() {
    int state = threadData.status;
    return (state == BLOCKED) || (state == NOTIFIED);
  }

  public boolean getBooleanLocal (String lname) {
    return Types.intToBoolean(getLocalVariable(lname));
  }
  
  public boolean getBooleanLocal (int lindex) {
    return Types.intToBoolean(getLocalVariable(lindex));
  }
  
  public boolean getBooleanLocal (int fr, String lname) {
    return Types.intToBoolean(getLocalVariable(fr, lname));
  }
  
  public boolean getBooleanLocal (int fr, int lindex) {
    return Types.intToBoolean(getLocalVariable(fr, lindex));
  }
  
  public boolean getBooleanReturnValue () {
    return Types.intToBoolean(peek());
  }
  
  public byte getByteLocal (String lname) {
    return (byte) getLocalVariable(lname);
  }
  
  public byte getByteLocal (int lindex) {
    return (byte) getLocalVariable(lindex);
  }
  
  public byte getByteLocal (int fr, String lname) {
    return (byte) getLocalVariable(fr, lname);
  }
  
  public byte getByteLocal (int fr, int lindex) {
    return (byte) getLocalVariable(fr, lindex);
  }
  
  public byte getByteReturnValue () {
    return (byte) peek();
  }
  
  public String[] getCallStack () {
    int len = stack.size();
    ArrayList<String> list = new ArrayList<String>(len);
    for (int i = 0; i < len; i++) {
      if (!stack.get(i).isDirectCallFrame()) {
        list.add(stack.get(i).getStackTrace());
      }
    }
    
    String[] callStack = new String[list.size()];
    list.toArray(callStack);
    
    return callStack;
  }
  
  public String getCallStackClass (int i) {
    if (i < stack.size()) {
      return frame(i).getClassName();
    } else {
      return null;
    }
  }
  
  public String getCallStackFile (int i) {
    if (i < stack.size()) {
      return frame(i).getSourceFile();
    } else {
      return null;
    }
  }
  
  public int getCallStackLine (int i) {
    if (i < stack.size()) {
      return frame(i).getLine();
    } else {
      return 0;
    }
  }
  
  public String getCallStackMethod (int i) {
    if (i < stack.size()) {
      return frame(i).getMethodInfo().getName();
    } else {
      return null;
    }
  }
  
  /**
   * Returns the this pointer of the callee from the stack.
   */
  public int getCalleeThis (MethodInfo mi) {
    return top.getCalleeThis(mi);
  }
  
  /**
   * Returns the this pointer of the callee from the stack.
   */
  public int getCalleeThis (int size) {
    return top.getCalleeThis(size);
  }
  
  public boolean isCalleeThis (ElementInfo r) {
    if (top == null || r == null) {
      return false;
    }
    
    Instruction pc = getPC();
    
    if (pc == null ||
        !(pc instanceof InvokeInstruction) ||
        pc instanceof INVOKESTATIC) {
      return false;
    }
    
    InvokeInstruction i = (InvokeInstruction) pc;
    
    return getCalleeThis(Types.getArgumentsSize(i.signature) + 1) == r.getIndex();
  }
  
  public char getCharLocal (String lname) {
    return (char) getLocalVariable(lname);
  }
  
  public char getCharLocal (int lindex) {
    return (char) getLocalVariable(lindex);
  }
  
  public char getCharLocal (int fr, String lname) {
    return (char) getLocalVariable(fr, lname);
  }
  
  public char getCharLocal (int fr, int lindex) {
    return (char) getLocalVariable(fr, lindex);
  }
  
  public char getCharReturnValue () {
    return (char) peek();
  }
  
  /**
   * Returns the class information.
   */
  public ClassInfo getClassInfo () {
    return threadData.ci;
  }
  
  public double getDoubleLocal (String lname) {
    return Types.longToDouble(getLongLocalVariable(lname));
  }
  
  public double getDoubleLocal (int lindex) {
    return Types.longToDouble(getLongLocalVariable(lindex));
  }
  
  public double getDoubleLocal (int fr, String lname) {
    return Types.longToDouble(getLongLocalVariable(fr, lname));
  }
  
  public double getDoubleLocal (int fr, int lindex) {
    return Types.longToDouble(getLongLocalVariable(fr, lindex));
  }
  
  public double getDoubleReturnValue () {
    return Types.longToDouble(longPeek());
  }
  
  /**
   * An enabled thread is either running, notified, or interrupted.
   */
  public boolean isEnabled () {
    boolean isEnabled = (threadData.status == RUNNING) ||
                        (threadData.status == INTERRUPTED) ||
                        (threadData.status == NOTIFIED);
    return isEnabled;
  }
  
  public float getFloatLocal (String lname) {
    return Types.intToFloat(getLocalVariable(lname));
  }
  
  public float getFloatLocal (int lindex) {
    return Types.intToFloat(getLocalVariable(lindex));
  }
  
  public float getFloatLocal (int fr, String lname) {
    return Types.intToFloat(getLocalVariable(fr, lname));
  }
  
  public float getFloatLocal (int fr, int lindex) {
    return Types.intToFloat(getLocalVariable(fr, lindex));
  }
  
  public float getFloatReturnValue () {
    return Types.intToFloat(peek());
  }
  
  public int getIntLocal (String lname) {
    return getLocalVariable(lname);
  }
  
  public int getIntLocal (int lindex) {
    return getLocalVariable(lindex);
  }
  
  public int getIntLocal (int fr, String lname) {
    return getLocalVariable(fr, lname);
  }
  
  public int getIntLocal (int fr, int lindex) {
    return getLocalVariable(fr, lindex);
  }
  
  public int getIntReturnValue () {
    return peek();
  }
  
  public boolean isInterrupted (boolean resetStatus) {
    boolean ret = (getStatus() == INTERRUPTED);
    
    if (resetStatus) {
      // <2do> pcm is that true? check the specs
      setStatus(RUNNING);
    }
    
    return ret;
  }
  
  /**
   * return our internal thread number (order of creation)
   */
  public int getIndex () {
    return index;
  }

  /**
   * record what this thread is being blocked on.
   */
  void setLockRef (int objref) {
    assert ((lockRef == -1) || (lockRef == objref)) :
      "attempt to overwrite lockRef: " + vm.getDynamicArea().get(lockRef) +
      " with: " + vm.getDynamicArea().get(objref);
    
    lockRef = objref;    
  }
  
  /**
   * thread is not blocked anymore
   * needs to be public since we have to use it from INVOKECLINIT (during call skipping)
   */
  public void resetLockRef () {
    lockRef = -1;
  }
  
  public ElementInfo getLockObject () {
    if (lockRef == -1) {
      return null;
    } else {
      return vm.getDynamicArea().get(lockRef);
    }
  }
  
  /**
   * Returns the line number of the program counter of the top stack frame.
   */
  public int getLine () {
    if (top == null) {
      return -1;
    } else {
      return top.getLine();
    }
  }
  
  /**
   * Returns the line the thread is at.
   */
  public int getLine (int idx) {
    return frame(idx).getLine();
  }
  
  public String[] getLocalNames () {
    return top.getLocalVariableNames();
  }
  
  public String[] getLocalNames (int fr) {
    return frame(fr).getLocalVariableNames();
  }
  
  /**
   * Sets the value of a local variable.
   */
  public void setLocalVariable (int idx, int v, boolean ref) {
    topClone().setLocalVariable(idx, v, ref);
  }
  
  /**
   * Returns the value of a local variable in a particular frame.
   */
  public int getLocalVariable (int fr, int idx) {
    return frame(fr).getLocalVariable(idx);
  }
  
  /**
   * Returns the value of a local variable.
   */
  public int getLocalVariable (int idx) {
    return top.getLocalVariable(idx);
  }
  
  /**
   * Gets the value of a local variable from its name and frame.
   */
  public int getLocalVariable (int fr, String name) {
    return frame(fr).getLocalVariable(name);
  }
  
  /**
   * Gets the value of a local variable from its name.
   */
  public int getLocalVariable (String name) {
    return top.getLocalVariable(name);
  }
  
  /**
   * Checks if a local variable is a reference.
   */
  public boolean isLocalVariableRef (int idx) {
    return top.isLocalVariableRef(idx);
  }
  
  /**
   * Gets the type associated with a local variable.
   */
  public String getLocalVariableType (int fr, String name) {
    return frame(fr).getLocalVariableType(name);
  }
  
  /**
   * Gets the type associated with a local variable.
   */
  public String getLocalVariableType (String name) {
    return top.getLocalVariableType(name);
  }
  
  /**
   * Sets the number of locks held at the time of a wait.
   */
  public void setLockCount (int l) {
    if (threadData.lockCount != l) {
      threadDataClone().lockCount = l;
    }
  }
  
  /**
   * Returns the number of locks in the last wait.
   */
  public int getLockCount () {
    return threadData.lockCount;
  }
    
  public LinkedList<ElementInfo> getLockedObjects () {
    return lockedObjects;
  }
  
  public int[] getLockedObjectReferences () {
    int[] a = new int[lockedObjects.size()];
    int i=0;
    for (ElementInfo e : lockedObjects) {
      a[i++] = e.getIndex();
    }
    
    return a;
  }
  
  public long getLongLocal (String lname) {
    return getLongLocalVariable(lname);
  }
  
  public long getLongLocal (int lindex) {
    return getLongLocalVariable(lindex);
  }
  
  public long getLongLocal (int fr, String lname) {
    return getLongLocalVariable(fr, lname);
  }
  
  public long getLongLocal (int fr, int lindex) {
    return getLongLocalVariable(fr, lindex);
  }
  
  /**
   * Sets the value of a long local variable.
   */
  public void setLongLocalVariable (int idx, long v) {
    topClone().setLongLocalVariable(idx, v);
  }
  
  /**
   * Returns the value of a long local variable.
   */
  public long getLongLocalVariable (int fr, int idx) {
    return frame(fr).getLongLocalVariable(idx);
  }
  
  /**
   * Returns the value of a long local variable.
   */
  public long getLongLocalVariable (int idx) {
    return top.getLongLocalVariable(idx);
  }
  
  /**
   * Gets the value of a long local variable from its name.
   */
  public long getLongLocalVariable (int fr, String name) {
    return frame(fr).getLongLocalVariable(name);
  }
  
  /**
   * Gets the value of a long local variable from its name.
   */
  public long getLongLocalVariable (String name) {
    return top.getLongLocalVariable(name);
  }
  
  public long getLongReturnValue () {
    return longPeek();
  }
  
  /**
   * Returns the current method in the top stack frame.
   */
  public MethodInfo getMethod () {
    if (top != null) {
      return top.getMethodInfo();
    } else {
      return null;
    }
  }
  
  public boolean isInCtor () {
    // <2do> - hmm, if we don't do this the whole stack, we miss factored
    // init funcs
    MethodInfo mi = getMethod();
    if (mi != null) {
      return mi.isCtor();
    } else {
      return false;
    }
  }
  
  /**
   * Returns the method info of a specific stack frame.
   */
  public MethodInfo getMethod (int idx) {
    StackFrame sf = frame(idx);
    if (sf != null) {
      return sf.getMethodInfo();
    } else {
      return null;
    }
  }
  
  public String getName () {
    return threadData.name;
  }
  
  
  public ElementInfo getObjectLocal (String lname) {
    return list.ks.da.get(getLocalVariable(lname));
  }
  
  public ElementInfo getObjectLocal (int lindex) {
    return list.ks.da.get(getLocalVariable(lindex));
  }
  
  public ElementInfo getObjectLocal (int fr, String lname) {
    return list.ks.da.get(getLocalVariable(fr, lname));
  }
  
  public ElementInfo getObjectLocal (int fr, int lindex) {
    return list.ks.da.get(getLocalVariable(fr, lindex));
  }
  
  /**
   * Returns the object reference.
   */
  public int getObjectReference () {
    return threadData.objref;
  }
  
  public ElementInfo getObjectReturnValue () {
    return list.ks.da.get(peek());
  }
  
  public Object getOperandAttr () {
    return top.getOperandAttr();
  }
  
  public Object getOperandAttr (int opStackOffset) {
    return top.getOperandAttr(opStackOffset);
  }
  
  public void setOperandAttr (Object attr) {
    top.setOperandAttr(attr);
  }
  
  /**
   * Checks if the top operand is a reference.
   */
  public boolean isOperandRef () {
    return top.isOperandRef();
  }
  
  /**
   * Checks if an operand is a reference.
   */
  public boolean isOperandRef (int idx) {
    return top.isOperandRef(idx);
  }
  
  /**
   * Sets the program counter of the top stack frame.
   */
  public void setPC (Instruction pc) {
    topClone().setPC(pc);
  }
  
  /**
   * Returns the program counter of a stack frame.
   */
  public Instruction getPC (int i) {
    return frame(i).getPC();
  }
  
  /**
   * Returns the program counter of the top stack frame.
   */
  public Instruction getPC () {
    if (top != null) {
      return top.getPC();
    } else {
      return null;
    }
  }
  
  public ExceptionInfo getPendingException () {
    return pendingException;
  }
  
  /**
   * Returns true if this thread is either RUNNING or UNBLOCKED
   */
  public boolean isRunnable () {
    switch (threadData.status) {
    case RUNNING:
    case UNBLOCKED:
    case UNBLOCKED_INTERRUPTED:
      return true;
    default:
      return false;
    }
  }

  public boolean willBeRunnable () {
    switch (threadData.status) {
    case RUNNING:
    case UNBLOCKED:
    case UNBLOCKED_INTERRUPTED:
      return true;
    case TIMEOUT_WAITING: // it's not yet, but it will be at the time it gets scheduled
      return true;
    default:
      return false;
    }
  }
  
  public short getShortLocal (String lname) {
    return (short) getLocalVariable(lname);
  }
  
  public short getShortLocal (int lindex) {
    return (short) getLocalVariable(lindex);
  }
  
  public short getShortLocal (int fr, String lname) {
    return (short) getLocalVariable(fr, lname);
  }
  
  public short getShortLocal (int fr, int lindex) {
    return (short) getLocalVariable(fr, lindex);
  }
  
  public short getShortReturnValue () {
    return (short) peek();
  }
  
  /**
   * get the current stack trace of this thread
   * this is called during creation of a Throwable, hence we should skip
   * all throwable ctors in here
   * <2do> this is only a partial solution,since we don't catch exceptions
   * in Throwable ctors yet
   */
  public String getStackTrace () {
    StringBuffer sb = new StringBuffer(256);

    for (int i = topIdx; i >= 0; i--) {
      StackFrame sf = stack.get(i);
      MethodInfo mi = sf.getMethodInfo();
      
      if (mi.isCtor()){
        ClassInfo ci = mi.getClassInfo();
        if (ci.instanceOf("java.lang.Throwable")) {
          continue;
        }
      }

      sb.append(stack.get(i).getStackTrace());
      sb.append("\n");
    }
    
    return sb.toString();
  }
  
  /**
   * Updates the status of the thread.
   */
  public void setStatus (int newStatus) {
    int oldStatus = threadData.status;
    threadDataClone().status = newStatus;
    
    if (oldStatus != newStatus) {
      
      switch (newStatus) {
      case NEW:
        break; // Hmm, shall we report a thread object creation?
      case RUNNING:
        // nothing. the notifyThreadStarted has to happen from
        // Thread.start(), since the thread could have been blocked
        // at the time with a sync run() method
        break;
      case TERMINATED:
        vm.notifyThreadTerminated(this);
        break;
      case BLOCKED:
        vm.notifyThreadBlocked(this);
        break;
      case WAITING:
        vm.notifyThreadWaiting(this);
        break;
      case INTERRUPTED:
        vm.notifyThreadInterrupted(this);
        break;
      case NOTIFIED:
        vm.notifyThreadNotified(this);
        break;
      }
//System.out.println("@@ SET STATUS of " + getName() + " from " + statusName[oldStatus] + " to " + statusName[newStatus]);
    }
  }
  
  /**
   * Returns the current status of the thread.
   */
  public int getStatus () {
    return threadData.status;
  }
  
  /**
   * Returns the information necessary to store.
   *
   * <2do> pcm - not clear to me how lower stack frames can contribute to
   * a different threadinfo state hash - only the current one can be changed
   * by the executing method
   */
  public void dumpStoringData (IntVector v) {

  }
  
  public String getStringLocal (String lname) {
    return list.ks.da.get(getLocalVariable(lname)).asString();
  }
  
  public String getStringLocal (int lindex) {
    return list.ks.da.get(getLocalVariable(lindex)).asString();
  }
  
  public String getStringLocal (int fr, String lname) {
    return list.ks.da.get(getLocalVariable(fr, lname)).asString();
  }
  
  public String getStringLocal (int fr, int lindex) {
    return list.ks.da.get(getLocalVariable(fr, lindex)).asString();
  }
  
  public String getStringReturnValue () {
    return list.ks.da.get(peek()).asString();
  }
  
  /**
   * Sets the target of the thread.
   */
  public void setTarget (int t) {
    if (threadData.target != t) {
      threadDataClone().target = t;
    }
  }
  
  /**
   * Returns the object reference of the target.
   */
  public int getTarget () {
    return threadData.target;
  }
  
  /**
   * Returns the pointer to the object reference of the executing method
   */
  public int getThis () {
    return top.getThis();
  }
  
  public boolean isThis (ElementInfo r) {
    if (r == null) {
      return false;
    }
    
    if (top == null) {
      return false;
    }
    
    return getMethod().isStatic()
      ? false : r.getIndex() == getLocalVariable(0);
  }
  
  public boolean atInvoke (String mname) {
    if (top == null) {
      return false;
    }
    
    Instruction pc = getPC();
    
    if (!(pc instanceof InvokeInstruction)) {
      return false;
    }
    
    InvokeInstruction i = (InvokeInstruction) pc;
    
    return mname.equals(i.cname + "." + i.mname);
  }
    
  public boolean atMethod (String mname) {
    return top != null && getMethod().getCompleteName().equals(mname);
  }
  
  public boolean atPosition (int position) {
    if (top == null) {
      return false;
    } else {
      Instruction pc = getPC();
      return pc != null && pc.getPosition() == position;
    }
  }
  
  public boolean atReturn () {
    if (top == null) {
      return false;
    } else {
      Instruction pc = getPC();
      return pc instanceof ReturnInstruction;
    }
  }
  
  
  /**
   * reset any information that has to be re-computed in a backtrack
   * (i.e. hasn't been stored explicitly)
   */
  void resetVolatiles () {
    // resetting lock sets goes here
    lockedObjects = new LinkedList<ElementInfo>();
    
    // the ref of the object we are blocked on or waiting for
    lockRef = -1;
  }
  
  void addLockedObject (ElementInfo ei) {
    lockedObjects.add(ei);
    vm.notifyObjectLocked(this, ei);
  }
  
  void removeLockedObject (ElementInfo ei) {
    lockedObjects.remove(ei);
    vm.notifyObjectUnlocked(this, ei);
  }
  

  /**
   * Pops a set of values from the caller stack frame.
   */
  public void callerPop (int n) {
    frameClone(-1).pop(n);
  }
  
  /**
   * Clears the operand stack of all value.
   */
  public void clearOperandStack () {
    topClone().clearOperandStack();
  }
    
  public StackFrame[] cloneStack() {
    StackFrame[] sf = null;
    int sz = stack.size();
    
    // add a StackFrame if the current method is native
    // (we only use this in debuging and exception handling so far)
    
    // are we executing in a native method? If yes, it's not on the stack
    // and we have to add it on the fly (assuming this is called rather
    // infrequently as part of exception handling). Otherwise we really should
    // add a StackFrame for every native call
    MethodInfo nativeMth = env.getMethodInfo();
    if (nativeMth != null){
      sf = new StackFrame[sz + 1];
    } else {
      sf = new StackFrame[sz];
    }

    stack.toArray(sf);
    
    if (nativeMth != null){
      sf[sz] = (new StackFrame(nativeMth, null));
      // <2do> we probably should fill in the params/locals here
    }
    
    return sf;
  }
  
  /**
   * Returns the number of stack frames.
   */
  public int countStackFrames () {
    return stack.size();
  }
  
  int createStackTraceElement ( String clsName, String mthName, String fileName, int line) {
    DynamicArea da = DynamicArea.getHeap();
    
    ClassInfo ci = ClassInfo.getClassInfo("java.lang.StackTraceElement");
    int sRef = da.newObject(ci, this);
    
    ElementInfo  sei = da.get(sRef);
    sei.setReferenceField("clsName", da.newString(clsName, this));
    sei.setReferenceField("mthName", da.newString(mthName, this));
    sei.setReferenceField("fileName", da.newString(fileName, this));
    sei.setIntField("line", line);
    
    return sRef;
  }
    
  int countVisibleStackFrames() {
    int n = 0;
    int len = stack.size();
    for (int i = 0; i < len; i++) {
      if (!stack.get(i).isDirectCallFrame()) {
        n++;
      }
    }
    return n;
  }
  
  public int getStackTrace (int objref) {
    DynamicArea da = DynamicArea.getHeap();
    int stackDepth = stack.size();
    int i, j=0;
    int aRef;
    ElementInfo aei;
    
    int nFrames = 0;
    for (i = 0; i < stackDepth; i++) {
      if (!stack.get(i).isDirectCallFrame() && (stack.get(i).getThis() != objref) ) {
        nFrames++;
      }
    }
    
    // aexecuting in a native method? If yes, it's not on the stack, but we want to see it
    // (but be aware this might come from a native fillInStackTrace(), which we skip)
    MethodInfo nativeMth = env.getMethodInfo();
    if ((nativeMth != null) && !nativeMth.getName().equals("fillInStackTrace")){
      aRef = da.newArray("Ljava/lang/StackTraceElement;", nFrames+1, this);
      aei = da.get(aRef);
      
      aei.setElement( j++,
          createStackTraceElement(  nativeMth.getClassInfo().getName(),
                                    nativeMth.getName(),
                                    nativeMth.getClassInfo().getSourceFileName(), -1));
    } else {
      aRef = da.newArray("Ljava/lang/StackTraceElement;", nFrames, this);
      aei = da.get(aRef);
    }
    
    for (i = stackDepth - 1; i >= 0; i--) {
      // so we have to filter out two things: (1) direct calls, and (2) everything that
      // might come from the <init> chain of the Throwable (in case it gets explicitly
      // thrown from the bytecode). The latter case means the object reference of the method
      // is the same that got passed in as a parameter. It's rather unlikely an exception
      // object throws itself from within some of its instance methods (but its possible)
      if (!stack.get(i).isDirectCallFrame() && (stack.get(i).getThis() != objref)) {
        aei.setElement( j++,
                        createStackTraceElement( getCallStackClass(i), getCallStackMethod(i),
                                                 getCallStackFile(i), getCallStackLine(i)));
      }
    }

    return aRef;
  }
  
  void print (PrintWriter pw, String s) {
    if (pw != null){
      pw.print(s);
    } else {
      vm.print(s);
    }
  }
    
  public void printStackTrace (int objRef) {
    printStackTrace(null, objRef);
  }
  
  public void printPendingExceptionOn (PrintWriter pw) {
    if (pendingException != null) {
      printStackTrace( pw, pendingException.getExceptionReference());
    }
  }
  
  public void printStackTrace (PrintWriter pw, int objRef) {
    // 'env' usage is not ideal, since we don't know from what context we are called, and
    // hence the MJIEnv calling context might not be set (no Method or ClassInfo)
    // on the other hand, we don't want to re-implement all the MJIEnv accessor methods
    print(pw, env.getClassInfo(objRef).getName());
    
    int msgRef = env.getReferenceField(objRef,"detailMessage");
    if (msgRef != MJIEnv.NULL) {
      print(pw, ": ");
      print(pw, env.getStringObject(msgRef));
    }
    print(pw, "\n");
    
    int aRef = env.getReferenceField(objRef, "stackTrace"); // StackTrace[]
    if (aRef != MJIEnv.NULL) {
      int len = env.getArrayLength(aRef);
      for (int i=0; i<len; i++) {
        int sRef = env.getReferenceArrayElement(aRef, i);
        String clsName = env.getStringObject(env.getReferenceField(sRef, "clsName"));
        String mthName = env.getStringObject(env.getReferenceField(sRef, "mthName"));
        String fileName = env.getStringObject(env.getReferenceField(sRef, "fileName"));
        int line = env.getIntField(sRef, "line");
        print(pw, "\tat ");
        print(pw, clsName);
        print(pw, ".");
        print(pw, mthName);
        print(pw, "(");
        print(pw, fileName);
        print(pw, ":");
        
        if (line < 0){
          print(pw, "native");
        } else {
          print(pw, Integer.toString(line));
        }
        
        print(pw, ")");
        print(pw, "\n");
      }
    }
  }
    
  /**
   * Creates and throws an exception. This is what is used if the exception is
   * thrown by the VM (or a listener)
   */
  public Instruction createAndThrowException (ClassInfo ci, String details) {
    DynamicArea da = DynamicArea.getHeap();
    int         objref = da.newObject(ci, this);
    int         msgref = -1;
    
    if (!ci.isInitialized()) {
      ci.loadAndInitialize(this); // we don't call clinits for throwables (yet)
    }
    
    ElementInfo ei = da.get(objref);
    
    // <2do> pcm - this is not correct! We have to call a propper ctor
    // for the Throwable (for now, we just explicitly set the details)
    // but since this is not used with user defined exceptions (it's only
    // called from within the VM, i.e. with standard exceptions), we for
    // now skip the hassle of doing direct calls that would change the
    // call stack
    if (details != null) {
      msgref = da.newString(details, this);
      ei.setDeclaredReferenceField("detailMessage", "java.lang.Throwable", msgref);
    }
    
    // fill in the stacktrace (would be done by the ctor otherwise)
    int stackTrace = getStackTrace(objref);
    ei.setReferenceField("stackTrace", stackTrace);
    
    return throwException(objref);
  }
    
  /**
   * Creates an exception and throws it.
   */
  public Instruction createAndThrowException (String cname) {
    return createAndThrowException(ClassInfo.getClassInfo(cname), null);
  }
  
  public Instruction createAndThrowException (String cname, String details) {
    return createAndThrowException(ClassInfo.getClassInfo(cname), details);
  }
    
  /**
   * Duplicates a value on the top stack frame.
   */
  public void dup () {
    topClone().dup();
  }
  
  /**
   * Duplicates a long value on the top stack frame.
   */
  public void dup2 () {
    topClone().dup2();
  }
  
  /**
   * Duplicates a long value on the top stack frame.
   */
  public void dup2_x1 () {
    topClone().dup2_x1();
  }
  
  /**
   * Duplicates a long value on the top stack frame.
   */
  public void dup2_x2 () {
    topClone().dup2_x2();
  }
  
  /**
   * Duplicates a value on the top stack frame.
   */
  public void dup_x1 () {
    topClone().dup_x1();
  }
  
  /**
   * Duplicates a value on the top stack frame.
   */
  public void dup_x2 () {
    topClone().dup_x2();
  }
  
  public void skipInstructionLogging () {
    logInstruction = false;
  }
  
  /**
   * Execute next instruction.
   */
  public Instruction executeInstruction () {
    Instruction pc = getPC();
    SystemState ss = vm.getSystemState();
    KernelState ks = vm.getKernelState();
        
    // the default, might be changed by the insn depending on if it's the first
    // time we exec the insn, and whether it does its magic in the top (before break)
    // or bottom half (re-exec after break) of the exec 
    logInstruction = true;
    skipInstruction = false;
    MethodInfo mth = getMethod(); // it's gone after executing a RETURN, so get it here

    if (log.isLoggable(Level.FINE)) {
      log.fine( pc.getMethod().getCompleteName() + " " + pc.getPosition() + " : " + pc);
    }

    // this is the pre-execution notification, during which a listener can perform
    // on-the-fly instrumentation or even replace the instruction alltogether
    vm.notifyExecuteInstruction(this, pc);
    
    if (!skipInstruction) {
      // execute the next bytecode
      nextPc = pc.execute(ss, ks, this);
    }
      
    if (logInstruction) {
      String src = mth.getSourceFileName();
      Step step = new Step(src, getLine(), pc);
      ss.recordExecutionStep( step);
    }
      
    // we did not return from the last frame stack
    if (top != null) {
      setPC(nextPc);
    }
    
    // here we have our bytecode exec observation point
    // (note we have to do this after we have set the pc, since a listener
    // might come back to do checks like isRunnable)
    vm.notifyInstructionExecuted(this, pc, nextPc);
    
    return nextPc;
  }
  
  /**
   * skip the next bytecode. To be used by listeners to on-the-fly replace
   * instructions. Note that you have to explicitly call setNextPc() in this case
   */
  public void skipInstruction () {
    skipInstruction = true;
  }
  
  /**
   * explicitly set the next insn to execute. To be used by listeners that
   * replace bytecode exec (during 'executeInstruction' notification)
   */
  public void setNextPC (Instruction insn) {
    nextPc = insn;
  }
  
  /**
   * Executes a method call. Be aware that it executes the whole method as one atomic
   * step. Arguments have to be already on the provided stack
   * 
   * <2do> this is deprecated and should be considered defunct. We just leave it in here
   * as a last measure for stuff that really can't be attached to a Instruction object
   * (like finalization), i.e. highly specialized and restricted operations. As soon as
   * we hit a ChoicePoint, we're lost (this is not backtrackable)
   */
  public void executeMethod (DirectCallStackFrame frame) {

    pushFrame(frame);
    int    depth = countStackFrames();
    Instruction pc = frame.getPC();
 
    frame.getMethodInfo().execute(this);
 
    while (depth < countStackFrames()) {
      Instruction nextPC = executeInstruction();
                  
      if (nextPC == pc) {
        // BANG - we can't have CG's inside of atomic exec
        // should be rather a Property violation
        throw new JPFException("choice point in sync executed method: " + frame);
      } else {
        pc = nextPC;
      }
    }
  }
     
  // we just got our last stack frame popped, so it's time to close down
  public void finish () {
    setStatus(TERMINATED);
    
    // give the thread a chance to clean up
    /* removed?  -peterd
    ClassInfo ci = ClassInfo.getClassInfo("java.lang.Thread");
    MethodInfo exitMth = ci.getMethod("exit()V", false);
    if (exitMth != null) {
      //push(threadData.objref, true);
      //executeMethod(exitMth);
    }
    */
    
    // need to own the lock before we can notify
    int     objref = getObjectReference();
    ElementInfo ei = list.ks.da.get(objref);
    
    ei.lock(this);
    ei.notifiesAll();
    ei.unlock(this);
    
    // stack is gone, so reachability might change
    vm.activateGC();
  }

  public void hash (HashData hd) {
    threadData.hash(hd);
    
    for (int i = 0, l = stack.size(); i < l; i++) {
      stack.get(i).hash(hd);
    }
  }
  
  public void interrupt () {
    if (getStatus() != WAITING) {
      return;
    }
    
    setStatus(INTERRUPTED);
  }
    
  public void log () {
    Debug.println(Debug.MESSAGE,
                  "TH#" + index + " #" + threadData.target + " #" +
                    threadData.objref + " " + threadData.status);
    
    for (int i = 0, l = stack.size(); i < l; i++) {
      stack.get(i).log(i);
    }
  }
  
  /**
   * Peeks the top long value from the top stack frame.
   */
  public long longPeek () {
    return top.longPeek();
  }
  
  /**
   * Peeks a long value from the top stack frame.
   */
  public long longPeek (int n) {
    return top.longPeek(n);
  }
  
  /**
   * Pops the top long value from the top stack frame.
   */
  public long longPop () {
    return topClone().longPop();
  }
  
  /**
   * Pushes a long value of the top stack frame.
   */
  public void longPush (long v) {
    topClone().longPush(v);
  }
  
  
  /**
   * mark all objects during gc phase1 which are reachable from this threads
   * root set (Thread object, Runnable, stack)
   * @aspects: gc
   */
  void markRoots () {
    DynamicArea        heap = DynamicArea.getHeap();
    
    // 1. mark the Thread object itself
    heap.markThreadRoot(threadData.objref, index);
    
    // 2. and its runnable
    if (threadData.target != -1) {
      heap.markThreadRoot(threadData.target,index);
    }
    
    // 3. now all references on the stack
    for (int i = 0, l = stack.size(); i < l; i++) {
      stack.get(i).markThreadRoots(index);
    }
  }
    
  /**
   * Adds a new stack frame for a new called method.
   */
  public void pushFrame (StackFrame frame) {
    topIdx = stack.size();
    stack.add(frame);
    top = frame;
    markChanged(topIdx);
  }
  
  
  /**
   * Peeks the top value from the top stack frame.
   */
  public int peek () {
    if (top != null) {
      return top.peek();
    } else {
      // <?> not really sure what to do here, but if the stack is gone, so is the thread
      return -1;
    }
  }
  
  /**
   * Peeks a int value from the top stack frame.
   */
  public int peek (int n) {
    if (top != null) {
      return top.peek(n);
    } else {
      // <?> see peek()
      return -1;
    }
  }
  
  /**
   * Pops the top value from the top stack frame.
   */
  public int pop () {
    if (top != null) {
      return topClone().pop();
    } else {
      // <?> see peek()
      return -1;
    }
  }
  
  /**
   * Pops a set of values from the top stack frame.
   */
  public void pop (int n) {
    if (top != null) {
      topClone().pop(n);
    }
  }
  
  /**
   * Removes a stack frame.
   */
  public boolean popFrame () {
    //if (getMethod().isAtomic()) {
      //list.ks.clearAtomic();
    //}
    
    if (top.hasAnyRef()) {
      vm.getSystemState().activateGC();
    }
        
    if (topIdx == 0) {
      finish();
    }
    
    if (top instanceof DirectCallStackFrame) {
      returnedDirectCall = (DirectCallStackFrame) top;
    } else {
      returnedDirectCall = null;
    }
    
    stack.remove(topIdx);
    
    markChanged(topIdx);
    
    topIdx--;

    if (topIdx >= 0) {
      top = stack.get(topIdx);
      return true;
    } else {
      top = null;
      return false;
    }
  }

  /**
   * NOTE - this has to be called *after* the returning frame was popped
   */
  public Instruction getReturnFollowOnPC () {
    if (returnedDirectCall != null) {
      Instruction next = returnedDirectCall.getNextPC();
      if (next != null) {
        return next;
      } else {
        return top.getPC();
      }
    } else {
      return top.getPC().getNext();
    }
  }
  
  public boolean isResumedInstruction (Instruction insn) {
    return (returnedDirectCall != null) && (returnedDirectCall.getNextPC() == insn);
  }
  
  public DirectCallStackFrame getReturnedDirectCall () {
    return returnedDirectCall;
  }
  
  public String getStateDescription () {
    StringBuffer sb = new StringBuffer();
    sb.append("thread index=");
    sb.append(index);
    sb.append(",");
    sb.append(threadData.getFieldValues());
    
    return sb.toString();
  }
  
  public String getStatusName () {
    return statusName[getStatus()];
  }
  
  /**
   * Prints the content of the stack.
   */
  public void printStackContent () {
    for (int i = topIdx; i >= 0; i--) {
      stack.get(i).printStackContent();
    }
  }
  
  /**
   * Prints the trace of the stack.
   */
  public void printStackTrace () {
    for (int i = topIdx; i >= 0; i--) {
      stack.get(i).printStackTrace();
    }
  }
  
  /**
   * Pushes a value on the top stack frame.
   */
  public void push (int v, boolean ref) {
    topClone().push(v, ref);
  }
  
  /**
   * Removes the arguments of a method call.
   */
  public void removeArguments (MethodInfo mi) {
    int i = mi.getArgumentsSize();
    
    if (i != 0) {
      pop(i);
    }
  }
  
  /**
   * Swaps two entry on the stack.
   */
  public void swap () {
    topClone().swap();
  }
  
  /**
   * unwind stack frames until we find a matching handler for the exception object
   */
  public Instruction throwException (int exceptionObjRef) {
    DynamicArea da = DynamicArea.getHeap();
    ElementInfo ei = da.get(exceptionObjRef);
    ClassInfo ci = ei.getClassInfo();
    MethodInfo mi;
    Instruction insn;
    int nFrames = countStackFrames();
    int i, j;

    //System.out.println("## ---- got: " + ci.getName());

    // we don't have to store the stacktrace explicitly anymore, since that is now
    // done in the Throwable ctor (more specifically the native fillInStackTrace)
    pendingException = new ExceptionInfo(this, ei);
        
    vm.notifyExceptionThrown(this, ei);
    
    if (!haltOnThrow) {
      for (j=0; j<nFrames; j++) {
        mi = getMethod();
        insn = getPC();
        
        ExceptionHandler[] exceptions = mi.getExceptions();
//System.out.println("## unwinding to: " + mi.getClassInfo().getName() + "." + mi.getUniqueName());
        // this is the point where we can check for
        // "articifial" stack frames. There is no corresponding
        // INVOKE instruction in the frame underneath
        if ( (j==0) || (insn instanceof InvokeInstruction)) {
          int p = insn.getPosition();
                    
          if (exceptions != null) {
            // checks the exception caught in order
            for (i = 0; i < exceptions.length; i++) {
              ExceptionHandler eh = exceptions[i];
              
              // if it falls in the right range
              if ((p >= eh.getBegin()) && (p < eh.getEnd())) {
                String en = eh.getName();
//System.out.println("## checking: " + ci.getName() + " handler: " + en + " depth: " + stack.size());
                
                // checks if this type of exception is caught here (null means 'any')
                if ((en == null) || ci.instanceOf(en)) {
//System.out.println("## handle");
                  // clears all the operand stack
                  clearOperandStack();
                  
                  // pushes the exception on the stack instead
                  push(exceptionObjRef, true);
                  
                  // jumps to the exception handler
                  Instruction startOfHandlerBlock = mi.getInstructionAt(eh.getHandler());
                  setPC(startOfHandlerBlock); // set! we might be in a isDeterministic / isRunnable

                  pendingException = null; // handled, no need to keep it
                  
                  return startOfHandlerBlock;
                }
              }
            }
          }
          
          if ("<clinit>".equals(mi.getName())) {
//System.out.println("## bail");
            // we are done here, nobody can handle this - <clinits> don't nest, there is no calling
            // context but we should maybe wrap it up into an ExceptionInInitializerError (even though
            // that makes it harder to read, and the indirection is not buying us anything)
            break;
          }
          
        } else { // ! (insn instanceof InvokeInstruction)
        }
        
        // that takes care of releasing locks
        // (which interestingly enough seem to be the compilers responsibility now)
        mi.leave(this);
        
        // remove a frame
        popFrame();
      }
      
      // we keep the thread alive to ease post mortem debugging
      // <2do> really just required if we check for uncaught exceptions,
      // but we always do
      //finish();  // toast this thread
    }

//System.out.println("## unhandled!");

    // Ok, I finally made my peace with UncaughtException - it can be called from various places,
    // including the VM (<clinit>, finalizer) and we can't rely on that all these locations check
    // for pc == null. Even if they would, at this point there is nothing to do anymore, get to the
    // NoUncaughtProperty reporting as quickly as possible, since chances are we would be even
    // obfuscating the problem
    NoUncaughtExceptionsProperty.setExceptionInfo(pendingException);
    throw new UncaughtException(this, exceptionObjRef);
  }
    
  public void replaceStackFrames(Iterable<StackFrame> iter) {
    stack.clear();
    for (StackFrame sf : iter) {
      stack.add(sf);
    }
    topIdx = stack.size() - 1;
    if (topIdx >= 0) {
      top = stack.get(topIdx); 
    } else {
      top = null;
    }
  }
    
  /**
   * Returns a clone of the thread data. To be called every time we change some ThreadData field
   * (which unfortunately includes lock counts, hence this should be changed)
   */
  protected ThreadData threadDataClone () {
    if (tdChanged) {
      // already cloned, so we don't have to clone
    } else {
      // reset, so that next storage request would recompute tdIndex
      markTdChanged();
      list.ks.changed();
    
      threadData = threadData.clone();
    }
    
    return threadData;
  }
  
  public void restoreThreadData(ThreadData td) {
    threadData = td;
  }
  
    
  /**
   * execute a step using on-the-fly partial order reduction
   */
  protected boolean executeStep (SystemState ss) throws JPFException {
    Instruction pc = getPC();
    Instruction nextPc = null;
        
    if (currentThread != this) {
      vm.notifyThreadScheduled(this);
      currentThread = this;
    }

    // this constitutes the main transition loop. It gobbles up
    // insns until there either is none left anymore in this thread,
    // or it didn't execute (which indicates the insn registered a CG for
    // subsequent invocation)
    isFirstStepInsn = true; // so that potential CG generators know
    do {
      //for debugging locks:  -peterd
      //vm.ss.ks.da.verifyLockInfo();
      nextPc = executeInstruction();
      //vm.ss.ks.da.verifyLockInfo();
      
      if ((nextPc == pc) || ss.isIgnored()) {
        // shortcut break if there was no progress (a ChoiceGenerator was created)
        // or if the state is explicitly set as ignored 
        break;
      } else {
        pc = nextPc;
      }
      isFirstStepInsn = false;
    } while (pc != null);
               
    return true;
  }
  
  /**
   * request a reschedule no matter what the next insn is
   * (can be used by listeners who directly modify thread states)
   */
  public void yield () {
    yield = true;
  }
  
  public boolean hasOtherRunnables () {
    return list.hasOtherRunnablesThan(this);
  }

  protected void markUnchanged() {
    hasChanged.clear();
    tdChanged = false;
  }
  
  protected void markChanged(int idx) {
    hasChanged.set(idx);
    list.ks.changed();
  }

  protected void markTdChanged() {
    tdChanged = true;
    list.ks.changed();
  }
  
  /**
   * Returns a specific stack frame.
   */
  protected StackFrame frame (int idx) {
    if (idx < 0) {
      idx += topIdx;
    }
    
    return stack.get(idx);
  }
  
  /**
   * Returns a clone of a specific stack frame.
   */
  protected StackFrame frameClone (int i) {
    if (i < 0) {
      i += topIdx;
    } else if (i == topIdx) {
      return topClone();
    }
    
    if (hasChanged.get(i)) {
      return stack.get(i);
    }
    // else
    markChanged(i);
    
    StackFrame clone = stack.get(i).clone();
    stack.set(i, clone);
    return clone;
  }
  
  /**
   * Returns a clone of the top stack frame.
   */
  protected StackFrame topClone () {
    if (!hasChanged.get(topIdx)) {
      markChanged(topIdx);
      top = top.clone();
      stack.set(topIdx, top);
    }
    return top;
  }
  
  /**
   * Returns the top stack frame.
   */
  public StackFrame top () {
    return top;
  }
  
  public String toString() {
    return "ThreadInfo [name=" + getName() + ",index=" + index + ']';
  }
  
  void setDaemon (boolean isDaemon) {
    threadDataClone().isDaemon = isDaemon;
  }
  
  boolean isDaemon () {
    return threadData.isDaemon;
  }
  
  MJIEnv getMJIEnv () {
    return env;
  }
  
  void setName (String newName) {
    threadDataClone().name = newName;
    
    // see 'setPriority()', only that it's more serious here, because the
    // java.lang.Thread name is stored as a char[]
  }
  
  void setPriority (int newPrio) {
    if (threadData.priority != newPrio) {
      threadDataClone().priority = newPrio;
            
      // note that we don't update the java.lang.Thread object, but
      // use our threadData value (which works because the object
      // values are just used directly from the Thread ctors (from where we pull
      // it out in our ThreadInfo ctor), and henceforth only via our intercepted
      // native getters
    }
  }
  
  int getPriority () {
    return threadData.priority;
  }
  
  /**
   * this is the method that factorizes common Thread object initialization
   * (get's called by all ctors).
   * BEWARE - it's hidden magic (undocumented), and should be replaced by our
   * own Thread impl at some point
   */
  void init (int rGroup, int rRunnable, int rName, long stackSize,
             boolean setPriority) {
    DynamicArea da = JVM.getVM().getDynamicArea();
    ElementInfo ei = da.get(rName);
    
    threadDataClone();
    threadData.name = ei.asString();
    threadData.target = rRunnable;
    //threadData.status = NEW; // should not be neccessary
    
    // stackSize and setPriority are only used by native subsystems
  }

  public Iterator<StackFrame> iterator () {
    return stack.iterator();
  }

  /**
   * Comparison for sorting based on index.
   */
  public int compareTo (ThreadInfo that) {
    return this.index - that.index;
  }
}