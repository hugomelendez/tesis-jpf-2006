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
 * This example shows how the use of the default <CODE>atomic-lines</CODE>
 * option could result in JPF reporting the existence of a bogus livelock. JPF
 * reports the livelock at:
 * <PRE>
 *    ...
 *    while (p2.y2 != 0 && y1 >= p2.y2);
 *    ...
 * </PRE>
 * The reason is obvious. By using the default option we have told JPF to run
 * the whole while loop atomically with no possibility of the other thread
 * changing the outcome of the evaluation of the while loop's condition. To
 * solve this you have two choices: You can use the <CODE>-no-atomic-lines
 * </CODE> option when you run JPF or alternatively add a dummy body to the
 * while loop, in effect making it a statement over multiple lines.
 */
class Property {
  static boolean flag = true;
}

/**
 * DOCUMENT ME!
 */
class Process1 extends Thread {
  public int       y1 = 0;
  private Process2 p2;

  public void run () {
    while (true) {
      y1 = p2.y2 + 1;

      //      System.out.println("P1: [ busy wait, y1=" + y1);
      while ((p2.y2 != 0) && (y1 >= p2.y2)) { }


      //      System.out.println("P1: ] busy wait, y1=" + y1);
      Property.flag = false;
      Property.flag = true;

      y1 = 0;
    }
  }

  void SetThread (Process2 p) {
    p2 = p;
  }
}

/**
 * DOCUMENT ME!
 */
class Process2 extends Thread {
  public int       y2 = 0;
  private Process1 p1;

  public void run () {
    while (true) {
      y2 = p1.y1 + 1;

      //      System.out.println("P2: [ busy wait, y2=" + y2);
      while ((p1.y1 != 0) && (y2 > p1.y1)) { }


      //      System.out.println("P2: ] busy wait, y2=" + y2);
      Property.flag = false;
      Property.flag = true;

      y2 = 0;
    }
  }

  void SetThread (Process1 p) {
    p1 = p;
  }
}

/**
 * DOCUMENT ME!
 */
class Bakery_original {
  public static void main (String[] args) {
    Process1 process1 = new Process1();
    Process2 process2 = new Process2();
    process1.SetThread(process2);
    process2.SetThread(process1);

    process1.start();
    process2.start();
  }
}
