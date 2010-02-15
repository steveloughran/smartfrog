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
package org.smartfrog.services.hadoop.components.namenode;

import org.apache.hadoop.hdfs.server.namenode.ExtDfsUtils;
import org.smartfrog.services.filesystem.FileSystem;
import org.smartfrog.services.hadoop.components.cluster.FileSystemNode;
import org.smartfrog.services.hadoop.components.cluster.FileSystemNodeImpl;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.prim.TerminationRecord;
import org.smartfrog.sfcore.utils.WorkflowThread;

import java.io.File;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.util.Vector;

/**
 * This component formats a filesystem in a worker thread then optionally terminates.
 * It does not have an associated service
 */

public class FormatImpl extends FileSystemNodeImpl implements FileSystemNode {
    private Vector<File> nameDirs;
    private WorkflowThread worker;

    public FormatImpl() throws RemoteException {
    }

    /**
     * {@inheritDoc}.
     *
     * @return false, always
     */
    @Override
    protected boolean requireNonNullServiceInPing() {
        return false;
    }

    /**
     * Return the name of this service; please override for better messages
     *
     * @return a name of the service for error messages
     */
    @Override
    protected String getServiceName() {
        return "formatter";
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
        try {
            nameDirs = FileSystem.convertToFiles(createDirectoryListAttribute(NAME_DIRECTORIES, DFS_NAME_DIR));
        } catch (FileNotFoundException e) {
            throw new SmartFrogException(e);
        }
        worker = new Formatter();
        worker.start();
    }

    /**
     * Provides hook for subclasses to implement useful termination behavior. Deregisters component from local process
     * compound (if ever registered)
     *
     * @param status termination status
     */
    @Override
    protected synchronized void sfTerminateWith(TerminationRecord status) {
        super.sfTerminateWith(status);
        WorkflowThread.requestThreadTermination(worker);
    }

    /**
     * This is the formatter
     */
    private class Formatter extends WorkflowThread {

        /**
         * Create a basic thread. Notification is bound to a local notification object.
         */
        private Formatter() {
            super(FormatImpl.this, true);
        }


        /**
         * If this thread was constructed using a separate {@link Runnable} run object, then that <code>Runnable</code>
         * object's <code>run</code> method is called; otherwise, this method does nothing and returns. <p> Subclasses
         * of <code>Thread</code> should override this method.
         *
         * @throws Throwable if anything went wrong
         */
        @Override
        public void execute() throws Throwable {
            ExtDfsUtils.formatNameNode(nameDirs, createConfiguration());
        }

    }
}
