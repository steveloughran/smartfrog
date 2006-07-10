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

package org.smartfrog;

import org.smartfrog.sfcore.common.ConfigurationDescriptor;
import org.smartfrog.sfcore.common.Logger;
import org.smartfrog.sfcore.common.MessageKeys;
import org.smartfrog.sfcore.common.MessageUtil;
import org.smartfrog.sfcore.common.OptionSet;
import org.smartfrog.sfcore.common.SmartFrogCoreKeys;
import org.smartfrog.sfcore.common.SmartFrogCoreProperty;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.logging.LogFactory;
import org.smartfrog.sfcore.logging.LogSF;
import org.smartfrog.sfcore.processcompound.ProcessCompound;
import org.smartfrog.sfcore.processcompound.SFProcess;
import org.smartfrog.sfcore.security.SFClassLoader;
import org.smartfrog.sfcore.security.SFGeneralSecurityException;
import org.smartfrog.sfcore.security.SFSecurity;
import org.smartfrog.sfcore.security.SFSecurityProperties;
import org.smartfrog.sfcore.prim.TerminationRecord;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.common.ExitCodes;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import org.smartfrog.sfcore.common.Diagnostics;


/**
 * SFSystem offers utility methods to deploy from a stream and a deployer.  It
 * attempts to deploy and start the component found in the sfConfig attribute.
 * Any failure will cause a termination of the component under deployment or
 * starting.  The main function looks for a property and configuration option
 * on the argument line and deploys locally based on these.
 *
 * <P>
 * You can create your own main loop, using the utility methods in this class.
 * The main loop of SFSystem reads an optionset, checks if -c or /? missing to
 * pring usage string (and exit). It then reads the system properties, and
 * does a deployFromURLs given the URLs on the command line. Any exception or
 * error causes the main loop to do an exit. If the /e option is present on
 * the command line, the main loop exits after deployment. This is good for
 * one shot deployment, with deployment occurring into other processes.
 * </p>
 *
 * <p>
 * This class can be subclassed, but one must be very, very careful about doing so.
 * </p>
 */
public class SFSystem implements MessageKeys {

    /** A flag that ensures only one system initialization. */
    private static boolean alreadySystemInit = false;

    /** Core Log  */
    private static  LogSF sflog = null;


    /**
     * root process. Will be null after termination.
     */
    private ProcessCompound rootProcess;

    /**
     * Entry point to get system properties. Works around a bug in some JVM's
     * (ie. Solaris) to return the default correctly.
     *
     * @param key property key to look up
     * @param def default to return if key not present
     *
     * @return property value under key or default if not present
     */
    public static String getProperty(String key, String def) {
        String res = System.getProperty(key, def);

        if (res == null) {
            return def;
        }

        return res;
    }

    /**
     * Common entry point to get system properties.
     *
     * @param key key to look up
     *
     * @return property value under key
     */
    public static String getProperty(String key) {
        return System.getProperty(key);
    }

    /**
     * Reads properties given a system property "org.smartfrog.iniFile".
     *
     * @throws SmartFrogException if failed to read properties from the
     * ini file
     */
    public static void readPropertiesFromIniFile() throws SmartFrogException {
        try {
            String source = System.getProperty(SmartFrogCoreProperty.iniFile);

            if (source!=null) {
                InputStream iniFileStream = getInputStreamForResource(source);
                try {
                    readPropertiesFrom(iniFileStream);
                } catch (IOException ioEx) {
                    throw new SmartFrogException(ioEx);
                }
            }
        } catch (Throwable ex) {
            //This should not prevent SF start.
            if (sflog!=null) { // This method can be called before LogSystem was initialized
                if (sfLog().isErrorEnabled()) {
                    sfLog().error("Could not use 'org.smartfrog.iniFile'", ex);
                }
            } else {
                System.err.println("ERROR: Could not use 'org.smartfrog.iniFile' "+ ex.getMessage());
            }
        }
    }

    /**
     * Reads and sets system properties given in input stream.
     *
     * @param is input stream
     *
     * @exception IOException failed to read properties
     */
    public static void readPropertiesFrom(InputStream is) throws IOException {
        Properties props = new Properties();
        props.load(is);

        Properties sysProps = System.getProperties();

        for (Enumeration e = props.keys(); e.hasMoreElements();) {
            Object key = e.nextElement();
            sysProps.put(key, props.get(key));
        }

        System.setProperties(sysProps);
        if (sflog!=null) { // This method can be called before LogSystem was initialized
            if (sfLog().isTraceEnabled()) {
                sfLog().trace("New system properties: \n"+ sysProps.toString().replace(',', '\n'));
            }
        }
    }


    /**
     * Sets stdout and stderr streams to different streams if class names
     * specified in system properties. Uses System.setErr and setOut to set
     * the <b>PrintStream</b>s which form stderr and stdout
     *
     * @exception SmartFrogException failed to create or set output/error streams
     */
    public static void setOutputStreams() throws SmartFrogException {
        String outClass = SFSystem.getProperty(SmartFrogCoreProperty.propOutStreamClass);
        String errClass = SFSystem.getProperty(SmartFrogCoreProperty.propErrStreamClass);

        try {
            if (errClass != null) {
                System.setErr((PrintStream) SFClassLoader.forName(errClass).newInstance());
            }

            if (outClass != null) {
                System.setOut((PrintStream) SFClassLoader.forName(outClass).newInstance());
            }
        } catch (InstantiationException e) {
            throw SmartFrogException.forward(e);
        } catch (IllegalAccessException e) {
            throw SmartFrogException.forward(e);
        } catch (ClassNotFoundException e) {
            throw SmartFrogException.forward(e);
        }
    }



    /**
     * Prints given error string and exits system.
     *
     * @param str string to print on out
     */
    public void exitWith(String str, int exitCode) {
        if (str != null) {
            System.err.println(str);
        }
        ExitCodes.exitWithError(exitCode);
    }


    /**
     * exit with an error code that depends on the status of the execution
     *
     * @param somethingFailed flag to indicate trouble
     */
    public static void exitWithStatus(boolean somethingFailed, int exitCode) {
        if(somethingFailed) {
            ExitCodes.exitWithError(exitCode);
        } else {
            ExitCodes.exitWithError(ExitCodes.EXIT_CODE_SUCCESS);
        }
    }


    /**
     * Shows the version info of the SmartFrog system.
     */
    private static void showVersionInfo(){
        sfLog().out(Version.versionString()+System.getProperty("line.separator")+Version.copyright());
    }

    /**
     * Runs a set (vector) of configuration descriptors
     * @param cfgDescs Vector of ConfigurationDescriptors
     * @see ConfigurationDescriptor
     */

    public static void runConfigurationDescriptors (Vector cfgDescs) {
        if (cfgDescs==null) return;
        for (Enumeration items = cfgDescs.elements(); items.hasMoreElements();) {
           runConfigurationDescriptor((ConfigurationDescriptor)items.nextElement());
        }
    }

    /**
     * Runs a configuration descripor trapping any possible exception
     * @param cfgDesc ConfigurationDescriptor
     * @see ConfigurationDescriptor
     */
    public static Object runConfigurationDescriptor (ConfigurationDescriptor cfgDesc) {
        try {
            return runConfigurationDescriptor(cfgDesc, false);
        } catch (SmartFrogException ex) {
            if (sfLog().isIgnoreEnabled()){ sfLog().ignore(ex); }
        }
        return null;
    }

    /**
     * run whatever action is configured
     * @param configuration
     * @param throwException
     * @return whatever came back from calling
     *  {@link ConfigurationDescriptor#execute(ProcessCompound)}
     * @throws SmartFrogException
     */
    public static Object runConfigurationDescriptor(ConfigurationDescriptor configuration,
                                                    boolean throwException) throws SmartFrogException {

        try {
            initSystem();
            Object targetC=configuration.execute(null);
            return targetC;

        } catch (Throwable thrown) {
            if (configuration.resultException == null) {
                configuration.setResult(ConfigurationDescriptor.Result.FAILED, null,thrown);
            } else {
                if (sfLog().isIgnoreEnabled()){ sfLog().ignore(thrown);}
            }
            if (throwException) {
                throw SmartFrogException.forward(thrown);
            }
        }
        return configuration;
    }



    /**
     * Method invoked to start the SmartFrog system.
     *
     * @param args command line arguments. Please see the usage to get more
     * details
     */
    public static void main(String[] args) {
        SFSystem system=new SFSystem();
        system.execute(args);
    }


    /**
     * This is the implementation of the main function.
     * @param args
     */
    public void execute(String args[]) {

        //First thing first: system gets initialized
        try {
            initSystem();
        } catch (Exception ex) {
            try {
                if (sfLog().isErrorEnabled()) {
                    sfLog().error(ex);
                }
            } catch (Exception ex1) {ex1.printStackTrace();}
            ExitCodes.exitWithError(ExitCodes.EXIT_ERROR_CODE_GENERAL);
        }

        setRootProcess(null);

        showVersionInfo();

        OptionSet opts = new OptionSet(args);

        showDiagnostics(opts);

        if (opts.errorString != null) {
            sfLog().out(opts.errorString);
            ExitCodes.exitWithError(opts.exitCode);
        }
        try {
            setRootProcess(runSmartFrog(opts.cfgDescriptors));
        } catch (SmartFrogException sfex) {
            sfLog().out(sfex);
            if (Logger.logStackTrace){ printStackTrace(sfex); }
            ExitCodes.exitWithError(ExitCodes.EXIT_ERROR_CODE_GENERAL);
        } catch (UnknownHostException uhex) {
            sfLog().err(MessageUtil.formatMessage(MSG_UNKNOWN_HOST, opts.host), uhex);
            if (Logger.logStackTrace){ printStackTrace(uhex); }
            ExitCodes.exitWithError(ExitCodes.EXIT_ERROR_CODE_GENERAL);
        } catch (ConnectException cex) {
            sfLog().err(MessageUtil.formatMessage(MSG_CONNECT_ERR, opts.host), cex);
            if (Logger.logStackTrace){ printStackTrace(cex); }
            ExitCodes.exitWithError(ExitCodes.EXIT_ERROR_CODE_GENERAL);
        } catch (RemoteException rmiEx) {
            // log stack trace
            sfLog().err(MessageUtil.formatMessage(MSG_REMOTE_CONNECT_ERR,
                    opts.host), rmiEx);
            if (Logger.logStackTrace){ printStackTrace(rmiEx); }
            ExitCodes.exitWithError(ExitCodes.EXIT_ERROR_CODE_GENERAL);
        } catch (Exception ex) {
            //log stack trace
            sfLog().err(MessageUtil.
                    formatMessage(MSG_UNHANDLED_EXCEPTION), ex);
            if (Logger.logStackTrace){ printStackTrace(ex); }
            ExitCodes.exitWithError(ExitCodes.EXIT_ERROR_CODE_GENERAL);
        }

        //Report Actions successes or failures.
         boolean somethingFailed = false;
         ConfigurationDescriptor cfgDesc = null;
         for (Enumeration items = opts.cfgDescriptors.elements();
              items.hasMoreElements(); ) {
             cfgDesc = (ConfigurationDescriptor)items.nextElement();
             if (cfgDesc.getResultType()==ConfigurationDescriptor.Result.FAILED) {
                 somethingFailed = true;
		 opts.exitCode =ExitCodes.EXIT_ERROR_CODE_GENERAL;
             }
             sfLog().out(" - "+(cfgDesc).statusString()+"\n");
             if (sfLog().isIgnoreEnabled() && cfgDesc.resultException!=null){
               sfLog().ignore(cfgDesc.resultException);
            }
         }
        // Check for exit flag
        if (opts.exit) {
            exitWithStatus(somethingFailed,opts.exitCode);
        } else {
            //sfLog().out(MessageUtil.formatMessage(MSG_SF_READY));
            if (true) {
                String name = "";
                int port =0;
                try {
                    if (rootProcess != null) {
                        name = rootProcess.sfResolve(SmartFrogCoreKeys.SF_PROCESS_NAME, name, false);
                        port = rootProcess.sfResolve(SmartFrogCoreKeys.SF_ROOT_LOCATOR_PORT, port, false);
                    }
                } catch (Exception ex) {
                    //ignore.
                }
                sfLog().out(MessageUtil.formatMessage(MSG_SF_READY, "[" + name + ":"+ port+"]") + " " + new Date(System.currentTimeMillis()));
            } else {
                sfLog().out(MessageUtil.formatMessage(MSG_SF_READY, ""));
            }
        }
    }

    /**
     * Shows diagnostics report
     * @param opts OptionSet
     */
    private void showDiagnostics(OptionSet opts) {
      if (opts.diagnostics){
        //org.smartfrog.sfcore.common.Diagnostics.doReport(System.out);
        StringBuffer report = new StringBuffer();
        org.smartfrog.sfcore.common.Diagnostics.doReport(report);
        sfLog().out(report.toString());
      }
    }

    /**
     * Prints StackTrace
     * @param thr Throwable
     */
    public void printStackTrace(Throwable thr){
      System.err.println(MessageUtil.formatMessage(MSG_STACKTRACE_FOLLOWS)+"' "+
                         ConfigurationDescriptor.parseExceptionStackTrace(thr,"\n"+"   ")+" '");
    }

    /**
     * Run SmartFrog as configured. This call does not exit SmartFrog, even if the OptionSet requests it.
     * This entry point exists so that alternate entry points (e.g. Ant Tasks) can start the system.
     * Important: things like the output streams can be redirected inside this call
     * @param  cfgDescriptors Vector of Configuration  opts with list of ConfigurationDescriptors
     *         @see ConfigurationDescriptor
     * @return the root process
     * @throws SmartFrogException for a specific SmartFrog problem
     * @throws UnknownHostException if the target host is unknown
     * @throws ConnectException if the remote system's SmartFrog daemon is unreachable
     * @throws RemoteException if something goes wrong during the communication
     * @throws Exception if anything else went wrong
     */

    public static ProcessCompound runSmartFrog(Vector cfgDescriptors) throws
        Exception {
        ProcessCompound process;
        process = runSmartFrog();
        if (cfgDescriptors!=null){
            runConfigurationDescriptors(cfgDescriptors);
        }
        return process;
    }


    /**
     * Run SmartFrog as configured. This call does not exit smartfrog, even if the OptionSet requests it.
     * This entry point exists so that alternate entry points (e.g. Ant Tasks) can start the system.
     * Important: things like the output streams can be redirected.
     * @return the root process
     * @throws SmartFrogException for a specific SmartFrog problem
     * @throws UnknownHostException if the target host is unknown
     * @throws ConnectException if the remote system's SmartFrog daemon is unreachable
     * @throws RemoteException if something goes wrong during the communication
     * @throws SFGeneralSecurityException for security trouble
     */
    public static ProcessCompound runSmartFrog()
            throws SmartFrogException, UnknownHostException, ConnectException,
            RemoteException, SFGeneralSecurityException {

        ProcessCompound process = null;

        initSystem();

        // Redirect output streams
        setOutputStreams();

        // Deploy process Compound
        process = createRootProcess();

        return process;
    }

    protected static ProcessCompound createRootProcess() throws SmartFrogException, RemoteException {
        return SFProcess.deployProcessCompound(true);
    }

    /**
     * initialise the system.  Turn security on, read properties from an ini file
     * and then look at stack tracing.
     * This method is idempotent and synchronised; you can only init the system once.
     * @throws SmartFrogException
     * @throws SFGeneralSecurityException
     */
    synchronized public static void initSystem() throws SmartFrogException,
        SFGeneralSecurityException {
        if (!alreadySystemInit) {
            // Initialize SmartFrog Security
            SFSecurity.initSecurity();
            // Read init properties
            readPropertiesFromIniFile();
            sfLog();
            // Notify status of Security
            if (!SFSecurity.isSecurityOn()){
                String securityRequired = System.getProperty(SFSecurityProperties.propSecurityRequired,"false");
                Boolean secured=Boolean.valueOf(securityRequired);
                if(secured.booleanValue()) {
                    //we need security, but it is not enabled
                    throw new SFGeneralSecurityException(MessageUtil.formatMessage(ERROR_NO_SECURITY_BUT_REQUIRED));
                }
                if (sfLog().isWarnEnabled()) {
                    sfLog().warn(MessageUtil.formatMessage(WARN_NO_SECURITY));
                }

            }
            // Init logging properties
            Logger.init();

            //Test local networking: checks localhost NIC
            if (Logger.testNetwork) {
                StringBuffer result = new StringBuffer();
                boolean failed = Diagnostics.doReportLocalNetwork(result);
                if (failed) {
                   if (sfLog().isWarnEnabled()) { sfLog().warn(result.toString()); }
                } else {
                    if (sfLog().isDebugEnabled()) { sfLog().debug(result.toString()); }
                }
            }
            alreadySystemInit = true;
        }
    }

    /**
     *
     * @return LogSF
     */
    public static LogSF sfLog(){
         if (sflog==null) {
             sflog=LogFactory.sfGetProcessLog();
         }
         return sflog;
    }

    /**
     * test for the system being initialised already
     * @return true if we have already initialised the system
     */
    public static boolean isSmartfrogInit() {
        return alreadySystemInit;
    }

    /**
     * Gets input stream for the given resource. Throws exception if stream is
     * null.
     * @param resourceSFURL Name of the resource. SF url valid.
     * @return Input stream for the resource
     * @throws SmartFrogException if input stream could not be created for the
     * resource
     * @see SFClassLoader
     */
    public static InputStream getInputStreamForResource(String resourceSFURL) throws SmartFrogException {
        InputStream  is = null;
        is = SFClassLoader.getResourceAsStream(resourceSFURL);
        if(is == null) {
            throw new SmartFrogException(MessageUtil.formatMessage(MSG_FILE_NOT_FOUND, resourceSFURL));
        }
        return is;
    }

    /**
     * Gets ByteArray for the given resource. Throws exception if stream is
     * null.
     * @param resourceSFURL Name of the resource. SF url valid.
     * @return ByteArray (byte []) with the resource data
     * @throws SmartFrogException if input stream could not be created for the
     * resource
     * @see SFClassLoader
     */
    public static byte[] getByteArrayForResource(String resourceSFURL) throws SmartFrogException {
        ByteArrayOutputStream bStrm = null;
        try {
            DataInputStream iStrm = new DataInputStream(getInputStreamForResource(resourceSFURL));
            byte resourceData[];
            bStrm = new ByteArrayOutputStream();
            int ch;
            while ((ch = iStrm.read())!=-1) {
                bStrm.write(ch);
            }
            resourceData = bStrm.toByteArray();
            bStrm.close();
            return resourceData;
        } catch (IOException ex) {
          throw SmartFrogException.forward(ex);
        } finally {
          if (bStrm!=null) { try { bStrm.close();} catch (IOException swallowed) { } }
        }
    }

    /**
     * get the root process
     * @return the root process, null for none.
     */
    public ProcessCompound getRootProcess() {
        return rootProcess;
    }

    /**
     * set the root process; this is called after it is started.
     * @param rootProcess process compoond; may be
     */
    public void setRootProcess(ProcessCompound rootProcess) {
        this.rootProcess = rootProcess;
    }


    /**
     * terminate the system. the rootProcess field is set to null afterwards.
     * Any error is dealt with by printing a stack trace to system.err.
     * @param message message to send
     */
    public void terminateSystem(String message) {
        try {
            if (rootProcess != null) {
                //we don't want a system exit any more
                rootProcess.systemExitOnTermination(false);
                //terminate
                rootProcess.sfTerminate(new TerminationRecord(TerminationRecord.NORMAL,
                        message,
                        ((Prim) rootProcess).sfCompleteName()));
            }
        } catch (RemoteException e) {
            e.printStackTrace(System.err);
        } finally {
            setRootProcess(null);
        }

    }
}
