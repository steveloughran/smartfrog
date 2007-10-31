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
package org.smartfrog.sfcore.utils;

import org.smartfrog.sfcore.common.SmartFrogInitException;

import java.util.List;
import java.util.ArrayList;
import java.util.Vector;

/**
 *
 * List utility functions
 *
 */

public final class ListUtils {
    public static final String EMPTY_LIST_ELEMENT = "Empty list element";
    public static final String ERROR_WRONG_WIDTH = "Wrong number of list elements in sublist ";


    private ListUtils() {
    }

    /**
     * take a vector of name value pairs like [["a",true],["b",3]] and create a list like
     * ["-Da=true","-Db=3"] with configurable prefix, joiner and suffix strings.
     * </p>
     * the return type is a vector
     *
     * @param source list source. All elements must contain a list, itself two elements long.
     * @param prefix optional prefix for every element
     * @param joiner optional string to use between each pair
     * @param suffix optional suffix to use at the end
     * @param skipEmptyElements should empty elements be skipped, or reported as an error
     * @return a merged array or null if an empty list came in.
     * @throws SmartFrogInitException if a list is the wrong width, type or missing
     */
    public static Vector<String> join(List source, String prefix, String joiner, String suffix,
        boolean skipEmptyElements) throws SmartFrogInitException {
        if (source == null) {
            return null;
        }
        if (prefix == null) {
            prefix = "";
        }
        if (joiner == null) {
            joiner = "";
        }
        if (suffix == null) {
            suffix = "";
        }
        Vector<String> results = new Vector<String>(source.size());
        for (Object element : source) {
            if (!(element instanceof List)) {
                throw new SmartFrogInitException("Not a list: " + element);
            }
            List subvector = (List) element;
            int subsize = subvector.size();
            if (subsize == 0) {
                if(skipEmptyElements) {
                    //empty sublist; skip
                    continue;
                } else {
                    throw new SmartFrogInitException(EMPTY_LIST_ELEMENT);
                }

            }
            if (subsize != 2) {
                throw new SmartFrogInitException(ERROR_WRONG_WIDTH + subvector);
            }
            String key = subvector.get(0).toString();
            //take any value and stringify it -we dont care about its underlying type
            String value = subvector.get(1).toString();
            String entry = prefix + key + joiner + value + suffix;
            results.add(entry);
        }
        return results;
    }
}
