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

package org.smartfrog.services.comm.slp;

import org.smartfrog.sfcore.prim.*;
import org.smartfrog.sfcore.reference.*;
import org.smartfrog.sfcore.common.*;

import java.rmi.RemoteException;

/**
Advertises any serializable java object.
 The object is made part of the URL, and a copy can be created from the URL
 when a service reply is received.
 */
public class SFSlpObjectAdvertiserImpl extends SFSlpAdvertiserImpl implements Prim, SFSlpObjectAdvertiser {
    protected Object toAdvertise = null;
    
    public SFSlpObjectAdvertiserImpl() throws RemoteException {
        
    }
    
    public synchronized void sfDeploy() throws SmartFrogException, RemoteException {
        // try {
        super.sfDeploy();  
        // get the object to advertise
        toAdvertise = sfResolve("toAdvertise");
        
        // create URL path (string representation of object)
        serviceLocation = "/" + ServiceURL.objectToString(toAdvertise);
    }  
}
