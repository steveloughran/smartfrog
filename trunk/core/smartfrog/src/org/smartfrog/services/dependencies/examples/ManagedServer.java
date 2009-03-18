/** (C) Copyright 1998-2004 Hewlett-Packard Development Company, LP

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
package org.smartfrog.services.dependencies.examples;

import java.rmi.RemoteException;
import java.util.HashMap;

import org.smartfrog.services.dependencies.statemodel.state.StateComponent;
import org.smartfrog.sfcore.componentdescription.ComponentDescription;
import org.smartfrog.sfcore.prim.Prim;

public class ManagedServer extends StateComponent implements Prim {
			
	public ManagedServer() throws RemoteException {super();}  
	
	public boolean requireThread(){ return true; }  
	
	public boolean threadBody(){
		/*try {
			
			HashMap<String, Object> sthm = getLocalState();
			
			HashMap<String, ComponentDescription> hm = getPossibleTransitions();
		    if (hm.get("tstart")!=null){ 
		    	//System.out.println("+++Component Transitioning:"+name+" starting");
		    	go("tstart");
		    } else if (hm.get("tstop")!=null){ 
		    	//System.out.println("+++Component Transitioning:"+name+" stopping");
		    	go("tstop");
		    } else return true;
		    
		} catch (Exception e){}*/
		return true;
	}
}