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

import org.apache.bcel.classfile.ConstantValue;
import org.apache.bcel.classfile.Field;


/**
 * type, name and attribute information of a field.
 */
public abstract class FieldInfo {
  /**
   * Name of the field.
   */
  protected final String name;

  /**
   * Type of the field.
   */
  protected final String type;

  /**
   * Class the field belongs to.
   */
  protected final ClassInfo ci;
  
  /**
   * Static attributes about the field.
   */
  protected final int staticAttr;
  
  // bits in `staticAttr' field
  protected static final int SATTR_STATIC = 0x1;
  protected static final int SATTR_FINAL  = 0x2;
  // more such as transient, volatile could be added

  /**
   * the (possibly null) initializer value for this field.
   */
  protected final ConstantValue cv;
  
  /**
   * what is the order of declaration of this field
   */
  final int fieldIndex;
  
  /**
   * where in the corresponding Fields object do we store the value
   * (note this works because of the wonderful single inheritance)
   */
  final int storageOffset;
  
  
  /**
   * high 16 bit: non-propagation relevant field attributes
   * low 16 bit: object attribute propagation mask
   * (non-final!)
   */
  int attributes = ElementInfo.ATTR_PROP_MASK;
  
  
  protected FieldInfo (String name, String type, int staticAttr,
                       ConstantValue cv, ClassInfo ci, int idx, int off) {
    this.name = name;
    this.type = type;
    this.staticAttr = staticAttr;
    this.ci = ci;
    this.cv = cv;
    this.fieldIndex = idx;
    this.storageOffset = off;
  }

  public static FieldInfo create (Field f, ClassInfo ci, int idx, int off) {
    String name = f.getName();
    String type = f.getSignature();
    ConstantValue cv = f.getConstantValue();
    int staticAttr = 0;
    if (f.isStatic()) staticAttr += SATTR_STATIC;
    if (f.isFinal()) staticAttr += SATTR_FINAL;
    
    FieldInfo ret;
    switch (Types.getBaseType(type)) {
    case Types.T_CHAR:
    case Types.T_BYTE:
    case Types.T_INT:
    case Types.T_SHORT:
    case Types.T_BOOLEAN:
      ret = new IntegerFieldInfo(name, type, staticAttr, cv, ci, idx, off);
      break;
    case Types.T_FLOAT:
      ret = new FloatFieldInfo(name, type, staticAttr, cv, ci, idx, off);
      break;
    case Types.T_DOUBLE:
      ret = new DoubleFieldInfo(name, type, staticAttr, cv, ci, idx, off);
      break;
    case Types.T_LONG:
      ret = new LongFieldInfo(name, type, staticAttr, cv, ci, idx, off);
      break;
    case Types.T_ARRAY:
    case Types.T_REFERENCE:
      ret = new ReferenceFieldInfo(name, type, staticAttr, cv, ci, idx, off);
      break;
    default:
      throw new InternalError("bad base type! " + type + " " +
                              Types.getBaseType(type));
    }
    
    JVM.getVM().annotations.loadAnnotations(ret, f.getAnnotationEntries());
    return ret;
  }

  public abstract String valueToString (Fields f);
  
  /**
   * Returns the class that this field is associated with.
   */
  public ClassInfo getClassInfo () {
    return ci;
  }

  public ConstantValue getConstantValue () {
    return cv;
  }

  public int getFieldIndex () {
    return fieldIndex;
  }
  
  public boolean isReference () {
    return false;
  }
  
  public boolean isArrayField () {
    return false;
  }
  
  /**
   * is this a static field? Counter productive to the current class struct,
   * but at some point we want to get rid of the Dynamic/Static branch (it's
   * really just a field attribute)
   */
  public boolean isStatic () {
    return (staticAttr & SATTR_STATIC) != 0;
  }

  /**
   * is this field declared `final'?
   */
  public boolean isFinal () {
    return (staticAttr & SATTR_FINAL) != 0;
  }
  
  /**
   * Returns the name of the field.
   */
  public String getName () {
    return name;
  }

  /**
   * @return the storage size of this field, @see Types.getTypeSize
   */
  public int getStorageSize () {
    return Types.getTypeSize(type);
  }

  /**
   * Returns the type of the field.
   */
  public String getType () {
    return type;
  }

  /**
   * initialize the corresponding data in the provided Fields instance
   */
  public abstract void initialize (Fields f);
  
  /**
   * Returns a string representation of the field.
   */
  public String toString () {
    StringBuffer sb = new StringBuffer();

    if (isStatic()) {
      sb.append("static ");
    }
    if (isFinal()) {
      sb.append("final ");
    }

    sb.append(Types.getTypeName(type));
    sb.append(" ");
    sb.append(ci.getName());
    sb.append(".");
    sb.append(name);

    return sb.toString();
  }
  
  void setAttributes (int a) {
    attributes = a;
  }
  
  public int getAttributes () {
    return attributes;
  }
  
  public int getStorageOffset () {
    return storageOffset;
  }
  
  public String getFullName() {
    return ci.getName() + '.' + name;
  }
}
