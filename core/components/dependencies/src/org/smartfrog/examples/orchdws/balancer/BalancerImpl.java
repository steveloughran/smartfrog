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

package org.smartfrog.examples.orchdws.balancer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.RemoteException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.smartfrog.examples.dynamicwebserver.gui.graphpanel.DataSource;
import org.smartfrog.services.dependencies.statemodel.state.InvokeAsynchronousStateChange;
import org.smartfrog.services.dependencies.statemodel.state.StateComponent;
import org.smartfrog.services.dependencies.statemodel.state.StateComponentTransitionException;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.prim.TerminationRecord;
import org.smartfrog.sfcore.reference.Reference;
import org.smartfrog.sfcore.utils.ListUtils;


/**
 * <p>
 * Title: BalancerImpl.
 * </p>
 *
 * <p>
 * Description: BalancerImpl is the main class for a software load balancer for
 * socket connections between a set of clients and a set of servers. The
 * clients connect to a specific port on the balancer host and the balancer
 * selects a server to handle the client session. The balancer opens a socket
 * connection to the selected server and relays data between the client and
 * the server. The balancer listens for client connections on a well-known
 * port, specified by the "port" SmartFrog parameter.
 * </p>
 *
 * <p>
 * The Balancer maintains a set of servers that are included in the selection
 * algorithm for balancing. The curent selection algorithm is round-robin. An
 * initial set of servers can be specified via the "hosts" SmartFrog
 * parameter; if "hosts" is specified, the parameter "hostsPort" is used to
 * specify the port number to connect to on the servers. Subsequent changes to
 * the set of servers can be made at run-time via the Balancer remote
 * interface; in this case the port number for each host can be specified
 * individually.
 * </p>
 *
 */
public class BalancerImpl extends StateComponent implements Balancer, DataSource {
    //
    // Other instance variables
    private ServerSelector serverSelector; // Object that does the load balancing to select a server from a list
    private volatile BalancerThread balancerThread = null; // The main Balancer thread
    private volatile boolean stopping = false; // Used to signal the Balancer server thread to terminate
    private int port; // port number to register balancer server socket
    private int hostsPort; // port number for connections to remote hosts
    private int connectionCount = 0;  // number of connections made since last time this was accessed through getdata()
    private Vector<String> serverHosts;  //the set of server hosts
    private int sleep = 0;
    
    /**
     * Constructor for the Balancer component
     *
     * @throws RemoteException in case of Remote/network error
     */
    public BalancerImpl() throws RemoteException {
    }
    
    
    //This need rechecking...
    protected boolean threadBody()  throws StateComponentTransitionException { 
    	if (asyncResponse) return true;  //timer still going...
    	
    	Timer timer = new Timer();
    	timer.schedule(new TimerTask(){
    		public void run(){
      	        BalancerImpl.this.sfLog().info("TIMER: In Timer, time to do something...");
                try {
                    invokeAsynchronousStateChange(new InvokeAsynchronousStateChange(){
                      public void actOn(StateComponent _lb) {
                          BalancerImpl.this.sfLog().info("actOn(...)");
                          try {
                              _lb.go(TIMER);
                          }catch (StateComponentTransitionException scte){/*Shouldn't happen*/}
                          BalancerImpl.this.sfLog().info("actOn(...): LEAVING!");
                    }
                    });
                } catch (StateComponentTransitionException e) {
                    sfLog().error(e);
                    throw new RuntimeException(e);  //propagate up...
                } catch (RemoteException e) {
                    sfLog().error(e);
                    throw new RuntimeException(e);
                }
                BalancerImpl.this.sfLog().info("TIMER: Now going to terminate timer instance...");
                cancel() ; //Terminate the timer...
      	    }
    	}, sleep);
        return false;
    }
	
    /** implementation of DataSource interface
     *  resets the data after every request - so only suited to single requestor
     *
     * @return the number of connections since last request
     * @throws RemoteException in case of Remote/network error
     */
    public int getData() throws RemoteException {
        int tmp = connectionCount;
        connectionCount = 0;
        return tmp;
    }


    /**
     * Start the load balancer.
     */
    private synchronized void start() {
        stopping = false;

        // Start the Server Selector
        serverSelector = new ServerSelector(sfCompleteNameSafe().toString());
        serverSelector.start();

        //
        //Create a thread to accept new connections
        if (balancerThread == null) {
            balancerThread = new BalancerThread();
            balancerThread.start();
        }
    }

    /**
     * Stop the load balancer.
     */
    public synchronized void stop() {
        stopping = true;

        //
        // Stop accepting new connections.
        if (balancerThread != null) {
            balancerThread.stop();
            balancerThread = null;
        }

        //
        // Close all open connections and terminate all threads.
        serverSelector.close();
    }

    //
    // SmartFrog Component interface
    //

    /**
     * Returns textual representation.
     *
     * @return textual representation
     */
    public String toString() {
        StringBuffer str = new StringBuffer();

        str.append("LoadBalancer - name: " + sfCompleteNameSafe());
        str.append(", port: " + port);
        str.append(", hostsPort: " + hostsPort);
       
        return (str.toString());
    }

    /**
     * Reads SF description.
     *
     * @throws SmartFrogException error while reading
     * @throws RemoteException In case of network/rmi error
     */
    private void readSFAttributes() throws SmartFrogException, RemoteException {
        //
        // Optional attributes.
        //

    	//System.out.println("Reading serverHosts...");
    	serverHosts = ListUtils.resolveStringList(this,new Reference(SERVERHOSTS), false);
    	//System.out.println("Reading serverHosts ..."+serverHosts);
        port = sfResolve(PORT, port, false);
        hostsPort = sfResolve(HOSTSPORT, hostsPort, false);
        sleep = sfResolve(SLEEP, 0, true);

        if (sfLog().isInfoEnabled()) sfLog().info(this.toString());
    }

    /**
     * Standard sfDeploy().
     *
     * @exception SmartFrogException error while deploying
     * @throws RemoteException In case of network/rmi error
     */
    public synchronized void sfDeploy() throws SmartFrogException, RemoteException {
        super.sfDeploy();

        //  any component-specific init code
        readSFAttributes();

        // Simply invoke the instance start method
	// This needs to be done here, so that others can invoke the addServer methods, etc
	// during their sfStarts.

        start();

        // Any error - propagate and hance fail to deploy
    }

    /**
     * Standard sfTerminateWith()
     *
     * @param tr TerminationRecord object
     */
    public synchronized void sfTerminateWith(TerminationRecord tr) {
        // Simply invoke the instance stop method
        stop();
        super.sfTerminateWith(tr);
    }

    public void enableServerInstance(int instance) {
        if (!stopping) {
        	String hostname = serverHosts.get(instance);
        	LoadBalancerBindingImpl lbb = null;
        	//System.out.println("LBB REFERENCE***"+new Reference(LBBPREFIX+instance+LBBSUFFIX).toString());
        	try {
        		lbb=(LoadBalancerBindingImpl) sfResolve(Reference.fromString(LBBPREFIX+instance+LBBSUFFIX));
        	} catch (Exception e){System.out.println(e);}
        	//System.out.println("Adding a server...");
            serverSelector.addServer(hostname, hostsPort, lbb);
        }
    }

    public boolean disableServerInstance(int instance) {
    	String hostname = serverHosts.get(instance);
        return serverSelector.removeServerZeroClose(hostname);
    }
    
    public String lookUpHost(int instance) {
    	return serverHosts.get(instance);
    }
   
    /**
     * The main thread of the Balancer.
     */
    private class BalancerThread implements Runnable {
        private Thread thread = null;
        private volatile boolean stopRequested = false;

        /**
         * Constructor
         */
        BalancerThread() {
        }

        /**
         * Start the thread
         */
        void start() {
            if (thread == null) {
                // Create writer thread.
                thread = new Thread(this, "SFBalancerThread");
                thread.start();
            }
        }

        /**
         * Stop the thread
         */
        void stop() {
            if (thread != null) {
                stopRequested = true;
                thread.interrupt();

                try {
                    thread.join();
                } catch (InterruptedException ie) {
                    if (sfLog().isIgnoreEnabled()) sfLog().ignore(ie);
                }

                thread = null;
            }
        }

        /**
         * Accept new connections from clients and perform balancing to connect
         * them to one of the servers.
         */
        public void run() {
        	ServerSocketChannel server = null; // Server socket to accept connections from clients
        	try {

        		try {
        			server = ServerSocketChannel.open();

        			InetSocketAddress address = new InetSocketAddress(port);
        			server.socket().bind(address);
        		} catch (IOException e) {
        			if (sfLog().isErrorEnabled()) sfLog().error("Error creating server socket: " + e.getMessage(),e);

        			// Exit thread because encountered a severe problem
        			return;
        		}

        		//
        		// Now loop to wait for connections from clients.

        		while (!stopRequested) {
        			try {
        				SocketChannel client = server.accept();
        				connectionCount += 1;
        				if (sfLog().isInfoEnabled()) sfLog().info("   - Accepted connection from " + client);

        				// Select a server to handle the new client socket, and hand off processing to it.
        				serverSelector.addClient(client);
        			} catch (IOException ioe) {
        				if (sfLog().isErrorEnabled()) sfLog().error("Error accepting connection from client" +ioe.getMessage(),ioe);
        			}
        		}

        		//

        	} finally {
        		// Clean up the socket when we stop.
        		try {
        			if( server != null) {
        				server.close();
        			}
        		} catch (IOException ioe) {
        			if (sfLog().isErrorEnabled()) sfLog().error("Error closing server socket " + ioe.getMessage(),ioe);
        		} 	
        		//and remove our self-reference
        		balancerThread = null;
        	}
            if (sfLog().isDebugEnabled()) sfLog().debug("BalancerThread terminated.");
        }
    }
}
