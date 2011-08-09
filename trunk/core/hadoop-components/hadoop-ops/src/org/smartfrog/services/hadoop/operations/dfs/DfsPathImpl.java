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
package org.smartfrog.services.hadoop.operations.dfs;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.smartfrog.services.hadoop.operations.conf.ManagedConfiguration;
import org.smartfrog.sfcore.common.SmartFrogException;

import java.rmi.RemoteException;

/**
 * Component to copy a file into DFS Created 17-Jun-2008 15:06:23
 */

public class DfsPathImpl extends DfsPathOperationImpl implements DfsPathOperation {

    public DfsPathImpl() throws RemoteException {
    }

    /**
     * Can be called to start components. Subclasses should override to provide functionality Do not block in this call,
     * but spawn off any main loops!
     *
     * @throws SmartFrogException failure while starting
     * @throws RemoteException    In case of network/rmi error
     */
    public synchronized void sfStart() throws SmartFrogException, RemoteException {
        super.sfStart();
        startWorkerThread();
    }

    /**
     * do the work
     *
     * @param fileSystem the filesystem; this is closed afterwards
     * @param conf       the configuration driving this operation
     * @throws Exception on any failure
     */
    @Override
    protected void performDfsOperation(FileSystem fileSystem, ManagedConfiguration conf) throws Exception {
        String text = sfResolve("text", "", true);
        Path path = getPath();
        boolean overwrite = sfResolve(DfsCopyOperation.ATTR_OVERWRITE, true, true);
        FSDataOutputStream out = null;
        try {
            out = fileSystem.create(path, overwrite);
            if (text.length() > 0) {
                out.writeChars(text);
            }
        } finally {
            if (out != null) {
                out.close();
            }
        }
        sfLog().info("Created file " + path.toString());
    }
}