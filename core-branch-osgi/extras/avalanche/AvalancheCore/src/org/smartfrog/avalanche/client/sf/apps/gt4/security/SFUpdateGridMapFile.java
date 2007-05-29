/**
(C) Copyright 1998-2007 Hewlett-Packard Development Company, LP

This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

For more information: www.smartfrog.org
*/
/*
 * Created on Feb 3, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.smartfrog.avalanche.client.sf.apps.gt4.security;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Properties;

import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.prim.PrimImpl;

import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.prim.TerminationRecord;

import org.smartfrog.avalanche.client.sf.apps.utils.FileUtils;
import org.smartfrog.avalanche.shared.CAService;

/**
 * @author sandya
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SFUpdateGridMapFile extends PrimImpl implements Prim {
	private String SHDTERMINATE = "shouldTerminate";
	
	private boolean shouldTerminate;
	
	private String globusLocation, certFile, mapUser, certSubject ; 
	GridSecurity gridSecurity ; 

	/**
	 * @throws java.rmi.RemoteException
	 */
	public SFUpdateGridMapFile() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	public synchronized void sfDeploy() throws SmartFrogException, RemoteException {
		super.sfDeploy();
		
		// optional attribute
		shouldTerminate = sfResolve(SHDTERMINATE, true, false);
		
		//System.out.println("Resolving ....."+ sfResolve("caServerLocator"));
		globusLocation = (String)sfResolve("globusLoc", globusLocation, true);
		certFile = (String)sfResolve("certFile", certFile, true);
		gridSecurity = new GridSecurity(globusLocation);
		mapUser = (String)sfResolve("mapUser", mapUser, true);
		certSubject = (String)sfResolve("certSubject", certSubject, true);
	}
	
	public synchronized void sfStart() throws SmartFrogException, RemoteException {
		super.sfStart();		
		
		try {			
			// update grid-mapfile
			// needs root access
			if (certSubject.length() != 0) {
				gridSecurity.updateGridMapfile(certSubject, mapUser);
			}
			else {
				String subject = gridSecurity.getUserSubject(certFile);
				gridSecurity.updateGridMapfile(subject, mapUser);
			}
			sfLog().info("grid-mapfile updated");			
		} catch (GT4SecurityException gse) {
			sfLog().err("Error while setting up grid security", gse);			
			throw new SmartFrogException("Error while setting up grid security", gse);			
		}
		
		sfLog().info("grid-mapfile updated successfully");
		
		// terminate synchronously
		if (shouldTerminate) {
			TerminationRecord tr = new TerminationRecord("normal", "Terminating ...", sfCompleteName());
			sfTerminate(tr);
		}
	}	

	public synchronized void sfTerminateWith(TerminationRecord status) {
		super.sfTerminateWith(status);
	}
	

}
