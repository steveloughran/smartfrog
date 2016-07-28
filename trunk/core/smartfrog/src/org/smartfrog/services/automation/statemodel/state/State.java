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

package org.smartfrog.services.automation.statemodel.state;

import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import org.smartfrog.services.automation.statemodel.dependency.RelationValidation;
import org.smartfrog.services.automation.statemodel.exceptions.SmartFrogStateLifecycleException;
import org.smartfrog.services.automation.statemodel.utils.CDUtil;
import org.smartfrog.sfcore.common.Context;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.common.SmartFrogResolutionException;
import org.smartfrog.sfcore.common.SmartFrogRuntimeException;
import org.smartfrog.sfcore.componentdescription.ComponentDescription;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.prim.PrimImpl;
import org.smartfrog.sfcore.reference.Reference;
import org.smartfrog.sfcore.reference.ReferencePart;



/**
 */
public abstract class State extends PrimImpl implements Prim, StateDependencies, StateSetting,
      StateSaving, StateChangeNotification, RelationValidation {

   protected HashSet automation = new HashSet();

   protected boolean asAndConnector = false;
   protected Vector stateData = new Vector();
   protected Vector stateNotify = new Vector();
   protected Vector stateListen = new Vector();
   protected Vector stateInvariant = new Vector();

   protected boolean firstTime = true;
   protected HashMap currentState = new HashMap();
   protected HashMap currentListen = new HashMap();

   NotificationLock parentLocking;

   public State() throws RemoteException {
   }

   public synchronized void sfDeploy() throws RemoteException, SmartFrogException {
      super.sfDeploy();
      
      Context cxt = sfContext();
      Enumeration keys = cxt.keys(); 
      
      while (keys.hasMoreElements()){
    	  Object key = keys.nextElement();
    	  if (cxt.sfContainsTag(key, "stateData")){
    		  stateData.add(key);
    	  }
    	  if (cxt.sfContainsTag(key, "stateListen")){
    		  stateListen.add(key);
    	  }
    	  if (cxt.sfContainsTag(key, "stateNotify")){
    		  stateNotify.add(key);
    	  }
    	  if (cxt.sfContainsTag(key,  "stateInvariant")) {
    		  stateInvariant.add(key);
    	  }
      }
      
      asAndConnector = sfResolve("asAndConnector", asAndConnector, false);
      parentLocking = (NotificationLock)sfParent();
   }

   
   public synchronized Object sfReplaceAttribute(Object name, Object value)
           throws SmartFrogRuntimeException, RemoteException {
	   if (stateNotify.contains(name)) {
		   Object oldVal = sfContext.get(name);
		   HashMap m = new HashMap();
		   m.put(name,  value);
		   saveState(m);
		   return oldVal;
	   } else 
		   return super.sfReplaceAttribute(name, value);
   }
   

   public void saveState(HashMap save) {
	  //System.out.println("saving state");
	  boolean notificationNeeded = false;
      try {
         if (sfLog().isTraceEnabled()) sfLog().trace("model locking");
         try {
            parentLocking.lock();
         } catch (RemoteException e) {
            //??
         }
         if (sfLog().isTraceEnabled()) sfLog().trace("saving state " + save + " " + stateNotify);
         for (Iterator keys = save.keySet().iterator(); keys.hasNext();) {
            Object key = keys.next();
            Object value = save.get(key);
            if (stateNotify.contains(key)) {
            //System.out.println("notifying");
               notificationNeeded = true;
               if (sfLog().isTraceEnabled()) sfLog().trace("notification needed for " + key);
            } else {
               // System.out.println("not notifying");
               if (sfLog().isTraceEnabled()) sfLog().trace("notification not needed for " + key);
            }
            try {
               super.sfReplaceAttribute(key, value);
            } catch (Exception e) {
               if (sfLog().isErrorEnabled()) sfLog().error("error adding attributes to state - ignored", e);
            }
         }
      } finally {
         try {
            parentLocking.unlock(notificationNeeded);
         } catch (RemoteException e) {
            //??
         }
         if (sfLog().isTraceEnabled()) sfLog().trace("model unlocked");
      }
   }

   public void register(RelationValidation d) throws SmartFrogStateLifecycleException {
      automation.add(d);
   }

   public void deregister(RelationValidation d) throws SmartFrogStateLifecycleException {
      automation.remove(d);
      notifyStateChange();
   }

   public void notifyStateChange() {
      try {
         parentLocking.notifyStateChange();
      } catch (RemoteException e) {
         //
      }
   }

   public void handleStateChange() {
	  //System.out.println(sfCompleteNameSafe() + " handle state");
      if (sfLog().isDebugEnabled())
         sfLog().debug("handler " + sfCompleteNameSafe() + automation);

      for (Enumeration e = stateInvariant.elements(); e.hasMoreElements(); ) {
          try {
        	  Object key = e.nextElement();
			  Object value = sfResolve(new Reference(ReferencePart.here(key)));
			  if (!(value instanceof Boolean)) {
				  sfLog().error(sfCompleteNameSafe() + ": state invariant " + key + " is not a boolean");
			  } else if (!((Boolean) value)) {
				  sfLog().error(sfCompleteNameSafe() + ": state invariant failure with " + key );
			  }
		  } catch (SmartFrogResolutionException e1) {
			e1.printStackTrace();
		  } catch (RemoteException e1) {
			e1.printStackTrace();
		  }
      }
      
      boolean enabled = checkIsEnabled();
      //System.out.println("enabled..." + sfCompleteNameSafe() + " " + enabled);
      if (sfLog().isTraceEnabled()) sfLog().trace("enabled..." + sfCompleteNameSafe() + " " + enabled);
      if (!enabled) return;

      if (stateChanged() || firstTime) {
         //System.out.println("state changed " + sfCompleteNameSafe() + " " + currentState);
         if (sfLog().isTraceEnabled()) sfLog().trace("changed " + sfCompleteNameSafe());
         firstTime = false;
         setState((HashMap) currentState.clone());
         //System.out.println(sfCompleteNameSafe() + "state changed");
      } else {
    	 //System.out.println("state not changed " + sfCompleteNameSafe() + " " + currentState);
         if (sfLog().isTraceEnabled()) sfLog().trace("not changed " + sfCompleteNameSafe());
      }
   }

   // ready in case we need it for the automation to link through components!
   protected boolean checkIsEnabled() {  //and connector on the automation
      for (Iterator d = automation.iterator(); d.hasNext();) {
         try {
            if (!((RelationValidation) d.next()).isEnabled()) {
               return false;
            }
         } catch (RemoteException e) {
            //??
         }
      }
      return true;
   }

   public boolean isEnabled() {
      return (!asAndConnector) || checkIsEnabled();
   }

   protected boolean stateChanged() {
      boolean changes = false;
      HashMap newState = new HashMap();

      for (Enumeration e = stateListen.elements(); e.hasMoreElements();) {
         Object key = e.nextElement();
         Object newValue = null;
         try {
            newValue = sfResolve(new Reference(ReferencePart.here(key)));
         } catch (Exception e1) {
            if (sfLog().isErrorEnabled())
               sfLog().error(sfCompleteNameSafe() + "error checking attributes for changes", e1);
         }
         if (!currentListen.containsKey(key) || !newValue.equals(currentListen.get(key))) {//@todo: need to check
            currentListen.put(key, newValue);
            changes = true;
         }
      }

      for (Enumeration e = stateData.elements(); e.hasMoreElements();) {
         Object key = e.nextElement();
         Object newValue = null;
         try {
            newValue = sfResolve(new Reference(ReferencePart.here(key)));
            if (newValue instanceof ComponentDescription) newValue = CDUtil.copy((ComponentDescription)newValue);
            //@todo: the rest of the types being copied...?
         } catch (Exception e1) {
            if (sfLog().isErrorEnabled())
               sfLog().error(sfCompleteNameSafe() + "error checking attributes for changes", e1);
         }
         if (newValue != null) {
            newState.put(key, newValue);
         }
      }

      currentState = newState;
      //System.out.println(sfCompleteNameSafe() + " " + changes);
      return changes;
   }

   public abstract void setState(HashMap data);
}
