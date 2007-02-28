/** (C) Copyright 1998-2007 Hewlett-Packard Development Company, LP

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

package org.smartfrog.services.os.runshell;

import org.smartfrog.services.display.PrintErrMsgInt;
import org.smartfrog.services.display.PrintMsgInt;
import org.smartfrog.services.utils.generic.OutputStreamIntf;
import org.smartfrog.services.utils.generic.StreamGobbler;
import org.smartfrog.services.utils.generic.StreamIntf;
import org.smartfrog.services.filesystem.FileSystem;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.common.SmartFrogLifecycleException;
import org.smartfrog.sfcore.common.SmartFrogResolutionException;
import org.smartfrog.sfcore.common.TerminatorThread;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.prim.PrimImpl;
import org.smartfrog.sfcore.prim.TerminationRecord;
import org.smartfrog.sfcore.utils.ComponentHelper;
import org.smartfrog.sfcore.logging.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;


/**
 *  This class implements the RunShell component to run a shell of things
 *
 */
public class RunShellImpl extends PrimImpl implements Prim, RunShell, Runnable {
    //ToDo provide and EXIT command to terminate Shell!
    /** String name for Process name. */
    private String processName = "defaultRunShell";

    //desired
    /** String name for process Id. */
    private String processId = "";
    /** String name for shell prefix. */
    private String[] shellPrefix = { "" };
    /** String name for shell command. */
    private String shellCommand = null;
    /** Set of shell command attributes. */
    private Vector shellCommandAtt = new Vector();
    /** strign name for exit command. */
    private String exitCmd = "exit 0";
    /** Flag indicating exit command. */
    private boolean useExitCmd = true;
    /** String name for line return. */
    private String lineReturn = "\n";

    //desired

    /**
     * The String for working directory.
     */
    private String workDir = ".";

    //File workDir = null; //desired
    /** String name for env properties. */
    private String[] envProp = null;

    /** Vector used to store environment variables */
    private Vector envVarsVector = null;


    //optional
    /** Set of commands. */
    private Vector cmds = null;

    // Process Data
    /** Runtime. */
    private Runtime runtime = Runtime.getRuntime();
    /** sub process. */
    private Process subProcess = null;
    /** delay between commands. */
    private int delayBetweenCmds = 0;
    /** Data output stream. */
    private DataOutputStream dos = null;
    /** Thread object. */
    private Thread thread = null;

    //Printers...
    /** Print message. */
    private PrintMsgInt printMsgImp = null;
    /** Print error message. */
    private PrintErrMsgInt printErrMsgImp = null;

    //Stream consumers
    /** Output stream consumer. */
    private OutputStreamIntf outputStreamObj = null;
    /** Error stream consumer. */
    private StreamIntf errorStreamObj = null;

    /** Should wait for GoAhead after executing a command? */
    private boolean waitSignalGoAhead = false;

    // Auxiliar variables for termination
    /** String for Termination type. */
    private String terminationType = "";
    /** TerminationRecord object. */
    private TerminationRecord terminationRecord;
    /** Flag indicating detachment. */
    private boolean shouldDetach = false;
    /**
     * flag indicating termination.
     * whenever execution ends
     */
    private boolean shouldTerminate = true;

    /**
     * only relevion if the {@link #shouldTerminate}
     * field is false.
     */
    private boolean terminateOnFailure=false;
    /**
     * Should we print the command on failure? The default is false, so that
     * nothing sensitive appears in logs.
     */
    private boolean printCommandOnFailure;

    /** logger value. */
    private int logger = 2;

    /** log */
    private Log log;

    /** helper */
    private ComponentHelper helper;
    private String fullShellCommand;
    public static final String STATUS_RUNNING = "running";

    /**
     *  Constructor.
     * @exception  RemoteException  In case of network/rmi error
     */
    public RunShellImpl() throws RemoteException {
    }

    /**
     *  This method retrieves the paramters from the .sf file. For the purposes
     *  of a demo default paramteres could be hard coded.
     *
     * @exception SmartFrogException deployment failure
     * @exception  RemoteException In case of network/rmi error
     */
    public synchronized void sfDeploy() throws SmartFrogException,
        RemoteException {
        try {
            super.sfDeploy();
            helper=new ComponentHelper(this);
            log=sfLog();
            readSFAttributes();

            //Create subProcess
            File workDirFile = new File(workDir);
            String[] commands = createCmd(shellPrefix, shellCommand, shellCommandAtt);
            if (log.isDebugEnabled()) {
                fullShellCommand = arrayToString(commands, "  '", "'\n");
                StringBuffer buffer=new StringBuffer();
                buffer.append("Running in dir ");
                buffer.append(workDirFile);
                buffer.append('\n');
                buffer.append(fullShellCommand);
                log.debug(buffer);
            }
            fullShellCommand = arrayToString(commands, " '", "' ");
            subProcess = runtime.exec(
                    commands,
                    envProp,
                    workDirFile);
            dos = new DataOutputStream(subProcess.getOutputStream());

            StreamGobbler outputGobbler;
            StreamGobbler errorGobbler;
            OutputStream outputStream = null;

            // any output?
            if (outputStreamObj != null) {
                outputStream = outputStreamObj.getOutputStream();
            }
            outputGobbler = new StreamGobbler(subProcess.getInputStream(),
                    "[" + getNotifierId() + "] " + "OUT",
                    outputStream, printMsgImp);

            // any error message?
            if (errorStreamObj != null) {
                outputStream = errorStreamObj.getOutputStream();
            } else {
                outputStream = null;
            }
            errorGobbler = new StreamGobbler(subProcess.getErrorStream(),
                    "[" + getNotifierId() + "] " + "ERR",
                    outputStream, printErrMsgImp);

            errorGobbler.setPassType(false);
            outputGobbler.setPassType(false);

            // kick them off
            errorGobbler.start();
            outputGobbler.start();
            thread = new Thread(this);

            //Start listener
            thread.start();
            sfReplaceAttribute(varStatus, "deployed");
        } catch (Throwable t) {
            throw SmartFrogLifecycleException.sfDeploy(t.getMessage(),t,this);
        }
    }

    /**
     *  Main processing method for the RunProcess object.
     */
    public void run() {
        int exitVal = 0;
        boolean failed=false;
        try {
            // wait until process finishes
            exitVal = subProcess.waitFor();
            //evaluate the result
            failed = didProcessFail(exitVal);
            sfReplaceAttribute(varStatus, "finished");
            sfReplaceAttribute(varExitValue,new Integer(exitVal));
            terminationType = failed ? TerminationRecord.ABNORMAL : TerminationRecord.NORMAL;
            if (sfLog().isDebugEnabled()) sfLog().debug("Finished: " + terminationType + ", ExitVal: " + exitVal);

            //Thread.sleep(3*1000);
        } catch (Exception ex) {
            failed=true;
            terminationType= TerminationRecord.ABNORMAL;
            if (sfLog().isErrorEnabled()) {
                sfLog().error(ex.getMessage(), ex);
            }

            try {
                sfReplaceAttribute(varStatus, "terminated");
            }
            catch (Exception ex1) {
                //Ignore
                sfLog().ignore("updating termination status",ex1);
            }
        }

        if (failed && printCommandOnFailure) {
            sfLog().error(fullShellCommand);
            sfLog().error(" exit value=" + exitVal);
        }
        StringBuffer details=new StringBuffer("Command ");
        details.append(shellCommand);
        details.append(" finished: ");
        details.append(getNotifierId());
        details.append(" exit value="+exitVal);
        if(failed && printCommandOnFailure) {
            details.append("\ncommand=");
            details.append(fullShellCommand);
        }
        terminationRecord = (new TerminationRecord(terminationType,
                details.toString(), null));

        TerminatorThread terminator = new TerminatorThread(this, terminationRecord);
        if (shouldDetach)   {
            terminator.detach();
        }
        if (!shouldTerminate) {
            if(!(terminateOnFailure && exitVal!=0)) {
            terminator.dontTerminate();
            }
        }
        terminator.start();
    }


    /**
     * This is something for subclasses to override if they have very
     * special logic as to what constitutes success and failure
     * @param exitVal the exit code
     * @return true if this is to be interpreted as a failure
     */
    protected boolean didProcessFail(int exitVal) {
        return exitVal != 0;
    }

    /**
     *  Reads SF description = initial configuration.
     * Override this to read/set properties before we read ours, but remember to call
     * the superclass afterwards
     * @throws SmartFrogException
     * @throws RemoteException
     */
    protected void readSFAttributes() throws SmartFrogException, RemoteException {

        logger = sfResolve(varLogger,logger,false);

        //Optional attributes
        delayBetweenCmds = sfResolve(varDelayBetweenCmds,delayBetweenCmds,true);
        //4 methods for intf...

        waitSignalGoAhead = sfResolve(varWaitSignalGoAhead,waitSignalGoAhead,true);
        shouldTerminate = sfResolve(varShouldTerminate,shouldTerminate,true);
        terminateOnFailure = sfResolve(varTerminateOnFailure,terminateOnFailure, true);
        shouldDetach = sfResolve(varShouldDetach,shouldDetach,true);
        lineReturn = sfResolve(varLineReturn, lineReturn, true);
        processId = sfResolve(varSFProcessId, processId, false);
        shellCommand = sfResolve(varShellCommand,shellCommand, false);
        workDir = sfResolve(varSFWorkDir, workDir, false);
        exitCmd = sfResolve(varExitCmd, exitCmd, true);
        useExitCmd =sfResolve(varUseExitCmd, useExitCmd, true);
        shellCommandAtt = readShellAttributes();
        cmds = readVarData(varCMDs);
        envVarsVector = sfResolve(varEnvProp, envVarsVector, true);
        if( (envVarsVector != null) && !(envVarsVector.isEmpty())) {
            envVarsVector.trimToSize();
            envProp = new String[envVarsVector.size()];
            envVarsVector.copyInto(envProp);
        }

        processName = sfResolve(varSFProcessName,processName,true);

        outputStreamObj = (OutputStreamIntf) sfResolve(varOutputStreamTo, outputStreamObj , false);
        errorStreamObj  = (StreamIntf) sfResolve(varErrorStreamTo, errorStreamObj , false);

        printMsgImp  =   (PrintMsgInt) sfResolve(varOutputMsgTo, printMsgImp , false);
        printErrMsgImp = (PrintErrMsgInt) sfResolve(varErrorMsgTo, printErrMsgImp , false);
        printCommandOnFailure = sfResolve(varPrintCommandOnFailure,false,true);

    }



    /**
     *  This sets a flag that will start the httpd process running.
     *
     * @exception  SmartFrogException starting failure
     * @exception  RemoteException In case of network/rmi error
     */
    public synchronized void sfStart()
            throws SmartFrogException,RemoteException {
        super.sfStart();
        sfReplaceAttribute(varStatus, STATUS_RUNNING);
        execBatch(cmds);
    }

    /**
     *  This shuts down the program  if it is running
     *
     * @param  tr  TerminationRecord object
     */
    public synchronized void sfTerminateWith(TerminationRecord tr) {
        FileSystem.close(dos);
        dos=null;
        try {
            if (subProcess != null) {
                subProcess.destroy();
                subProcess = null;
            }
        } catch (Exception ex) {
            if (sfLog().isErrorEnabled()) sfLog().error(ex);
        }

        super.sfTerminateWith(tr);
    }

    /**
     *  Executes the given command.
     *
     * @param  cmd  command to be exceuted
     */
    public void execCmd(String cmd) {
        if (cmd==null) return;
        cmd = cmd + lineReturn;

        if (delayBetweenCmds > 0) {
            try {
                Thread.sleep(delayBetweenCmds * 1000);
            } catch (InterruptedException iex) {
                if (sfLog().isErrorEnabled()) sfLog().error(iex);
            }
        } else {
            if (sfLog().isTraceEnabled()) sfLog().trace("No delay between two consecutive cmds.");
        }

        synchronized(this) {
            if (dos != null) {
                try {
                    if (sfLog().isDebugEnabled()) sfLog().debug("Executing: " + cmd);
                    dos.writeBytes(cmd);
                    dos.flush();
                } catch (IOException ex) {
                    sfLog().error(ex);
                }
            } else {
               sfLog().error("Error: Stream closed. Shell probably terminated.");
            }
        }
    }

    /**
     *  Exceutes the batch of commands.
     *
     * @param  commands  vector of commands to be executed
     */
    public  void execBatch(Vector commands) {

        if ((commands==null)||(commands.isEmpty())) return;

        if (sfLog().isDebugEnabled()) sfLog().debug("Executing Batch: " + commands);
        Object element;

        for (Enumeration e = commands.elements(); e.hasMoreElements();) {
            element = e.nextElement();
            if (element instanceof String) {
                execCmd((String) element);
            } else {
                sfLog().warn("Ignoring non-string command "+element);
            }
        }

        synchronized(this) {
            if (useExitCmd) {
                execCmd(exitCmd);
            }
        }

        if (sfLog().isDebugEnabled()) sfLog().debug("Executing Batch: DONE." + commands);
    }

    // Aux Methods!

    /**
     *  Read all Replace Var Attributes in Vectors
     *
     *@param  typeAttrib  all var attributes
     *@return a vector containing all Replace Var Attributes or null.
     */
    private Vector readVarData(String typeAttrib) {
        if (typeAttrib==null) return null;

        if (sfLog().isTraceEnabled()) sfLog().trace(" runShell.readVarData()");

        Object key;
        Object value;
        Vector data = new Vector();

        for (Enumeration e = sfContext().keys(); e.hasMoreElements();) {
            key = e.nextElement();

            if (key instanceof String) {
                try {
                    if (((String) key).startsWith(typeAttrib)) {
                        value = (sfResolve((String) key));

                        if (value instanceof Vector) {
                            Object element;

                            for (Enumeration enu = ((Vector) value).elements();
                                    enu.hasMoreElements();) {
                                element = enu.nextElement();

                                if (element instanceof String) {
                                    data.add(element);
                                    if (sfLog().isTraceEnabled())
                                        sfLog().trace("runShell.readVarData().Adding(Vect): " + element);
                                }
                            }
                        } else if (value instanceof String) {
                            data.add(value);
                            if (sfLog().isTraceEnabled())
                                sfLog().trace("runShell.readVarData().Adding(Str):  " +value);
                        }
                    }
                } catch (Exception ex) {
                    sfLog().error("Failed to read an element in runShell.readVarData()",ex);
                }
            }
        }

        if (data.isEmpty()) {
            sfLog().debug("runShell.readVarData(): No data.");
            data = null;
        }

        if (sfLog().isTraceEnabled()) sfLog().trace("runShell.readVarData().data: " + data);
        return data;
    }

    /**
     *
     *  Read all shell Attributes in Vectors. If and attribute name ends with
     *  'b' then it is added to the previous one!
     *  We also read in the shell arguments list as is.
     * @return    a vector containing all shell attributes
     * @throws SmartFrogResolutionException if an attribute is missing
     * @throws RemoteException on network problems
     */
    private Vector readShellAttributes() throws SmartFrogResolutionException, RemoteException {
        Vector shellCommandAttrs = new Vector();
        Object key;
        String auxString;

        //read in an argument list
        Vector arguments=sfResolve(varShellArguments,(Vector)null,true);
        if(arguments!=null) {
            shellCommandAttrs.addAll(arguments);
        }
        //now read in all components call Att and add them as arguments too
        for (Enumeration e = sfContext().keys(); e.hasMoreElements();) {
            key = e.nextElement();

            if (key instanceof String) {
                try {
                    if (((String) key).startsWith(varShellCommand + "Att")) {
                        if (((String) key).endsWith("b")) {
                            // To concatenate two parameters
                            // for parameters like ATTa=ATTb
                            //shellCommandAtt = shellCommandAtt + "" +
                //sfResolve((String)key);
                //former code when shellCommandAtt was a String
                            auxString = shellCommandAttrs.lastElement() +
                                (sfResolve((String) key)).toString();
                            shellCommandAttrs.remove(shellCommandAttrs.size() - 1);
                            shellCommandAttrs.add(auxString);
                        } else {
                            shellCommandAttrs.add((sfResolve((String) key)).
                        toString());
                        }
                    }
                } catch (Exception ex) {
                    if (sfLog().isErrorEnabled()) sfLog().error(ex);
                }
            }
        }

        return shellCommandAttrs;
    }

    /**
     * Test for an argument being empty
     * @param arg argument to check
     * @return true iff arg is null, an empty string, or a space.
     */
    private boolean isEmptyArg(String arg) {
        return arg==null || arg.length()==0 || " ".equals(arg);
    }
    /**
     *  Creates a command.
     *
     *@param  cmdGeneral  command
     *@param  cmdStr      command string
     *@param  attributes  attributes
     *@return             textual representation of command
     */
    private String[] createCmd(String[] cmdGeneral, String cmdStr,
        Vector attributes) {
        String[] cmd = null;

        try {
            if (attributes == null) {
                attributes = new Vector();
                attributes.add("");
            }

            int additionalParam = 3;

            int i = 0;
            cmd = new String[attributes.size() + additionalParam];

            //Cleaning empty args in the array
            for (int a = 0; a < cmdGeneral.length; a++) {
                if (!isEmptyArg(cmdGeneral[a])) {
                    cmd[i++] = cmdGeneral[a];
                }
            }

            if (!isEmptyArg(cmdStr)) {
                cmd[i++] = cmdStr;
            }

            if (!attributes.isEmpty()) {
                attributes.trimToSize();

                Iterator iter = attributes.iterator();

                while (iter.hasNext()) {
                    String temp = (String) iter.next();

                    if (!isEmptyArg(temp)) {
                        cmd[i++] = temp;
                    }
                }

                //end envProp
            }

            //Cleaning end empty args
            String[] result = new String[i];

            System.arraycopy(cmd, 0, result, 0, i);

            cmd = result;
            if (sfLog().isDebugEnabled()) sfLog().debug("CreatedCmd:" + arrayToString(cmd, "'", "'"));
        } catch (Exception e) {
            if (sfLog().isErrorEnabled()) sfLog().trace("SFRunCommand.createCmd:Error creating Cmd.(" + e.getMessage() +")",e);
        }

        return cmd;
    }

    /**
     *  Converts array to string
     *
     * @param  array  array
     * @param prefix prefix before every element
     * @param suffix suffix after every element
     * @return        array converted to string
     */
    private String arrayToString(String[] array, String prefix, String suffix) {
        int i = 0;
        StringBuffer stringB = new StringBuffer();

        while (i < array.length) {
            stringB.append(prefix);
            stringB.append(array[i++]);
            stringB.append(suffix);
        }

        return stringB.toString();
    }

    /**
     *  Gets the identity of the notifier process.
     *
     * @return process identity
     */
    public String getNotifierId() {
        return (processName + processId);
    }
}
