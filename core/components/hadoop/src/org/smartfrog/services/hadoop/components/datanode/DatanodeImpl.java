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
package org.smartfrog.services.hadoop.components.datanode;

import org.apache.hadoop.dfs.ExtDataNode;
import org.smartfrog.services.filesystem.FileSystem;
import org.smartfrog.services.hadoop.components.HadoopCluster;
import org.smartfrog.services.hadoop.components.cluster.FileSystemNodeImpl;
import org.smartfrog.services.hadoop.conf.ManagedConfiguration;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.common.SmartFrogLivenessException;
import org.smartfrog.sfcore.prim.Liveness;
import org.smartfrog.sfcore.prim.TerminationRecord;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Vector;

/**
 * Created 06-May-2008 16:31:49
 */

public class DatanodeImpl extends FileSystemNodeImpl implements HadoopCluster {
    private ExtDataNode datanode;
    public static final String ERROR_FAILED_TO_START_DATANODE = "Failed to start datanode: ";

    public DatanodeImpl() throws RemoteException {
    }

    /**
     * Create the datanode
     *
     * @throws SmartFrogException failure while starting
     * @throws RemoteException    In case of network/rmi error
     */
    public synchronized void sfStart() throws SmartFrogException, RemoteException {
        super.sfStart();
        Vector<String> dataDirs = createDirectoryListAttribute(DATA_DIRECTORIES, DFS_DATA_DIR);
        Vector<File> dataDirFiles = FileSystem.convertToFiles(dataDirs);
        ManagedConfiguration conf = createConfiguration();
        try {
            datanode = new ExtDataNode(this,conf,dataDirFiles);
            datanode.start();
        } catch (IOException e) {
            shutDownDataNode();
            throw new SmartFrogException(ERROR_FAILED_TO_START_DATANODE
                    + e.getMessage() + '\n' + conf.dumpQuietly(), e);
        } catch (IllegalArgumentException e) {
            shutDownDataNode();
            throw new SmartFrogException(ERROR_FAILED_TO_START_DATANODE
                    + e.getMessage() + "\n" + conf.dumpQuietly(), e);
        }
    }

    /**
     * Provides hook for subclasses to implement useful termination behavior.
     * Deregisters component from local process compound (if ever registered)
     *
     * @param status termination status
     */
    protected synchronized void sfTerminateWith(TerminationRecord status) {
        super.sfTerminateWith(status);
        shutDownDataNode();
    }

    private synchronized void shutDownDataNode() {
        if (datanode != null) {
            datanode.shutdown();
            datanode = null;
        }
    }

    /**
     * Liveness call in to check if this component is still alive.
     * @param source source of call
     *
     * @throws SmartFrogLivenessException component is terminated
     * @throws RemoteException for consistency with the {@link Liveness}
     * interface
     */
    public void sfPing(Object source)
            throws SmartFrogLivenessException, RemoteException {
        super.sfPing(source);
        if (datanode != null) {
            //there's no health check here, so no way to see what is going on.
        } else {
            throw new SmartFrogLivenessException("No running data node");
        }
    }
}
