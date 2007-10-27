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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;


/**
 * utility class to access arbitrary source files by line number
 *
 * <2do> pcm - should recursively look for sources underneath srcRoot dirs
 * <2do> pcm - should be more sophisticated, e.g. by using LRU caching
 */
public class Source {
          static ArrayList<String>        srcRoots = new ArrayList<String>();
  private static Hashtable<String,Source>  sources = new Hashtable<String,Source>();

  static {
    srcRoots.add("src");
    srcRoots.add("test");
    srcRoots.add("examples");
  }

  protected List<String>  program;
  protected String        name;

  protected Source (String fname) {
    name = fname;
    program = loadSource(fname);
  }

  public static void addSourceRoot (String pathName) {
    srcRoots.add(pathName);
  }

  public boolean isLineMissing (int line) {
    return (program == null) || (line <= 0 || line > program.size());
  }

  public static Source getSource (String fname) {
    Source s = sources.get(fname);

    if (s == null) {
      sources.put(fname, s = new Source(fname));
    }

    return s;
  }

  public String getLine (int line) {
    if (program == null) {
      return "";
    }

    if ((line <= 0) || (line > program.size())) {
      return "";
    }

    return program.get(line - 1);
  }

  private static boolean exists (String filename) {
    return (new File(filename)).exists();
  }

  private List<String> loadFile (String fname) {
    List<String> result = null;
    try {
      BufferedReader in = new BufferedReader(new FileReader(fname));
      try {
        ArrayList<String> l = new ArrayList<String>();
        String line;
        for (;;) {
          line = in.readLine();
          if (line == null) break;
          l.add(line);
        }
        result = l;
      } catch (IOException e) {
        // result is null
      }
      in.close();
    } catch (IOException e) {
      // result is null
    }
    return result;
  }

  private List<String> loadSource (String fname) {
    ListIterator<String> it = srcRoots.listIterator();

    while (it.hasNext()) {
      String pn = it.next() + File.separatorChar + fname;

      if (exists(pn)) {
        return loadFile(pn);
      }
    }

    if (exists(fname)) {
      return loadFile(fname);
    }

    return null;
  }
}
