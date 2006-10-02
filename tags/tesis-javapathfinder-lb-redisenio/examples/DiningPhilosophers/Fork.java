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
/* This file is part of the Java Pathfinder (JPF) distribution from * NASA Ames Research Center. See file LICENSE for usage terms. * (C) 1999,2003 NASA Ames Research Center *//* Author: Masoud Mansouri-Samani * Date: 15 Sept 2003 */package DiningPhilosophers;public class Fork {  private boolean free;  private int num;  Fork(int n) {    free = true;    this.num = n;  }  synchronized void grab(int phil) {    while (!free) {      try {        wait();      } catch (InterruptedException e) {        System.out.println(e);      }    }    free = false;    System.out.println(phil+" Grabbed fork#: "+num);  }  synchronized void release(int phil) {    free = true;    notify();    System.out.println(phil+" Released fork#: "+num);  }}
