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
package org.smartfrog.tools.ant;

/**
 * This test tests the daemon starting.
 * This is quite a functional test, as it runs new processes and things, which
 * is why there is complexity in cleanup. Even so, there is non-zero
 * risk that bad things are happening, so we test for that, too.
 * @author steve loughran
 *         created 01-Mar-2004 16:19:21
 */

public class StartTest extends TaskTestBase {

    /**
     * constructor
     * @param s
     */
    public StartTest(String s) {
        super(s);
    }

    /**
     * implementation point: return the name of a test build file
     *
     * @return the path (from the test files base dir) to the build file
     */
    protected String getBuildFile() {
        return "start.xml" ;
    }

    /**
     * shut down anything running
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        executeTarget("teardown");
    }

    /**
     * test for bad settings
     */
    public void testIncompatibleSettings() {
        expectBuildExceptionContaining("testIncompatibleSettings",
                "spawn and timeout",
                "have used an attribute which is not compatible with spawn");
    }

    /**
     * test for a bad hostname raising an error
     */
    public void testBadHost() {
        expectBuildExceptionContaining("testBadHost", "host parameter",
                "failed to start the smartfrog daemon");
        assertLogContaining("java.net.UnknownHostException: no-such-hostname");
    }

    public void testDefaults() {
        expectBuildExceptionContaining("testDefaults", "timeout",
                "Timeout:");
        assertRootProcessInLog();
    }

    /**
     * Look for this trace
     * [sf-startdaemon] Standalone SmartFrog daemon started
     [sf-stopdaemon] SmartFrog 3.01.003_alpha
     [sf-stopdaemon] (C) Copyright 1998-2004 Hewlett-Packard Development Company, LP
     [sf-stopdaemon] Warning: SmartFrog security is NOT active
     [sf-stopdaemon] SmartFrog daemon terminated


     */
    public void testSpawn() {
        executeTarget("testSpawn");
        assertLogContaining("Standalone SmartFrog daemon started");
        assertLogContaining("SmartFrog daemon terminated");
    }



    public void testEmptyApplication() {
        expectBuildExceptionContaining("testEmptyApplication", "anon app",
                "no application name");
    }

    public void testAnonApplication() {
        expectBuildExceptionContaining("testAnonApplication", "anon app",
                "no application name");
    }

    public void testDatalessApplication() {
        expectBuildExceptionContaining("testDatalessApplication", "no descriptor",
                "no descriptor provided");
    }

    public void testBadFile() {
        expectBuildExceptionContaining("testBadFile", "missing file",
                "missing-file.sf does not exist");
    }

    public void testRunFile() {
        executeTarget("testRunFile");
        assertLogContaining("failed to deploy 'app' component");
        assertLogContaining("Reference not found, Unresolved Reference: sfClass");
        assertLogContaining("SmartFrog [rootProcess] dead");
    }

    public void testResource() {
        executeTarget("testResource");
        assertLogContaining("COUNTER: hello - here is a constructed message");
        assertLogContaining("value is 99");
        assertLogContaining("goodbye");
        assertLogContaining("[[elementA, elementB], Message from outerVector, [value is , 99]]");
        assertLogContaining("1");
        assertTerminationInLog();
    }

    private void assertTerminationInLog() {
        assertLogContaining("SmartFrog [rootProcess] dead");
    }
}
