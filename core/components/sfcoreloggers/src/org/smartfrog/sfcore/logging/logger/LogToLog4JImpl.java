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


package org.smartfrog.sfcore.logging.logger;

import org.smartfrog.sfcore.logging.Log;
import org.smartfrog.sfcore.logging.LogImpl;
import org.smartfrog.sfcore.logging.LogLevel;
import org.smartfrog.sfcore.logging.LogMessage;
import org.smartfrog.sfcore.logging.LogLevel;
import org.smartfrog.sfcore.logging.LogMessage;

import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.prim.TerminationRecord;
import org.smartfrog.sfcore.componentdescription.ComponentDescription;

import java.io.PrintStream;
import java.io.Serializable;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.smartfrog.sfcore.common.*;
import javax.xml.parsers.*;

/**
 */
public class LogToLog4JImpl implements LogToLog4J, Log, LogMessage, LogLevel,
    Serializable {

//Configuration for LogImpl class
  protected ComponentDescription classComponentDescription = null;

  /** Log to this logger */
  private transient Logger logger = null;

  /** Logger name */
  String logName = null;

  /** Output stream to print to. Bonded at construct time, and usually system.err unless
   * otherwise chosen
   */
  protected PrintStream outstream;

  /** The LogToLog4J class name. */
  private static final String clazz = LogImpl.class.getName();

  java.net.URL configuratorURL = null;
  String configuratorString = null;
  boolean configureAndWatch = false;
  long configureAndWatchDelay = 60 * 1000;

  /**
   * Construct a simple log with given name and log level
   * and log to output level

   */
  protected LogToLog4JImpl() {
  }

  /**
   * Construct a simple log with given name and log level
   * and log to output level
   * @param name log name
   * @param initialLogLevel level to log at
   */
  public LogToLog4JImpl(String name, Integer initialLogLevel) {
    this(name, initialLogLevel, System.out);
  }

  /**
   * Construct a simple log with given name and log level
   * and log to output level
   * @param name log name
   * @param initialLogLevel level to log at
   * @param out output stream to log to
   */

  public LogToLog4JImpl(String name, Integer initialLogLevel, PrintStream out) {
    setOutstream(out);
    assert name != null;
    logName = name;
    if (logger == null) {
      logger = Logger.getLogger(name);
    }
    setLevel(initialLogLevel.intValue());
    //Initial configurator to get output in case of failures
    org.apache.log4j.BasicConfigurator.configure();
    //Check Class and read configuration...including system.properties
    try {
      classComponentDescription = LogImpl.getClassComponentDescription(this, true);
    } catch (SmartFrogException ex) {
      if (isWarnEnabled()) this.warn(ex.toString());
    }
    if (isTraceEnabled() &&
        this.getClass().toString().endsWith("LogToLog4JImpl")) {
      trace(this.getClass().toString() + " '" + name +
            "' using ComponentDescription:\n" +
            classComponentDescription.toString());
    }
    try {
      readSFAttributes();
    } catch (SmartFrogException ex1) {
      if (isErrorEnabled()) this.error("", ex1);
    }

    if (configuratorURL != null) {
      try {
        if (configureAndWatch) {
          if (isWarnEnabled()) {
            this.warn(
                "LogToLog4JImpl: ConfigureAndWatch not available with URL (" +
                configuratorURL.toString() + ")");
          }
        }
        if (configuratorURL.getFile().endsWith(".xml")) {
          //Initial configurator is removed
          org.apache.log4j.BasicConfigurator.resetConfiguration();
          org.apache.log4j.xml.DOMConfigurator.configure(configuratorURL);
          if (isTraceEnabled()) {
            this.out(
                "LogToLog4JImpl: Using Log4J.xml.DOMConfigurator with URL " +
                configuratorURL.toString());
          }
        } else {
          //Initial configurator is removed
          org.apache.log4j.BasicConfigurator.resetConfiguration();
          org.apache.log4j.PropertyConfigurator.configure(configuratorURL);
          if (isTraceEnabled()) {
            this.out("LogToLog4JImpl: Using Log4J.PropertyConfigurator with URL " + configuratorURL.toString());
          }
        }
      } catch (FactoryConfigurationError ex2) {
        if (isErrorEnabled()) {
          this.err("", ex2);
        }
      }
//      } else if (configuratorURL.getFile().endsWith(".sf")) {
//      we don't have a sf configurator yet.
    } else if (configuratorString != null) {
      try {
        if (configuratorString.endsWith(".xml")) {
          //Initial configurator is removed
          org.apache.log4j.BasicConfigurator.resetConfiguration();
          if (!configureAndWatch) {
            org.apache.log4j.xml.DOMConfigurator.configure(configuratorString);
            if (isTraceEnabled()) {
              this.out("LogToLog4JImpl: Using Log4J.xml.DOMConfigurator with " +
                       configuratorString);
            }
          } else {
            org.apache.log4j.xml.DOMConfigurator.configureAndWatch(configuratorString, configureAndWatchDelay);
            if (isTraceEnabled()) {
              this.out("LogToLog4JImpl: Using Log4J.xml.DOMConfigurator with " +
                       configuratorString + " and watch every " +
                       configureAndWatchDelay + "ms");
            }
          }
        } else {
          //Initial configurator is removed
          org.apache.log4j.BasicConfigurator.resetConfiguration();
          if (!configureAndWatch) {
            org.apache.log4j.PropertyConfigurator.configure(configuratorString);
            if (isTraceEnabled()) {
              this.out("LogToLog4JImpl: Using Log4J.PropertyConfigurator with " +
                       configuratorString);
            }
          } else {
            org.apache.log4j.PropertyConfigurator.configureAndWatch(configuratorString, configureAndWatchDelay);
            if (isTraceEnabled()) {
              this.out("LogToLog4JImpl: Using Log4J.PropertyConfigurator with " +
                       configuratorString + " and watch every " +
                       configureAndWatchDelay + "ms");
            }
          }
        }
      } catch (FactoryConfigurationError ex3) {
        if (isErrorEnabled()) {
          this.err("", ex3);
        }
      }
    } else {
      if (isTraceEnabled()) {
        this.out("LogToLog4JImpl: Using Log4J.BasicConfigurator");
      }
    }
    // Set initial log level after reading configuration to the lowest one
    // the most verbose of the two
    if (logger.getLevel().toInt() >= initialLogLevel.intValue()) {
      setLevel(initialLogLevel.intValue());
    }
    if (isTraceEnabled()) {
        this.trace("LogToLog4JImpl logger: "+ logger.toString() +"("+logger.getLevel().toString()+")");
    }
  }

  /**
   *  Reads optional and mandatory attributes.
   *
   * @exception  SmartFrogException error while reading attributes
   */
  protected void readSFAttributes() throws SmartFrogException {
    if (classComponentDescription == null) {
      return;
    }
    //Optional attributes.
    try {
      configuratorURL = (java.net.URL)classComponentDescription.sfResolve(ATR_CONFIGURATOR_FILE, configuratorURL, true);
    } catch (SmartFrogException sex) {
      if (isTraceEnabled()) {
        trace("Failed to use URL (" + sex.toString() + ")");
      }
      configuratorString = classComponentDescription.sfResolve( ATR_CONFIGURATOR_FILE, configuratorString, true);
    }
    configureAndWatch = classComponentDescription.sfResolve( ATR_CONFIGURE_AND_WATCH, configureAndWatch, false);
    double delay = Double.longBitsToDouble(configureAndWatchDelay);
    delay = (classComponentDescription.sfResolve(ATR_CONFIGURE_AND_WATCH_DELAY, delay, false));
    configureAndWatchDelay = (long) delay;
  }

  /**
   * set the output stream for logging. must not be null
   * @param outstream
   */
  public void setOutstream(PrintStream outstream) {
    assert (outstream != null);
    this.outstream = outstream;
  }

  /**
   * <p> Set logging level. </p>
   *
   * @param currentLogLevel new logging level
   */
  public void setLevel(int currentLogLevel) {
    logger.setLevel( (Level) Level.toLevel(currentLogLevel));
  }

  /**
   * <p> Get logging level. </p>
   */
  public int getLevel() {
    return logger.getLevel().toInt();
  }

// -------------------------------------------------------- Log Implementation
  protected void log(Level level, Object message, Throwable t) {
    if (logger == null) {
      logger = Logger.getLogger(logName);
    }
    logger.log(clazz, level, message, t);
  }

  /**
   * Is the given log level currently enabled?
   *
   * @param logLevel is this level enabled?
   */
  public boolean isLevelEnabled(int logLevel) {
// log level are numerically ordered so can use simple numeric
// comparison
//System.out.println("Local: Current "+currentLogLevel+", compared "+logLevel);
    return (logLevel >= getLevel());

  }

// --------------------------------------------------END -- Log Implementation


  /**
   * <p> Log a message with debug log level.</p>
   */
  public final void debug(Object message) {
    log(Level.DEBUG, message, null);
  }

  /**
   * <p> Log an error with debug log level.</p>
   */
  public final void debug(Object message, Throwable t) {
    log(Level.DEBUG, message, t);
  }

  /**
   * <p> Log a message with trace log level.</p>
   */
  public final void trace(Object message) {
    log(Level.DEBUG, message, null);
  }

  /**
   * <p> Log an error with trace log level.</p>
   */
  public final void trace(Object message, Throwable t) {
    log(Level.DEBUG, message, t);
  }

  /**
   * <p> Log a message with info log level.</p>
   */
  public final void info(Object message) {
    log(Level.INFO, message, null);
  }

  /**
   * <p> Log an error with info log level.</p>
   */
  public final void info(Object message, Throwable t) {
    log(Level.INFO, message, t);
  }

  /**
   * <p> Log a message with warn log level.</p>
   */
  public final void warn(Object message) {
    log(Level.WARN, message, null);
  }

  /**
   * <p> Log an error with warn log level.</p>
   */
  public final void warn(Object message, Throwable t) {
    log(Level.WARN, message, t);
  }

  /**
   * <p> Log a message with error log level.</p>
   */
  public final void error(Object message) {
    log(Level.ERROR, message, null);
  }

  /**
   * <p> Log an error with error log level.</p>
   */
  public final void error(Object message, Throwable t) {
    log(Level.ERROR, message, t);
  }

  /**
   * <p> Log a message with fatal log level.</p>
   */
  public final void fatal(Object message) {
    log(Level.FATAL, message, null);
  }

  /**
   * <p> Log an error with fatal log level.</p>
   */
  public final void fatal(Object message, Throwable t) {
    log(Level.FATAL, message, t);
  }

  /**
   * <p> Are debug messages currently enabled? </p>
   *
   * <p> This allows expensive operations such as <code>String</code>
   * concatenation to be avoided when the message will be ignored by the
   * logger. </p>
   */
  public final boolean isDebugEnabled() {
    return logger.isDebugEnabled();
  }

  /**
   * <p> Are error messages currently enabled? </p>
   *
   * <p> This allows expensive operations such as <code>String</code>
   * concatenation to be avoided when the message will be ignored by the
   * logger. </p>
   */
  public final boolean isErrorEnabled() {

    return logger.isEnabledFor(Level.ERROR);
  }

  /**
   * <p> Are fatal messages currently enabled? </p>
   *
   * <p> This allows expensive operations such as <code>String</code>
   * concatenation to be avoided when the message will be ignored by the
   * logger. </p>
   */
  public final boolean isFatalEnabled() {

    return logger.isEnabledFor(Level.FATAL);
  }

  /**
   * <p> Are info messages currently enabled? </p>
   *
   * <p> This allows expensive operations such as <code>String</code>
   * concatenation to be avoided when the message will be ignored by the
   * logger. </p>
   */
  public final boolean isInfoEnabled() {
    return logger.isInfoEnabled();
  }

  /**
   * <p> Are trace messages currently enabled? </p>
   *
   * <p> This allows expensive operations such as <code>String</code>
   * concatenation to be avoided when the message will be ignored by the
   * logger. </p>
   */
  public final boolean isTraceEnabled() {
    return logger.isDebugEnabled();
  }

  /**
   * <p> Are warn messages currently enabled? </p>
   *
   * <p> This allows expensive operations such as <code>String</code>
   * concatenation to be avoided when the message will be ignored by the
   * logger. </p>
   */
  public final boolean isWarnEnabled() {
    return logger.isEnabledFor(Level.WARN);
  }

// Special LogMessages interface to produce output.
  /**
   * <p> Log an error with message log level. </p>
   * <p> Same as info messages but without Labels.</p>
   *
   * @param message log this message
   */
  public void out(Object message) {
    outstream.println(message.toString());
    this.info(message);
  }

  /**
   * <p> Log an error with message log level. </p>
   * <p> Same as info messages but without Labels.</p>
   *
   * @param message log this message
   */
  public void err(Object message) {
    err(message, null);
  }

  /**
   * <p> Log an error with message log level. </p>
   * <p> Same as info messages but without Labels.</p>
   *
   * @param message log this message
   * @param t log this cause
   */
  public void err(Object message, Throwable t) {
    outstream.println(message.toString());
    this.error(message, t);
    if (t != null) {
      t.printStackTrace(outstream);
    }
  }

  /**
   * <p> Log a message with message log level. </p>
   * <p> Same as info messages but without Labels.</p>
   *
   * @param message log this message
   */
  public void err(Object message, SmartFrogException t, TerminationRecord tr) {
    err(message, t);
    outstream.println(tr.toString());
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
