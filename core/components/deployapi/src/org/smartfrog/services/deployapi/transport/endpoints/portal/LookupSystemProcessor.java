/** (C) Copyright 2005 Hewlett-Packard Development Company, LP

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
package org.smartfrog.services.deployapi.transport.endpoints.portal;

import nu.xom.Element;
import org.smartfrog.services.deployapi.binding.XomHelper;
import org.smartfrog.services.deployapi.engine.Application;
import org.smartfrog.services.deployapi.transport.endpoints.alpine.AlpineProcessor;
import org.smartfrog.services.deployapi.transport.endpoints.alpine.WsrfHandler;
import org.smartfrog.projects.alpine.om.base.SoapElement;

/**
 * created 21-Sep-2005 10:37:53
 */

public class LookupSystemProcessor extends AlpineProcessor {

    public LookupSystemProcessor(WsrfHandler owner) {
        super(owner);
    }


    /**
     * Override point: process the body of a message.
     *
     * @param rootElement received contents of the SOAP Body
     * @return the body of the response or null for an empty response
     */
    public Element process(SoapElement rootElement) {
        String resourceId=XomHelper.getElementValue(rootElement, "api:ResourceId");
        Application job = lookupJob(resourceId);
        Element address = (Element) job.getEndpointer().copy();
        XomHelper.adopt(address, "lookupSystemResponse");
        return address;
    }
}
