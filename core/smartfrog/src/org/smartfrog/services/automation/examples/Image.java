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

public class Image extends State implements Prim {
	
	String name = "";
	String image = "";
	String status = "deleted";
	int threadDelay = 5000;
	
	Thread deleteThread = null; 
	Thread createThread = null;
	
	Object threadlock = new Object();
	
	public Image() throws RemoteException {super();}  

	public synchronized void sfDeploy() throws RemoteException, SmartFrogException {
		super.sfDeploy();
		name = sfCompleteNameSafe().toString();
		image = (String) sfResolve("image", image, false);
		status = (String) sfResolve("status", status, false);
		threadDelay = ((Integer) sfResolve("threadDelay", threadDelay, false)).intValue();
	}
	
	public void sfTerminateWith (TerminationRecord tr) {
		if (deleteThread != null) {
			deleteThread.interrupt();
		}
		if (createThread != null) {
			createThread.interrupt();
		}
		super.sfTerminateWith(tr);
	}
	
	public void setState(HashMap data){
		String desiredStatus = (String) data.get("desiredStatus");
		String desiredImage = (String) data.get("desiredImage");
		String status = (String) data.get("status");
		
		if ( status.equals("deleted") && desiredStatus.equals("created") && !desiredImage.equals("")) {
			 create(desiredImage);
			 return;
		}
	
		if ((!status.equals("deleted")) && (!status.equals("deleting")) && desiredStatus.equals("deleted")) {
			 delete(); 
			 return;
		}
		
		//System.out.println(name + ": no changes");
		return; //no changes
	}

	
	private void delete() {
        HashMap save = new HashMap();
		System.out.println(name + ": deleting image");
		if (createThread != null) {
			createThread.interrupt();
			createThread = null;
		}
		deleteThread = new Thread() {
			public void run() {
				HashMap save = new HashMap();
		        try {Thread.sleep(threadDelay);} catch (Exception e) { return; }
		        
				synchronized (threadlock) {
			        System.out.println(name + ": image deleted");
			        save.put("status", "deleted");
				    save.put("baseImage", "");
			        save.put("image", "");
			        saveState(save);
			        
			        deleteThread = null;
				}
			}
		};
		deleteThread.start();
		
		save.put("status", "deleting");
		saveState(save);		
	}
	
	
	private void create(String baseImage) {
        HashMap save = new HashMap();
        String imagename = baseImage + "." + imageId();
		System.out.println(name + ": creating image " + imagename);
		if (deleteThread != null) {
			deleteThread.interrupt();
			deleteThread = null; 
		}

		createThread = new Thread() {		
			public void run() {
				HashMap save = new HashMap();
			    try {Thread.sleep(threadDelay);} catch (Exception e) { return; }
			    
				synchronized (threadlock) {
					System.out.println(name + ": created image " + image);
				    save.put("status", "created");
					saveState(save);
			        
			        createThread = null;
				}
			}        
		};
		createThread.start();
		
	    save.put("image", imagename);
	    save.put("baseImage", baseImage);
	    save.put("status", "creating");
		saveState(save);
	}
	
	Random rn = new Random();
	private int imageId() {
		return rn.nextInt(100000000);
	}
}
