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

package org.smartfrog.services.deployapi.transport.endpoints;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.context.MessageContext;

import javax.xml.namespace.QName;


/**
 * Implement WSRP
 */
public class WsrfEndpoint extends SFEndpoint {


    OMElement GetResourceProperty(OMElement request) throws AxisFault {
        return null;
    }

    OMElement GetMultipleResourceProperties(OMElement request) throws AxisFault {
        return null;
    }

    OMElement Subscribe(OMElement request) throws AxisFault {
        return null;
    }

    OMElement GetCurrentMessage(OMElement request) throws AxisFault {
        return null;
    }

}
