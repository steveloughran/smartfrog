/** (C) Copyright 2004-2006 Hewlett-Packard Development Company, LP

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


package org.smartfrog.test.system.workflow.sequence;

import org.smartfrog.test.DeployingTestBase;
import org.smartfrog.services.assertions.TestBlock;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.prim.TerminationRecord;

/**
 * @author Ashish Awasthi
 * Date: 02-Jun-2004
 */
public class SequenceTest extends DeployingTestBase {
    protected static final String FILES = "org/smartfrog/test/system/workflow/sequence/";

    public SequenceTest(String s) {
        super(s);
    }
    public void testSequence() throws Throwable {
        application=deployExpectingSuccess(FILES+"testSequence.sf","testSequence");
        TestBlock block=(TestBlock)application;
        expectSuccessfulTermination(block);
        assertAttributeEquals(application, "value", true);
    }

    public void testEmptySequence() throws Throwable {
        application = deployExpectingSuccess(FILES + "testEmptySequence.sf", "testEmptySequence");
        TestBlock block = (TestBlock) application;
        expectSuccessfulTermination(block);
    }

    public void testFailingSequence() throws Throwable {
        application = deployExpectingSuccess(FILES + "testFailingSequence.sf", "testFailingSequence");
        TestBlock block = (TestBlock) application;
        TerminationRecord record = expectAbnormalTermination(block);
        assertContains(record.description, "mid-sequence");
    }

    public void testFailingSequence2() throws Throwable {
        application = deployExpectingSuccess(FILES + "testFailingSequence2.sf",
                "testFailingSequence2");
        TestBlock block = (TestBlock) application;
        TerminationRecord record = expectAbnormalTermination(block);
        assertContains(record.description, "mid-sequence");
        assertAttributeEquals(application, "value", true);
    }

}
