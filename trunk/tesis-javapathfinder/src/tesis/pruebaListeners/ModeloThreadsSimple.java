package tesis.pruebaListeners;

import gov.nasa.jpf.jvm.Verify;

import java.util.Random;

class ModeloThreadSimple implements Runnable {

void mA(){
	int a;
}	
	
public void run () {
	mA();
 }
}

public class ModeloThreadsSimple {

	public static void main (String[] args) {
		ModeloThreadSimple o1 = new ModeloThreadSimple();
		ModeloThreadSimple o2 = new ModeloThreadSimple();

		Thread t1 = new Thread(o1);
		Thread t2 = new Thread(o2);

		t1.start();
		t2.start();
	}
}
