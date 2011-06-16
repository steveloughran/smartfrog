/** (C) Copyright 2006 Hewlett-Packard Development Company, LP

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
package org.smartfrog.services.logging.jcl.front;

import org.apache.commons.logging.Log;

/**
 * This is a bridge between commons logging and smartfrog logging. Every commons-log operation
 * is relayed to the smartfrog back end.
 *
 */

public class CommonsLogFrontEnd implements Log {

    /**
     * The Log to which we are bound
     */
    private final org.smartfrog.sfcore.logging.Log backEnd;

    /**
     * Create bound to a named back end
     * @param backEnd
     */
    public CommonsLogFrontEnd(org.smartfrog.sfcore.logging.Log backEnd) {
        this.backEnd = backEnd;
    }

    /**
     * Create bind to the given log by name
     * @param name log name
     */
    public CommonsLogFrontEnd(String name) {
        this.backEnd = CommonsLogFactory.getSmartFrogLog(name);
    }

    /**
     * <p> Is debug logging currently enabled? </p>
     * <p/>
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than debug. </p>
     */
    @Override
    public boolean isDebugEnabled() {
        return backEnd.isDebugEnabled();
    }

    /**
     * <p> Is error logging currently enabled? </p>
     * <p/>
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than error. </p>
     */
    @Override
    public boolean isErrorEnabled() {
        return backEnd.isErrorEnabled();
    }

    /**
     * <p> Is fatal logging currently enabled? </p>
     * <p/>
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than fatal. </p>
     */
    @Override
    public boolean isFatalEnabled() {
        return backEnd.isFatalEnabled();
    }

    /**
     * <p> Is info logging currently enabled? </p>
     * <p/>
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than info. </p>
     */
    @Override
    public boolean isInfoEnabled() {
        return backEnd.isInfoEnabled();
    }

    /**
     * <p> Is trace logging currently enabled? </p>
     * <p/>
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than trace. </p>
     */
    @Override
    public boolean isTraceEnabled() {
        return backEnd.isTraceEnabled();
    }

    /**
     * <p> Is warning logging currently enabled? </p>
     * <p/>
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatenation)
     * when the log level is more than warn. </p>
     */
    @Override
    public boolean isWarnEnabled() {
        return backEnd.isWarnEnabled();
    }

    /**
     * <p> Log a message with trace log level. </p>
     *
     * @param message log this message
     */
    @Override
    public void trace(Object message) {
        backEnd.trace(message);
    }

    /**
     * <p> Log an error with trace log level. </p>
     *
     * @param message log this message
     * @param t       log this cause
     */
    @Override
    public void trace(Object message, Throwable t) {
        backEnd.trace(message, t);
    }

    /**
     * <p> Log a message with debug log level. </p>
     *
     * @param message log this message
     */
    @Override
    public void debug(Object message) {
        backEnd.debug(message);
    }

    /**
     * <p> Log an error with debug log level. </p>
     *
     * @param message log this message
     * @param t       log this cause
     */
    @Override
    public void debug(Object message, Throwable t) {
        backEnd.debug(message, t);
    }

    /**
     * <p> Log a message with info log level. </p>
     *
     * @param message log this message
     */
    @Override
    public void info(Object message) {
        backEnd.info(message);
    }

    /**
     * <p> Log an error with info log level. </p>
     *
     * @param message log this message
     * @param t       log this cause
     */
    @Override
    public void info(Object message, Throwable t) {
        backEnd.info(message, t);
    }

    /**
     * <p> Log a message with warn log level. </p>
     *
     * @param message log this message
     */
    @Override
    public void warn(Object message) {
        backEnd.warn(message);
    }

    /**
     * <p> Log an error with warn log level. </p>
     *
     * @param message log this message
     * @param t       log this cause
     */
    @Override
    public void warn(Object message, Throwable t) {
        backEnd.warn(message, t);
    }

    /**
     * <p> Log a message with error log level. </p>
     *
     * @param message log this message
     */
    @Override
    public void error(Object message) {
        backEnd.error(message);
    }

    /**
     * <p> Log an error with error log level. </p>
     *
     * @param message log this message
     * @param t       log this cause
     */
    @Override
    public void error(Object message, Throwable t) {
        backEnd.error(message, t);
    }

    /**
     * <p> Log a message with fatal log level. </p>
     *
     * @param message log this message
     */
    @Override
    public void fatal(Object message) {
        backEnd.fatal(message);
    }

    /**
     * <p> Log an error with fatal log level. </p>
     *
     * @param message log this message
     * @param t       log this cause
     */
    @Override
    public void fatal(Object message, Throwable t) {
        backEnd.fatal(message, t);
    }

}
