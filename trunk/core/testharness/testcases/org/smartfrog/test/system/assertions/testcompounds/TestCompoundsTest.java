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
package org.smartfrog.test.system.assertions.testcompounds;

import org.smartfrog.test.SmartFrogTestBase;
import org.smartfrog.test.system.assertions.AssertionsTest;
import org.smartfrog.services.assertions.SmartFrogAssertionException;

/**
 * Date: 30-Apr-2004
 * Time: 22:03:23
 */
public class TestCompoundsTest extends SmartFrogTestBase {

    private static final String FILES = "org/smartfrog/test/system/assertions/testcompounds/";

    public TestCompoundsTest(String name) {
        super(name);
    }

    public void testEmptyCompound() throws Throwable {
        deployExpectingSuccess(TestCompoundsTest.FILES +"testEmptyCompound.sf","testEmptyCompound");
    }

    public void testEmptySequence() throws Throwable {
        deployExpectingSuccess(TestCompoundsTest.FILES + "testEmptySequence.sf", "testEmptySequence");
    }

    public void testFailure() throws Throwable {
        deployExpectingSuccess(TestCompoundsTest.FILES + "testFailure.sf", "testFailure");
    }

}
