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

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

import org.smartfrog.SFSystem;
import org.smartfrog.sfcore.common.SmartFrogCoreProperty;
import org.smartfrog.sfcore.common.Copying;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.common.SmartFrogResolutionException;
import org.smartfrog.sfcore.parser.SFParser;


/**
 * A reference is a list of ReferencePart objects.  An example of a reference
 * is: ATTRIB a-b, which would resolve to an attribute in one of the
 * containing contexts called a, which has to be a component that contains an
 * attribute named b. &lt;br &gt; WARNING: References are not safe for use in
 * hashtables (and therefor as attribute keys) if they are modified, since the
 * hashCode is based on the first element.
 *
 */
public class Reference implements Copying, Cloneable, Serializable {

    /**
     * Initial capacity for references. Looks up Reference.initCap (offset by
     * propBase). Defaults to 5 if not there
     * @see Reference
     */
    public static int initCap = Integer.getInteger(SmartFrogCoreProperty.initCapReference, 5).intValue();

    /**
     * Capacity increment for references. Looks up Reference.inc (offset by
     * propBase). Defaults to 2 if not there
     * @see Reference
     */
    public static int inc = Integer.getInteger(SmartFrogCoreProperty.incReference, 2).intValue();


    /** Actual reference. */
    protected Vector ref = new Vector(initCap, inc);

    /** Indicator whether reference is eager or lazy. */
    protected boolean eager = true;

    /**
     * Constructs an empty reference.
     */
    public Reference() {
    }


    /**
     * Constructs a reference with a single reference part from a String.
     *
     * @param referencePart part to be put in reference
     */
    public Reference(Object referencePart) {
        this(ReferencePart.here(referencePart));
    }

    /**
     * Constructs a reference with a single reference part from a String
     * or from a string in cannonical form using the parser.
     *
     * @param refString to be put/used in reference
     * @param parse boolean to ask for string reference to be parsed or not
     * @throws SmartFrogResolutionException if reference is not valid
     */
    public Reference(String refString, boolean parse) throws SmartFrogResolutionException{
        if (!parse) {
           ref.addElement(ReferencePart.here(refString));
        } else {
            ref = fromString(refString).ref;
        }
    }
    /**
     * Constructs a reference with a single reference part.
     *
     * @param referencePart part to be put in reference
     */
    public Reference(ReferencePart referencePart) {
        ref.addElement(referencePart);
    }

    /**
     * Utility method to create a reference from a string. Uses the SFParser
     * class.
     *
     * @param refString string representing reference
     *
     * @return new reference parsed from string
     *
     * @throws SmartFrogResolutionException if illegal reference in string
     */
    public static Reference fromString(String refString)
        throws SmartFrogResolutionException {
        try {
            return new SFParser("sf").sfParseReference(refString);
        } catch (SmartFrogException sex){
            throw (SmartFrogResolutionException)SmartFrogResolutionException.forward(sex);
        }
    }

    /**
     * Returns a copy of the reference, by cloning itself and then copying all
     * the reference parts.
     *
     * @return copy of reference
     *
     * @see org.smartfrog.sfcore.common.Copying
     */
    public Object copy() {
        Reference ret = (Reference) clone();

        for (int i = 0; i < ref.size(); i++)
            ret.ref.setElementAt(elementAt(i).copy(), i);

        return ret;
    }

    /**
     * Makes a clone of the reference. The inside ref holder is cloned, but the
     * contained reference parts are NOT. If a CloneException occurs null is
     * returned
     *
     * @return clone of reference or null on clone error
     */
    public Object clone() {
        Reference res = null;

        try {
            res = (Reference) super.clone();
            res.ref = (Vector) ref.clone();
        } catch (CloneNotSupportedException cex) {
            // should not happen
        }

        return res;
    }

    /**
     * Clone this reference from start with length elements. Reference parts
     * are NOT copied. On clone failure, null is returned
     *
     * @param start index for copy
     * @param len length of copy
     *
     * @return clone of part of reference
     */
    public Reference clone(int start, int len) {
        Reference res = (Reference) clone();

        if (res != null) {
            res.ref.removeAllElements();

            for (int i = start; i < len; i++)
                res.addElement(elementAt(i));
        }

        return res;
    }

    /**
     * Gets the eager flag for the reference.
     *
     * @return current eager flag
     *
     * @see #setEager
     */
    public boolean getEager() {
        return eager;
    }

    /**
     * Sets the eager flag for the reference.
     *
     * @param eager new eager flag
     *
     * @return old eager flag
     *
     * @see #getEager
     */
    public boolean setEager(boolean eager) {
        boolean tmp = eager;
        this.eager = eager;

        return tmp;
    }

    /**
     * Checks if this and given reference are equal. Two references are
     * considered to be equal if all their reference parts are equal.
     * Reference equality does NOT mean that the two references point to the
     * same object
     *
     * @param ref to be compared
     *
     * @return true if equal, false if not
     */
    public boolean equals(Object ref) {
        if (!(ref instanceof Reference)) {
            return false;
        }

        if (((Reference) ref).size() != size()) {
            return false;
        }

        Enumeration e1 = elements();
        Enumeration e2 = ((Reference) ref).elements();

        for (; e1.hasMoreElements();)
            if (!e1.nextElement().equals(e2.nextElement())) {
                return false;
            }

        return true;
    }

    /**
     * Returns the hashcode for this reference. Hash code for reference is made
     * out of the sum of the parts hashcodes
     *
     * @return integer hashcode
     */
    public int hashCode() {
        if (size() == 0) {
            return 0;
        } else {
            return elementAt(0).hashCode();
        }
    }

    /**
     * Returns an enumeration over the elements of the reference.
     *
     * @return enumeration of elements
     */
    public Enumeration elements() {
        return ref.elements();
    }

    /**
     * Returns the size of the reference.
     *
     * @return size of reference
     */
    public int size() {
        return ref.size();
    }

    /**
     * Returns element at index.
     *
     * @param index to look up
     *
     * @return reference to the element
     */
    public ReferencePart elementAt(int index) {
        return (ReferencePart) ref.elementAt(index);
    }

    /**
     * Sets the element at given index to a new element.
     *
     * @param elem element to set to
     * @param index index to set at
     *
     * @return previous value at given index
     */
    public ReferencePart setElementAt(ReferencePart elem, int index) {
        ReferencePart ret = elementAt(index);
        ref.setElementAt(elem, index);

        return ret;
    }

    /**
     * Returns last element in reference.
     *
     * @return last element in reference. Null if none
     */
    public ReferencePart lastElement() {
        return (ReferencePart) ref.lastElement();
    }

    /**
     * Returns first element of reference.
     *
     * @return first element
     */
    public ReferencePart firstElement() {
        return (ReferencePart) ref.firstElement();
    }

    /**
     * Adds element to the end of the reference.
     *
     * @param o reference part to add
     *
     * @return part that was added
     */
    public ReferencePart addElement(ReferencePart o) {
        ref.addElement(o);

        return o;
    }

    /**
     * Adds a reference to the end of the reference. ReferenceParts in the
     * given reference are NOT copied.
     *
     * @param ref of elements to be added
     *
     * @return Reference reference that was added to this one
     */
    public Reference addElements(Reference ref) {
        for (int i = 0; i < ref.size(); i++)
            addElement(ref.elementAt(i));

        return ref;
    }

    /**
     * Remove element from reference.
     *
     * @param o to remove from reference
     *
     * @return true if element there and removed, false otherwise
     */
    public boolean removeElement(ReferencePart o) {
        return ref.removeElement(o);
    }

    /**
     * Resolves this reference using the given reference resolver, and starting
     * at index of this reference. If the reference size is 0 the given
     * reference resolver is returned.
     *
     * @param rr ReferenceResolver to be used for resolving this reference
     * @param index index of first referencepart to start resolving at
     *
     * @return attribute found on resolving this reference
     *
     * @throws SmartFrogResolutionException reference failed to resolve
     * @throws RemoteException In case of network/rmi error
     */
    public Object resolve(ReferenceResolver rr, int index)
        throws SmartFrogResolutionException {
        // If no elements, the rr is the requested object
        if (size() == 0) {
            return rr;
        }

        // Forward the resolution request to the reference part, which will
        // resolve given this reference, its index and the resolver
        return elementAt(index).resolve(rr, this, index);
    }

    /**
     * Resolves this reference using the given remote reference resolver, and
     * starting at index of this reference. If the reference size is 0 the
     * given reference resolver is returned.
     *
     * @param rr ReferenceResolver to be used for resolving this reference
     * @param index index of first referencepart to start resolving at
     *
     * @return attribute found on resolving this reference
     *
     * @throws SmartFrogResolutionException if reference failed to resolve
     */
    public Object resolve(RemoteReferenceResolver rr, int index)
        throws SmartFrogResolutionException {
        // If no elements, the rr is the requested object
        if (size() == 0) {
            return rr;
        }

        // Forward the resolution request to the reference part, which will
        // resolve given this reference, its index and the resolver
        return elementAt(index).resolve(rr, this, index);
    }
    /**
     * Returns string representation of the reference.
     * Overrides Object.toString.
     *
     * @return String representing the reference
     */
    public String toString() {
        String res = (eager ? "" : "LAZY ");

        for (int i = 0; i < ref.size(); i++)
            res += (elementAt(i).toString(i) +
            ((i < (ref.size() - 1)) ? ":" : ""));

        return res;
    }
}
