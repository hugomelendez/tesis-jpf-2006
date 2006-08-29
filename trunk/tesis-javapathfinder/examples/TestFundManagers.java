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
import gov.nasa.jpf.jvm.Verify;


/**
 * DOCUMENT ME!
 */
interface FundConstants {
  static final int noOfStocks = 3;
  static final int noOfManagers = 2; //150;
  static final int testDuration = 20;
  static final int randomTransfers = 2; // 1000;
  static final int initialBalance = 1000; // 10000
  static final int totalMoneyInUniverse = noOfStocks * initialBalance;
}

/**
 * DOCUMENT ME!
 */
class FundManager implements Runnable, FundConstants {
  boolean trading = false;

  public boolean isTrading () {
    return trading;
  }

  public void run () {
    int next = 0;

    while (true) {
      trading = true;
      Stocks.transfer(TestFundManagers.transfers[next++]);
      trading = false;

      if (next == randomTransfers) {
        next = 0;
      }
    }
  }
}

/**
 * DOCUMENT ME!
 */
class Stocks implements FundConstants {
  static int[] balances = new int[noOfStocks];

  static void checkSystem () {
    int actual = 0;

    for (int n = 0; n < noOfStocks; n++) {
      actual += balances[n];
    }

    assert (actual == totalMoneyInUniverse);
  }

  static void init () {
    for (int n = 0; n < noOfStocks; n++) {
      balances[n] = initialBalance;
    }
  }

  static void transfer (Transfer t) {
    balances[t.fundFrom] -= t.amount;
    balances[t.fundTo] += t.amount;
  }
}

/**
 * DOCUMENT ME!
 */
public class TestFundManagers implements FundConstants {
  public static Transfer[] transfers = new Transfer[randomTransfers];

  static {
    for (int n = 0; n < randomTransfers; n++) {
      transfers[n] = new Transfer();
    }
  }

  public static void main (String[] args) {
    Stocks.init();

    java.lang.Thread[] threads = new java.lang.Thread[noOfManagers];
    FundManager[]      mgrs = new FundManager[noOfManagers];

    for (int n = 0; n < noOfManagers; n++) {
      mgrs[n] = new FundManager();
      threads[n] = new java.lang.Thread(mgrs[n]);
      threads[n].start();
    }

    while (true) {
      Verify.beginAtomic();

      boolean doCheck = true;

      for (int n = 0; n < noOfManagers; n++) {
        doCheck = doCheck && !mgrs[n].isTrading();
      }

      if (doCheck) {
        Stocks.checkSystem();
      }

      Verify.endAtomic();
    }
  }
}

/**
 * DOCUMENT ME!
 */
class Transfer implements FundConstants {
  public final int fundFrom;
  public final int fundTo;
  public final int amount;

  public Transfer () {
    fundFrom = Verify.random(noOfStocks - 1);
    fundTo = Verify.random(noOfStocks - 1);
    amount = 100;
  }
}
