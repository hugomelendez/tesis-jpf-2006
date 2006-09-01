package tesis.pruebaVerify;

import gov.nasa.jpf.jvm.TestJPF;
import gov.nasa.jpf.jvm.Verify;

import org.junit.Test;
import org.junit.runner.JUnitCore;

public class TestVerify2 extends TestJPF {
  static final String TEST_CLASS = "tesis.pruebaVerify.PruebaVerify1";


  public static void main (String[] args) {
    JUnitCore.main("tesis.pruebaVerify.TestVerify2");
  }


  /**************************** tests **********************************/
  @Test
  public void test1 () {
    String[] args = { TEST_CLASS};
    
	boolean cond = Verify.getBoolean();

    runJPFnoException(args);

    }
}
