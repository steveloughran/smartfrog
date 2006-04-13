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
package org.smartfrog.services.deployapi.alpineclient.model;

import nu.xom.Element;
import static org.ggf.cddlm.generated.api.CddlmConstants.API_ELEMENT_LOOKUPSYSTEM_REQUEST;
import org.smartfrog.projects.alpine.om.soap11.MessageDocument;
import org.smartfrog.projects.alpine.transport.Transmission;
import org.smartfrog.projects.alpine.transport.TransmitQueue;
import org.smartfrog.projects.alpine.wsa.AlpineEPR;
import org.smartfrog.services.deployapi.binding.XomHelper;
import org.smartfrog.services.deployapi.system.Constants;

/**
 * created 10-Apr-2006 17:07:57
 */

public class PortalSession extends WsrfSession {

    /**
     * Package scoped constructor.
     *
     * @param endpoint
     * @param validating
     * @param queue
     */
    public PortalSession(AlpineEPR endpoint, boolean validating, TransmitQueue queue) {
        super(endpoint, validating, queue);
    }

    public Transmission beginLookupSystem(String id) {
        Element resid = XomHelper.apiElement("ResourceId", id);
        Element request;
        request = XomHelper.apiElement(API_ELEMENT_LOOKUPSYSTEM_REQUEST, resid);
        return queue(Constants.API_PORTAL_OPERATION_LOOKUPSYSTEM, request);
    }

    /**
     * wait for something to finish. this is where any errors get raised
     *
     * @param tx
     * @return
     * @throws org.smartfrog.projects.alpine.faults.AlpineRuntimeException
     *          for trouble
     */
    public SystemSession endLookupSystem(Transmission tx) {
        MessageDocument response = tx.blockForResult(getTimeout());
        Element payload = response.getPayload();
        return new SystemSession(this, payload);
    }

    /**
     * Blocking implementation of the lookup system operation
     *
     * @param id system to look up
     * @return a system
     * @throws org.smartfrog.projects.alpine.faults.AlpineRuntimeException
     *          for trouble
     */
    public SystemSession lookupSystem(String id) {
        return endLookupSystem(beginLookupSystem(id));
    }

    /**
     * Create a session on a remote system.
     *
     * @param hostname is an optional hostname for the system
     * @return the queued transmission
     * @throws org.smartfrog.projects.alpine.faults.AlpineRuntimeException
     *          for trouble
     */
    public Transmission beginCreate(String hostname) {
        Element request;
        request = XomHelper.apiElement(Constants.API_ELEMENT_CREATE_REQUEST);
        if (hostname != null) {
            Element child = XomHelper.apiElement("hostname", hostname);
            request.appendChild(child);
        }
        return queue(Constants.API_PORTAL_OPERATION_CREATE, request);
    }

    /**
     * Wait for the creation operation to complete
     *
     * @param tx
     * @return the created session
     * @throws org.smartfrog.projects.alpine.faults.AlpineRuntimeException
     *          for trouble
     * @throws org.smartfrog.projects.alpine.faults.AlpineRuntimeException
     *          for trouble
     */
    public SystemSession endCreate(Transmission tx) {
        //this method matches exactly the postprocessing for the lookup system
        //call, the only difference being the localname of the response, which
        //isn't actually checked for.
        return endLookupSystem(tx);
    }

    /**
     * Blocking create operation
     *
     * @param hostname hostname, can be null
     * @return a session
     * @throws org.smartfrog.projects.alpine.faults.AlpineRuntimeException
     *          for trouble
     */
    public SystemSession create(String hostname) {
        return endCreate(beginCreate(hostname));
    }

}
