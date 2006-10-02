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

import gov.nasa.jpf.JPFException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StreamTokenizer;

/**
 * a little helper class that is used to replay previously stored traces
 * (which are little more than just a list of ChoiceGenerator classnames and
 * choice indexes stored in a previous run)
 */
public class ChoicePoint {
  String cgClassName;
  int choice;
  ChoicePoint next, prev;
  
  ChoicePoint (String cgClassName, int choice, ChoicePoint prev) {
    this.cgClassName = cgClassName;
    this.choice = choice;
    
    if (prev != null) {
      this.prev = prev;
      prev.next = this;
    }
  }

  public String getCgClassName() {
    return cgClassName;
  }
  
  public int getChoice() {
    return choice;
  }
  
  public ChoicePoint getNext() {
    return next;
  }
    
  public ChoicePoint getPrevious() {
    return prev;
  }
  
  public static void storeTrace (String fileName, String mainClass, String[] args, 
                                 ChoiceGenerator[] trace) {
    int i;
    if (fileName != null) {
      try {
        FileWriter fw = new FileWriter(fileName);
        PrintWriter pw = new PrintWriter(fw);
        
        // store the main app class and args here, so that we can do at least some checking
        pw.print( "application: ");
        pw.println( mainClass);
        
        for (i=0; i<args.length; i++) {
          pw.print(' ');
          pw.print( args[i]);
        }
        pw.println();
        
        for (i=0; i<trace.length; i++) {
          pw.print('[');
          pw.print(i);
          pw.print("] ");
          pw.print(trace[i].getClass().getName());
          pw.print(" ");
          pw.println( trace[i].getProcessedNumberOfChoices());
        }
        
        pw.close();
        fw.close();
      } catch (Throwable t) {
        throw new JPFException(t);
      }
    }
  }

  
  static final int TT_EOF = StreamTokenizer.TT_EOF;
  static final int TT_EOL = StreamTokenizer.TT_EOL;
  static final int TT_NUMBER = StreamTokenizer.TT_NUMBER;
  static final int TT_WORD = StreamTokenizer.TT_WORD;
  
  public static ChoicePoint readTrace (String fileName, String mainClass) {
    ChoicePoint firstCp = null, cp = null;
    int t;
    
    if (fileName == null) {
      return null;
    }
    
    File f = new File(fileName);
    if (f.exists()) {
      try {
        FileReader fr = new FileReader(f);
        StreamTokenizer scanner = new StreamTokenizer(fr);
        scanner.whitespaceChars(',',',');
        scanner.whitespaceChars('[','[');
        scanner.whitespaceChars(']',']');
        scanner.whitespaceChars('{','{');
        scanner.whitespaceChars('}','}');
        scanner.whitespaceChars('(','(');
        scanner.whitespaceChars(')',')');
        scanner.whitespaceChars(':',':');
        scanner.whitespaceChars('=','=');
        scanner.ordinaryChar('\n');        
    
        if ((scanner.nextToken() == TT_WORD) && (scanner.sval.equals("application"))) {
          scanner.nextToken();
          
          // a little sanity check
          if (!scanner.sval.equals(mainClass)) {
            throw new JPFException("inconsistent main class in trace: " + fileName);
          }
          
          do {
            t = scanner.nextToken();
          } while (t != TT_EOL && t != TT_EOF);
        }

        do {
          t = scanner.nextToken();
        } while (t == TT_EOL);
        
        while (t != TT_EOF) {
          String cpClass = null;
          int choiceIndex = -1;
          
          // skip level number
          if (t == TT_NUMBER) {
            t = scanner.nextToken();
          
            if (t == TT_WORD) {
              // get the ChoiceGenerator classname
              cpClass = scanner.sval;
              
              // get the choice index
              t = scanner.nextToken();
              if (t == TT_WORD) {
                for (int i = 0; t != TT_EOL; t = scanner.nextToken(), i++) {
                  if ((scanner.ttype == ChoiceGenerator.MARKER)) {
                    choiceIndex = i;
                    break;
                  }
                }
              } else if (t == TT_NUMBER) {
                choiceIndex = (int) scanner.nval -1;
              }
              
              if (choiceIndex >= 0) {
                
                cp = new ChoicePoint(cpClass, choiceIndex, cp);
                if (firstCp == null) {
                  firstCp = cp;
                }
                
                // skip to end of line
                do {
                  t = scanner.nextToken();
                } while (t != TT_EOL && t != TT_EOF);
                t = scanner.nextToken();
                continue;
              }
            }
          }

          throw new JPFException("malformed choice trace: " + fileName);
        }
        
      } catch (Throwable x) {
        throw new JPFException(x);
      }
    }
    
    return firstCp;
  }
}
