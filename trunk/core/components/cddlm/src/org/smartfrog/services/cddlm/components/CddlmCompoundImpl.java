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
package org.smartfrog.services.cddlm.components;

import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.compound.CompoundImpl;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.prim.PrimHook;
import org.smartfrog.sfcore.prim.TerminationRecord;

import java.rmi.RemoteException;

/**
 * created Sep 8, 2004 2:33:27 PM
 */

public class CddlmCompoundImpl extends CompoundImpl
        implements CddlmCompound {

    public CddlmCompoundImpl() throws RemoteException {
    }


    public class startupHook implements PrimHook {

        public void sfHookAction(Prim prim,
                TerminationRecord terminationRecord)
                throws SmartFrogException {

        }
    }
    //PrimHook

}
