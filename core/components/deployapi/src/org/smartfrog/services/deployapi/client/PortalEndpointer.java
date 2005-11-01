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
package org.smartfrog.services.deployapi.client;


import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOutAxisOperation;
import org.smartfrog.services.deployapi.system.Constants;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * created Aug 31, 2004 4:27:08 PM represents a server binding.
 */

public class PortalEndpointer extends Endpointer {



    protected static AxisOperation[] operations;
    protected static AxisService serviceDescription;

    
    
    /**
     * This is pasted in from generated axis code
     */
    static {

        //creating the Service
        serviceDescription = new AxisService(
                new QName(Constants.CDL_API_WSDL_NAMESPACE, "PortalEPR"));

        //creating the operations
        AxisOperation __operation;
        operations = new AxisOperation[7];

        __operation = new InOutAxisOperation();
        __operation.setName(new QName(Constants.CDL_API_WSDL_NAMESPACE,
                "GetMultipleResourceProperties"));
        operations[0] = __operation;
        serviceDescription.addOperation(__operation);

        __operation = new InOutAxisOperation();
        __operation.setName(
                new QName(Constants.CDL_API_WSDL_NAMESPACE, "Create"));
        operations[1] = __operation;
        serviceDescription.addOperation(__operation);

        __operation = new InOutAxisOperation();
        __operation.setName(
                new QName(Constants.CDL_API_WSDL_NAMESPACE, "LookupSystem"));
        operations[2] = __operation;
        serviceDescription.addOperation(__operation);

        __operation = new InOutAxisOperation();
        __operation.setName(new QName(Constants.CDL_API_WSDL_NAMESPACE,
                "GetCurrentMessage"));
        operations[3] = __operation;
        serviceDescription.addOperation(__operation);

        __operation = new InOutAxisOperation();
        __operation.setName(
                new QName(Constants.CDL_API_WSDL_NAMESPACE, "Resolve"));
        operations[4] = __operation;
        serviceDescription.addOperation(__operation);

        __operation = new InOutAxisOperation();
        __operation.setName(
                new QName(Constants.CDL_API_WSDL_NAMESPACE, "Subscribe"));
        operations[5] = __operation;
        serviceDescription.addOperation(__operation);

        __operation = new InOutAxisOperation();
        __operation.setName(new QName(Constants.CDL_API_WSDL_NAMESPACE,
                "GetResourceProperty"));
        operations[6] = __operation;
        serviceDescription.addOperation(__operation);

    }


    public PortalEndpointer() throws AxisFault {
        init();
    }

    public PortalEndpointer(URL url) throws AxisFault {
        setURL(url);
        init();
    }

    public PortalEndpointer(String url) throws AxisFault {
        super(url);
        init();
    }

    public AxisService getServiceDescription() {
        return serviceDescription;
    }

    public static PortalEndpointer createDefaultBinding() throws IOException {
        PortalEndpointer endpointer = new PortalEndpointer();
        URL defURL = new URL("http",
                Constants.DEFAULT_HOST,
                Constants.DEFAULT_SERVICE_PORT,
                Constants.DEFAULT_PATH);

        endpointer.setURL(defURL);
        return endpointer;
    }

    public String toString() {
        if (url == null) {
            return "(unbound)";
        } else {
            return url.toExternalForm();
        }
    }

    /**
     * convert to an external form.
     *
     * @return
     */
    public String toCommandLineElement() {
        return URL_COMMAND + url.toExternalForm();
    }


    /**
     * get the binding of this element, null for no match,
     * will patch that entry in the cmd line to null as a side effect
     * @param commandLine full command line args
     * @return a portal
     * @throws java.net.MalformedURLException if there was anything wrong with the URL
     */
    public static PortalEndpointer fromCommandLine(String[] commandLine)
            throws MalformedURLException, AxisFault {
        URL url = UrlFromCommandLine(commandLine);
        if(url!=null) {
            return new PortalEndpointer(url);
        } else {
            return null;
        }
    }


}
