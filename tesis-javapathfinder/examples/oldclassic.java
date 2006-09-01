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
/**
 * This example shows a deadlock which occurs as a result of a missed signals.
 * @author wvisser
 */
public class oldclassic {
  public static void main (String[] args) {
    Event      new_event1 = new Event();
    Event      new_event2 = new Event();

    FirstTask  task1 = new FirstTask(new_event1, new_event2);
    SecondTask task2 = new SecondTask(new_event1, new_event2);

    task1.start();
    task2.start();
  }
}

/**
 * DOCUMENT ME!
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
 * DOCUMENT ME!
 */
class FirstTask extends java.lang.Thread {
  Event event1;
  Event event2;
  int   count = 0;

  public FirstTask (Event e1, Event e2) {
    this.event1 = e1;
    this.event2 = e2;
  }

  public void run () {
    count = event1.count;

    while (true) {
      System.out.println("1");

      if (count == event1.count) {
        event1.wait_for_event();
      }

      count = event1.count;
      event2.signal_event();
    }
  }
}

/**
 * DOCUMENT ME!
 */
class SecondTask extends java.lang.Thread {
  Event event1;
  Event event2;
  int   count = 0;

  public SecondTask (Event e1, Event e2) {
    this.event1 = e1;
    this.event2 = e2;
  }

  public void run () {
    count = event2.count;

    while (true) {
      System.out.println("2");
      event1.signal_event();

      if (count == event2.count) {
        event2.wait_for_event();
      }

      count = event2.count;
    }
  }
}