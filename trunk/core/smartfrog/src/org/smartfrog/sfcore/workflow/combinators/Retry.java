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

package org.smartfrog.sfcore.workflow.combinators;

import java.rmi.RemoteException;

import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.componentdescription.ComponentDescription;
import org.smartfrog.sfcore.compound.Compound;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.prim.TerminationRecord;
import org.smartfrog.sfcore.reference.Reference;
import org.smartfrog.sfcore.workflow.eventbus.EventCompoundImpl;


/**
 * Retry is a modified compound which differs in that its single sub-component
 * is created and should it terminate abnormally, is recreated. This is
 * repeated a number of times or until the sub-component suceeds. A Retry
 * combinator creates no subcomponents until it's sfStart phase at which point
 * all the subcomponent is created in the normal way. The Retry combinator
 * waits for its sub-component to terminate normally at which point it too
 * terminates normally. If an error occurs at any point, or a sub-component is
 * retried unless a limit is reached in which case it too terminates
 * abnormall.
 *
 * <p>
 * The file retry.sf contains the SmartFrog configuration file for the base
 * Retry combinator. This file conatins the details of the attributes which
 * may be passed to Retry.
 * </p>
 */
public class Retry extends EventCompoundImpl implements Compound {
    static Reference actionRef = new Reference("action");
    static Reference retryRef = new Reference("retry");
    ComponentDescription action;
    int retry;
    Reference name;
    int currentRetries = 0;

    /**
     * Constructs Retry.
     *
     * @throws java.rmi.RemoteException In case of RMI or network error.
     */
    public Retry() throws java.rmi.RemoteException {
        super();
    }

    /**
     * Reads the basic configuration of the component and deploys it.
     *
     * @throws RemoteException In case of network/rmi error
     * @throws SmartFrogException In case of any error while
     *         deploying the component
     */
    public synchronized void sfDeploy() throws SmartFrogException, RemoteException {
        super.sfDeploy();
        action = (ComponentDescription) sfResolve(actionRef);
        retry = ((Integer) sfResolve(retryRef)).intValue();
        name = sfCompleteNameSafe();
    }

    /**
     * Starts the component and starts subcomponents.
     * Overrides CompoundImpl.sfStart.
     *
     * @throws RemoteException The required remote exception.
     * @throws RemoteException In case of network/rmi error
     */
    public synchronized void sfStart() throws SmartFrogException, RemoteException {
        super.sfStart();

        sfCreateNewChild(name+"_actionRunning", 
			 (ComponentDescription) action.copy(), null);
    }

    /**
     * Terminates the component. This is invoked by sub-components on
     * termination. If normal termination, Retry behaviour is to terminate
     * normally. If an abnormal termination - retry unless some limit is
     * reached, in which case terminate abnormally.

     * @param status termination status of sender
     * @param comp sender of termination
     */
    public void sfTerminatedWith(TerminationRecord status, Prim comp) {
        if (sfContainsChild(comp)) {
            try {
                sfRemoveChild(comp);

                if (!(status.errorType.equals("normal".intern()))) {
                    if (currentRetries++ < retry) {
                        sfCreateNewChild(name+"_actionRunning"+currentRetries,
					 (ComponentDescription) action.copy(), null);
                    } else {
                        //System.out.println("terminated incorrectly: too many reties - fail " + name.toString());
                        sfTerminate(TerminationRecord.abnormal(
                                "too many retries...", name));
                    }
                } else {
                    //System.out.println("terminated correctly - no need to retry " + name.toString());
                    sfTerminate(TerminationRecord.normal(name));
                }
            } catch (Exception e) {
                sfTerminate(TerminationRecord.abnormal(
                        "error in restarting next component", name));
            }
        }
    }
}
