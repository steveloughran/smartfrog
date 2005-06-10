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
import org.smartfrog.sfcore.languages.cdl.faults.CdlXmlParsingException;
import org.smartfrog.sfcore.languages.cdl.faults.CdlXmlParsingException;
import org.smartfrog.sfcore.languages.cdl.dom.DocNode;

/**
 * created 26-May-2005 11:17:40
 */

public class RefAttribute extends GenericAttribute {

    public RefAttribute() {
    }

    public RefAttribute(Attribute attribute) throws CdlXmlParsingException {
        super(attribute);
    }

    /**
     * Extract a ref attribute from an element.
     * @param element element to extract from
     * @param required flag to set to true if the attribute is required
     * @return
     */
    public static RefAttribute extract(Element element, boolean required)
            throws CdlXmlParsingException {
        return (RefAttribute)findAndBind(ATTR_REF,RefAttribute.class,element,required, true);
    }
}
