/** (C) Copyright 2006 Hewlett-Packard Development Company, LP

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
package org.smartfrog.sfcore.workflow.conditional.conditions;

import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.common.SmartFrogLivenessException;

import java.rmi.RemoteException;

/**
 * created 30-Nov-2006 12:50:51
 */

public class IsAlive extends TargetedConditionImpl implements TargetedCondition {


    public IsAlive() throws RemoteException {
    }

    /**
     * Ping the target. return true if the operation did not fail for any reason
     *
     * @return true if it is successful, false if not
     * @throws java.rmi.RemoteException for network problems
     * @throws org.smartfrog.sfcore.common.SmartFrogException
     *                                  for any other problem
     */
    public boolean evaluate() throws RemoteException, SmartFrogException {
        Throwable thrown;
        try {
            getTarget().sfPing(this);
            return false;
        } catch (SmartFrogLivenessException e) {
            thrown=e;
        } catch (RemoteException e) {
            thrown=e;
        }
        if(sfLog().isDebugEnabled()) {
            sfLog().debug("liveness failure",thrown);
        }
        return false;
    }
}
