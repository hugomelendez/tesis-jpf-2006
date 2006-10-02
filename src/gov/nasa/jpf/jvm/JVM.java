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
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.jvm.bytecode.FieldInstruction;
import gov.nasa.jpf.jvm.bytecode.Instruction;
import gov.nasa.jpf.jvm.choice.ThreadChoiceFromSet;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.logging.Logger;


/**
 * This class represents the virtual machine. The virtual machine is able to
 * move backward and forward one transition at a time.
 */
public class JVM {
  
  static Logger log = JPF.getLogger("gov.nasa.jpf.jvm.JVM");
  
  /**
   * The number of errors saved so far.
   * Used to generate the name of the error trail file.
   */
  protected static int error_id;
        
  /**
   * <2do> - this is a hack to be removed once there are no static references
   * anymore
   */
  protected static JVM jvm;
  
  static {
    initStaticFields();
  }
  
  protected SystemState ss;
  
  // <2do> - if you are confused about the various pieces of state and its
  // storage/backtrack structures, I'm with you. It's mainly an attempt to
  // separate non-policy VM state (objects), policy VM state (Scheduler)
  // and general JPF execution state, with special support for stack oriented
  // state restoration (backtracking).
  // this needs to be cleaned up and the principle reinstated
  
  
  protected String mainClassName;
  protected String[] args;  /** main() arguments */
  
  protected Path path;  /** execution path to current state */
  protected StringBuilder out;  /** buffer to store output along path execution */
  
  
  /**
   * various caches for VMListener state acqusition. NOTE - these are only
   * valid during notification
   * 
   * <2do> this is not too nice, it probably would be better to pass the
   * corresponding objects as arguments (together with the VM), but that
   * means we finally give up on the gov.nasa.jpf package (which is a 
   * leftover anyways), and expose all these classes
   */
  Transition      lastTrailInfo;
  ClassInfo      lastClassInfo;
  ThreadInfo     lastThreadInfo;
  Instruction    lastInstruction;
  Instruction    nextInstruction;
  ElementInfo    lastElementInfo;
  ChoiceGenerator lastChoiceGenerator;
    
  /** the repository we use to find out if we already have seen a state */
  protected StateSet stateSet;
  
  protected int newStateId;
  
  /** the structure responsible for storing and restoring backtrack info */
  protected Backtracker backtracker;

  /** optional serializer/restorer to support backtracker */
  protected StateRestorer<?> restorer;
  
  /** optional serializer to support stateSet */
  protected StateSerializer serializer;
  
  /** annotation information structure. */
  protected final LoadedAnnotations annotations = new LoadedAnnotations();
  
  /** potential execution listeners */
  VMListener    listener;
  
  Config config; // that's for the options we use only once
  
  // JVM options we use frequently
  boolean runGc;
  boolean checkFP;
  boolean checkFPcompare;
  boolean atomicLines;
  boolean treeOutput;
  boolean pathOutput;
  boolean indentOutput;
  
  /**
   * VM instances are another example of evil throw-up ctors, but this is
   * justified by the fact that they are only created via (configured)
   * reflection from within the safe confines of the JPF ctor - which
   * shields clients against blowups
   */
  public JVM (Config conf) throws Config.Exception {
    // <2do> that's really a bad hack and should be removed once we
    // have cleaned up the reference chains
    jvm = this;
    
    config = conf;
    
    runGc = config.getBoolean("vm.gc", true);
    checkFP = config.getBoolean("vm.check_fp", false);
    checkFPcompare = config.getBoolean("vm.check_fp_compare", true);
    atomicLines = config.getBoolean("vm.por.atomic_lines", true);
    treeOutput = config.getBoolean("vm.tree_output", true);
    pathOutput = config.getBoolean("vm.path_output", false);
    indentOutput = config.getBoolean("vm.indent_output",false);

    initSubsystems(config);
    initFields(config); 
  }
    
  public void initFields (Config config) throws Config.Exception {
    mainClassName = config.getTargetArg();
    args = config.getTargetArgParameters();
            
    path = new Path(mainClassName);
    out = null;
    
    ss = new SystemState(config, this);
    
    stateSet = config.getInstance("vm.storage.class", StateSet.class);
    if (stateSet != null) stateSet.attach(this);
    backtracker = config.getEssentialInstance("vm.backtracker.class", Backtracker.class);
    backtracker.attach(this);
  }
  
  void initSubsystems (Config config) throws Config.Exception {
    ClassInfo.init(config);
    ThreadInfo.init(config);
    MethodInfo.init(config);
    DynamicArea.init(config);
    StaticArea.init(config);
    NativePeer.init(config);
    Transition.init(config);
    Step.init(config);
    FieldInstruction.init(config);
    JPF_gov_nasa_jpf_jvm_Verify.init(config);
    annotations.init(config);
  }
  
  /**
   * do we see our model classes? Some of them cannot be used from the standard CLASSPATH, because they
   * are tightly coupled with the JPF core (e.g. java.lang.Class, java.lang.Thread,
   * java.lang.StackTraceElement etc.)
   * Our strategy here is kind of lame - we just look into java.lang.Class, if we find the 'int cref' field
   * (that's a true '42')
   */
  static boolean checkModelClassAccess () {
    ClassInfo ci = ClassInfo.getClassInfo("java.lang.Class");
    return (ci.getDeclaredInstanceField("cref") != null);
  }

  
  /**
   * load and initialize startup classes, return 'true' if successful.
   * 
   * This loads a bunch of core library classes, initializes the main thread,
   * and then all the required startup classes, but excludes the static init of
   * the main class. Note that whatever gets executed in here should NOT contain
   * any non-determinism, since we are not backtrackable yet, i.e.
   * non-determinism in clinits should be constrained to the app class (and
   * classes used by it)
   */
  public boolean initialize () {
    // from here, we get into some bootstrapping process
    //  - first, we have to load class structures (fields, supers, interfaces..)
    //  - second, we have to create a thread (so that we have a stack)
    //  - third, with that thread we have to create class objects 
    //  - forth, we have to push the clinit methods on this stack
    List<ClassInfo> clinitQueue = registerStartupClasses();
    
    if (!checkModelClassAccess()) {
      log.severe( "error during VM runtime initialization: wrong model classes (check vm.[boot]classpath)");
      return false;      
    }
    
    // create the thread for the main class
    // note this is incomplete for Java 1.3 where Thread ctors rely on main's
    // 'inheritableThreadLocals' being set to 'Collections.EMPTY_SET', which
    // pulls in the whole Collections/Random smash, but we can't execute the
    // Collections.<clinit> yet because there's no stack before we have a main
    // thread. Let's hope none of the init classes creates threads in their <clinit>.
    ThreadInfo main = createMainThread();

    pushMain(config);
      
    pushClinits(clinitQueue, main);
          
    // the first transition probably doesn't have much choice (unless there were
    // threads started in the static init), but we want to keep it uniformly anyways
    ChoiceGenerator cg = new ThreadChoiceFromSet(getThreadList().getRunnableThreads(), true);
    ss.setNextChoiceGenerator(cg);
    
    return true;
  }
     
  /**
   * be careful - everything that's executed from within here is not allowed
   * to depend on static class init having been done yet
   */
  protected ThreadInfo createMainThread () {
    ElementInfo ei;
    DynamicArea da = getDynamicArea();
    
    // first we need a group for this baby (happens to be called "main")
    int grpObjref = da.newObject(ClassInfo.getClassInfo("java.lang.ThreadGroup"),
                                 null);
    
    // since we can't call methods yet, we have to init explicitly (BAD)
    int grpName = da.newString("main", null);
    ei = da.get(grpObjref);
    ei.setReferenceField("name", grpName);
    ei.setIntField("maxPriority", java.lang.Thread.MAX_PRIORITY);
    
    int tObjref = da.newObject(ClassInfo.getClassInfo("java.lang.Thread"), null);
    
    ei = da.get(tObjref);
    ei.setReferenceField("group", grpObjref);
    ei.setReferenceField("name", da.newString("main", null));
    ei.setIntField("priority", Thread.NORM_PRIORITY);
    
    // we need to keep the attributes on the JPF side in sync here
    // <2do> factor out the Thread/ThreadInfo creation so that it's less
    // error prone (even so this is the only location it's required for)
    ThreadInfo ti = new ThreadInfo( this, tObjref);
    ti.setPriority(java.lang.Thread.NORM_PRIORITY);
    ti.setName("main");    
    ti.setStatus(ThreadInfo.RUNNING);
    
    // we are already running, so add it to the list (and make it a root object)
    addThread(ti);
    
    return ti;
  }

  // note this has to be in order - we don't want to init a derived class before
  // it's parent is initialized
  void registerStartupClass (ClassInfo ci, List<ClassInfo> queue) {
    StaticArea sa = getStaticArea();
    
    if (!queue.contains(ci)) {

      if (ci.getSuperClass() != null) {
        registerStartupClass( ci.getSuperClass(), queue);
      }
      
      queue.add(ci);
            
      if (!sa.containsClass(ci.getName())){
        sa.addStartupClass(ci);
      }
    }
  }
  
  protected List<ClassInfo> registerStartupClasses () {
    ArrayList<ClassInfo> queue = new ArrayList<ClassInfo>(32);
    
    // the bare essentials
    registerStartupClass( ClassInfo.getClassInfo("java.lang.Object"), queue);
    registerStartupClass( ClassInfo.getClassInfo("java.lang.Class"), queue);
    
    // the builtin types (and their arrays)
    registerStartupClass( ClassInfo.getClassInfo("boolean"), queue);
    registerStartupClass( ClassInfo.getClassInfo("[Z"), queue);
    registerStartupClass( ClassInfo.getClassInfo("byte"), queue);
    registerStartupClass( ClassInfo.getClassInfo("[B"), queue);
    registerStartupClass( ClassInfo.getClassInfo("char"), queue);
    registerStartupClass( ClassInfo.getClassInfo("[C"), queue);
    registerStartupClass( ClassInfo.getClassInfo("short"), queue);
    registerStartupClass( ClassInfo.getClassInfo("[S"), queue);
    registerStartupClass( ClassInfo.getClassInfo("int"), queue);
    registerStartupClass( ClassInfo.getClassInfo("[I"), queue);
    registerStartupClass( ClassInfo.getClassInfo("long"), queue);
    registerStartupClass( ClassInfo.getClassInfo("[J"), queue);
    registerStartupClass( ClassInfo.getClassInfo("float"), queue);
    registerStartupClass( ClassInfo.getClassInfo("[F"), queue);
    registerStartupClass( ClassInfo.getClassInfo("double"), queue);
    registerStartupClass( ClassInfo.getClassInfo("[D"), queue);

    // standard system classes
    registerStartupClass( ClassInfo.getClassInfo("java.lang.String"), queue);
    registerStartupClass( ClassInfo.getClassInfo("java.lang.ThreadGroup"), queue);
    registerStartupClass( ClassInfo.getClassInfo("java.lang.Thread"), queue);
    registerStartupClass( ClassInfo.getClassInfo("java.io.PrintStream"), queue);
    registerStartupClass( ClassInfo.getClassInfo("java.io.InputStream"), queue);
    registerStartupClass( ClassInfo.getClassInfo("java.lang.System"), queue);
    
    // and finally the application class
    registerStartupClass( ClassInfo.getClassInfo(mainClassName), queue);
        
    return queue;
  }

  void pushClinits (List<ClassInfo> queue, ThreadInfo ti) {
    StaticArea sa = getStaticArea();
    
    // we have to traverse backwards, since what gets pushed last is executed first
    for (ListIterator<ClassInfo> it=queue.listIterator(queue.size()); it.hasPrevious(); ) {
      ClassInfo ci = it.previous();
      StaticElementInfo sei = sa.get(ci.getName());
      sei.createStartupClassObject(ci, ti);
      
      MethodInfo mi = ci.getMethod("<clinit>()V", false);      
      if (mi != null) {
        MethodInfo stub = mi.createDirectCallStub("[clinit]");
        StackFrame frame = new DirectCallStackFrame(stub);
        ti.pushFrame(frame);
      } else {
        ci.setInitialized();
      }
    }
  }
  
  
  void pushMain (Config config) {
    DynamicArea da = ss.ks.da;
    ClassInfo ci = ClassInfo.getClassInfo(mainClassName);
    MethodInfo mi = ci.getMethod("main([Ljava/lang/String;)V", false);
    ThreadInfo ti = ss.getThreadInfo(0);
    
    if (mi == null) {
      throw new JPFException("no main() method in " + ci.getName());
    }
    
    ti.pushFrame(new StackFrame(mi, null));
    ti.setStatus(ThreadInfo.RUNNING);
    
    int argsObjref = da.newArray("Ljava/lang/String;", args.length, null);
    ElementInfo argsElement = ss.ks.da.get(argsObjref);
    
    for (int i = 0; i < args.length; i++) {
      int stringObjref = da.newString(args[i], null);
      argsElement.setElement(i, stringObjref);
    }
    ti.setLocalVariable(0, argsObjref, true);
  }

  public void addListener (VMListener newListener) {
    listener = VMListenerMulticaster.add(listener, newListener);
  }

  public void removeListener (VMListener removeListener) {
    listener = VMListenerMulticaster.remove(listener,removeListener);
  }
   
  void notifyChoiceGeneratorSet (ChoiceGenerator cg) {
    if (listener != null) {
      lastChoiceGenerator = cg;
      listener.choiceGeneratorSet(this);
      lastChoiceGenerator = null;
    }
  }
  
  void notifyChoiceGeneratorAdvanced (ChoiceGenerator cg) {
    if (listener != null) {
      lastChoiceGenerator = cg;
      listener.choiceGeneratorAdvanced(this);
      lastChoiceGenerator = null;
    }
  }
  
  void notifyChoiceGeneratorProcessed (ChoiceGenerator cg) {
    if (listener != null) {
      lastChoiceGenerator = cg;
      listener.choiceGeneratorProcessed(this);
      lastChoiceGenerator = null;
    }
  }

  void notifyExecuteInstruction (ThreadInfo ti, Instruction insn) {
    if (listener != null) {
      lastThreadInfo = ti;
      lastInstruction = insn;

      listener.executeInstruction(this);

      //nextInstruction = null;
      //lastInstruction = null;
      //lastThreadInfo = null;
    }
  }
  
  void notifyInstructionExecuted (ThreadInfo ti, Instruction insn, Instruction nextInsn) {
    if (listener != null) {
      lastThreadInfo = ti;
      lastInstruction = insn;
      nextInstruction = nextInsn;

      listener.instructionExecuted(this);

      //nextInstruction = null;
      //lastInstruction = null;
      //lastThreadInfo = null;
    }
  }
  
  void notifyThreadStarted (ThreadInfo ti) {
    if (listener != null) {
      lastThreadInfo = ti;
      listener.threadStarted(this);
      //lastThreadInfo = null;
    }
  }

  void notifyThreadBlocked (ThreadInfo ti) {
    if (listener != null) {
      lastThreadInfo = ti;
      listener.threadBlocked(this);
      //lastThreadInfo = null;
    }
  }

  void notifyThreadWaiting (ThreadInfo ti) {
    if (listener != null) {
      lastThreadInfo = ti;
      listener.threadWaiting(this);
      //lastThreadInfo = null;
    }
  }

  void notifyThreadNotified (ThreadInfo ti) {
    if (listener != null) {
      lastThreadInfo = ti;
      listener.threadNotified(this);
      //lastThreadInfo = null;
    }
  }

  void notifyThreadInterrupted (ThreadInfo ti) {
    if (listener != null) {
      lastThreadInfo = ti;
      listener.threadInterrupted(this);
      //lastThreadInfo = null;
    }
  }

  void notifyThreadTerminated (ThreadInfo ti) {
    if (listener != null) {
      lastThreadInfo = ti;
      listener.threadTerminated(this);
      //lastThreadInfo = null;
    }
  }

  void notifyThreadScheduled (ThreadInfo ti) {
    if (listener != null) {
      lastThreadInfo = ti;
      listener.threadScheduled(this);
      //lastThreadInfo = null;
    }
  }
  
  void notifyClassLoaded (ClassInfo ci) {
    if (listener != null) {
      lastClassInfo = ci;
      listener.classLoaded(this);
      lastClassInfo = null;
    }
  }
  
  void notifyObjectCreated (ThreadInfo ti, ElementInfo ei) {
    if (listener != null) {
      lastThreadInfo = ti;
      lastElementInfo = ei;

      listener.objectCreated(this);

      lastElementInfo = null;
      lastThreadInfo = null;
    }
  }
  
  void notifyObjectReleased (ElementInfo ei) {
    if (listener != null) {
      lastElementInfo = ei;
      listener.objectReleased(this);
      lastElementInfo = null;
    }
  }
  
  void notifyObjectLocked (ThreadInfo ti, ElementInfo ei){
    if (listener != null) {
      lastThreadInfo = ti;
      lastElementInfo = ei;
      
      listener.objectLocked(this);

      lastElementInfo = null;
      lastThreadInfo = null;
    }
  }
  
  void notifyObjectUnlocked (ThreadInfo ti, ElementInfo ei) {
    if (listener != null) {
      lastThreadInfo = ti;
      lastElementInfo = ei;
      
      listener.objectUnlocked(this);

      lastElementInfo = null;
      lastThreadInfo = null;
    }
  }
  
  void notifyObjectWait (ThreadInfo ti, ElementInfo ei) {
    if (listener != null) {
      lastThreadInfo = ti;
      lastElementInfo = ei;
      
      listener.objectWait(this);

      lastElementInfo = null;
      lastThreadInfo = null;
    }
  }
  
  void notifyObjectNotifies (ThreadInfo ti, ElementInfo ei) {
    if (listener != null) {
      lastThreadInfo = ti;
      lastElementInfo = ei;
      
      listener.objectNotify(this);

      lastElementInfo = null;
      lastThreadInfo = null;
    }
  }

  void notifyObjectNotifiesAll (ThreadInfo ti, ElementInfo ei) {
    if (listener != null) {
      lastThreadInfo = ti;
      lastElementInfo = ei;
      
      listener.objectNotifyAll(this);

      lastElementInfo = null;
      lastThreadInfo = null;
    }
  }
  
  void notifyGCBegin () {
    if (listener != null) {
      listener.gcBegin(this);
    }
  }
  
  void notifyGCEnd () {
    if (listener != null) {
      listener.gcEnd(this);
    }
  }
  
  void notifyExceptionThrown (ThreadInfo ti, ElementInfo ei) {
    if (listener != null) {
      lastThreadInfo = ti;
      lastElementInfo = ei;

      listener.exceptionThrown(this);
      
      lastElementInfo = null;
      lastThreadInfo = null;
    }
  }
  
  // VMListener acquisition
  public int getThreadNumber () {
    if (lastThreadInfo != null) {
      return lastThreadInfo.getIndex();
    } else {
      return -1;
    }
  }
  
  // VMListener acquisition
  public String getThreadName () {
    ThreadInfo ti = ThreadInfo.getCurrent();
    
    return ti.getName();
  }
  
  // VMListener acquisition
  Instruction getInstruction () {
    ThreadInfo ti = ThreadInfo.getCurrent();
    return ti.getPC();
  }

  
  public int getAbstractionNonDeterministicThreadCount () {
    int n = 0;
    int imax = ss.getThreadCount();
    
    for (int i = 0; i < imax; i++) {
      ThreadInfo th = ss.getThreadInfo(i);
      
      if (th.isAbstractionNonDeterministic()) {
        n++;
      }
    }
    
    return n;
  }
  
  public int getAliveThreadCount () {
    return getThreadList().getLiveThreadCount();
  }
  
  public ExceptionInfo getPendingException () {
    return ThreadInfo.currentThread.getPendingException();
  }
  
  public boolean isBoringState () {
    return ss.isBoring();
  }
  
  /**
   * @deprecated doesn't appear to be used/important  -peterd
   */
  public ElementInfo getClassReference (String name) {
    if (ClassInfo.exists(name)) {
      return ss.ks.sa.get(name);
    }
    
    return null;
  }
  
  public boolean hasPendingException () {
    return (ThreadInfo.currentThread.pendingException != null);
  }
  
  public boolean isDeadlocked () {
    int     length = ss.getThreadCount();
    boolean result = false;
    
    for (int i = 0; i < length; i++) {
      ThreadInfo th = ss.getThreadInfo(i);
      
      // if there's at least one runnable, we are not deadlocked
      if (th.willBeRunnable()) {
        return false;
      }
      
      // if stack depth is 0, it's done
      // otherwise we have a deadlock if we don't find any runnable
      if (th.countStackFrames() != 0) {
        result = true;
      }
    }
    
    return result;
  }
  
  public boolean isEndState () {    
    // note this uses 'alive', not 'runnable', hence isEndStateProperty won't
    // catch deadlocks - but that would be NoDeadlockProperty anyway
    return ss.isEndState();
  }
  
  public Exception getException () {
    return ss.getUncaughtException();
  }
  
  public boolean isInterestingState () {
    return ss.isInteresting();
  }
    
  public Step getLastStep () {
    Transition trail = ss.getTrail();
    if (trail != null) {
      return trail.getLastStep();
    }
    
    return null;
  }
  
  public Transition getLastTransition () {
    if (path.length() == 0) {
      return null;
    }
    return path.get(path.length() - 1);
  }
  
  /**
   * answer the ClassInfo that was loaded most recently
   * part of the VMListener state acqusition (only valid from inside of
   * notification)
   */
  public ClassInfo getLastClassInfo () {
    return lastClassInfo;
  }
  
  /**
   * answer the ThreadInfo that was most recently started or finished
   * part of the VMListener state acqusition (only valid from inside of
   * notification)
   */
  public ThreadInfo getLastThreadInfo () {
    return lastThreadInfo;
  }
  
  /**
   * answer the last executed Instruction
   * part of the VMListener state acqusition (only valid from inside of
   * notification)
   */
  public Instruction getLastInstruction () {
    return lastInstruction;
  }
  
  /**
   * answer the next Instruction to execute in the current thread
   * part of the VMListener state acqusition (only valid from inside of
   * notification)
   */
  public Instruction getNextInstruction () {
    return nextInstruction;
  }
  
  /**
   * answer the Object that was most recently created or collected
   * part of the VMListener state acqusition (only valid from inside of
   * notification)
   */
  public ElementInfo getLastElementInfo () {
    return lastElementInfo;
  }
  
  /**
   * return the most recently used CoiceGenerator
   */
  public ChoiceGenerator getLastChoiceGenerator () {
    return lastChoiceGenerator;
  }
  
  /**
   * answer the ClassInfo that was loaded most recently
   * part of the VMListener state acqusition
   */
  public ClassInfo getClassInfo () {
    return lastClassInfo;
  }
  
  public String getMainClassName () {
    return mainClassName;
  }
  
  public String[] getArgs () {
    return args;
  }
  
  public void setPath (Path p) {
  }
  
  public Path getPath () {
    return path.clone();
  }
  
  public int getPathLength () {
    return path.length();
  }
  
  
  /**
   * Gets reference (element info) for 
   * @param name - gov.my.class my.var.name
   * @return - 
   * @deprecated doesn't appear to be used -peterd
   */
  public ElementInfo getReference (String name) {
    // first of all I have to get to a class
    StringTokenizer st = new StringTokenizer(name, ".");
    StringBuffer    sb = new StringBuffer();
    ElementInfo     r = null;
    
    while (st.hasMoreTokens()) {
      sb.append(st.nextToken());
      
      r = getClassReference(sb.toString());
      
      if (r != null) {
        break;
      }
      
      sb.append('.');
    }
    
    if (r == null) {
      throw new JPFException("invalid argument: " + name);
    }
    
    // now walk through the fields
    while (st.hasMoreTokens()) {
      r = r.getDeclaredObjectField(st.nextToken(), sb.toString());
    }
    
    return r;
  }
  
  /**
   * @deprecated
   */
  public VMState getRestorableForwardState () {
    return new VMState(this);
  }
  
  public int getRunnableThreadCount () {
    return ss.getRunnableThreadCount();
  }
    
  public ThreadList getThreadList () {
    return getKernelState().getThreadList();
  }
  
  public boolean checkFP () {
    return checkFP;
  }
  
  public boolean checkNaN (double r) {
    if (checkFP) {
      return !(Double.isNaN(r) || Double.isInfinite(r));
    } else {
      return true;
    }
  }
  
  public boolean checkNaN (float r) {
    if (checkFP) {
      return !(Float.isNaN(r) || Float.isInfinite(r));
    } else {
      return true;
    }
  }
  
  public boolean checkNaNcompare (float r1, float r2) {
    if (checkFPcompare) {
      return !(Float.isNaN(r1) || Float.isNaN(r2) ||
        (Float.isInfinite(r1) && Float.isInfinite(r2) && (r1 == r2)));
    } else {
      return true;
    }
  }
  
  public boolean checkNaNcompare (double r1, double r2) {
    if (checkFPcompare) {
      return !(Double.isNaN(r1) || Double.isNaN(r2) ||
        (Double.isInfinite(r1) && Double.isInfinite(r2) && (r1 == r2)));
    } else {
      return true;
    }
  }
  
  /**
   * Bundles up the state of the system for export
   */
  public VMState getState () {
    return new VMState(this);
  }
  
  /**
   * Gets the system state.
   */
  public SystemState getSystemState () {
    return ss;
  }
  
  public KernelState getKernelState () {
    return ss.ks;
  }
  
  public Config getConfig() {
    return config;
  }
  
  public Backtracker getBacktracker() {
    return backtracker;
  }
  
  @SuppressWarnings("unchecked")
  public <T> StateRestorer<T> getRestorer() throws Config.Exception {
    if (restorer == null) {
      if (serializer instanceof StateRestorer) {
        restorer = (StateRestorer<?>) serializer;
      } else if (stateSet instanceof StateRestorer) {
        restorer = (StateRestorer<?>) stateSet;
      } else {
        // config read only if serializer is not also a restorer
        restorer = config.getInstance("vm.restorer.class", StateRestorer.class);
        if (serializer instanceof IncrementalChangeTracker &&
            restorer instanceof IncrementalChangeTracker) {
          throw config.new Exception("Incompatible serializer and restorer!");
        }
      }
      restorer.attach(this);
    }
    return (StateRestorer<T>) restorer;
  }
  
  public StateSerializer getSerializer() throws Config.Exception {
    if (serializer == null) {
      serializer = config.getEssentialInstance("vm.serializer.class",
                                      StateSerializer.class);
      serializer.attach(this);
    }
    return serializer;
  }
  
  /**
   * Returns the stateSet if states are being matched.
   */
  public StateSet getStateSet() {
    return stateSet;
  }
  
  public LoadedAnnotations getLoadedAnnotations() {
    return annotations;
  }
  
  /**
   * return the current SystemState's ChoiceGenerator object
   */
  public ChoiceGenerator getChoiceGenerator () {
    return ss.getChoiceGenerator();
  }
  
  public boolean isTerminated () {
    return ss.ks.isTerminated();
  }
  
  public void print (String s) {
    if (treeOutput) {
      System.out.print(s);
    }
    
    if (pathOutput) {
      appendOutput(s);
    }
  }

  public void println (String s) {
    if (treeOutput) {
      if (indentOutput){
        StringBuffer indent = new StringBuffer();
        int i;
        for (i = 0;i<=path.length();i++) {
          indent.append("|" + i); 	
        }
        System.out.println(indent + "|" +s);
      }
      else {
        System.out.println(s);
      }
    }
    
    if (pathOutput) {
      appendOutput(s);
      appendOutput('\n');
    }
  }

  public void print (boolean b) {
    if (treeOutput) {
      System.out.print(b);
    }
    
    if (pathOutput) {
      appendOutput(Boolean.toString(b));
    }
  }
  
  public void print (char c) {
    if (treeOutput) {
      System.out.print(c);
    }
    
    if (pathOutput) {
      appendOutput(c);
    }
  }
  
  public void print (int i) {
    if (treeOutput) {
      System.out.print(i);
    }
    
    if (pathOutput) {
      appendOutput(Integer.toString(i));
    }
  }

  public void print (long l) {
    if (treeOutput) {
      System.out.print(l);
    }
    
    if (pathOutput) {
      appendOutput(Long.toString(l));
    }
  }

  public void print (double d) {
    if (treeOutput) {
      System.out.print(d);
    }
    
    if (pathOutput) {
      appendOutput(Double.toString(d));
    }
  }

  public void print (float f) {
    if (treeOutput) {
      System.out.print(f);
    }
    
    if (pathOutput) {
      appendOutput(Float.toString(f));
    }
  }

  public void println () {
    if (treeOutput) {
      System.out.println();
    }
    
    if (pathOutput) {
      appendOutput('\n');
    }
  }

  
  void appendOutput (String s) {
    if (out == null) {
      out = new StringBuilder();
    }
    out.append(s);
  }
  
  void appendOutput (char c) {
    if (out == null) {
      out = new StringBuilder();
    }
    out.append(c);
  }
  
  public void storeTrace () {
    ChoicePoint.storeTrace(config.getString("vm.use_trace"),
                           mainClassName, args, ss.getChoiceGenerators());
  }
  
  /**
   * JVM specific results
   */
  public void printResults (PrintWriter pw) {
    if (config.getBoolean("vm.report.show_threads")) {      
      if (!ss.isInitState()) { // otherwise there is not much to report
        pw.println("----------------------------------- live threads");
        printStackTraces(pw);

        storeTrace();
      }
    }
  }
  
      
  
  /**
   * print call stacks of all live threads
   */
  public void printStackTraces (PrintWriter pw) {
    int imax = ss.getThreadCount();
    int n=0;
    
    for (int i = 0; i < imax; i++) {
      ThreadInfo ti = ss.getThreadInfo(i);
      String[] cs = ti.getCallStack();
      
      if (cs.length > 0) {
        n++;
        //pw.print("Thread: ");
        //pw.print(ti.getName());
        pw.println(ti.getStateDescription());
        
        LinkedList<ElementInfo> locks = ti.getLockedObjects();
        if (!locks.isEmpty()) {
          pw.print("  owned locks:");
          boolean first = true;
          for (ElementInfo e : locks) {
            if (first) {
              first = false;
            } else {
              pw.print(",");
            }
            pw.print(e);
          }
          pw.println();
        }
        
        ElementInfo ei = ti.getLockObject();
        if (ei != null) {
          if (ti.getStatus() == ThreadInfo.WAITING) {
            pw.print( "  waiting on: ");
          } else {
            pw.print( "  blocked on: ");
          }
          pw.println(ei);
        }
        
        pw.println("  call stack:");
        for (int j=cs.length-1; j >= 0; j--) {
          pw.println(cs[j]);
        }
        pw.println();
      }
    }
    
    if (n==0) {
      pw.println("no live threads");
    }
  }
  
  // just a Q&D debugging aid
  void dumpThreadStates () {
    java.io.PrintWriter pw = new java.io.PrintWriter(System.out, true);
    printStackTraces(pw);
    pw.flush();
  }
  
  /**
   * Moves one step backward. This method and forward() are the main methods
   * used by the search object.
   * Note this is called with the state that caused the backtrack still being on
   * the stack, so we have to remove that one first (i.e. popping two states
   * and restoring the second one)
   */
  public boolean backtrack () {
    boolean success = backtracker.backtrack();

    if (success) {
      // restore the path
      path.removeLast();
      lastTrailInfo = path.getLast();

      return ((ss.getId() != StateSet.UNKNOWN_ID) || (stateSet == null));
    } else {
      return false;
    }
  }
    
  /**
   * store the current SystemState's TrainInfo in our path, after updating it
   * with whatever annotations the JVM wants to add.
   * This is supposed to be called after each transition we want to keep
   */
  void updatePath () {
    Transition t = ss.getTrail();
    
    // <2do> we should probably store the output directly in the TrailInfo,
    // but this might not be our only annotation in the future

    // did we have output during the last transition? If yes, add it
    if ((out != null) && (out.length() > 0)) {
      t.setOutput( out.toString());
      out.setLength(0);
    }
    
    path.add(t); 
  }
  
  /**
   * try to advance the state
   * forward() and backtrack() are the two primary interfaces towards the Search
   * driver
   * return 'true' if there was an un-executed sequence out of the current state,
   * 'false' if it was completely explored
   * note that the caller still has to check if there is a next state, and if
   * the executed instruction sequence led into a new or already visited state
   */
  public boolean forward () {
    while (true) { // loop until we find a state that isn't ignored
      try {
        // saves the current state for backtracking purposes of depth first
        // searches and state observers. If there is a previously cached
        // kernelstate, use that one
        backtracker.pushKernelState();
        
        // cache this before we execute (and increment) the next insn(s)
        lastTrailInfo = path.getLast();
        
        // execute the instruction(s) to get to the next state
        // this changes the SystemState (e.g. finds the next thread to run)
        if (ss.nextSuccessor(this)) {
          //for debugging locks:  -peterd
          //ss.ks.da.verifyLockInfo();

          if (ss.isIgnored()) {
            // do it again
            backtracker.backtrackKernelState();
            continue;
            
          } else { // this is the normal forward that executed insns, and wasn't ignored 
            // runs the garbage collector (if necessary), which might change the
            // KernelState (DynamicArea). We need to do this before we hash the state to
            // find out if it is a new one
            // Note that we don't collect if there is a pending exception, since
            // we want to preserve as much state as possible for debug purposes
            if (runGc && !hasPendingException()) {
              ss.gcIfNeeded();
            }
            
            // saves the backtrack information. Unfortunately, we cannot cache
            // this (except of the optional lock graph) because it is changed
            // by the subsequent operations (before we return from forward)
            backtracker.pushSystemState();
            
            updatePath();
            break;
          }
          
        } else { // state was completely explored, no transition ocurred
          backtracker.popKernelState();
          return false;
        }
        
      } catch (UncaughtException e) {
        updatePath(); // or we loose the last transition
        // something blew up, so we definitely executed something (hence return true)
        return true;
      } catch (RuntimeException e) {
        throw new JPFException(e);
      }
    }
    
    if (stateSet != null) {
      newStateId = stateSet.size();
      int id = stateSet.addCurrent();
      ss.setId(id);
    }
    
    // the idea is that search objects or observers can query the state
    // *after* forward/backtrack was called, and that all changes of the
    // System/KernelStates happen from *within* forward/backtrack, i.e. the
    // (expensive) getBacktrack/storingData operations can be cached and used
    // w/o re-computation in the next forward pushXState()
    //cacheKernelState(); // for subsequent getState() and the next forward()

    return true;
  }
  
    
  /**
   * Prints the current stack trace.
   */
  public void printCurrentStackTrace () {
    ThreadInfo th = ThreadInfo.getCurrent();
    
    if (th != null) {
      th.printStackTrace();
    }
  }
      

  public void restoreState (VMState state) {
    if (state.path == null) {
      throw new JPFException("tried to restore partial VMState: " + state);
    }
    backtracker.restoreState(state.getBkState());
    path = state.path.clone();
  }
  
  public void savePath (Path path, Writer w) {
    PrintWriter out = new PrintWriter(w);
    
    Transition.toXML(out, path);
  }
  
  public void activateGC () {
    ss.activateGC();
  }
  
  /**
   * override the state matching - ignore this state, no matter if we changed
   * the heap or stacks.
   * use this with care, since it prunes whole search subtrees
   */
  public void ignoreState () {
    ss.setIgnored(true);
  }
  
  /**
   * answers if the current state already has been visited. This is mainly
   * used by the searches (to control backtracking), but could also be useful
   * for observers to build up search graphs (based on the state ids)
   */
  public boolean isNewState() {
    if (stateSet != null) {
      if (ss.isIgnored()){
        return false;
      } else {
        return newStateId == ss.getId();
      }
    } else {
      return true;
    }
  }
  
  /**
   * get the numeric id for the current state
   * Note: this can be called several times (by the search and observers) for
   * every forward()/backtrack(), so we want to cache things a bit
   */
  public int getStateId() {
    return ss.getId();
  }
    
  public void addThread (ThreadInfo ti) {
    // link the new thread into the list
    ThreadList tl = getThreadList();
    int idx = tl.add(ti);
    
    // link back the thread to the list 
    ti.setListInfo(tl, idx); 

    getKernelState().changed();
  }
  
  
  public ThreadInfo createThread (int objRef) {
    ThreadInfo ti = ThreadInfo.createThreadInfo(this, objRef);    
    
    // we don't add this thread to the threadlist before it starts to execute
    // since it would otherwise already be a root object
    
    return ti;
  }  
    
  public static JVM getVM () {
    // <2do> remove this, no more static refs!
    return jvm;
  }
  
  /**
   * initialize all our static fields. Called from <clinit> and reset
   */
  static void initStaticFields () {
    error_id = 0;
  }
  
  /**
   * return the 'heap' object, which is a global service
   */
  public DynamicArea getDynamicArea () {
    return ss.ks.da;
  }
    
  public ThreadInfo getCurrentThread () {
    return ThreadInfo.currentThread;
  }
  
  /**
   * same for "loaded classes", but be advised it will probably go away at some point
   */
  public StaticArea getStaticArea () {
    return ss.ks.sa;
  }

  public void resetNextCG() {
    if (ss.nextCg != null) {
      ss.nextCg.reset();
    }
  }
  
  
}




