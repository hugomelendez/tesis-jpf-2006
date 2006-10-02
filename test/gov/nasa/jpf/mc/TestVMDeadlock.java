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
package gov.nasa.jpf.mc;

abstract class SyncRunnable implements Runnable {
  SyncRunnable other;

  public void setOther (SyncRunnable other) {
    this.other = other;
  }

  public abstract void doSomething ();

  public abstract void doSomethingElse ();

  public abstract void run ();
}

/**
 * deadlock detection test
 */
public class TestVMDeadlock {
  public static void main (String[] args) {
    TestVMDeadlock t = new TestVMDeadlock();

    if (args.length > 0) {
      // just run the specified tests
      for (int i = 0; i < args.length; i++) {
        String func = args[i];

        // note that we don't use reflection here because this would
        // blow up execution/test scope under JPF
        if ("testSyncMthDeadlock".equals(func)) {
          t.testSyncMthDeadlock();
        } else if ("testSyncBlockDeadlock".equals(func)) {
          t.testSyncBlockDeadlock();
        } else if ("testMixedDeadlock".equals(func)) {
          t.testMixedDeadlock();
        } else if ("testMissedSignalDeadlock".equals(func)) {
          t.testMissedSignalDeadlock();
        } else {
          throw new IllegalArgumentException("unknown test function");
        }
      }
    } else {
      // quite useless to call the others, it locks up
      t.testSyncMthDeadlock();
    }
  }

  /**
   * Testing for deadlocks resulting from missed signal. This is the original
   * oldclassic example which has been turned into a test case.
   */
  public void testMissedSignalDeadlock () {
    Event      new_event1 = new Event();
    Event      new_event2 = new Event();

    FirstTask  task1 = new FirstTask(new_event1, new_event2);
    SecondTask task2 = new SecondTask(new_event1, new_event2);

    task1.start();
    task2.start();
  }

  /**
   * the test which mixes synchronized method attr with MONITORENTER/EXIT
   */
  public void testMixedDeadlock () {
    SyncRunnable r1 = new SyncMthRunnable();
    SyncRunnable r2 = new SyncBlockRunnable();
    r1.setOther(r2);
    r2.setOther(r1);

    //Thread t1 = new Thread(r1);
    Thread t2 = new Thread(r2);
    Thread t1 = new Thread(r1);

    
    t1.start();
    t2.start();
  }

  /**
   * the test which checks MONITORENTER / MONITOREXIT
   */
  public void testSyncBlockDeadlock () {
    SyncRunnable r1 = new SyncBlockRunnable();
    SyncRunnable r2 = new SyncBlockRunnable();
    r1.setOther(r2);
    r2.setOther(r1);

    Thread t1 = new Thread(r1);
    Thread t2 = new Thread(r2);

    t1.start();
    t2.start();
  }

  /**
   * the test which checks the synchronized method attribute
   */
  public void testSyncMthDeadlock () {
    SyncRunnable r1 = new SyncMthRunnable();
    SyncRunnable r2 = new SyncMthRunnable();
    r1.setOther(r2);
    r2.setOther(r1);

    Thread t1 = new Thread(r1);
    Thread t2 = new Thread(r2);

    t1.start();
    t2.start();
  }

  /**
   * Required by the testMissedSignalDeadlock().
   */
  class Event {
    int count = 0;

    public synchronized void signal_event () {
      count = (count + 1) % 3;
      notifyAll();
    }

    public synchronized void wait_for_event () {
      try {
        wait();
      } catch (InterruptedException e) {
      }
    }
  }

  /**
   * Required by the testMissedSignalDeadlock().
   */
  class FirstTask extends java.lang.Thread {
    Event event1;
    Event event2;
    int   count = 0;

    public FirstTask (Event e1, Event e2) {
      event1 = e1;
      event2 = e2;
    }

    public void run () {
      count = event1.count;

      while (true) {
        if (count == event1.count) {
          event1.wait_for_event();
        }

        count = event1.count;
        event2.signal_event();
      }
    }
  }

  /**
   * Required by the testMissedSignalDeadlock().
   */
  class SecondTask extends java.lang.Thread {
    Event event1;
    Event event2;
    int   count = 0;

    public SecondTask (Event e1, Event e2) {
      event1 = e1;
      event2 = e2;
    }

    public void run () {
      count = event2.count;

      while (true) {
        event1.signal_event();

        if (count == event2.count) {
          event2.wait_for_event();
        }

        count = event2.count;
      }
    }
  }

  /**
   * DOCUMENT ME!
   */
  class SyncBlockRunnable extends SyncRunnable {
    public void doSomething () {
      synchronized (this) {
        other.doSomethingElse();
      }
    }

    public void doSomethingElse () {
      synchronized (this) {
      }
    }

    public void run () {
      while (true) {
        synchronized (this) {
          other.doSomething();
        }
      }
    }
  }

  /**
   * DOCUMENT ME!
   */
  class SyncMthRunnable extends SyncRunnable {
    public synchronized void doSomething () {
      other.doSomethingElse();
    }

    public synchronized void doSomethingElse () {
    }

    public synchronized void run () {
      while (true) {
        other.doSomething();
      }
    }
  }
}
