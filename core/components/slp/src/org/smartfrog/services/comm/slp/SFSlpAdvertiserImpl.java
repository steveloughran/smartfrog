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

import java.util.*;
import java.net.InetAddress;
import java.rmi.*;

/**
    Implements an SLP advertiser component for SmartFrog.
    The component can be used to advertise any service.
    The service type, attributes and URL are set in the SF description.
*/
public class SFSlpAdvertiserImpl extends PrimImpl implements Prim {
    protected Advertiser advertiser;
    protected Properties properties;
    protected ServiceURL serviceURL;
    protected Vector serviceAttributes;
    protected String serviceType;
    protected String serviceLocation;
    protected int serviceLifetime;
    
    public SFSlpAdvertiserImpl() throws RemoteException {
        super();
    }
    
    /**
        Creates the SLP advertiser object.
    */
    public synchronized void sfDeploy() throws SmartFrogException, RemoteException {
        super.sfDeploy();  
        properties = new Properties();
        // get slp configuration
        
        String s = (String)sfResolve("slp_config_interface", "", false);
        if(!s.equals("")) properties.setProperty("net.slp.interface", s);
        properties.setProperty("net.slp.multicastMaximumWait", sfResolve("slp_config_mc_max").toString());
        properties.setProperty("net.slp.randomWaitBound", sfResolve("slp_config_rnd_wait").toString());
        properties.setProperty("net.slp.initialTimeout", sfResolve("slp_config_retry").toString());
        properties.setProperty("net.slp.unicastMaximumWait", sfResolve("slp_config_retry_max").toString());
        properties.setProperty("net.slp.DAActiveDiscoveryInterval", sfResolve("slp_config_da_find").toString());
        properties.setProperty("net.slp.DAAddresses", sfResolve("slp_config_daAddresses").toString());
        properties.setProperty("net.slp.useScopes", sfResolve("slp_config_scope_list").toString());
        properties.setProperty("net.slp.mtu", sfResolve("slp_config_mtu").toString());
        properties.setProperty("net.slp.port", sfResolve("slp_config_port").toString());
        properties.setProperty("net.slp.locale", sfResolve("slp_config_locale").toString());
        properties.setProperty("net.slp.multicastAddress", sfResolve("slp_config_mc_addr").toString());
        properties.setProperty("net.slp.debug", sfResolve("slp_config_debug").toString());
        properties.setProperty("net.slp.logErrors", sfResolve("slp_config_log_errors").toString());
        properties.setProperty("net.slp.logMsg", sfResolve("slp_config_log_msg").toString());
        properties.setProperty("net.slp.logfile", sfResolve("slp_config_logfile").toString());
        
        // get parameters for service advertisement.
        
        serviceType = sfResolve("serviceType").toString();
        if(serviceType.endsWith(":")) serviceType = serviceType.substring(0, serviceType.length()-1);
        serviceLocation = sfResolve("serviceLocation").toString();
        serviceLifetime = ((Integer)sfResolve("serviceLifetime")).intValue();
        serviceAttributes = new Vector();
        Vector attrs = (Vector)sfResolve("serviceAttributes");
        Iterator iter = attrs.iterator();
        while(iter.hasNext()) {
            Vector a = (Vector)iter.next();
            serviceAttributes.add(new ServiceLocationAttribute((String)a.elementAt(0), (Vector)a.elementAt(1)));
        }
        
        // get advertiser
        ServiceLocationManager.setProperties(properties);
        try {
            advertiser = ServiceLocationManager.getAdvertiser(new Locale(properties.getProperty("net.slp.locale")));
        }catch(Exception ex) {
            throw (SmartFrogException) SmartFrogException.forward(ex);
        }
    }    
    
    /**
        Registers the service with the advertiser, starting the advertisement
    */
    public synchronized void sfStart() throws SmartFrogException, RemoteException {
        super.sfStart();
        try {
            // create url from type + location
            if(serviceLocation.equals("")) serviceLocation = InetAddress.getLocalHost().getHostAddress();
            serviceURL = new ServiceURL(serviceType + "://" + serviceLocation, serviceLifetime);
            advertiser.register(serviceURL, serviceAttributes);
        }catch(Exception ex) {
            throw (SmartFrogException) SmartFrogException.forward(ex);
        }
    }
    
    /**
        Deregisters the service.
        After this, the service is no longer advertised.
    */
    public synchronized void sfTerminateWith(TerminationRecord tr) {
        // deregister the component.
        try {
            advertiser.deregister(serviceURL);
        }catch(Exception ex) {
            ex.printStackTrace();
        }
        
	super.sfTerminateWith(tr);
    }
}

