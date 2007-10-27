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

import java.io.PrintStream;
import java.io.PrintWriter;


/**
 * common root for all exceptions thrown by JPF
 */
@SuppressWarnings("serial")
public class JPFException extends RuntimeException {
  Throwable throwable;

  public JPFException (String s) {
    super(s);
    throwable = null;
  }

  public JPFException (Throwable s) {
    super(s.getClass() + ": " + s.getMessage());
    throwable = s;
  }

  public void printStackTrace () {
    if (throwable != null) {
      throwable.printStackTrace();
    }

    super.printStackTrace();
  }

  public void printStackTrace (PrintStream out) {
    if (throwable != null) {
      throwable.printStackTrace(out);
    }

    super.printStackTrace(out);
  }

  public void printStackTrace (PrintWriter out) {
    if (throwable != null) {
      throwable.printStackTrace(out);
    }

    super.printStackTrace(out);
  }
}
