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


package org.smartfrog.services.junit.test.system;

import org.smartfrog.services.xunit.base.TestRunner;
import org.smartfrog.services.xunit.listeners.BufferingListener;
import org.smartfrog.services.xunit.serial.Statistics;

/** Test deploying against a localhost Date: 06-Jul-2004 Time: 21:54:25 */
public class ConditionalTest extends TestRunnerTestBase {

    public ConditionalTest(String name) {
        super(name);
    }

    public void testConditionalTrue() throws Throwable {
        executeBufferedTestRun("junit-conditional-true.sf", 0, 0, 0);
    }

    public void testConditionalUnless() throws Throwable {
        executeBufferedTestRun("junit-conditional-unless.sf", 0, 0, 0);
    }

    public void testConditionalFalse() throws Throwable {
        executeBufferedTestRun("junit-conditional-false.sf", 0, 0, 0);
    }

}
