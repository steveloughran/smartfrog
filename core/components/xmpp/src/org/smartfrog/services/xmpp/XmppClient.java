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
package org.smartfrog.services.xmpp;

import org.smartfrog.sfcore.common.SmartFrogException;

import java.rmi.RemoteException;

/**
 * Interface of the XmppClient
 */
public interface XmppClient extends Xmpp {

    /**
     * username for the message or any other unaddressed messages. {@value}
     */
    String ATTR_DESTINATION = "destination";
    /**
     * a message to send when this component starts. {@value}
     */
    String ATTR_MESSAGE = "message";

    /**
     * Post a message to the default destination.
     *
     * @param message text to send
     *
     * @throws RemoteException    on networking trouble
     * @throws SmartFrogException if there is no default destination, or
     *                            something went wrong with the communications
     */
    public void post(String message) throws RemoteException, SmartFrogException;

    /**
     *
     */
    /**
     * Post a message to the specified recipient
     *
     * @param recipient target user
     * @param message   text to send
     *
     * @throws RemoteException    on networking trouble
     * @throws SmartFrogException if something went wrong with the
     *                            communications
     */
    public void post(String recipient, String message)
            throws RemoteException, SmartFrogException;

}
