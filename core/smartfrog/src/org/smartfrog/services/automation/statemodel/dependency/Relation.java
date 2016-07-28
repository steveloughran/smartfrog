/** (C) Copyright 1998-2004 Hewlett-Packard Development Company, LP

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
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

package org.smartfrog.services.automation.statemodel.dependency;

import org.smartfrog.SFSystem;
import org.smartfrog.services.automation.statemodel.state.StateChangeNotification;
import org.smartfrog.services.automation.statemodel.state.StateDependencies;
import org.smartfrog.sfcore.prim.PrimImpl;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.prim.TerminationRecord;
import org.smartfrog.sfcore.reference.Reference;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.common.SmartFrogResolutionException;
import org.smartfrog.sfcore.common.Context;

import java.rmi.RemoteException;


/**
 * Class that implements the dependency between two StateDependency-implementing objects.
 * <p/>
 * On creation will add the dependency, or termination will remove it.
 */
public class Relation extends PrimImpl implements Prim, RelationValidation, StateChangeNotification {
   StateDependencies by = null;
   Prim on = null;
   
   boolean relevant;
   boolean enabled;
   boolean isEnabled; //the result...

   public Relation() throws RemoteException {
   }

   public synchronized void sfDeploy() throws SmartFrogException, RemoteException {
      super.sfDeploy();
      by = (StateDependencies) sfResolve("by", true);
      on = (Prim) sfResolve("on", true);
   }

   public synchronized void sfStart() throws SmartFrogException, RemoteException {
      super.sfStart();
      by.register(this);
      
      try {
    	  SFSystem.sfLog().debug("isEnabled " + sfCompleteNameSafe() + " " + isEnabled()); 
      } catch (Throwable e) {
    	  SFSystem.sfLog().debug("error evaluating isEnabled " + e);
    	  e.printStackTrace();
      }
   }

   public synchronized void sfTerminateWith(TerminationRecord tr) {
      try {
         by.deregister(this);
      } catch (Exception e) {
      }
      super.sfTerminateWith(tr);
   }

   public boolean isEnabled() {
      try {
         relevant = sfResolve("relevant", true, false);
         enabled = sfResolve("enabled", true, false);
         isEnabled = (!relevant) || (enabled && (!(on instanceof RelationValidation) || ((RelationValidation) on).isEnabled()));
      } catch (Throwable e) {
         // ?? what to do ??
    	 SFSystem.sfLog().err("error evaluating isEnabled on " + sfCompleteNameSafe() + " with exception " + e);
    	 e.printStackTrace();
         isEnabled = false;
      }
      //System.out.println(">>>" + sfCompleteNameSafe() + " " + isEnabled + " " + relevant + " " + enabled);
      return isEnabled;
   }

   public void handleStateChange() {
      //maybe cache and clear cache, or something....
   }

   public void notifyStateChange() {
      //@TODO deal with this better or split StateChangeNotification interface
      //not needed on dependency
   }
}
