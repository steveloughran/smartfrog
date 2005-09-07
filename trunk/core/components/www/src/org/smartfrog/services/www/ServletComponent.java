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
package org.smartfrog.services.www;


/**
 * Interface defining a servlet
 * created 18-Jul-2005 15:55:13
 */


public interface ServletComponent extends ServletContextComponent {
    final static String ATTR_NAME = "name";
    final static String ATTR_PATH_SPEC = "pathSpec";
    final static String ATTR_CLASSNAME = "className";
    final static String ATTR_INIT_PARAMS = "initParams";
    final static String ATTR_ABSOLUTE_PATH = ApplicationServerContext.ATTR_ABSOLUTE_PATH;
    /**
     * Initialisation order.
     * {@value}
     */

    String ATTR_INIT_ORDER = "initOrder";
}
