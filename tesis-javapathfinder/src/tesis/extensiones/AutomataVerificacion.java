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
package tesis.extensiones;

import java.util.HashSet;
import java.util.Iterator;

/**
 * Clase generica que implementa la logica de los AFD verificacion 
 * (toma los datos de un XML)
 * 
 */
public class AutomataVerificacion {
	private HashSet<Transicion> setTransiciones;
	private HashSet<Integer> setEstadosFinales;  
	protected int estadoActual;
	protected boolean blnEstadoFinal = false;
	
	public AutomataVerificacion (XMLAFDReader xmlafd) {
		estadoActual = xmlafd.estadoInicial();
		setTransiciones = xmlafd.transiciones();
		setEstadosFinales = xmlafd.estadosFinales();
	}
	
	public final void irAEstado(int est){
		System.out.println("AFD BACKTRACK al estado " + est);
		estadoActual = est;
	}

	public final boolean estadoFinal() {
		return setEstadosFinales.contains(new Integer(estadoActual));
	}

	public void consumir (Evento e) {
		Transicion tran;
		Iterator<Transicion> it;
		boolean transicionValida = false;
		
		it = setTransiciones.iterator();
		
		//System.out.println("EVENTO: " + e.label());
		while (it.hasNext() && !transicionValida) {
			tran = it.next();
			if (tran.estadoDesde() == estadoActual && tran.evento().equals(e)) {
				estadoActual = tran.estadoHacia();
				transicionValida = true;
				//DEBUG
				System.out.println("AFD avanza: " + tran.estadoDesde() + " -> " + tran.estadoHacia());
			}
		}
	}

	public final int getEstadoActual() {
		return estadoActual;
	}
}
