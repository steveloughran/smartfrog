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

import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.common.SmartFrogDeploymentException;
import org.smartfrog.sfcore.common.SmartFrogLivenessException;
import org.smartfrog.sfcore.prim.PrimImpl;
import org.smartfrog.sfcore.prim.Liveness;
import org.smartfrog.sfcore.utils.ComponentHelper;
import static org.smartfrog.services.filesystem.FileSystem.*;

import java.rmi.RemoteException;
import java.io.File;
import java.util.List;

/**
 * Java6+ code to check for disk space
 */

public class CheckDiskSpaceImpl extends PrimImpl implements CheckDiskSpace {

    public long requiredSpace;
    public List<File> directories;
    private boolean skipAbsentDirectories;
    private boolean checkOnLiveness;
    private boolean checkOnStartup;
    private static long MB = 1024 * 1024;
    public static final String ERROR_NO_DIRECTORY = "Directory does not exist:";
    public static final String ERROR_NOT_ENOUGH_SPACE = "Not enough space in ";

    public CheckDiskSpaceImpl() throws RemoteException {
    }


    /**
     * Startup.
     *
     * @throws SmartFrogException failure while starting
     * @throws RemoteException    In case of network/rmi error
     */
    public synchronized void sfStart() throws SmartFrogException, RemoteException {
        super.sfStart();
        directories = convertToFiles(resolveFileList(this, ATTR_DIRECTORIES, null, true, null));
        skipAbsentDirectories = sfResolve(ATTR_SKIPABSENTDIRECTORIES, false, true);
        checkOnLiveness = sfResolve(ATTR_CHECKONLIVENESS, false, true);
        checkOnStartup = sfResolve(ATTR_CHECKONLIVENESS, false, true);
        int minAvailableGB = sfResolve(ATTR_MINAVAILABLEGB, 0, true);
        int minAvailableMB = sfResolve(ATTR_MINAVAILABLEMB, 0, true);
        requiredSpace = ((minAvailableGB * 1024L) + minAvailableMB) * MB;
        if (checkOnStartup) {
            String error = checkDiskSpace();
            if (error != null) {
                throw new SmartFrogDeploymentException(error, this);
            }
        }
        new ComponentHelper(this).sfSelfDetachAndOrTerminate(null, null, sfCompleteName(), null);
    }

    /**
     * Liveness call in to check if this component is still alive.
     *
     * @param source source of call
     * @throws SmartFrogLivenessException component is terminated
     * @throws RemoteException            for consistency with the {@link Liveness} interface
     */
    public void sfPing(Object source) throws SmartFrogLivenessException, RemoteException {
        super.sfPing(source);
        if (checkOnLiveness) {
            String error = checkDiskSpace();
            if (error != null) {
                throw new SmartFrogLivenessException(error, this);
            }
        }
    }

    String checkDiskSpace() {
        for (File dir : directories) {
            if (!dir.exists()) {
                if (!skipAbsentDirectories) {
                    return ERROR_NO_DIRECTORY + dir;
                }
            } else {
                long space = dir.getUsableSpace();
                if (space < requiredSpace) {
                    long spaceMB = space / MB;
                    return ERROR_NOT_ENOUGH_SPACE + dir + " only " + spaceMB + " MB space available";
                }
            }
        }
        return null;
    }

    /**
     * Evaluate the condition.
     *
     * @return true if it is successful, false if not
     * @throws RemoteException    for network problems
     * @throws SmartFrogException for any other problem
     */
    public boolean evaluate() throws RemoteException, SmartFrogException {
        return checkDiskSpace() == null;
    }
}
