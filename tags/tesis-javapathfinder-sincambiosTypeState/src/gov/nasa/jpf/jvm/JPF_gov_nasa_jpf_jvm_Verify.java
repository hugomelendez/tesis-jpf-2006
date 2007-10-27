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
import gov.nasa.jpf.jvm.choice.IntIntervalGenerator;

/**
 * native peer class for programmatic JPF interface (that can be used inside
 * of apps to verify - if you are aware of the danger that comes with it)
 */
public class JPF_gov_nasa_jpf_jvm_Verify {
  static final int MAX_COUNTERS = 10;
  
  static int[] counter;
  static boolean supportIgnorePath;

  static Config config;  // we need to keep this around for CG creation
  
  // our const ChoiceGenerator ctor argtypes
  static Class[] cgArgTypes = { Config.class, String.class };
  // this is our cache for ChoiceGenerator ctor parameters
  static Object[] cgArgs = { null, null };

  
  public static boolean init (Config conf) {
  	supportIgnorePath = conf.getBoolean("vm.verify.ignore_path");
    counter = null;
    config = conf;
    
    Verify.setPeerClass( JPF_gov_nasa_jpf_jvm_Verify.class);
    
  	return true;
  }
  
  public static final int getCounter__I__I (MJIEnv env, int clsObjRef, int counterId) {    
    if ((counter == null) || (counterId < 0) || (counterId >= counter.length)) {
      return 0;
    }

    return counter[counterId];
  }

  public static final void resetCounter__I__V (MJIEnv env, int clsObjRef, int counterId) {
    if ((counter == null) || (counterId < 0) || (counterId >= counter.length)) {
      return;
    }
    counter[counterId] = 0;
  }
  
  public static final int incrementCounter__I__I (MJIEnv env, int clsObjRef, int counterId) {
    if (counterId < 0) {
      return 0;
    }
    
    if (counter == null) {
      counter = new int[(counterId >= MAX_COUNTERS) ? counterId+1 : MAX_COUNTERS];
    } else if (counterId >= counter.length) {
      int[] newCounter = new int[counterId+1];
      System.arraycopy(counter, 0, newCounter, 0, counter.length);
      counter = newCounter;
    }

    return ++counter[counterId];
  }

  public static final long currentTimeMillis____J (MJIEnv env, int clsObjRef) {
    return System.currentTimeMillis();
  }
  
  public static String getType (int objRef, MJIEnv env) {
    DynamicArea da = env.getDynamicArea();

    return Types.getTypeName(da.get(objRef).getType());
  }

  public static void addComment__Ljava_lang_String_2__V (MJIEnv env, int clsObjRef, int stringRef) {
    SystemState ss = env.getSystemState();
    String      cmt = env.getStringObject(stringRef);
    ss.getTrail().setAnnotation(cmt);
  }

  /** deprectated, just use assert */
  public static void assertTrue__Z__V (MJIEnv env, int clsObjRef, boolean b) {
    if (!b) {
      env.throwException("java.lang.AssertionError", "assertTrue failed");
    }
  }

  // those are evil - use with extreme care
  public static void beginAtomic____V (MJIEnv env, int clsObjRef) {
    env.getSystemState().setAtomic();
  }
  public static void endAtomic____V (MJIEnv env, int clsObjRef) {
    env.getSystemState().clearAtomic();
  }

  public static void busyWait__J__V (MJIEnv env, int clsObjRef, long duration) {
    // nothing required here (we systematically explore scheduling
    // sequences anyway), but we need to intercept the call
  }

  public static void dumpState____V (MJIEnv env, int clsObjRef) {
    System.out.println("dumping the state");
    env.getKernelState().log();
  }


  public static void ignoreIf__Z__V (MJIEnv env, int clsObjRef, boolean cond) {
    if (supportIgnorePath) {
      env.getSystemState().setIgnored(cond);
    }
  }

  public static void interesting__Z__V (MJIEnv env, int clsObjRef, boolean cond) {
    env.getSystemState().setInteresting(cond);
  }

  public static boolean isCalledFromClass__Ljava_lang_String_2__Z (MJIEnv env, int clsObjRef,
                                           int clsNameRef) {
    String refClassName = env.getStringObject(clsNameRef);
    ThreadInfo ti = env.getThreadInfo();
    int        stackDepth = ti.countStackFrames();

    if (stackDepth < 2) {
      return false;      // native methods don't have a stackframe
    }
    
    String mthClassName = ti.getCallStackClass(1);
    ClassInfo ci = ClassInfo.getClassInfo(mthClassName);
    
    return ci.instanceOf(refClassName);
  }
  
  public static final boolean getBoolean____Z (MJIEnv env, int clsObjRef) {
    ThreadInfo ti = env.getThreadInfo();
    SystemState ss = env.getSystemState();
    ChoiceGenerator cg;
    
    if (!ti.isFirstStepInsn()) { // first time around
      cg = new BooleanChoiceGenerator(config, "boolean");
      ss.setNextChoiceGenerator(cg);
      env.repeatInvocation();
      return true;  // not used anyways
      
    } else {  // this is what really returns results
      cg = ss.getChoiceGenerator();

      assert (cg != null) && (cg instanceof BooleanChoiceGenerator) : "expected BooleanChoiceGenerator, got: " + cg;
      return ((BooleanChoiceGenerator)cg).getNextChoice();
    }
  }
  
  public static final int getInt__II__I (MJIEnv env, int clsObjRef, int min, int max) {    
    ThreadInfo ti = env.getThreadInfo();
    SystemState ss = env.getSystemState();
    ChoiceGenerator cg;
    
    if (!ti.isFirstStepInsn()) { // first time around
      cg = new IntIntervalGenerator(min,max);
      ss.setNextChoiceGenerator(cg);
      //ti.skipInstructionLogging();
      env.repeatInvocation();
      return 0;  // not used anyways

    } else {
      cg = ss.getChoiceGenerator();
      
      assert (cg != null) && (cg instanceof IntChoiceGenerator) : "expected IntChoiceGenerator, got: " + cg;
      return ((IntChoiceGenerator)cg).getNextChoice();
    }    
  }
  
  static ChoiceGenerator createChoiceGenerator (SystemState ss, String id) {
    ChoiceGenerator gen = null;
    
    cgArgs[0] = config;
    cgArgs[1] = id; // good thing we are not multithreaded (other fields are const)
    
    try {
      String key = id + ".class";
      gen = config.getEssentialInstance(key, ChoiceGenerator.class, 
                                                  cgArgTypes, cgArgs);
      ss.setNextChoiceGenerator(gen);
    } catch (Config.Exception cex) {
      // bail, nothing we can do to cover up
      throw new JPFException(cex);
    }

    return gen;
  }

  
  public static final int getInt__Ljava_lang_String_2__I (MJIEnv env, int clsObjRef, int idRef) {
    ThreadInfo ti = env.getThreadInfo();
    SystemState ss = env.getSystemState();
    ChoiceGenerator cg;
    
    if (!ti.isFirstStepInsn()) { // first time around
      String id = env.getStringObject(idRef);
      cg = createChoiceGenerator( ss, id);
      ss.setNextChoiceGenerator(cg);
      //ti.skipInstructionLogging();
      env.repeatInvocation();
      return 0;  // not used anyways

    } else {
      cg = ss.getChoiceGenerator();
      
      assert (cg != null) && (cg instanceof IntChoiceGenerator) : "expected IntChoiceGenerator, got: " + cg;
      return ((IntChoiceGenerator)cg).getNextChoice();
    }    
  }
  
  public static int getObject__Ljava_lang_String_2__Ljava_lang_Object_2 (MJIEnv env, int clsObjRef, int idRef) {
    ThreadInfo ti = env.getThreadInfo();
    SystemState ss = env.getSystemState();
    ChoiceGenerator cg;
    
    if (!ti.isFirstStepInsn()) { // first time around
      String id = env.getStringObject(idRef);
      cg = createChoiceGenerator( ss, id);
      ss.setNextChoiceGenerator(cg);
      //ti.skipInstructionLogging();
      env.repeatInvocation();
      return 0;  // not used anyways

    } else {
      cg = ss.getChoiceGenerator();
      
      assert (cg != null) && (cg instanceof ReferenceChoiceGenerator) : "expected ReferenceChoiceGenerator, got: " + cg;
      return ((ReferenceChoiceGenerator)cg).getNextChoice();
    }    
  }
  
  public static final double getDouble__Ljava_lang_String_2__D (MJIEnv env, int clsObjRef, int idRef) {
    ThreadInfo ti = env.getThreadInfo();
    SystemState ss = env.getSystemState();
    ChoiceGenerator cg;
    
    if (!ti.isFirstStepInsn()) { // first time around
      String id = env.getStringObject(idRef);
      cg = createChoiceGenerator( ss, id);
      ss.setNextChoiceGenerator(cg);
      //ti.skipInstructionLogging();
      env.repeatInvocation();
      return 0.0;  // not used anyways

    } else {
      cg = ss.getChoiceGenerator();
      
      assert (cg != null) && (cg instanceof DoubleChoiceGenerator) : "expected DoubleChoiceGenerator, got: " + cg;
      return ((DoubleChoiceGenerator)cg).getNextChoice();
    }    
  }


  /**
   *  deprecated, use getBoolean() 
   */
  public static final boolean randomBool (MJIEnv env, int clsObjRef) {
    //SystemState ss = env.getSystemState();
    //return (ss.random(2) != 0);

    return getBoolean____Z(env, clsObjRef);
  }  
  
  /**
   * deprecated, use getInt
   */
  public static final int random__I__I (MJIEnv env, int clsObjRef, int x) {
   return getInt__II__I( env, clsObjRef, 0, x);
  }

  static void boring__Z__V (MJIEnv env, int clsObjRef, boolean b) {
    env.getSystemState().setBoring(b);
  }

  protected static int[] arrayOfObjectsOfType (DynamicArea da, String type) {
    int[] map = new int[0];
    int   map_size = 0;

    for (int i = 0; i < da.getLength(); i++) {
      if (da.get(i) != null) {
        if ((Types.getTypeName(da.get(i).getType())).equals(type)) {
          if (map_size >= map.length) {
            int[] n = new int[map_size + 1];
            System.arraycopy(map, 0, n, 0, map.length);
            map = n;
          }

          map[map_size] = i;
          map_size++;
        }
      }
    }

    return map;
  }
  
  public static boolean vmIsMatchingStates____Z(MJIEnv env, int clsObjRef) {
    return env.getVM().getStateSet() != null;
  }
  
  public static void storeTrace____V (MJIEnv env, int clsObjRef) {
    env.getVM().storeTrace();
  }
}
