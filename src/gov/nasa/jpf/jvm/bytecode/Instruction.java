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
package gov.nasa.jpf.jvm.bytecode;

import gov.nasa.jpf.jvm.ClassInfo;
import gov.nasa.jpf.jvm.KernelState;
import gov.nasa.jpf.jvm.MethodInfo;
import gov.nasa.jpf.jvm.SystemState;
import gov.nasa.jpf.jvm.ThreadInfo;

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.generic.InstructionHandle;

/**
 * common root of all JPF bytecode instruction classes 
 */
public abstract class Instruction {
  protected static final List<String> unimplemented = new ArrayList<String>();
  
  protected int         position; // accumulated position (prev pos + prev bc-length)
  protected int         offset;   // consecutive index of instruction
  
  /**
   * NOTE - this is the method this instruction belongs to!
   * <2do> pcm - Seems nobody has noticed this gets shadowed all over the place
   * by local vars with a different meaning (InvokeInstruction)
   */
  protected MethodInfo mi;
  protected String     asString;

  abstract public int getByteCode();
  
  // to allow a classname and methodname context for each instruction
  public void setContext (String className, String methodName, int lineNumber,
                          int offset) {
  }

  public boolean isFirstInstruction () {
    return (offset == 0);
  }
  
  /**
   * answer if this is a potential loop closing jump
   */
  public boolean isBackJump () {
    return false;
  }
  
  public boolean isDeterministic (SystemState ss, KernelState ks, ThreadInfo ti) {
    return true;
  }

  public boolean isExecutable (SystemState ss, KernelState ks, ThreadInfo th) {
    return true;
  }

  public MethodInfo getMethod () {
    return mi;
  }

  
  public Instruction getNext () {
    return mi.getInstruction(offset + 1);
  }

  public int getOffset () {
    return offset;
  }

  public int getPosition () {
    return position;
  }

  public Instruction getPrev () {
    if (offset > 0) {
      return mi.getInstruction(offset - 1);
    } else {
      return null;
    }
  }

  public boolean isSchedulingRelevant(SystemState ss, KernelState ks, ThreadInfo ti) {
    return false;
  }
  
  public abstract Instruction execute (SystemState ss, KernelState ks,
                                       ThreadInfo th);

  public boolean examine (SystemState ss, KernelState ks, ThreadInfo th) {
    return false;
  }

  public boolean examineAbstraction (SystemState ss, KernelState ks,
                                     ThreadInfo th) {
    return false;
  }

  public String toString () {
    if (asString == null) {
      asString = getMnemonic() + " @ " + offset;
    }
    return asString;
  }

  public String getMnemonic () {
    String s = getClass().getSimpleName();
    return s.toLowerCase();
  }
  
  public String getSourceLocation () {
    ClassInfo ci = mi.getClassInfo();
    int line = mi.getLineNumber(this);
    String file = ci.getSourceFileName();
    
    String s = ci.getName() + '.' + mi.getFullName() + " at ";
    
    if (file != null) {
      s +=  file;
      s += ':'; 
      s += line;
    } else {
      s += "pc ";
      s += position;
    }
    
    return s;
  }
  
  //SUNYSB
  protected abstract void setPeer (org.apache.bcel.generic.Instruction i,
                                   ConstantPool cp);

  public void init (InstructionHandle h, int off, MethodInfo m,
                       ConstantPool cp) {
    position = h.getPosition();
    offset = off;
    mi = m;
    //asString = h.getInstruction().toString(cp);
    setPeer(h.getInstruction(), cp);
  }

  public boolean requiresClinitCalls (ThreadInfo ti, ClassInfo ci) {
    
    if (!ti.isResumedInstruction(this)) {
      if (!ci.isInitialized()) {
        if (ci.loadAndInitialize(ti, this) > 0) {
          //ti.skipInstructionLogging();
          return true; // there are new <clinit> frames on the stack, execute them
        }
      }
    }
    
    return false;
  }
  
  /**
   * this is returning the next Instruction to execute, to be called after
   * we executed ourselves.
   *
   * Be aware of that we might have had exceptions caused by our execution,
   * i.e. we can't simply assume it's the next insn following us (we have to
   * acquire the 'current' insn after our exec from the ThreadInfo).
   *
   * To make it even more interesting, the ThreadInfo might return null because
   * we might have run into a System.exit, which purges all stacks.
   * It's questionable if it's the right way to handle this by just returning
   * our own successor, and rely on ThreadInfo.executeXXMethod to catch
   * this, but it seems to be the smallest change
   *
   * <?> pcm - this is hackish control in case of System.exit
   */
  Instruction getNext (ThreadInfo th) {
    Instruction insn = th.getPC();

    if (insn == null) {
      // could be all purged (System.exit)
      insn = this;
    }

    return insn.getNext();
  }
}


