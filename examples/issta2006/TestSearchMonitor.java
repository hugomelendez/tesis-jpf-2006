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
package issta2006;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import issta2006.Counter.TestCount;


import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.jvm.bytecode.INVOKESTATIC;
import gov.nasa.jpf.jvm.bytecode.Instruction;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.ListenerAdapter;

import gov.nasa.jpf.search.heuristic.*;

/**
 * SearchListener class to collect and report statistical
 * data during JPF execution.
 * This replaces the old JPF Statistics mechanism
 */
public class TestSearchMonitor extends ListenerAdapter {

  boolean logger; // true if this is just listening & reporting on an externally running JPF
  boolean autoclose = false;
  String host = "localhost";
  int port = 5556;
  
  Socket sock;
  PrintWriter out;
  
  int reportNumber;
  
  int  interval = 1000;  // min duration in ms between reports 
  long time;
  long lastTime;
  long startTime;
  long startFreeMemory;
  
  int searchLevel;
  
  int newStates;
  int endStates;
  int backtracks;
  int visitedStates;
  int processedStates;
  int restoredStates;
  
  int steps;

  long maxMemory;
  long totalMemory;
  long freeMemory;
    
  boolean isHeuristic = false;
  int queueSize = 0;
  
  /*
   * SearchListener interface
   */  
  
  public void stateAdvanced(Search search) {
    
    steps += search.getTransition().getStepCount();
   
    if (isHeuristic)
    	queueSize = ((HeuristicSearch)(search)).getQueueSize();
    
    if (search.isNewState()) {
      searchLevel = search.getDepth();
      newStates++;
      
      if (search.isEndState()) {
        endStates++;
      }
    } else {
      visitedStates++;
    }
    
    checkReport();
  }

  public void stateProcessed(Search search) {
    processedStates++;
    checkReport();
  }

  public void stateBacktracked(Search search) {
    searchLevel = search.getDepth();
    backtracks++;
    checkReport();
  }

  public void stateRestored(Search search) {
    searchLevel = search.getDepth();
    restoredStates++;
    checkReport();
  }

  public void propertyViolated(Search search) {
  }

  public void searchStarted(Search search) {
    connect();
    
    if (search instanceof HeuristicSearch)
    	isHeuristic = true;
    
    startTime = lastTime = System.currentTimeMillis();
    
    Runtime rt = Runtime.getRuntime();
    startFreeMemory = rt.freeMemory();
    totalMemory = rt.totalMemory();
    maxMemory = rt.maxMemory();
    reportNumber = 1;
  }

  public void searchConstraintHit(Search search) {
  }

  public void searchFinished(Search search) {
    report("------------------------------------ statistics");
    
    if (sock != null) {
      try {
        out.close();
        sock.close();
      } catch (IOException iox) {
      }
    }
    
    reportOldStyle();
    
  }

  void checkReport () {
    time = System.currentTimeMillis();
    
    Runtime rt = Runtime.getRuntime();
    long m = rt.totalMemory();
    if (m > totalMemory) {
      totalMemory = m;
    }
    
    if ((time - lastTime) >= interval) {
      freeMemory = rt.freeMemory();
      
      report("# " + reportNumber++);
      lastTime = time;
    }
  }
  
  void reportRuntime () {
    long td = time - startTime;
    
    int h = (int) (td / 3600000);
    int m = (int) (td / 60000) % 60;
    int s = (int) (td / 1000) % 60;
    
    out.print("  abs time:          ");
    if (h < 10) out.print('0');
    out.print( h);
    out.print(':');
    if (m < 10) out.print('0');
    out.print( m);
    out.print(':');
    if (s < 10) out.print('0');
    out.print( s);
    
    out.print( "  (");
    out.print(td);
    out.println(" ms)");
  }
  
  int length = 0;
  int params = 0;
  String technique = "";
  String symbolic = "";
  
	public void instructionExecuted(JVM jvm) {
		Instruction insn = jvm.getLastInstruction();
		ThreadInfo ti = jvm.getLastThreadInfo();

		if ((insn instanceof INVOKESTATIC) &&
		        (((INVOKESTATIC)insn).mname.equals("TestConfig(IIII)V"))) {
					length = ti.getIntLocal("length");
					params = ti.getIntLocal("params");
					int t = ti.getIntLocal("technique");
					int s = ti.getIntLocal("symbolic");
					if (s == 1)
						symbolic = "symbolic";
					if (t == 0) {
						technique = "Random";
					}
					else if (t == 1)
						technique = "Model Checking";
					else if (t > 1) {
						if ( t == 6)
						  technique = "Abstract Matching Shape (" + t + ")";
						if (t == 7)
						  technique = "Abstract Matching Complete (" + t + ")";	
					}
		}

	}
  
  
  void reportOldStyle() {
  	System.err.println("---------------------");
  	System.err.println("" + technique + " " + symbolic);
  	System.err.println("Sequence = " + length + " Parameters = " + params);
  	System.err.println("States visited: " + newStates);
  	System.err.println("Memory used: " + (totalMemory / (1024*1024)));
  	System.err.println("Execution time: " + (time - startTime)/1000);
  	System.err.println("TestsCovered: " + TestCount.number);
  	System.err.println("---------------------");
  	
  }
  
  void report (String header) {

    out.println(header);

    reportRuntime();
    
    out.print("  rel. time [ms]:    ");
    out.println(time - lastTime);
    
    out.println();
    out.print("  search depth:      ");
    out.println(searchLevel);
    
    out.print("  new states:        ");
    out.println(newStates);
    
    out.print("  visited states:    ");
    out.println(visitedStates);
        
    out.print("  end states:        ");
    out.println(endStates);

    out.print("  backtracks:        ");
    out.println(backtracks);

    out.print("  processed states:  ");
    out.print( processedStates);
    out.print(" (");
    // a little ad-hoc rounding
    double d = (double) backtracks / (double)processedStates;
    int n = (int) d;
    int m = (int) ((d - /*(double)*/ n) * 10.0);
    out.print( n);
    out.print('.');
    out.print(m);
    out.println( " bt/proc state)");
    
    out.print("  restored states:   ");
    out.println(restoredStates);

    if (isHeuristic) {
    	out.print("  queue size:        ");
    	out.println(queueSize);
    }
    
    out.println();
    out.print("  total memory [kB]: ");
    out.print(totalMemory / 1024);
    out.print(" (max: ");
    out.print(maxMemory / 1024);
    out.println(")");
    
    out.print("  free memory [kB]:  ");
    out.println(freeMemory / 1024);
    
    out.println();
  }
  
  void filterArgs (String[] args) {
    for (int i=0; i<args.length; i++) {
      if (args[i].equals("-logger")) {
        args[i] = null;
        logger = true;
      } else if (args[i].equals("-autoclose")) {
        args[i] = null;
        autoclose = true;
      }
    }
  }
  
  static void printUsage () {
    System.out.println("SearchMonitor - a JPF listener tool to monitor JPF searches");
    System.out.println("usage: java gov.nasa.jpf.tools.SearchMonitor <jpf-options> <monitor-options> <class>");
    System.out.println("<monitor-options>:");
    System.out.println("       -logger : run as a logger (don't run JPF)");
    System.out.println("       -autoclose : terminate logger after disconnect");
  }
  
  
  void connect () {
      
    try {
      sock = new Socket(host, port);
      out = new PrintWriter(sock.getOutputStream(), true);
    } catch (UnknownHostException uhx) {
      System.err.println("unknown log host: " + host + ", using System.out");
    } catch (ConnectException cex) {
      System.err.println("no log host detected, using System.out");
    } catch (IOException iox) {
      System.err.println(iox);
    }
    
    if (out == null) {
      out = new PrintWriter(System.out, true);
    }
  }
  
  void log () {
    try {
      ServerSocket ss = new ServerSocket(port);
      
      try {          
        do {
          System.out.println("logger listening on port: " + port);

          Socket cs = ss.accept();
          BufferedReader in = new BufferedReader( new InputStreamReader(cs.getInputStream()));
          String msg; 
          
          System.out.println("logger connected");
          System.out.println("------------------------------");
          try {
            
            while ((msg = in.readLine()) != null) {
              System.out.println(msg);
            }
            
            System.out.println("------------------------------");            
            System.out.println("logger disconnected");
          } catch (IOException iox) {
            System.err.println(iox);
          } finally {
            try { 
              in.close();
              cs.close();
            } catch (IOException iox) {
            }
          }
          
        } while (!autoclose);
        
      } catch (IOException iox) {
        System.err.println("error: accept failed on port: " + port);
      } finally {
        ss.close();          
      }
    } catch (IOException iox) {
      System.err.println("error: cannot listen on port: " + port);
    }
  }
  
  public void run (Config conf) {
    if (logger) {
      log();
      
    } else {
      JPF jpf = new JPF(conf);
      jpf.addSearchListener(this);
    
      jpf.run();
    }
  }
  
  public static void main (String[] args) {
    if (args.length == 0) {
      printUsage();
      return;
    }
    
    TestSearchMonitor listener = new TestSearchMonitor();
    listener.filterArgs(args);
    
    Config conf = JPF.createConfig(args);
    listener.initFromConfig(conf);
    
    listener.run(conf);    
  }

  void initFromConfig (Config config) {
    host = config.getString("monitor.log_hostname", host);
    port = config.getInt("monitor.log_port", port);
    interval = config.getInt("monitor.interval", port);
  }
  
  public TestSearchMonitor () {
    // nothing to do here
  }
  
  /**
   * that is our JPF instantiation ctor
   */
  public TestSearchMonitor (Config config) {
    initFromConfig(config);
  }
}
