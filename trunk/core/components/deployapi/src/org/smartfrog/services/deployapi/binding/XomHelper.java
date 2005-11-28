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

package org.smartfrog.services.deployapi.binding;

import static org.ggf.cddlm.generated.api.CddlmConstants.CDL_API_TYPES_NAMESPACE;
import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.Document;
import nu.xom.Serializer;
import org.ggf.cddlm.generated.api.CddlmConstants;
import org.smartfrog.services.deployapi.system.Constants;
import org.smartfrog.services.deployapi.transport.faults.FaultRaiser;
import org.smartfrog.services.xml.utils.XomUtils;
import org.smartfrog.services.xml.utils.XsdUtils;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

/** generic xom stuff */
public class XomHelper extends XomUtils  {
    public static final String API = "api:";
    public static final String WSRF_RL = "wsrf-rl:";
    public static final String MUWSP1_XS = "muws-p1-xs:";
    public static final String TNS = CDL_API_TYPES_NAMESPACE;

    /**
     * Create a new API element with the api: prefix in the API namespace
     * @param name localname
     * @return a new element
     */
    public static Element apiElement(String name) {
        return new Element(API + name,
                CDL_API_TYPES_NAMESPACE);
    }


    /**
     * Create a new API element
     * @param name localname
     * @param value string name
     * @return
     */
    public static Element apiElement(String name,String value) {
        Element e=apiElement(name);
        e.appendChild(value);
        return e;
    }

    /**
     * Create a new Api element with the specified child element
     * @param name localname
     * @param child child element. May be null.
     * @return
     */
    public static Element apiElement(String name, Element child) {
        Element e = apiElement(name);
        if(child!=null) {
            e.appendChild(child);
        }
        return e;
    }


    /**
     * Add a new API attribute to an element
     * @param element element to add to
     * @param name attribute name
     * @param value string value of the element
     */
    public static void addApiAttr(Element element, String name, String value) {
        Attribute attribute = new Attribute(API + name,
                CDL_API_TYPES_NAMESPACE,
                value);
        element.addAttribute(attribute);
    }

    /**
     * Get the value of an api attribute
     * @param element element to look at
     * @param name attribute name
     * @param required flag if needed
     * @return
     */
    public static String getApiAttrValue(Element element, String name,boolean required) {
        Attribute val = element.getAttribute(name, CDL_API_TYPES_NAMESPACE);
        if(val==null) {
            if(required) {
                throw FaultRaiser.raiseBadArgumentFault("No attribute api:"+name+" on "+element);
            } else {
                return null;
            }
        }
        return val.getValue();
    }

    /**
     * Get the boolean value of an attribute
     * @param element
     * @param name
     * @param required
     * @param defval
     * @return
     * @throws org.smartfrog.services.deployapi.transport.faults.BaseException if the value doesnt map to a bool
     */
    public static boolean getBoolApiAttrValue(Element element, String name, boolean required,boolean defval) {
        String val=getApiAttrValue(element,name, required);
        if(val==null) {
            return defval;
        }
        return getXsdBoolValue(val);
    }

    /**
     *
     * @param string value to parse
     * @return value
     * @throws org.smartfrog.services.deployapi.transport.faults.BaseException if the value doesnt map to a bool
     */
    public static boolean getXsdBoolValue(String string) {
        if(XsdUtils.isXsdBooleanTrue(string)) {
            return true;
        }
        if(XsdUtils.isXsdBooleanFalse(string)) {
            return false;
        }
        throw FaultRaiser.raiseBadArgumentFault("Not a valid boolean value:" +string);
    }

    /**
     * Create a MUWS resource ID element from a string ID
     * @param id new ID
     * @return the new element
     */
    public static Element makeResourceId(String id) {
        Element element = new Element(MUWSP1_XS +
                Constants.PROPERTY_MUWS_RESOURCEID.getLocalPart(),
                Constants.MUWS_P1_NAMESPACE);
        element.appendChild(id);
        return element;

    }

    /**
     * Move an element into the cdl namespace
     * @param element
     * @param name
     */
    public static void adopt(Element element,String name) {
        element.setLocalName(name);
        element.setNamespaceURI(CDL_API_TYPES_NAMESPACE);
        element.setNamespacePrefix("api");
    }

    /**
     * Get an element's value. Throws a BadArgument Deployment fault if it
     * doesnt resolve.
     *
     * @param node  node to start
     * @param query query to ask
     * @return
     */
    public static String getElementValue(Node node, String query) {
        return getElement(node, query).getValue();
    }

    /**
     * Get an element. Throws a BadArgument Deployment fault if it doesnt
     * resolve.
     *
     * @param node  node to start
     * @param query query to ask
     * @return the element
     */
    public static Element getElement(Node node, String query) {
        return getElement(node, query, true);
    }

    /**
     * Get an element. Throws a BadArgument Deployment fault if it doesnt
     * resolve.
     *
     * @param node  node to start
     * @param query query to ask
     * @param required flag to indicate a node is required or not
     * @return the element or null if not found && required==false.
     */
    public static Element getElement(Node node,
                                     String query,
                                     boolean required) {
        Nodes nodes = node.query(query, Constants.XOM_CONTEXT);
        if (nodes.size() == 0) {
            if (required) {
                throw FaultRaiser.raiseBadArgumentFault("Nothing at " + query);
            } else {
                return null;
            }
        }
        Node n = nodes.get(0);
        if (!(n instanceof Element)) {
            throw FaultRaiser.raiseBadArgumentFault("Expected an element at " +
                    query
                    +
                    " but got " +
                    n);
        }
        return (Element) n;
    }


}
