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
package org.smartfrog.test.system.filesystem;

import org.smartfrog.test.SmartFrogTestBase;
import org.smartfrog.sfcore.prim.Prim;

import java.io.File;

/**
 * created 18-May-2004 13:28:43
 */

public class TempFileTest extends SmartFrogTestBase {

    private static final String FILES = "org/smartfrog/test/system/filesystem/";

    public TempFileTest(String name) {
        super(name);
    }

    public void testEmpty() throws Throwable {
        deployExpectingException(FILES + "tempFileTestEmpty.sf",
                "tempFileTestEmpty",
                        "SmartFrogDeploymentException", null,
                        "SmartFrogCompileResolutionException", "non-optional attribute 'prefix' is missing");
    }

    public void testWorking() throws Throwable {
        Prim application=deployExpectingSuccess(FILES + "tempFileTestWorking.sf", "tempFileTestWorking");
        String filename = application.sfResolve("filename", (String) null, true);
        String suffix = application.sfResolve("suffix", (String) null, true);
        String prefix = application.sfResolve("prefix", (String) null, true);
        File file = new File(filename);
        assertTrue("found " + suffix + " in " + filename, filename.endsWith(suffix));
        assertTrue("found "+prefix+" in "+filename,file.getName().indexOf(prefix)==0);
        //now verify we clean up

    }

}
