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
package org.smartfrog.services.deployapi.binding.bindings;

import org.apache.xmlbeans.XmlOptions;
import org.ggf.xbeans.cddlm.api.InitializeRequestDocument;
import org.ggf.xbeans.cddlm.api.InitializeResponseDocument;
import org.smartfrog.services.deployapi.binding.EndpointBinding;

/**
 * created 21-Sep-2005 13:08:40
 */

public class InitializeBinding extends EndpointBinding<InitializeRequestDocument, InitializeResponseDocument> {
    public InitializeBinding(XmlOptions inOptions, XmlOptions outOptions) {
        super(inOptions, outOptions);
    }

    public InitializeBinding() {
    }

    /**
     * create a request object
     *
     * @return
     */
    public InitializeRequestDocument createRequest() {
        return InitializeRequestDocument.Factory.newInstance(getInOptions());
    }

    /**
     * create a request object
     *
     * @return
     */
    public InitializeResponseDocument createResponse() {
        return InitializeResponseDocument.Factory.newInstance(getOutOptions());
    }
}
