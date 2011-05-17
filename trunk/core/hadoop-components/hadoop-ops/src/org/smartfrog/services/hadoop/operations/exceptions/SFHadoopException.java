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
package org.smartfrog.services.hadoop.operations.exceptions;

import org.mortbay.util.MultiException;
import org.smartfrog.services.hadoop.operations.conf.ManagedConfiguration;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.prim.Prim;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * A SmartFrogException with special hadoop support, one that can
 * extract information from Hadoop classes (and helper libraries)
 */

public class SFHadoopException extends SmartFrogException {
    public static final String CONFIGURATION = "configuration";
    public static final String SMARTFROG_DUMP_CONF = "smartfrog.dump.conf";

    /**
     * Constructs a SmartFrogException with no message.
     */
    public SFHadoopException() {
    }

    /**
     * Constructs a SmartFrogException with specified message.
     *
     * @param message exception message
     */
    public SFHadoopException(String message) {
        super(message);
    }

    /**
     * Constructs a SmartFrogException with specified cause.
     *
     * @param cause exception causing this exception
     */
    public SFHadoopException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a SmartFrogException with specified message and cause.
     *
     * @param message exception message
     * @param cause   exception causing this exception
     */
    public SFHadoopException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a SmartFrogException with specified message. Also initializes the exception context with component
     * details.
     *
     * @param message  exception message
     * @param sfObject The Component that has encountered the exception
     */
    public SFHadoopException(String message, Prim sfObject) {
        super(message, sfObject);
    }

    /**
     * Constructs a SmartFrogException with specified message. Also initializes the exception context with component
     * details.
     *
     * @param message  exception message
     * @param sfObject The Component that has encountered the exception
     * @param conf the configuration at the time
     */
    public SFHadoopException(String message, Prim sfObject, ManagedConfiguration conf) {
        super(message, sfObject);
        addConfiguration(conf);
    }

    /**
     * Constructs a SmartFrogException with specified cause. Also initializes the exception context with component
     * details.
     *
     * @param cause    cause of the exception
     * @param sfObject The Component that has encountered the exception
     */
    public SFHadoopException(Throwable cause, Prim sfObject) {
        super(cause, sfObject);
    }

    /**
     * Constructs a SmartFrogException with specified message. Also initializes the exception context with component
     * details.
     *
     * @param message  message
     * @param cause    exception causing this exception
     * @param sfObject The Component that has encountered the exception
     */
    public SFHadoopException(String message, Throwable cause, Prim sfObject) {
        super(message, cause, sfObject);
    }

    /**
     * Constructs a SmartFrogException with specified message. Also initializes the exception context with component
     * details.
     *
     * @param message  message
     * @param cause    exception causing this exception
     * @param sfObject The Component that has encountered the exception
     * @param conf the configuration at the time
     */
    public SFHadoopException(String message, Throwable cause, Prim sfObject, ManagedConfiguration conf) {
        super(message, cause, sfObject);
        addConfiguration(conf);
    }


    /**
     * Dump the configuration to the {@link #CONFIGURATION} attribute
     *
     * @param conf configuration
     */
    public void addConfiguration(ManagedConfiguration conf) {
        add(CONFIGURATION, conf.dump());
    }

    /**
     * Turn a MultiExcept into a nested exception with the stack traces in the body. That's pretty nasty, but it stops
     * the information getting lost
     *
     * @param message     header message
     * @param multiExcept nested exceptions
     * @param sfObject    source
     * @param conf optional configuration
     * @return a new exception
     */
    @SuppressWarnings("unchecked")
    public static SFHadoopException forward(String message,
                                            MultiException multiExcept,
                                            Prim sfObject,
                                            ManagedConfiguration conf) {
        if (multiExcept == null) {
            //no value, hand off to the other handler
            return forward(message, (Throwable) null, sfObject, conf);
        }
        List<Throwable> exceptions = (List<Throwable>) multiExcept.getThrowables();
        int exCount = exceptions.size();
        if (exCount == 1) {
            //special case: one child.
            Throwable e = multiExcept.getThrowable(0);
            return new SFHadoopException(message + "\n" + e,
                                         e,
                                         sfObject);
        }

        String trace = dumpToString(exceptions);
        SFHadoopException hadoopException = new SFHadoopException(message
                                                                  + maybeDumpConfiguration(conf)
                                                                  + "\nmultiple (" + exCount + ") nested exceptions: \n"
                                                                  + multiExcept + "\n"
                                                                  + trace,
                                                                  multiExcept,
                                                                  sfObject);
        hadoopException.addConfiguration(conf);
        return hadoopException;
    }

    /**
     * Dump the exception list to a string. If it is empty, return the empty string
     * @param throwables a list of thrown exceptions
     * @return a dumped set of stack traces, or an empty string.
     */
    private static String dumpToString(List<Throwable> throwables) {
        if (throwables.size() == 0) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw;
        pw = new PrintWriter(sw);
        try {
            for (Throwable thrown : throwables) {
                thrown.printStackTrace(pw);
            }
        } finally {
            pw.close();
        }
        String trace = sw.toString();
        return trace;
    }

    /**
     * Forward the exception. Jetty exceptions have special handling. If the configuration is enabled
     * for dumping, the exception text includes a dump of the configuration
     * @param message custom messsage
     * @param throwable throwable
     * @param sfObject prim source
     * @param conf optional configuration
     * @return a new exception to throw
     */
    public static SFHadoopException forward(String message, Throwable throwable, Prim sfObject,
                                            ManagedConfiguration conf) {
        if (throwable == null) {
            SFHadoopException hadoopException = new SFHadoopException(message
                                                                      + maybeDumpConfiguration(conf),
                                                                      throwable, sfObject);
            hadoopException.addConfiguration(conf);
            return hadoopException;
        }
        if (throwable instanceof MultiException) {
            return forward(message, (MultiException) throwable, sfObject, conf);
        } else {
            SFHadoopException hadoopException = new SFHadoopException(message + ": "
                                                                      + throwable
                                                                      + maybeDumpConfiguration(conf),
                                                                      throwable, sfObject);
            hadoopException.addConfiguration(conf);
            return hadoopException;
        }

    }

    /**
     * Dump the configuration into the string if {@link #SMARTFROG_DUMP_CONF} is true
     * in the configuration.
     * @param conf configuration to work with
     * @return an empty string or a dumped configuration
     */
    private static String maybeDumpConfiguration(ManagedConfiguration conf) {
        try {
            if (conf != null && conf.getBoolean(SMARTFROG_DUMP_CONF, false)) {
                return "\n" + conf.dump();
            }
        } catch (SFHadoopRuntimeException ignored) {
            //whatever got here, it won't let us dump things
        }
        return "";
    }


    /**
     * To forward SmartFrog exceptions instead of chain them. If thr is an instance of SmartFrogException then the
     * exception is returned without any modification, if not a new SmartFrogException is created with message as a
     * paramenter
     *
     * @param message String message
     * @param thr     throwable object to be forwarded
     * @return Throwable that is a SmartFrogException
     */
    public static SFHadoopException forward(String message, Throwable thr) {
        if (thr instanceof SFHadoopException) {
            if (message != null) {
                ((SFHadoopException) thr).add("msg: ", message);
            }
            return (SFHadoopException) thr;
        } else {
            return new SFHadoopException(message, thr);
        }
    }

}
