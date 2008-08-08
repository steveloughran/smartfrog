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

import java.io.File;

/**
 * created 21-Jun-2004 17:25:24
 */

public class MkdirTest  extends SmartFrogTestBase {

    private static final String FILES = TouchFileTest.FILES;

    public MkdirTest(String name) {
        super(name);
    }

    /**
     * test case
     * @throws Throwable on failure
     */

    public void testWorking() throws Throwable {
        application = deployExpectingSuccess(FILES + "mkdirTestWorking.sf", "mkdirFileTestWorking");
        File file = null;
        try {
            String filename = application.sfResolve("newdir",
                    (String) null,
                    true);
            file = new File(filename);
            //now verify we clean up
            assertIsDirectory(file, filename);
            
        } finally {
            //cleanup
            if (file!=null) {
                file.delete();
                assertFalse("Should be able to clean up after test. Directory remaining: " + file,
                        file.exists());
            }
        }
    }

    private void assertIsDirectory(File file, String filename) {
        assertTrue("Directory should exist: " + filename, file.exists());
        assertTrue("Should be a directory: " + filename, file.isDirectory());
    }

    public void testCleanOnStartup() throws Throwable {
        String dirname = System.getProperty("java.io.tmpdir")
                + System.getProperty("file.separator") + "directory-to-be-cleaned";
        File dir = new File(dirname);

        try {
            assertFalse("Temp directory should not exist yet", dir.exists());
            assertTrue("Should be able to create a new temporary directory: " + dirname,
                    dir.mkdir());
            File dirInsideDir = new File(dir, "testDir");
            assertTrue("Should be able to create a new directory: " + dirInsideDir,
                    dirInsideDir.mkdir());
            File fileInsideDir = new File(dir, "testFile.txt");
            assertTrue("Should be able to create a new file inside directory: " + fileInsideDir,
                    fileInsideDir.createNewFile());

            application = deployExpectingSuccess
                    (FILES + "mkdirCleanOnStartup.sf", "mkdirCleanOnStartupTest");

            assertIsDirectory(dir, dirname);
            assertEquals("The directory should be cleaned up and not contain anything",
                    0, dir.listFiles().length);
        } finally {
            dir.delete();
            assertFalse("Should be able to clean up after test. Directory remaining: " + dir,
                    dir.exists());
        }
    }
}