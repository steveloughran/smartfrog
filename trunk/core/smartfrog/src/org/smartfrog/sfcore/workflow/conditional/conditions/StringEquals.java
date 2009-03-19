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

import java.rmi.RemoteException;
import java.util.Locale;

/**
 * Convert the left and right arguments to strings, then compare them
 * created 30-Nov-2006 12:38:02
 */

public class StringEquals extends AbstractTwoArgumentCondition implements TwoArgumentCondition {

    public final String ATTR_CASE_SENSITIVE = "caseSensitive";

    private boolean caseSensitive;

    public StringEquals() throws RemoteException {
    }


    /**
     * Can be called to start components. Subclasses should override to provide
     * functionality Do not block in this call, but spawn off any main loops!
     *
     * @throws SmartFrogException failure of some kind
     * @throws RemoteException    In case of network/rmi error
     */
    public synchronized void sfStart() throws SmartFrogException, RemoteException {
        super.sfStart();
        caseSensitive = sfResolve(ATTR_CASE_SENSITIVE, caseSensitive, true);
    }

    /**
     * Evaluate the condition.
     *
     * @return true if it is successful, false if not
     * @throws SmartFrogException failure of some kind
     * @throws RemoteException    In case of network/rmi error
     */
    public boolean evaluate() throws RemoteException, SmartFrogException {
        String lstr = getLeft().toString();
        String rstr = getRight().toString();
        if (!caseSensitive) {
            lstr = lstr.toLowerCase(Locale.ENGLISH);
            rstr = rstr.toLowerCase(Locale.ENGLISH);
        }
        return lstr.equals(rstr);
    }
}
