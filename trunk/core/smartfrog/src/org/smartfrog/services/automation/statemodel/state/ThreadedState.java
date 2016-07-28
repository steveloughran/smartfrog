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

import org.smartfrog.services.automation.threadpool.ThreadPool;
import org.smartfrog.services.automation.statemodel.dependency.RelationValidation;

import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.common.SmartFrogException;

import java.util.HashMap;
import java.rmi.RemoteException;

/**
 */
public abstract class ThreadedState extends State implements Prim, StateDependencies, StateSetting,
      StateSaving, StateChangeNotification, RelationValidation {

   protected ThreadPool threadpool;
   protected final Object threadpoolLock = new Object();

   // a thread is in the pool, and may still be removable or in flight
   protected boolean actionInProgress = false;

   // there is an async response that is still outstanding
   boolean asyncResponse = false;

   Object currentAction = null;
   
   protected HashMap continuation_data = null;


   public ThreadedState() throws RemoteException {
   }

   public synchronized void sfDeploy() throws RemoteException, SmartFrogException {
      super.sfDeploy();
      threadpool = (ThreadPool) sfResolve("threadpool", false);
   }

   public void setState(HashMap data) {
      synchronized (threadpoolLock) {
         if (threadpool.removeFromQueue(currentAction)) {
            actionInProgress = false;
             //this is run as part of the notification thread, so we do not need to worry about accidental queiescence
             //when temporarily removing the thread which may be put back on a few lines later!
            try { parentLocking.threadStopped(); } catch (RemoteException e) {}
         } //either already running, or none on the queue...
         if (requireThread(data)) {
            if (actionInProgress) {//ie there was one and it was already in flight, so not removed
               continuation_data = data;
            } else { // there was none, or was removed
               actionInProgress = true;
               try { parentLocking.threadStarted(); } catch (RemoteException e) {}
               currentAction = threadpool.addToQueue(new StateUpdateThread(data));
            }
         } else {
            // do nothing
         }
      }
   }

   public void asyncResponseComplete(HashMap data) {
      HashMap state = null;
      try {
         synchronized (threadpoolLock) {

            if (sfLog().isTraceEnabled()) sfLog().trace("async response with " + data);
            if (!asyncResponse) {
               if (sfLog().isErrorEnabled()) sfLog().error("async response arrived unexpectedly, ignored...");
            } else {
               state = data;

               if (continuation_data != null) {
                  if (sfLog().isTraceEnabled()) sfLog().trace("async response have continuation");
                  try {parentLocking.threadStarted();} catch (RemoteException e) {}
                  currentAction = threadpool.addToQueue(new StateUpdateThread(continuation_data));
                  continuation_data = null;
               } else {
                  if (sfLog().isTraceEnabled()) sfLog().trace("async response no continuation");
                  actionInProgress = false;
               }
               asyncResponse = false;
            }
         }
         if (sfLog().isTraceEnabled()) sfLog().trace("async response saving state");
         if (state != null) saveState(data);
         if (sfLog().isTraceEnabled()) sfLog().trace("async response saved state");
      } finally {
         try {parentLocking.threadStopped();} catch (RemoteException e) {}
      }
   }

   public abstract boolean requireThread(HashMap data);

   public abstract HashMap threadBody(HashMap data);


   /**
    * ********************************************************
    * thread definition
    * ********************************************************
    */
   public class StateUpdateThread implements Runnable {
      HashMap data;

      public StateUpdateThread(HashMap data) {
         this.data = data;
      }

      public void run() {
         try {
            HashMap state = null;
            try {
               state = threadBody(data);
            } finally {
               synchronized (threadpoolLock) {
                  if (state == null) {
                     asyncResponse = true;
                  } else {
                     asyncResponse = false;
                     if (continuation_data != null) {
                        try { parentLocking.threadStarted(); } catch (RemoteException e) {}
                        currentAction = threadpool.addToQueue(new StateUpdateThread(continuation_data));
                        continuation_data = null;
                     } else {
                        actionInProgress = false;
                     }
                  }
               }
               if (state != null) saveState(state);
            }
         } finally {
            if (!asyncResponse) try { parentLocking.threadStopped(); } catch (RemoteException e) {}
         }
      }
   }
}

