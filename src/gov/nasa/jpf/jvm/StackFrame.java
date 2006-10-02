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

import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.jvm.bytecode.Instruction;
import gov.nasa.jpf.util.Debug;
import gov.nasa.jpf.util.HashData;

import org.apache.bcel.Constants;


/**
 * Describes a stack frame.
 *
 * implementation is based on the fact that each Java method has a fixed size
 * operand stack (overrun actually checked by a real VM), and the heuristics that
 *  (a) stack / local operations are frequent
 *  (b) stack / local sizes are typically small (both < 10)
 * hence a BitSet is not too useful
 */
public class StackFrame implements Constants {
  protected int top;   /** top index of the operand stack (NOT size) */

  protected int thisRef = -1;  /** local[0] can change, but we have to keep 'this' */
  
  protected int[] operands;            /** the operand stack */
  protected boolean[] isOperandRef;    /** which operand slots hold references */
  
  /** This array can be used to store attributes (e.g. variable names) for
   * operands. We don't do anything with this except of preserving it (across
   * dups etc.), so it's pretty much up to the VM listeners what's stored
   */
  protected Object[] operandAttr;
  
  protected int[] locals;               /** the local variables */
  protected boolean[] isLocalRef;       /** which local slots hold references */
  
  protected Instruction pc;             /** the next insn to execute (program counter) */

  protected MethodInfo mi;              /** which method is executed in this frame */

  
  /**
   * Creates a new stack frame for a given method.
   * 'isDirect' specifies if this method was called directly by the VM, i.e. there is
   * no corresponding INVOKE insn in the underlying stack frame (for instance, that's
   * important to know for handling return values and computing the next pc)
   * 'caller' is the calling stack frame (if any)
   */  
  public StackFrame (MethodInfo m, StackFrame caller) {
    mi = m;
    pc = mi.getInstruction(0);
    
    int nOperands = mi.getMaxStack();
    operands = new int[nOperands];
    isOperandRef = new boolean[nOperands];
    operandAttr = new Object[nOperands];
    top = -1;  // index, not size!

    int nargs = mi.getArgumentsSize();
    int nlocals = (pc == null) ? nargs : mi.getMaxLocals();
    locals = new int[nlocals];
    isLocalRef = new boolean[nlocals];

    // copy the args, if any
    if (nargs > 0 && (caller != null)) {
      int[] a = caller.operands;
      boolean[] r = caller.isOperandRef;
      
      for (int i=0, j=caller.top-nargs+1; i<nargs; i++, j++) {
        locals[i] = a[j];
        isLocalRef[i] = r[j];
      }
      
      if (!mi.isStatic()) { // according to the spec, this is guaranteed upon entry
        thisRef = locals[0];
      }
    }
  }

  public StackFrame (MethodInfo m, int objRef) {
    this(m, null);
    
    // maybe we should check here if this is an instance method
    
    thisRef = objRef;
    
    locals[0] = thisRef;
    isLocalRef[0] = true;
  }
  
  /**
   * Creates an empty stack frame. Used by clone.
   */
  protected StackFrame () {
  }
    
  /**
   * return the object reference for an instance method to be called (we are still in the
   * caller's frame). This only makes sense after all params have been pushed, before the
   * INVOKEx insn is executed
   */
  public int getCalleeThis (MethodInfo mi) {
    return getCalleeThis(mi.getArgumentsSize());
  }

  /**
   * return reference of called object in the context of the caller
   * (i.e. we are in the caller frame)
   */
  public int getCalleeThis (int size) {
    // top is the topmost index
    int i = size-1;
    if (top < i) {
      return -1;
    }

    return operands[top-i];
  }

  public ClassInfo getClassInfo () {
    return mi.getClassInfo();
  }

  public String getClassName () {
    return mi.getClassInfo().getName();
  }
  
  public String getSourceFile () {
    return mi.getClassInfo().getSourceFileName();
  }
  
  public boolean isDirectCallFrame () {
    return false;
  }
  
  // gets and sets some derived information
  public int getLine () {
    return mi.getLineNumber(pc);
  }

  public void setOperandAttr (Object o) {
    operandAttr[top] = o;
  }
  
  public int getAbsOperand(int idx) {
    return operands[idx];
  }
  
  public boolean isAbsOperandRef(int idx) {
    return isOperandRef[idx];
  }
  
  public Object getOperandAttr () {
    if (top >=0 ){
      return operandAttr[top];
    } else {
      return null;
    }
  }

  public Object getOperandAttr (int offset) {
    if (top >= offset) {
      return operandAttr[top-offset];
    } else {
      return null;
    }
  }
  
  public void setLocalVariable (int index, int v, boolean ref) {
    boolean activateGc = (isLocalRef[index] && (locals[index] != -1));

    locals[index] = v;
    isLocalRef[index] = ref;

    if (ref) {
      if (v != -1) activateGc = true;
    }

    if (activateGc) {
        JVM.getVM().getSystemState().activateGC();
    }
  }

  public int getLocalVariable (int i) {
    return locals[i];
  }

  public int getLocalVariable (String name) {
    return getLocalVariable(getLocalVariableOffset(name));
  }
  
  public int getLocalVariableCount() {
    return locals.length;
  }

  public String[] getLocalVariableNames () {
    return mi.getLocalVariableNames();
  }

  public boolean isLocalVariableRef (int idx) {
    return isLocalRef[idx];
  }

  public String getLocalVariableType (String name) {
    String[] lNames = mi.getLocalVariableNames();
    String[] lTypes = mi.getLocalVariableTypes();

    if ((lNames != null) && (lTypes != null)) {
      for (int i = 0, l = lNames.length; i < l; i++) {
        if (name.equals(lNames[i])) {
          return lTypes[i];
        }
      }
    }

    throw new JPFException("Local variable " + name + " not found");
  }

  int[] getLocalVariables () {
    return locals;
  }

  public void setLongLocalVariable (int index, long v) {

    // local slots shouldn't change type, and 'long' can't be a reference,
    // hence no isLocalRef update or gc request
    
    locals[index + 1] = Types.loLong(v);
    locals[index] = Types.hiLong(v);
  }

  public long getLongLocalVariable (int i) {
    return Types.intsToLong(locals[i + 1], locals[i]);
  }

  public long getLongLocalVariable (String name) {
    return getLongLocalVariable(getLocalVariableOffset(name));
  }

  public MethodInfo getMethodInfo () {
    return mi;
  }

  public String getMethodName () {
    return mi.getName();
  }

  public boolean isOperandRef (int idx) {
    return isOperandRef[top-idx];
  }

  public boolean isOperandRef () {
    return isOperandRef[top];
  }

  public void setPC (Instruction newpc) {
    pc = newpc;
  }

  public Instruction getPC () {
    return pc;
  }

  public int getTopPos() {
    return top;
  }
  
  public String getStackTrace () {
    StringBuffer sb = new StringBuffer(128);
    sb.append("\tat ");
    sb.append(mi.getClassInfo().getName());
    sb.append(".");
    sb.append(mi.getName());

    if (pc != null) {
      sb.append("(");
      sb.append(mi.getClassInfo().getSourceFileName());
      sb.append(":");
      sb.append(getLine());
      sb.append(")");
    } else {
      sb.append("(Native Method)");
    }
    //sb.append('\n');

    return sb.toString();
  }

  /**
   * if this is an instance method, return the reference of the corresponding object
   * (note this only has to be in slot 0 upon entry)
   */
  public int getThis () {
    return thisRef;
  }

  // stack operations
  public void clearOperandStack () {
    top = -1;
  }

  // functions to handle exceptions
  // creates a clone of the stack frame
  protected void copy (StackFrame sf) {

    int len = sf.operands.length;
    
    operands = new int[len];
    System.arraycopy(sf.operands,0,operands,0,len);

    isOperandRef = new boolean[len];
    System.arraycopy( sf.isOperandRef, 0, isOperandRef, 0, len);

    operandAttr = new Object[len];
    System.arraycopy(sf.operandAttr, 0, operandAttr, 0, len);
    
    top = sf.top;

    len = sf.locals.length;
    locals = new int[len];
    System.arraycopy(sf.locals, 0, locals, 0, len);

    isLocalRef = new boolean[len];
    System.arraycopy(sf.isLocalRef, 0, isLocalRef, 0, len);
        
    pc = sf.pc;
    mi = sf.mi;

    thisRef = sf.thisRef;
  }

  public StackFrame clone () {
    StackFrame sf = new StackFrame();
    sf.copy(this);
    return sf;
  }
  
  // all the dupses don't have any GC side effect (everything is already
  // on the stack), so skip the GC requests associated with push()/pop()
  
  public void dup () {
    // .. A => .. A.A
    int t= top;
    top++;
    
    operands[top] = operands[t];
    isOperandRef[top] = isOperandRef[t];
    operandAttr[top] = operandAttr[t];
  }

  public void dup2 () {
    // .. A B => .. A B.A B
    int td = top+1;
    int ts = top-1;
    operands[td] = operands[ts];
    isOperandRef[td] = isOperandRef[ts];
    operandAttr[td] = operandAttr[ts];
    
    td++; ts++;
    operands[td] = operands[ts];
    isOperandRef[td] = isOperandRef[ts];
    operandAttr[td] = operandAttr[ts];
    
    top = td;
  }

  public void dup2_x1 () {
    // .. A B C => .. B C A.B C
    
    int b, c;
    boolean bRef, cRef;
    Object bAnn, cAnn;
    int ts, td;
    
    // duplicate B
    ts = top-1; td = top+1;
    operands[td] = b = operands[ts];
    isOperandRef[td] = bRef = isOperandRef[ts];
    operandAttr[td] = bAnn = operandAttr[ts];
    
    // duplicate C
    ts=top; td++;
    operands[td] = c = operands[ts];
    isOperandRef[td] = cRef = isOperandRef[ts];
    operandAttr[td] = cAnn = operandAttr[ts];

    // shuffle A
    ts=top-2; td=top;
    operands[td] = operands[ts];
    isOperandRef[td] = isOperandRef[ts];
    operandAttr[td] = operandAttr[ts];
    
    // shuffle B
    td = ts;
    operands[td] = b;
    isOperandRef[td] = bRef;
    operandAttr[td] = bAnn;
    
    // shuffle C
    td++;
    operands[td] = c;
    isOperandRef[td] = cRef;
    operandAttr[td] = cAnn;
    
    top += 2;
  }

  public void dup2_x2 () {
    // .. A B C D => .. C D A B.C D
    
    int c, d;
    boolean cRef, dRef;
    Object cAnn, dAnn;
    int ts, td;
    
    // duplicate C
    ts = top-1; td = top+1;
    operands[td] = c = operands[ts];
    isOperandRef[td] = cRef = isOperandRef[ts];
    operandAttr[td] = cAnn = operandAttr[ts];
    
    // duplicate D
    ts=top; td++;
    operands[td] = d = operands[ts];
    isOperandRef[td] = dRef = isOperandRef[ts];
    operandAttr[td] = dAnn = operandAttr[ts];

    // shuffle A
    ts = top-3; td = top-1;
    operands[td] = operands[ts];
    isOperandRef[td] = isOperandRef[ts];
    operandAttr[td] = operandAttr[ts];
    
    // shuffle B
    ts++; td = top;
    operands[td] = operands[ts];
    isOperandRef[td] = isOperandRef[ts];
    operandAttr[td] = operandAttr[ts];
    
    // shuffle C
    td=ts;
    operands[td] = c;
    isOperandRef[td] = cRef;
    operandAttr[td] = cAnn;
    
    // shuffle C
    td++;
    operands[td] = d;
    isOperandRef[td] = dRef;
    operandAttr[td] = dAnn;
    
    top += 2;
  }

  public void dup_x1 () {
    // .. A B => .. B A.B
    
    int b;
    boolean bRef;
    Object bAnn;
    int ts, td;
    
    // duplicate B
    ts = top; td = top+1;
    operands[td] = b = operands[ts];
    isOperandRef[td] = bRef = isOperandRef[ts];
    operandAttr[td] = bAnn = operandAttr[ts];
    
    // shuffle A
    ts = top-1; td = top;
    operands[td] = operands[ts];
    isOperandRef[td] = isOperandRef[ts];
    operandAttr[td] = operandAttr[ts];
    
    // shuffle B
    td = ts;
    operands[td] = b;
    isOperandRef[td] = bRef;
    operandAttr[td] = bAnn;
    
    top++;
  }

  public void dup_x2 () {
    // .. A B C => .. C A B.C
    
    int c;
    boolean cRef;
    Object cAnn;
    int ts, td;
    
    // duplicate C
    ts = top; td = top+1;
    operands[td] = c = operands[ts];
    isOperandRef[td] = cRef = isOperandRef[ts];
    operandAttr[td] = cAnn = operandAttr[ts];
    
    // shuffle B
    ts=top-1; td=top;
    operands[td] = operands[ts];
    isOperandRef[td] = isOperandRef[ts];
    operandAttr[td] = operandAttr[ts];
    
    // shuffle A
    td=ts; ts--;
    operands[td] = operands[ts];
    isOperandRef[td] = isOperandRef[ts];
    operandAttr[td] = operandAttr[ts];

    // shuffle C
    td = ts;
    operands[td] = c;
    isOperandRef[td] = cRef;
    operandAttr[td] = cAnn;
    
    top++;
  }

  
  // <2do> pcm - I assume this compares snapshots, not types. Otherwise it
  // would be pointless to compare stack/local values
  public boolean equals (Object object) {
    // casts to stack frame
    StackFrame sf = (StackFrame) object;

    // compares the program counter REFERENCES
    // the code is statically read into the vm so the same
    // chunk of code means the same reference
    if (pc != sf.pc) {
      return false;
    }

    if (mi != sf.mi) {
      return false;
    }

    // compare the locals
    int[] l = sf.locals;
    boolean[] lr = sf.isLocalRef;
    int   nlocals = locals.length;

    if (nlocals != l.length) {
      return false;
    }
    for (int idx = 0; idx < nlocals; idx++) {
      if ((locals[idx] != l[idx]) || (isLocalRef[idx] != lr[idx])) {
        return false;
      }
    }

    // compare the operand stacks
    int[] o = sf.operands;
    boolean[] or = sf.isOperandRef;

    if (top != sf.top) {
      return false;
    }
    for (int idx = 0; idx <= top; idx++) {
      if ((operands[idx] != o[idx]) || (isOperandRef[idx] != or[idx]) ) {
        return false;
      }
    }

    return true;
  }

  public boolean hasAnyRef () {
    for (int i=0; i<=top; i++) {
      if (isOperandRef[i]) {
        return true;
      }
    }
    
    for (int i = 0, l = locals.length; i < l; i++) {
      if (isLocalRef[i]) {
        return true;
      }
    }

    return false;
  }

  public void hash (HashData hd) {
    for (int i = 0, l = locals.length; i < l; i++) {
      hd.add(locals[i]);
    }

    for (int i=0; i<=top; i++) {
      hd.add(operands[i]);
    }
  }

  // computes an hash code for the hash table
  // the default hash code is different for each object
  // we need to redifine it to make the hash table work
  public int hashCode () {
    HashData hd = new HashData();

    hash(hd);

    return hd.getValue();
  }

  public void log (int id) {
    Debug.print(Debug.MESSAGE, "  SF#" + id + " S(");

    for (int i = 0; i <=top; i++) {
      Debug.print(Debug.MESSAGE, " ");

      if (isOperandRef[i]) {
        Debug.print(Debug.MESSAGE, "#");
      }

      Debug.print(Debug.MESSAGE, operands[i] + "");
    }

    Debug.print(Debug.MESSAGE, " ) L(");

    for (int i = 0; i < locals.length; i++) {
      int j = locals[i];
      Debug.print(Debug.MESSAGE, " ");

      if (isLocalRef[i]) {
        Debug.print(Debug.MESSAGE, "#");
      }

      Debug.print(Debug.MESSAGE, j + "");
    }

    Debug.print(Debug.MESSAGE, " ) ");

    if (mi == null) {
      Debug.print(Debug.MESSAGE, "null");
    } else {
      Debug.print(Debug.MESSAGE, mi.getCompleteName());
    }

    Debug.print(Debug.MESSAGE, ":");

    if (pc == null) {
      Debug.println(Debug.MESSAGE, "null");
    } else {
      Debug.println(Debug.MESSAGE, pc.getPosition() + "  " + pc);
    }
  }

  /**
   * mark all objects reachable from local or operand stack positions containing
   * references. Done during phase1 marking of threads (the stack is one of the
   * Thread gc roots)
   * @aspects: gc
   */
  public void markThreadRoots (int tid) {
    DynamicArea heap = DynamicArea.getHeap();

    for (int i=0; i<= top; i++) {
      if (isOperandRef[i]) {
        heap.markThreadRoot(operands[i], tid);
      }
    }

    for (int i = 0, l = locals.length; i < l; i++) {
      if (isLocalRef[i]) {
        heap.markThreadRoot(locals[i], tid);
      }
    }
  }
  
  /**
   * this includes locals and pc
   */
  public void printStackContent () {
    Debug.print(Debug.ERROR, "\tat ");
    Debug.print(Debug.ERROR, mi.getCompleteName());

    if (pc != null) {
      Debug.println(Debug.ERROR, ":" + pc.getPosition());
    } else {
      Debug.println(Debug.ERROR);
    }

    Debug.println(Debug.ERROR, "\t  Operand stack is:");

    for (int i = 0; i <=top; i++) {
      Debug.print(Debug.ERROR, "\t    ");

      if (isOperandRef[i]) {
        Debug.print(Debug.ERROR, "#");
      }

      Debug.println(Debug.ERROR, operands[i]);
    }

    Debug.println(Debug.ERROR, "\t  Local variables are:");

    for (int i = 0, l = locals.length; i < l; i++) {
      Debug.print(Debug.ERROR, "\t    ");

      if (isLocalRef[i]) {
        Debug.print(Debug.ERROR, "#");
      }

      Debug.println(Debug.ERROR, "" + locals[i]);
    }
  }

  public void printStackTrace () {
    Debug.println(Debug.ERROR, getStackTrace());
  }

  public void swap () {
    int t = top-1;
    int v = operands[top];
    boolean isRef = isOperandRef[top];
    Object a = operandAttr[top];
    
    operands[top] = operands[t];
    isOperandRef[top] = isOperandRef[t];
    operandAttr[top] = operandAttr[t];
    
    operands[t] = v;
    isOperandRef[t] = isRef;
    operandAttr[t] = a;
  }

  public String toString () {
    StringBuffer sb = new StringBuffer();

    sb.append("StackFrame[");
    sb.append(mi.getUniqueName());
    sb.append(",top="); sb.append(top);
    sb.append(",operands=");
    sb.append('[');

    for (int i = 0; i <= top; i++) {
      if (i != 0) {
        sb.append(',');
      }

      sb.append(operands[i]);
      if (operandAttr[i] != null) {
        sb.append('(');
        sb.append(operandAttr[i]);
        sb.append(')');
      }
    }

    sb.append(']');
    sb.append(',');
    sb.append("locals=");
    sb.append('[');

    for (int i = 0; i < locals.length; i++) {
      if (i != 0) {
        sb.append(',');
      }

      sb.append(locals[i]);
    }

    sb.append(']');
    sb.append(',');
    sb.append("pc=");
    sb.append(pc.getPosition());
    sb.append(',');
    sb.append("oRefs=");

    for (int i = 0; i <= top; i++) {
      sb.append(isOperandRef[i] ? 'R' : '-');
    }

    sb.append(',');
    sb.append("lRefs=");

    for (int i = 0; i < locals.length; i++) {
      sb.append(isLocalRef[i] ? 'R' : '-');
    }

    sb.append(']');

    return sb.toString();
  }

  long longPeek () {
    return Types.intsToLong( operands[top], operands[top-1]);
  }

  long longPeek (int n) {
    int i = top - n;
    return Types.intsToLong( operands[i], operands[i-1]);
  }

  void longPush (long v) {
    push(Types.hiLong(v), false);
    push(Types.loLong(v), false);
  }

  void doublePush (double v) {
    push(Types.hiDouble(v), false);
    push(Types.loDouble(v), false);    
  }
  
  double doublePop () {
    int lo = pop();
    int hi = pop();

    return Types.intsToDouble(lo, hi);
  }

  long longPop () {
    int lo = pop();
    int hi = pop();

    return Types.intsToLong(lo, hi);
  }
  
  int peek () {
    return operands[top];
  }

  int peek (int n) {
    return operands[top-n];
  }

  void pop (int n) {
    int t = top - n;
    for (int i=top; i>t; i--) {
      if (isOperandRef[i] && (operands[i] != -1)) {
        JVM.getVM().getSystemState().activateGC();
        break;
      }
    }
    
    top = t;
  }

  int pop () {
    int v = operands[top];

    if (isOperandRef[top]) {
      if (v != -1) {
        JVM.getVM().getSystemState().activateGC();
      }
    }
    top--;

    // note that we don't reset the operands or oRefs values, so that
    // we can still access them after the insn doing the pop got executed
    // (e.g. useful for listeners)
    
    return v;
  }

  void push (int v, boolean ref) {
    top++;
    operands[top] = v;
    isOperandRef[top] = ref;
    operandAttr[top] = null;
    
    if (ref && (v != -1)) {
      JVM.getVM().getSystemState().activateGC();
    }
  }

  // return the value of a variable given the name
  private int getLocalVariableOffset (String name) {
    String[] lNames = mi.getLocalVariableNames();
    String[] lTypes = mi.getLocalVariableTypes();

    int      offset = 0;

    for (int i = 0, l = lNames.length; i < l; i++) {
      if (name.equals(lNames[i])) {
        return offset;
      } else {
        offset += Types.getTypeSize(lTypes[i]);
      }
    }

    throw new JPFException("Local variable " + name + " not found");
  }

}

