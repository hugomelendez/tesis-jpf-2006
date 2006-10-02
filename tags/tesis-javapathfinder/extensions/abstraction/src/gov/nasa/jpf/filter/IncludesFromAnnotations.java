package gov.nasa.jpf.filter;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.filter.AmmendableFilterConfiguration.FieldAmmendment;
import gov.nasa.jpf.filter.AmmendableFilterConfiguration.FrameAmmendment;
import gov.nasa.jpf.jvm.FieldInfo;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.LoadedAnnotations;
import gov.nasa.jpf.jvm.MethodInfo;
import gov.nasa.jpf.util.BitArray;

public class IncludesFromAnnotations
implements FieldAmmendment, FrameAmmendment {
  protected Config config;
  protected LoadedAnnotations annotations;
  
  public IncludesFromAnnotations(Config config) throws Config.Exception {
    this.config = config;
    annotations = JVM.getVM().getLoadedAnnotations();
    annotations.addInterestingAnnotationType(ForceMatch.class);
  }
  
  public boolean ammendFieldInclusion(FieldInfo fi, boolean sofar) {
    {
      ForceMatch ann = annotations.getAnnotationOfType(ForceMatch.class, fi);
      if (ann != null) {
        return POLICY_INCLUDE;
      }
    }
    return sofar;
  }

  public BitArray ammendLocalInclusion(MethodInfo mi, BitArray sofar) {
    // NOT YET IMPLEMENTED
    return sofar;
  }
}
