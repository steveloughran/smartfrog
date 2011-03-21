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

package org.smartfrog.sfcore.processcompound;

import org.smartfrog.sfcore.common.SmartFrogCoreKeys;
import org.smartfrog.sfcore.common.SmartFrogDeploymentException;
import org.smartfrog.sfcore.common.SmartFrogResolutionException;
import org.smartfrog.sfcore.componentdescription.ComponentDescription;
import org.smartfrog.sfcore.logging.LogFactory;
import org.smartfrog.sfcore.logging.LogSF;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.prim.PrimDeployerImpl;
import org.smartfrog.sfcore.reference.Reference;

import java.net.InetAddress;
import java.rmi.ConnectException;

/**
 * Implements a specialized description deployer. This deployer uses the
 * sfProcessHost and sfRootLocatorPort attribute to locate the appropriate
 * remote ProcessCompound to forward descriptions to. Also registers the
 * component with the processcompound after deployment. If
 * sfProcessComponentName is specified in the target the component is
 * registered with that name. Otherwise the registration is called with null,
 * causing a name to be made up.
 *
 */
public class PrimHostDeployerImpl extends PrimDeployerImpl {

    /** ProcessLog. This log is used to log into the core log: SF_CORE_LOG
     * Although unused, it forces the logging to be set up, so must not be removed
     * */
    private LogSF sflog = LogFactory.sfGetProcessLog();

    /** Efficiency holder of sfProcessHost attribute. */
    protected static final Reference refProcessHost =
            new Reference(SmartFrogCoreKeys.SF_PROCESS_HOST);

    /** Efficiency holder of sfRootLocatorPort attribute. */
    protected static final Reference refRootLocatorPort =
            new Reference(SmartFrogCoreKeys.SF_ROOT_LOCATOR_PORT);

    /**
     * Constructs the PrimHostDeployerImpl with ComponentDescription.
     *
     * @param descr target to operate on
     */
    public PrimHostDeployerImpl(ComponentDescription descr) {
        super(descr);
    }

    /**
     * Returns the process compound on a particular host and with a particular
     * process name. "sfProcessHost" is used to determine the host to use to
     * locate the root process compound on that host. If the process host is
     * not specified the local process compound is returned.
     *
     * @return ProcessCompound on host with name
     *
     * @throws Exception if failed to find process compound
     */
    protected ProcessCompound getProcessCompound() throws Exception {
        InetAddress hostAddress = null;
        Object hostname = null;
        try {
            hostname = target.sfResolve(refProcessHost);
            if (hostname instanceof String) {
                hostAddress = InetAddress.getByName((String) hostname);
            } else if (hostname instanceof InetAddress) {
                hostAddress = (InetAddress) hostname;
            } else {
                Object name = null;
                if (target.sfContext().containsKey(SmartFrogCoreKeys.SF_PROCESS_COMPONENT_NAME)) {
                    name = target.sfResolveHere(SmartFrogCoreKeys.SF_PROCESS_COMPONENT_NAME, false);
                }
                throw new SmartFrogDeploymentException(refProcessHost, null, name, target, null,
                        "illegal sfProcessHost class: found " + hostname + ", of class " + hostname
                                .getClass(), null,
                        hostname);
            }
        } catch (SmartFrogResolutionException resex) {
            return SFProcess.getProcessCompound();
        } catch (java.net.UnknownHostException unhex) {
            Object name = null;
            if (target.sfContext().containsKey(SmartFrogCoreKeys.SF_PROCESS_COMPONENT_NAME)) {
                name = target.sfResolveHere(SmartFrogCoreKeys.SF_PROCESS_COMPONENT_NAME, false);
            }
            throw new SmartFrogDeploymentException(refProcessHost, null, name, target, null,
                    "Unknown host: " + hostname, unhex, hostname);
        }

        return SFProcess.getRootLocator().getRootProcessCompound(hostAddress);
    }

    /**
     * Overrides superclass behaviour to forward description to another process
     * based on sfProcessHost attribute.
     *
     * @param parent parent for deployed component
     *
     * @return The Component Reference after it gets deployed
     *
     * @throws SmartFrogDeploymentException if failed to deploy target
     */
    protected Prim deploy(Prim parent)
            throws SmartFrogDeploymentException {
        String targetProcessName = "(unknown process)";
        ProcessCompound targetProcess = null;
        ProcessCompound local;
        String stage= "";
        try {
            stage = "retrieving local process compound";
            local = SFProcess.getProcessCompound();
            stage = "retrieving target process compound";
            targetProcess = getProcessCompound();
            targetProcessName = targetProcess.toString();
            stage = "determining process name of target process " + targetProcessName;
            targetProcessName = targetProcess.sfProcessName() + " [" + targetProcess.toString() + "]";
            if (targetProcess.equals(local)) {
                targetProcessName = "local process "+ targetProcessName;
                if (parent == null) {
                    stage ="deploying to the root of the local process";
                    return local.sfDeployComponentDescription(null, null, target, null);
                } else {
                    stage = "deploying under the parent node";
                    return super.deploy(parent);
                }
            } else {
                stage = "deploying remotely to " + targetProcessName;
                return targetProcess.sfDeployComponentDescription(null, parent, target, null);
            }
        } catch (ConnectException ex) {
            //failure 
            throw (SmartFrogDeploymentException) SmartFrogDeploymentException
                    .forward("PrimHostDeployerImpl.deploy failed to connect to " + targetProcessName
                            + " while " + stage,
                            ex);
        } catch (Exception ex) {
            throw (SmartFrogDeploymentException) SmartFrogDeploymentException
                    .forward("PrimHostDeployerImpl.deploy failed to deploy to " + targetProcessName
                            + " while " + stage,
                            ex);
        }
    }
}
