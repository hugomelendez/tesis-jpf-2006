package gov.nasa.jpf.jvm;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;

import org.apache.bcel.util.*;
import org.apache.bcel.Constants;
import org.apache.bcel.classfile.AnnotationDefault;
import org.apache.bcel.classfile.AnnotationElementValue;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.Annotations;
import org.apache.bcel.classfile.ArrayElementValue;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.ClassElementValue;
import org.apache.bcel.classfile.ElementValue;
import org.apache.bcel.classfile.ElementValuePair;
import org.apache.bcel.classfile.EnumElementValue;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.SimpleElementValue;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.CHECKCAST;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.DLOAD;
import org.apache.bcel.generic.FLOAD;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.ILOAD;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.LLOAD;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.Type;

/**
 * Loads annotations of JavaClass pieces as instances of Annotation.  This
 * method of finding out about annotations in the analyzed code is more
 * convenient than the bcel.classfile API if the annotations of interest
 * are "staticly linked" to the analyzing code.  In other words, if the
 * analyzed code uses annotations that the analyzer knows about ahead of
 * time, this class enables access to those annotations in the most direct
 * way--just like reflection provides.
 * <br><br>
 * For example, if <code>AnnotationEntry foo</code> is known to be a
 * java.lang.annotation.Retention,
 * <blockquote><code>
 * <pre>Retention bar = (Retention) myAnnotationLoader.load(foo);
 *      if (bar.value == RetentionPolicy.RUNTIME) ...
 * </pre></code></blockquote>
 * 
 * @author Peter C. Dillinger (freelance time; used with permission)
 */
public class AnnotationLoader {
  protected static java.lang.ClassLoader thisClassLoader = AnnotationLoader.class.getClassLoader();
  
  // ==================== Public Interface =================== //
  
  public static final AnnotationLoader sysInstance = new AnnotationLoader(null,null);
  
  public static class IncompatabilityException extends Exception {
    private static final long serialVersionUID = 1L;

    public IncompatabilityException() { super(); }

    public IncompatabilityException(String message) { super(message); }
  }
  
  public AnnotationLoader(ClassPath path, boolean loadUnknown) {
    this(loadUnknown ? new ClassPathClassLoader(path, thisClassLoader) : null,
         SyntheticRepository.getInstance(path));
  }
  
  public AnnotationLoader(java.lang.ClassLoader loadedSrc, Repository compiledSrc) {
    this.loadedSrc = loadedSrc == null ?
        thisClassLoader : loadedSrc;
    this.compiledSrc = compiledSrc == null ?
        SyntheticRepository.getInstance() : compiledSrc;
    definer = new DefineClass(this.loadedSrc);
  }
  
  
  public Annotation[] loadCompatible(Annotations anns) {
    AnnotationEntry[] entries = anns.getAnnotationEntries();
    int len = entries.length;
    Annotation[] pre = new Annotation[len];
    int loaded = 0;
    for (int i = 0; i < len; i++) {
      try {
        pre[loaded] = load(entries[i]);
        loaded++;
      } catch (ClassNotFoundException e) {
        // ignore
      } catch (IncompatabilityException e) {
        // ignore
      }
    }
    Annotation[] ret = new Annotation[loaded];
    System.arraycopy(pre, 0, ret, 0, loaded);
    return ret;
  }
  

  public Annotation[] load(Annotations anns)
  throws ClassNotFoundException, IncompatabilityException {
    AnnotationEntry[] entries = anns.getAnnotationEntries();
    int len = entries.length;
    Annotation[] ret = new Annotation[len];
    for (int i = 0; i < len; i++) {
      ret[i] = load(entries[i]);
    }
    return ret;
  }
  

  public Annotation load(AnnotationEntry entry) 
  throws ClassNotFoundException, IncompatabilityException {
    String annName = entry.getAnnotationType().replace('/', '.');
    if (annName.startsWith("L") && annName.endsWith(";")) {
      annName = annName.substring(1, annName.length()-1);
    }
    Class<?> loadedAnnType = loadedSrc.loadClass(annName);
    Loader cached = cache.get(loadedAnnType);
    if (cached == null) {
      try {
        if (!Annotation.class.isAssignableFrom(loadedAnnType)) {
          throw new IncompatabilityException("" + annName +
            " does not represent a loaded annotation type.");
        }
        Class<? extends Annotation> loadedAnnType0 =
          loadedAnnType.asSubclass(Annotation.class);
        JavaClass compiledAnnType = compiledSrc.loadClass(annName);
        JavaClass compiledImpl = generateImpl(compiledAnnType);
        byte[] implBytes = compiledImpl.getBytes();
        Class<? extends Annotation> loadedImpl =
          definer.defineAnnotation(compiledImpl.getClassName(), implBytes);
        cached = new SuccessLoader(loadedAnnType0,loadedImpl,compiledAnnType);
      } catch(ClassNotFoundException e) {
        cached = new ClassNotFoundLoader(e.getMessage());
      } catch (NoClassDefFoundError e) {
        cached = new ClassNotFoundLoader(e.getMessage());
      } catch(IncompatabilityException e) {
        cached = new IncompatabilityLoader(e.getMessage());
      }
      cache.put(loadedAnnType, cached);
    }
    return cached.load(entry);
  }
  

  public static JavaClass generateImpl(JavaClass annClass) {
    String implName = "AnnotationImpl$$" + annClass.getClassName().replace('.','_');
    Method[]  stubs = annClass.getMethods();
    int       count = stubs.length;
    Field[]  fields = new Field[count];
    Type[]    types = new Type[count];
    Type[] augTypes = new Type[count+2];
    String[]  names = new String[count];
    String[] augNms = new String[count+2];
    int[] fieldIdxs = new int[count];
    Field  toString; int toStringIdx;
    Field  hashCode; int hashCodeIdx;

    ClassGen cg = new ClassGen(implName, "java.lang.Object", "", Constants.ACC_PUBLIC,
        new String[] { annClass.getClassName() } );
    cg.setMajor(Constants.MAJOR_1_5);
    cg.setMinor(Constants.MINOR_1_5);
    ConstantPoolGen cp = cg.getConstantPool();
    
    toString = new FieldGen(Constants.ACC_PROTECTED, Type.STRING, "toString", cp).getField();
    cg.addField(toString);
    toStringIdx = cp.addFieldref(implName, "toString", toString.getSignature());
    hashCode = new FieldGen(Constants.ACC_PROTECTED, Type.INT, "hashCode", cp).getField();
    cg.addField(hashCode);
    hashCodeIdx = cp.addFieldref(implName, "hashCode", hashCode.getSignature());
    
    augTypes[0] = Type.STRING;
    augNms  [0] = "toString";
    augTypes[1] = Type.INT;
    augNms  [1] = "hashCode";
    
    for (int i = 0; i < count; i++) {
      types[i] = stubs[i].getReturnType();
      augTypes[i+2] = types[i];
      names[i] = stubs[i].getName();
      augNms[i+2] = names[i];
      fields[i] = new FieldGen(Constants.ACC_PROTECTED, types[i], names[i], cp).getField();
      cg.addField(fields[i]);
      fieldIdxs[i] = cp.addFieldref(implName, fields[i].getName(), fields[i].getSignature());
    }

    InstructionList il = new InstructionList();
    il.append(InstructionConstants.THIS); // Push `this'
    il.append(new INVOKESPECIAL(cp.addMethodref(cg.getSuperclassName(), "<init>", "()V")));

    il.append(InstructionConstants.THIS);
    il.append(new ALOAD(1));
    il.append(new PUTFIELD(toStringIdx));

    il.append(InstructionConstants.THIS);
    il.append(new ILOAD(2));
    il.append(new PUTFIELD(hashCodeIdx));

    int offset = 3;
    for (int i = 0; i < count; i++) {
      il.append(InstructionConstants.THIS);
      switch (types[i].getType()) {
      case Constants.T_OBJECT:
      case Constants.T_ARRAY:
        il.append(new ALOAD(offset));
        offset++;
        break;
      case Constants.T_LONG:
        il.append(new LLOAD(offset));
        offset += 2;
        break;
      case Constants.T_DOUBLE:
        il.append(new DLOAD(offset));
        offset += 2;
        break;
      case Constants.T_FLOAT:
        il.append(new FLOAD(offset));
        offset++;
        break;
      default:
        il.append(new ILOAD(offset));
        offset++;
      }
      il.append(new PUTFIELD(fieldIdxs[i]));
    }
    il.append(InstructionConstants.RETURN);
    MethodGen mg = new MethodGen(Constants.ACC_PUBLIC, Type.VOID, augTypes, augNms, "<init>",
        cg.getClassName(), il, cp);
    mg.setMaxLocals();
    mg.setMaxStack();
    cg.addMethod(mg.getMethod());

    il = new InstructionList();
    il.append(new LDC(cp.addClass(annClass.getClassName())));
    il.append(InstructionConstants.ARETURN);
    mg = new MethodGen(Constants.ACC_PUBLIC, Type.CLASS, Type.NO_ARGS, null, "annotationType",
        cg.getClassName(), il, cp);
    mg.setMaxLocals();
    mg.setMaxStack();
    cg.addMethod(mg.getMethod());

    il = new InstructionList();
    il.append(InstructionConstants.THIS);
    il.append(new GETFIELD(toStringIdx));
    il.append(InstructionConstants.ALOAD_1);
    il.append(new CHECKCAST(cp.addClass(implName)));
    il.append(new GETFIELD(toStringIdx));
    il.append(new INVOKEVIRTUAL(cp.addMethodref("java.lang.String", "equals", "(Ljava/lang/Object;)Z")));
    il.append(InstructionConstants.IRETURN);
    mg = new MethodGen(Constants.ACC_PUBLIC,
                       Type.BOOLEAN,
                       new Type[] { Type.OBJECT },
                       new String[] { "o" },
                       "toString",
                       implName, il, cp);
    mg.setMaxLocals();
    mg.setMaxStack();
    cg.addMethod(mg.getMethod());
    
    il = new InstructionList();
    il.append(InstructionConstants.THIS);
    il.append(new GETFIELD(toStringIdx));
    il.append(InstructionConstants.ARETURN);
    mg = new MethodGen(Constants.ACC_PUBLIC, Type.STRING, Type.NO_ARGS, null, "toString",
        implName, il, cp);
    mg.setMaxLocals();
    mg.setMaxStack();
    cg.addMethod(mg.getMethod());
    
    il = new InstructionList();
    il.append(InstructionConstants.THIS);
    il.append(new GETFIELD(hashCodeIdx));
    il.append(InstructionConstants.IRETURN);
    mg = new MethodGen(Constants.ACC_PUBLIC, Type.INT, Type.NO_ARGS, null, "hashCode",
        implName, il, cp);
    mg.setMaxLocals();
    mg.setMaxStack();
    cg.addMethod(mg.getMethod());
        
    for (int i = 0; i < count; i++) {
      il = new InstructionList();
      il.append(InstructionConstants.THIS);
      il.append(new GETFIELD(fieldIdxs[i]));
      if (types[i] instanceof ArrayType) {
        ArrayType t = (ArrayType) types[i];
        il.append(new INVOKEVIRTUAL(cp.addMethodref(t.getSignature(), "clone", "()Ljava/lang/Object;")));
        il.append(new CHECKCAST(cp.addClass(t.getSignature())));
      }
      switch(types[i].getType()) {
      case Constants.T_OBJECT:
      case Constants.T_ARRAY:
        il.append(InstructionConstants.ARETURN);
        break;
      case Constants.T_LONG:
        il.append(InstructionConstants.LRETURN);
        break;
      case Constants.T_DOUBLE:
        il.append(InstructionConstants.DRETURN);
        break;
      case Constants.T_FLOAT:
        il.append(InstructionConstants.FRETURN);
        break;
      default:
        il.append(InstructionConstants.IRETURN);
      }
      mg = new MethodGen(Constants.ACC_PUBLIC, types[i], Type.NO_ARGS, null, names[i],
          implName, il, cp);
      mg.setMaxLocals();
      mg.setMaxStack();
      cg.addMethod(mg.getMethod());
    }
    
    return cg.getJavaClass();
  }
  
  
  
  // ================== Implementation Details ================ //
  
  protected final DefineClass definer;
  protected final java.lang.ClassLoader loadedSrc;
  protected final Repository compiledSrc;
  protected final HashMap<Class<?>,Loader> cache = new HashMap<Class<?>,Loader>();
  
  protected static class ClassPathClassLoader extends java.lang.ClassLoader {
    protected final ClassPath cp;
    
    public ClassPathClassLoader(ClassPath cp, java.lang.ClassLoader parent) {
      super(parent);
      this.cp = cp;
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
      try {
        byte[] bytes = cp.getBytes(name.replace('.', '/'), ".class");
        return defineClass(name, bytes, 0, bytes.length);
      } catch (IOException ioe) {
        throw new ClassNotFoundException("Could not load class " + name, ioe);
      }
    }

    protected URL findResource(String name) {
      return cp.getResource(name);
    }
  }
  
  protected static class DefineClass extends java.lang.ClassLoader {
    public DefineClass(java.lang.ClassLoader parent) {
      super(parent);
    }

    public Class<?> define(String name, byte[] bytes) {
      try {
        FileOutputStream out = new FileOutputStream(name + ".class");
        out.write(bytes);
        out.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return defineClass(name, bytes, 0, bytes.length);
    }

    public Class<? extends Annotation> defineAnnotation(String name, byte[] bytes) {
      return define(name, bytes).asSubclass(Annotation.class);
    }
  }
  
  protected static interface Loader {
    public Annotation load(AnnotationEntry entry)
    throws ClassNotFoundException, IncompatabilityException;
  }

  protected static class ClassNotFoundLoader implements Loader {
    final String message;
    public ClassNotFoundLoader(String msg) { message = msg; }
    public Annotation load(AnnotationEntry entry) throws ClassNotFoundException {
      throw new ClassNotFoundException(message);
    }
  }
  
  protected static class IncompatabilityLoader implements Loader {
    final String message;
    public IncompatabilityLoader(String msg) { message = msg; }
    public Annotation load(AnnotationEntry entry) throws IncompatabilityException {
      throw new IncompatabilityException(message);
    }
  }
  
  protected class SuccessLoader implements Loader {
    final Constructor<? extends Annotation> constructor;
    final Class[] paramTypes;
    final String[] names;
    final ElementValue[] defaults;
    final JavaClass compiledBase;
    
    public SuccessLoader(Class<? extends Annotation> loadedBase,
                         Class<? extends Annotation> loadedImpl,
                         JavaClass compiledBase)
    throws IncompatabilityException, ClassNotFoundException {
      this.compiledBase = compiledBase;
      Method[] stubs = compiledBase.getMethods();
      int count = stubs.length;
      paramTypes = new Class[count];
      names  = new String[count];
      defaults = new ElementValue[count];
      
      Class[] augParamTypes = new Class[count + 2];
      augParamTypes[0] = String.class; // toString
      augParamTypes[1] = Integer.TYPE; // hashCode
      
      for (int i = 0; i < count; i++) {
        names[i] = stubs[i].getName();
        
        Attribute[] attrs = stubs[i].getAttributes();
        for (int j = 0; j < attrs.length; j++) {
          if (attrs[j] instanceof AnnotationDefault) {
            defaults[i] = ((AnnotationDefault)attrs[j]).getDefaultValue();
            break;
          }
        }
        // if no default, defaults[i] = null;
        
        Type t = stubs[i].getReturnType();
        Class<?> c;
        switch (t.getType()) {
        case Constants.T_BOOLEAN: c = Boolean.TYPE; break;
        case Constants.T_CHAR:    c = Character.TYPE; break;
        case Constants.T_FLOAT:   c = Float.TYPE; break;
        case Constants.T_DOUBLE:  c = Double.TYPE; break;
        case Constants.T_BYTE:    c = Byte.TYPE; break;
        case Constants.T_SHORT:   c = Short.TYPE; break;
        case Constants.T_INT:     c = Integer.TYPE; break;
        case Constants.T_LONG:    c = Long.TYPE; break;
        case Constants.T_ARRAY:
        case Constants.T_OBJECT:
          String cname = ((ReferenceType)t).getSignature().replace('/', '.');
          if (cname.startsWith("L") && cname.endsWith(";")) {
            cname = cname.substring(1, cname.length()-1);
          }
          c = loadedSrc.loadClass(cname);
          break;
        default:
          throw new IncompatabilityException("Illegal return type for Annotation");
        }
        paramTypes[i] = c;
        augParamTypes[i+2] = c;
      }
      
      try {
        constructor = loadedImpl.getConstructor(augParamTypes);
      } catch (SecurityException e) {
        throw new IncompatabilityException(e.getMessage());
      } catch (NoSuchMethodException e) {
        // should not happen; problem creating class!
        throw new RuntimeException(e);
      }
    }
    
    public Annotation load(AnnotationEntry entry)
    throws IncompatabilityException, ClassNotFoundException {
      int paramCount = paramTypes.length;
      Object[] augParams = new Object[paramCount + 2];
      ElementValuePair[] pairs = entry.getElementValuePairs();
      int pairCount = pairs.length;
      
      StringBuffer toStringBuffer = new StringBuffer();
      toStringBuffer.append('@');
      toStringBuffer.append(compiledBase.getClassName());
      toStringBuffer.append('(');
      
      for (int i = 0; i < paramCount; i++) {
        String name = names[i];
        ElementValue ev = null;
        for (int j = 0; j < pairCount; j++) {
          if (name.equals(pairs[j].getNameString())) {
            ev = pairs[j].getValue();
            break;
          }
        }
        if (ev == null) { // use default from annotation class
          ev = defaults[i];
        }
        if (ev == null) {
          throw new IncompatabilityException("No value specified & no default available.");
        }
        Object v = getElementValueObject(ev, paramTypes[i]);
        augParams[i+2] = v;
        toStringBuffer.append(name);
        toStringBuffer.append('=');
        stringify(v,toStringBuffer);
        if (i + 1 < paramCount) toStringBuffer.append(';');
      }
      
      toStringBuffer.append(')');
      augParams[0] = toStringBuffer.toString();
      augParams[1] = new Integer(augParams[0].hashCode()); // good enough
      
      try {
        return constructor.newInstance(augParams);
      } catch (InstantiationException e) {
        // should not happen; problem creating class!
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        // should not happen; problem creating class!
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        // should not happen; problem creating class!
        throw new RuntimeException(e);
      }
    }
  }

  protected static void stringify(Object o, StringBuffer buf) {
    Class<?> c = o.getClass();
    if (c.isArray()) {
      int len = Array.getLength(o);
      buf.append('[');
      for (int i = 0; i < len; i++) {
        stringify(Array.get(o, i), buf);
        if (i + 1 < len) buf.append(',');
      }
      buf.append(']');
    } else if (c == String.class) {
      buf.append('"');
      buf.append((String)o);
      buf.append('"');
    } else {
      buf.append(o.toString());
    }
  }
  
  protected Object getElementValueObject(ElementValue v, Class<?> typ)
  throws ClassNotFoundException, IncompatabilityException {
    switch (v.getElementValueType()) {
    case ElementValue.STRING:
      //assert typ == String.class;
      return ((SimpleElementValue)v).getValueString();
    case ElementValue.PRIMITIVE_INT:
      //assert typ == Integer.TYPE;
      return new Integer(((SimpleElementValue)v).getValueInt());
    case ElementValue.PRIMITIVE_BYTE:
      //assert typ == Byte.TYPE;
      return Byte.valueOf(((SimpleElementValue)v).getValueByte());
    case ElementValue.PRIMITIVE_CHAR:
      //assert typ == Character.TYPE;
      return Character.valueOf(((SimpleElementValue)v).getValueChar());
    case ElementValue.PRIMITIVE_DOUBLE:
      //assert typ == Double.TYPE;
      return new Double(((SimpleElementValue)v).getValueDouble());
    case ElementValue.PRIMITIVE_FLOAT:
      //assert typ == Float.TYPE;
      return new Float(((SimpleElementValue)v).getValueFloat());
    case ElementValue.PRIMITIVE_LONG:
      //assert typ == Long.TYPE;
      return new Long(((SimpleElementValue)v).getValueLong());
    case ElementValue.PRIMITIVE_SHORT:
      //assert typ == Short.TYPE;
      return new Short(((SimpleElementValue)v).getValueShort());
    case ElementValue.PRIMITIVE_BOOLEAN:
      //assert typ == Boolean.TYPE;
      return Boolean.valueOf(((SimpleElementValue)v).getValueBoolean());
    case ElementValue.ENUM_CONSTANT:
      String fname = ((EnumElementValue) v).getEnumValueString();
      try {
        return typ.getField(fname).get(typ);
      } catch (IllegalArgumentException e1) {
        throw new IncompatabilityException("Could not get Enum field " + fname);
      } catch (SecurityException e1) {
        throw new IncompatabilityException("Could not get Enum field " + fname);
      } catch (IllegalAccessException e1) {
        throw new IncompatabilityException("Could not get Enum field " + fname);
      } catch (NoSuchFieldException e1) {
        throw new IncompatabilityException("No Enum field " + fname + " in " + typ.getName());
      }
    case ElementValue.ANNOTATION:
           // recursive!
      return load(((AnnotationElementValue)v).getAnnotationEntry());
    case ElementValue.ARRAY:
      Class<?> ctyp = typ.getComponentType();
      ArrayElementValue av = (ArrayElementValue) v;
      ElementValue[] ava = av.getElementValuesArray();
      int len = ava.length;
      Object a = Array.newInstance(ctyp, len);
      for (int i = 0; i < len; i++) {
        Array.set(a, i, getElementValueObject(ava[i], ctyp));
      }
      return a;
    case ElementValue.CLASS:
      String cname = ((ClassElementValue)v).getClassString().replace('/', '.');
      if (cname.startsWith("L") && cname.endsWith(";")) {
        cname = cname.substring(1, cname.length()-1);
      }
      return loadedSrc.loadClass(cname);
    default:
      //assert false : "Invalid/unknown ElementValue type.";
      try {
        // fallback
        return typ.newInstance();
      } catch (InstantiationException e) {
        throw new RuntimeException("Invalid/unknown ElementValue type.");
      } catch (IllegalAccessException e) {
        throw new RuntimeException("Invalid/unknown ElementValue type.");
      }
    }
  }
}