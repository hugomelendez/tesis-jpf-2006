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
package java.lang;

import gov.nasa.jpf.JPFException;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.net.URL;

/**
 * MJI model class for java.lang.Class library abstraction
 * 
 * This is a JPF specific version of a system class because we can't use the real,
 * platform JVM specific version (it's native all over the place, its field
 * structure isn't documented, most of its methods are private, hence we can't
 * even instantiate it properly).
 *
 * Note that this class never gets seen by the real VM - it's for JPF's eyes only.
 *
 * For now, it's only a fragment to test the mechanism, and provide basic
 * class.getName() / Class.ForName() support (which is (almost) enough for
 * Java assertion support
 */
@SuppressWarnings("unused")  // native peer uses
public final class Class<T> {
  private String name;

  /**
   * this is the StaticArea ref of the class we refer to
   * (so that we don't have to convert to a Java String in the peer all the time)
   */
  private int cref;

  /**
   * to be set during <clinit> of the corresponding class
   */
  private boolean isPrimitive;
  
  public native boolean isArray ();

  public native Class<?> getComponentType ();

  public native Field getDeclaredField (String fieldName) throws NoSuchFieldException,
                                                          SecurityException;

  public native Method getDeclaredMethod (String mthName, Class<?>... paramTypes)
                            throws NoSuchMethodException, SecurityException;

  public native Method getMethod (String mthName, Class<?>... paramTypes)
                    throws NoSuchMethodException, SecurityException;
  
  public Method[] getDeclaredMethods () throws SecurityException {
    // <2do>
    throw new JPFException("Class.getDeclaredMethods() not yet supported");
  }

  public InputStream getResourceAsStream (String name) {
    throw new JPFException("Class.getResourceAsStream() not yet supported");    
  }
  
  public URL getResource (String name) {
    throw new JPFException("Class.getResource() not yet supported");    
  }
  
  public Constructor<?> getDeclaredConstructor (Class<?>... paramTypes)
              throws NoSuchMethodException, SecurityException {
  // <2do>
    throw new JPFException("Class.getDeclaredConstructor() not yet supported");
  }

  
  public native Field getField (String fieldName) throws NoSuchFieldException,
                                                  SecurityException;

  public native boolean isInstance (Object o);

  public native boolean isAssignableFrom (Class<?> clazz);
  
  public Constructor<T> getConstructor (Class<?>[] argTypes) throws NoSuchMethodException {
    throw new JPFException("Class.getConstructor() not yet supported");
  }
  
  // no use to have a ctor, we can't call it
  public String getName () {
    return name;
  }

  public String getSimpleName () {
    int idx = name.lastIndexOf('.'); // <2do> not really - inner classes?
    return name.substring(idx+1);
  }
  
  public static native Class<?> getPrimitiveClass (String clsName);

  /**
   * this one is in JPF reflection land, it's 'native' for us
   */
  public static native Class<?> forName (String clsName)
                               throws ClassNotFoundException;

  public boolean isPrimitive () {
    return isPrimitive;
  }

  public native Class<?> getSuperclass ();

  public native T newInstance () throws InstantiationException,
                                      IllegalAccessException;

  public String toString () {
    return ("class " + name);
  }

  @SuppressWarnings("unchecked")
  public T cast(Object o) {
    if (o != null && !isInstance(o)) throw new ClassCastException();
    return (T) o;
  }

  public <U> Class<? extends U> asSubclass(Class<U> clazz) {
    if (this.isAssignableFrom(clazz)) {
      return clazz;
    } else {
      throw new ClassCastException("" + this + " is not a " + clazz);
    }
  }
  
  native boolean desiredAssertionStatus ();
  
  public ClassLoader getClassLoader() {
    // not yet implemented
    return null;
  }
}
