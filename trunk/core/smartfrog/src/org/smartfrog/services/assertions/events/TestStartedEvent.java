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
package org.smartfrog.services.assertions.events;

import org.smartfrog.sfcore.workflow.events.LifecycleEvent;
import org.smartfrog.sfcore.prim.Prim;

/**
 *
 * Created 16-Jul-2007 13:21:23
 *
 */

public class TestStartedEvent extends LifecycleEvent {


    /**
     * child component, may be null
     */
    private Prim child;

    public TestStartedEvent() {
    }

    public TestStartedEvent(Prim component) {
        super(component);
    }

    /**
     * Construct with both component and child
     * @param component component starting the tests
     * @param child any child component that is the test action
     */
    public TestStartedEvent(Prim component,Prim child) {
        this(component);
        this.child=child;
    }


    /**
     * Get the child
     * @return
     */
    public Prim getChild() {
        return child;
    }

    /** {@inheritDoc} */
    public String getEventName() {
        return "TestStartedEvent";
    }


}
