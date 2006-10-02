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

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.jvm.ChoiceGenerator;
import gov.nasa.jpf.jvm.ElementInfo;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.jvm.ClassInfo;
import gov.nasa.jpf.jvm.SystemState;

import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Type;
import org.apache.bcel.generic.ReferenceType;
import gov.nasa.jpf.jvm.FieldInfo;
import gov.nasa.jpf.jvm.FieldLockInfo;

/**
 * parent class for PUT/GET FIELD/STATIC insns
 *
 * <2do> there is a inheritance level missing to deal with instance/static
 * fields - w/o the instance/static helper methods we would have to duplicate
 * code in the getters/setters
 */
public abstract class FieldInstruction extends Instruction implements VariableAccessor
{
  static FieldLockInfo prototype; //optimization measure
  static String[] excludeFields;
  static String[] includeFields;
  
  
  protected String fname;
  protected String className;
  protected String varId;
  
  protected FieldInfo fi; // lazy eval, hence not public
  
  protected int    size;
  protected boolean isReferenceField;
  
  public static void init (Config config) throws Config.Exception {
    if (config.getBoolean("vm.por") && config.getBoolean("vm.por.sync_detection")) {
      prototype = config.getEssentialInstance("vm.por.fieldlockinfo.class",
                                                  FieldLockInfo.class);
      
      excludeFields = config.getStringArray("vm.por.exclude_fields");
      includeFields = config.getStringArray("vm.por.include_fields");
    }
  }
  
  public void setPeer (org.apache.bcel.generic.Instruction i, ConstantPool cp) {
    org.apache.bcel.generic.FieldInstruction fi;
    ConstantPoolGen                          cpg;
    
    fi = (org.apache.bcel.generic.FieldInstruction) i;
    cpg = ClassInfo.getConstantPoolGen(cp);
    
    fname = fi.getFieldName(cpg);
    className = fi.getReferenceType(cpg).toString();
    
    Type ft = fi.getFieldType(cpg);
    if (ft instanceof ReferenceType) {
      isReferenceField = true;
    }
    
    size = ft.getSize();
  }
  
  public abstract FieldInfo getFieldInfo ();
     
  public boolean isReferenceField () {
    return isReferenceField;
  }
  
  public String getId(ElementInfo ei) {
    // <2do> - OUTCH, should be optimized
    return (ei.toString() + '.' + fname);
  }
  
  public String getVariableId () {
    if (varId == null) {
      varId = className + '.' + fname;
    }
    return varId;
  }

  /**
   * is this field supposed to be protected by a lock?
   * this only gets called if on-the-fly POR is in effect
   */
  protected boolean isLockProtected (ElementInfo ei, ThreadInfo ti) {
    //String id = getId(ei);
    //FieldLockInfo flInfo = ei.getFieldLockInfo( id);
    
    FieldInfo fi = getFieldInfo(); // so that we make sure it's computed
    FieldLockInfo flInfo = ei.getFieldLockInfo(fi);
    FieldLockInfo flInfoNext;
    
    
    if (flInfo == null) {
      try {
        flInfoNext = (FieldLockInfo) prototype.clone();
      } catch (Exception x) {
        throw new JPFException(x);   // pretty lame error handling here
      }
    } else {
      flInfoNext = flInfo.checkProtection(ei,fi,ti);
    }
    
    if (flInfoNext != flInfo) {
      //ei.setFieldLockInfo(id, flInfoNext);
      ei.setFieldLockInfo(fi, flInfoNext);
    }
    
    return flInfoNext.isProtected();
  }

  /**
   * check if the field name is filtered from por reduction (i.e. access should not be
   * considered as a transition step). If there is an non-empty excludeFields list,
   * return false if the field starts with any of those patterns. If there is a
   * non-zero includeFields list, return false if the field starts with none of those
   */
  protected boolean checkFieldFilter() {
    String fid = getVariableId();
    int i;
    
    if (excludeFields != null) {
      for (i=0; i<excludeFields.length; i++) {
        if (fid.startsWith(excludeFields[i])) {
          return false;
        }
      }
    }
    
    if (includeFields != null) {
      for (i=0; i<includeFields.length; i++) {
        if (fid.startsWith(includeFields[i])) {
          return true;
        }
      }
      return false;
    }
   
    // default is transition boundary
    return true;
  }
    
  boolean createAndSetFieldCG ( SystemState ss, ElementInfo ei, ThreadInfo ti) {
    if (checkFieldFilter()) {  
      ChoiceGenerator cg = ss.getSchedulerFactory().createSharedFieldAccessCG(ei, ti);
      if (cg != null) {
        ss.setNextChoiceGenerator(cg);
        ti.skipInstructionLogging();
        return true;
      }
    }
        
    return false;
  } 

}








