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
package org.smartfrog.services.filesystem.csvfiles;

import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.common.SmartFrogDeploymentException;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.prim.PrimImpl;
import org.smartfrog.sfcore.prim.TerminationRecord;
import org.smartfrog.sfcore.utils.SmartFrogThread;
import org.smartfrog.sfcore.utils.ComponentHelper;
import org.smartfrog.sfcore.reference.Reference;

import java.rmi.RemoteException;
import java.util.Vector;

/**
 * component to test the file read operations; lets you validate headers and such like Created 20-Feb-2008 15:50:56
 */

public class CSVColumnReader extends PrimImpl {

    private CSVFileRead source;
    private ReaderThread reader;

    /**
     * Source component
     */
    public static final String ATTR_SOURCE = "source";

    /**
     * Column number
     */
    public static final String ATTR_COLUMN = "column";
    /**
     * optional target prim to set the attribute on
     */
    public static final String ATTR_TARGET = "target";
    /**
     * name of the attribute to set
     */
    public static final String ATTR_TARGET_ATTRIBUTE = "targetAttribute";

    /**
     * skip empty "" entries?
     */
    public static final String ATTR_SKIP_EMPTY_FIELDS = "skipEmptyFields";

    /**
     * skip empty "" entries?
     */
    public static final String ATTR_SKIP_NARROW_LINES = "skipNarrowLines";
    /**
     * {@value}
     */
    private static final String ATTR_TRIM_FIELDS = "trimFields";
    /**
     * the attribute we set
     */
    public static final String ATTR_RESULT = "result";
    private Prim target;
    private boolean skipEmptyFields;
    private String targetAttribute;
    private int column;
    private boolean skipNarrowLines;
    private boolean trimFields;


    public CSVColumnReader() throws RemoteException {
    }

    /**
     * Can be called to start components. Subclasses should override to provide functionality Do not block in this call,
     * but spawn off any main loops!
     *
     * @throws SmartFrogException failure while starting
     * @throws RemoteException    In case of network/rmi error
     */
    public synchronized void sfStart() throws SmartFrogException, RemoteException {
        super.sfStart();
        source = (CSVFileRead) sfResolve(ATTR_SOURCE, (Prim) null, true);
        target = sfResolve(ATTR_TARGET, (Prim) null, false);
        targetAttribute = sfResolve(ATTR_TARGET_ATTRIBUTE, "", target!=null);
        trimFields = sfResolve(ATTR_TRIM_FIELDS, false, true);
        skipEmptyFields = sfResolve(ATTR_SKIP_EMPTY_FIELDS, false, true);
        skipNarrowLines = sfResolve(ATTR_SKIP_NARROW_LINES, false, true);
        column = sfResolve(ATTR_COLUMN, 0, true);
        reader = new ReaderThread(source);
        reader.start();
    }


    /**
     * Provides hook for subclasses to implement useful termination behavior. Deregisters component from local process
     * compound (if ever registered)
     *
     * @param status termination status
     */
    protected synchronized void sfTerminateWith(TerminationRecord status) {
        super.sfTerminateWith(status);
        SmartFrogThread.requestThreadTermination(reader);

    }

    private class ReaderThread extends SmartFrogThread {
        /**
         * data source
         */
        private CSVFileRead source;

        /**
         * Create a basic thread
         *
         * @see Thread#Thread(ThreadGroup,Runnable,String)
         */
        private ReaderThread(CSVFileRead source) {
            this.source = source;
        }

        /**
         * read the thread in, validate the values, then maybe terminate the component
         *
         * @throws Throwable if anything went wrong
         */
        public void execute() throws Throwable {
            String[] line;
            Vector<String> result=new Vector<String>();
            int count = 0;
            source.start();
            while ((line = source.getNextLine()) != null) {
                if (isTerminationRequested()) {
                    //bail out completely
                    return;
                }
                int w = line.length;
                if (w < column) {
                    //too narrow for this column
                    if (skipNarrowLines) {
                        continue;
                    } else {
                        throw new SmartFrogDeploymentException("Too narrow, line #" + count + ": "
                                + CSVFileReadImpl.merge(line), CSVColumnReader.this);
                    }
                }
                String columnValue=line[column-1];
                if(trimFields) {
                    columnValue=columnValue.trim();
                }
                if(columnValue.length()==0 && skipEmptyFields) {
                    continue;
                }
                result.add(columnValue);
            }
            //end of lines set the results
            sfReplaceAttribute(ATTR_RESULT,result);
            if(target!=null) {
                target.sfReplaceAttribute(targetAttribute,result);
            }

        }

        /**
         * Runs the {@link #execute()} method, catching any exception it throws and storing it away for safe keeping
         * After the run, the notify object is notified, and we trigger a workflow termination
         */
        public void run() {
            super.run();
            TerminationRecord tr = new TerminationRecord(
                    getThrown() == null ? TerminationRecord.NORMAL : TerminationRecord.ABNORMAL,
                    "CSV file read",
                    sfCompleteNameSafe(),
                    getThrown());
            new ComponentHelper(CSVColumnReader.this).targetForWorkflowTermination(tr);
        }


    }

}