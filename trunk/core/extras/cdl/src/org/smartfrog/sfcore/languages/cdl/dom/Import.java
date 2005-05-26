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
package org.smartfrog.sfcore.languages.cdl.dom;

import nu.xom.Element;
import nu.xom.Node;
import org.ggf.cddlm.generated.api.CddlmConstants;
import org.smartfrog.sfcore.languages.cdl.CdlParsingException;
import org.smartfrog.sfcore.languages.cdl.dom.attributes.GenericAttribute;

/**
 * Import element
 * created 21-Apr-2005 14:26:27
 */

public class Import extends DocNode {

    private String namespace;

    private String location;

    public Import() {
    }

    public Import(Element node) throws CdlParsingException {
        super(node);
    }

    /**
     * Parse from XM
     *
     * @throws CdlParsingException
     */
    public void bind(Element element) throws CdlParsingException {
        super.bind(element);
        namespace = GenericAttribute.extractCdlAttributeValue(element, ATTR_NAMESPACE, false);
        location = GenericAttribute.extractCdlAttributeValue(element, ATTR_LOCATION, true);
    }


    /**
     * test that a node is of the right type
     * @param element
     * @return true if the element namespace and localname match what we handle
     */
    static boolean isA(Element element) {
        return isNode(element,ELEMENT_IMPORT);
    }

}
