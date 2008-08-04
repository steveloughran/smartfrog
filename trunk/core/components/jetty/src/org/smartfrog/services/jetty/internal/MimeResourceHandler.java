/** (C) Copyright 2007 Hewlett-Packard Development Company, LP

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
package org.smartfrog.services.jetty.internal;

import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.MimeTypes;
import org.mortbay.resource.Resource;
import org.mortbay.io.Buffer;

import javax.servlet.http.HttpServletResponse;

/**
 *
 * Trying to fix JETTY-442 by subclassing; no joy.
 */

public class MimeResourceHandler extends ResourceHandler {

    private MimeTypes _mimeTypes = new MimeTypes();

    public MimeResourceHandler() {
    }

    public MimeTypes getMimeTypes() {
     //   return super._mimeTypes;
        return null;
    }


    public MimeTypes get_mimeTypes() {
        return _mimeTypes;
    }
}