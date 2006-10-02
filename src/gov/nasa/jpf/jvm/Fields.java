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
import gov.nasa.jpf.util.HashData;
import gov.nasa.jpf.util.IntVector;


/**
 * Represents the variable, hash-collapsed pooled data associated with an object
 * that is related to the object values (as opposed to synchronization ->Monitor).
 * Contains the values of the fields, not their descriptors.  Descriptors are represented
 * by gov.nasa.jpf.jvm.FieldInfo objects, which are stored in the ClassInfo structure.
 *
 * @see gov.nasa.jpf.jvm.FieldInfo
 * @see gov.nasa.jpf.jvm.Monitor
 */
public abstract class Fields implements Cloneable {
  static int FATTR_MASK = 0xffff; // pass all propagated attributes
  
  /** Type of the object or class */
  protected final String type;

  /** the class of this object */
  protected final ClassInfo ci;

  /** this is where we store the instance data */
  protected int[] values;

  protected Fields (String type, ClassInfo ci, int dataSize) {
    this.type = type;
    this.ci = ci;
    
    values = new int[dataSize];
  }
  
  /**
   * give an approximation of the heap size in bytes - we assume fields a word
   * aligned, hence the number of fields*4 should be good. Note that this is
   * overridden by ArrayFields (arrays would be packed)
   */
  public int getHeapSize () {
    return values.length * 4;
  }
  
  /**
   * do we have a reference field with value objRef? This is used by
   * the reachability analysis
   */
  public boolean hasRefField (int objRef) {
    return ci.hasRefField( objRef, this);
  }
  
  /**
   * Returns true if the fields belong to an array.
   */
  public boolean isArray () {
    return Types.getBaseType(type) == Types.T_ARRAY;
  }

  public boolean isReferenceArray () {
    return false;
  }
  
  /**
   * Returns a reference to the class information.
   */
  public ClassInfo getClassInfo () {
    return ci;
  }

  public abstract int getNumberOfFields ();
  // NOTE - fieldIndex (ClassInfo) != storageOffset (Fields). We *don't pad anymore!
  public abstract FieldInfo getFieldInfo (int fieldIndex);
  
  // our low level getters and setters
  public int getIntValue (int index) {
    return values[index];
  }
  
  // same as above, just here to make intentions clear
  public int getReferenceValue (int index) {
    return values[index];
  }
  
  public long getLongValue (int index) {
    return Types.intsToLong(values[index + 1], values[index]);
  }
  
  public boolean getBooleanValue (int index) {
    return Types.intToBoolean(values[index]);
  }
  
  public byte getByteValue (int index) {
    return (byte) values[index];
  }
  
  public char getCharValue (int index) {
    return (char) values[index];
  }
  
  public short getShortValue (int index) {
    return (short) values[index];
  }
       
  public void setReferenceValue (int index, int newValue) {
    values[index] = newValue;
  }
  
  public void setIntValue (int index, int newValue) {
    values[index] = newValue;
  }
  
  public void setLongValue (int index, long newValue) {
		values[index++] = Types.hiLong(newValue);
    values[index] = Types.loLong(newValue);
  }
  
  public void setDoubleValue (int index, double newValue) {
    values[index++] = Types.hiDouble(newValue);
    values[index] = Types.loDouble(newValue);
  }
  
  public void setFloatValue (int index, float newValue) {
    values[index] = Types.floatToInt(newValue);
  }
  
  public float getFloatValue (int index) {
    return Types.intToFloat(values[index]);
  }
  
  public double getDoubleValue (int index) {
    return Types.intsToDouble( values[index+1], values[index]);
  }
  
  /**
   * Returns the type of the object or class associated with the fields.
   */
  public String getType () {
    return type;
  }

  /**
   * Creates a clone.
   */
  public Fields clone () {
    Fields f;

    try {
      f = (Fields) super.clone();
      f.values = values.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError(e.getMessage());
    }

    return f;
  }

  /**
   * Checks for equality.
   */
  public boolean equals (Object o) {
    if (o == null) {
      return false;
    }

    if (!(o instanceof Fields)) {
      return false;
    }

    Fields f = (Fields) o;

    if (!type.equals(f.type)) {
      return false;
    }

    if (ci != f.ci) {
      return false;
    }

    int[] v1 = values;
    int[] v2 = f.values;
    int   l = v1.length;

    if (l != v2.length) {
      return false;
    }

    for (int i = 0; i < l; i++) {
      if (v1[i] != v2[i]) {
        return false;
      }
    }

    return true;
  }

  public void copyTo(IntVector v) {
    v.append(values);
  }
  
  /**
   * Adds some data to the computation of an hashcode.
   */
  public void hash (HashData hd) {
    for (int i = 0, s = values.length; i < s; i++) {
      hd.add(values[i]);
    }
  }

  public int arrayLength () {
    // re-implemented by ArrayFields
    throw new JPFException ("attempt to get length of non-array: " + ci.getName());
  }
  
  public boolean[] asBooleanArray () {
    throw new JPFException( "not an array object: " + ci.getName());
  }
  public byte[] asByteArray () {
    throw new JPFException( "not an array object: " + ci.getName());
  }
  public char[] asCharArray () {
    throw new JPFException( "not an array object: " + ci.getName());
  }
  public short[] asShortArray () {
    throw new JPFException( "not an array object: " + ci.getName());
  }
  public int[] asIntArray () {
    throw new JPFException( "not an array object: " + ci.getName());
  }
  public long[] asLongArray () {
    throw new JPFException( "not an array object: " + ci.getName());
  }
  public float[] asFloatArray () {
    throw new JPFException( "not an array object: " + ci.getName());
  }
  public double[] asDoubleArray () {
    throw new JPFException( "not an array object: " + ci.getName());
  }
  
  /**
   * Computes an hash code.
   */
  public int hashCode () {
    HashData hd = new HashData();

    hash(hd);

    return hd.getValue();
  }

  /**
   * Size of the fields.
   */
  public int size () {
    return values.length;
  }
  
  public String toString () {
    StringBuffer sb = new StringBuffer();
    sb.append("Fields(");

    sb.append("type=");
    sb.append(type);
    sb.append(",");

    sb.append("ci=");
    sb.append(ci.getName());
    sb.append(",");

    sb.append("values=");
    sb.append('[');

    for (int i = 0; i < values.length; i++) {
      if (i != 0) {
        sb.append(',');
      }

      sb.append(values[i]);
    }

    sb.append(']');
    sb.append(",");

    sb.append(")");

    return sb.toString();
  }

  protected abstract String getLogChar ();
  
  // do not modify result!
  public int[] dumpRawValues() {
    return values;
  }
}




