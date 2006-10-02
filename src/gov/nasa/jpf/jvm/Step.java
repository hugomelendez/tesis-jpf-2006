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

import gov.nasa.jpf.*;
import gov.nasa.jpf.jvm.bytecode.Instruction;
import gov.nasa.jpf.util.Left;

import java.io.PrintWriter;
import java.io.StringWriter;
import gov.nasa.jpf.util.SourceRef;
import gov.nasa.jpf.util.Source;


/**
 * this corresponds to an executed instruction. Note that we can have a
 * potentially huge number of Steps, hence we want to save objects here
 * (e.g. Collection overhead)
 */
public class Step extends SourceRef {
    
  Instruction   insn;
  String        comment;
  Step          next;

  static int nMissing; // number of missing lines since last printed source line

  // report options - statics are suboptimal, but we don't have a good path
  // to the VM here
  static boolean showBytecode;
  static boolean showMissingLines;
  
  public String description; // on demand (cache)
  
  public Step (String f, int l) {
    super(f, l);
  }

  public Step (String file, int line, Instruction insn) {
    this(file, line);
    this.insn = insn;
  }

  static boolean init (Config config) {
    showBytecode = config.getBoolean("vm.report.show_bytecode");
    showMissingLines = config.getBoolean("vm.report.show_missing_lines");
    return true;
  }
  
  public Step getNext() {
    return next;
  }
  
  public SourceRef getSourceRef () {
    return this;
  }

  public String getDescription () {
    if (description != null) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      
      printStepOn(pw, null, null);
      description = sw.toString();
    }
    
    return description;
  }
  
  public void setComment (String s) {
    comment = s;
  }

  public String getComment () {
    return comment;
  }

  void printMissingPlaceholderOn (PrintWriter pw, String prefix) {
    pw.print( (prefix != null) ? prefix : "  ");
    for (; nMissing > 0; nMissing--) {
      pw.print('.');
    }
    pw.println();    
  }
  
  public void printStepOn (PrintWriter pw, SourceRef lastPrinted, String prefix) {
    Source source = Source.getSource(fileName);
    boolean isLineMissing = source.isLineMissing(line);

    if (!this.equals(lastPrinted)) {
      if (isLineMissing) {
        if (showMissingLines) {
          pw.print( (prefix != null) ? prefix : "  ");
          pw.print("[no source for: ");
          pw.print(fileName);
          pw.println(']');          
        } else {
          nMissing++;
          if (next == null) {
            printMissingPlaceholderOn(pw,prefix);
          }
        }
      } else {
        if (nMissing > 0) {
          printMissingPlaceholderOn(pw,prefix);
        }
        
        pw.print( (prefix != null) ? prefix : "  ");
        pw.print(Left.format(fileName + ":" + line, 20));
        pw.print(' ');
        pw.println(source.getLine(line));        
      }
      
      lastPrinted.set(this);
    }

    if (showBytecode) {
      if (insn != null) {
        MethodInfo mi = insn.getMethod();
        pw.print('\t');
        pw.print(Left.format(mi.getClassName() + "." +
                               mi.getUniqueName() + "." + insn.getPosition() + ":", 40));
        pw.println(insn);
      }
    }
    
    if (comment != null) {
      pw.print("  // ");
      pw.println(comment);
    }    
  }

  public void toXML (PrintWriter pw) {
    pw.print("\t<Instruction File=\"");
    pw.print( fileName);
    pw.print("\" Line=\"");
    pw.print(line);
    pw.println("\"/>");

    if (comment != null) {
      pw.print("\t<Comment>");
      pw.print( comment);
      pw.println("</Comment>");
    }
  }
}
