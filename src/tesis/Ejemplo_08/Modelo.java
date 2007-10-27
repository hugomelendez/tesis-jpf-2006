package tesis.Ejemplo_08;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Modelo representacion de interaccion entre sockets
 * Obtenido de <blockquote>Effective Typestate Veriﬁcation in the Presence of Aliasing</blockquote>
 * 
 * @author hugo
 */

class Modelo {
	public static Socket createSocket() {
		return new Socket();
	}

	public static Collection createSockets() {
		Collection<Socket> result = new LinkedList<Socket>();
		for (int i = 0; i < 5; i++) {
			result.add(new Socket());
		}
		return result;
	}

	public static Collection readMessages() throws IOException {
		Collection result = new ArrayList();
		FileInputStream f = new FileInputStream("/etc/fstab");

		f.read();
		
		return result;
	}

	public static void talk(Socket s) throws IOException {
		Collection messages = readMessages();
		PrintWriter o = new PrintWriter(s.getOutputStream(), true);
		for (Iterator it = messages.iterator(); it.hasNext();) {
			Object message = it.next();
			o.print(message);
		}
		o.close();
	}

	public static void example() throws IOException {
		InetAddress ad;
		ad = InetAddress.getByName("www.google.com");
		Socket handShake = createSocket();
		handShake.connect(new InetSocketAddress(ad, 80));
		InputStream inp = handShake.getInputStream();
		Collection sockets = createSockets();
		for (Iterator it = sockets.iterator(); it.hasNext();) {
			Socket s = (Socket) it.next();
				s.connect(new InetSocketAddress(ad, 80));
			talk(s);
		}
		talk(handShake);
	}

	public static void main(String[] args) {
		try {
			example();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
