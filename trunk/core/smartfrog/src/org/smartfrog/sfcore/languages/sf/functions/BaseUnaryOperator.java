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

package org.smartfrog.sfcore.languages.sf.functions;

import java.util.Enumeration;

import org.smartfrog.sfcore.common.MessageKeys;
import org.smartfrog.sfcore.common.MessageUtil;
import org.smartfrog.sfcore.languages.sf.PhaseAction;
import org.smartfrog.sfcore.languages.sf.SmartFrogCompileResolutionException;

/**
 * Defines the baseoperator for the operator functions.
 */
public abstract class BaseUnaryOperator extends BaseFunction implements PhaseAction {

    /**
     * The method to implement the functionality of any operator.
     *
     * @param a Object 1 for operator
     *
     * @return an Object
     *  */
    protected abstract Object doOperator(Object a) throws SmartFrogCompileResolutionException;

    /** Implements the functionality of base operator. */
    protected Object doFunction() throws SmartFrogCompileResolutionException {
	Object data = context.get("data");

	if (data == null)
	    throw new SmartFrogCompileResolutionException(MessageUtil.formatMessage(MISSING_PARAMETER, "data"), 
							  null, name, "function", null);

	return doOperator(data);
    }
}
