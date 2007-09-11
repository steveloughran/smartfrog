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

package org.smartfrog.test.system.start;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.smartfrog.sfcore.common.SmartFrogDeploymentException;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.prim.PrimImpl;


public class OutputImpl extends PrimImpl implements Prim, Output, Remote {
    private Input to;

    // standard constructor
    public OutputImpl() throws RemoteException {
    }

    // public component methods
    public void output(int value) throws RemoteException {
        to.input(value);
    }

    // lifecycle methods
    public void sfDeploy() throws SmartFrogException, RemoteException {
        super.sfDeploy();
        try {
            // get the binding to forward to, if non-existant fail
            to = (Input) sfResolve("to");
        } catch (SmartFrogException sfe) {
            //TODO: terminate the component
            throw sfe;
        } catch (Exception t) {
            //TODO: terminate the component
            throw new SmartFrogDeploymentException(t, this);
        }
    }
}
