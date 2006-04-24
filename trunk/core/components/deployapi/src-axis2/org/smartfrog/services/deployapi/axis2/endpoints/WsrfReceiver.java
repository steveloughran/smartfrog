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

package org.smartfrog.services.deployapi.axis2.endpoints;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.DependencyManager;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.soap.SOAPEnvelope;
import org.apache.wsdl.WSDLService;
import org.smartfrog.services.deployapi.transport.faults.BaseException;
import org.smartfrog.services.deployapi.axis2.endpoints.SmartFrogAxisEndpoint;
import org.smartfrog.sfcore.security.SFClassLoader;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.rmi.RemoteException;

/**
 */
public class WsrfReceiver extends AbstractInOutSyncMessageReceiver
        implements MessageReceiver {

    protected static Log faultLog = LogFactory.getLog("DISPATCH-FAULTS");

    /**
     * flag which indicates that an address is mandatory
     */
    private static boolean addressingMandatory = false;

    public static boolean isAddressingMandatory() {
        return addressingMandatory;
    }

    public static void setAddressingMandatory(boolean addressingMandatory) {
        WsrfReceiver.addressingMandatory = addressingMandatory;
    }

    public void invokeBusinessLogic(MessageContext inMessage, MessageContext outMessage) throws AxisFault {

        try {
            // get the implementation class for the Web Service
            Object destObject = getTheImplementationObject(inMessage);

            //Inject the Message Context if it is asked for
            DependencyManager.configureBusinessLogicProvider(destObject, inMessage,outMessage);

/*
            
            MessageInformationHeaders addressInfo = inMessage.getMessageInformationHeaders();
            if (isAddressingMandatory() && addressInfo != null) {
                throw new BaseException("WS-Addressing is mandatory on WSRF resources");
            }

*/
            //get the operation
            AxisOperation opDesc = inMessage.getOperationContext()
                    .getAxisOperation();
            QName operation = opDesc.getName();
            String style = inMessage.getOperationContext()
                    .getAxisOperation()
                    .getStyle();
            if (!WSDLService.STYLE_DOC.equals(style)) {
                throw new AxisFault(org.smartfrog.services.deployapi.system.Constants.ERROR_NOT_DOCLIT);
            }

            SOAPEnvelope envelope = null;
            OMElement result = null;
            SmartFrogAxisEndpoint endpoint = (SmartFrogAxisEndpoint) destObject;
            result = endpoint.dispatch(operation, inMessage);
            envelope = getSOAPFactory(inMessage).getDefaultEnvelope();
            if (result != null) {
                envelope.getBody().addChild(result);
            }
            outMessage.setEnvelope(envelope);
        } catch (BaseException e) {
            faultLog.error("BaseException ",e);
            throw e.makeAxisFault();
        } catch (AxisFault e) {
            faultLog.error("AxisFault", e);
            throw e;
        } catch (RemoteException e) {
            faultLog.error("RemoteException", e);
            throw AxisFault.makeFault(e);
        } catch (IOException e) {
            faultLog.error("IOException", e);
            throw AxisFault.makeFault(e);
        } catch (Exception e) {
            faultLog.error("Exception", e);
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * Method getTheImplementationObject
     *
     * @param msgContext
     * @return
     * @throws AxisFault
     */
/*
    protected Object getTheImplementationObject(MessageContext msgContext)
            throws AxisFault {
        ServiceDescription service =
                msgContext
                        .getOperationContext()
                        .getServiceContext()
                        .getServiceConfig();
        return makeNewServiceObject(msgContext);
    }
*/


    protected Object makeNewServiceObject(MessageContext msgContext)
            throws AxisFault {
        try {
            AxisService service =
                    msgContext.getOperationContext()
                            .getServiceContext()
                            .getAxisService();
            ClassLoader classLoader = service.getClassLoader();
            Parameter implInfoParam = service.getParameter(SERVICE_CLASS);

            if (implInfoParam != null) {
                return loadImplementationClassViaSmartFrog(msgContext);
            } else {
                throw new AxisFault(Messages.getMessage("paramIsNotSpecified",
                        "SERVICE_CLASS"));
            }
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }
    
 
    
    
    private Object loadImplementationClassViaClassloader(MessageContext msgContext) throws AxisFault {
        try {

            AxisService service =
                    msgContext
                            .getOperationContext()
                            .getServiceContext().getAxisService();
            //this is the override: we use the same classloader that loads this receiver.
            ClassLoader classLoader;
            ClassLoader axis2classLoader = service.getClassLoader();
            classLoader = this.getClass().getClassLoader();
            Parameter implInfoParam = service.getParameter(SERVICE_CLASS);
            if (implInfoParam != null) {
                Class implClass =
                        Class.forName(
                                (String) implInfoParam.getValue(),
                                true,
                                classLoader);
                return implClass.newInstance();
            } else {
                throw new AxisFault(
                        Messages.getMessage(
                                "paramIsNotSpecified",
                                "SERVICE_CLASS"));
            }

        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * Load an impl class via smartfrog
     *
     * @param msgContext
     * @return
     * @throws AxisFault
     */
    private Object loadImplementationClassViaSmartFrog(MessageContext msgContext) throws AxisFault {
        try {

            AxisService service =
                    msgContext
                            .getOperationContext()
                            .getServiceContext().getAxisService();
            Parameter implInfoParam = service.getParameter(SERVICE_CLASS);

            if (implInfoParam == null) {
                throw new AxisFault(
                        Messages.getMessage(
                                "paramIsNotSpecified",
                                "SERVICE_CLASS"));
            }
            String classname = (String) implInfoParam.getValue();
            classname = classname.trim();
            Class implClass =
                    SFClassLoader.forName(classname, null, true);
            return implClass.newInstance();

        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

}
