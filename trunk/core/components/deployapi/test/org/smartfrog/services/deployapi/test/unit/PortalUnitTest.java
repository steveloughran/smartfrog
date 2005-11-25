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
package org.smartfrog.services.deployapi.test.unit;

import org.apache.axis2.om.OMElement;
import org.ggf.xbeans.cddlm.api.CreateRequestDocument;
import org.ggf.xbeans.cddlm.api.CreateResponseDocument;
import org.smartfrog.services.deployapi.binding.xmlbeans.Axis2Beans;
import org.smartfrog.services.deployapi.system.Constants;
import org.smartfrog.services.deployapi.transport.endpoints.PortalEndpoint;
import org.smartfrog.services.deployapi.transport.endpoints.portal.CreateProcessor;

import javax.xml.namespace.QName;

/**
 * created 14-Sep-2005 11:53:51
 */

public class PortalUnitTest extends UnitTestBase {

    /**
     * Constructs a test case with the given name.
     */
    public PortalUnitTest(String name) {
        super(name);
    }

    private PortalEndpoint portal;

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp() throws Exception {
        super.setUp();
        portal = new PortalEndpoint();
    }

    public void testOMLookup() throws Exception {
        OMElement ome = loadTestOMElement(DOC_CREATE, TEST_CREATE_REQUEST_HOSTNAME);
        assertNotNull(ome);
    }

    public void BrokentestRoundTrip() throws Exception {
        Axis2Beans<CreateRequestDocument.CreateRequest> requestBinder = new Axis2Beans<CreateRequestDocument.CreateRequest>();
        CreateRequestDocument.CreateRequest doc= (CreateRequestDocument.CreateRequest)
                loadTestElement(DOC_CREATE,TEST_CREATE_REQUEST_HOSTNAME);
        QName requestQname=new QName(Constants.CDL_API_TYPES_NAMESPACE, Constants.API_ELEMENT_CREATE_REQUEST);
        assertName(doc,requestQname);
        assertValid(doc);
        //test round tripping
        OMElement request = requestBinder.convert(doc);
        assertEquals(requestQname, request.getQName());
        Axis2Beans<CreateRequestDocument> docBinder = new Axis2Beans<CreateRequestDocument>();
        CreateRequestDocument doc2 = docBinder.convert(request);
        CreateRequestDocument.CreateRequest createRequest = doc2.getCreateRequest();
        assertName(createRequest, requestQname);
        assertValid(createRequest);
        assertEquals(doc, createRequest);

    }

    public void NotestDispatch() throws Exception {
        OMElement request=loadTestOMElement(DOC_CREATE, TEST_CREATE_REQUEST_HOSTNAME);
        CreateProcessor createProcessor = new CreateProcessor(portal);
        OMElement response = createProcessor.process(request);
        Axis2Beans<CreateResponseDocument> responseBinder = new Axis2Beans<CreateResponseDocument>();
        CreateResponseDocument responseDoc=responseBinder.convert(response);
        assertValid(responseDoc);
    }

}
