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
package gov.nasa.jpf.util;

/**
 * a source reference abstraction wrapping file and line information
 */
public class SourceRef {
  public String fileName;
  public int    line;

  public SourceRef () {
    fileName = null;
    line = -1;
  }

  public SourceRef (String f, int l) {
    fileName = f;
    line = l;
  }

  public String getLineString () {
    Source source = Source.getSource(fileName);

    return source.getLine(line);
  }

  public boolean equals (Object o) {
    if (o == null) {
      return false;
    }

    if (!(o instanceof SourceRef)) {
      return false;
    }

    SourceRef that = (SourceRef) o;

    if (this.fileName == null) {
      return false;
    }

    if (this.line == -1) {
      return false;
    }

    if (!this.fileName.equals(that.fileName)) {
      return false;
    }

    if (this.line != that.line) {
      return false;
    }

    return true;
  }

  public boolean equals (String f, int l) {
    if (fileName == null) {
      return false;
    }

    if (line == -1) {
      return false;
    }

    if (!fileName.equals(f)) {
      return false;
    }

    if (line != l) {
      return false;
    }

    return true;
  }

  public int hashCode() {
    assert false : "hashCode not designed";
    return 42; // any arbitrary constant will do
    // thanks, FindBugs!
  }

  public String getFileName () {
    return fileName;
  }

  public void set (SourceRef sr) {
    fileName = sr.fileName;
    line = sr.line;
  }

  public String toString () {
    return (fileName + ':' + line);
  }
}
