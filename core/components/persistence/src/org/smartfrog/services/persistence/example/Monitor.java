package org.smartfrog.services.persistence.example;

import java.io.*;
import java.rmi.*;
import java.rmi.server.*;

import org.smartfrog.services.persistence.recoverablecomponent.*;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.prim.PrimImpl;
import org.smartfrog.sfcore.common.Context;
import org.smartfrog.sfcore.common.SmartFrogDeploymentException;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.prim.TerminationRecord;

import java.awt.*;
import java.awt.event.*;


public class Monitor extends RComponentImpl implements RComponent {

	Frame frame = null;
	
	private class Killer implements ActionListener{
		Monitor myMonitor;
		String myName;
		
		public Killer (Monitor myMonitor,String myName){
			this.myMonitor = myMonitor;
			this.myName = myName;
		}
		
		public void actionPerformed(ActionEvent e){
		    try{
		    	RMIServerInterface server = (RMIServerInterface )sfResolve(myName); 
		    	server.killMe();
		    }catch(Exception exc){}
		}
	}
	
	
	public Monitor() throws RemoteException {
		super();
	}

    public void sfDeployWith(Prim parent, Context cxt) throws
    SmartFrogDeploymentException, RemoteException {

    	super.sfDeployWith(parent,cxt);
    }	

    private synchronized void execute () throws SmartFrogException, RemoteException{
		frame = new Frame("Processes to kill");

		// Add a listener for the close event
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
			    // Exit the application
			    System.exit(0);
			}
		});

		Panel panel = new Panel(new FlowLayout(FlowLayout.CENTER));
	    
		Button b = new Button("Jill"); 
		b.addActionListener( new Killer(this,"jill"));
		panel.add(b);
		b = new Button("John"); 
		b.addActionListener( new Killer(this,"john"));
		panel.add(b);
		b = new Button("Paterson"); 
		b.addActionListener( new Killer(this,"paterson"));
		panel.add(b);
		b = new Button("Marie"); 
		b.addActionListener( new Killer(this,"marie"));
		panel.add(b);

		frame.add(panel, BorderLayout.NORTH);

		frame.pack();
		frame.show();
    }
    
	public synchronized void sfStart() throws SmartFrogException, RemoteException {
		super.sfStart();
		execute();
	}

	public synchronized void sfRecover() throws SmartFrogException, RemoteException {
		super.sfRecover();
		execute();
	}
		
	public synchronized void sfDeploy() throws SmartFrogException, RemoteException {
		super.sfDeploy();
	}

	public synchronized void sfTerminateQuietlyWith(TerminationRecord status) {
		super.sfTerminateQuietlyWith(status);
	}

	public synchronized void sfTerminateWith(TerminationRecord status) {
		super.sfTerminateWith(status);
		frame.dispose();
	}
	
	
	
}
