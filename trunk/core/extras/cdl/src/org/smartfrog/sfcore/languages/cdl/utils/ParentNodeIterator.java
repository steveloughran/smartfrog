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

package org.smartfrog.sfcore.languages.cdl.utils;

import nu.xom.ParentNode;
import nu.xom.Node;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Java1.5 iteration support
 */
public class ParentNodeIterator implements Iterator {

    private ParentNode parent;
    private int index=0;
    private int current =- 1;

    public ParentNodeIterator(ParentNode node) {
        this.parent = node;
    }

    /**
     * Returns <tt>true</tt> if the iteration has more elements. (In other words, returns <tt>true</tt> if <tt>next</tt>
     * would return an element rather than throwing an exception.)
     *
     * @return <tt>true</tt> if the iterator has more elements.
     */
    public boolean hasNext() {
        return index<parent.getChildCount();
    }

    /**
     * Returns the next element in the iteration.  Calling this method repeatedly until the {@link #hasNext()} method
     * returns false will return each element in the underlying collection exactly once.
     *
     * @return the next element in the iteration.
     * @throws NoSuchElementException iteration has no more elements.
     */
    public Node next() {
        if(!hasNext()) {
            throw new NoSuchElementException();
        }
        current=index;
        final Node child = parent.getChild(current);
        index++;
        return child;
    }

    /**
     * Removes from the underlying collection the last element returned by the iterator (optional operation).  This
     * method can be called only once per call to <tt>next</tt>.  The behavior of an iterator is unspecified if the
     * underlying collection is modified while the iteration is in progress in any way other than by calling this
     * method.
     *
     * @throws UnsupportedOperationException if the <tt>remove</tt> operation is not supported by this Iterator.
     * @throws IllegalStateException         if the <tt>next</tt> method has not yet been called, or the <tt>remove</tt>
     *                                       method has already been called after the last call to the <tt>next</tt>
     *                                       method.
     */
    public void remove() {
        if(current<0 || current>=parent.getChildCount()) {
            throw new IllegalStateException();
        }
        parent.removeChild(current);
        current=-1;
    }

}
