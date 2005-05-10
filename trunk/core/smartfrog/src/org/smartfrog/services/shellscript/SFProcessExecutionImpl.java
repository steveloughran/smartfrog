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

import java.util.*;

import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.componentdescription.ComponentDescriptionImpl;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.prim.PrimImpl;
import java.rmi.RemoteException;
import org.smartfrog.sfcore.prim.TerminationRecord;
import java.io.InputStream;
import java.io.OutputStream;

public class SFProcessExecutionImpl  extends PrimImpl implements Prim, SFProcessExecution, SFReadConfig {

  private long ID = -1;
  private String name = null;

  /**
   * Exec data
   */
  private Cmd cmd = new Cmd();

  /**
   * Process Exec component
   */
  private  RunProcess runProcess = null;

  // For Prim
  public SFProcessExecutionImpl() throws RemoteException {

  }

  //Component and LiveCycle methods

  /**
   *  Reads SF description = initial configuration.
   * Override this to read/set properties before we read ours, but remember to call
   * the superclass afterwards
   */
  public void  readConfig() throws SmartFrogException, RemoteException {
      this.ID = sfResolve(ATR_ID, ID, true);
      this.name = sfResolve(ATR_NAME, name, false);
      if (name==null) {
          name = this.sfCompleteNameSafe().toString();
      }
      this.cmd = new Cmd(sfResolve(ATR_EXEC, new ComponentDescriptionImpl(null, null, false), true));
  }

  /**
   *  This method retrieves the paramters from the .sf file. For the purposes
   *  of a demo default paramteres could be hard coded.
   *
   * @exception SmartFrogException deployment failure
   * @exception  RemoteException In case of network/rmi error
   */
  public synchronized void sfDeploy() throws SmartFrogException, RemoteException {
      super.sfDeploy();
      readConfig();
      // RunProcessImpl
      if (cmd.autoStart()){
          runProcess = new RunProcessImpl(ID, name, cmd, this);
          ((RunProcessImpl)runProcess).start();
          runProcess.waitForReady(200);
          sfLog().info("Process started");
      }
  }
  /**
  *  This sets a flag that will start the httpd process running.
  *
  * @exception  SmartFrogException starting failure
  * @exception  RemoteException In cas eof network/rmi error
  */
 public synchronized void sfStart() throws SmartFrogException,RemoteException {
     super.sfStart();
 }


 /**
  *  This shuts down Apache by requesting that the ApacheState variable be
  *  set to false.
  *
  * @param  tr  TerminationRecord object
  */
 public synchronized void sfTerminateWith(TerminationRecord tr) {
     try {
         if (sfLog().isDebugEnabled()){
             sfLog().debug("Terminating.",null,tr);
          }
         if (runProcess != null) {
             (runProcess).kill();
             runProcess = null;
         }
     } catch (Exception ex) {
     }
     super.sfTerminateWith(tr);
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

  public InputStream getStdOutStream(){
      if (runProcess!=null) {
          return runProcess.getInputStream();
      } else {
          return null;
      }
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
  public InputStream getStdErrStream(){
      if ( runProcess!=null){
     return runProcess.getErrorStream();
      } else {
          return null;
      }
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
  public OutputStream getStdInpStream() {
      if ( runProcess!=null){
          return runProcess.getOutputStream();
      } else {
          return null;
      }
     }

     /**
      * Kill the process
      */
     public void kill(){
         if (runProcess==null) {
             return;
         } else {
            runProcess.kill();
         }
     }

     /**
      * Restarts the process
      */
     public void restart() throws SmartFrogException {
         if (runProcess!=null){
            runProcess.kill();
            runProcess = null;
         }
        try {
            readConfig();
        } catch (RemoteException rex) {
            throw SmartFrogException.forward("Problem during reStart ",rex);
        }
         // RunProcessImpl
         runProcess = new RunProcessImpl (ID, name, cmd);
         runProcess.waitForReady(200);
         sfLog().info("Restart done");

     }

     /**
      * Is the process ready?
      * @return boolean
      */
     public boolean isRunning() {
         if (runProcess!=null){
             return runProcess.ready();
         } else {
             return false;
         }
     }

}
