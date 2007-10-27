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

import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.VMListener;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.search.SearchListener;
import gov.nasa.jpf.util.Debug;
import gov.nasa.jpf.util.LogManager;
import gov.nasa.jpf.util.ObjArray;

import java.io.PrintWriter;
import java.util.logging.Logger;


/**
 * main class of the JPF verification framework. This reads the configuration,
 * instantiates the Search and VM objects, and kicks off the Search
 */
public class JPF implements Runnable {

  /** JPF version, we read this inlater from default.properties */
  static String            VERSION    = "3.?";

  static Logger            logger     = null; // initially
  
  public Config            config;

  /**
   * Reference to the virtual machine used by the model checker.
   */
  public JVM                vm;

  /**
   * The search engine used to visit the state space.
   */
  public Search            search;



  private static Logger initLogging(Config conf) {
    LogManager.init(conf);
    return getLogger("gov.nasa.jpf");
  }
  
  /**
   * use this one to get a Logger that is initialized via our Config mechanism. Note that
   * our own Loggers do NOT pass
   */
  public static Logger getLogger (String name) {
    return LogManager.getLogger( name);
  }
  
  /**
   * create new JPF object. Note this is always guaranteed to return, but the
   * Search and/or VM object instantiation might have failed (i.e. the JPF
   * object might not be really usable). If you directly use getSearch() / getVM(),
   * check for null
   */
  public JPF(Config conf) {
    config = conf;
    if (logger == null) { // maybe somebody created a JPF object explicitly
      logger = initLogging(config);
    }
    initialize();
  }
  
  private void initialize() { 
    
    try {
      vm = config.getEssentialInstance("vm.class", JVM.class);
      
      Class[] searchArgTypes = { Config.class, JVM.class };
      Object[] searchArgs = { config, vm };
      search = config.getEssentialInstance("search.class", Search.class,
                                                searchArgTypes, searchArgs);
      
      addListeners();
    } catch (Config.Exception cx) {
      Debug.println(Debug.ERROR, cx);
    }
  }

  /**
   * As if called from main().
   */
  public JPF(String[] args) {
    config = createConfig(args);
    if (logger == null) {
      logger = initLogging(config);
    }

    printBanner(config);
    
    LogManager.printStatus(logger);
    config.printStatus(logger);
    
    if (isHelpRequest(args)) {
      showUsage();
    }
    
    if (isPrintConfigRequest(args)) {
      config.print(new PrintWriter(System.out));
    }
    
    if (config.getTargetArg() != null) {
      checkUnknownArgs(args);
      
      initialize();
    }
  }
  
  public boolean isRunnable () {
    return ((vm != null) && (search != null));
  }
  
  public void addSearchListener (SearchListener l) {
    if (search != null) {
      search.addListener(l);
    }
  }
  
  public void addVMListener (VMListener l) {
    if (vm != null) {
      vm.addListener(l);
    }
  }

  public void addSearchProperty (Property p) {
    if (search != null) {
      search.addProperty(p);
    }
  }
  
  void addListeners () throws Config.Exception {
    
    ObjArray<JPFListener> listeners =
      config.getInstances("jpf.listener", JPFListener.class);
    
    if (listeners != null) {
      for (JPFListener l : listeners) {
        if (l instanceof VMListener) {
          if (vm != null) {
            vm.addListener( (VMListener) l);
          }
        }
        if (l instanceof SearchListener) {
          if (search != null) {
            search.addListener( (SearchListener) l);
          }
        }
      }
    }
  }
  
  /**
   * return the search object. This can be null if the initialization has failed
   */
  public Search getSearch() {
    return search;
  }

  /**
   * return the VM object. This can be null if the initialization has failed
   */
  public JVM getVM() {
    return vm;
  }
  
  public static void exit() {
    // Hmm, exception as non local return. But we might be called from a
    // context we don't want to kill
    throw new ExitException();
  }

  public boolean foundErrors() {
    return !(search.getErrors().isEmpty());
  }


  public static boolean isHelpRequest (String[] args) {
    if (args == null) return true;
    if (args.length == 0) return true;
    
    for (int i=0; i<args.length; i++) {
      if ("-help".equals(args[i])) {
        args[i] = null;
        return true;
      }
    }
    
    return false;
  }
  
  public static boolean isPrintConfigRequest(String[] args) {
    if (args == null) return false;
    
    for (int i=0; i<args.length; i++) {
      if ("-show".equals(args[i])) {
        args[i] = null;
        return true;
      }
    }
    return false;
  }
    
  /**
   * this assumes that we have checked and 'consumed' (nullified) all known
   * options, so we just have to check for any '-' option prior to the
   * target class name
   */
  static void checkUnknownArgs (String[] args) {
    for ( int i=0; i<args.length; i++) {
      if (args[i] != null) {
        if (args[i].charAt(0) == '-') {
          logger.warning("unknown command line option: " + args[i]);
        } 
        else {
          // this is supposed to be the target class name - everything that follows
          // is supposed to be processed by the program under test
          break;
        }
      }
    }
  }
  
  public static void printBanner (Config config) {
    System.out.println("Java Pathfinder Model Checker v" +
                  config.getString("jpf.version", "4.0a") +
                  " - (C) 1999-2006 RIACS/NASA Ames Research Center");
  }
  
  
  public static void main(String[] args) {
    JPF jpf =  new JPF(args);
    jpf.run();
  }

  /**
   * find the value of an arg that is either specific as  
   * "-key=value" or as "-key value". If not found, the supplied
   * defValue is returned 
   */
  static String getArg(String[] args, String pattern, String defValue, boolean consume) {
    String s = defValue;

    for (int i = 0; i < args.length; i++) {
      String arg = args[i];

      if (arg != null) {
        if (arg.matches(pattern)) {
          int idx=arg.indexOf('=');
          if (idx > 0) {
            s = arg.substring(idx+1);
            if (consume) {
              args[i]=null;
            }
          } else if (i < args.length-1) {
            s = args[i+1];
            if (consume) {
              args[i] = null;
              args[i+1] = null;
            }
          }
          break;
        }
      }
    }
    return s;
  }
  
  /**
   * what property file to look for
   */
  static String getConfigFileName (String[] args) {
    return getArg(args, "-c(onfig)?(=.+)?", "jpf.properties", true);
  }
  
  /**
   * where to look for the file (if it's not in the current dir)
   */
  static String getRootDirName (String[] args) {
    return getArg(args, "[+]jpf[.]basedir(=.+)?", null, false); // stupid compiler complaining about escape seq
  }
  
  /**
   * return a Config object that holds the JPF options. This first
   * loads the properties from a (potentially configured) properties file, and
   * then overlays all command line arguments that are key/value pairs
   */
  public static Config createConfig (String[] args) {
    String pf = getConfigFileName(args);
    String rd = getRootDirName(args);

    return new Config(args, pf, rd, JPF.class);
  }
  
  /**
   * runs the verification.
   */
  public void run() {
    if (isRunnable()) {
      try {
        if (vm.initialize()) {
          search.search();
          printResults();
        }
      } catch (ExitException ex) {
        logger.severe( "JPF terminated");
      } catch (JPFException jx) {
        logger.severe( "JPF exception, terminating: " + jx.getMessage());
        if (config.getBoolean("jpf.print_exception_stack")) {
          jx.printStackTrace();
        }
        // bail here
      } catch (Throwable t) {
        // the last line of defense
        t.printStackTrace();
      }
    }
  }
  
  public ErrorList getSearchErrors () {
    if (search != null) {
      return search.getErrors();
    }
    
    return null;
  }
  
  static void showUsage() {
    System.out
        .println("Usage: \"java [<vm-option>..] gov.nasa.jpf.JPF [<jpf-option>..] [<app> [<app-arg>..]]");
    System.out.println("  <jpf-option> : -c <config-file>  : name of config properties file (default \"jpf.properties\")");
    System.out.println("               | -help  : print usage information");
    System.out.println("               | -show  : print configuration dictionary contents");
    System.out.println("               | +<key>=<value>  : add or override key/value pair to config dictionary");
    System.out.println("  <app>        : application class or *.xml error trace file");
    System.out.println("  <app-arg>    : arguments passed into main(String[]) if application class");
  }

  /**
   * Prints the results of the verification.
   * 
   * <2do>pcm - we have to unify the result output, Debug is the wrong place
   * for this
   */
  private void printResults() {
    ErrorList errors = search.getErrors();

    PrintWriter pw = new PrintWriter(System.out, true); // <2do> need to use logger for this
    
    // let the VM report whatever it has to (and we don't know about
    vm.printResults(pw);

    pw.println();
    pw.println("===================================");

    int nerrors = errors.size();

    if (nerrors == 0) {
      pw.println( "  No Errors Found");
    } else {
      if (nerrors == 1) {
        pw.println( "  1 Error Found: " + errors.get(0).getMessage());
      } else {
        pw.println( "  " + nerrors + " Errors Found");
      }
    }

    pw.println( "===================================");
  }

  public static void handleException(JPFException e) {
    Debug.println(Debug.ERROR, "jpf: " + e.getMessage());

    Debug.println(Debug.ERROR);
    e.printStackTrace();

    exit();
  }

  /**
   * private helper class for local termination of JPF (without killing the
   * whole Java process via System.exit).
   * While this is basically a bad non-local goto exception, it seems to be the
   * least of evils given the current JPF structure, and the need to terminate
   * w/o exiting the whole Java process. If we just do a System.exit(), we couldn't
   * use JPF in an embedded context
   */
  @SuppressWarnings("serial")
  static class ExitException extends RuntimeException {
    ExitException() {
    }

    ExitException(String msg) {
      super(msg);
    }
  }
}
