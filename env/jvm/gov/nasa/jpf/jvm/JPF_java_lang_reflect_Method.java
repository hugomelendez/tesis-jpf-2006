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

import gov.nasa.jpf.jvm.bytecode.Instruction;


public class JPF_java_lang_reflect_Method {
  // the registry is rather braindead, let's hope we don't have many lookups - 
  // using Methods is fine, but creating them is not efficient until we fix this
  
  static final int NREG = 10;
  static MethodInfo[] registered = new MethodInfo[NREG];
  static int nRegistered;
  
  static int registerMethodInfo (MethodInfo mi) {
    int idx;
    
    for (idx=0; idx < nRegistered; idx++) {
      if (registered[idx] == mi) {
        return idx;
      }
    }
    
    if (idx == registered.length) {
      MethodInfo[] newReg = new MethodInfo[registered.length+NREG];
      System.arraycopy(registered, 0, newReg, 0, registered.length);
    }
    
    registered[idx] = mi;
    nRegistered++;
    return idx;
  }
  
  static MethodInfo getRegisteredFieldInfo (int idx) {
    return registered[idx];
  }

  static MethodInfo getMethodInfo (MJIEnv env, int objRef) {
    int idx = env.getIntField( objRef, "regIdx");
    assert ((idx >= 0) || (idx < nRegistered)) : "illegal MethodInfo request: " + idx + ", " + nRegistered;
    
    return registered[idx];
  }

  
  public static int getName____Ljava_lang_String_2 (MJIEnv env, int objRef) {
    MethodInfo mi = getMethodInfo(env, objRef);
    
    int nameRef = env.getReferenceField( objRef, "name");
    if (nameRef == -1) {
      nameRef = env.newString(mi.getName());
      env.setReferenceField(objRef, "name", nameRef);
    }
   
    return nameRef;
  }

  static int createBoxedReturnValueObject (MJIEnv env, MethodInfo mi, StackFrame frame) {
    byte rt = mi.getReturnType();
    int ret = MJIEnv.NULL;
    ElementInfo rei;
    
    if (rt == Types.T_DOUBLE) {
      double v = frame.doublePop();
      ret = env.newObject(ClassInfo.getClassInfo("java.lang.Double"));
      rei = env.getElementInfo(ret);
      rei.setDoubleField("value", v);
    } else if (rt == Types.T_LONG) {
      long v = frame.longPop();
      ret = env.newObject(ClassInfo.getClassInfo("java.lang.Long"));
      rei = env.getElementInfo(ret);
      rei.setLongField("value", v);
    } else if (rt == Types.T_BYTE) {
      int v = frame.pop(); 
      ret = env.newObject(ClassInfo.getClassInfo("java.lang.Byte"));
      rei = env.getElementInfo(ret);
      rei.setIntField("value", v);
    } else if (rt == Types.T_CHAR) {
      int v = frame.pop(); 
      ret = env.newObject(ClassInfo.getClassInfo("java.lang.Character"));
      rei = env.getElementInfo(ret);
      rei.setIntField("value", v);
    } else if (rt == Types.T_SHORT) {
      int v = frame.pop(); 
      ret = env.newObject(ClassInfo.getClassInfo("java.lang.Short"));
      rei = env.getElementInfo(ret);
      rei.setIntField("value", v);
    } else if (rt == Types.T_INT) {
      int v = frame.pop(); 
      ret = env.newObject(ClassInfo.getClassInfo("java.lang.Integer"));
      rei = env.getElementInfo(ret);
      rei.setIntField("value", v);
    } else if (rt == Types.T_BOOLEAN) {
      int v = frame.pop(); 
      ret = env.newObject(ClassInfo.getClassInfo("java.lang.Boolean"));
      rei = env.getElementInfo(ret);
      rei.setIntField("value", v);
    } else if (mi.isReferenceReturnType()){ 
      ret = frame.pop();
    }

    return ret;
  }

  static void pushUnboxedArguments (MJIEnv env, MethodInfo mi, StackFrame frame, int argsRef) {
    int nArgs = mi.getNumberOfArguments();
    
    for (int i=0; i<nArgs; i++) {
      int argRef = env.getReferenceArrayElement(argsRef, i);
      ElementInfo aei = env.getElementInfo(argRef);
      ClassInfo aci = aei.getClassInfo();
      if (aci.isBoxClass()) { // unbox
        String cname = aci.getName();
        if (cname.equals("java.lang.Long")) {
          long l = aei.getLongField("value");
          frame.longPush(l);
        } else if (cname.equals("java.lang.Double")) {
          double d = aei.getDoubleField("value");
          frame.push(Types.hiDouble(d), false);
          frame.push(Types.loDouble(d), false);
        } else {
          int v = aei.getIntField("value");
          frame.push(v, false);
        }
      } else { // otherwise it's a reference
        frame.push(argRef, true);
      }
    }
  }
  
  public static int invoke__Ljava_lang_Object_2_3Ljava_lang_Object_2__Ljava_lang_Object_2 (MJIEnv env, int mthRef,
                                                                                           int objRef, int argsRef) {
    ThreadInfo ti = env.getThreadInfo();
    Instruction insn = ti.getPC();
        
    int regIdx = env.getIntField( mthRef, "regIdx");
    MethodInfo mi = registered[regIdx];
    DirectCallStackFrame frame;
    
    if (!ti.isResumedInstruction(insn)) { // make a direct call  
      MethodInfo stub = mi.createDirectCallStub("[reflection]");
      frame = new DirectCallStackFrame(stub, insn);
        
      if (!mi.isStatic()) {
        frame.push(objRef, true);
      }
      
      pushUnboxedArguments(env, mi, frame, argsRef);
      
      ti.pushFrame(frame);
      env.repeatInvocation();
      
      return MJIEnv.NULL;
    } else { // direct call returned, unbox return type (if any)      
      return createBoxedReturnValueObject(env, mi, ti.getReturnedDirectCall());
    }
  }
}
