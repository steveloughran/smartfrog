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

package org.smartfrog.sfcore.reference;

import org.smartfrog.sfcore.common.SFMarshalledObject;
import org.smartfrog.sfcore.common.SmartFrogResolutionException;
import org.smartfrog.sfcore.logging.LogFactory;
import org.smartfrog.sfcore.logging.LogSF;

/**
 * Implements the most basic of reference parts. This reference part knows how
 * to resolve itself to the value of a given id in a given reference resolver.
 *
 */
public class HereReferencePart extends ReferencePart {
    /** Base string representation of this part (HERE). */
    public static final String HERE = "HERE";

    /** Value for here part. */
    private Object value = null;

    public Object getValue() {
            return value;
    }

    /**
     * Sets new value and returs old value.
     * @param value Object
     * @return Object
     */
    public Object setValue (Object value){
        Object oldValue = getValue();
        this.value=value;
        return oldValue;
    }


    /**
     * Constructs HereReferencePart with a here part.
     *
     * @param v value for here part
     */
    public HereReferencePart(Object v) {
        this.setValue(v);
    }

    /**
     * Converts this reference part to an AttribReferencePart. This is used by
     * type references, which get their first part converted to an attrib
     * reference part in order to do relatvice type lookups
     *
     * @return attrib reference part
     */
    public ReferencePart asAttribReferencePart() {
        return ReferencePart.attrib(getValue());
    }

    /**
     * Returns a string representation of the reference part.
     * Implements abstract method ReferencePart.toString.
     * @return stringified reference part
     */
    public String toString() {
        if (value == null) {
            return "";
        }

        return HERE + " " + getValue().toString();
    }

    /**
     * Return a string which is a representation of the reference part in a reference in the
     * index position given.
     *
     * @param index the position in the reference
     * @return the representation
     */
    public String toString(int index) {
	if (value == null) return "";
	if (index == 0)
	    return HERE + ' ' + getValue().toString();
	else
	    return getValue().toString();
    }

    /**
     * Returns hashcode of this part. This is the hashCode of the stored value
     *
     * @return hash code for part
     */
    public int hashCode() {
        return getValue().hashCode();
    }

    /**
     * Compares this reference part with another one. Equality means that the
     * type and value are equal
     *
     * @param refPart to be compared to
     *
     * @return true if equal, false if not
     */
    public boolean equals(Object refPart) {
        return refPart.getClass().equals(this.getClass()) &&
        ((HereReferencePart) refPart).getValue().equals(getValue());
    }

    /**
     * Resolves this reference part using the reference resolver. The
     * originating reference and index are needed to enable request forwarding
     *
     * @param rr reference resolver
     * @param r reference which this part sits in
     * @param index index of this reference part in r
     *
     * @return the attribute found on resolution
     *
     * @throws SmartFrogResolutionException if failed to resolve reference
     */
    public Object resolve(ReferenceResolver rr, Reference r, int index)
        throws SmartFrogResolutionException {
        // Find here
        Object result = rr.sfResolveHere(getValue(),false);

        if (result == null) {
            throw SmartFrogResolutionException.notFound(r, null);
        }

        // if reference ask rr to resolve it (chaining)
        if (result instanceof Reference) {
            result = rr.sfResolve((Reference) result, 0);
        }

        // If the end we are there!
        if (index == (r.size() - 1)) {
            return result;
        }

        // Else forward on to result
        return forwardReference(result, r, index + 1);
    }

    /**
     * Resolves this reference part using the remote reference resolver. The
     * originating reference and index are needed to enable request forwarding
     *
     * @param rr reference resolver
     * @param r reference which this part sits in
     * @param index index of this reference part in r
     *
     * @return the attribute found on resolution
     *
     * @throws SmartFrogResolutionException if failed to resolve reference
     */
    public Object resolve(RemoteReferenceResolver rr, Reference r, int index)
        throws SmartFrogResolutionException {
        try {
            // Find here
            Object result = rr.sfResolveHere(getValue(),false);

            if (result == null) {
                throw SmartFrogResolutionException.notFound(r, null);
            }

            // if reference ask rr to resolve it (chaining)
            if (result instanceof Reference) {
                result = rr.sfResolve((Reference) result, 0);
            }

            // If the end we are there!
            if (index == (r.size() - 1)) {
                //Marshall!
                if (!(result instanceof SFMarshalledObject)) {
                    return new SFMarshalledObject(result);
                } else {
                    return result;
                }
            }

            // Else forward on to result
            return forwardReference(result, r, index + 1);

        } catch (Exception ex){
            throw (SmartFrogResolutionException)SmartFrogResolutionException.forward(ex);
        }
    }

    /**
     *  To get the sfCore logger
     * @return Logger implementing LogSF and Log
     */
    public LogSF sfGetProcessLog() {
       LogSF sflog  =  LogFactory.sfGetProcessLog();
       return sflog;
    }
}
