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

package org.smartfrog.services.deployapi.transport.wsrf;

import nu.xom.Element;
import org.apache.ws.commons.om.OMElement;
import org.smartfrog.services.deployapi.system.Utils;

import javax.xml.namespace.QName;

/**
 * this class represents something that can provide a resource
 */
public class StaticProperty implements Property {

    private QName name;
    private OMElement value;

    public StaticProperty() {
    }

    public StaticProperty(QName name, OMElement value) {
        this.name = name;
        this.value = value;
    }

    public StaticProperty(QName name, String value) {
        this.name = name;
        OMElement elt=Utils.createOmElement(name.getNamespaceURI(), 
                name.getLocalPart(), null);
        elt.setText(value);
        this.value = elt;
    }

    
    public QName getName() {
        return name;
    }

    public void setName(QName name) {
        this.name = name;
    }

    public OMElement getValue() {
        return value;
    }

    public void setValue(OMElement value) {
        this.value = value;
    }

    public void setValue(Element element) {
        setValue(Utils.xomToAxiom(element));
    }

}
