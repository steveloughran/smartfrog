/** (C) Copyright 1998-2006 Hewlett-Packard Development Company, LP

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
import java.util.Enumeration;

import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.common.SmartFrogLifecycleException;
import org.smartfrog.sfcore.compound.CompoundImpl;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.prim.TerminationRecord;
import org.smartfrog.sfcore.reference.Reference;


/**
 * Implements the detaching compound component behavior. A detaching compound
 * deploys component descriptions, and may detach them from its liveness after
 * succesfull deployment, or detach itself from its parent. After this, the
 * component can be set to terminate itself.
 *
 */
public class DetachingCompoundImpl extends CompoundImpl implements DetachingCompound {

    /**
     * Name of the component.
     */
    Reference name = null;
    /**
     * Set to true if you want the compound to detach its children on start
     */
    boolean detachDownwards;
    /**
     * Set to true if you want the compound to detach itself on start
     */
    boolean detachUpwards;
    /**
     * Set to true if you want the compound to terminate at the end of the
     */
    boolean autoDestruct;
    /**
     * Flag to indicate terminate is called.
     */
    boolean terminateCalled = false;
    /**
     * A thread for lifecycle operations during the start phase
     */
    Thread detacher;

    //  boolean normalDeath = true;
    /**
     * Constructs DetachingCompoundImpl.
     * @throws RemoteException if there is any network or RMI error
     */
    public DetachingCompoundImpl() throws RemoteException {
    }

    /**
     * Collects the values of the behavior booleans on the deploy phase and
     * deploys the component.
     *
     * @throws SmartFrogException In case of error while deployment
     * @throws RemoteException In case of Remote/nework error
     */
    public synchronized void sfDeploy() throws SmartFrogException, RemoteException {
        try {
            super.sfDeploy();
            detachDownwards = ((Boolean) sfResolve("detachDownwards")).booleanValue();
            detachUpwards = ((Boolean) sfResolve("detachUpwards")).booleanValue();
            autoDestruct = ((Boolean) sfResolve("autoDestruct")).booleanValue();
        } catch (Exception t) {
            throw SmartFrogLifecycleException.sfDeploy("",t , this);
        }
    }

    /**
     * Starts the detaching compound. This sends sfStart to all managed
     * components in the compound context, then detaches them from itself if
     * detachDownwards has been set to true in the compound description. The
     * compound detaches itslef from its parent if detachUpwards has been set
     * to true.Failures while detaching cause autodestruct to be set to true,
     * and a flag indicates abormal termination. Components that did not
     * detach will terminate, since there liveness times out. On completion
     * (successfull or not) the compound terminates itself if autoDestruct has
     * been set to true.
     *
     * @throws SmartFrogException if failed to start detaching compound
     * @throws RemoteException In case of Remote/nework error
     */
    public synchronized void sfStart() throws SmartFrogException, RemoteException {
        try {
            super.sfStart();
            detacher = new Thread(new Runnable() {
                        public void run() {
                            // detach this compound
                            if (detachUpwards) {
                                try {
                                    sfDetach();
                                } catch (SmartFrogException dex) {
                                    sfTerminate(TerminationRecord.abnormal(
                                            "DetachingCompound failed to detach ",
                                            null));

                                    return;
                                } catch (RemoteException rex) {
                                    sfTerminate(TerminationRecord.abnormal(
                                            "DetachingCompound failed to detach due to remote exception",
                                            null));

                                    return;
                                }
                            }

                            if (detachDownwards) {
                                // detach all children
                                for (Enumeration e = sfChildren();
                                        e.hasMoreElements();) {
                                    try {
                                        ((Prim) e.nextElement()).sfDetach();
                                    } catch (SmartFrogException remex) {
                                        sfTerminate(TerminationRecord.abnormal(
                                                "DetachingCompound failed to detach children",
                                                null));

                                        return;
                                    } catch (RemoteException rex) {
                                        sfTerminate(TerminationRecord.abnormal(
                                                "DetachingCompound failed to detach children due to remote exception",
                                                null));

                                        return;
                                    }
                                }

                                if (autoDestruct) {
                                    try {
                                        name = sfCompleteName();
                                    } catch (Exception e) {
                                    }

                                    sfTerminate(TerminationRecord.normal(name));
                                }
                            }
                        }
                    });

            if (detachDownwards || detachUpwards || autoDestruct) {
                detacher.start();
            }
        } catch (Throwable t) { // catch throwable as user code is involved
             throw SmartFrogLifecycleException.sfDeploy("",t , this);
        }
    }
}
