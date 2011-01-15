/** (C) Copyright 2004 Hewlett-Packard Development Company, LP

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


package org.smartfrog.test.system.components.net;

import org.smartfrog.test.SmartFrogTestBase;
import org.smartfrog.test.DeployingTestBase;
import org.smartfrog.sfcore.prim.Prim;

/**
 * JUnit test class for test cases related to "telnet" component
 */
public class TelnetFailureTest
    extends DeployingTestBase {

    private static final String FILES = "org/smartfrog/test/system/components/net/";

    public TelnetFailureTest(String s) {
        super(s);
    }

    public void testCaseTCN68() throws Throwable {
        expectSuccessfulTestRunOrSkip(FILES, "tcn68");
    }

    public void testCaseTCN69() throws Exception {
        deployExpectingException(FILES+"tcn69.sf",
                                 "tcn69",
                                 EXCEPTION_DEPLOYMENT,
                                 null,
                                 EXCEPTION_LINKRESOLUTION,
                                 "error in schema: non-optional attribute 'host' is missing");
    }

    public void testCaseTCN70() throws Exception {
        deployExpectingException(FILES+"tcn70.sf",
                                 "tcn70",
                                 EXCEPTION_DEPLOYMENT,
                                 null,
                                 EXCEPTION_LINKRESOLUTION,
                                 "error in schema: non-optional attribute 'username' is missing");
    }

    public void testCaseTCN71() throws Exception {
        deployExpectingException(FILES+"tcn71.sf",
                                 "tcn71",
                                 EXCEPTION_DEPLOYMENT,
                                 null,
                                 EXCEPTION_LINKRESOLUTION,
                                 "error in schema: non-optional attribute 'commands' is missing");
    }

    public void testCaseTCN72() throws Throwable {
        expectSuccessfulTestRunOrSkip(FILES, "tcn72");
    }

    public void testCaseTCN73() throws Exception {
        deployExpectingException(FILES+"tcn73.sf",
                                 "tcn73",
                                 EXCEPTION_DEPLOYMENT,
                                 null,
                                 EXCEPTION_LINKRESOLUTION,
                                 "error in schema: non-optional attribute 'ftpHost' is missing");
    }

    public void testCaseTCN74() throws Exception {
        deployExpectingException(FILES+"tcn74.sf",
                                 "tcn74",
                                 EXCEPTION_DEPLOYMENT,
                                 null,
                                 EXCEPTION_LINKRESOLUTION,
                                 "error in schema: non-optional attribute 'username' is missing");
    }

    public void testCaseTCN75() throws Exception {
        deployExpectingException(FILES+"tcn75.sf",
                                 "tcn75",
                                 EXCEPTION_DEPLOYMENT,
                                 null,
                                 EXCEPTION_LINKRESOLUTION,
                                 "error in schema: non-optional attribute 'localFiles' is missing");
    }

    public void testCaseTCN76() throws Exception {
        deployExpectingException(FILES+"tcn76.sf",
                                 "tcn76",
                                 EXCEPTION_DEPLOYMENT,
                                 null,
                                 EXCEPTION_LINKRESOLUTION,
                                 "error in schema: non-optional attribute 'remoteFiles' is missing");
    }
}

