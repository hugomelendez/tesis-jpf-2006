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
import gov.nasa.jpf.util.IntTable;
import gov.nasa.jpf.util.ObjVector;
import gov.nasa.jpf.util.Source;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.util.ClassPath;
import org.apache.bcel.util.ClassPath.ClassFile;


/**
 * Describes the JVM's view of a java class.  Contains descriptions of the
 * static and dynamic fields, methods, and information relevant to the
 * class.
 */
public class ClassInfo {
  
  public static final int UNINITIALIZED = -1;
  // 'INITIALIZING' is any number >=0, which is the thread index that executes the clinit
  public static final int INITIALIZED = -2;
  
  static Logger logger = JPF.getLogger("gov.nasa.jpf.jvm.ClassInfo");
  
  /**
   * this is our BCEL classpath. Note that this actually might be
   * turned into a call or ClassInfo instance field if we ever support
   * ClassLoaders (for now we keep it simple)
   */
  protected static ClassPath modelClassPath;
  
  // part of broken hack for getting annotation information -peterd
  /*
   * this is used to get annotations from code by loading model classes
   * into host VM.  this should be eliminated once a better solution is
   * found/created.
   
  protected static ClassLoader modelHostClassLoader;
  protected static ClassPath modelSpecificClassPath;
  protected Class<?> hostClass;
  */
  
  /**
   * ClassLoader that loaded this class.
   */
  protected static final ClassLoader thisClassLoader = ClassInfo.class.getClassLoader();
  
  /**
   * Maps class names to unique id numbers.
   */
  protected static final IntTable<String> globalIdTable =
    new IntTable<String>(8);

  /**
   * Loaded classes, indexed by id number.
   */
  protected static final ObjVector<ClassInfo> loadedClasses =
    new ObjVector<ClassInfo>(100);

  /**
   * optionally used to determine atomic methods of a class (during class loading)
   */
  protected static Attributor attributor;
  
  /**
   * here we get infinitly recursive, so keep it around for
   * identity checks
   */
  static ClassInfo classClassInfo;
  
  /*
   * some distinguished classInfos we keep around for efficiency reasons 
   */
  static ClassInfo objectClassInfo;
  static ClassInfo stringClassInfo;
  static ClassInfo weakRefClassInfo;
  static ClassInfo refClassInfo;
  
  static FieldInfo[] emptyFields = new FieldInfo[0];
      
  /**
   * Name of the class. e.g. "java.lang.String"
   */
  protected final String name;
  
  // various class attributes
  protected boolean      isClass = true;
  protected boolean      isWeakReference = false;
  protected MethodInfo   finalizer = null;
  protected boolean      isArray = false;
  protected boolean      isReferenceArray = false;
  
  
  /** type based object attributes (for GC, partial order reduction and
   * property checks)
   */
  protected int elementInfoAttrs = 0;
  
  /**
   * all our declared methods (we don't flatten, this is not
   * a high-performance VM)
   */
  protected final Map<String, MethodInfo> methods;
  
  /**
   * our instance fields.
   * Note these are NOT flattened, i.e. only contain the declared ones
   */
  protected FieldInfo[] iFields;
  
  /** the storage size of instances of this class (stored as an int[]) */
  protected int instanceDataSize;
  
  /** where in the instance data array (int[]) do our declared fields start */
  protected int instanceDataOffset;
  
  /** total number of instance fields (flattened, not only declared ones) */
  protected int nInstanceFields;
  
  /**
   * our static fields. Again, not flattened
   */
  protected FieldInfo[] sFields;
  
  /** the storage size of static fields of this class (stored as an int[]) */
  protected int staticDataSize;
  
  /** where to get static field values from - it can be used quite frequently
   * (to find out if the class needs initialization, so cache it.
   * BEWARE - this is volatile (has to be reset&restored during backtrack */
  StaticElementInfo sei;
  
  protected final ClassInfo  superClass;
  
  /**
   * Interfaces implemented by the class.
   */
  protected final Set<String> interfaces;
  
  /** all interfaces (parent interfaces and interface parents) - lazy eval */
  protected Set<String> allInterfaces;
  
  /** Name of the package. */
  protected final String packageName;
  
  /** Name of the file which contains the source of this class. */
  protected String sourceFileName;
  
  /** A unique id associate with this class. */
  protected final int uniqueId;
  
  /**
   * this is the object we use to execute methods in the underlying JVM
   * (it replaces Reflection)
   */
  private NativePeer nativePeer;
  
  /** Source file associated with the class.*/
  protected Source source;

  static String[] assertionPatterns;
  boolean enableAssertions;
  
  static boolean init (Config config) throws Config.Exception {
    loadedClasses.clear();
    globalIdTable.clear();
    classClassInfo = null;
    objectClassInfo = null;
    stringClassInfo = null;
    weakRefClassInfo = null;

    setSourceRoots(config);
    buildModelClassPath(config);
    
    attributor = config.getEssentialInstance("vm.attributor.class",
                                                         Attributor.class);  
    
    assertionPatterns = config.getStringArray("vm.enable_assertions");
        
    return true;
  }
    
  /**
   * ClassInfo ctor used for builtin types (arrays and primitive types)
   * i.e. classes we don't have class files for
   */
  protected ClassInfo (String builtinClassName) {
    isArray = (builtinClassName.charAt(0) == '[');
    isReferenceArray = isArray && builtinClassName.endsWith(";");
    
    name = builtinClassName;

    logger.log(Level.FINE, "generating builtin class ", name);
        
    packageName = ""; // builtin classes don't reside in java.lang !
    sourceFileName = null;
    source = null;
    
    // part of broken hack for getting annotation information -peterd
    /*
    try {
      hostClass = thisClassLoader.loadClass(builtinClassName);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    */

    // no fields
    iFields = emptyFields;
    sFields = emptyFields;
    
    if (isArray) {
      superClass = objectClassInfo;
      interfaces = loadArrayInterfaces();
      methods = loadArrayMethods();
    } else {
      superClass = null; // strange, but true, a 'no object' class
      interfaces = loadBuiltinInterfaces(name);
      methods = loadBuiltinMethods(name);
    }
    
    enableAssertions = true; // doesn't really matter - no code associated
    
    uniqueId = globalIdTable.poolIndex(name);
    loadedClasses.set(uniqueId,this);
    
  }
  
  /**
   * Creates a new class from the JavaClass information.
   */
  protected ClassInfo (JavaClass jc) {
    name = jc.getClassName();
    
    logger.log(Level.FINE, "loading class ", name);
    
    uniqueId = globalIdTable.poolIndex(name);
    loadedClasses.set(uniqueId,this);
    
    if ((objectClassInfo == null) && name.equals("java.lang.Object")) {
      objectClassInfo = this;
    } else if ((classClassInfo == null) && name.equals("java.lang.Class")) {
      classClassInfo = this;
    } else if ((stringClassInfo == null) && name.equals("java.lang.String")) {
      stringClassInfo = this;
    } else if ((weakRefClassInfo == null) &&
               name.equals("java.lang.ref.WeakReference")) {
      weakRefClassInfo = this;
    } else if ((refClassInfo == null) && name.equals("java.lang.ref.Reference")) {
      refClassInfo = this;
    }

    // part of broken hack for getting annotation information -peterd
    /*
    try {
      hostClass = modelHostClassLoader.loadClass(name);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    */
    
    isClass = jc.isClass();
    superClass = loadSuperClass(jc);
    
    interfaces = loadInterfaces(jc);
    packageName = jc.getPackageName();
    
    iFields = loadInstanceFields(jc);
    instanceDataSize = computeInstanceDataSize();
    instanceDataOffset = computeInstanceDataOffset();
    nInstanceFields = (superClass != null) ?
      superClass.nInstanceFields + iFields.length : iFields.length;
    
    sFields = loadStaticFields(jc);
    staticDataSize = computeStaticDataSize();
    
    methods = loadMethods(jc);
    
    // Used to execute native methods (in JVM land).
    // This needs to be initialized AFTER we get our
    // MethodInfos, since it does a reverse lookup to determine which
    // ones are handled by the peer (by means of setting MethodInfo attributes)
    nativePeer = NativePeer.getNativePeer(this);
    
    sourceFileName = jc.getSourceFileName();
    
    // bcel seems to behave differently on Windows, returning <Unknown>,
    // which gives us problems when writing/reading XML traces
    if (sourceFileName.equalsIgnoreCase("<Unknown>")) {
      sourceFileName = "Unkown";
    }
    
    if (packageName.length() > 0) {
      sourceFileName = packageName.replace('.', File.separatorChar) +
        File.separator + sourceFileName;
    }
    
    source = null;
    
    isWeakReference = isWeakReference0();
    finalizer = getFinalizer0();
    
    // get type specific object and field attributes
    elementInfoAttrs = loadElementInfoAttrs(jc);
    
    // the corresponding java.lang.Class object gets set when we initialize
    // this class from StaticArea.newClass() - we don't know the
    // DynamicArea (Heap) here, until we turn this into a global object
    
    enableAssertions = getAssertionStatus();
    
    // be advised - we don't have fields initialized yet
    JVM.getVM().notifyClassLoaded(this);
  }
    
  boolean getAssertionStatus () {
    if ((assertionPatterns == null) || (assertionPatterns.length == 0)){
      return false;
    } else if ("*".equals(assertionPatterns[0])) {
      return true;
    } else {
      for (int i=0; i<assertionPatterns.length; i++) {
        if (name.matches(assertionPatterns[i])) { // Ok, not very efficient
          return true;
        }
      }
      
      return false;
    }
  }
  
  public boolean isArray () {
    return isArray;
  }
  
  public boolean isReferenceArray () {
    return isReferenceArray;
  }
  
  /**
   * Loads the class specified.
   * @param dotName The fully qualified name of the class to load.
   * @return Returns the ClassInfo for the classname passed in,
   * or null if null is passed in.
   * @throws JPFException if class cannot be found (by BCEL)
   */
  public static synchronized ClassInfo getClassInfo (String className) {
    if (className == null) {
      return null;
    }
    
    String slashName = className.replace('.', '/');
    String dotName = className.replace('/', '.');
    
    ClassInfo ci;
    IntTable.Entry<String> entry = globalIdTable.get(dotName);
    
    if (entry != null) {
      ci = loadedClasses.get(entry.val);
    } else if (isBuiltinClass(dotName)) {
      // this is a array class - there's no class file for this, it
      // gets automatically generated by the VM
      ci = new ClassInfo(dotName);
    } else {
      InputStream is = null;
      try {
        ClassFile file = modelClassPath.getClassFile(slashName, ".class");
        if (file != null) {
          is = file.getInputStream();
        }
      } catch (IOException ioe) {
      }
      if (is == null) {
        is = thisClassLoader.getResourceAsStream(slashName + ".class");
      }
      if (is == null) {
        throw new JPFException("could not load class " + dotName);
      }
      try {
        ClassParser parser = new ClassParser(is, className);
        JavaClass clazz = parser.parse();
        ci = new ClassInfo(clazz);
        JVM.getVM().annotations.loadAnnotations(ci, clazz.getAnnotationEntries());
      } catch (IOException e) {
        throw new JPFException("While loading class " + dotName + ": " +
            e.toString());
      }
    }
    return ci;
  }
  
  public boolean areAssertionsEnabled() {
    return enableAssertions;
  }
  
  public boolean hasInstanceFields () {
    return (instanceDataSize > 0);
  }
  
  public int getClassObjectRef () {
    return (sei != null) ? sei.getClassObjectRef() : -1;
  }
  
  /**
   * Note that 'uniqueName' is the name plus the argument type part of the
   * signature, i.e. everything that's relevant for overloading
   * (besides saving some const space, we also ease reverse lookup
   * of natives that way).
   * Note also that we don't have to make any difference between
   * class and instance methods, because that just matters in the
   * INVOKExx instruction, when looking up the relevant ClassInfo to start
   * searching in (either by means of the object type, or by means of the
   * constpool classname entry).
   */
  public MethodInfo getMethod (String uniqueName, boolean isRecursiveLookup) {
    MethodInfo mi = methods.get(uniqueName);
    
    if ((mi == null) && isRecursiveLookup && (superClass != null)) {
      mi = superClass.getMethod(uniqueName, true);
    }
    
    return mi;
  }
  
  /**
   * almost the same as above, except of that Class.getMethod() doesn't specify
   * the return type. Not sure if that is a bug in the Java specs waiting to be
   * fixed, or if covariant return types are not allowed in reflection lookup.
   * Until then, it's awfully inefficient
   */
  public MethodInfo getReflectionMethod (String fullName, boolean isRecursiveLookup) {
    for (Map.Entry<String, MethodInfo>e : methods.entrySet()) {
      String name = e.getKey();
      if (name.startsWith(fullName)) {
        return e.getValue();
      }
    }
    
    if (isRecursiveLookup && (superClass != null)) {
      return superClass.getReflectionMethod(fullName, true);
    }
    
    return null;
  }
  
  
  /**
   * Search up the class hierarchy to find a static field
   * @param fName - name of field
   * @return returns null if field name not found (not declared)
   */
  public FieldInfo getStaticField (String fName) {
    FieldInfo fi;
    ClassInfo c = this;
    
    while (c != null) {
      fi = c.getDeclaredStaticField(fName);
      if (fi != null) return fi;
      c = c.superClass;
    }
    
    //interfaces can have static fields too
    for (String interface_name : getAllInterfaces()) {
        fi = ClassInfo.getClassInfo(interface_name).getDeclaredStaticField(fName);
        if (fi != null) return fi;
    }

    return null;
  }
  
  /**
   * FieldInfo lookup in the static fields that are declared in this class
   * <2do> pcm - should employ a map at some point, but it's usually not that
   * important since we can cash the returned FieldInfo in the PUT/GET_STATIC insns
   */
  public FieldInfo getDeclaredStaticField (String fName) {
    for (int i=0; i<sFields.length; i++) {
      if (sFields[i].getName().equals(fName)) return sFields[i];
    }
    
    return null;
  }
  
  /**
   * base relative FieldInfo lookup - the workhorse
   * <2do> again, should eventually use Maps
   * @param clsBase - the class where we start the lookup (self or some super)
   * @param fName - the field name
   */
  public FieldInfo getInstanceField (String fName) {
    FieldInfo fi;
    ClassInfo c = this;
    
    while (c != null) {
      fi = c.getDeclaredInstanceField(fName);
      if (fi != null) return fi;
      c = c.superClass;
    }
    
    return null;
  }
    
  /**
   * FieldInfo lookup in the fields that are declared in this class
   */
  public FieldInfo getDeclaredInstanceField (String fName) {
    for (int i=0; i<iFields.length; i++) {
      if (iFields[i].getName().equals(fName)) return iFields[i];
    }
    
    return null;
  }
  
    
  /**
   * Returns the name of the class.  e.g. "java.lang.String".  similar to
   * java.lang.Class.getName().
   */
  public String getName () {
    return name;
  }
  
  public String getPackageName () {
    return packageName;
  }
  
  public int getUniqueId() {
    return uniqueId;
  }
  
  public int getFieldAttrs (int fieldIndex) {
    return 0;
  }
  
  public int getElementInfoAttrs () {
    return elementInfoAttrs;
  }
  
  public Source getSource () {
    if (source == null) {
      source = loadSource();
    }
    
    return source;
  }
  
  public String getSourceFileName () {
    return sourceFileName;
  }
  
  /**
   * Returns the information about a static field.
   */
  public FieldInfo getStaticField (int index) {
    return sFields[index];
  }
  
  /**
   * Returns the name of a static field.
   */
  public String getStaticFieldName (int index) {
    return getStaticField(index).getName();
  }
  
  /**
   * Checks if a static method call is deterministic, but only for
   * abtraction based determinism, due to Bandera.choose() calls
   */
  public boolean isStaticMethodAbstractionDeterministic (ThreadInfo th,
                                                         MethodInfo mi) {
    //    Reflection r = reflection.instantiate();
    //    return r.isStaticMethodAbstractionDeterministic(th, mi);
    // <2do> - still has to be implemented
    return true;
  }
  
  /**
   * Return the super class.
   */
  public ClassInfo getSuperClass () {
    return superClass;
  }
  
  /**
   * return the ClassInfo for the provided superclass name. If this is equal
   * to ourself, return this (a little bit strange if we hit it in the first place)
   */
  public ClassInfo getSuperClass (String clsName) {
    if (clsName.equals(name)) return this;
    
    if (superClass != null) {
      return superClass.getSuperClass(clsName);
    } else {
      return null;
    }
  }
  
  public boolean isInstanceOf (ClassInfo ci) {
    ClassInfo c = this;
    do {
      if (c == ci) {
        return true;
      }
      c = c.superClass;
    } while (c != null);
    
    return false;
  }
  
  /**
   * Returns true if the class is a system class.
   */
  public boolean isSystemClass () {
    return name.startsWith("java.") || name.startsWith("javax.");
  }
  
  /**
   * <2do> that's stupid - we should use subclasses for builtin and box types
   */
  public boolean isBoxClass () {
    if (name.startsWith("java.lang.")) {
      String rawType = name.substring(10);
      if (rawType.startsWith("Boolean") ||
          rawType.startsWith("Byte") ||
          rawType.startsWith("Character") ||
          rawType.startsWith("Integer") ||
          rawType.startsWith("Float") ||
          rawType.startsWith("Long") ||
          rawType.startsWith("Double")) {
        return true;
      }
    }
    return false;
  }
    
  /**
   * Returns the type of a class.
   */
  public String getType () {
    return "L" + name.replace('.', '/') + ";";
  }
  
  /**
   * is this a (subclass of) WeakReference? this must be efficient, since it's
   * called in the mark phase on all live objects
   */
  public boolean isWeakReference () {
    return isWeakReference;
  }
  
  /**
   * note this only returns true is this is really the java.lang.ref.Reference classInfo
   */
  public boolean isRefClass () {
    return (this == refClassInfo);
  }
  
  /**
   * whether this refers to a primitive type.
   */
  public boolean isPrimitive() {
    return superClass == null && this != objectClassInfo;
  }
  
  /**
   * Creates the fields for an object.
   */
  public Fields createInstanceFields () {
    return new DynamicFields(getType(), this);
  }
  
  boolean hasRefField (int ref, Fields fv) {
    ClassInfo c = this;
    
    do {
      FieldInfo[] fia = c.iFields;
      for (int i=0; i<fia.length; i++) {
        FieldInfo fi = c.iFields[i];
        if (fi.isReference() && (fv.getIntValue( fi.getStorageOffset()) == ref)) return true;
      }
      c = c.superClass;
    } while (c != null);
    
    return false;
  }
  
  boolean hasImmutableInstances () {
    return ((elementInfoAttrs & ElementInfo.ATTR_IMMUTABLE) != 0);
  }
    
  public NativePeer getNativePeer () {
    return nativePeer;
  }
  
  /**
   * @deprecated doesn't appear to be used/important -peterd
   */
  public static boolean exists (String cname) {
    assert false : "Unsupported operation";
    return globalIdTable.hasEntry(cname);
  }
    
  /**
   * Returns true if the given class is an instance of the class
   * or interface specified.
   */
  public boolean instanceOf (String cname) {
    cname = cname.replace('/', '.');
    
    // trivial case - ourself
    if (name.equals(cname)) {
      return true;
    }
    
    // (recursive) parent
    if ((superClass != null) && (superClass.instanceOf(cname))) {
      return true;
    }
    
    // direct interface
    if (interfaces.contains(cname)) {
      return true;
    }
    
    // now it's getting more expensive - look for all interfaces
    if (getAllInterfaces().contains(cname)) {
      return true;
    }
    
    // Ok, we give up
    return false;
  }
  
  /**
   * clean up statics for another 'main' run
   */
  public static void reset () {
    loadedClasses.clear();
    globalIdTable.clear();
    
    classClassInfo = null;
    objectClassInfo = null;
    stringClassInfo = null;
  }

  /**
   * provide a default path from where to load essential model classes
   * (associated with native peers that JPF needs)
   */
  static String getDefaultBootClassPath () {
    StringBuffer sb = new StringBuffer();
    
    // assuming we are in the JPF root dir, add build/env/jpf for explicit classes
    sb.append("build");
    sb.append(File.separatorChar);
    sb.append("env");
    sb.append(File.separatorChar);
    sb.append("jpf");
    
    // but maybe this is a binary distrib, so add lib/env_jpf.jar
    sb.append(File.pathSeparatorChar);
    sb.append("lib");
    sb.append(File.separatorChar);
    sb.append("env_jpf.jar");

    return sb.toString();
  }
  
  /**
   * this is for application specific classes that should not be seen by the host VM 
   */
  static String getDefaultClassPath () {
    return null;
  }
  
  protected static void buildModelClassPath (Config config) {
    StringBuffer buf = new StringBuffer(128);
    String  sep = System.getProperty("path.separator");
    String  v, param;

    // this is where we get our essential model classes from (java.lang.Thread etc)
    param = config.getExpandedString("vm.bootclasspath");
    if (param == null) { 
      v = getDefaultBootClassPath();
    } else {
      v = param;
    }
    buf.append(v);
    
    // that's where the application specific environment should be loaded from
    param = config.getExpandedString("vm.classpath");
    if (param == null) { 
      v = getDefaultClassPath();
    } else {
      v = param;
    }

    if (v != null) {
      buf.append(sep);
      buf.append(v);
    }

    // part of broken hack for getting annotation information -peterd
    /*
    modelSpecificClassPath = new ClassPath(buf.toString());
    modelHostClassLoader = new ModelHostClassLoader();
    */
    
    // now we look into the system classpath (all stuff loaded from here is
    // the same codebase that's also used by the host VM)
    if (buf.length() > 0) {
      buf.append(sep);
    }
    
    buf.append(System.getProperty("java.class.path"));
    
    // finally, we load from the standard Java libraries
    if (buf.length() > 0) {
      buf.append(sep);
    }
    
    buf.append(System.getProperty("sun.boot.class.path"));
    
    modelClassPath = new ClassPath(buf.toString());
  }
  
  // part of broken hack for getting annotation information -peterd
  /*
  static class ModelHostClassLoader extends ClassLoader  {
    ModelHostClassLoader() {
      super(thisClassLoader);
    }
    
    @Override
    protected URL findResource(String name) {
      return modelSpecificClassPath.getResource(name);
    }

    @Override
    protected Enumeration<URL> findResources(final String name) throws IOException {
      return new Enumeration<URL>() {
        URL cur = findResource(name);
        public boolean hasMoreElements() {
          return cur != null;
        }
        public URL nextElement() {
          if (cur == null) throw new NoSuchElementException();
          URL ret = cur;
          cur = null;
          return ret;
        }
      };
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
      if (name.startsWith("java.")) throw new ClassNotFoundException();
      try {
        ClassFile f = modelSpecificClassPath.getClassFile(name.replace('.', '/'));
        byte[] bytes = new byte[(int) f.getSize()];
        DataInputStream dis = new DataInputStream(f.getInputStream());
        dis.readFully(bytes);
        dis.close();
        return defineClass(name, bytes, 0, bytes.length);
      } catch (IOException e) {
        throw new ClassNotFoundException("Error loading " + name, e);
      }
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
      Class<?> ret = findLoadedClass(name);
      if (ret == null) {
        ClassNotFoundException ex = null;
        Class<?> a = null, b = null;
        try {
          a = thisClassLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
          ex = e;
        }
        try {
          b = findClass(name); 
        } catch (ClassNotFoundException e) {
          ex = e;
        }
        if (a != null) {
          if (b != null) {
            if (Annotation.class.isAssignableFrom(a)) {
              ret = a; // default to us for annotations
              // TODO: more checking/breakage?
            } else {
              ret = b; // default to model for non-annotations
            }
          } else {
            ret = a;
          }
        } else { // a == null
          if (b != null) {
            ret = b;
          } else {
            throw ex;
          }
        }
      }
      if (resolve) {
        resolveClass(ret);
      }
      return ret;
    }
  }
  */
  
  protected static Set<String> loadArrayInterfaces () {
    Set<String> interfaces;
    
    interfaces = new HashSet<String>();
    interfaces.add("java.lang.Cloneable");
    interfaces.add("java.io.Serializable");
    
    return Collections.unmodifiableSet(interfaces);
  }
  
  protected static Set<String> loadBuiltinInterfaces (String type) {
    return Collections.unmodifiableSet(new HashSet<String>(0));
  }
  
  /**
   * Loads the interfaces of a class.
   */
  protected static Set<String> loadInterfaces (JavaClass jc) {
    Set<String> interfaces;
    String[]    interfaceNames;
    
    interfaceNames = jc.getInterfaceNames();
    interfaces = new HashSet<String>();
    
    for (int i = 0, l = interfaceNames.length; i < l; i++) {
      interfaces.add(interfaceNames[i]);
    }
    
    return Collections.unmodifiableSet(interfaces);
  }
    
  FieldInfo[] loadInstanceFields (JavaClass jc) {
    Field[] fields = jc.getFields();
    int i, j, n;
    int off = (superClass != null) ? superClass.instanceDataSize : 0;
    
    for (i=0, n=0; i<fields.length; i++) {
      if (!fields[i].isStatic()) n++;
    }
    
    int idx = (superClass != null) ? superClass.nInstanceFields : 0;
    FieldInfo[] ifa = new FieldInfo[n];
    
    for (i=0, j=0; i<fields.length; i++) {
      Field f = fields[i];
      if (!f.isStatic()) {
        FieldInfo fi = FieldInfo.create(f, this, idx, off);
        ifa[j++] = fi;
        off += fi.getStorageSize();
        idx++;
        
        if (attributor != null) {
          fi.setAttributes( attributor.getFieldAttributes(jc, f));
        }
      }
    }
    
    return ifa;
  }
  
  int computeInstanceDataOffset () {
    if (superClass == null) {
      return 0;
    } else {
      return superClass.getInstanceDataSize();
    }
  }
  
  int getInstanceDataOffset () {
    return instanceDataOffset;
  }
  
  ClassInfo getClassBase (String clsBase) {
    if ((clsBase == null) || (name.equals(clsBase))) return this;
    
    if (superClass != null) {
      return superClass.getClassBase(clsBase);
    }
    
    return null; // Eeek - somebody asked for a class that isn't in the base list
  }
  
  int computeInstanceDataSize () {
    int n = getDataSize( iFields);
    
    for (ClassInfo c=superClass; c!= null; c=c.superClass) {
      n += c.getDataSize(c.iFields);
    }
    
    return n;
  }
  
  public int getInstanceDataSize () {
    return instanceDataSize;
  }
  
  int getDataSize (FieldInfo[] fields) {
    int n=0;
    for (int i=0; i<fields.length; i++) {
      n += fields[i].getStorageSize();
    }
    
    return n;
  }
  
  public int getNumberOfDeclaredInstanceFields () {
    return iFields.length;
  }
  
  public FieldInfo getDeclaredInstanceField (int i) {
    return iFields[i];
  }
  
  public int getNumberOfInstanceFields () {
    return nInstanceFields;
  }
  
  public FieldInfo getInstanceField (int i) {
    int idx = i - (nInstanceFields - iFields.length);
    if (idx >= 0) {
      return ((idx < iFields.length) ? iFields[idx] : null);
    } else {
      return ((superClass != null) ? superClass.getInstanceField(i) : null);
    }
  }
  
  FieldInfo[] loadStaticFields (JavaClass jc) {
    Field[] fields = jc.getFields();
    int i, n;
    int off = 0;
    
    for (i=0, n=0; i<fields.length; i++) {
      if (fields[i].isStatic()) n++;
    }
    
    FieldInfo[] sfa = new FieldInfo[n];
    int idx = 0;
    
    for (i=0; i<fields.length; i++) {
      Field f = fields[i];
      if (f.isStatic()) {
        FieldInfo fi = FieldInfo.create(f, this, idx, off);
        sfa[idx] = fi;
        idx++;
        off += fi.getStorageSize();
      }
    }
    
    return sfa;
  }
  
  public int getStaticDataSize () {
    return staticDataSize;
  }
  
  int computeStaticDataSize () {
    return getDataSize(sFields);
  }
  
  public int getNumberOfStaticFields () {
    return sFields.length;
  }
  
  protected Source loadSource () {
    return Source.getSource(sourceFileName);
  }
  
  static boolean isBuiltinClass (String cname) {
    char c = cname.charAt(0);
    
    // array class
    if (c == '[') {
      return true;
    }
    
    // primitive type class
    if (Character.isLowerCase(c)) {
      if ("int".equals(cname) || "byte".equals(cname) ||
          "boolean".equals(cname) || "double".equals(cname) ||
          "long".equals(cname) || "char".equals(cname) ||
          "short".equals(cname) || "float".equals(cname)) {
        return true;
      }
    }
    
    return false;
  }
  
  /**
   * set the locations where we look up sources
   */
  static void setSourceRoots (Config config) {
    String srcPath = config.getExpandedString("vm.sourcepath");
    if (srcPath == null) { // our fallbacks, assuming we are in the JPF root dir
      Source.addSourceRoot("examples");
      Source.addSourceRoot("test");
    } else {
      StringTokenizer st = new StringTokenizer(srcPath, File.pathSeparator);
      while (st.hasMoreTokens()) {
        String sroot = st.nextToken();
        
        if ((sroot.length() > 0) && !sroot.endsWith(".jar")) {
          Source.addSourceRoot(sroot);
        }
      }
    }
  }
  
  /**
   * get names of all interfaces
   * @return a Set of String interface names
   */
  Set<String> getAllInterfaces () {
    if (allInterfaces == null) {
      HashSet<String> set = new HashSet<String>();
      
      
      // load all own interfaces
      loadInterfaceRec(set, this);
      
      
      // all parent class interfaces
      loadInterfaceRec(set, superClass);
      
      allInterfaces = Collections.unmodifiableSet(set);
    }
    
    return allInterfaces;
  }
  
  ClassInfo getComponentClassInfo () {
    if (isArray()) {
      String cn = name.substring(1);
      
      if (cn.charAt(0) != '[') {
        cn = Types.getTypeName(cn);
      }
      
      ClassInfo cci = getClassInfo(cn);
      
      return cci;
    }
    
    return null;
  }
  
  /**
   * most definitely not a public method, but handy for the NativePeer
   */
  Map<String, MethodInfo> getDeclaredMethods () {
    return methods;
  }
  
  MethodInfo getFinalizer () {
    return finalizer;
  }
    
  public int createClassObject (ThreadInfo th, int cref) {
    int         objref;
    int         cnref;
    DynamicArea da = th.getVM().getDynamicArea();
    
    objref = da.newObject(classClassInfo, th);
    cnref = da.newString(name, th);
    
    // we can't execute methods nicely for which we don't have caller bytecode
    // (would run into a (pc == null) assertion), so we have to bite the bullet
    // and init the java.lang.Class object explicitly. But that's probably Ok
    // since it is a very special beast, anyway
    ElementInfo e = da.get(objref);
    
    try {
      e.setReferenceField("name", cnref);
      
      // this is the StaticArea ElementInfo index of what we refer to
      e.setIntField("cref", cref);
    } catch (Exception x) {
      // if we don't have the right (JPF specific) java.lang.Class version,
      // we are screwed in terms of java.lang.Class usage
      if (classClassInfo == null) { // report it just once
        logger.severe("FATAL ERROR: wrong java.lang.Class version (wrong 'vm.classpath' property)");
      }
      
      return -1;
    }
    
    return objref;
  }
  
  public boolean isInitializing () {
    return ((sei != null) && (sei.getStatus() >= 0));    
  }
  
  public boolean isInitialized () {
    return ((sei != null) && (sei.getStatus() == INITIALIZED));
  }
  
  public boolean needsInitialization () {
    return ((sei == null) || (sei.getStatus() == UNINITIALIZED));
  }
  
  public void setInitializing(ThreadInfo ti) {
    sei.setStatus(ti.getIndex());
  }
  
  protected void setInitialized() {
    sei.setStatus(INITIALIZED);
  }
    
  // this one is for classes w/o superclasses or clinits (e.g. array and builtin classes)
  public void loadAndInitialize (ThreadInfo ti) {
    StaticArea sa = ti.getVM().getStaticArea();
    if (!sa.containsClass(name)) {
      sa.addClass(this, ti);
      setInitialized();
    }
  }
  
  public int loadAndInitialize (ThreadInfo ti, Instruction continuation) {
    ClassInfo  ci = this;
    StaticArea sa = ti.getVM().getStaticArea();
    int pushedFrames = 0;

    while (ci != null) {
      if (!sa.containsClass(ci.getName())) {
        sa.addClass(ci, ti);
      }
      
      int stat = ci.sei.getStatus();
          
      if (stat != INITIALIZED) {
        if (stat != ti.getIndex()) {
          // even if it's already initializing - if it's not in the current thread
          // we have to sync, which we do by calling clinit
          MethodInfo mi = ci.getMethod("<clinit>()V", false);          
          if (mi != null) {
            MethodInfo stub = mi.createDirectCallStub("[clinit]");
            StackFrame sf = new DirectCallStackFrame(stub, continuation);
            ti.pushFrame( sf);
            
            continuation = null;
            pushedFrames++;
          } else {
            // it has no clinit, so it already is initialized
            ci.sei.setStatus(INITIALIZED);
          }
        } else {
          // if it's initialized by our own thread (recursive request), just go on
        }
      }

      ci = ci.getSuperClass();
    }
    
    return pushedFrames;
  }
  
  protected void setStaticElementInfo (StaticElementInfo sei) {
    this.sei = sei;
  }
  
  public StaticElementInfo getStaticElementInfo () {
    return sei;
  }
  
  /**
   * Creates the fields for a class.  This gets called by the StaticArea
   * when a class is loaded.
   */
  Fields createStaticFields () {
    return new StaticFields(this);
  }
  
  void initializeStaticData (Fields f) {
    for (int i=0; i<sFields.length; i++) {
      FieldInfo fi = sFields[i];
      fi.initialize(f);
    }
  }
  
  void initializeInstanceData (Fields f) {
    // Note this is only used for field inits, and array elements are not fields!
    // Since Java has only limited element init requirements (either 0 or null),
    // we do this ad hoc in the ArrayFields ctor
    
    // the order of inits should not matter, since this is only
    // for constant inits. In case of a "class X { int a=42; int b=a; ..}"
    // we have a explicit "GETFIELD a, PUTFIELD b" in the ctor
    for (int i=0; i<iFields.length; i++) {
      FieldInfo fi = iFields[i];
      fi.initialize(f);
    }
    if (superClass != null) {
      superClass.initializeInstanceData(f);
    }
  }
  
  Map<String, MethodInfo> loadArrayMethods () {
    return new HashMap<String, MethodInfo>(0);
  }
  
  Map<String, MethodInfo> loadBuiltinMethods (String type) {
    return new HashMap<String, MethodInfo>(0);
  }
  
  /**
   * Loads the ClassInfo for named class.  
   * @param set - a Set to which the interface names (String) are added
   * @param ci - class to find interfaces for.
   */
  void loadInterfaceRec (Set<String> set, ClassInfo ci) {
    if (ci != null) {
      for (String iname : ci.interfaces) {
        set.add(iname);
        
        ci = getClassInfo(iname);
        loadInterfaceRec(set, ci);
      }
    }
  }
  
  /**
   * this is a optimization to work around the BCEL strangeness that some
   * insn info (types etc.) are only accessible with modifiable ConstPools
   * (the ConstantPoolGen, which is costly to create), and some others
   * (toString) are only provided via ConstPools. It's way to expensive
   * to create this always on the fly, for each relevant insn, so we cache it
   * here
   */
  static ConstantPool cpCache;
  static ConstantPoolGen cpgCache;
  
  public static ConstantPoolGen getConstantPoolGen (ConstantPool cp) {
    if (cp != cpCache) {
      cpCache = cp;
      cpgCache = new ConstantPoolGen(cp);
    }
    
    return cpgCache;
  }
  
  /** avoid memory leaks */
  static void resetCPCache () {
    cpCache = null;
    cpgCache = null;
  }
  
  Map<String, MethodInfo> loadMethods (JavaClass jc) {
    Method[] ms = jc.getMethods();
    HashMap<String,MethodInfo>  map = new HashMap<String,MethodInfo>(ms.length);
    
    for (int i = 0; i < ms.length; i++) {
      MethodInfo mi = new MethodInfo( ms[i], this);
      String id = mi.getUniqueName();
      map.put(id, mi);
    
      if (attributor != null) {
        mi.setAtomic( attributor.isMethodAtomic(jc, ms[i], id));
        mi.setSchedulingRelevance( attributor.getSchedulingRelevance( jc, ms[i], id));
      }
    }
    
    resetCPCache(); // no memory leaks
    
    return map;
  }
  
  ClassInfo loadSuperClass (JavaClass jc) {
    if (this == objectClassInfo) {
      return null;
    } else {
      String superName = jc.getSuperclassName();
      
      return getClassInfo(superName);
    }
  }
  
  int loadElementInfoAttrs (JavaClass jc) {
    int attrs = 0;
    // we use the atomicizer for it because the only attribute for now is the
    // immutability, and it is used to determine if a field insn should be
    // a step boundary. Otherwise it's a bit artificial, but we don't want
    // to intro another load time class attributor for now
    if (attributor != null) {
      attrs = attributor.getObjectAttributes(jc);
    }
    
    // if it has no fields, it is per se immutable
    if (!isArray && (instanceDataSize == 0)) {
      attrs |= ElementInfo.ATTR_IMMUTABLE;
    }

    return attrs;
  }

  public String toString() {
    return "ClassInfo[name=" + name + "]";
  }
  
  private MethodInfo getFinalizer0 () {
    MethodInfo mi = getMethod("finalize()V", true);
    
    // we are only interested in non-empty method bodies, Object.finalize()
    // is a dummy
    if ((mi != null) && (mi.getClassInfo() != objectClassInfo)) {
      return mi;
    }
    
    return null;
  }
  
  private boolean isWeakReference0 () {
    for (ClassInfo ci = this; ci != objectClassInfo; ci = ci.superClass) {
      if (ci == weakRefClassInfo) {
        return true;
      }
    }
    
    return false;
  }
}


