package org.smartfrog.services.persistence.example;
import java.rmi.RemoteException;


import java.net.*;
import java.io.*;

import org.smartfrog.services.persistence.recoverablecomponent.*;
import org.smartfrog.services.persistence.storage.Storage;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.common.Context;
import org.smartfrog.sfcore.common.SmartFrogDeploymentException;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.prim.TerminationRecord;

public class RMIServer extends RComponentImpl implements RComponent, RMIServerInterface{

	int initialvalue = 0;
	TimeCounter timer=null;
	Object mylock = new Object();
	
	
	private class localKiller extends Thread{
		public void run(){
			try{
				Thread.sleep(1000);
			} catch (Exception exc) {
				
			}
			System.exit(0);
		}
	}
	
	private class TimeCounter extends Thread{
		
		public void run (){

			RMIServerInterface neighbor = null;

			try{
				neighbor = (RMIServerInterface) sfResolve("neighbor");
			} catch(Exception exc){
				System.out.println("Error getting neighbor value."+exc);
				System.exit(0);
			}
			
			while (true){
				try{
					if(!hasSent()){
						sleep(5000);
						Send(neighbor);
					}else{
						sleep(400);
					}
				} catch (Exception exc){
					System.out.println("Error while verifying/sending token (will retry later)\n"+exc);
				}
			}
		}
	}

	public void killMe() throws RemoteException {
		localKiller t = new localKiller();
		t.start();
	}
	
	public void Send(RMIServerInterface neighbor) throws SmartFrogException,RemoteException {
		synchronized (mylock){
			neighbor.setToken(initialvalue+1);
			sfReplaceAttribute("sent",new Boolean(true));
		}
	}

	public boolean hasSent() throws SmartFrogException,RemoteException {
		synchronized (mylock){
		return ((Boolean) sfResolve("sent")).booleanValue();
		}
	}
	
	public void setToken(int token) throws RemoteException{
		synchronized (mylock){
		if (initialvalue < token){
			System.out.println("Received token "+token);
			initialvalue = token;
			try{
				sfReplaceAttribute("token",new Integer(token));
				sfReplaceAttribute("sent",new Boolean(false));
			} catch (Exception exc){}
		}
		}
	}
	
	public RMIServer() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	
	private void execute() throws SmartFrogException{
		timer = new TimeCounter();
		timer.start();
		try{
			setToken(((Integer)sfResolve("token")).intValue());
		}
		catch (Exception exc){
			throw new SmartFrogException("Impossible to resume execution.",exc);
		}
	}
	
    public void sfDeployWith(Prim parent, Context cxt) throws
	SmartFrogDeploymentException, RemoteException {
		super.sfDeployWith(parent,cxt);
    }
	
	public synchronized void sfDeploy() throws RemoteException, SmartFrogException{
		System.out.println("It is going to be deployed.");
		super.sfDeploy();
	}

	public synchronized void sfStart() throws RemoteException, SmartFrogException{
		try{
			RMIServerInterface neighbor = (RMIServerInterface) sfResolve("neighbor");
			sfReplaceAttribute("neighbor",neighbor);
		} catch(Exception exc){
			System.out.println("Error getting neighbor value for the first time."+exc);
			System.exit(0);
		}
		super.sfStart();
		execute();
	}

	public synchronized void sfRecover() throws SmartFrogException, RemoteException {
		super.sfRecover();
		execute();
	}
	
	
	public synchronized void sfTerminateQuietlyWith(TerminationRecord status) {
		System.out.println("Someone is terminating me quitely......"+status);
		super.sfTerminateQuietlyWith(status);
	}

	public synchronized void sfTerminateWith(TerminationRecord status) {
		System.out.println("Someone is terminating me......"+status);
		super.sfTerminateWith(status);
	}
	
}


