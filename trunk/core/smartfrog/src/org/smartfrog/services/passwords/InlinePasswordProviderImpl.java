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

package org.smartfrog.services.passwords;

import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.prim.PrimImpl;

import java.rmi.RemoteException;

/**
 * A password provider that gets the password from an attribute. The attribute is re-evaluated whenever the password is
 * resolved
 */
public class InlinePasswordProviderImpl extends PrimImpl implements PasswordProvider {


    public InlinePasswordProviderImpl() throws RemoteException {
    }

    /**
     * {@value}
     */
    public static final String ATTR_PASSWORD = "password";

    /**
     * {@inheritDoc}
     *
     * @return a password
     * @throws SmartFrogException If unable to get the password
     * @throws RemoteException in case of network or RMI error
     */
    public String getPassword() throws SmartFrogException, RemoteException {
        return sfResolve(ATTR_PASSWORD, (String) null, true);
    }
}
