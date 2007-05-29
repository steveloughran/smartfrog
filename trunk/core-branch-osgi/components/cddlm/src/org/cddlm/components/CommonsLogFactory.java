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
package org.cddlm.components;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.smartfrog.sfcore.prim.Prim;

/**
 * Place for commons log instaces
 * created 29-Apr-2004 17:19:36
 */

public class CommonsLogFactory {
    /**
     * create a new log for a prim instance
     *
     * @param instance
     * @return
     */
    public static Log createLog(Prim instance) {
        return LogFactory.getLog(instance.getClass().getName());
    }

    /**
     * create a log of the given name
     *
     * @param instance
     * @param name
     * @return
     */
    public static Log createLog(Prim instance, String name) {
        return LogFactory.getLog(name);
    }

}
