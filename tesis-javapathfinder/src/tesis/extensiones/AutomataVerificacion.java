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
	private HashSet<Transicion> transiciones;
	private HashSet<Integer> estadosFinales;
	protected int estadoActual;
	protected int estadoAnterior;
	private String type;
	//protected boolean blnEstadoFinal = false;
	
	public AutomataVerificacion () {
	}

	public AutomataVerificacion (XMLAFDReader xmlafd) throws XMLException {
		estadoActual = xmlafd.estadoInicial();
		transiciones = xmlafd.transiciones();
		estadosFinales = xmlafd.estadosFinales();
		type = "GlobalProperty";
	}
	
	/**
	 * Constructor
	 * Crea el AFD basandose en la informacion obtenida de un TypeStatePropertyTemplate
	 * @param tpl
	 */
	public AutomataVerificacion(TypeStatePropertyTemplate tpl) {
		estadoAnterior = estadoActual = tpl.estadoInicial();
		transiciones = tpl.transiciones();
		estadosFinales = tpl.estadosFinales();
		type = "TypeStatePropery (" + tpl.getType() + ")";
	}

	public void irAEstado(int est){
		System.out.println("AFD BACKTRACK al estado " + est);
		estadoAnterior = estadoActual;
		estadoActual = est;
	}

	public boolean estadoFinal() {
		return estadosFinales.contains(new Integer(estadoActual));
	}

	public void consumir (Evento e) {
		Transicion tran;
		Iterator<Transicion> it;
		boolean transicionValida = false;
		
		it = transiciones.iterator();
		
		//System.out.println("EVENTO: " + e.label());
		while (it.hasNext() && !transicionValida) {
			tran = it.next();
			if (tran.estadoDesde() == estadoActual && tran.evento().equals(e)) {
				estadoAnterior = estadoActual;
				estadoActual = tran.estadoHacia();
				transicionValida = true;
			}
		}
	}

	public int getEstadoActual() {
		return estadoActual;
	}

	public int getEstadoAnterior() {
		return estadoAnterior;
	}

	public String getType() {
		return type;
	}
}
