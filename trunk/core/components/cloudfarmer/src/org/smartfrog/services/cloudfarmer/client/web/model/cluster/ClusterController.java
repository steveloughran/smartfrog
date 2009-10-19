/* (C) Copyright 2009 Hewlett-Packard Development Company, LP

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


package org.smartfrog.services.cloudfarmer.client.web.model.cluster;

import org.smartfrog.services.cloudfarmer.client.web.exceptions.UnimplementedException;
import org.smartfrog.services.cloudfarmer.client.web.hadoop.descriptions.TemplateNames;
import org.smartfrog.services.cloudfarmer.client.web.model.AbstractEndpoint;
import org.smartfrog.services.cloudfarmer.client.web.model.LocalSmartFrogDescriptor;
import org.smartfrog.services.cloudfarmer.client.web.model.RemoteDaemon;
import org.smartfrog.services.cloudfarmer.client.web.model.workflow.Workflow;
import org.smartfrog.sfcore.common.SmartFrogException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The cluster controller is whatever gives us new clusters
 */
public abstract class ClusterController extends AbstractEndpoint implements Iterable<HostInstance>, TemplateNames {

    private HostInstanceList hosts;
    private Map<String, HostInstance> hostMap;
    private static final int INITIAL_HOSTLIST_CAPACITY = 1;

    /**
     * Basic constructor clears the host list
     */
    protected ClusterController() {
        clearHostList();
    }

    /**
     * Bind to the URL
     * @param baseURL the URL for the controller
     */
    protected ClusterController(String baseURL) {
        super(baseURL);
        clearHostList();
    }

    /**
     * Bind to the controller. If this fails, the controller must be considered invalid.
     *
     * @throws IOException        network trouble
     * @throws SmartFrogException SF trouble
     */
    public void bind() throws IOException, SmartFrogException {

    }

    /**
     * Get the description -can be displayed in the view
     *
     * @return an end-user description
     */
    public abstract String getDescription();

    /**
     * Clear the host list
     */
    protected synchronized void clearHostList() {
        hosts = new HostInstanceList(INITIAL_HOSTLIST_CAPACITY);
        hostMap = new HashMap<String, HostInstance>(INITIAL_HOSTLIST_CAPACITY);
    }

    /**
     * Replace the existing host list with a new one. Nothing is copied over; if you want to preserve the application
     * list, do it first.
     *
     * @param list new list of hosts
     */
    protected synchronized void replaceHostList(HostInstanceList list) {
        hosts = list;
        hostMap = new HashMap<String, HostInstance>(list.size());
        for (HostInstance instance : list) {
            hostMap.put(instance.getId(), instance);
        }
    }

    /**
     * Get a list of the hosts. The list is a clone, no need to worry about synchronization problems, though hosts may
     * have been deleted by the time you get to them
     *
     * @return the cloned list
     */
    public synchronized HostInstanceList getHosts() {
        return (HostInstanceList) hosts.clone();
    }

    /**
     * Get the master
     *
     * @return the master or null
     */
    public synchronized HostInstance getMaster() {
        return hosts.getMaster();
    }

    /**
     * Test for the cluster having a master
     *
     * @return true iff the cluster has a master
     */
    public boolean hasMaster() {
        return getMaster() != null;
    }

    /**
     * Get the number of hosts
     *
     * @return the count of hosts
     */
    public synchronized int getHostCount() {
        return hosts.size();
    }

    /**
     * Get an iterator over a clone of the host list.
     *
     * @return a new iterator.
     */
    public Iterator<HostInstance> iterator() {
        return getHosts().iterator();
    }

    /**
     * Return the number of task slots for this cluster. Subclasses can change how this is calculated
     *
     * @return a default value, or something cleverer
     */
    public int getTaskSlots() {
        return 4;
    }

    /**
     * Refreshes the host list
     *
     * @throws IOException        network trouble
     * @throws SmartFrogException SF trouble
     */
    public abstract void refreshHostList() throws IOException, SmartFrogException;


    /**
     * @param role role of these hosts
     * @param min  minimum number
     * @param max  maximum number
     * @return the allocated machines
     * @throws IOException        network trouble
     * @throws SmartFrogException SF trouble
     */
    public HostInstanceList createHosts(String role, int min, int max)
            throws IOException, SmartFrogException {
        throw new UnimplementedException("Cannot add hosts to this cluster");
    }


    /**
     * Create a new host
     *
     * @param hostname      a hostname
     * @param largeInstance the instance
     * @param descriptor    descriptor to deploy on the host
     * @return a new host
     * @throws IOException        network trouble
     * @throws SmartFrogException SF trouble
     */
    public HostInstance createHost(String hostname,
                                   boolean largeInstance,
                                   LocalSmartFrogDescriptor descriptor)
            throws IOException, SmartFrogException {
        throw new UnimplementedException("Cannot add a host to this cluster");
    }


    /**
     * Override point
     *
     * @return true iff hosts can be added through createHost
     */
    public boolean canCreateHost() {
        return false;
    }

    /**
     * Override point
     *
     * @return true iff hosts can be manually added
     */
    public boolean canAddNamedHost() {
        return false;
    }

    /**
     * Add a host to the system.
     *
     * The base class validates against {@link #canAddNamedHost()} and the current cluster state, then creates a stub
     * instance, which can be replaced with a different one, if desired. It does not add it to the host list.
     *
     * @param hostname a hostname
     * @param isMaster is the node a master
     * @param isWorker is the node a worker
     * @return the instance  
     * @throws IOException            network trouble
     * @throws SmartFrogException     SF trouble
     * @throws UnimplementedException if you cannot add hosts to this manager
     */
    public HostInstance addNamedHost(String hostname, boolean isMaster, boolean isWorker)
            throws IOException, SmartFrogException {
        if (!canAddNamedHost()) {
            throw new UnimplementedException("Cannot add a host to this cluster");
        }
        validateRoles(isMaster, isWorker);
        return new HostInstance(hostname);
    }


    /**
     * Delete a host
     *
     * @param hostID a host ID
     * @return true if the request has been queued
     * @throws IOException        network trouble
     * @throws SmartFrogException SF trouble
     */
    public boolean deleteHost(String hostID)
            throws IOException, SmartFrogException {
        return false;
    }

    /**
     * Look up a host by ID
     *
     * @param hostID ID
     * @return the host information or null for no match
     * @throws IOException        network trouble
     * @throws SmartFrogException SF trouble
     */
    public synchronized HostInstance lookupHost(String hostID)
            throws IOException, SmartFrogException {
        return hostMap.get(hostID);
    }

    /**
     * Look up a host in the list by name. This does not trigger a refresh of the list
     *
     * @param name host name to look for
     * @return the instance
     */
    public synchronized HostInstance lookupHostByName(String name) {
        for (HostInstance host : hosts) {
            if (host.hostname.equals(name)) {
                return host;
            }
        }
        return null;
    }

    /**
     * Add a new host to the hosts list
     *
     * @param hi instance
     */
    protected void addHostInstance(HostInstance hi) {
        synchronized (this) {
            hosts.add(hi);
            hostMap.put(hi.getId(), hi);
        }
    }

    /**
     * Remove an instance from the list
     *
     * @param hi the host instance. Can be null.
     * @return true if it was removed.
     */
    protected synchronized boolean removeHostInstance(HostInstance hi) {
        if (hi != null) {
            hosts.remove(hi);
            hostMap.remove(hi.getId());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Shut down the cluster
     *
     * @throws IOException        network trouble
     * @throws SmartFrogException SF trouble
     */
    public abstract void shutdownCluster() throws IOException, SmartFrogException;

    @Override
    public String toString() {
        return getDescription();
    }


    /**
     * Validate the roles of this node
     *
     * @param isMaster is the node to be a master
     * @param isWorker is the node to be a worker
     * @throws SmartFrogException if the cluster does not support the role at this time
     */
    protected void validateRoles(boolean isMaster, boolean isWorker) throws SmartFrogException {
        HostInstance master = getMaster();
        boolean hasMaster = master != null;
        if (isMaster && hasMaster) {
            throw new SmartFrogException("Cluster already has the master " + master.hostname
                    + " proposed values master=" + isMaster + " worker=" + isWorker);
        }
        if (isWorker && !isMaster && !hasMaster) {
            throw new SmartFrogException("Cluster has no master"
                    + " proposed values master=" + isMaster + " worker=" + isWorker);
        }
    }

    /**
     * Based on the role, choose what to install on the target host. There is no waiting for the host to be up here;
     * that work has to be done in some other mechanism.
     *
     * @param host     target host
     * @param isMaster is the node to be a master
     * @param isWorker is the node to be a worker
     * @return the workflow
     * @throws IOException        network trouble
     * @throws SmartFrogException SF trouble
     */
    protected Workflow installRole(HostInstance host, boolean isMaster, boolean isWorker)
            throws IOException, SmartFrogException {
        String resource;
        String masterName;
        HostInstance master = getMaster();
        boolean hasMaster = master != null;
        HostRole role = HostRole.getRole(isMaster, isWorker);
        switch (role) {
            case masterworker:
                resource = HADOOP_MASTER_WORKER;
                if (hasMaster) {
                    throw new SmartFrogException("Cluster already has the master " + master.hostname);
                }
                masterName = host.getHostname();
                break;
            case master:
                resource = HADOOP_MASTER;
                if (hasMaster) {
                    throw new SmartFrogException("Cluster already has the master " + master.hostname);
                }
                masterName = host.getHostname();
                break;
            case worker:
                resource = HADOOP_WORKER;
                if (!hasMaster) {
                    throw new SmartFrogException("Cluster has no master");
                }
                masterName = master.getHostname();
                break;
            case none:
            default:
                masterName = "";
                resource = null;
        }
        if (resource == null) {
            //nothing to deploy
            return null;
        } else {
            String appname = role.toString();
            return installApplication(host, appname, resource, masterName, isMaster, isWorker);
        }
    }

    /**
     * Install the application
     *
     * @param host       target host
     * @param appname    application name
     * @param resource   resource name (must be on classpath)
     * @param masterName hostname for the master
     * @param isMaster   is the node to be a master
     * @param isWorker   is the node to be a worker
     * @return the workflow
     * @throws IOException        network trouble
     * @throws SmartFrogException SF trouble
     */
    protected Workflow installApplication(HostInstance host,
                                          String appname,
                                          String resource,
                                          String masterName,
                                          boolean isMaster,
                                          boolean isWorker
    ) throws IOException, SmartFrogException {
        ///set the binding values
        System.setProperty(BINDING_MASTER_HOSTNAME, masterName);
        System.setProperty(BINDING_TASKTRACKER_SLOTS, "" + getTaskSlots());

        //load the CD -so that any binding problem shows up early
        LocalSmartFrogDescriptor localApp = new LocalSmartFrogDescriptor();
        localApp.parseResource(resource);
        localApp.throwParseExceptionIfNeeded();

        //bind to the daemon
        RemoteDaemon daemon = host.bindToDaemon();

        //create the workflow
        Workflow workflow = daemon.createWorkflow(appname, localApp.getComponentDescription());
        host.setApplication(workflow);
        host.setMaster(isMaster);
        host.setWorker(isWorker);
        return workflow;
    }
}
