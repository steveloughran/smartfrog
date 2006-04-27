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
package org.smartfrog.services.junit.test;

import org.smartfrog.services.junit.data.Statistics;
import org.smartfrog.services.junit.TestRunner;
import org.smartfrog.services.junit.listeners.XmlListener;
import org.smartfrog.services.junit.listeners.XmlListenerFactory;
import org.smartfrog.sfcore.prim.Prim;

import java.io.File;

/**
 * created Nov 22, 2004 4:31:45 PM
 */

public class DeployedXMLListenerTest extends TestRunnerTestBase {
    public static final String TEST_SUITE_COMPONENT_NAME = "tests";
    public static final String SUITENAME = "tests";

    public DeployedXMLListenerTest(String name) {
        super(name);
    }

    public void testAll() throws Throwable {
        String url;
        Prim deploy = null;
        url = "/files/xml-all.sf";

        int seconds = getTimeout();
        try {
            deploy = deployExpectingSuccess(url, "XmlTest");
            TestRunner runner = (TestRunner) deploy;
            assertTrue(runner != null);
            XmlListenerFactory listenerFactory = null;
            listenerFactory =
                    (XmlListenerFactory) deploy.sfResolve(
                            TestRunner.ATTR_LISTENER,
                            listenerFactory,
                            true);
            boolean finished = spinTillFinished(runner, seconds);
            assertTrue("Test run timed out", finished);

            String path = listenerFactory.lookupFilename(SUITENAME);
            assertNotNull("path of test suite " + SUITENAME, path);

            assertTrue("File does not exist " + path, new File(path).exists());

            //now fetch from the tests
            Prim tests;
            tests =
                    deploy.sfResolve(TEST_SUITE_COMPONENT_NAME,
                            (Prim) null,
                            true);
            String output = tests.sfResolve(XmlListener.ATTR_XMLFILE,
                    "",
                    true);
            File xmlfile = new File(output);
            assertTrue("File " + output + " not found", xmlfile.exists());

            //validate the file
            validateXmlLog(xmlfile);

            Statistics statistics = runner.getStatistics();
            /*
            assertEquals("statistics.testRun==1", 1, statistics.getTestsRun());
            assertEquals("statistics.errors==0", 0, statistics.getErrors());
            assertEquals("statistics.failures==0",
                    0,
                    statistics.getFailures());
            */
            //TODO: more tests

        } finally {
            terminateApplication(deploy);
        }

    }

}
