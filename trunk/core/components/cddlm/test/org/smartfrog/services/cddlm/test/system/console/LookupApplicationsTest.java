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


package org.smartfrog.services.cddlm.test.system.console;

import org.apache.axis.AxisFault;
import org.apache.axis.types.URI;
import org.cddlm.client.console.Lookup;
import org.cddlm.client.generated.api.types.ApplicationStatusType;
import org.smartfrog.services.cddlm.generated.faults.FaultCodes;

/**
 * Date: 02-Sep-2004 Time: 20:40:57
 */
public class LookupApplicationsTest extends ConsoleTestBase {

    Lookup operation;
    public static final String INVALID_URI_NAME = "http://invalid.org/lookup";

    /**
     * Sets up the fixture, by creating an operation
     */
    protected void setUp() throws Exception {
        super.setUp();
        operation = new Lookup(getBinding(), getOut());
    }

    public void testLookupBadName() throws Exception {
        try {
            URI uri = operation.lookupApplication("noname");
        } catch (AxisFault fault) {
            assertFaultMatches(fault,
                    FaultCodes.FAULT_NO_SUCH_APPLICATION,
                    null);
        }
    }

    public void testStatusOfMissingApp() throws Exception {
        URI uri = new URI(INVALID_URI_NAME);
        try {
            ApplicationStatusType status = operation.lookupApplicationStatus(
                    uri);
        } catch (AxisFault fault) {
            assertFaultMatches(fault,
                    FaultCodes.FAULT_NO_SUCH_APPLICATION,
                    INVALID_URI_NAME);
        }
    }
}
