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

package org.smartfrog.services.cddlm.cdl.functions;

import org.smartfrog.sfcore.languages.sf.PhaseAction;
import org.smartfrog.sfcore.languages.sf.SmartFrogCompileResolutionException;
import org.smartfrog.sfcore.languages.sf.Function;
import org.smartfrog.sfcore.languages.sf.functions.BaseUnaryOperator;

import java.io.File;

/**
 * something to convert paths on the fly to the local filesys. 
 * Remember that conversion only takes place locally
 */
public class PatchPath extends Function implements PhaseAction {

    /**
     * extract the boolean value
     *
     * @param a a boolean
     * @return the negation
     */
    protected Object doOperator(Object a) throws SmartFrogCompileResolutionException {
        String path = a.toString();
        char local=File.separatorChar;
        if(local=='\\') {
            path=path.replace('/','\\');
        }
        if (local == '/') {
            path = path.replace('\\', '/');
        }
        return path;
    }

}
