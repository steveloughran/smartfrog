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


package org.smartfrog.test.system.examples;

import junit.framework.TestCase;
import org.smartfrog.test.SmartFrogTestBase;
import org.smartfrog.sfcore.parser.Phases;
import org.smartfrog.sfcore.common.SmartFrogException;
/*import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.security.SFClassLoader;
import org.smartfrog.sfcore.common.MessageUtil;
import org.smartfrog.sfcore.common.SmartFrogParseException;
import org.smartfrog.sfcore.common.MessageKeys;
import org.smartfrog.sfcore.parser.SFParser;
import java.io.*;
import java.net.*;*/

/**
 * JUnit test class for test cases for "schemas" example
 */
public class SchemaTest extends SmartFrogTestBase {

    private static final String FILES = "org/smartfrog/examples/schemas/";

    public SchemaTest(String s) {
        super(s);
    }
     
    public void testCaseTCP33() throws SmartFrogException {
	Phases phases = parse(FILES + "schema.sf");
        assertNotNull(phases);
    }

    public void testCaseTCP34() throws SmartFrogException {
        Phases phases = parse(FILES+ "test.sf");
        assertNotNull(phases);
    } 
}
