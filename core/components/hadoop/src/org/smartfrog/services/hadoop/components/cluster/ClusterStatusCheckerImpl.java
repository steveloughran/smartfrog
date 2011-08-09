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

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.ClusterStatus;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobTracker;
import org.smartfrog.services.hadoop.operations.core.HadoopCluster;
import org.smartfrog.services.hadoop.mapreduce.submitter.SubmitterImpl;
import org.smartfrog.services.hadoop.operations.conf.HadoopConfiguration;
import org.smartfrog.services.hadoop.operations.conf.ManagedConfiguration;
import org.smartfrog.services.hadoop.operations.exceptions.SFHadoopException;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.common.SmartFrogLivenessException;
import org.smartfrog.sfcore.prim.Liveness;
import org.smartfrog.sfcore.prim.TerminationRecord;
import org.smartfrog.sfcore.reference.Reference;
import org.smartfrog.sfcore.utils.ComponentHelper;
import org.smartfrog.sfcore.workflow.conditional.Condition;
import org.smartfrog.sfcore.workflow.conditional.conditions.AbstractConditionPrim;

import java.io.IOException;
import java.net.URI;
import java.rmi.RemoteException;

/**
 * Created 30-Apr-2008 14:20:50
 */


public class ClusterStatusCheckerImpl extends AbstractConditionPrim
        implements HadoopConfiguration, HadoopCluster, ClusterStatusChecker, Condition {
    private JobClient client;
    private boolean checkOnLiveness;
    private boolean jobtrackerLive;

    //declares that we can handle the filesystem
    private boolean supportedFileSystem;

    /**
     * For all the min/max values, <0 means 'dont check'
     */
    private int minActiveMapTasks;
    private int maxActiveMapTasks;
    private int minActiveReduceTasks;
    private int maxActiveReduceTasks;
    private int maxSupportedMapTasks;
    private int maxSupportedReduceTasks;
    private String jobtracker;
    public static final String STATUS_CHECKED = "Hadoop Cluster status checked against ";
    public static final String ERROR_CANNOT_CONNECT = "Cannot connect to ";


    public ClusterStatusCheckerImpl() throws RemoteException {
    }

    /**
     * Can be called to start components. Subclasses should override to provide functionality Do not block in this call,
     * but spawn off any main loops!
     *
     * @throws SmartFrogException failure while starting
     * @throws RemoteException    In case of network/rmi error
     */
    @Override
    public synchronized void sfStart() throws SmartFrogException, RemoteException {
        super.sfStart();
        jobtracker = SubmitterImpl.resolveJobTracker(this, new Reference(MAPRED_JOB_TRACKER));
        supportedFileSystem = sfResolve(ATTR_SUPPORTEDFILESYSTEM, false, true);
        jobtrackerLive = sfResolve(ATTR_JOBTRACKERLIVE, false, true);
        minActiveMapTasks = sfResolve(ATTR_MIN_ACTIVE_MAP_TASKS, 0, true);
        maxActiveMapTasks = sfResolve(ATTR_MAX_ACTIVE_MAP_TASKS, 0, true);
        maxSupportedMapTasks = sfResolve(ATTR_MAX_SUPPORTED_MAP_TASKS, 0, true);
        minActiveReduceTasks = sfResolve(ATTR_MIN_ACTIVE_REDUCE_TASKS, 0, true);
        maxActiveReduceTasks = sfResolve(ATTR_MAX_ACTIVE_REDUCE_TASKS, 0, true);
        maxSupportedReduceTasks = sfResolve(ATTR_MAX_SUPPORTED_REDUCE_TASKS, 0, true);
        checkOnLiveness = sfResolve(ATTR_CHECK_ON_LIVENESS, false, true);
        boolean checkOnStartup = sfResolve(ATTR_CHECK_ON_STARTUP, false, true);

        if (checkOnStartup) {
            checkClusterStatus();
            new ComponentHelper(this)
                    .sfSelfDetachAndOrTerminate(TerminationRecord.NORMAL,
                            STATUS_CHECKED + jobtracker, null, null);
        }
    }

    /**
     * Demand create the cluster proxy. This can take time and fail.
     *
     * @return a client which may exist already
     * @throws SFHadoopException if the connection cannot be made
     * @throws RemoteException for network problems
     */

    private synchronized JobClient createClientOnDemand() throws SmartFrogException, RemoteException {
        if (client != null) {
            return client;
        }
        try {
            ManagedConfiguration conf = ManagedConfiguration.createConfiguration(this, false, false, true);
            sfLog().info("Connecting to Job Tracker at " + jobtracker);
            client = new JobClient(conf);
            return client;
        } catch (RemoteException e) {
            throw e;
        } catch (IOException e) {
            throw new SFHadoopException(ERROR_CANNOT_CONNECT + jobtracker, e, this);
        }
    }


    /**
     * Liveness call in to check if this component is still alive.
     *
     * @param source source of call
     * @throws SmartFrogLivenessException component is terminated
     * @throws RemoteException            for consistency with the {@link Liveness} interface
     */
    @Override
    public void sfPing(Object source) throws SmartFrogLivenessException, RemoteException {
        super.sfPing(source);
        if (checkOnLiveness) {
            try {
                checkClusterStatus();
            } catch (SmartFrogException e) {
                throw (SmartFrogLivenessException) SmartFrogLivenessException.forward(e);
            }
        }
    }

    private void maybeDumpConfiguration(ManagedConfiguration conf) {
        if(sfLog().isDebugEnabled()) {
            sfLog().debug(conf.dump());
        }
    }

    /**
     * Check the cluster status
     *
     * @throws SFHadoopException on any problem with the checks
     * @return a cluster status string
     */
    private String checkClusterStatus() throws SmartFrogException {

        try {
            JobClient cluster = createClientOnDemand();
            ClusterStatus status = cluster.getClusterStatus();
            StringBuilder result = new StringBuilder();

            if (supportedFileSystem) {
                Path sysDir = cluster.getSystemDir();
                URI uri = sysDir.toUri();
                sfLog().info("Checking filesystem " + uri);
                ManagedConfiguration conf = (ManagedConfiguration) cluster.getConf();
                String impl = "fs." + uri.getScheme() + ".impl";
                String classname = conf.get(impl);
                if(classname == null) {
                    maybeDumpConfiguration(conf);
                    throw new SFHadoopException("File system " + uri + " will not load "
                            +" - no configuration mapping for " + impl
                            +" in "+ conf.dump(),
                            this,
                            conf);
                }
                try {
                    conf.getClassByName(classname);
                } catch (ClassNotFoundException e) {
                    throw new SFHadoopException("File system " + uri + " will not load "
                            + " - unable to locate class " + impl + " : "+e,
                            e,
                            this,
                            conf);
                }
                try {
                    result.append("Filesystem: ").append(uri).append(" ; ");
                    FileSystem fs = cluster.getFs();
                } catch (IOException e) {
                    throw new SFHadoopException("File system "+ uri + " will not load "
                            + e,
                            e,
                            this,
                            conf);
                } catch (IllegalArgumentException e) {
                    throw new SFHadoopException("Bad File system URI"
                            + e,
                            e,
                            this,
                            conf);
                }
            }
            if (jobtrackerLive) {
                sfLog().info("Checking jobTracker ");
                JobTracker.State state = status.getJobTrackerState();
                if (!state.equals(JobTracker.State.RUNNING)) {
                    throw new SFHadoopException("Job Tracker at " + jobtracker
                            + " is not running. It is in the state " + state, this);
                }
                result.append("Job tracker is in state ").append(status);
            }
            checkRange(minActiveMapTasks, maxActiveMapTasks, status.getMapTasks(), "map task");
            checkRange(minActiveReduceTasks, maxActiveReduceTasks, status.getReduceTasks(), "reduce task");
            checkMax(maxSupportedMapTasks, status.getMaxMapTasks(), "supported max map task");
            checkMax(maxSupportedReduceTasks, status.getMaxReduceTasks(), "supported max reduce task");
            result.append(" Map Tasks = ").append(status.getMapTasks());
            result.append(" Reduce Tasks = ").append(status.getReduceTasks());
            return result.toString();
        } catch (IOException e) {
            throw new SFHadoopException("Cannot connect to" + jobtracker, e, this);
        }
    }


    /**
     * Check that the range is valid
     *
     * @param min    minimum value
     * @param max    maxiumum value
     * @param actual current value
     * @param field  field name
     * @throws SFHadoopException if we are out of range
     */
    private void checkRange(int min, int max, int actual, String field) throws SFHadoopException {
        checkMin(min, actual, field);
        checkMax(max, actual, field);
    }

    private void checkMin(int min, int actual, String field) throws SFHadoopException {
        if (min >= 0 && actual < min) {
            throw new SFHadoopException(field + " count too low - minimum " + min + " actual " + actual);
        }
    }

    private void checkMax(int max, int actual, String field) throws SFHadoopException {
        if (max >= 0 && actual > max) {
            throw new SFHadoopException(field + " count too high - maximum " + max + " actual " + actual);
        }
    }

    /**
     * Check that the cluster is reachable.
     *
     * @return true if it is successful, false if not
     * @throws RemoteException    for network problems
     * @throws SmartFrogException for any other problem
     */
    @Override
    public boolean evaluate() throws RemoteException, SmartFrogException {
        boolean live;
        String description;
        try {
            description = checkClusterStatus();
            live = true;
        } catch (SFHadoopException e) {
            setFailureCause(e);
            description = e.toString();
            sfLog().debug(e);
            live = false;
            
        }
        sfReplaceAttribute(IsHadoopServiceLive.ATTR_SERVICE_DESCRIPTION, description);
        return live;
    }
}
