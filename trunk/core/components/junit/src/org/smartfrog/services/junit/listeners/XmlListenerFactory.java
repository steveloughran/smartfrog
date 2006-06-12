/** (C) Copyright 2005-2006 Hewlett-Packard Development Company, LP

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


package org.smartfrog.services.junit.listeners;

import org.smartfrog.services.junit.TestListenerFactory;

import java.rmi.RemoteException;

/**
 * Date: 12-Jun-2004 Time: 00:16:06
 */

public interface XmlListenerFactory extends TestListenerFactory {

    /**
     * name of a directory for output
     * {@value}
     */
    String ATTR_OUTPUT_DIRECTORY = "outputDirectory";

    /**
     * flag to include hostname logic in file/dir choice
     * {@value}
     */
    String ATTR_USE_HOSTNAME = "useHostname";

    /**
     * flag to include process name logic in file/dir choice
     * {@value}
     */
    String ATTR_USE_PROCESSNAME = "useProcessname";

    /**
     * {@value}
     */
    String INDEX_FILE="indexFile";

    /**
     * {@value}
     */
    String ATTR_PREAMBLE = "preamble";


    /**
     * {@value}
     */
    String ATTR_SUFFIX = "suffix";
    /**
     * frequency of publish.
     * {@value}
     */
    String ATTR_PUBLISH_FREQUENCY = "publishFrequency";

    /**
     * map from a test suite name to a filename
     *
     * @param hostname host that the suite ran on
     * @param suitename test suite
     * @return name of output file, or null for no match
     * @throws RemoteException
     */
    public String lookupFilename(String hostname,
                                 String suitename) throws RemoteException;

}
