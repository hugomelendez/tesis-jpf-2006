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

import gov.nasa.jpf.Property;
import gov.nasa.jpf.jvm.Path;
import gov.nasa.jpf.util.Printable;

import java.io.PrintWriter;


/**
 * class used to store property violations (property and path)
 */
public class Error implements Printable {

  Property       property;
  private String propertyMessage;
  private Path   path;

  boolean printPath;
  
  public Error (Property prop, Path p, boolean printPath) {
    property = prop;
    propertyMessage = prop.getErrorMessage();
    path = p;
    
    this.printPath = printPath;
  }

  public String getMessage () {
    return propertyMessage;
  }

  public Path getPath () {
    return path;
  }

  public Property getProperty () {
    return property;
  }

  public void printOn (PrintWriter ps) {
    
    ps.print("----------------------------------- error: ");
    ps.println(propertyMessage);
    property.printOn( ps);
    ps.println();
    
    if (printPath) {
      ps.print("----------------------------------- path to error (length: ");
      ps.print(path.length());
      ps.println(')');
      path.printOn(ps);
    }
    
    if (path.hasOutput()) {    
      ps.println("------------------------------------ path output");
      path.printOutputOn( ps);
    }
  }
}
