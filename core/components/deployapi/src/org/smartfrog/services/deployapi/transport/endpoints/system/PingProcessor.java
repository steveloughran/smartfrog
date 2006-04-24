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
package org.smartfrog.services.deployapi.transport.endpoints.system;

import nu.xom.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.smartfrog.services.deployapi.transport.endpoints.alpine.WsrfHandler;

import java.rmi.RemoteException;

/**
 * This class is *NOT* re-entrant. Create one for each deployment. created Aug
 * 4, 2004 3:58:37 PM
 */

public class PingProcessor extends SystemProcessor {
    /**
     * log
     */
    private static final Log log = LogFactory.getLog(PingProcessor.class);


    public PingProcessor(WsrfHandler owner) {
        super(owner);
    }

    public Element process(Element request) throws RemoteException {
        jobMustExist();

/*
        Element body = XomHelper.getElement(request,
                "api:"+ Constants.API_ELEMENT_PING_REQUEST);
*/

        Element response = job.ping(request);
        return response;
    }


}
