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
package org.smartfrog.services.automation.examples;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import org.smartfrog.services.automation.statemodel.state.State;
import org.smartfrog.services.automation.threadpool.ThreadPool;
import org.smartfrog.sfcore.common.Context;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.prim.TerminationRecord;

public class VM extends State implements Prim {
	
	String name = "";
	String status = "deleted";
	int threadDelay = 5000;
	int failureFrequency = 0;
	int checkFrequency = 1;
	
	Random rn = new Random();
	
	Thread failureThread = null;
	Thread deleteThread = null; 
	Thread createThread = null;
	Object threadlock = new Object();
	
	public VM() throws RemoteException {super();}  

	public synchronized void sfDeploy() throws RemoteException, SmartFrogException {
		super.sfDeploy();
		name = sfCompleteNameSafe().toString();
		status = (String) sfResolve("status", status, false);
		threadDelay = ((Integer) sfResolve("threadDelay", threadDelay, false)).intValue();		
		failureFrequency = ((Integer) sfResolve("failureFrequency", failureFrequency, false)).intValue();
		checkFrequency = ((Integer) sfResolve("checkFrequency", checkFrequency, false)).intValue();
	}
	
	public void sfTerminateWith (TerminationRecord tr) {
		stopThreads();
		super.sfTerminateWith(tr);
	}
		
	public void setState(HashMap data){
		//System.out.println(name + " " + data);
		String desiredStatus = (String) data.get("desiredStatus");
		String status = (String) data.get("status");

		if (status.equals("failed") && desiredStatus.equals("deleted")) {
			reset();
			return;
		}
		
		if (status.equals("deleted") && desiredStatus.equals("created")) {
			create();
			return;
		}
		
		if ((!status.equals("deleted")) && desiredStatus.equals("deleted")) {
			delete();
			return;
		}

		return; //no changes

	}

	
	private void delete() {
		stopThreads();
        HashMap save = new HashMap();
		System.out.println(name + ": deleting vm");

		deleteThread = new Thread() {
			public void run() {
					HashMap save = new HashMap();
					
					try {Thread.sleep(threadDelay);} catch (Exception e) { return; }
					synchronized(threadlock) {
						try {
							status = (String) sfResolve("status", status, false);
							if (status.equals("failed")) return;
						} catch (Exception e) {}
						
						System.out.println(name + ": deleted vm");
						save = new HashMap();
						save.put("status", "deleted");
						saveState(save);		        
				        deleteThread = null;
					}
			}
		};
		deleteThread.start();

		save.put("status", "deleting");
		saveState(save);	

	}
	
	
	private void create() {
		stopThreads();
	    HashMap save = new HashMap();
	    System.out.println(name + ": creating vm");
	    
	    String image = "";
	    try { image = (String) sfResolve("image", "", false); } catch (Exception e) {}
	    if (image.equals("")) {
	    	System.out.println(name + ": failed to create VM as image is non-existent");
	    	save.put("status", "failed");
	    	saveState(save);
	    	return;
	    }

		createThread = new Thread() {		
			public void run() {
			    startFailureThread();
			    try {Thread.sleep(threadDelay);} catch (Exception e) { return;}
			    synchronized(threadlock) {
					try {
						status = (String) sfResolve("status", status, false);
					} catch (Exception e) {}
					if (status.equals("creating")) {
						HashMap save = new HashMap();
					    System.out.println(name + ": created vm");
					    save.put("status", "created");
					    saveState(save);		        
					}
			        createThread = null;
			    }
			}        
		};
		createThread.start();
				
		status = "creating";
		save.put("status", status);
		saveState(save);
	}
	
	private void reset() {
		HashMap save = new HashMap();
	    System.out.println(name + " reset");
		save.put("status", "deleted");
	    saveState(save);
	}

	
	private void startFailureThread() {
		if (failureThread != null) {
			failureThread.interrupt();
			failureThread = null;
		};
		//System.out.println("starting failure thread " + checkFrequency + " " + failureFrequency);
		if ((failureFrequency > 0) && (checkFrequency > 0)) {
			failureThread = new Thread() {
				public void run() {
					boolean failed = false;
					while (!failed) {
						try {Thread.sleep(1000 * checkFrequency);} catch (Exception e) { failureThread = null; return; }
						int r = (rn.nextInt(99) + 1);
						//System.out.println(name + " failure check " + r + " " + failureFrequency);
						if ( r <= failureFrequency) failed = true;
					}
					synchronized (threadlock) {
						failureThread = null;
						if (!status.equals("deleted")) {
							HashMap save = new HashMap();
							System.out.println(name + " has failed");
							save.put("status", "failed");
							saveState(save);
						}
					}
				}
			};
			failureThread.start();
		}		
	}
	
	private void stopThreads() {
		if (failureThread != null) {
			failureThread.interrupt();
			failureThread = null;
		}
		if (deleteThread != null) {
			deleteThread.interrupt();
			deleteThread = null;
		}
		if (createThread != null) {
			createThread.interrupt();
			createThread = null;
		}
	}
}

