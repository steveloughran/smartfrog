/** (C) Copyright 2005 Hewlett-Packard Development Company, LP

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
package org.smartfrog.services.os.java;

import org.smartfrog.services.filesystem.FileSystem;
import org.smartfrog.services.filesystem.FileUsingComponentImpl;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.common.SmartFrogResolutionException;
import org.smartfrog.sfcore.common.SmartFrogLifecycleException;
import org.smartfrog.sfcore.common.SmartFrogRuntimeException;
import org.smartfrog.sfcore.compound.CompoundImpl;
import org.smartfrog.sfcore.logging.Log;
import org.smartfrog.sfcore.utils.PlatformHelper;

import java.io.File;
import java.rmi.RemoteException;

/**
 * Implementation of libraries logic
 * created 04-Apr-2005 14:14:30
 */

public class LibraryImpl extends FileUsingComponentImpl implements Library {

    /**
     * we are not a directory
     */
    public static final String ERROR_NOT_A_DIRECTORY = "Cache directory is not a directory: ";

    /**
     * cache directory
     */
    private File cacheDir;
    
    /**
     * flag to tell us whether to collect JARs to subdirs or not
     */
    private boolean flatten=false;
    
    private Log log;

    public LibraryImpl() throws RemoteException {
    }

    /**
     * deployment: validate and create
     * @throws SmartFrogException
     * @throws RemoteException
     */
    public synchronized void sfDeploy() throws SmartFrogException,
            RemoteException {
        super.sfDeploy();
        log=sfGetApplicationLog();
        flatten=sfResolve(ATTR_FLATTEN,flatten,true);
        //bind our directory
        bindDirectory();
    }

    /**
     * bind our cache directory information
     * @throws RemoteException
     * @throws SmartFrogRuntimeException
     */
    private void bindDirectory() throws RemoteException,
            SmartFrogRuntimeException {
        String cacheDirname=FileSystem.lookupAbsolutePath(this,ATTR_CACHE_DIR,null,null,true,null);
        cacheDir = new File(cacheDirname);
        cacheDir.mkdirs();
        if(!cacheDir.isDirectory()) {
            throw new SmartFrogResolutionException(ERROR_NOT_A_DIRECTORY+cacheDir,this);
        }
        //set up cache information
        bind(cacheDir);
    }


    /**
     * @see org.smartfrog.services.os.java.Library#determineArtifactPath(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public String determineArtifactPath(String project, String artifact, String version, String extension) throws RemoteException {
        assert project!=null;
        assert artifact!=null;
        String path="/";
        if(!flatten) {
            path+=LibraryHelper.patchProject(project)+"/";
        } 
        String name=LibraryHelper.createArtifactName(artifact,version,extension);
        path+=name;
        PlatformHelper helper=PlatformHelper.getLocalPlatform();
        String localpath=helper.convertFilename(path);
        File file=new File(cacheDir,localpath);
        return file.getAbsolutePath();
    }
    
    


}
