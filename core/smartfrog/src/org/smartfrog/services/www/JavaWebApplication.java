/** (C) Copyright 1998-2004 Hewlett-Packard Development Company, LP

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
package org.smartfrog.services.www;

import java.rmi.Remote;

/**
 * created 23-Jun-2004 13:42:09
 */


public interface JavaWebApplication extends Remote {


    /**
     * path on the application server
     */
    String CONTEXT_PATH = "contextPath";

    /**
     * name or File reference of a war file
     */
    String WARFILE = "warFile";

    /**
     * reference to the server to deploy on
     */
    String SERVER = "server";

    /**
     * run time attribute: the full URL to the component
     */
    String APPLICATION_URL ="applicationURL";
}
