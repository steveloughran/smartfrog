/** (C) Copyright 2004 Hewlett-Packard Development Company, LP

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


package org.smartfrog.services.cddlm.components;

import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.logging.LogToErrImpl;

import java.util.LinkedList;

/**
 * Date: 16-Sep-2004
 * Time: 12:08:13
 */
public class BufferingLogImpl extends LogToErrImpl implements BufferingLog {

    private int limit;

    /**
     * store every
     */
    LinkedList logList;

    /**
     * Reads optional and mandatory attributes.
     *
     * @throws org.smartfrog.sfcore.common.SmartFrogException
     *          error while reading attributes
     */
    protected void readSFAttributes() throws SmartFrogException {
        super.readSFAttributes();
        if ( classComponentDescription == null ) {
            return;
        }
        limit= classComponentDescription.sfResolve(ATTR_LIMIT, limit, true);
    }


}
