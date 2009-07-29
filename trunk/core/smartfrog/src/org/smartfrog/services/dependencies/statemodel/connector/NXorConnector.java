/** (C) Copyright 1998-2009 Hewlett-Packard Development Company, LP

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

package org.smartfrog.services.dependencies.statemodel.connector;

import java.rmi.RemoteException;
import java.util.Iterator;

import org.smartfrog.services.dependencies.statemodel.dependency.DependencyValidation;

public class NXorConnector extends Connector {
	   public NXorConnector() throws RemoteException {
		   super();
	   }

	   public boolean isEnabled() {
			  boolean any_enabled = false;
		      for (Iterator d = dependencies.iterator(); d.hasNext();) {
		    	   boolean enabled = ((DependencyValidation) d.next()).isEnabled();
		    	   if (any_enabled && enabled) return false;
		    	   if (enabled) any_enabled=true;
		      }
		      return any_enabled;
	   }
}
