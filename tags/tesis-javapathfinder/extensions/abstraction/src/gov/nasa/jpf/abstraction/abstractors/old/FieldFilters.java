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
package gov.nasa.jpf.abstraction.abstractors.old;

import gov.nasa.jpf.jvm.FieldInfo;

import java.util.List;
import java.util.Set;

public class FieldFilters {
  private FieldFilters() {}
  
  public static interface IDualFilter
  extends IStaticFieldFilter, IInstanceFieldFilter {}
  
  public static final IDualFilter allowAll = new IDualFilter() {
    public boolean staticFieldRelevant(FieldInfo field) {
      return true;
    }
    public boolean instanceFieldRelevant(FieldInfo field) {
      return true;
    }
  };

  public static final IDualFilter allowNone = new IDualFilter() {
    public boolean staticFieldRelevant(FieldInfo field) {
      return false;
    }
    public boolean instanceFieldRelevant(FieldInfo field) {
      return false;
    }
  };
  
  public static final IDualFilter filterPrims = new IDualFilter() {
    public boolean staticFieldRelevant(FieldInfo field) {
      return field.isReference();
    }
    public boolean instanceFieldRelevant(FieldInfo field) {
      return field.isReference();
    }
  };
  
  public static IDualFilter allowWithNames(final Set<String> allowed) {
    return new IDualFilter() {
      public boolean staticFieldRelevant(FieldInfo field) {
        return allowed.contains(field.getName());
      }
      public boolean instanceFieldRelevant(FieldInfo field) {
        return allowed.contains(field.getName());
      }
    };
  }
  
  public static IDualFilter filterWithNames(final Set<String> filtered) {
    return new IDualFilter() {
      public boolean staticFieldRelevant(FieldInfo field) {
        return filtered.contains(field.getName());
      }
      public boolean instanceFieldRelevant(FieldInfo field) {
        return filtered.contains(field.getName());
      }
    };
  }
  
  public static IDualFilter allowWithFullNames(final Set<String> allowed) {
    return new IDualFilter() {
      public boolean staticFieldRelevant(FieldInfo field) {
        return allowed.contains(field.getFullName());
      }
      public boolean instanceFieldRelevant(FieldInfo field) {
        return allowed.contains(field.getFullName());
      }
    };
  }
  
  public static IDualFilter filterWithFullNames(final Set<String> filtered) {
    return new IDualFilter() {
      public boolean staticFieldRelevant(FieldInfo field) {
        return filtered.contains(field.getFullName());
      }
      public boolean instanceFieldRelevant(FieldInfo field) {
        return filtered.contains(field.getFullName());
      }
    };
  }
  
  
  public static IDualFilter allowIfAllAllow(final List<Object> filters) {
    return new IDualFilter() {
      public boolean staticFieldRelevant(FieldInfo field) {
        for (Object f : filters) {
          if (f instanceof IStaticFieldFilter &&
              !((IStaticFieldFilter) f).staticFieldRelevant(field)) {
            return false;
          }
        }
        return true;
      }
      public boolean instanceFieldRelevant(FieldInfo field) {
        for (Object f : filters) {
          if (f instanceof IInstanceFieldFilter &&
              !((IInstanceFieldFilter) f).instanceFieldRelevant(field)) {
            return false;
          }
        }
        return true;
      }
    };
  }
  
  public static IDualFilter allowIfAnyAllow(final Iterable<Object> filters) {
    return new IDualFilter() {
      public boolean staticFieldRelevant(FieldInfo field) {
        for (Object f : filters) {
          if (f instanceof IStaticFieldFilter &&
              ((IStaticFieldFilter) f).staticFieldRelevant(field)) {
            return true;
          }
        }
        return false;
      }
      public boolean instanceFieldRelevant(FieldInfo field) {
        for (Object f : filters) {
          if (f instanceof IInstanceFieldFilter &&
              ((IInstanceFieldFilter) f).instanceFieldRelevant(field)) {
            return true;
          }
        }
        return false;
      }
    };
  }
  
  
}
