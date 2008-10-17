/* (C) Copyright 2008 Hewlett-Packard Development Company, LP

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
package org.smartfrog.services.hadoop.components.cluster;

import org.apache.hadoop.util.Service;
import org.smartfrog.services.hadoop.conf.ManagedConfiguration;
import org.smartfrog.services.hadoop.core.SFHadoopException;
import org.smartfrog.services.hadoop.core.ServiceStateChangeHandler;
import org.smartfrog.sfcore.common.SmartFrogDeploymentException;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.common.SmartFrogLivenessException;
import org.smartfrog.sfcore.prim.Liveness;
import org.smartfrog.sfcore.prim.TerminationRecord;
import org.smartfrog.sfcore.reference.Reference;
import org.smartfrog.sfcore.utils.ComponentHelper;
import org.smartfrog.sfcore.utils.WorkflowThread;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;

/**
 * This is a component that deploys a hadoop service. If the
 */

public abstract class HadoopServiceImpl extends HadoopComponentImpl
        implements HadoopService, ServiceStateChangeHandler {

    private Service service;
    private ServiceDeployerThread deployerThread;
    private static final int SHUTDOWN_DELAY = 1000;
    public static final String NO_FILESYSTEM = "Filesystem is not running";
    private boolean expectNodeTermination;
    private volatile boolean terminationInitiated;
    private Reference completeName;
    private static final String SERVICE_HAS_HALTED = "Service has halted";
    public static final String ERROR_NO_START = "Failed to start the ";


    protected HadoopServiceImpl() throws RemoteException {
    }


    /**
     * Called after instantiation for deployment purposes. Heart monitor is started and if there is a parent the
     * deployed component is added to the heartbeat. Subclasses can override to provide additional deployment behavior.
     * Attributees that require injection are handled during sfDeploy().
     *
     * @throws SmartFrogException error while deploying
     * @throws RemoteException    In case of network/rmi error
     */
    @Override
    public synchronized void sfDeploy() throws SmartFrogException, RemoteException {
        super.sfDeploy();
        expectNodeTermination = true; //conf.getBoolean(FileSystemNode.ATTR_EXPECT_NODE_TERMINATION, true);
        completeName = sfCompleteName();
    }

    /**
     * On shutdown, terminates any non-null service.
     *
     * @param status termination status
     */
    @Override
    protected synchronized void sfTerminateWith(TerminationRecord status) {
        super.sfTerminateWith(status);
        terminationInitiated = true;
        terminateHadoopService();
        terminateDeployerThread();
    }

    /**
     * Terminate the deployer thread
     */
    protected void terminateDeployerThread() {
        ServiceDeployerThread deployer = getDeployerThread();
        if (deployer != null) {
            terminateDeployerThread(deployer);
        }
    }

    /**
     * Terminate the specified thread. subclasses can use alternate shutdown policies
     *
     * @param deployer thread to shut down
     */
    protected void terminateDeployerThread(ServiceDeployerThread deployer) {
        deployer.requestAndWaitForThreadTermination(SHUTDOWN_DELAY);
    }

    /**
     * Liveness call in to check if this component is still alive.
     *
     * @param source source of call
     * @throws SmartFrogLivenessException on any liveness problem
     * @throws RemoteException            for consistency with the {@link Liveness} interface
     */
    @Override
    public void sfPing(Object source)
            throws SmartFrogLivenessException, RemoteException {
        super.sfPing(source);
        pingHadoopService();
    }


    /**
     * Test for the service being live
     *
     * @return true if the service is live
     * @throws RemoteException for RMI problems
     */
    @Override
    public boolean isServiceLive() throws RemoteException {
        Service s = service;
        return s != null && s.getServiceState() == Service.ServiceState.LIVE;
    }

    /**
     * Return the name of this service; please override for better messages
     *
     * @return a name of the service for error messages
     */
    protected String getName() {
        return "Service";
    }

    /**
     * Get the deployer thread; may be null
     *
     * @return
     */
    protected ServiceDeployerThread getDeployerThread() {
        return deployerThread;
    }

    /**
     * Override point; tell the base class whether or not {@link #service} must be non-null in the {@link
     * #sfPing(Object)} operation
     *
     * @return true for non-null, false to allow null values.
     */
    protected boolean requireNonNullServiceInPing() {
        return true;
    }

    /**
     * When the namenode is terminated, and we are not shutting down ourself, then we notify
     * {@inheritDoc}
     */
    @Override
    public void onStateChange(Service service, Service.ServiceState oldState,
                              Service.ServiceState newState) {
        if (newState == Service.ServiceState.TERMINATED && !terminationInitiated) {
            TerminationRecord tr;
            if (expectNodeTermination) {
                tr = TerminationRecord.normal(SERVICE_HAS_HALTED, completeName);
            } else {
                tr = TerminationRecord.abnormal(SERVICE_HAS_HALTED, completeName);
            }
            ComponentHelper helper = new ComponentHelper(this);
            helper.targetForWorkflowTermination(
                    tr);
        }
    }

    /**
     * Ping the hadoop service.
     *
     * @throws SmartFrogLivenessException on a ping failure, or a null service and non-null services are forbidden
     * @throws RemoteException for remoting problems
     */
    protected void pingHadoopService() throws SmartFrogLivenessException, RemoteException {
        try {
            ServiceDeployerThread deployer = deployerThread;
            if (deployer != null) {
                deployer.ping(true);
            }
            Service hadoopService = service;
            if (hadoopService != null) {
                Service.ServiceStatus serviceStatus = hadoopService.ping();
                List<Throwable> throwables = serviceStatus.getThrowables();
                if (!throwables.isEmpty()) {
                    //trouble
                    throw (SmartFrogLivenessException) SmartFrogLivenessException.forward(
                            new SFHadoopException(serviceStatus, this));
                }
                //now look at the service state alone
                //as SF has stricter service state rules
                Service.ServiceState state = serviceStatus.getState();
                switch(state) {
                  case STARTED:
                  case LIVE:
                    break;
                  case UNDEFINED:
                  case CREATED:
                  case FAILED:
                  case TERMINATED:
                  default:
                    throw new SmartFrogLivenessException("Service is not live: "+serviceStatus, this);
                }
            } else if (requireNonNullServiceInPing()) {
                throw new SmartFrogLivenessException("No running " + getName(), this);
            }
        } catch (RemoteException e) {
            throw e;
        } catch (IOException e) {
            throw new SmartFrogLivenessException(e, this);
        }
    }

    /**
     * Terminate a service and set the {@link #service} field to null. Does nothing if the service is already
     * terminated
     */
    protected synchronized void terminateHadoopService() {
        if (service != null) {
            sfLog().debug("Terminating hadoop service " + service.getServiceName());
            try {
                service.close();
            } catch (IOException e) {
                sfLog().warn("When closing the service : "+ e, e);
            }
            service = null;
        }
    }


    /**
     * Get the current service value; may be null
     *
     * @return the service
     */
    public synchronized Service getService() {
        return service;
    }

    /**
     * Set the current service value; null is acceptable
     *
     * @param service the new value
     */
    protected synchronized void setService(Service service) {
        this.service = service;
    }


    /**
     * Deploy a service. It must be in the CREATED state. The field {@link #service} is set to the service argument,
     * which must be null. There is special handling for wrapped Jetty exceptions
     *
     * @param hadoopService service to bind to and deploy.
     * @param conf          configuration -used for diagnostics
     * @throws SmartFrogException           on any problem
     * @throws SFHadoopException            for Jetty exceptions and other causes of trouble
     * @throws SmartFrogDeploymentException on some wrapped IOExceptions
     */
    protected void deployService(Service hadoopService, ManagedConfiguration conf) throws SmartFrogException {
        deployerThread = createDeployerThread(hadoopService, conf);
        deployerThread.start();
    }

    /**
     * {@inheritDoc}
     */
    public Service.ServiceState getServiceState() throws RemoteException {
        Service hadoop = service;
        if (hadoop == null) {
            return Service.ServiceState.UNDEFINED;
        } else {
            return hadoop.getServiceState();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Service.ServiceStatus pingService() throws IOException {
        Service hadoop = service;
        if (hadoop == null) {
            return null;
        } else {
            return hadoop.ping();
        }
    }


    /**
     * Create the deployer thread
     *
     * @param hadoopService service to bind to and deploy.
     * @param conf          configuration -used for diagnostics
     * @return a new thread that is an instance of ServiceDeployerThread
     */
    protected ServiceDeployerThread createDeployerThread(Service hadoopService,
                                                         ManagedConfiguration conf) {
        return new ServiceDeployerThread(hadoopService, conf, false);
    }


    /**
     * reject any attempts to set the service property to a non-null value if it is already non-null; stops us trying to
     * deploy multiple services
     *
     * @param hadoopService the service
     * @throws SmartFrogDeploymentException if a service is already deployed 
     */
    private synchronized void bindToService(Service hadoopService) throws SmartFrogDeploymentException {
        if (service != null) {
            throw new SmartFrogDeploymentException("Cannot bind to a new service (" + hadoopService + ")"
                    + " when an existing service is in use: "
                    + service);
        }
        setService(hadoopService);
    }

    /**
     * Inner deploy operation called on a second thread; can be subclassed for fun.
     *
     * @param hadoopService service to deploy
     * @param conf          configuration
     * @throws SmartFrogException
     */
    private void innerDeploy(Service hadoopService, ManagedConfiguration conf) throws SmartFrogException {
        try {
            bindToService(hadoopService);
            //start the service
            hadoopService.start();
            onServiceDeploymentComplete();
        } catch (SmartFrogException e) {
            sfLog().warn("Unable to deploy " + hadoopService, e);
            terminateHadoopService();
            throw e;
        } catch (Throwable e) {
            sfLog().warn("Unable to deploy " + hadoopService, e);
            terminateHadoopService();
            //convert illegal argument exceptions
            throw SFHadoopException.forward("Failed to deploy " + hadoopService,
                    e,
                    this,
                    conf);
        }
    }

    /**
     * Override point: Callback once service deployment is finished. It is not called in a synchronized context.
     *
     * @throws IOException        IO/hadoop problems
     * @throws SmartFrogException smartfrog problems
     */
    protected void onServiceDeploymentComplete() throws IOException, SmartFrogException {
        Service s = getService();
        if(s !=null) {
            sfLog().info(getName() + " deployment complete: service is now " + s.getServiceState());
        } else {
            sfLog().warn(getName() + " deployment complete: but service==null");
        }
    }

    /**
     * For use in sfStart(); calls {@link #createTheService(ManagedConfiguration)}
     * to create the service, then {@link #deployService(Service, ManagedConfiguration)}.
     * @throws SmartFrogException wrapping any other exception thrown
     */
    protected void createAndDeployService() throws SmartFrogException {
        ManagedConfiguration configuration;
        configuration = createConfiguration();
        try {
            Service service = createTheService(configuration);
            deployService(service, configuration);
        } catch (Throwable thrown) {
            throw SFHadoopException.forward(ERROR_NO_START + getName(),
                    thrown,
                    this,
                    configuration);
        }
    }

    /**
     * Create the specific service
     *
     * @param configuration configuration to use
     * @return the
     * @throws IOException
     * @throws SmartFrogException
     */
    protected Service createTheService(ManagedConfiguration configuration) throws IOException, SmartFrogException {
        return null;
    }
    /**
     * This is the thread that starts the deployment
     */
    public class ServiceDeployerThread extends WorkflowThread {

        private Service hadoopService;
        private ManagedConfiguration conf;

        private boolean useWorkflowTermination;

        public ServiceDeployerThread(Service hadoopService, ManagedConfiguration conf,
                                      boolean useWorkflowTermination) {
            super(HadoopServiceImpl.this, useWorkflowTermination);
            this.useWorkflowTermination = useWorkflowTermination;
            this.hadoopService = hadoopService;
            this.conf = conf;
        }


        /**
         * only terminate if there was a failure to deploy.
         */
        @Override
        protected void processRunResults() {
            if (useWorkflowTermination) {
                super.processRunResults();
            } else {
                //now analyse the result, create a term record and maybe terminate the owner
                terminateIFFAbnormal();
            }
        }

        protected void terminateIFFAbnormal() {
            boolean isNormal = getThrown() == null;
            if (!isNormal) {
                TerminationRecord tr = createTerminationRecord();
                aboutToTerminate(tr);
                ComponentHelper helper = new ComponentHelper(HadoopServiceImpl.this);
                helper.targetForTermination(tr, false, false, false);
            }
        }

        /**
         * The executor method calls {@link HadoopServiceImpl#innerDeploy(Service, ManagedConfiguration)} for the actual
         * deployment
         *
         * @throws Throwable any failure desired
         */
        @Override
        public void execute() throws Throwable {
            innerDeploy(hadoopService, conf);
        }


    }
}
