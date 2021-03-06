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
package gov.nasa.jpf.tools;

import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.bytecode.Instruction;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;

/**
 * simple tools to gather statistics about instructions executed by JPF.
 * InsnCounter is mostly a VMListener that observes 'instructionExecuted'
 */
public class InsnCounter extends ListenerAdapter {

  String[] opCodes = new String[256];
  int[] counts = new int[256];
  int   total;
  
  //----------------------------------------- SearchKistener interface
  public void searchFinished(Search search) {
    reportStatistics();
  }
    
  //----------------------------------------------------- VMListener interface
  public void instructionExecuted(JVM jvm) {
    Instruction insn = jvm.getLastInstruction();
    int bc = insn.getByteCode();
    
    if (opCodes[bc] == null) {
      opCodes[bc] = insn.getMnemonic();
    }
    counts[bc]++;
    total++;
  }

  
  //----------------------------------------------------- internal stuff
  void reportStatistics () {
    int[] sorted = getSortedCounts();
    int i;
    
    for (i=0; i<sorted.length; i++) {
      int idx = sorted[i];
      String opc = opCodes[idx];
            
      if (counts[idx] > 0) {
        System.out.print( i);
        System.out.print( "  ");
        System.out.print( opc);
        System.out.print( " : ");
        System.out.println( counts[idx]);
      } else {
        break;
      }
    }
  }
  
  int[] getSortedCounts () {
    int[] sorted = new int[256];
    int last = -1;
    int i, j;
    
    for (i=0; i<256; i++) {
      int c = counts[i];
      if (c > 0) {
        for (j=0; j<last; j++) {
          if (counts[sorted[j]] < c) {
            System.arraycopy(sorted, j, sorted, j+1, (last-j));
            break;
          }
        }
        sorted[j] = i;
        last++;
      }
    }
    
    return sorted;
  }
  
  void filterArgs (String[] args) {
    // we don't have any yet
  }
  
  public static void main (String[] args) {
    InsnCounter listener = new InsnCounter();
    listener.filterArgs(args);
    
    Config conf = JPF.createConfig(args);
    // do own settings here
    
    JPF jpf = new JPF(conf);
    jpf.addSearchListener(listener);
    jpf.addVMListener(listener);

    jpf.run();
  }
}

