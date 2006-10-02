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
/* Author: Masoud Mansouri-Samani
 * Date: 15 Sept 2003
 */

package DiningPhilosophers;

public class Philosopher extends Thread {
  private Fork left;
  private Fork right;
  private int num;

  Philosopher(int num, Fork left, Fork right) {
    this.num=num;
    this.left = left;
    this.right = right;
  }

  public void run() {
    for (;;) {
      System.out.println(num+" Hungry ...");
      left.grab(num);
      try {
        sleep(100);
      } catch (InterruptedException e) {
        System.out.println(e);
      }

      right.grab(num);

      System.out.println(num+" Eating ...");
      try {
        sleep((long) (Math.random() * 1000));
      } catch (InterruptedException e) {
        System.out.println(e);
      }
      System.out.println(num+" Finished eating.");
      
      left.release(num);

      right.release(num);

      System.out.println(num+" Thinking ...");
      try {
        sleep((long) (Math.random() * 1000));
      } catch (InterruptedException e) {
        System.out.println(e);
      }
      System.out.println(num+" Finished thinking.");
    }
  }
}
