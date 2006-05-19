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
package org.smartfrog.services.junit.listeners;

import org.smartfrog.services.junit.data.TestInfo;
import org.smartfrog.services.junit.TestListener;
import org.smartfrog.services.junit.TestSuite;
import org.smartfrog.services.junit.data.ThrowableTraceInfo;
import org.smartfrog.services.junit.data.LogEntry;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.prim.PrimImpl;
import org.smartfrog.sfcore.prim.TerminationRecord;

import java.io.PrintStream;
import java.rmi.RemoteException;

/**
 * The basic listener for trouble; the console listener. created 14-May-2004
 * 15:42:31
 */

public class ConsoleListenerComponent extends PrimImpl
        implements ConsoleListenerFactory {

    /**
     * cache the prinstream so that when system.out is used to capture output we
     * still go to the original console (i.e. no recursion)
     */

    PrintStream outputstream = System.out;

    public ConsoleListenerComponent() throws RemoteException {
    }


    /**
     * set a new output stream
     *
     * @param out
     */
    public void setOutputStream(PrintStream out) {
        outputstream = out;
    }

    /**
     * get the current output stream
     *
     * @return
     */
    public PrintStream getOutputstream() {
        return outputstream;
    }

    /**
     * Provides hook for subclasses to implement useful termination behavior.
     * Deregisters component from local process compound (if ever registered)
     *
     * @param status termination status
     */
    public synchronized void sfTerminateWith(TerminationRecord status) {
        outputstream.flush();
        super.sfTerminateWith(status);
    }


    public void println(String s) {
        outputstream.println(s);
    }

    public void flush() {
        outputstream.flush();
    }

    private void logTrouble(String message, TestInfo test) {
        StringBuffer buffer = new StringBuffer(128);
        buffer.append(message);
        buffer.append(test.getClassname());
        buffer.append(" on ");
        buffer.append(test.getHostname());
        buffer.append('\n');
        buffer.append(test.getText());
        println(buffer.toString());
        flush();
        logTrace(test.getFault());
    }

    private void logTrace(ThrowableTraceInfo fault) {
        if (fault == null) {
            return;
        }
        println(fault.getClassname());
        println(fault.getMessage());
        StackTraceElement stack[] = fault.getStack();
        for (int i = 0; i < stack.length; i++) {
            println(stack[i].toString());
        }
        flush();
        //recurse
        logTrace(fault.getCause());
    }

    /**
     * bind to a caller
     *
     * @param suite
     * @param hostname  name of host
     * @param suitename name of test suite
     * @param timestamp start timestamp (UTC)
     * @return a listener to talk to
     */
    public TestListener listen(TestSuite suite, String hostname,
            String suitename,
            long timestamp) throws RemoteException,
            SmartFrogException {
        return new ConsoleTestListener();
    }

    /**
     * this class exists to forward tests to the console
     */
    protected class ConsoleTestListener implements TestListener {

        /**
         * end this test suite. After calling this, caller should discard all
         * references; they may no longer be valid. <i>No further methods may be
         * called</i>
         */
        public void endSuite() throws RemoteException, SmartFrogException {

        }

        public void addError(TestInfo test) throws RemoteException {
            logTrouble("Error:", test);
        }

        public void addFailure(TestInfo test) throws RemoteException {
            logTrouble("Failure:", test);
        }

        public void endTest(TestInfo test) throws RemoteException {
            println("   ending " +
                    test.getClassname() +
                    " on " +
                    test.getHostname());

        }

        public void startTest(TestInfo test) throws RemoteException {
            println("Starting " +
                    test.getClassname() +
                    " on " +
                    test.getHostname());
        }

        public void log(LogEntry event) throws RemoteException {
            println(event.toString());
        }
    }
}
