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
package org.smartfrog.services.deployapi.transport.endpoints.system;

import nu.xom.Document;
import nu.xom.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.smartfrog.services.deployapi.transport.endpoints.SmartFrogAxisEndpoint;

import java.io.IOException;

/**
 * This class is *NOT* re-entrant. Create one for each deployment. created Aug
 * 4, 2004 3:58:37 PM
 */

public class RunProcessor extends SystemProcessor {
    /**
     * log
     */
    private static final Log log = LogFactory.getLog(RunProcessor.class);


    public RunProcessor(SmartFrogAxisEndpoint owner) {
        super(owner);
    }

    /**
     * override this for Xom-based processing
     *
     * @param request
     * @return the response
     * @throws java.io.IOException
     */
    public Element process(Document request) throws IOException {
        jobMustExist();

        Element response = job.run();
        return response;
    }

}
