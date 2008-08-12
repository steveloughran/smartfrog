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
package org.smartfrog.tools.ant;

import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.types.Resource;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

/**
 * Class to help with resources
 * <p/>
 * Created 19-Oct-2007 16:11:10
 *
 */

public class ResourceHelper {

    protected ProjectComponent owner;


    /**
     * construct with information about a file
     * @param owner owning component (For logging etc)
     */
    public ResourceHelper(ProjectComponent owner) {
        this.owner = owner;
    }

    /**
     * Save an Ant resource to a file
     * @param resource resource instance
     * @param target target file
     * @throws BuildException if the resource doesn't provide an input stream, or on any IO Exception.
     */
    public void saveResourceToFile(Resource resource,File target) {
        BufferedReader reader = null;
        BufferedWriter dest=null;
        try {
            InputStream inputStream = resource.getInputStream();
            if(inputStream==null) {
                throw new BuildException("Resource has no input stream "+resource);
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String policy = FileUtils.readFully(reader);
            dest = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(target)));
            dest.write(policy);
        } catch (IOException e) {
            throw new BuildException(e);
        } finally {
            FileUtils.close(reader);
            FileUtils.close(dest);
        }
    }
}