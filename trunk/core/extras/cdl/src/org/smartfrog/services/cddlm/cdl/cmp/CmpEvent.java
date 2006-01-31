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
package org.smartfrog.services.cddlm.cdl.cmp;

import org.smartfrog.services.cddlm.cdl.cmp.CmpComponent;


/**
 * created 23-Jun-2005 16:36:10
 * <p/>
 * CmpEvent extends CmpComponent { a_process TBD; a_target TBD; }
 */


public interface CmpEvent extends CmpComponent {

    public static final String ATTR_PROCESS = "a_process";
    public static final String ATTR_TARGET = "a_target";

}
