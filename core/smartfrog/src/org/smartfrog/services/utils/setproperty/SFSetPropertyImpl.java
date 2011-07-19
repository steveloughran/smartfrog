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

package org.smartfrog.services.utils.setproperty;

import org.smartfrog.sfcore.common.Context;
import org.smartfrog.sfcore.common.SmartFrogContextException;
import org.smartfrog.sfcore.common.SmartFrogDeploymentException;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.compound.Compound;
import org.smartfrog.sfcore.compound.CompoundImpl;
import org.smartfrog.sfcore.logging.Log;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.utils.ComponentHelper;

import java.rmi.RemoteException;


/**
 *  Component to set properties very early on.
 */
public class SFSetPropertyImpl extends CompoundImpl implements Compound, SFSetProperty {

    private String name = null;
    private Object value = null;
    private boolean replace = true;
    private Log log;
    private String finalValue;

    /**
     * Constructor
     *
     * @throws RemoteException In case of Remote/nework error
     */
    public SFSetPropertyImpl() throws RemoteException {
    }

    /**
     * set system property very early on sfDeployWith.
     *
     * @throws SmartFrogDeploymentException In case of error while deployment
     * @throws RemoteException In case of Remote/nework error
     */
    @Override
    public synchronized void sfDeployWith(Prim parent, Context cxt)
            throws SmartFrogDeploymentException, RemoteException {
        try {
            log = sfGetApplicationLog();//.sfGetLog(sfResolve(SmartFrogCoreKeys.SF_APP_LOG_NAME, "", true));
            // Mandatory attributes.
            try {
                name = (String) cxt.sfResolveAttribute(ATTR_NAME);
                value = cxt.sfResolveAttribute(ATTR_VALUE);
            } catch (SmartFrogException e) {
                if (log.isErrorEnabled()) {
                    log.error("Failed to read mandatory attribute: " + e, e);
                }
                throw e;
            }
            try {
                replace = ((Boolean) cxt.sfResolveAttribute(ATTR_REPLACE)).booleanValue();
            } catch (SmartFrogContextException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Failed to read optional attribute: " + e, e);
                }
            }


            String oldValue = System.getProperty(name, "");
            if (replace) {
                finalValue = value.toString();
            } else {
                finalValue = oldValue + value.toString();
            }
            System.setProperty(name, finalValue);
            if (log.isDebugEnabled()) {
                log.debug("Setting property: " + name +
                        ", NEW VALUE: " + value.toString() +
                        ", OLD VALUE: " + oldValue +
                        ", VALUE READ:" + System.getProperty(name));
            }

        } catch (Throwable t) {
            if (log.isErrorEnabled()) {
                log.error(t.getMessage(),t);
            }
            throw (SmartFrogDeploymentException) SmartFrogDeploymentException.forward(t.getMessage(), t);
        }
        //super.sfDeploy();
        super.sfDeployWith(parent, cxt);
    }

    /**
     * Starts the compound. This sends a synchronous sfStart to all managed
     * components in the compound context. Any failure will cause the compound
     * to terminate
     *
     * @throws SmartFrogException failed to start compound
     * @throws RemoteException In case of Remote/nework error
     */
    @Override
    public synchronized void sfStart() throws SmartFrogException, RemoteException {
        super.sfStart();
        new ComponentHelper(this).sfSelfDetachAndOrTerminate(null,
                "set " + name + " to " + finalValue, null, null);
    }
}
