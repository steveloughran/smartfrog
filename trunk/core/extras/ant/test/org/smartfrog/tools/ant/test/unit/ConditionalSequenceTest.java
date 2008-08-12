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
package org.smartfrog.tools.ant.test.unit;

import org.smartfrog.tools.ant.test.TaskTestBase;

/**
 * created Nov 2, 2004 2:20:45 PM
 */

public class ConditionalSequenceTest extends TaskTestBase {
    public ConditionalSequenceTest(String s) {
        super(s);
    }


    /**
     * Tears down the fixture, for example, close a network connection. This method is called after a test is executed.
     */
    public void tearDown() throws Exception {
        executeTarget("teardown");
    }

    /**
     * implementation point: return the name of a test build file
     *
     * @return the path (from the test files base dir) to the build file
     */
    protected String getBuildFile() {
        return "conditional.xml";
    }

    public void testNoProperty() {
        executeTarget("testNoProperty");
    }

    public void testIfSet() {
        executeTarget("testIfSet");
    }

    public void testIfUnset() {
        executeTarget("testIfUnset");
    }

    public void testUnlessSet() {
        executeTarget("testUnlessSet");
    }

    public void testUnlessUnset() {
        executeTarget("testUnlessUnset");
    }
}