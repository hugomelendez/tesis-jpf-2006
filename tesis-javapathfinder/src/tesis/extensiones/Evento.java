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

/**
 * Clase para instanciar objetos de tipo Evento
 * Lo �nico que interesa de un Evento es su nombre 
 */
public class Evento {
	private String type;
	private String keyword;
	private String label;
	
	public Evento () {
		this.label = "";
	}

	public Evento (String label) {
		this.label = label;
	}

	public Evento (String type, String keyword, String label) {
		this.type = type;
		this.keyword = keyword;
		this.label = label;
	}
	/**
	 * Determina si el Evento corresponde al nombre pasado como par�metro
	 * @return
	 */
	public boolean sos(String lbl) {
		return label.equals(lbl);
	}
	
	public boolean equals(Evento e) {
		return label.equals(e.label);
	}

	public boolean esObservable() {
		return (!label.equals(""));
	}

	public String keyword() {
		return keyword;
	}

	public String label() {
		return label;
	}

	public String type() {
		return type;
	}

	@Override
	public String toString() {
		return 
			"type => " + this.type +
			"keyword => " + this.keyword +
			"label => " + this.label;
	}
}
