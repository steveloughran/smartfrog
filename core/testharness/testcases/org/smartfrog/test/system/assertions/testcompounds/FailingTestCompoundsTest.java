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
package org.smartfrog.test.system.assertions.testcompounds;

import org.smartfrog.test.DeployingTestBase;
import org.smartfrog.services.assertions.events.TestCompletedEvent;
import org.smartfrog.services.assertions.TestCompoundImpl;
import org.smartfrog.services.assertions.TestBlock;
import org.smartfrog.sfcore.prim.TerminationRecord;

/**
 *
 * Created 01-Aug-2007 10:52:25
 *
 */

public class FailingTestCompoundsTest extends DeployingTestBase {

    public static final String FILES= TestCompoundsEventsTest.FILES;

    public FailingTestCompoundsTest(String name) {
        super(name);
    }


    public void testFailureWrongMessageNested() throws Throwable {
        TestCompletedEvent event = expectSuccessfulTestRun(FILES, "testFailureWrongMessageNested");
        TerminationRecord status = event.getStatus();
    }

    public void testUnexpectedFailure() throws Throwable {
        application = deployExpectingSuccess(FILES + "testUnexpectedFailure" + ".sf", "testUnexpectedFailure");
        expectAbnormalTermination((TestBlock) application);
    }

    public void testFailureWrongMessage() throws Throwable {
        application = deployExpectingSuccess(FILES + "testFailureWrongMessage" + ".sf", "testFailureWrongMessage");
        TerminationRecord record = expectSuccessfulTermination((TestBlock) application);
        assertTerminationRecordContains(record, TestCompoundImpl.TEST_FAILED_WRONG_STATUS, null, null);
    }
}
