package gov.nasa.jpf.filter;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.filter.AmmendableFilterConfiguration.FieldAmmendment;
import gov.nasa.jpf.filter.AmmendableFilterConfiguration.FrameAmmendment;
import gov.nasa.jpf.jvm.FieldInfo;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.LoadedAnnotations;
import gov.nasa.jpf.jvm.MethodInfo;
import gov.nasa.jpf.util.BitArray;

public class IgnoresFromAnnotations
implements FieldAmmendment, FrameAmmendment {
  protected Config config;
  protected LoadedAnnotations annotations;
  
  public IgnoresFromAnnotations(Config config) throws Config.Exception {
    this.config = config;
    annotations = JVM.getVM().getLoadedAnnotations();
    annotations.addInterestingAnnotationType(NoMatch.class);
    annotations.addInterestingAnnotationType(NoMatchIf.class);
    annotations.addInterestingAnnotationType(MatchOnlyIf.class);
  }
  
  public boolean ammendFieldInclusion(FieldInfo fi, boolean sofar) {
    {
      NoMatch ann = annotations.getAnnotationOfType(NoMatch.class, fi);
      if (ann != null) {
        return POLICY_IGNORE;
      }
    }
    {
      NoMatchIf ann = annotations.getAnnotationOfType(NoMatchIf.class, fi);
      if (ann != null && config.getBoolean(ann.value())) {
        return POLICY_IGNORE;
      }
    }
    {
      MatchOnlyIf ann = annotations.getAnnotationOfType(MatchOnlyIf.class, fi);
      if (ann != null && config.getBoolean(ann.value())) {
        return POLICY_IGNORE;
      }
    }
    return sofar;
  }

  public BitArray ammendLocalInclusion(MethodInfo mi, BitArray sofar) {
    // NOT YET IMPLEMENTED
    return sofar;
  }
}
