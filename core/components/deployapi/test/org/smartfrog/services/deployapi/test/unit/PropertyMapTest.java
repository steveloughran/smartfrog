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

import org.smartfrog.services.deployapi.transport.wsrf.PropertyMap;
import org.smartfrog.services.deployapi.transport.wsrf.Property;
import org.smartfrog.services.deployapi.binding.XomHelper;
import org.smartfrog.services.deployapi.system.Constants;
import org.smartfrog.services.deployapi.system.Utils;
import org.apache.axis2.om.OMElement;
import org.ggf.cddlm.utils.QualifiedName;
import nu.xom.Element;

import javax.xml.namespace.QName;
import java.util.Date;

/**

 */
public class PropertyMapTest extends UnitTestBase {
    private static final QualifiedName PROPNAME = Constants.PROPERTY_SYSTEM_SYSTEM_STATE;
    private static final QName QNAME = Utils.convert(PROPNAME);

    public PropertyMapTest(String name) {
        super(name);
    }

    private PropertyMap map;

    private Element elt;

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() throws Exception {
        super.setUp();
        map=new PropertyMap();
        elt= XomHelper.apiElement("w");
    }

    public void testAddLookupRemove() throws Exception {
        OMElement result = map.getPropertyValue(PROPNAME);
        assertNull(result);
        map.addStaticProperty(PROPNAME,
                elt);
        result=map.getPropertyValue(PROPNAME);
        assertNotNull(result);
        map.remove(PROPNAME);
        result = map.getPropertyValue(PROPNAME);
        assertNull(result);
    }

    public void testAddLookupRemove2() throws Exception {
        Property result = map.getProperty(QNAME);
        assertNull(result);
        map.addStaticProperty(PROPNAME,
                elt);
        result = map.getProperty(QNAME);
        assertNotNull(result);
        map.remove(result);
        result = result = map.getProperty(QNAME);
        assertNull(result);
    }


    public void testAddStatic() throws Exception {
        Property result = map.getProperty(QNAME);
        assertNull(result);
        OMElement e =Utils.xomToAxiom(elt);
        map.addStaticProperty(QNAME,
                e);
        result = map.getProperty(QNAME);
        assertNotNull(result);
        OMElement value=result.getValue();
        assertSame(e,value);
        value = map.getPropertyValue(QNAME);
        assertSame(e, value);
        map.remove(result);
        result = result = map.getProperty(QNAME);
        assertNull(result);
    }

    public void testAddTextValue() throws Exception {
        final String text = Utils.toIsoTime(new Date());
        assertIsoDate(text);
        map.addStaticProperty(QNAME, text);
        Property result = map.getProperty(QNAME);
        assertNotNull(result);
        OMElement value=result.getValue();
        assertEquals(text,value.getText());
        value = map.getPropertyValue(QNAME);
        assertEquals(text, value.getText());
    }


}
