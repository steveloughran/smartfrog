/** (C) Copyright 2007 Hewlett-Packard Development Company, LP

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
package org.smartfrog.services.assertions.lifecycle;

import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.common.ContextImpl;
import org.smartfrog.sfcore.common.SmartFrogResolutionException;
import org.smartfrog.sfcore.common.SmartFrogLivenessException;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.componentdescription.ComponentDescription;
import org.smartfrog.sfcore.utils.ComponentHelper;
import org.smartfrog.services.assertions.TestBlockImpl;
import org.smartfrog.services.assertions.TestBlock;

import java.rmi.RemoteException;

/**
 *
 * This component tests the lifecycle of other components by calling some of their methods out of cycle
 *
 */

public class LifecycleTesterImpl extends TestBlockImpl implements TestBlock, LifecycleTester {



    public LifecycleTesterImpl() throws RemoteException {
    }


    /**
     * Called in sfStart to start the child action.
     * Overridden to disable that action
     *
     * @throws SmartFrogException failed to start compound
     * @throws RemoteException In case of Remote/nework error
     */
    @Override
    protected void startChildAction() throws RemoteException, SmartFrogException {
        //do not call the super, as we want to start ourselves, differently.

    }


    /**
     * Starts the component.
     * This will walk the action through its lifecycle, terminating it before it is expected
     *
     * A TestStartedEvent will always be sent.
     *
     * @throws SmartFrogException failed to start compound
     * @throws RemoteException In case of Remote/nework error
     */
    @Override
    public synchronized void sfStart() throws SmartFrogException, RemoteException {
        super.sfStart();
        if (sfLog().isDebugEnabled()) sfLog().debug("Deploying action child");
        //now start the child
        ComponentDescription cd = null;
        cd = sfResolve(ATTR_ACTION, cd, true);

        boolean terminateBeforeDeploy = sfResolve(ATTR_TERMINATE_BEFORE_DEPLOY, true, true);
        boolean terminateBeforeStart = sfResolve(ATTR_TERMINATE_BEFORE_START, true, true);

        Prim child = sfDeployComponentDescription(ATTR_ACTION, this,
                (ComponentDescription) cd.copy(), new ContextImpl());
        //child is instantiated, but not deployed.
        ComponentHelper childHelper = new ComponentHelper(this);

        //first ping
        pingIfRequested(child, ATTR_PING_BEFORE_DEPLOY);
        if (!terminateBeforeDeploy) {
            //deploy the child
            if (sfLog().isDebugEnabled()) sfLog().debug("Deploying action child");
            child.sfDeploy();
            //second ping
            pingIfRequested(child, ATTR_PING_BEFORE_START);

            if (!terminateBeforeStart) {
                //start the child unless we want to terminate first
                if (sfLog().isDebugEnabled()) sfLog().debug("Action is deployed, starting it");
                child.sfStart();
            }
            //and shut ourselves down afterwards
        } else {
            if (sfLog().isDebugEnabled()) sfLog().debug("Child is to be terminated before deployment");
        }
        if (sfLog().isDebugEnabled()) sfLog().debug("Terminating the child");
        childHelper.targetForTermination();
    }

    private void pingIfRequested(Prim child, String pingAttr) throws SmartFrogResolutionException,
            RemoteException, SmartFrogLivenessException {
        if (sfResolve(pingAttr, true, true)) {
            if (sfLog().isDebugEnabled()) sfLog().debug("Pinging child");
            child.sfPing(this);
        }
    }


}
