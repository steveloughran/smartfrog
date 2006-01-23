/** (C) Copyright 2006 Hewlett-Packard Development Company, LP

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
package org.smartfrog.sfcore.languages.cdl.utils;

import nu.xom.Element;
import nu.xom.ParentNode;

import java.util.Map;
import java.util.HashMap;

/**
 * created 23-Jan-2006 17:21:13
 */

public class Namespaces implements NamespaceLookup{

    private Map<String, String> map=new HashMap<String, String>();

    public Namespaces() {
    }

    public Namespaces(Element e) {
        addNamespacesAndParentValues(e);
    }

    private void addNamespaces(Element element) {
        int count = element.getNamespaceDeclarationCount();
        for(int i=0;i<count;i++) {
            String prefix=element.getNamespacePrefix(i);
            String value=element.getNamespaceURI(prefix);
            if(map.get(prefix)==null) {
                //only add things that are not defined already
                map.put(prefix,value);
            }
        }
    }

    /**
     * Tail recursion up the graph to add all ns values
     * @param element
     */
    private void addNamespacesAndParentValues(Element element) {
        addNamespaces(element);
        final ParentNode parent = element.getParent();
        if(parent!=null && parent instanceof Element) {
            addNamespacesAndParentValues((Element) parent);
        }
    }

    /**
     * Get the URI of a namespace
     *
     * @param prefix the prefix
     * @return the URI or null for none.
     */
    public String resolveNamespaceURI(String prefix) {
        return map.get(prefix);
    }

}
