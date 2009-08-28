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
package org.apache.hadoop.mapred;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.util.NodeUtils;
import org.apache.hadoop.util.Service;
import org.smartfrog.services.hadoop.conf.ConfigurationAttributes;
import org.smartfrog.services.hadoop.core.BindingTuple;
import org.smartfrog.services.hadoop.core.ServiceInfo;
import org.smartfrog.services.hadoop.core.ServiceStateChangeHandler;
import org.smartfrog.services.hadoop.core.ServiceStateChangeNotifier;
import org.smartfrog.services.hadoop.core.ServicePingStatus;
import org.smartfrog.services.hadoop.core.InnerPing;
import org.smartfrog.services.hadoop.core.PingHelper;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created 19-May-2008 12:01:47
 *
 */

public class ExtJobTracker extends JobTracker implements ServiceInfo, ConfigurationAttributes, InnerPing {

    private static final Log LOG= LogFactory.getLog(ExtJobTracker.class);
    private ServiceStateChangeNotifier notifier;
    private final PingHelper pingHelper = new PingHelper(this);


    public ExtJobTracker(JobConf conf)
            throws IOException, InterruptedException, LoginException {
        this(null, conf);
    }

    public ExtJobTracker(ServiceStateChangeHandler owner, JobConf conf) 
            throws IOException, InterruptedException, LoginException {
        super(conf);
        notifier = new ServiceStateChangeNotifier(this, owner);
    }

    /**
     * Override point - aethod called whenever there is a state change.
     *
     * The base class logs the event.
     *
     * @param oldState existing state
     * @param newState new state.
     */
    @Override
    protected void onStateChange(ServiceState oldState, ServiceState newState) {
        super.onStateChange(oldState, newState);
        LOG.info("State change: JobTracker is now " + newState);
        notifier.onStateChange(oldState, newState);
    }

    /**
     * This service terminates itself by stopping the {@link JobEndNotifier}
     * and then closing down the job tracker
     *
     * @throws IOException exceptions which will be logged
     */
/*
    @Override
    protected void innerClose() throws IOException {
        super.innerClose();
        //also: shut down the filesystem
        //closeTheFilesystemQuietly();
    }
*/


    /**
     * Ping: checks that a component considers itself live.
     *
     * This method makes the ping public
     *
     * @return the current service state.
     * @throws IOException for any ping failure
     */
    @Override
    public ServicePingStatus ping() throws IOException {
        return pingHelper.ping();
    }


    /**
     * {@inheritDoc}
     *
     * @param status a status that can be updated with problems
     * @throws IOException for any problem
     */
    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    @Override
    public void innerPing(ServicePingStatus status) throws IOException {
        if (infoServer == null || !infoServer.isAlive()) {
            status.addThrowable(
                    new IOException("TaskTracker HttpServer is not running on port "
                            + infoPort));
        }
        if (interTrackerServer == null) {
            status.addThrowable(
                    new IOException("InterTrackerServer is not running on port "
                            + port));
        }
    }

    /**
     * Get the port used for IPC communications
     * @return the port number; not valid if the service is not LIVE
     */
    @Override
    public int getIPCPort() {
        return getTrackerPort();
    }

    /**
     * Get the port used for HTTP communications
     * @return the port number; not valid if the service is not LIVE
     */
    @Override
    public int getWebPort() {
        return getInfoPort();
    }




  /**
     * Get the current number of workers
     *
     * @return the worker count
     */
/*
    @Override
    public int getLiveWorkerCount() {
        return getNumResolvedTaskTrackers();
    }
*/

    /**
     * {@inheritDoc}
     *
     * @return the binding information
     */
    @Override
    public List<BindingTuple> getBindingInformation() {
        List<BindingTuple> bindings = new ArrayList<BindingTuple>();
        InetSocketAddress ipcAddress = interTrackerServer.getListenerAddress();
        bindings.add(NodeUtils.toBindingTuple(MAPRED_JOB_TRACKER, "ipc",
                ipcAddress));
        //try and work out the underlying bindings by going back to the configuration
        InetSocketAddress httpAddr = NodeUtils.resolveAddress(getConf(), MAPRED_JOB_TRACKER_HTTP_ADDRESS);
        InetSocketAddress realHttpAddr = new InetSocketAddress(httpAddr.getAddress(), getInfoPort());
        bindings.add(NodeUtils.toBindingTuple(MAPRED_JOB_TRACKER_HTTP_ADDRESS, "http", realHttpAddr));
        return bindings;
    }

    /**
     * Get the filesystem. Will be null when the service is not live
     * @return the filesystem or null
     */
    public FileSystem getFileSystem() {
        return super.getFileSystem();
    }
}
