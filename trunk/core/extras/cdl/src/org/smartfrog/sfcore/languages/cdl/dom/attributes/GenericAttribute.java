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
package org.smartfrog.sfcore.languages.cdl.dom.attributes;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Node;
import org.smartfrog.sfcore.languages.cdl.CdlParsingException;
import org.smartfrog.sfcore.languages.cdl.dom.Names;
import org.smartfrog.sfcore.languages.cdl.dom.DocNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * This is the base class for attributes; it represents an attribute of some type.
 * created 26-May-2005 11:20:02
 */

public class GenericAttribute implements Names {

    /**
     * cached attribute
     */
    private Attribute attribute;

    /**
     * Get the value of this attribute - only valid when bound
     * @return
     */
    public String getValue() {
        return attribute.getValue();
    }

    public Attribute getAttribute() {
        return attribute;
    }

    /**
     * simple constructor
     */
    protected GenericAttribute() {

    }

    /**
     * bind at construct time.
     * calls {@link #bind(Attribute)}
     * @param attribute
     */
    protected GenericAttribute(Attribute attribute) throws CdlParsingException {
        bind(attribute);
    }



    /**
     * bind to an attribute. Sets the attribute value
     * subclasses can extend to extract more information; always call the parent
     * @param attr
     */
    public void bind(Attribute attr) throws CdlParsingException {
        attribute=attr;
    }

    /**
     * Get the parent element of an attribute.
     * Only valid when bound to an attribute/
     * @returns the parent element or null for no parent.
     */
    Element getParentElement() {
        assert attribute!=null;
        Node parent=attribute.getParent();
        if(parent==null) {
            return null;
        }
        else return (Element)parent;
    }


    /**
     * Find a CDL attribute and bind to it. There are some
     * reflection tricks here to instantiate a class instance
     * and call #extract on it.
     * @param localname
     * @param clazz
     * @param element
     * @param required
     * @return
     * @throws CdlParsingException
     */
    protected static GenericAttribute findAndBind(String localname,
            Class clazz,
            Element element, boolean required)
            throws CdlParsingException {
        Attribute attr = extractCdlAttribute(element,
                localname,
                required);
        if (attr == null) {
            return null;
        } else {
            try {
                Constructor ctor = clazz.getConstructor();
                GenericAttribute newattr = (GenericAttribute) ctor.newInstance();
                newattr.bind(attr);
                return newattr;
            } catch (NoSuchMethodException e) {
                throw new CdlParsingException(e);
            } catch (InstantiationException e) {
                throw new CdlParsingException(e);
            } catch (IllegalAccessException e) {
                throw new CdlParsingException(e);
            } catch (InvocationTargetException e) {
                throw new CdlParsingException(e);
            } catch (ClassCastException e) {
                throw new CdlParsingException("Cannot cast " +
                        clazz +
                        " to a GenericAttribute",
                        e);
            }
        }
    }

    /**
     * Get the value of an attribute in the CDL namespace
     * @param element node to examine
     * @param attributeName attribute to get
     * @param required flag set to true if needed
     * @return the string value of the attribute
     * @throws CdlParsingException
     */
    public static String extractCdlAttributeValue(Element element, String attributeName,boolean required)
        throws CdlParsingException {
        Attribute attribute = extractCdlAttribute(element, attributeName, required);
        return attribute!=null? attribute.getValue() : null;
    }

    /**
     * Get an attribute in the CDL namespace
     *
     * @param element       node to examine
     * @param attributeName attribute to get
     * @param required      flag set to true if needed
     * @return the string value of the attribute
     * @throws CdlParsingException
     */
    public static Attribute extractCdlAttribute(Element element,
            String attributeName, boolean required) throws CdlParsingException {
        Attribute attribute = element.getAttribute(attributeName,DOC_NAMESPACE);
        if(attribute==null && required) {
            throw new CdlParsingException("Missing attribute "+attributeName+" from element "+element);
        }
        return attribute;
    }
}
