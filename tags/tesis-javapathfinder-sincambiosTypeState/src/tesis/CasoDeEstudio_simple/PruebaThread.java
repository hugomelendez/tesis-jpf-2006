package tesis.CasoDeEstudio_simple;

import java.util.LinkedList;
import java.util.Queue;

public class PruebaThread {

    public static void main(String args[]) throws Exception {

        Object monitorPapa = new Integer(100);
        Hilo hijo = new Hilo(monitorPapa);

        new Thread(hijo).start();
        synchronized (monitorPapa) {
            System.out.println("Durmiendo");
            monitorPapa.wait();
        }
        System.out.println("Me despertó mi hijo");
        hijo.terminar();
    }

    public static class Hilo implements Runnable {

        private Object monitorPapa;
        private Object miMonitor;
        private boolean condicion;

        private Queue<Object> cola = new LinkedList<Object>();

        public Hilo(Object monitorPapa) {
            this.monitorPapa = monitorPapa;
            miMonitor = new String("Yo");
            condicion = true;
        }

        public void run() {
            synchronized (miMonitor) {
                while(condicion) {
                    System.out.println("Notificando a papá");
                    synchronized (monitorPapa) {
                        monitorPapa.notifyAll();
                    }
                    try {
                        Thread.sleep(10000);
                        System.out.println("A dormir");
                        miMonitor.wait();
                        while(!cola.isEmpty()) {
                            procesarCola(cola);
                        }
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                    //procesar
                }
                System.out.println("Terminé");
            }
        }

        private void procesarCola(Queue<Object> cola2) {
            // TODO Auto-generated method stub
        }

        public void meterTrabajo(Object trabajo) {
            synchronized (miMonitor) {
                cola.add(trabajo);
                miMonitor.notify();
            }
        }

        public void terminar() {
            synchronized (miMonitor) {
                condicion = false;
                miMonitor.notify();
            }
        }

    }
}
