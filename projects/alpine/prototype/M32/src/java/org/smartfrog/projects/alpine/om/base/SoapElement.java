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
package org.smartfrog.projects.alpine.om.base;

import nu.xom.*;
import org.smartfrog.projects.alpine.xmlutils.AttributeIterator;
import org.smartfrog.projects.alpine.xmlutils.NodeIterator;
import org.smartfrog.projects.alpine.xmlutils.XsdUtils;
import org.smartfrog.projects.alpine.xmlutils.NodesIterator;
import org.smartfrog.projects.alpine.interfaces.ValidateXml;
import org.smartfrog.projects.alpine.faults.InvalidXmlException;

import javax.xml.namespace.QName;

/**
 * Extended element with a backpointer to the element
 */
public class SoapElement extends Element implements ValidateXml {


    public SoapElement(String name) {
        super(name);
    }

    public SoapElement(QName qname) {
        super(qname.getLocalPart(),qname.getNamespaceURI());
    }
    
    public SoapElement(String name, String uri) {
        super(name, uri);
    }

    public SoapElement(String name, String uri,String text) {
        super(name, uri);
        appendChild(text);
    }

    public SoapElement(Element element) {
        super(element);
    }

    /**
     * <p/>
     * Creates a very shallow copy of the element with the same name and
     * namespace URI, but no children, attributes, base URI, or namespace
     * declaration. This method is invoked as necessary by the {@link
     * nu.xom.Element#copy() copy} method and the {@link
     * nu.xom.Element#Element(nu.xom.Element) copy constructor}. </p>
     * <p/>
     * <p/>
     * Subclasses should override this method so that it returns an instance of
     * the subclass so that types are preserved when copying. This method should
     * not add any attributes, namespace declarations, or children to the
     * shallow copy. Any such items will be overwritten. </p>
     *
     * @return an empty element with the same name and namespace as this
     *         element
     */
    protected Element shallowCopy() {
        return new SoapElement(getQualifiedName(), getNamespaceURI());
    }

    /**
     * Iterate just over elements
     *
     * @return an iterator
     */
    public NodeIterator nodes() {
        return new NodeIterator(this);
    }

    /**
     * get our attributes
     *
     * @return
     */
    public AttributeIterator attributes() {
        return new AttributeIterator(this);
    }



    /**
     * Get the QName of an element
     *
     * @return
     */
    public QName getQName() {
        return XsdUtils.makeQName(this);
    }

    /**
     * <p/>
     * Returns the first child element with the specified nqame or null
     * null. </p>
     *
     * @param name the name of the element to return
     * @return the first child element with the specified name or null if there is no such
     *         element
     */
    public final Element getFirstChildElement(QName name) {
        return getFirstChildElement(name.getLocalPart(), name.getNamespaceURI());
    }
    
    /**
     * <p/>
     * Returns the first child element with the specified nqame or null null. </p>
     *
     * @param name the name of the element to return
     * @return the first child element with the specified name or null if there is no such element
     */
    public final Elements getChildElements(QName name) {
        return getChildElements(name.getLocalPart(), name.getNamespaceURI());
    }    
    /**
     * Test for a propertylist instance name
     *
     * @param testName
     * @return
     */
    public boolean isNamed(QName testName) {
        return getLocalName().equals(testName.getLocalPart()) &&
                getNamespaceURI().equals(testName.getNamespaceURI());
    }



    /**
     * Get the immediate text value of an element. That is -the concatenation
     * of all direct child text elements. This string is not trimmed.
     * @return a next string, which will be empty "" if there is no text
     */ 
    public String getTextValue() {
        StringBuilder builder=new StringBuilder();
        for (Node n:nodes()) {
            if (n instanceof Text) {
                Text text=(Text) n;
                builder.append(text.getValue());
            }
        }
        return builder.toString();
    }

    /**
     * Validate the Xml. Throw {@link InvalidXmlException} if invalid.
     */
    public void validateXml() {
        for(Node child:nodes()) {
            if(child instanceof ValidateXml) {
                ValidateXml validation=(ValidateXml) child;
                validation.validateXml();
            }
        }

    }

    
    public NodesIterator xpath(String path,XPathContext context) {
        Nodes nodes = query(path, context);
        NodesIterator it = new NodesIterator(nodes);
        return it;
    }

    public NodesIterator xpath(String path) {
        Nodes nodes = query(path);
        NodesIterator it = new NodesIterator(nodes);
        return it;
    }
    /**
     * Remove a child element; do nothing if it is absent
     * @param name qname of the element
     * @return the detached element or null for no match
     */
    public Element removeChildElement(QName name) {
        Element child =getFirstChildElement(name);
        if(child !=null) {
            removeChild(child);
            return child;
        }
        return null;
    }

    /**
     * Add an element, or, if it exists, replace it with the new one
     * @param newElement the new element
     * @return any old element, or null if there was none
     */
    public Element addOrReplaceChild(Element newElement) {
        Element child = getFirstChildElement(newElement.getLocalName(),newElement.getNamespaceURI());
        if (child != null) {
            replaceChild(child,newElement);
            return child;
        } else {
            appendChild(newElement);
            return null;
        }
    }

    /**
     * copy all the child elements and attributes from a node
     * @param that the source of the data
     */
    public void copyChildrenFrom(Element that) {
        for (Attribute a: new AttributeIterator(that)) {
            appendChild(a.copy());
        }
        for (Node n : new NodeIterator(that)) {
            appendChild(n.copy());
        }

    }
}
