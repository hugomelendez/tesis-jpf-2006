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
import gov.nasa.jpf.jvm.bytecode.Instruction;

import java.lang.reflect.*;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * native peer classes are part of MJI and contain the code that is
 * executed by the host VM (i.e. outside the state-tracked JPF JVM). Each
 * class executed by JPF that has native mehods must have a native peer class
 * (which is looked up and associated at class loadtime)
 */
public class NativePeer {
  
  static Logger logger = JPF.getLogger("gov.nasa.jpf.jvm.NativePeer");
  
  static ClassLoader  loader;
  static HashMap<String, NativePeer>      peers;
  static final int    MAX = 6;
  static Object[][]   argCache;

  ClassInfo ci;
  Class<?>     peerClass;
  HashMap<String, Method>   methods;

  public static void init (Config config) {
    // we don't do fancy things yet, but at some point, we might
    // want to use a specific ClassLoader - just to make sure the
    // peer classes are not colliding with other stuff
    loader = NativePeer.class.getClassLoader();
    peers = new HashMap<String, NativePeer>();

    argCache = new Object[MAX][];

    for (int i = 0; i < MAX; i++) {
      argCache[i] = new Object[i];
    }    
  }
  
  NativePeer () {
    // just here for our derived classes
  }

  NativePeer (Class<?> peerClass, ClassInfo ci) {
    initialize(peerClass, ci, true);
  }

  /**
   * this becomes the factory method to load either a plain (slow)
   * reflection-based peer (a NativePeer object), or some speed optimized
   * derived class object.
   * Watch out - this gets called before the ClassInfo is fully initialized
   * (we shouldn't rely on more than just its name here)
   */
  static NativePeer getNativePeer (ClassInfo ci) {
    String     clsName = ci.getName();
    NativePeer peer = peers.get(clsName);
    Class<?>      peerCls = null;
    String     pcn = null;

    if (peer == null) {
      // <2do> - we really should come up with our own classloader
      // so that we don't have to rely on ClassNotFoundException for
      // the lookup
      try {
        pcn = getSystemPeerClassName(clsName);
        peerCls = loader.loadClass(pcn);
      } catch (Throwable x) {
        try {
          pcn = getUserPeerClassName(clsName);
          peerCls = loader.loadClass(pcn);
        } catch (Throwable xx) {
          try {
            pcn = getGlobalPeerClassName(clsName);
            peerCls = loader.loadClass(pcn);
          } catch (Throwable xxx) {
            // we could throw an exception in case there are native methods
            // in ci, but a JVM would only hickup with UnsatisfiedLinkError
            // if such a method is actually called
          }
        }
      }

      if (peerCls != null) {
        try {
          String dcn = getPeerDispatcherClassName(peerCls.getName());
          Class<?>  dispatcherClass = loader.loadClass(dcn);

          if (logger.isLoggable(Level.INFO)) {
            logger.info("load peer dispatcher: " + dcn);
          }

          peer = (NativePeer) dispatcherClass.newInstance();
          peer.initialize(peerCls, ci, false);
        } catch (Throwable xxxx) {
          if (!(xxxx instanceof ClassNotFoundException)) {
            // maybe we should bail here
            logger.severe("error loading dispatcher: " + xxxx);
          }

          if (logger.isLoggable(Level.INFO)) {
            logger.info("load peer: " + pcn);
          }

          peer = new NativePeer(peerCls, ci);
        }

        peers.put(clsName, peer);
      }
    }

    return peer;
  }

  static String getPeerDispatcherClassName (String clsName) {
    return (clsName + '$');
  }

  /**
   * "system peer" classes reside in gov...jvm, i.e. they can do
   * whatever JPF internal stuff they want to
   */
  static String getSystemPeerClassName (String clsName) {
    // we still need the "JPF_" prefix in case of native methods w/o packages
    return "gov.nasa.jpf.jvm.JPF_" + clsName.replace('.', '_');
  }

  /**
   * look up peer classes in same package like the model class. This is kind
   * of misleading since the model only gets seen by JPF, and the peer only
   * by the host VM, but it makes it easier to distribute if this is an
   * application specific combo. Otherwise the package of the native peer should
   * be chosen so that it can access the host VM classes it needs
   */
  static String getUserPeerClassName (String clsName) {
    int i = clsName.lastIndexOf('.');
    String pkg = clsName.substring(0, i+1);
    
    return (pkg + "JPF_" + clsName.replace('.', '_'));
  }
  
  /**
   * lookup the native peer as a global (i.e. no package) class. This is still
   * safe because the peer classname is fully qualified (has the model
   * package in it)
   * "user peer" classes are confined to what MJIEnv allows them to do
   * (no package required, since the target class package is mangled into
   * the name itself)
   */
  static String getGlobalPeerClassName (String clsName) {
    return "JPF_" + clsName.replace('.', '_');
  }



  Instruction executeMethod (ThreadInfo ti, MethodInfo mi) {
    Object   ret = null;
    Object[] args = null;
    Method   mth;
    String   exception;
    MJIEnv   env = ti.getMJIEnv();
    ElementInfo ei = null;

    env.setCallEnvironment(mi);

    if ((mth = getMethod(mi)) == null) {
      return ti.createAndThrowException("java.lang.UnsatisfiedLinkError",
                                        "cannot find native " + ci.getName() + '.' +
                                        mi.getName());
    }

    try {
      args = getArguments(env, ti, mi, mth);
      
      // we have to lock here in case a native method does sync stuff, so that
      // we don't run into IllegalMonitorStateExceptions
      if (mi.isSynchronized()){
        ei = env.getElementInfo(((Integer)args[1]).intValue());
        ei.lock(ti);
        
        if (mi.isClinit()) {
          ci.setInitializing(ti);
        }
      }
      
      ret = mth.invoke(peerClass, args);

      // these are our non-standard returns
      if ((exception = env.getException()) != null) {
        String details = env.getExceptionDetails();

        // even though we should prefer throwing normal exceptions,
        // sometimes it might be better/required to explicitly throw
        // something that's not wrapped into a InvocationTargetException
        // (e.g. InterruptedException), which is why there still is a
        // MJIEnv.throwException()
        return ti.createAndThrowException(exception, details);
      }

      if (env.getRepeat()) {
        // call it again
        return ti.getPC();
      }

      // Ok, we did 'return', clean up the stack
      // note that we don't have a stack frame for this
      // sucker (for state and speed sake), so we just pop the arguments here
      ti.removeArguments(mi);

      pushReturnValue(ti, mi, ret);
    } catch (IllegalArgumentException iax) {
      System.out.println(iax);
      return ti.createAndThrowException("java.lang.IllegalArgumentException",
                                        "calling " + ci.getName() + '.' +
                                        mi.getName());
    } catch (IllegalAccessException ilax) {
      return ti.createAndThrowException("java.lang.IllegalAccessException",
                                        "calling " + ci.getName() + '.' +
                                        mi.getName());
    } catch (InvocationTargetException itx) {
      // this will catch all exceptions thrown by the native method execution
      // (automatically transformed by java.lang.reflect.Method.invoke())
      return ti.createAndThrowException(
                   "java.lang.reflect.InvocationTargetException",
                   "in " + ci.getName() + '.' + mi.getName() + " : " + itx.getCause());
    } finally {
      // no matter what - if we grabbed the lock, we have to release it
      // but the native method body might actually have given up the lock, so
      // check first
      if (mi.isSynchronized() && ei != null && ei.isLocked()){
        ei.unlock(ti);
        
        if (mi.isClinit()) {
          ci.setInitialized();
        }
      }
      
      // bad native methods might keep references around
      env.clearCallEnvironment();
    }

    Instruction pc = ti.getPC();

    // <2do> - in case of the current System.exit() implementation, all the
    // stackframes are gone and there is no pc anymore. Until we use a more
    // explicit end condition, we have to check for this here (the return value
    // should not matter)
    if (pc == null) {
      return null;
    }
    
    // there is no RETURN for a native method, so we have to advance explicitly
    return pc.getNext();
  }

  void initialize (Class<?> peerClass, ClassInfo ci, boolean cacheMethods) {
    if ((this.ci != null) || (this.peerClass != null)) {
      throw new RuntimeException("cannot re-initialize NativePeer: " +
                                 peerClass.getName());
    }

    this.ci = ci;
    this.peerClass = peerClass;
    
    loadMethods(cacheMethods);
  }

  private static boolean isMJICandidate (Method mth) {
    // only the public static ones are supposed to be native method impls
    if ((mth.getModifiers() & (Modifier.PUBLIC | Modifier.STATIC)) != (Modifier.PUBLIC | Modifier.STATIC)) {
      return false;
    }
    
    // native method always have a MJIEnv and int as the first parameters
    Class<?>[] argTypes = mth.getParameterTypes();
    if ((argTypes.length >= 2) && (argTypes[0] == MJIEnv.class) && (argTypes[1] == int.class) ) {
      return true;
    } else {
      return false;
    }
  }

  private Object[] getArgArray (int n) {
    // JPF BC execution is not multi-threaded, and argument conversion
    // should NOT happen recursively (hint hint), so we are rather primitive here.
    // (actually, argument array can happen recursive, but since they are
    // only used to pass the values, we just care up to the invoke() call)
    if (n < MAX) {
      return argCache[n];
    } else {
      return new Object[n];
    }
  }

  /**
   * Get and convert the native method parameters off the ThreadInfo stack.
   * Use the MethodInfo parameter type info for this (not the reflect.Method
   * type array), or otherwise we won't have any type check
   */
  private Object[] getArguments (MJIEnv env, ThreadInfo ti, MethodInfo mi,
                                 Method mth) {
    int      nArgs = mi.getNumberOfArguments();
    Object[] a = getArgArray(nArgs + 2);
    byte[]   argTypes = mi.getArgumentTypes();
    int      stackOffset;
    int      i;
    int      j;
    int      k;
    int      ival;
    long     lval;

    for (i = 0, stackOffset = 0, j = nArgs + 1, k = nArgs - 1;
         i < nArgs;
         i++, j--, k--) {
      switch (argTypes[k]) {
      case Types.T_BOOLEAN:
        ival = ti.peek(stackOffset);
        a[j] = Boolean.valueOf(Types.intToBoolean(ival));

        break;

      case Types.T_BYTE:
        ival = ti.peek(stackOffset);
        a[j] = Byte.valueOf((byte) ival);

        break;

      case Types.T_CHAR:
        ival = ti.peek(stackOffset);
        a[j] = Character.valueOf((char) ival);

        break;

      case Types.T_SHORT:
        ival = ti.peek(stackOffset);
        a[j] = new Short((short) ival);

        break;

      case Types.T_INT:
        ival = ti.peek(stackOffset);
        a[j] = new Integer(ival);

        break;

      case Types.T_LONG:
        lval = ti.longPeek(stackOffset);
        stackOffset++; // 2 stack words
        a[j] = new Long(lval);

        break;

      case Types.T_FLOAT:
        ival = ti.peek(stackOffset);
        a[j] = new Float(Types.intToFloat(ival));

        break;

      case Types.T_DOUBLE:
        lval = ti.longPeek(stackOffset);
        stackOffset++; // 2 stack words
        a[j] = new Double(Types.longToDouble(lval));

        break;

      default:
        ival = ti.peek(stackOffset);
        a[j] = new Integer(ival);
      }

      stackOffset++;
    }

    if (mi.isStatic()) {
      a[1] = new Integer(ci.getClassObjectRef());
    } else {
      a[1] = new Integer(ti.getCalleeThis(mi));
    }

    a[0] = env;

    return a;
  }


  private Method getMethod (MethodInfo mi) {
    return getMethod(null, mi);
  }

  private Method getMethod (String prefix, MethodInfo mi) {
    String name = mi.getUniqueName();

    if (prefix != null) {
      name = prefix + name;
    }

    return methods.get(name);
  }

  /**
   * look at all public static methods in the peer and set their
   * corresponding model class MethodInfo attributes
   * <2do> pcm - this is too long, break it down
   */
  private void loadMethods (boolean cacheMethods) {
    Method[] m = peerClass.getDeclaredMethods();
    methods = new HashMap<String, Method>(m.length);

    Map<String,MethodInfo> methodInfos = ci.getDeclaredMethods();
    MethodInfo[] mis = null;

    for (int i = 0; i < m.length; i++) {
      Method  mth = m[i];

      if (isMJICandidate(mth)) {
        // Note that we can't mangle the name automatically, since we loose the
        // object type info (all mapped to int). This has to be handled
        // the same way like with overloaded JNI methods - you have to
        // mangle them manually
        String mn = mth.getName();

        // JNI doesn't allow <clinit> or <init> to be native, but MJI does
        // (you should know what you are doing before you use that, really)
        if (mn.startsWith("$clinit")) {
          mn = "<clinit>";
        } else if (mn.startsWith("$init")) {
          mn = "<init>" + mn.substring(5);
        }

        String mname = Types.getJNIMethodName(mn);
        String sig = Types.getJNISignature(mn);

        if (sig != null) {
          mname += sig;
        }

        // now try to find a corresponding MethodInfo object and mark it
        // as 'peer-ed'
        // <2do> in case of <clinit>, it wouldn't be strictly required to
        // have a MethodInfo upfront (we could create it). Might be handy
        // for classes where we intercept just a few methods, but need
        // to init before
        MethodInfo mi = methodInfos.get(mname);

        if ((mi == null) && (sig == null)) {
          // nothing found, we have to do it the hard way - check if there is
          // a single method with this name (still unsafe, but JNI behavior)
          // Note there's no point in doing that if we do have a signature
          if (mis == null) { // cache it for subsequent lookup
            mis = new MethodInfo[methodInfos.size()];
            methodInfos.values().toArray(mis);
          }

          mi = searchMethod(mname, mis);
        }

        if (mi != null) {
          if (logger.isLoggable(Level.INFO)) {
            logger.info("load MJI method: " + mname);
          }

          mi.setMJI(true);


          if (cacheMethods) {
            methods.put(mi.getUniqueName(), mth); // no use to store unless it can be called!
          } else {
            // otherwise we are just interested in setting the MethodInfo attributes
          }
        } else {
          // issue a warning if we have a NativePeer native method w/o a corresponding
          // method in the model class (this might happen due to compiler optimizations
          // silently skipping empty methods)
          logger.warning("orphant NativePeer method: " + ci.getName() + '.' + mname);
        }
      }
    }
  }

  private static MethodInfo searchMethod (String mname, MethodInfo[] methods) {
    int idx = -1;
    
    for (int j = 0; j < methods.length; j++) {
      if (methods[j].getName().equals(mname)) {
        // if this is actually a overloaded method, and the first one
        // isn't the right choice, we would get an IllegalArgumentException,
        // hence we have to go on and make sure it's not overloaded

        if (idx == -1) {
          idx = j;
        } else {
          throw new JPFException("overloaded native method without signature: " + mname);
        }
      }
    }

    if (idx >= 0) {
      return methods[idx];
    } else {
      return null;
    }
  }

  private void pushReturnValue (ThreadInfo ti, MethodInfo mi, Object ret) {
    int  ival;
    long lval;

    // in case of a return type mismatch, we get a ClassCastException, which
    // is handled in executeMethod() and reported as a InvocationTargetException
    // (not completely accurate, but we rather go with safety)
    if (ret != null) {
      switch (mi.getReturnType()) {
      case Types.T_BOOLEAN:
        ival = Types.booleanToInt(((Boolean) ret).booleanValue());
        ti.push(ival, false);

        break;

      case Types.T_BYTE:
        ti.push(((Byte) ret).byteValue(), false);

        break;

      case Types.T_CHAR:
        ti.push(((Character) ret).charValue(), false);

        break;

      case Types.T_SHORT:
        ti.push(((Short) ret).shortValue(), false);

        break;

      case Types.T_INT:
        ti.push(((Integer) ret).intValue(), false);

        break;

      case Types.T_LONG:
        ti.longPush(((Long) ret).longValue());

        break;

      case Types.T_FLOAT:
        ival = Types.floatToInt(((Float) ret).floatValue());
        ti.push(ival, false);

        break;

      case Types.T_DOUBLE:
        lval = Types.doubleToLong(((Double) ret).doubleValue());
        ti.longPush(lval);

        break;

      default:

        // everything else is supposed to be a reference
        ti.push(((Integer) ret).intValue(), true);
      }
    }
  }
}

