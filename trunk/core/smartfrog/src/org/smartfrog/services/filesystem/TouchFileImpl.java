/** (C) Copyright 1998-2004 Hewlett-Packard Development Company, LP

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
package org.smartfrog.services.filesystem;

import org.smartfrog.sfcore.common.SmartFrogException;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import org.smartfrog.sfcore.utils.ComponentHelper;

/**
 * This is a minimal component whose aim in life is to touch files.
 * It has no life after starting, and no remote interface.
 * created 19-Apr-2004 13:57:24
 */

public class TouchFileImpl extends FileUsingComponentImpl implements TouchFileIntf {
    private long age=-1;

    /**
     * Constructor.
     * @throws RemoteException  In case of network/rmi error
     */
    public TouchFileImpl() throws RemoteException {
    }

    public synchronized void sfDeploy() throws SmartFrogException,
            RemoteException {
        super.sfDeploy();
        bind(true, "");
        age = sfResolve(ATTR_AGE, age, false);
    }

    /**
     * this is called at runtime
     *
     * @throws SmartFrogException error while starting
     * @throws RemoteException In case of network/rmi error
     */
    public synchronized void sfStart() throws SmartFrogException,
            RemoteException {
        super.sfStart();
        touch();
        new ComponentHelper(this).sfSelfDetachAndOrTerminate("normal","TouchFile "+getFile().getAbsolutePath()+", "+age,this.sfCompleteNameSafe(),null);
    }

    /**
     * extract the current values of the attributes, and then
     * touch the file
     * @throws RemoteException In case of network/rmi error
     * @throws SmartFrogException for IO error
     */
    public void touch() throws RemoteException, SmartFrogException {
        //get the file
        String file = getFile().getAbsolutePath();
        try {
            touch(file, age);
        } catch (IOException e) {
            throw SmartFrogException.forward(e);
        }
    }

    /**
     * touch a file
     *
     * @param filename file to create
     * @param age      timestamp (optional, use -1 for current time)
     * @throws IOException for IO error
     * @throws RemoteException In case of network/rmi error
     */
    public void touch(String filename, long age) throws IOException, RemoteException {
        File file = new File(filename);
        File parentFile = file.getParentFile();
        if(parentFile!=null) {
            parentFile.mkdirs();
        }
        file.createNewFile();
        if (age >= 0) {
            file.setLastModified(age);
        }
    }
}
