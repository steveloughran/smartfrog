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


package org.smartfrog.services.hadoop.components.dfs;

import org.apache.hadoop.fs.FileSystem;
import org.smartfrog.services.hadoop.common.DfsUtils;
import org.smartfrog.services.hadoop.conf.ManagedConfiguration;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.utils.WorkflowThread;

import java.rmi.RemoteException;

/**
 * Base class for DFS operations does nothing useful at all other than resolve the cluster settings and fail if they are
 * absent. It also has support for a worker thread (which get terminated during shutdown, if set)
 */
public abstract class DfsOperationImpl extends DfsClusterBoundImpl implements DfsOperation {
    protected boolean closeFilesystem;


    protected DfsOperationImpl() throws RemoteException {
    }


    /**
     * start up, bind to the cluster
     *
     * @throws SmartFrogException failure while starting
     * @throws RemoteException    In case of network/rmi error
     */
    @Override
    public void sfStart() throws SmartFrogException, RemoteException {
        super.sfStart();
        closeFilesystem = sfResolve(ATTR_CLOSE_FILESYSTEM, true, true);
    }

    /**
     * For subclassing: this routine will be called by the default worker thread, if that thread gets started
     *
     * @param fileSystem the filesystem; this is closed afterwards
     * @param conf       the configuration driving this operation
     * @throws Exception on any failure
     */
    protected void performDfsOperation(FileSystem fileSystem, ManagedConfiguration conf)
            throws Exception {

    }

    /**
     * do the work
     *
     * @throws Exception on any failure
     */
    protected void performDfsOperation() throws Exception {
        ManagedConfiguration conf = createConfiguration();
        FileSystem fileSystem = DfsUtils.createFileSystem(conf);
        
        try {
            performDfsOperation(fileSystem, conf);
        } catch (Exception e) {
            if(closeFilesystem) {
                DfsUtils.closeQuietly(fileSystem);
            }
            throw e;
        }
        if (closeFilesystem) {
            DfsUtils.closeDfs(fileSystem);
        }
    }

    /**
     * Create a worker thread.
     */
    protected synchronized void startWorkerThread() {
        startWorkerThread(true);
    }

    /**
     * Create a worker thread.
     *
     * @param workflowTermination is workflow termination expected?
     */
    protected synchronized void startWorkerThread(boolean workflowTermination) {
        assert getWorker() == null : "A worker thread has already been created";
        setWorker(new DfsWorkerThread(workflowTermination));
        getWorker().start();
    }


    /**
     * This is a worker thread that optionally can be started by the DfsOperationImpl subclass, in which case it calls
     * back to do useful work.
     */
    protected class DfsWorkerThread extends WorkflowThread {

        /**
         * Create a worker thread. Notification is bound to a local notification object.
         *
         * @param workflowTermination is workflow termination expected?
         */
        protected DfsWorkerThread(boolean workflowTermination) {
            super(DfsOperationImpl.this, workflowTermination);
        }

        /**
         * Call back into the {@link DfsOperationImpl#performDfsOperation()} operation of the owner class; that is where
         * the work should be implemented.
         *
         * @throws Throwable if anything went wrong
         */
        @SuppressWarnings({"RefusedBequest"})
        public void execute() throws Throwable {
            performDfsOperation();
        }
    }
}
