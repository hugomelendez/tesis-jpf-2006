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

/**
 * MJI NativePeer class for java.io.PrintStream library abstraction
 */
class JPF_java_io_PrintStream {
  
  /****************************************************************************
   * these are the native methods we intercept
   */
  
  public static void print__C__V (MJIEnv env, int objref, char c) {
    env.getVM().print(c);
  }

  public static void print__D__V (MJIEnv env, int objref, double d) {
    env.getVM().print(d);
  }

  public static void print__F__V (MJIEnv env, int objref, float f) {
    env.getVM().print(f);
  }

  public static void print__I__V (MJIEnv env, int objref, int i) {
    env.getVM().print(i);
  }

  public static void print__J__V (MJIEnv env, int objref, long j) {
    env.getVM().print(j);
  }

  public static void print__Ljava_lang_String_2__V (MJIEnv env, int objRef,
                                                 int strRef) {
    env.getVM().print(env.getStringObject(strRef));
  }

  public static void print__Z__V (MJIEnv env, int objref, boolean z) {
    env.getVM().print(z);
  }

  public static void println____V (MJIEnv env, int objRef) {
    env.getVM().println();
  }

  public static void println__C__V (MJIEnv env, int objref, char c) {
    env.getVM().print(c);
    env.getVM().println();
  }

  public static void println__D__V (MJIEnv env, int objref, double d) {
    env.getVM().print(d);
    env.getVM().println();
  }

  public static void println__F__V (MJIEnv env, int objref, float f) {
    env.getVM().print(f);
    env.getVM().println();
  }

  public static void println__I__V (MJIEnv env, int objref, int i) {
    env.getVM().print(i);
    env.getVM().println();
  }

  public static void println__J__V (MJIEnv env, int objref, long j) {
    env.getVM().print(j);
    env.getVM().println();
  }

  public static void println__Ljava_lang_String_2__V (MJIEnv env, int objRef,
                                                   int strRef) {
    env.getVM().println(env.getStringObject(strRef));
  }

  public static void println__Z__V (MJIEnv env, int objref, boolean z) {
    env.getVM().print(z);
    env.getVM().println();
  }

  public static void write__Ljava_lang_String_2__V (MJIEnv env, int objref,
                                                 int strRef) {
    env.getVM().println(env.getStringObject(strRef));
  }
}
