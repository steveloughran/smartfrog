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

import org.apache.hadoop.hdfs.server.namenode.ExtNameNode;
import org.mortbay.util.MultiException;
import org.smartfrog.services.filesystem.FileSystem;
import org.smartfrog.services.hadoop.components.cluster.FileSystemNode;
import org.smartfrog.services.hadoop.components.cluster.FileSystemNodeImpl;
import org.smartfrog.services.hadoop.components.cluster.ClusterManager;
import org.smartfrog.services.hadoop.conf.ManagedConfiguration;
import org.smartfrog.services.hadoop.core.SFHadoopException;
import org.smartfrog.sfcore.common.SmartFrogException;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

/**
 * Created 06-May-2008 16:31:49
 */

public class NamenodeImpl extends FileSystemNodeImpl implements
        FileSystemNode, ClusterManager {
    private static final String NAME = "NameNode";
    public static final String ERROR_NO_START = "Failed to start "+ NAME;
    private ExtNameNode nameNode;


    public NamenodeImpl() throws RemoteException {
    }

    /**
     * Return the name of this service; please override for better messages
     *
     * @return a name of the service for error messages
     */
    @Override
    protected String getName() {
        return NAME;
    }

    /**
     * Create the datanode
     *
     * @throws SmartFrogException failure while starting
     * @throws RemoteException    In case of network/rmi error
     */
    @Override
    public synchronized void sfStart()
            throws SmartFrogException, RemoteException {
        super.sfStart();
        createDirectoryListAttribute(DATA_DIRECTORIES, DFS_DATA_DIR);
        createDirectoryListAttribute(NAME_DIRECTORIES, DFS_NAME_DIR);
        File logDir = FileSystem.lookupAbsoluteFile(this,
                ATTR_LOG_DIR, null, null, true, null);
        logDir.mkdirs();
        sfReplaceAttribute(HADOOP_LOG_DIR, logDir.getAbsolutePath());
        ManagedConfiguration conf = createConfiguration();
        try {
            nameNode = ExtNameNode.create(this, conf);
        } catch (IOException e) {
            throw SFHadoopException.forward(ERROR_NO_START,
                    e,
                    this,
                    conf);
        } catch (IllegalArgumentException e) {
            throw SFHadoopException.forward(ERROR_NO_START,
                    e,
                    this,
                    conf);
        }
        deployService(nameNode, conf);
    }

    /**
     * Get the count of current workers
     *
     * @return 0 if not live, or the count of active workers
     * @throws RemoteException for network problems
     */
    public int getLiveWorkerCount() throws RemoteException {
        if (!isServiceLive()) {
            return 0;
        }
        return nameNode.getLiveWorkerCount();
    }
}