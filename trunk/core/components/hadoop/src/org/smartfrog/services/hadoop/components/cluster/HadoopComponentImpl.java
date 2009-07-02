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

import org.smartfrog.services.filesystem.FileSystem;
import org.smartfrog.services.hadoop.conf.ClusterBound;
import org.smartfrog.services.hadoop.conf.ManagedConfiguration;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.common.SmartFrogResolutionException;
import org.smartfrog.sfcore.logging.LogFactory;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.prim.PrimImpl;
import org.smartfrog.sfcore.reference.Reference;

import java.io.File;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.util.Vector;

/**
 * A base class for hadoop components. It does not export any remote interface and is not very interesting on its own.
 * Created 19-May-2008 14:29:07
 */

public class HadoopComponentImpl extends PrimImpl /* EventCompoundImpl */ implements ClusterBound {

    public HadoopComponentImpl() throws RemoteException {
    }

    /**
     * Create and dump the configuration on startup
     * @throws SmartFrogResolutionException resolution failure
     * @throws RemoteException              network problems
     */
    protected void dumpConfiguration() throws SmartFrogException, RemoteException {
        if (sfLog().isDebugEnabled()) {
            ManagedConfiguration configuration;
            configuration = createConfiguration();
            debugDumpConfiguration(configuration);
        }
    }


    /**
     * Dump the configuration
     * @param configuration to dump
     *
     */
    protected void debugDumpConfiguration(ManagedConfiguration configuration) {
        if (sfLog().isDebugEnabled()) {
            sfLog().debug(configuration.dump());
        }
    }

    /**
     * create a configuration against ourselves.
     *
     * @return the new configuration
     * @throws SmartFrogResolutionException resolution failure
     * @throws RemoteException              network problems     */
    protected ManagedConfiguration createConfiguration() throws SmartFrogException, RemoteException {
        return createConfiguration(this);
    }

    /**
     * Create a managed configuration against a different component
     *
     * @param target target component
     * @return the target configuration
     * @throws SmartFrogResolutionException resolution failure
     * @throws RemoteException              network problems     */
    private ManagedConfiguration createConfiguration(Prim target) throws SmartFrogException, RemoteException {
        boolean clusterRequired = sfResolve(ATTR_CLUSTER_REQUIRED, false, false);
        return ManagedConfiguration.createConfiguration(target, true, clusterRequired, false);
    }


    /**
     * Create a managed configuration against a different component, one identified by an attribute
     *
     * @return the target configuration
     * @throws SmartFrogResolutionException resolution failure
     * @throws RemoteException              network problems
     */
    protected ManagedConfiguration createClusterAttrConfiguration()
            throws SmartFrogException, RemoteException {
        return ManagedConfiguration.createConfiguration(this, true, true, false);
    }

    /**
     * Run through the directories, create all that are there
     *
     * @param dirs       list of directories
     * @param createDirs create the directories?
     * @return the directories all converted to a list split by commas
     */
    public static String createDirectoryList(Vector<String> dirs, boolean createDirs) {
        StringBuilder path = new StringBuilder();
        for (String dir : dirs) {
            File directory = new File(dir);
            if (createDirs) {
                if(!directory.mkdirs() && !directory.exists()) {
                    LogFactory.getLog(HadoopComponentImpl.class.getName())
                            .warn("Failed to create directory " + directory);
                }
            }
            if (path.length() > 0) {
                path.append(',');
            }
            path.append(directory.getAbsolutePath());
        }
        return path.toString();
    }


    /**
     * Go from a list of paths/fileIntfs to a comma separated list, create directories on demand
     *
     * @param prim             the component to work with
     * @param sourceRef        source reference
     * @param replaceAttribute attribute to replace
     * @return the directories
     * @throws SmartFrogException failure while starting
     * @throws RemoteException    In case of network/rmi error
     */
    public static Vector<String> createDirectoryListAttribute(Prim prim, Reference sourceRef,
                                                              String replaceAttribute)
            throws RemoteException, SmartFrogException {
        Vector<String> dirs;
        dirs = FileSystem.resolveFileList(prim, sourceRef, null, true, null);
        String value = createDirectoryList(dirs, true);
        prim.sfReplaceAttribute(replaceAttribute, value);
        return dirs;
    }

    /**
     * Go from a list of paths/fileIntfs to a comma separated list, create directories on demand
     *
     * @param sourceRef        source reference
     * @param replaceAttribute attribute to replace
     * @return the directories
     * @throws SmartFrogException failure while starting
     * @throws RemoteException    In case of network/rmi error
     */
    protected Vector<String> createDirectoryListAttribute(Reference sourceRef,
                                                          String replaceAttribute)
            throws RemoteException, SmartFrogException {
        return createDirectoryListAttribute(this, sourceRef, replaceAttribute);
    }

    /**
     * Resolve an attribute that names the address attribute to use
     * @param configuration configuration to work with
     * @param addressAttr name of an attribute that identifies the underlying configuration attribute to work with
     * @return a socket address
     * @throws SmartFrogResolutionException for resolution problems
     * @throws RemoteException network problems
     */
    protected InetSocketAddress resolveAddressIndirectly(ManagedConfiguration configuration, String addressAttr)
            throws SmartFrogResolutionException, RemoteException {
        String addressAttribute = sfResolve(addressAttr, "", true);
        if (addressAttr == null) {
            throw new SmartFrogResolutionException("Null attribute " + addressAttr);
        }

        return resolveAddress(configuration, addressAttribute);
    }

    /**
     * Given an a conf and an attribute, resolve it and build the address
     * @param configuration configuration
     * @param addressAttribute attribute to look up
     * @return a bound address
     * @throws SmartFrogResolutionException for resolution problems
     * @throws RemoteException network problems
     */
    protected InetSocketAddress resolveAddress(ManagedConfiguration configuration, String addressAttribute)
            throws SmartFrogResolutionException, RemoteException {
      return configuration.bindToNetwork(addressAttribute);
    }

    protected PortEntry resolvePortEntry(ManagedConfiguration configuration,String addressAttribute)
            throws SmartFrogResolutionException, RemoteException {
        return new PortEntry(addressAttribute,
            resolveAddress(configuration, addressAttribute));
    }
}
