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
package org.smartfrog.services.ant.test.system.antbuild;

import org.smartfrog.test.DeployingTestBase;

/**
 * JUnit test class for test cases related to Ant
 */
public class AntBuildTest
        extends DeployingTestBase {

    private static final String FILES = "/org/smartfrog/services/ant/test/system/antbuild";

    public AntBuildTest(String s) {
        super(s);
    }

    public void testBuildFile() throws Throwable {
        expectSuccessfulTestRun(FILES,"testBuildFile");
    }

    public void testProperties() throws Throwable {
        expectSuccessfulTestRun(FILES, "testProperties");
    }

    public void testPropertyPassdown() throws Throwable {
        expectSuccessfulTestRun(FILES, "testPropertyPassdown");
    }
    
    public void testSkipUnimplementedTargets() throws Throwable {
        expectSuccessfulTestRun(FILES, "testSkipUnimplementedTargets");
    }
}