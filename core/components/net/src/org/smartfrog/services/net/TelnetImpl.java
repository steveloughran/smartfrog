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

package org.smartfrog.services.net;

import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TelnetNotificationHandler;
import org.smartfrog.services.passwords.PasswordProvider;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.common.SmartFrogLifecycleException;
import org.smartfrog.sfcore.common.SmartFrogResolutionException;
import org.smartfrog.sfcore.common.TerminatorThread;
import org.smartfrog.sfcore.logging.LogSF;
import org.smartfrog.sfcore.prim.PrimImpl;
import org.smartfrog.sfcore.prim.TerminationRecord;
import org.smartfrog.sfcore.reference.Reference;
import org.smartfrog.sfcore.utils.ListUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.Vector;

/**
 * SmartFrog implementation of telnet component.
 * <p/>
 * It uses apache commons net libraries
 *
 * @author Ashish Awasthi
 */
public class TelnetImpl extends PrimImpl implements Telnet,
    TelnetNotificationHandler {

    private final int DEFAULT_TIMEOUT = 30000;
    private final int DEFAULT_PORT = 23;
    private final String DEFAULT_PROMPT = "#";

    private String host = null;
    private String user = null;
    private String ostype = null;
    private String password = "";
    private String shellPrompt = "$";
    private int port = DEFAULT_PORT;
    private Vector<String> commandsList = null;
    private Vector<String> cmdsFailureMsgs = null;
    private TelnetClient client = null;
    private OutputStream opStream = null;
    private InputStream inpStream = null;
    private int timeout = DEFAULT_TIMEOUT;
    private FileOutputStream fout = null;
    private String logFile = null;
    private Reference pwdProviderRef = new Reference(ATTR_PASSWORD_PROVIDER);
    private PasswordProvider pwdProvider = null;
    private boolean shouldTerminate = true;  // default
    protected LogSF logCore = null;
    protected LogSF logApp = null;
    private static final Reference attr_commands = new Reference(COMMANDS);
    private static final Reference attr_cmds_failure_msgs = new Reference(CMDS_FAILURE_MSGS);

    /**
     * Constructs TelnetImpl object.
     *
     * @throws RemoteException in case of network/rmi error
     */
    public TelnetImpl() throws RemoteException {
    }

    /**
     * Reads SmartFrog attributes and deploys TelnetImpl component.
     *
     * @throws SmartFrogException in case of error in deploying or reading the
     *                            attributes
     * @throws RemoteException    in case of network/emi error
     */
    public synchronized void sfDeploy() throws SmartFrogException,
        RemoteException {
        super.sfDeploy();
        //read SmartFrog Attributes
        readSFAttributes();
    }

    /**
     * Connects to remote host and executes commands.
     *
     * @throws SmartFrogException in case of error in connecting to remote host
     *                            ,executing commands or command output is same
     *                            as failure message provided in the component
     *                            desciption.
     * @throws RemoteException    in case of network/emi error
     */
    public synchronized void sfStart() throws SmartFrogException,
        RemoteException {
        super.sfStart();
        try {
            // create optional log file for the telnet session
            try {
                if (logFile != null) {
                    fout = new FileOutputStream(logFile, false);
                }
            } catch (IOException ioex) {
                sfLog().error("Error in opening log file:"
                    + ioex.getMessage());
            }

            client = new TelnetClient();
            client.connect(host, port);

            opStream = client.getOutputStream();
            inpStream = client.getInputStream();
            boolean operationStatus = waitForString(inpStream, "login:",
                timeout);
            if (operationStatus) {
                String loginName = user + '\n';
                opStream.write(loginName.getBytes());
                opStream.flush();
                if ("linux".equals(ostype)) {
                    operationStatus = waitForString(inpStream, "Password:",
                        timeout);
                } else if ("windows".equals(ostype)) {
                    operationStatus = waitForString(inpStream, "password:",
                        timeout);
                }
            }
            if (operationStatus) {
                String passWd = password + '\n';
                opStream.write(passWd.getBytes());
                opStream.flush();
            }
            operationStatus = isLoginSuccessful(inpStream, shellPrompt,
                timeout);

            if (!operationStatus) {
                throw new SmartFrogLifecycleException(
                    "Unable to login in remote machine");
            }
            if (sfLog().isInfoEnabled()) {
                sfLog().info("login Successful in host:" + host);
            }

            client.registerSpyStream(fout);
            boolean checkCmdExecStatus = false;
            if ((cmdsFailureMsgs != null) && (!cmdsFailureMsgs.isEmpty())) {
                checkCmdExecStatus = true;
            }
            // Execute commands
            for (int i = 0; i < commandsList.size(); i++) {
                String cmd = commandsList.get(i);
                cmd = cmd + '\n';
                opStream.write(cmd.getBytes());
                opStream.flush();
                /*   try {
                                    //Thread.sleep(1000);
                                    this.wait(1000);
                                }catch (InterruptedException e) {
                                    //ignore
                            if (sfLog().isInfoEnabled()) sfLog().error("", e);
                        }
                */
                // wait for prompt to return.
                boolean getPrompt =
                    waitForString(inpStream,
                        shellPrompt,
                        timeout);         // CJB

                // check if command was successfully executed
                if (checkCmdExecStatus) {
                    String errMsg = cmdsFailureMsgs.get(i);
                    boolean execError = waitForString(inpStream, errMsg,
                        timeout);
                    if (execError) {
                        // throw exception
                        throw new SmartFrogTelnetException(cmd, errMsg);
                    }
                }

            }
            // check if it should terminate by itself
            if (shouldTerminate) {
                TerminationRecord termR = TerminationRecord.normal(
                    "Telnet Session finished: ", sfCompleteName());
                TerminatorThread terminator = new TerminatorThread(this, termR);
                terminator.start();
            }
        } catch (Exception e) {
            throw SmartFrogLifecycleException.forward(e);
        } finally {
            client.stopSpyStream();
        }
    }

    /**
     * Life cycle method for terminating the SmartFrog component.
     *
     * @param tr Termination record
     */
    public synchronized void sfTerminateWith(TerminationRecord tr) {
        super.sfTerminateWith(tr);
        try {
            if (client != null) {
                // It also closes input and output streams
                client.disconnect();
            }
        } catch (IOException ioex) {
            sfLog().ignore("When terminating the client",ioex);
            // ignore
        }
    }

    /**
     * Reads SmartFrog attributes.
     *
     * @throws SmartFrogResolutionException if failed to read any attribute or a
     *                                      mandatory attribute is not defined.
     * @throws RemoteException              in case of network/rmi error
     */
    private void readSFAttributes() throws SmartFrogException, RemoteException {
        // Mandatory attributes
        host = sfResolve(HOST, host, true);
        user = sfResolve(USER, user, true);
        ostype = sfResolve(OSTYPE, ostype, true);
        pwdProvider = (PasswordProvider) sfResolve(pwdProviderRef);
        password = pwdProvider.getPassword();
        commandsList = ListUtils.resolveStringList(this, attr_commands,  true);

        //optional attributes
        cmdsFailureMsgs = ListUtils.resolveStringList(this, attr_cmds_failure_msgs, false);
        port = sfResolve(PORT, port, false);
        timeout = sfResolve(TIMEOUT, timeout, false);
        shellPrompt = sfResolve(SHELL_PROMPT, shellPrompt, false);
        logFile = sfResolve(LOG_FILE, logFile, false);
        shouldTerminate = sfResolve(TERMINATE, shouldTerminate, false);
    }

    /**
     * Callback method called when TelnetClient receives an option negotiation
     * command.
     * <p/>
     *
     * @param negotiation_code - type of negotiation command received
     *                         (RECEIVED_DO, RECEIVED_DONT, RECEIVED_WILL,
     *                         RECEIVED_WONT)
     *                         <p/>
     * @param option_code      - code of the option negotiated
     *                         <p/>
     *                         *
     */
    public void receivedNegotiation(int negotiation_code, int option_code) {
        String command = null;
        if (negotiation_code == TelnetNotificationHandler.RECEIVED_DO) {
            command = "DO";
        } else
        if (negotiation_code == TelnetNotificationHandler.RECEIVED_DONT) {
            command = "DONT";
        } else
        if (negotiation_code == TelnetNotificationHandler.RECEIVED_WILL) {
            command = "WILL";
        } else
        if (negotiation_code == TelnetNotificationHandler.RECEIVED_WONT) {
            command = "WONT";
        }
    }

    /**
     * Waits for a string with timeout.
     *
     * @param is      Input Stream which is searched
     * @param end     String to search
     * @param timeout Timeout
     * @return true if string is located in the inp stream, false if search is
     *         timedout or string is not found
     * @throws IOException for network problems
     */
    public boolean waitForString(InputStream is, String end, long timeout) throws IOException {
        byte buffer[] = new byte[32];
        long starttime = System.currentTimeMillis();

        String readbytes = "";
        while ((!readbytes.contains(end)) &&
            ((System.currentTimeMillis() - starttime) < timeout)) {
            if (is.available() > 0) {
                int ret_read = is.read(buffer);
                readbytes = readbytes + new String(buffer, 0, ret_read);
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    //interrupted
                    return false;
                }
            }
        }
        return readbytes.contains(end);
    }

    /**
     * Checks if login is successful.
     *
     * @param is input stream
     * @param end end string to wait for
     * @param loginTimeout how log to wait in millis
     * @return true if login sucessful else false
     * @throws IOException for network problems
     */
    private boolean isLoginSuccessful(InputStream is, String end,
                                      long loginTimeout) throws IOException {
        byte[] buffer = new byte[32];
        long starttime = System.currentTimeMillis();

        String readbytes = "";

        while ((!readbytes.contains(end)) &&
            ((System.currentTimeMillis() - starttime) < loginTimeout)) {
            if (is.available() > 0) {
                int ret_read = is.read(buffer);
                readbytes = readbytes + new String(buffer, 0, ret_read);
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    //interrupted
                    return false;
                }
            }
        }

        return readbytes.contains(end) || readbytes.contains(DEFAULT_PROMPT);
    }
}