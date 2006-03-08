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
import org.smartfrog.tools.ant.FunctionalTestTask;

/**
 */
public class FunctionalTestTaskTest extends TaskTestBase {

    public FunctionalTestTaskTest(String s) {
        super(s);
    }

    /**
     * implementation point: return the name of a test build file
     *
     * @return the path (from the test files base dir) to the build file
     */
    protected String getBuildFile() {
        return "functionaltest.xml";
    }
/*
[sf-functionaltest] No junit element has been defined -no tests will run
     [echo] setup
     [echo] application
     [echo] teardown
*/


    public void testSuccess() {
        executeTarget("testSuccess");
        assertAppSequenceFollowed(true);
    }

    private void assertAppSequenceFollowed(boolean noJunit) {
        String output = getFullLog();
        if(noJunit) {
            assertTrue("Not found :"+FunctionalTestTask.MESSAGE_NO_JUNIT+" in " + output,
                output.indexOf(FunctionalTestTask.MESSAGE_NO_JUNIT)>=0);
        }
        int setup=output.indexOf("setup");
        int application= output.indexOf("application");
        int teardown= output.indexOf("teardown");
        assertTrue("no setup in "+output,setup>=0);
        assertTrue("app before setup in "+output,application>setup);
        assertTrue("teardown before app in"+output, teardown>application);
    }

    public void testTimeout() {
        expectBuildExceptionContaining("testTimeout",
                "timeout",
                FaultingWaitForTask.ERROR_TIMEOUT);
        assertAppSequenceFollowed(true);
    }

    public void NotestParallelTimeout() {
        expectBuildExceptionContaining("testParallelTimeout",
                "timeout",
                "Parallel execution timed out");
        assertAppSequenceFollowed(true);
    }

    public void testFunctional() {
        executeTarget("testFunctional");
        assertAppSequenceFollowed(true);
    }

    public void testJunit() {
        executeTarget("testJunit");
        assertAppSequenceFollowed(false);
    }

    public void testApplicationFailure() {
        expectBuildExceptionContaining("testApplicationFailure",
                "failure exception not thrown",
                "failure!");
        assertAppSequenceFollowed(false);
    }
    public void testApplicationFailurePreemptsTeardown() {
        expectBuildExceptionContaining("testApplicationFailurePreemptsTeardown",
                "failure exception not thrown",
                "failure!");
        assertAppSequenceFollowed(false);
    }
}
