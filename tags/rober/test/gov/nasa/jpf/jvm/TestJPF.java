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
import gov.nasa.jpf.Error;
import gov.nasa.jpf.ErrorList;
import gov.nasa.jpf.JPF;

import java.io.PrintStream;


import org.junit.*;
//import junit.framework.TestCase;


/**
 * base class for JPF unit tests. TestJPF mostly includes JPF invocations
 * that check for occurrence or absence of certain execution results 
 */
public abstract class TestJPF extends Assert /*extends TestCase*/ {
  static PrintStream out = System.out;

  
  protected void runJPFDeadlock (String[] args) {
    report(args);
    
    try {
      Config conf = JPF.createConfig(args);
      
      if (conf.getTargetArg() != null) {
        JPF jpf = new JPF(conf);
        jpf.run();
        
        ErrorList errors = jpf.getSearchErrors();
        
        if (errors != null) {
          for (int i = 0; i < errors.size(); i++) {
            Error e = errors.get(i);
            
            if ("Deadlock".equals(e.getMessage())) {
              System.out.println("found Deadlock");
              
              return; // success, we got the sucker
            }
          }
        }          
      }
    } catch (Throwable x) {
      x.printStackTrace();
      fail("JPF internal exception executing: " + args[0] + "." + args[1] + " : " + x);
    }
    
    fail("JPF failed to detect deadlock");
  }

  /**
   * run JPF and case the test to fail if NO AssertionsError is encountered
   */
  protected void runJPFassertionError (String[] args) {
    runJPFException(args, "java.lang.AssertionError");
  }

  /**
   * It runs JPF and cause the test to fail if an AssertionError is encountered
   */
  protected void runJPFnoAssertionError (String[] args) {
    runJPFnoException(args);
  }

  protected void runJPFnoException (String[] args) {
    ExceptionInfo xi = null;
    
    report(args);

    try {
      Config conf = JPF.createConfig(args);
      
      if (conf.getTargetArg() != null) {
        JPF jpf = new JPF(conf);
        jpf.run();
        
        ErrorList errors = jpf.getSearchErrors();      
        if ((errors != null) && (errors.size() > 0)) {
          fail("JPF found unexpected errors: " + (errors.get(0)).getMessage());
        }

        xi = JVM.getVM().getPendingException();
      }
    } catch (Throwable t) {
      // we get as much as one little hickup and we declare it failed
      fail("JPF internal exception executing: " + args[0] + " " + args[1] +
           " : " + t);
    }

    if (xi != null) {
      fail("JPF caught exception executing: " + args[0] + " " + args[1] +
           " : " + xi.getExceptionClassname());
    }
  }

  protected void runJPFException (String[] args, String xClassName) {
    ExceptionInfo xi = null;
    
    report(args);

    try {
      // run JPF on our target test func
      gov.nasa.jpf.JPF.main(args);

      xi = JVM.getVM().getPendingException();
      if (xi == null){
        String test = (args.length > 1) ? (args[0] + '.' + args[1]) : args[0];
        fail("JPF failed to catch exception executing: " + test +
            " , expected: " + xClassName);
      } else if (!xClassName.equals(xi.getExceptionClassname())) {        
        fail("JPF caught wrong exception: " + xi.getExceptionClassname() +
                   ", expected: " + xClassName);
      }
    } catch (Throwable x) {
      String test = (args.length > 1) ? (args[0] + '.' + args[1]) : args[0];
      fail("JPF internal exception executing: " + test + " : " + x);
    }
  }
  
  void report (String[] args) {
    out.print("  running jpf with args:");

    for (int i = 0; i < args.length; i++) {
      out.print(' ');
      out.print(args[i]);
    }

    out.println();
  }
}
