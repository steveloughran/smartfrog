/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
  Cut version of SimpleLog: Using this one for intial tests!
*/

package org.smartfrog.sfcore.logging;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.smartfrog.sfcore.common.SmartFrogLogException;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.prim.TerminationRecord;


/**
 * <p>Simple implementation of Log that sends all enabled log messages,
 * for all defined loggers, to System.err.  The following system properties
 * are supported to configure the behavior of this logger:</p>
 * <ul>
 * <li><code>defaultlog</code> -
 *     Default logging detail level for all instances of SimpleLog.
 *     Must be one of ("trace", "debug", "info", "warn", "error", or "fatal").
 *     If not specified, defaults to "info". </li>
 * <li><code>showlogname</code> -
 *     Set to <code>true</code> if you want the Log instance name to be
 *     included in output messages. Defaults to <code>false</code>.</li>
 * <li><code>showShortLogname</code> -
 *     Set to <code>true</code> if you want the last component of the name to be
 *     included in output messages. Defaults to <code>true</code>.</li>
 * <li><code>showdatetime</code> -
 *     Set to <code>true</code> if you want the current date and time
 *     to be included in output messages. Default is false.</li>
 * </ul>
 *
 *
 */
public class LogToErr implements Log, LogMessage, Serializable {

    /** Include the instance name in the log message? */
    static protected boolean showLogName = true;

    /** Include the short name ( last component ) of the logger in the log
        message. Default to true - otherwise we'll be lost in a flood of
        messages without knowing who sends them.
    */
    static protected boolean showShortName = false;

    /** Include the current time in the log message */
    static protected boolean showDateTime = true;

    /** Include thread name in the log message */
    static protected boolean showThreadName = true;

    /** Include package name in the log message */
    static protected boolean showMethodCall = true;

    /** Include package name in the log message */
    static protected boolean showStackTrace =true;

    /** Used to format times */
    static protected DateFormat dateFormatter = null;

    /** "Trace" level logging. */
    public static final int LOG_LEVEL_TRACE  = 1;
    /** "Debug" level logging. */
    public static final int LOG_LEVEL_DEBUG  = 2;
    /** "Info" level logging. */
    public static final int LOG_LEVEL_INFO   = 3;
    /** "Warn" level logging. */
    public static final int LOG_LEVEL_WARN   = 4;
    /** "Error" level logging. */
    public static final int LOG_LEVEL_ERROR  = 5;
    /** "Fatal" level logging. */
    public static final int LOG_LEVEL_FATAL  = 6;

    /** Enable all logging levels */
    public static final int LOG_LEVEL_ALL    = (LOG_LEVEL_TRACE - 1);

    /** Enable no logging levels */
    public static final int LOG_LEVEL_OFF    = (LOG_LEVEL_FATAL + 1);


    // Initialize class attributes.
    // Load properties file, if found.
    // Override with system properties.
    static {
        if(showDateTime) {
            dateFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS zzz");
        }
    }

    /** The name of this simple log instance */
    protected String logName = null;
    /** The current log level */
    protected int currentLogLevel;
    /** The short name of this simple log instance */
    private String shortLogName = null;

    //To get from where this Log was call
    private static CallDetective detective = CallDetective.Factory.makeCallDetective();
    //Depth in StackTrace it will depend on how this Log is used and connected to LogImpl
    int callDepth = 8;

    /**
     * Construct a simple log with given name.
     *
     * @param name log name
     */
    public LogToErr(String name, int intialLogLevel) {

        logName = name;
        // Set initial log level
        setLevel(intialLogLevel);

    }


    /**
     * <p> Set logging level. </p>
     *
     * @param currentLogLevel new logging level
     */
    public void setLevel(int currentLogLevel) {

        this.currentLogLevel = currentLogLevel;

    }


    /**
     * <p> Get logging level. </p>
     */
    public int getLevel() {

        return currentLogLevel;
    }


    // -------------------------------------------------------- Logging Methods


    /**
     * <p> Do the actual logging.
     * This method assembles the message
     * and then calls <code>write()</code> to cause it to be written.</p>
     *
     * @param type One of the LOG_LEVE_XXX constants defining the log level
     * @param message The message itself (typically a String)
     * @param t The exception whose stack trace should be logged
     */
    protected void log(int type, Object message, Throwable t) {
        // Use a string buffer for better performance
        StringBuffer buf = new StringBuffer();

        // Append date-time if so configured
        if(showDateTime) {
            buf.append(dateFormatter.format(new Date()));
            buf.append(" ");
        }

        // Append a readable representation of the log level
        switch(type) {
            case LogToErr.LOG_LEVEL_TRACE: buf.append("[TRACE] "); break;
            case LogToErr.LOG_LEVEL_DEBUG: buf.append("[DEBUG] "); break;
            case LogToErr.LOG_LEVEL_INFO:  buf.append("[INFO]  ");  break;
            case LogToErr.LOG_LEVEL_WARN:  buf.append("[WARN]  ");  break;
            case LogToErr.LOG_LEVEL_ERROR: buf.append("[ERROR] "); break;
            case LogToErr.LOG_LEVEL_FATAL: buf.append("[FATAL] "); break;
        }

        if ((showThreadName)&&(message!=null)) {
            buf.append("["+Thread.currentThread().getName()+"]");
        }

        if ((showMethodCall)&&(message!=null)) {
            buf.append(" * "+detective.findCaller(callDepth)+" * ");
        }

        // Append the name of the log instance if so configured
        if (showShortName) {
            if (shortLogName==null) {
                // Cut all but the last component of the name for both styles
                shortLogName = logName.substring(logName.lastIndexOf(".")+1);
                shortLogName =
                    shortLogName.substring(shortLogName.lastIndexOf("/")+1);
            }
            buf.append(String.valueOf(shortLogName)).append(" - ");
        } else if (showLogName) {
            buf.append(String.valueOf(logName)).append(" - ");
        }

        // Append the message
        buf.append(String.valueOf(message));


//        if ((showPackageName)&&(message!=null)) {
//         // Append the package name
//            String className = null;
//            String classBaseName = null;
//            if (sourceObject instanceof Class) {
//                // for trace from static methods
//                className = ((Class)source).getName();
//            } else {
//                className = source.getClass().getName();
//            }
//
//            int lastDotIndex = className.lastIndexOf('.');
//            if (lastDotIndex!=-1) {
//                thisPackageName = className.substring(0, lastDotIndex);
//                classBaseName = className.substring(lastDotIndex+1,
//                    className.length());
//            }
//        }

// Append stack trace if not null
        if(t != null) {
            buf.append(" <");
            if (t instanceof SmartFrogException)
                 buf.append(((SmartFrogException)t).toString("\n    "));
            else
                 buf.append(t.toString());
            buf.append(">\n        ");
        }

        // Append stack trace if not null
        if(t != null) {
            if (showStackTrace || this.isLevelEnabled(LOG_LEVEL_WARN)) {
                java.io.StringWriter sw = new java.io.StringWriter(1024);
                java.io.PrintWriter pw = new java.io.PrintWriter(sw);
                t.printStackTrace(pw);
                pw.close();
                buf.append(sw.toString());
            }
        }

        // Print to the appropriate destination
        write(buf);

    }


    /**
     * <p>Write the content of the message accumulated in the specified
     * <code>StringBuffer</code> to the appropriate output destination.  The
     * default implementation writes to <code>System.err</code>.</p>
     *
     * @param buffer A <code>StringBuffer</code> containing the accumulated
     *  text to be logged
     */
    protected void write(StringBuffer buffer) {
        System.err.println(buffer.toString());

    }


    /**
     * Is the given log level currently enabled?
     *
     * @param logLevel is this level enabled?
     */
    protected boolean isLevelEnabled(int logLevel) {
        // log level are numerically ordered so can use simple numeric
        // comparison
        return (logLevel >= currentLogLevel);
    }


    // -------------------------------------------------------- Log Implementation


    /**
     * <p> Log a message with debug log level.</p>
     */
    public final void debug(Object message) {

        if (isLevelEnabled(LogToErr.LOG_LEVEL_DEBUG)) {
            log(LogToErr.LOG_LEVEL_DEBUG, message, null);
        }
    }


    /**
     * <p> Log an error with debug log level.</p>
     */
    public final void debug(Object message, Throwable t) {

        if (isLevelEnabled(LogToErr.LOG_LEVEL_DEBUG)) {
            log(LogToErr.LOG_LEVEL_DEBUG, message, t);
        }
    }


    /**
     * <p> Log a message with trace log level.</p>
     */
    public final void trace(Object message) {

        if (isLevelEnabled(LogToErr.LOG_LEVEL_TRACE)) {
            log(LogToErr.LOG_LEVEL_TRACE, message, null);
        }
    }


    /**
     * <p> Log an error with trace log level.</p>
     */
    public final void trace(Object message, Throwable t) {

        if (isLevelEnabled(LogToErr.LOG_LEVEL_TRACE)) {
            log(LogToErr.LOG_LEVEL_TRACE, message, t);
        }
    }


    /**
     * <p> Log a message with info log level.</p>
     */
    public final void info(Object message) {

        if (isLevelEnabled(LogToErr.LOG_LEVEL_INFO)) {
            log(LogToErr.LOG_LEVEL_INFO,message,null);
        }
    }


    /**
     * <p> Log an error with info log level.</p>
     */
    public final void info(Object message, Throwable t) {

        if (isLevelEnabled(LogToErr.LOG_LEVEL_INFO)) {
            log(LogToErr.LOG_LEVEL_INFO, message, t);
        }
    }


    /**
     * <p> Log a message with warn log level.</p>
     */
    public final void warn(Object message) {

        if (isLevelEnabled(LogToErr.LOG_LEVEL_WARN)) {
            log(LogToErr.LOG_LEVEL_WARN, message, null);
        }
    }


    /**
     * <p> Log an error with warn log level.</p>
     */
    public final void warn(Object message, Throwable t) {

        if (isLevelEnabled(LogToErr.LOG_LEVEL_WARN)) {
            log(LogToErr.LOG_LEVEL_WARN, message, t);
        }
    }


    /**
     * <p> Log a message with error log level.</p>
     */
    public final void error(Object message) {

        if (isLevelEnabled(LogToErr.LOG_LEVEL_ERROR)) {
            log(LogToErr.LOG_LEVEL_ERROR, message, null);
        }
    }


    /**
     * <p> Log an error with error log level.</p>
     */
    public final void error(Object message, Throwable t) {

        if (isLevelEnabled(LogToErr.LOG_LEVEL_ERROR)) {
            log(LogToErr.LOG_LEVEL_ERROR, message, t);
        }
    }


    /**
     * <p> Log a message with fatal log level.</p>
     */
    public final void fatal(Object message) {

        if (isLevelEnabled(LogToErr.LOG_LEVEL_FATAL)) {
            log(LogToErr.LOG_LEVEL_FATAL, message, null);
        }
    }


    /**
     * <p> Log an error with fatal log level.</p>
     */
    public final void fatal(Object message, Throwable t) {

        if (isLevelEnabled(LogToErr.LOG_LEVEL_FATAL)) {
            log(LogToErr.LOG_LEVEL_FATAL, message, t);
        }
    }


    /**
     * <p> Are debug messages currently enabled? </p>
     *
     * <p> This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger. </p>
     */
    public final boolean isDebugEnabled() {

        return isLevelEnabled(LogToErr.LOG_LEVEL_DEBUG);
    }


    /**
     * <p> Are error messages currently enabled? </p>
     *
     * <p> This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger. </p>
     */
    public final boolean isErrorEnabled() {

        return isLevelEnabled(LogToErr.LOG_LEVEL_ERROR);
    }


    /**
     * <p> Are fatal messages currently enabled? </p>
     *
     * <p> This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger. </p>
     */
    public final boolean isFatalEnabled() {

        return isLevelEnabled(LogToErr.LOG_LEVEL_FATAL);
    }


    /**
     * <p> Are info messages currently enabled? </p>
     *
     * <p> This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger. </p>
     */
    public final boolean isInfoEnabled() {

        return isLevelEnabled(LogToErr.LOG_LEVEL_INFO);
    }

    /**
     * <p> Are trace messages currently enabled? </p>
     *
     * <p> This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger. </p>
     */
    public final boolean isTraceEnabled() {

        return isLevelEnabled(LogToErr.LOG_LEVEL_TRACE);
    }

    /**
     * <p> Are warn messages currently enabled? </p>
     *
     * <p> This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger. </p>
     */
    public final boolean isWarnEnabled() {

        return isLevelEnabled(LogToErr.LOG_LEVEL_WARN);
    }


    // Special LogMessages interface to produce output.
    /**
     * <p> Log an error with message log level. </p>
     * <p> Same as info messages but without Labels.</p>
     *
     * @param message log this message
     */
    public void out(Object message) {
         System.out.println(message.toString());
    }

    /**
     * <p> Log an error with message log level. </p>
     * <p> Same as info messages but without Labels.</p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void err(Object message, Throwable t) {
          System.err.println(message.toString());
          t.printStackTrace();
    }

    /**
     * <p> Log a message with message log level. </p>
     * <p> Same as info messages but without Labels.</p>
     *
     * @param message log this message
     */
    public void err(Object message, SmartFrogException t, TerminationRecord tr) {
        err(message, t);
        System.err.println(tr.toString());
    }

    /**
     * <p> Log an error with message log level. </p>
     * <p> Same as info messages but without Labels.</p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void err(Object message, SmartFrogException t) {
        err(message, t);
    }

}

