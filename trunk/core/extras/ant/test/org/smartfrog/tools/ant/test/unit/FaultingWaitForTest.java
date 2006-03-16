/** (C) Copyright 2006 Hewlett-Packard Development Company, LP

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
import org.smartfrog.tools.ant.FaultingWaitForTask;

/**
 */
public class FaultingWaitForTest extends TaskTestBase {
    private static final String REASON = "task should throw an exception";

    public FaultingWaitForTest(String s) {
        super(s);
    }

    /**
     * implementation point: return the name of a test build file
     *
     * @return the path (from the test files base dir) to the build file
     */
    protected String getBuildFile() {
        return "faultingwaitfor.xml";
    }

    public void testSuccess() {
        executeTarget("testSuccess");
    }

    public void testSuccessPropertyUnset() {
        executeTarget("testSuccessPropertyUnset");
    }
    public void testTimeoutProperty() {
        expectBuildExceptionContaining("testTimeoutProperty",
                REASON,
                FaultingWaitForTask.ERROR_TIMEOUT);
    }
    public void testTimeout() {
        expectBuildExceptionContaining("testTimeout",
                REASON,
                FaultingWaitForTask.ERROR_TIMEOUT);
    }

    public void testTimeoutMessage() {
        expectBuildExceptionContaining("testTimeoutMessage",
                REASON,
                "equality failed");
    }
}
