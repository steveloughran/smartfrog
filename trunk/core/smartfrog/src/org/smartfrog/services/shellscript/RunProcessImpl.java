/** (C) Copyright 1998-2005 Hewlett-Packard Development Company, LP

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

package org.smartfrog.services.shellscript;

import org.smartfrog.services.shellscript.FilterImpl;
import org.smartfrog.sfcore.logging.LogFactory;
import org.smartfrog.sfcore.logging.LogSF;
import org.smartfrog.sfcore.prim.Prim;

import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Vector;
import org.smartfrog.sfcore.prim.TerminationRecord;
import org.smartfrog.sfcore.common.TerminatorThread;

//------------------- RUNProcess -------------------------------
public class RunProcessImpl  extends Thread implements RunProcess {

    private Prim prim = null; // SF wrapper, it can be null.

    /** Used to format times */
    protected static DateFormat dateFormatter = null;

    static {
      dateFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS zzz");
    }

    public static final int STATE_INACTIVE = 0;
    public static final int STATE_STARTING = 1;
    public static final int STATE_STARTED = 2;
    public static final int STATE_PROCESSING = 3;

    private int state = 0;

    /**
     * Number of execs done, 0 = none
     */
    private Integer numberOfExecs = new Integer(0);

    /**
     * Exit codes from execs. Postion 0 contains 'numberOfExecs' copleted.
     */

    private Vector execExitCodes = new Vector();

    // java Exec exitValue
    int exitValue = -9999;

    public int getProcessState()
    {
        return state;
    }

    public boolean ready()
    {
        return (getProcessState()==STATE_PROCESSING);
    }

    private void setState( int newState )
    {
      if (sfLog.isDebugEnabled()){
        sfLog.debug("setState "+ stateToString(state) + " -> " + stateToString(newState));
      }
      state = newState;
    }



    public void waitForReady(long time){

      //@TODO change to use notify()

      while (!ready()) {
        if (sfLog.isDebugEnabled()) {
          sfLog.debug("WaitForReady");
        }
        try {
          Thread.sleep(time);
        } catch (InterruptedException ex) {
        }
      }
      if (sfLog.isDebugEnabled()){
          sfLog.debug("WaitForReady- Ready");
      }
    }

    private String stateToString( int state )
    {
        String s = null;

        switch ( state )
        {
            case STATE_INACTIVE:
                s = "INACTIVE";
                break;

            case STATE_STARTING:
                s = "STARTING";
                break;

            case STATE_STARTED:
                s = "STARTED";
                break;

            case STATE_PROCESSING:
                s = "PROCESSING";
                break;

            default:
                s = "UNKNOWN";
                break;
        }

        return s;
    }

    private Runtime runtime = Runtime.getRuntime();

    // cmd Data
    private Cmd cmd = new Cmd();

//    private long ID = -1;
    private String name = null;

    private FilterImpl stdoutFilter = null;
    private FilterImpl stderrFilter = null;

    private Process process = null;

    /** Data output stream. It will be used to send commands to process*/
    DataOutputStream processDos = null;

    private volatile boolean killRequested = false;

    private LogSF sfLog = LogFactory.sfGetProcessLog(); //Temp log until getting its own.

    private int sleepBeforeRestart = 1000;

    // Name can be null, not sure if we still need name.
    public RunProcessImpl( String name, Cmd cmd, Prim prim) {
        this(name,cmd);
        this.prim=prim;
    }

    // Name can be null, not sure if we still need name.
    public RunProcessImpl( String name, Cmd cmd) {
        this.setName(name);
        if (name == null) name = "";

        this.cmd = cmd;
//        this.ID = ID;
        this.name = name;
        setName("RunProcess");
        sfLog = LogFactory.getLog(name);
        killRequested = false;
        execExitCodes.add(numberOfExecs);
    }

    public void run() {
        {
            startProcess();
            if ((prim!=null) && (prim instanceof SFReadConfig)){
                try {
                    ((SFReadConfig)prim).readConfig();
                } catch (Exception ex) {
                    if (sfLog.isWarnEnabled()){ sfLog.warn(ex); }
                }
            }
            if (cmd.restart()) {
                try {  this.sleep(sleepBeforeRestart); } catch (InterruptedException ex1) {  }
            }
        } while (cmd.restart());

        if ((prim!=null)&&(cmd.terminate()||cmd.detatch())){
           String terminationType = TerminationRecord.ABNORMAL;
           if (exitValue==0){
               terminationType=TerminationRecord.NORMAL;
           }
           TerminationRecord termR = new TerminationRecord(terminationType, "Exit code: " + exitValue , null);

           TerminatorThread terminator = new TerminatorThread(prim,termR);
           if (cmd.detatch())   {
               terminator.detach();
           }
           if (!cmd.terminate()) {
               terminator.dontTerminate();
           }
           terminator.start();
           this.prim = null;
        }
    }

    private void startProcess() {
        // Check that a kill has not been requested even before the application has started.
        if (killRequested==true) {
            setState(STATE_INACTIVE);
            return;
        }
        setState(STATE_STARTING);
        exitValue = -9999;

        try {
            synchronized (cmd) {
                if (sfLog.isDebugEnabled()){
                    sfLog.debug(cmd.toString());
                }
                process = runtime.exec(cmd.getCmdArray(), cmd.getEnvp(),
                                       cmd.getFile());
                setState(STATE_STARTED);
                if (sfLog.isTraceEnabled()){
                   sfLog.trace("attaching data output stream");
                }
                processDos = new DataOutputStream(process.getOutputStream());

                replaceFilters(
                  new FilterImpl( name, process.getInputStream(), "out", cmd.getFiltersOut(), cmd.getFilterOutListener()),
                  new FilterImpl( name, process.getErrorStream(), "err", cmd.getFiltersErr(), cmd.getFilterErrListener())
                );

            // process may be null by the time we get here after the synchronized
            // block above if a terminate has been requested while the app was
            // starting or the exec failed, e.g. the FP application is not installed
            // on the node.  This will cause a NullPointerException but there is
            // nothing else to do anyway but tidy up.
            if (process!=null) {
                setState(STATE_PROCESSING);
                cmd.notify();
                if (sfLog.isTraceEnabled()){
                      sfLog.trace("waiting for application to exit");
                }
                processStarted();
                exitValue = process.waitFor();
                processFinished();

            } else {
                cmd.notify();
                if (sfLog.isWarnEnabled()){
                      sfLog.warn("process null");
                }
            }
          } //synchronized

        } catch (Throwable t) {
          if (sfLog.isErrorEnabled()){
            sfLog.error("failed to complete execution", t);
          }
        }

        int endState = state;

        setState(STATE_INACTIVE);

        if (!killRequested) {
            // If a kill was requested then this is taken care of in kill().
            try {
                stopFilters();

                // Necessary to free O/S resources??
                // If the process was killed, there is no need to do this.
                //
                // If the exec failed, process will already be null.
                if (process!=null) {
                    process.destroy();
                    process = null;
                }

                // If the exec completed normally then now that we have tidied up.
                if (endState==STATE_PROCESSING&&exitValue==0) {
                    //Done!
                    if (sfLog.isDebugEnabled()){
                        sfLog.debug("Succesfully completed!");
                    }
                } else {
                  // Something went wrong,
                  String message = null;
                  if (endState == STATE_STARTED) {
                    message ="Application executed, but finished without starting processing";
                  } else if (endState == STATE_STARTING) {
                    message = "Application failed to execute";
                  } else if (exitValue != 0) {
                    message = "Application finished prematurely due to an application/system error";
                  } else {
                    message = "Unexpected application state: " + state;
                  }
                  if (sfLog.isWarnEnabled()) {
                    sfLog.warn(message);
                  }
                }
                // Regardless of what happens, make sure to indicate that the application
                // has stopped.
            } catch (Throwable t) {
              if (sfLog.isErrorEnabled()) {
                sfLog.error("failed to release resources", t);
              }
            }
        }
        if (sfLog.isInfoEnabled()){
            sfLog.info("exit code = " + exitValue);
        }
    }


    private void processStarted(){
        //Update counter
        int count = numberOfExecs.intValue()+1;
        this.numberOfExecs = new Integer(count);
        if (prim!=null) {
            try {
                prim.sfReplaceAttribute(SFExecution.ATR_NUMBER_OF_EXECS,this.numberOfExecs);
            } catch (Exception ex) {
                if (sfLog.isWarnEnabled()) { sfLog.warn(ex); }
            }
        }
    }
    private void processFinished(){
        //Update counters
        Integer exitCode = new Integer(exitValue);
        this.execExitCodes.add(0,numberOfExecs);
        this.execExitCodes.add(exitCode);
        if (prim!=null) {
            try {
                prim.sfReplaceAttribute(SFExecution.ATR_EXEC_EXIT_CODE, exitCode);
                prim.sfReplaceAttribute(SFExecution.ATR_EXEC_EXIT_CODES, execExitCodes);
            } catch (Exception ex) {
                if (sfLog.isWarnEnabled()){ sfLog.warn(ex); }
            }
        }
    }

    public synchronized void replaceFilters(FilterImpl fout, FilterImpl ferr) {
      if (sfLog.isTraceEnabled()){
        sfLog.trace("attaching filters");
      }
      stopFilters();

      stdoutFilter = fout;
      stderrFilter = ferr;

      stdoutFilter.start();
      stderrFilter.start();
      if (sfLog.isTraceEnabled()){
        sfLog.trace("filters attached");
      }
    }

    public FilterImpl getOutFilter(){
        return stdoutFilter;
    }

    public FilterImpl getErrFilter(){
        return stderrFilter;
    }

    private void stopFilters() {
        // Stop filters after process has died so that the process doesn't fill its
        // output buffer(s).
        if (stdoutFilter!=null) {
            stdoutFilter.stopRequest();
        }

        if (stderrFilter!=null) {
            stderrFilter.stopRequest();
        }

        waitForFilters();
        stdoutFilter = null;
        stderrFilter = null;
        if (sfLog.isDebugEnabled()) {
          sfLog.debug("Application log stopped");// @ "+ dateFormatter.format(new Date()));
        }
    }

    private void waitForFilters() {
      if (sfLog.isTraceEnabled()) {
        sfLog.trace("waiting for filters to stop");
      }
      boolean bothFinished = false;

      while (!bothFinished) {
          if (stdoutFilter!=null) {
              try {
                if (sfLog.isTraceEnabled()) {
                  sfLog.trace(" waiting for stdout filter");
                }
                stdoutFilter.join();
              } catch (InterruptedException e) {
                if (sfLog.isTraceEnabled()) {
                  sfLog.trace("interrupted while waiting for stdout filter", e);
                }
                continue;
              }
          }

          if (stderrFilter!=null) {
              try {
                if (sfLog.isTraceEnabled()) {
                  sfLog.trace("waiting for stderr filter");
                }
                  stderrFilter.join();
              } catch (InterruptedException e) {
                if (sfLog.isTraceEnabled()) {
                  sfLog.trace("interrupted while waiting for stderr filter");
                }
                continue;
              }
          }

          bothFinished = true;
      }

      if (sfLog.isDebugEnabled()) {
        sfLog.debug("filters stopped");
      }

    }

    public void kill() {
        // It is possible to request a kill before the process has actually been
        // created.  If this is the case just prevent the run() method from creating
        // the process.
        synchronized (this) {
            if (process==null) {
              if (sfLog.isTraceEnabled()) {
                sfLog.trace("kill " + name + " -- application has not started yet");
              }
              killRequested = true;
            }
        }

        // If the process has already been created then kill it.
        if (!killRequested) {
          if (sfLog.isTraceEnabled()) {
            sfLog.trace("kill" + " -- terminating process");
          }
          killRequested = true;
          process.destroy();

          try {
            if (sfLog.isTraceEnabled()) {
              sfLog.trace("kill" + " -- waiting for exit");
            }
            process.waitFor();
          } catch (InterruptedException e) {
          }

          if (sfLog.isDebugEnabled()) {
            sfLog.debug("kill"+ " -- exit = " + process.exitValue());
          }
          process = null;
          try {
              processDos.close();
          } catch (IOException e) {
          }
          // Wait for filters to stop before issuing Terminated
          stopFilters();
          // notify termination!
          // @todo
        }
    }

    /**
      *  Executes the given command.
      *
      * @param  cmd  command to be exceuted
      */
     public void execCommand(String command) {
         if ((command==null)|| killRequested || state!=STATE_PROCESSING ){
           //@TODO thow exception? Log return cause
           return;
         }
         command = command + cmd.getLineSeparator();
         synchronized (processDos) {
           if (processDos != null) {
             try {
               if (sfLog.isDebugEnabled()) {
                 sfLog.debug("Executing IN command: " + command);
               }
               processDos.writeBytes(command);
               processDos.flush();
             } catch (IOException ex) {
               if (sfLog.isErrorEnabled()) {
                 sfLog.error(ex);
               }
             }
           } else {
             if (sfLog.isErrorEnabled()) {
               sfLog.error("Error: Stream closed. Process probably terminated.");
             }
           }
         }
     }


     /**
      * Gets the input stream of the subprocess.
      * The stream obtains data piped from the standard output stream
      * of the process (<code>Process</code>) object.
      * <p>
      * Implementation note: It is a good idea for the input stream to
      * be buffered.
      *
      * @return  the input stream connected to the normal output of the
      *          subprocess.
      */

     public synchronized InputStream getInputStream(){
       return process.getInputStream();
     }

     /**
      * Gets the error stream of the subprocess.
      * The stream obtains data piped from the error output stream of the
      * process (<code>Process</code>) object.
      * <p>
      * Implementation note: It is a good idea for the input stream to be
      * buffered.
      *
      * @return  the input stream connected to the error stream of the
      *          subprocess.
      */
     public synchronized InputStream getErrorStream(){
        return process.getErrorStream();
     }

     /**
      * Gets the output stream of the subprocess.
      * Output to the stream is piped into the standard input stream of
      * the process (<code>Process</code>) object.
      * <p>
      * Implementation note: It is a good idea for the output stream to
      * be buffered.
      *
      * @return  the output stream connected to the normal input of the
      *          subprocess.
      */
     public synchronized OutputStream getOutputStream() {
         return process.getOutputStream();
     }

}

