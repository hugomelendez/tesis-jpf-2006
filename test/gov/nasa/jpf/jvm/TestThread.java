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

/**
 * threading test
 */
public class TestThread {
  static String didRunThread = null;

  public static void main (String[] args) {
    TestThread t = new TestThread();

    if (args.length > 0) {
      // just run the specified tests
      for (int i = 0; i < args.length; i++) {
        String func = args[i];

        // note that we don't use reflection here because this would
        // blow up execution/test scope under JPF
        if ("testMain".equals(func)) {
          t.testMain();
        } else if ("testName".equals(func)) {
          t.testName();
        } else if ("testPriority".equals(func)) {
          t.testPriority();
        } else if ("testDaemon".equals(func)) {
          t.testDaemon();
        } else if ("testSingleYield".equals(func)) {
          t.testSingleYield();
        } else if ("testYield".equals(func)) {
          t.testYield();
        } else if ("testSyncRunning".equals(func)) {
          t.testSyncRunning();
        } else {
          throw new IllegalArgumentException("unknown test function");
        }
      }
    } else {
      // that's mainly for our standalone test verification
      t.testMain();
      t.testName();
      t.testPriority();
      t.testDaemon();
      t.testSingleYield();
      t.testYield();
    }
  }

  public void testDaemon () {
    // we should also test the correct Daemon behavior at some point
    // (running daemons not keeping the system alive)
    Runnable r = new Runnable() {
      public void run () {
        Thread t = Thread.currentThread();

        if (!t.isDaemon()) {
          throw new RuntimeException("isDaemon failed");
        }

        didRunThread = t.getName();
      }
    };

    didRunThread = null;

    Thread t = new Thread(r);
    t.setDaemon(true);
    t.start();

    try {
      t.join();
    } catch (InterruptedException ix) {
      throw new RuntimeException("thread was interrupted");
    }

    String tname = Thread.currentThread().getName();
    if ((didRunThread == null) || (didRunThread.equals(tname))) {
      throw new RuntimeException("thread did not execute: " + didRunThread);
    }
  }

  public void testMain () {
    Thread t = Thread.currentThread();
    String refName = "main";

    String name = t.getName();

    if (!name.equals(refName)) {
      throw new RuntimeException("wrong main thread name, is: " + name +
                                 ", expected: " + refName);
    }

    refName = "my-main-thread";
    t.setName(refName);
    name = t.getName();

    if (!name.equals(refName)) {
      throw new RuntimeException("Thread.setName() failed, is: " + name +
                                 ", expected: " + refName);
    }

    int refPrio = Thread.NORM_PRIORITY;
    int prio = t.getPriority();

    if (prio != refPrio) {
      throw new RuntimeException("main thread has no NORM_PRIORITY: " + prio);
    }

    refPrio++;
    t.setPriority(refPrio);
    prio = t.getPriority();

    if (prio != refPrio) {
      throw new RuntimeException("main thread setPriority failed: " + prio);
    }

    if (t.isDaemon()) {
      throw new RuntimeException("main thread is daemon");
    }
  }

  public void testName () {
    Runnable r = new Runnable() {
      public void run () {
        Thread t = Thread.currentThread();
        String name = t.getName();

        if (!name.equals("my-thread")) {
          throw new RuntimeException("wrong Thread name: " + name);
        }

        didRunThread = name;
      }
    };

    didRunThread = null;

    Thread t = new Thread(r, "my-thread");


    //Thread t = new Thread(r);
    t.start();

    try {
      t.join();
    } catch (InterruptedException ix) {
      throw new RuntimeException("thread was interrupted");
    }

    String tname = Thread.currentThread().getName();
    if ((didRunThread==null) || (didRunThread.equals(tname))) {
      throw new RuntimeException("thread did not execute: " + didRunThread);
    }
  }

  public void testSingleYield () {
    Thread.yield();
  }
  
  public void testYield () {
    Runnable r = new Runnable() {
      public void run () {
        Thread t = Thread.currentThread();

        while (!didRunThread.equals("blah")) {
          Thread.yield();
        }
        
        didRunThread = t.getName();
      }
    };

    didRunThread = "blah";

    Thread t = new Thread(r);
    t.start();
    
    while (didRunThread.equals("blah")) {
      Thread.yield();
    }
    
    String tname = Thread.currentThread().getName();
    if ((didRunThread == null) || (didRunThread.equals(tname))) {
      throw new RuntimeException("thread did not execute: " + didRunThread);
    }
  }
  
  public void testPriority () {
    Runnable r = new Runnable() {
      public void run () {
        Thread t = Thread.currentThread();
        int    prio = t.getPriority();

        if (prio != (Thread.MIN_PRIORITY + 2)) {
          throw new RuntimeException("wrong Thread priority: " + prio);
        }

        didRunThread = t.getName();
      }
    };

    didRunThread = null;

    Thread t = new Thread(r);
    t.setPriority(Thread.MIN_PRIORITY + 2);
    t.start();

    try {
      t.join();
    } catch (InterruptedException ix) {
      throw new RuntimeException("thread was interrupted");
    }

    String tname = Thread.currentThread().getName();
    if ((didRunThread == null) || (didRunThread.equals(tname))) {
      throw new RuntimeException("thread did not execute: " + didRunThread);
    }
  }
  
  public void testSyncRunning () {
    Runnable r = new Runnable() {
      public synchronized void run () {
        didRunThread = Thread.currentThread().getName();
      }
    };
    
    Thread t = new Thread(r);
    
    synchronized (r) {
      t.start();
      Thread.yield();
      if (didRunThread != null) {
        throw new RuntimeException("sync thread did execute before lock release"); 
      }
    }
    
    try {
      t.join();
    } catch (InterruptedException ix) {
      throw new RuntimeException("main thread was interrupted");
    }

    if (didRunThread == null) {
      throw new RuntimeException("sync thread did not run after lock release");       
    }
  }
}
