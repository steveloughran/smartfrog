/*
 Service Location Protocol - SmartFrog components.
 Copyright (C) 2004 Glenn Hisdal <ghisdal(a)c2i.net>
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
 This library was originally developed by Glenn Hisdal at the 
 European Organisation for Nuclear Research (CERN) in Spring 2004. 
 The work was part of a master thesis project for the Norwegian 
 University of Science and Technology (NTNU).
 
 For more information: http://home.c2i.net/ghisdal/slp.html 
 */

package org.smartfrog.services.comm.slp.util;

import org.smartfrog.services.comm.slp.ServiceLocationEnumeration;
import org.smartfrog.services.comm.slp.ServiceType;

import java.util.*;

public class ServiceTypeEnumeration implements ServiceLocationEnumeration {
    private Vector elements;
    private int currentElement;
    
    public ServiceTypeEnumeration() {
        elements = new Vector();
        currentElement = 0;
    }
    
    public ServiceTypeEnumeration(Vector v) {
        elements = v;
        currentElement = 0;
    }
    /**
        This method is currently the same as nextElement(). It does not block.
     If the next element does not exist, null is returned.
     */
    public Object next() {
        // The call to Locator.findServices is not asynchronous, so when
        // we get a ServiceURLEnumeration, the results are allready collected.
        // For that reason, there is no need to wait here...
        return nextElement();
    }
    
    public Object nextElement() {
        if(currentElement == elements.size()) return null;
        else return elements.elementAt(currentElement++);
    }
    
    public boolean hasMoreElements() {
        return (currentElement != elements.size());
    }
    /**
        Adds the given elements to the enumeration.
     @param v A vector with the elements to add
     */
    public void addElements(Vector v) {
        //elements.addAll(v);
        for(Iterator iter=v.iterator(); iter.hasNext(); ) {
            Object o = iter.next();
            if(!elements.contains(o)) {
                elements.add(o);
            }
        }
    }
    /*
        Adds an element to the enumeration.
     @param url The element to add
     */
    /*
    public void addElement(ServiceURL url) {
        if(!elements.contains(url)) {
            elements.add(url);
        }
    }
    */
    public boolean contains(Object o) {
        return elements.contains(o);
    }
}

