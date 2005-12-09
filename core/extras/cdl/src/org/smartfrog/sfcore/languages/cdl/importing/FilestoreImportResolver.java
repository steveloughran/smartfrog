/** (C) Copyright 2005 Hewlett-Packard Development Company, LP

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
package org.smartfrog.sfcore.languages.cdl.importing;

import java.io.File;
import java.io.IOException;
import java.io.Closeable;
import java.util.Hashtable;
import java.net.URL;

/**
 * This component resolves URLs for import against a filestore (somewhere)
 * created 09-Dec-2005 16:31:58
 */

public class FilestoreImportResolver extends BaseImportResolver implements Closeable {

    private File directory;

    private Hashtable<String,File> entries=new Hashtable<String, File>();

    public FilestoreImportResolver(File directory) {
        this.directory = directory;
        directory.mkdirs();
    }


    /**
     * map the path to a URI. For in-classpath resolution, URLs of the type
     * returned by
     *
     * @param path
     * @return the URL to the resource
     * @throws java.io.IOException on failure to locate or other problems
     */
    public URL resolveToURL(String path) throws IOException {
        File mapping = entries.get(path);
        if(mapping==null) {
            return super.resolveToURL(path);
        } else {
            return mapping.toURL();
        }
    }

    public File createEntry(String path,String suffix) throws IOException {
        assert path !=null;
        assert suffix != null;
        File tempFile = File.createTempFile("import", suffix, directory);
        tempFile.deleteOnExit();
        entries.put(path,tempFile);
        return tempFile;
    }


    /**
     * Close this filestore by deleting all entries
     *
     * @throws java.io.IOException if an I/O error occurs
     */
    public void close() throws IOException {
        for(File f:entries.values()) {
            f.delete();
        }

    }

    /**
     * Returns a string representation of the object.
     * @return a string representation of the object.
     */
    public String toString() {
        return "FilestoreImportResolver in "+directory+" with "+entries.size()+" entries";
    }
}
