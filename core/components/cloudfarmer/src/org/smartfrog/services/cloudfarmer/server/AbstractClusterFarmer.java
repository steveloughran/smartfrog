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

package org.smartfrog.services.cloudfarmer.server;

import org.smartfrog.services.cloudfarmer.api.ClusterFarmer;
import org.smartfrog.services.cloudfarmer.api.ClusterRoleInfo;
import org.smartfrog.services.cloudfarmer.server.common.ClusterRole;
import org.smartfrog.sfcore.common.SmartFrogDeploymentException;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.common.SmartFrogResolutionException;
import org.smartfrog.sfcore.compound.CompoundImpl;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.reference.Reference;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Intermediate class for cluster farmer implementations contains any helper methods that they should be sharing.
 *
 * This class is a compound,
 */
public abstract class AbstractClusterFarmer extends CompoundImpl implements ClusterFarmer {

    /**
     * {@value}
     */
    public static final String WRONG_MACHINE_COUNT
            = "The maximum number of machines requested was less than the minimum";
    /**
     * {@value}
     */
    public static final String NEGATIVE_VALUES_NOT_SUPPORTED = "Negative values not supported";
    protected Map<String, ClusterRoleInfo> roleInfoMap = new HashMap<String, ClusterRoleInfo>();

    protected int clusterLimit = 1000;

    protected AbstractClusterFarmer() throws RemoteException {
    }


    /**
     * check the min and max arguments
     *
     * @param min minimum number of nodes desired
     * @param max maximumum number  desired
     * @throws SmartFrogDeploymentException if the parameters are somehow invalid
     */
    public static void validateClusterRange(int min, int max) throws SmartFrogDeploymentException {
        if (max < min) {
            throw new SmartFrogDeploymentException(WRONG_MACHINE_COUNT);
        }
        if (min < 0) {
            throw new SmartFrogDeploymentException(NEGATIVE_VALUES_NOT_SUPPORTED);
        }
    }

    /**
     * Stub method to stop breaking the build
     *
     * @return an empty list of role names
     * @throws IOException        IO/network problems
     * @throws SmartFrogException other problems
     */
    @Override
    public String[] listAvailableRoles() throws IOException, SmartFrogException {
        return new String[0];
    }

    /**
     * Stub method to stop breaking the build.
     *
     * @return alist of roles
     * @throws IOException        IO/network problems
     * @throws SmartFrogException other problems
     */
    @Override
    public ClusterRoleInfo[] listClusterRoles() throws IOException, SmartFrogException {
        return new ClusterRoleInfo[0];
    }

    public int getClusterLimit() {
        return clusterLimit;
    }

    protected void resolveClusterLimit() throws SmartFrogResolutionException, RemoteException {
        clusterLimit = sfResolve(ATTR_CLUSTER_LIMIT, clusterLimit, true);
    }

    /**
     * Run through the roles attribute and build a role map. For every role found, {@link #processRole(String,
     * ClusterRoleInfo)} is called after the role has been added to the map.
     *
     * @throws SmartFrogException SF problems
     * @throws RemoteException    network problems
     */
    protected void buildRoleMap() throws SmartFrogException, RemoteException {
        StringBuilder rolenames = new StringBuilder();
        Prim rolesChild = sfResolve(ATTR_ROLES, (Prim) null, true);
        Iterator attrs = rolesChild.sfAttributes();
        while (attrs.hasNext()) {
            Object key = attrs.next();
            String roleName = key.toString();
            Reference roleRef = new Reference(roleName);
            Object value = rolesChild.sfResolve(roleRef, true);
            if (value instanceof ClusterRole) {
                ClusterRole targetRole = (ClusterRole) value;
                ClusterRoleInfo roleInfo = targetRole.resolveRoleInfo(roleName);
                roleInfo = processRole(roleName, roleInfo);
                roleInfoMap.put(roleName, roleInfo);
                rolenames.append(roleName);
                rolenames.append(" ");
            } else {
                if (value instanceof Prim) {
                    throw new SmartFrogResolutionException(roleRef,
                            sfCompleteName,
                            "Expected a component implementing ClusterRole",
                            value);
                } else {
                    sfLog().debug("Ignoring roles attribute " + roleName + " which maps to " + value);
                }
            }
        }
        sfLog().info("Roles; " + rolenames);
    }

    /**
     * Override point: process a role. A new role info can be returned here, or parts of the configuration validated
     *
     * @param roleName name of the role
     * @param roleInfo information about the role
     * @return the role info passed in, or a newly createdone
     * @throws SmartFrogException SF problems
     * @throws RemoteException    network problems
     */
    protected ClusterRoleInfo processRole(String roleName, ClusterRoleInfo roleInfo)
            throws SmartFrogException, RemoteException {
        return roleInfo;
    }


}
