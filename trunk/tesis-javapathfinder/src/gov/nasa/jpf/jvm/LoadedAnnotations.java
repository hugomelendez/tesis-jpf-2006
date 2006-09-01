package gov.nasa.jpf.jvm;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.jvm.AnnotationLoader.IncompatabilityException;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.bcel.classfile.AnnotationEntry;

public class LoadedAnnotations {
  private AnnotationLoader loader;
  private final HashMap<String,HashMap<Object,Annotation>> mapMap =
    new HashMap<String,HashMap<Object,Annotation>>();
  private final ArrayList<HashMap<Object,Annotation>> mapList =
    new ArrayList<HashMap<Object,Annotation>>();
  
  public void init(Config config) throws Config.Exception {
    if (ClassInfo.modelClassPath == null) {
      throw new RuntimeException("ClassInfo must be initialized before LoadedAnnotations");
    }
    loader = new AnnotationLoader(ClassInfo.modelClassPath, false);
  }
  
  public void addInterestingAnnotationType(Class<? extends Annotation> typ) {
    if (mapMap.get(typ.getName()) == null) {
      HashMap<Object,Annotation> map = new HashMap<Object,Annotation>();
      mapList.add(map);
      String name = typ.getName();
      mapMap.put(name, map);
      name = name.replace('.', '/');
      mapMap.put(name, map);
      name = "L" + name + ";";
      mapMap.put(name, map);
    }
  }
  
  public <A extends Annotation> A getAnnotationOfType(Class<A> typ, Object annotated) {
    HashMap<Object,Annotation> map = mapMap.get(typ.getName()); 
    if (map == null) return null; // not an interesting annotation type
    // else
    Annotation ann = map.get(annotated);
    if (ann == null) return null;
    // else
    return typ.cast(ann);
  }
  
  
  
  void loadAnnotations(Object annotated, AnnotationEntry[] entries) {
    if (entries == null) return;
    for (AnnotationEntry entry : entries) {
      HashMap<Object,Annotation> map = mapMap.get(entry.getAnnotationType()); 
      if (map != null) {
        Annotation loaded;
        try {
          loaded = loader.load(entry);
        } catch (ClassNotFoundException e) {
          // TODO Auto-generated catch block
          System.err.println("Warning loading annotation: " + 
              e.getLocalizedMessage());
          loaded = null;
        } catch (IncompatabilityException e) {
          // TODO Auto-generated catch block
          System.err.println("Warning loading annotation: " + 
              e.getLocalizedMessage());
          loaded = null;
        }
        if (loaded != null) {
          map.put(annotated, loaded);
        }
      }
    }
  }
}
