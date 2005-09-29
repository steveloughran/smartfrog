
/** (C) Copyright 1998-2005 Hewlett-Packard Development Company, LP

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

For more information: www.smartfrog.org

*/

package org.smartfrog.services.persistence.storage;

import java.net.*;
import java.rmi.*;

import org.smartfrog.services.persistence.recoverablecomponent.*;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.compound.*;
import org.smartfrog.sfcore.prim.TerminationRecord;


public class StorageAgentImpl extends CompoundImpl implements StorageAgent, Compound {

	public StorageAgentImpl() throws RemoteException {
		super();
		return;
	}
	
	static public String getUrl() {
		try{
			return InetAddress.getLocalHost().getHostAddress();
		}catch (java.net.UnknownHostException exc){
			throw new RuntimeException("Could not find the local IP address.",exc);
		}
	}

	public synchronized void sfDeploy() throws RemoteException, SmartFrogException{
		super.sfDeploy();
	}

	public synchronized void sfStart() throws RemoteException, SmartFrogException{
		System.out.println("Storage Agent started here");
		super.sfStart();
	}
	
	
	public synchronized Object getComponentStub(StorageRef storef) throws RemoteException, StorageException {
		System.out.println("StorageAgent being queried for "+storef);
		StoragePollee stopoll = storef.getStoragePollee();
		Object returnobject = stopoll.pollEntry(RComponent.DBStubEntry);
		stopoll.close();
		System.out.println("Returning current stub.");
		return returnobject;
	}
	
	public synchronized boolean isDead(StorageRef storef) throws RemoteException, StorageException{
		System.out.println("StorageAgent being queried for "+storef);
		StoragePollee stopoll = storef.getStoragePollee();
		Object returnobject = stopoll.pollEntry(RComponent.WFSTATUSENTRY);
		stopoll.close();
		System.out.println("Returning component's status.");
		return returnobject.equals(RComponent.WFSTATUS_DEAD);
	}
	
	public synchronized void sfTerminateQuietlyWith(TerminationRecord status) {
		super.sfTerminateQuietlyWith(status);
	}

	public synchronized void sfTerminateWith(TerminationRecord status) {
		super.sfTerminateWith(status);
	}
	
}
