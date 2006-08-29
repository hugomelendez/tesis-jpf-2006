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

public class DiningPhilosophers {
  
  // The number of philosophers
  public static final int NUM_PHILS = 2;

  public static void main(String args[]) {
    
    // Create the forks
    Fork[] forks = new Fork[NUM_PHILS];
    for (int i=0; i<NUM_PHILS; i++) {
      System.out.println("Creating fork#: "+i);
      forks[i]=new Fork(i);
    }
    
    // Create and start the Philosophers
    Philosopher[] philosophers = new Philosopher[NUM_PHILS];
    for (int i=0; i<NUM_PHILS; i++) {
      System.out.println("Creating philosopher#: "+i);
      philosophers[i]=new Philosopher(i, forks[i], forks[(i+1)%NUM_PHILS]);
      philosophers[i].start();
    }
  }
}
