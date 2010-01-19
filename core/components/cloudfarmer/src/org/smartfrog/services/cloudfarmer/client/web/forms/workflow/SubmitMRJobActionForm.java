/* (C) Copyright 2009 Hewlett-Packard Development Company, LP

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

package org.smartfrog.services.cloudfarmer.client.web.forms.workflow;

import org.smartfrog.services.cloudfarmer.client.web.model.RemoteDaemon;
import org.smartfrog.services.cloudfarmer.client.web.model.workflow.Workflow;
import org.smartfrog.sfcore.common.SmartFrogException;

import java.io.IOException;


/**
 * Submit a basic MR job
 */
public class SubmitMRJobActionForm extends AbstractQueueJobActionForm {

    private static final long serialVersionUID = -8368336957081183982L;
    private String inputFile;
    private String outputFile;
    private String mapper;
    private String reducer;
    private String combiner;

    public String getInputFile() {
        return inputFile;
    }

    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }

    public String getMapper() {
        return mapper;
    }

    public void setMapper(String mapper) {
        this.mapper = mapper;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public String getReducer() {
        return reducer;
    }

    public void setReducer(String reducer) {
        this.reducer = reducer;
    }

    public String getCombiner() {
        return combiner;
    }

    public void setCombiner(String combiner) {
        this.combiner = combiner;
    }

    /**
     * Call this to queue the job. subclasses should call the type-specific RemoteDaemon.queue method
     *
     * @throws SmartFrogException SmartFrog problems
     * @throws IOException        Network or other I/O issues
     */
    @Override
    public Workflow queue(RemoteDaemon daemon) throws SmartFrogException, IOException {
        return daemon.queue(this);
    }
}