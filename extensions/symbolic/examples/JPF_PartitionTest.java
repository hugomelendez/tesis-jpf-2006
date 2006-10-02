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
import gov.nasa.jpf.jvm.MJIEnv;
import java.util.*;

public class JPF_PartitionTest {

	public static HashSet visited = new HashSet();

	public static boolean predsVisited(MJIEnv env, int robj, 
																		 int loc,
										   boolean p, boolean q, boolean r, boolean s) {
		Vector v = new Vector();
		v.add (new Integer(loc));
		v.add (new Boolean(p));
		v.add (new Boolean(q));
		v.add (new Boolean(r));
		v.add (new Boolean(s));
		return (visited.contains(v));
	}

	public static void addPredsVisited(MJIEnv env, int robj, 
																		 int loc,
										   boolean p, boolean q, boolean r, boolean s) {
		Vector v = new Vector();
		v.add (new Integer(loc));
		v.add (new Boolean(p));
		v.add (new Boolean(q));
		v.add (new Boolean(r));
		v.add (new Boolean(s));
		visited.add(v);	                                       		  
	}


}

