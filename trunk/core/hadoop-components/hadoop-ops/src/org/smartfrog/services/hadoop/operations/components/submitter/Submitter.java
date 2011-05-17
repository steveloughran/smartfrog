/* (C) Copyright 2008 Hewlett-Packard Development Company, LP

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
package org.smartfrog.services.hadoop.operations.components.submitter;

import org.smartfrog.services.hadoop.operations.conf.HadoopConfiguration;
import org.smartfrog.services.hadoop.operations.core.HadoopCluster;

/**
 * Created 16-Apr-2008 14:28:09
 */


public interface Submitter extends HadoopConfiguration, HadoopCluster, Job {

    /**
     * {@value}
     */
//    String ATTR_JOB = "job";

    /**
     * {@value}
     */
    String ATTR_TERMINATEJOB = "terminateJob";

    /**
     * {@value}
     */
    String ATTR_JOBID = "jobID";
    /**
     * {@value}
     */
    String ATTR_JOBURL = "jobURL";

    /**
     ping the job on liveness by checking its status
     */
    String ATTR_PINGJOB = "pingJob";

    /**
     only relevant when pingJob==true ; should we terminate when the job has finished?
     */
    String ATTR_TERMINATE_WHEN_JOB_FINISHES = "terminateWhenJobFinishes";

    /**
     * should we delete the output directory on startup?
     * {@value}
     */

    String ATTR_DELETE_OUTPUT_DIR_ON_STARTUP = "deleteOutputDirOnStartup";

    /**
     * {@value}
     */
    String ATTR_RESULTS = "results";

    /**
     * {@value}
     */
    String ATTR_JOB_TIMEOUT = "jobTimeout";

    /**
     * {@value}
     * ask for the job configuration to be dumped on a complete failure.
     * useful when you get odd configuration errors
     */
    String ATTR_DUMP_ON_FAILURE = "dumpOnFailure";

}
