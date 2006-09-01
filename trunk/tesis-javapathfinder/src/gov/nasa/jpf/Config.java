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
package gov.nasa.jpf;

import gov.nasa.jpf.util.ObjArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * class that encapsulates property-based JPF configuration. This is mainly an
 * associative array with various typed accessors, and a structured
 * initialization process. This implementation has the design constraint that it
 * does not promote symbolic information to concrete types, which means that
 * frequently accessed data should be promoted and cached in client classes.
 * This in turn means we assume the data is not going to change at runtime.
 * Major motivation for this mechanism is to avoid 'Option' classes that have
 * concrete type fields, and hence are structural bottlenecks, i.e. every
 * parameterized user extension (Heuristics, Scheduler etc.) require to update
 * this single class. Note that Config is also not thread safe with respect to
 * retrieving exceptions that occurrred during instantiation
 * 
 * Another important caveat for both implementation and usage of Config is that
 * it is supposed to be our master configuration mechanism, i.e. it is also used
 * to configure other core services like logging. This means that Config
 * initialization should not depend on these services. Initialization has to
 * return at all times, recording potential problems for later handling. This is
 * why we have to keep the Config data model and initialization fairly simple
 * and robust.
 */
@SuppressWarnings("serial")
public class Config extends Properties {
  static final String TARGET_KEY = "target";

  static final String TARGET_ARGS_KEY = "target_args";

  static final String DELIMS = "[:;, ]+";  // for String arrays

  /**
   * this class wraps the various exceptions we might encounter esp. during
   * reflection instantiation
   */
  public class Exception extends java.lang.Exception {
    public Exception(String msg) {
      super(msg);
    }

    public Exception(String msg, Throwable cause) {
      super(msg, cause);
    }

    public Exception(String key, Class<?> cls, String failure) {
      super("error instantiating class " + cls.getName() + " for entry \""
          + key + "\":" + failure);
    }

    public Exception(String key, Class<?> cls, String failure, Throwable cause) {
      this(key, cls, failure);
      initCause(cause);
    }

    public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("JPF configuration error: ");
      sb.append(getMessage());

      return sb.toString();
    }
    
    public Config getConfig() {
      return Config.this;
    }
  }
  
  String fileName;

  Object source;

  boolean gotProperties;

  /**
   * all arguments that are not <key>= <value>pairs
   */
  String[] freeArgs;

  /**
   * an [optional] hashmap to keep objects we want to keep as singletons
   */
  HashMap<String,Object> singletons;
  
  /**
   * this is our internal defaults ctor
   */
  private Config(String alternatePath, Class<?> codeBase) {
    gotProperties = loadFile("default.properties", alternatePath, codeBase);
    normalizeValues();
  }

  public Config(String[] args, String fileName, String alternatePath,
                Class<?> codeBase) {
    super(new Config(alternatePath,
        (codeBase == null) ? codeBase = getCallerClass(1) : codeBase));

    this.fileName = fileName;
    gotProperties = loadFile(fileName, alternatePath, codeBase);

    if (args != null){
      processArgs(args);
    }
    normalizeValues();
  }

  public boolean gotDefaultProperties() {
    if (defaults != null && defaults instanceof Config) {
      return ((Config) defaults).gotProperties();
    }
    return false;
  }

  public boolean gotProperties() {
    return gotProperties;
  }

  public String getFileName() {
    return fileName;
  }

  public String getSourceName() {
    if (source == null) {
      return null;
    } else if (source instanceof File) {
      return ((File) source).getAbsolutePath();
    } else if (source instanceof URL) {
      return source.toString();
    } else {
      return source.toString();
    }
  }

  public String getDefaultsSourceName() {
    if ((defaults != null) && (defaults instanceof Config)) {
      return ((Config) defaults).getSourceName();
    } else {
      return null;
    }
  }

  public Object getSource() {
    return source;
  }

  public String[] getArgs() {
    return freeArgs;
  }

  public Config.Exception exception (String msg) {
    return new Config.Exception(msg);
  }
  
  public void throwException(String msg) throws Exception {
    throw new Exception(msg);
  }

  /**
   * find callers class
   * 
   * @param up -
   *          levels upwards from our caller (NOT counting ourselves)
   * @return caller class, null if illegal 'up' value
   */
  public static Class<?> getCallerClass(int up) {
    int idx = up + 1; // don't count ourselves

    StackTraceElement[] st = (new Throwable()).getStackTrace();
    if ((up < 0) || (idx >= st.length)) {
      return null;
    } else {
      try {
        return Class.forName(st[idx].getClassName());
      } catch (Throwable t) {
        return null;
      }
    }
  }

  boolean loadFile(String fileName, String alternatePath, Class<?> codeBase) {
    InputStream is = null;

    try {
      // first, try to load from a file
      File f = new File(fileName);
      if (!f.exists()) {
        // Ok, try alternatePath, if fileName wasn't absolute
        if (!f.isAbsolute() && (alternatePath != null)) {
          f = new File(alternatePath, fileName);
        }
      }

      if (f.exists()) {
        source = f;
        is = new FileInputStream(f);
      } else {
        // if there is no file, try to load as a resource (jar)
        Class<?> clazz = (codeBase != null) ? codeBase : Config.class;
        is = clazz.getResourceAsStream(fileName);
        if (is != null) {
          source = clazz.getResource(fileName); // a URL
        }
      }

      if (is != null) {
        load(is);
        return true;
      }
    } catch (IOException iex) {
      return false;
    }

    return false;
  }

  /**
   * extract all "+ <key>= <val>" parameters, store/overwrite them in our
   * dictionary, collect all other parameters in a String array
   * 
   * @param args -
   *          array of String parameters to process
   */
  void processArgs(String[] args) {
    int i;
    ArrayList<String> list = new ArrayList<String>();

    for (i = 0; i < args.length; i++) {
      String a = args[i];
      if (a != null) {
        if (a.charAt(0) == '+') {
          int idx = a.indexOf("=");
          if (idx > 0) {
            String key = a.substring(1, idx).trim();
            String val = a.substring(idx+1).trim();
            setProperty(key, val);
          } else {
            setProperty(a.substring(1), "");
          }
        } else {
          list.add(a);
        }
      }
    }

    int n = list.size();
    freeArgs = new String[n];
    for (i = 0; i < n; i++) {
      freeArgs[i] = list.get(i);
    }
  }

  /**
   * return the index of the first free argument that does not start with an
   * hyphen
   */
  public int getNonOptionArgIndex() {
    if ((freeArgs == null) || (freeArgs.length == 0))
      return -1;

    for (int i = 0; i < freeArgs.length; i++) {
      String a = freeArgs[i];
      if (a != null) {
        char c = a.charAt(0);
        if (c != '-') {
          return i;
        }
      }
    }

    return -1;
  }

  /**
   * return the first non-option freeArg, or 'null' if there is none (usually
   * denotes the application to start)
   */
  public String getTargetArg() {
    int i = getNonOptionArgIndex();
    if (i < 0) {
      return getString(TARGET_KEY);
    } else {
      return freeArgs[i];
    }
  }

  /**
   * return all args that follow the first non-option freeArgs (usually denotes
   * the parametsr to pass to the application to start)
   */
  public String[] getTargetArgParameters() {
    int i = getNonOptionArgIndex();
    if (i >= freeArgs.length - 1) {
      String[] a = getStringArray(TARGET_ARGS_KEY);
      if (a != null) {
        return a;
      } else {
        return new String[0];
      }
    } else {
      int n = freeArgs.length - (i + 1);
      String[] a = new String[n];
      System.arraycopy(freeArgs, i + 1, a, 0, n);
      return a;
    }
  }

  public String getArg(int i) {
    if (freeArgs == null)
      return null;
    if (freeArgs.length - 1 < i)
      return null;
    if (i < 0)
      return null;

    return freeArgs[i];
  }

  /**
   * turn standard type values (boolean etc.) into common formats
   * ("true"/"false" for booleans)
   */
  void normalizeValues() {
    for (Enumeration<?> keys = propertyNames(); keys.hasMoreElements();) {
      String k = (String) keys.nextElement();
      String v = getProperty(k);
      
      // trim heading and trailing blanks (at least Java 1.4.2 does not take care of trailing blanks)
      String v0 = v;
      v = v.trim();
      if (v != v0) {
        put(k, v);
      }      
      
      if ("true".equalsIgnoreCase(v) || "t".equalsIgnoreCase(v)
          || "yes".equalsIgnoreCase(v) || "y".equalsIgnoreCase(v)) {
        put(k, "true");
      } else if ("false".equalsIgnoreCase(v) || "f".equalsIgnoreCase(v)
          || "no".equalsIgnoreCase(v) || "n".equalsIgnoreCase(v)) {
        put(k, "false");
      }
    }
  }

  public boolean getBoolean(String key) {
    String v = getProperty(key);
    return (v != null) && ("true".equals(v));
  }

  public boolean getBoolean(String key, boolean def) {
    String v = getProperty(key);
    if (v != null) {
      return ("true".equals(v));
    } else {
      return def;
    }
  }

  public int[] getIntArray (String key) throws Exception {
    String v = getProperty(key);
    
    if (v != null) {
      String[] sa = v.split("[:;, ]+");
      int[] a = new int[sa.length];
      int i = 0;
      try {
        for (; i<sa.length; i++) {
          a[i] = Integer.parseInt(sa[i]);
        }
        return a;
      } catch (NumberFormatException nfx) {
        throw new Exception("illegal int[] element in '" + key + "' = \"" + sa[i] + '"');
      }
    } else {
      return null;
    }
  }

  public int getInt(String key) {
    return getInt(key, 0);
  }

  public int getInt(String key, int defValue) {
    String v = getProperty(key);
    if (v != null) {
      try {
        return Integer.parseInt(v);
      } catch (NumberFormatException nfx) {
        return defValue;
      }
    }

    return defValue;
  }

  public long getLong(String key) {
    return getLong(key, 0L);
  }

  public long getLong(String key, long defValue) {
    String v = getProperty(key);
    if (v != null) {
      try {
        return Long.parseLong(v);
      } catch (NumberFormatException nfx) {
        return defValue;
      }
    }

    return defValue;
  }

  public long[] getLongArray (String key) throws Exception {
    String v = getProperty(key);
    
    if (v != null) {
      String[] sa = v.split("[:;, ]+");
      long[] a = new long[sa.length];
      int i = 0;
      try {
        for (; i<sa.length; i++) {
          a[i] = Long.parseLong(sa[i]);
        }
        return a;
      } catch (NumberFormatException nfx) {
        throw new Exception("illegal long[] element in " + key + " = " + sa[i]);
      }
    } else {
      return null;
    }
  }

  
  public double getDouble (String key) {
    return getDouble(key, 0.0);
  }
  
  public double getDouble (String key, double defValue) {
    String v = getProperty(key);
    if (v != null) {
      try {
        return Double.parseDouble(v);
      } catch (NumberFormatException nfx) {
        return defValue;
      }
    }

    return defValue;    
  }

  public double[] getDoubleArray (String key) throws Exception {
    String v = getProperty(key);
    
    if (v != null) {
      String[] sa = v.split("[:;, ]+");
      double[] a = new double[sa.length];
      int i = 0;
      try {
        for (; i<sa.length; i++) {
          a[i] = Double.parseDouble(sa[i]);
        }
        return a;
      } catch (NumberFormatException nfx) {
        throw new Exception("illegal double[] element in " + key + " = " + sa[i]);
      }
    } else {
      return null;
    }
  }

  
  public String getString(String key) {
    return getProperty(key);
  }

  public String getString(String key, String defValue) {
    String s = getProperty(key);
    if (s != null) {
      return s;
    } else {
      return defValue;
    }
  }

  /**
   * same as getString(), except of that we look for '${ <key>}' patterns, and
   * replace them with values if we find corresponding keys. Expansion is not
   * done recursively (but could be)
   */
  public String getExpandedString(String key) {
    int i, j = 0;
    String s = getString(key);
    if (s == null || s.length() == 0) {
      return s;
    }

    while ((i = s.indexOf("${", j)) >= 0) {
      if ((j = s.indexOf('}', i)) > 0) {
        String k = s.substring(i + 2, j);
        String v = getString(k, "");
        if (v != null) {
          s = s.substring(0, i) + v + s.substring(j + 1, s.length());
          j = i + v.length();
        } else {
          s = s.substring(0, i) + s.substring(j + 1, s.length());
          j = i;
        }
      }
    }

    return s;
  }

  /**
   * return memory size in bytes, or 'defValue' if not in dictionary. Encoding
   * can have a 'M' or 'k' postfix, values have to be positive integers (decimal
   * notation)
   */
  public long getMemorySize(String key, long defValue) {
    String v = getProperty(key);
    long sz = defValue;

    if (v != null) {
      int n = v.length() - 1;
      try {
        char c = v.charAt(n);

        if ((c == 'M') || (c == 'm')) {
          sz = Long.parseLong(v.substring(0, n)) << 20;
        } else if ((c == 'K') || (c == 'k')) {
          sz = Long.parseLong(v.substring(0, n)) << 10;
        } else {
          sz = Long.parseLong(v);
        }

      } catch (NumberFormatException nfx) {
        return defValue;
      }
    }

    return sz;
  }

  
  public String[] getStringArray(String key) {
    String v = getProperty(key);
    if (v != null) {
      return v.split(DELIMS);
    }

    return null;
  }

  
  /**
   * return an [optional] id part of a property value (all that follows the first '@')
   */
  String getIdPart (String key) {
    String v = getProperty(key);
    if ((v != null) && (v.length() > 0)) {
      int i = v.indexOf('@');
      if (i >= 0){
        return v.substring(i+1);
      }
    }
    
    return null;
  }
  
  public Class<?> getClass(String key) throws Exception {
    String v = getProperty(key);
    if ((v != null) && (v.length() > 0)) {
      
      int i = v.indexOf('@'); // hack off any potential tag
      if (i >=0 ) {
        v = v.substring(0,i);
      }
      
      try {
        return Class.forName(v);
      } catch (ClassNotFoundException cfx) {
        throw new Exception("class not found " + v);
      } catch (ExceptionInInitializerError ix) {
        throw new Exception("class initialization of " + v + " failed: " + ix,
            ix);
      }
    }

    return null;
  }

  public Class<?> getEssentialClass(String key) throws Exception {
    Class<?> cls = getClass(key);
    if (cls == null) {
      throw new Exception("no classname entry for: \"" + key + "\"");
    }

    return cls;
  }

  String stripId (String v) {
    int i = v.indexOf('@');
    if (i >= 0) {
      return v.substring(0,i);
    } else {
      return v;
    }
  }
  
  String getId (String v){
    int i = v.indexOf('@');
    if (i >= 0) {
      return v.substring(i+1);
    } else {
      return null;
    }
  }
  
  public Class[] getClasses(String key) throws Exception {
    String[] v = getStringArray(key);
    if (v != null) {
      int n = v.length;
      Class[] a = new Class[n];
      for (int i = 0; i < n; i++) {
        try {
          String clsName = stripId(v[i]);
          a[i] = Class.forName(clsName);
        } catch (ClassNotFoundException cnfx) {
          throw new Exception("class not found " + v[i]);
        } catch (ExceptionInInitializerError ix) {
          throw new Exception("class initialization of " + v[i] + " failed: "
              + ix, ix);
        }
      }

      return a;
    }

    return null;
  }

  // <2do> - that's kind of cludged together, not very efficient
  String[] getIds (String key) {
    String v = getProperty(key);
    
    if (v != null) {
      int i = v.indexOf('@');
      if (i >= 0) { // Ok, we have ids
        String[] a = v.split(DELIMS);
        String[] ids = new String[a.length];
        for (i = 0; i<a.length; i++) {
          ids[i] = getId(a[i]);
        }
        return ids;
      }
    }
    
    return null;
  }
  
  public <T> ObjArray<T> getInstances(String key, Class<T> type) throws Exception {
    Class[] c = getClasses(key);
    
    if (c != null) {
      String[] ids = getIds(key);

      Class[] argTypes = { Config.class };
      Object[] args = { this };
      ObjArray<T> a = new ObjArray<T>(c.length);

      for (int i = 0; i < c.length; i++) {
        String id = (ids != null) ? ids[i] : null;
        a.set(i,getInstance(key, c[i], type, argTypes, args, id));
      }

      return a;
    }

    return null;
  }

  public <T> T getInstance(String key, Class<T> type) throws Exception {
    Class[] argTypes = { Config.class };
    Object[] args = { this };

    return getInstance(key, type, argTypes, args);
  }

  public <T> T getInstance(String key, Class<T> type, Class[] argTypes,
                            Object[] args) throws Exception {
    Class<?> cls = getClass(key);
    String id = getIdPart(key);
    
    if (cls != null) {
      return getInstance(key, cls, type, argTypes, args, id);
    } else {
      return null;
    }
  }

  public <T> T getEssentialInstance(String key, Class<T> type) throws Exception {
    Class<?>[] argTypes = { Config.class };
    Object[] args = { this };
    return getEssentialInstance(key, type, argTypes, args);
  }

  /**
   * just a convenience method for ctor calls that take two arguments
   */
  public <T> T getEssentialInstance(String key, Class<T> type, Object arg1, Object arg2)  throws Exception {
    Class<?>[] argTypes = new Class<?>[2];
    argTypes[0] = arg1.getClass();
    argTypes[1] = arg2.getClass();
    
    Object[] args = new Object[2];
    args[0] = arg1;
    args[1] = arg2;
    
    return getEssentialInstance(key, type, argTypes, args);
  }

  public <T> T getEssentialInstance(String key, Class<T> type, Class<?>[] argTypes,
                                     Object[] args) throws Exception {
    Class<?> cls = getEssentialClass(key);
    String id = getIdPart(key);

    return getInstance(key, cls, type, argTypes, args, id);
  }

  
  /**
   * this is our private instantiation workhorse try to instantiate an object of
   * class 'cls' by using the following ordered set of ctors 1. <cls>(
   * <argTypes>) 2. <cls>(Config) 3. <cls>() if all of that fails, or there was
   * a 'type' provided the instantiated object does not comply with, return null
   */
  <T> T getInstance(String key, Class<?> cls, Class<T> type, Class[] argTypes,
                     Object[] args, String id) throws Exception {
    Object o = null;
    Constructor<?> ctor = null;

    if (cls == null) {
      return null;
    }

    if (id != null) { // check first if we already have this one instantiated as a singleton
      if (singletons == null) {
        singletons = new HashMap<String,Object>();
      } else {
        o = type.cast(singletons.get(id));
      }
    }
    
    while (o == null) {
      try {
        ctor = cls.getConstructor(argTypes);
        o = ctor.newInstance(args);
      } catch (NoSuchMethodException nmx) {
        
        if ((argTypes.length > 1) || ((argTypes.length == 1) && (argTypes[0] != Config.class))) {
          // fallback 1: try a single Config param
          argTypes = new Class[1];
          argTypes[0] = Config.class;
          args = new Object[1];
          args[0] = this;
          
        } else if (argTypes.length > 0) {
          // fallback 2: try the default ctor
          argTypes = new Class[0];
          args = new Object[0];
          
        } else {
          // Ok, there is no suitable ctor, bail out
          throw new Exception(key, cls, "no suitable ctor found");
        }
      } catch (IllegalAccessException iacc) {
        throw new Exception(key, cls, "\n> ctor not accessible: "
            + getMethodSignature(ctor));
      } catch (IllegalArgumentException iarg) {
        throw new Exception(key, cls, "\n> illegal constructor arguments: "
            + getMethodSignature(ctor));
      } catch (InvocationTargetException ix) {
        Throwable tx = ix.getTargetException();
        if (tx instanceof Config.Exception) {
          throw new Exception(tx.getMessage() + "\n> used within \"" + key
              + "\" instantiation of " + cls);
        } else {
          throw new Exception(key, cls, "\n> exception in "
              + getMethodSignature(ctor) + ":\n>> " + tx, tx);
        }
      } catch (InstantiationException ivt) {
        throw new Exception(key, cls,
            "\n> abstract class cannot be instantiated");
      } catch (ExceptionInInitializerError eie) {
        throw new Exception(key, cls, "\n> static initialization failed:\n>> "
            + eie.getException(), eie.getException());
      }
    }
    
    // check type
    if (!type.isInstance(o)) {
      throw new Exception(key, cls, "\n> instance not of type: "
          + type.getName());
    }

    if (id != null) { // add to singletons (in case it's not already in there)
      singletons.put(id, o);
    }
    
    return type.cast(o); // safe according to above
  }

  String getMethodSignature(Constructor<?> ctor) {
    StringBuffer sb = new StringBuffer();
    sb.append(ctor.getName());
    sb.append('(');
    Class[] argTypes = ctor.getParameterTypes();
    for (int i = 0; i < argTypes.length; i++) {
      if (i > 0) {
        sb.append(',');
      }
      sb.append(argTypes[i].getName());
    }
    sb.append(')');
    return sb.toString();
  }

  /**
   * check if any of the freeArgs matches a regular expression
   * 
   * @param regex -
   *          regular expression to check for
   * @return true if found, false if not found or no freeArgs
   */
  public boolean hasArg(String regex) {
    if (freeArgs == null) {
      return false;
    }

    for (int i = 0; i < freeArgs.length; i++) {
      if (freeArgs[i].matches(regex)) {
        return true;
      }
    }

    return false;
  }

  public boolean hasValue(String key) {
    String v = getProperty(key);
    return ((v != null) && (v.length() > 0));
  }

  public boolean hasValueIgnoreCase(String key, String value) {
    String v = getProperty(key);
    if (v != null) {
      return v.equalsIgnoreCase(value);
    }

    return false;
  }

  public int getChoiceIndexIgnoreCase(String key, String[] choices) {
    String v = getProperty(key);

    if ((v != null) && (choices != null)) {
      for (int i = 0; i < choices.length; i++) {
        if (v.equalsIgnoreCase(choices[i])) {
          return i;
        }
      }
    }

    return -1;
  }

  public void print (PrintWriter pw) {
    pw.println("----------- dictionary contents");
    
    // just how much do you have to do to get a sorted printout :(
    TreeSet<String> kset = new TreeSet<String>();
    
    for (Object o : keySet()) {
      if (o instanceof String) { // should be true
        kset.add((String) o);
      }
    }
    for (String key : kset) {
      String val = getExpandedString(key);
      pw.print(key);
      pw.print(" = ");
      pw.println(val);
    }
    
    if ((freeArgs != null) && (freeArgs.length > 0)) {
      pw.println("----------- free arguments");
      for (int i = 0; i < freeArgs.length; i++) {
        pw.println(freeArgs[i]);
      }
    }
    
    pw.flush();
  }

  public void printStatus(Logger log) {
    log.config("configuration initialized from: " + getSourceName());

    Config def = (Config) defaults;
    if (def.source == null) {
      log.warning("no defaults.properties found");
    } else {
      log.config("default configuration initialized from: "
          + def.getSourceName());
    }
  }
}
