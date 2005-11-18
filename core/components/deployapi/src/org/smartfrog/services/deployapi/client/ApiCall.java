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

package org.smartfrog.services.deployapi.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Call;
import org.apache.axis2.client.Callback;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wsdl.WSDLInterface;
import org.smartfrog.services.deployapi.binding.EprHelper;

import javax.xml.namespace.QName;
import java.util.HashMap;

/**

 */
public class ApiCall extends Call {
    /**
     * The default timeout is huge, because it makes stepping through in a debugger possible.
     */
    private static final int DEFAULT_TIMEOUT = 30*60*1000;

    int timeout=DEFAULT_TIMEOUT;

    /**
     * this is a convenience Class, here the Call will assume a Anoynmous
     * Service.
     *
     * @throws org.apache.axis2.AxisFault
     */
    public ApiCall() throws AxisFault {
        init();
    }

    /**
     * This is used to create call object with client home , using only this
     * constructor it can able to engage modules  , addning client side
     * parameters
     *
     * @param clientHome
     * @throws org.apache.axis2.AxisFault
     */
    public ApiCall(String clientHome) throws AxisFault {
        super(clientHome);
        init();
    }

    /**
     * @param service
     * @see org.apache.axis2.clientapi.InOutMEPClient constructer
     */
    public ApiCall(ServiceContext service) {
        super(service);
        init();
    }

    private void init() {
        setTimeOutInMilliSeconds(DEFAULT_TIMEOUT);
    }

    public ServiceContext getServiceContext() {
        return serviceContext;
    }


    public AxisOperation lookupOperation(String operation) {
        QName operationName = new QName(operation);
        ServiceContext serviceContext = getServiceContext();
        assert serviceContext!=null:"service context is null";
        AxisService service = serviceContext.getAxisService();
        WSDLInterface serviceInterface = service.getServiceInterface();
        HashMap allOperations = serviceInterface.getAllOperations();
        AxisOperation description;
        description= (AxisOperation) allOperations.get(operation);
        description = service.getOperation(operationName);
        return description;
    }

    private static Log log= LogFactory.getLog(ApiCall.class);
    
    public void invokeNonBlocking(String axisop,
                                  OMElement toSend,
                                  Callback callback) throws AxisFault {
        logInvocation(axisop);
        super.invokeNonBlocking(axisop, toSend, callback);
    }

    private void logInvocation(String axisop) {
        log.info(this.toString()+": "+axisop);
    }


    public OMElement invokeBlocking(String axisop, OMElement toSend)
            throws AxisFault {
        logInvocation(axisop);
        return super.invokeBlocking(axisop, toSend);
    }

    /**
     * Get the EPR that this call is addressed to
     * @return the address or null
     */
    public EndpointReference getAddress() {
        return this.messageInformationHeaders.getTo();
    }
    
    public String toString() {
        return "Apicall "+ EprHelper.stringify(getAddress());
    }

    /**
     *  patch in decent timeouts
     */
    protected void prepareInvocation(AxisOperation axisop, MessageContext msgCtx) throws AxisFault {
        super.prepareInvocation(axisop, msgCtx);
        msgCtx.setProperty(HTTPConstants.SO_TIMEOUT, Integer.valueOf(timeout));
        msgCtx.setProperty(HTTPConstants.CONNECTION_TIMEOUT, Integer.valueOf(timeout));
    }
}
