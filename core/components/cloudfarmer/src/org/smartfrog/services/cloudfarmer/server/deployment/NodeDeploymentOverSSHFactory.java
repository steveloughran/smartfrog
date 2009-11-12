package org.smartfrog.services.cloudfarmer.server.deployment;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.smartfrog.services.cloudfarmer.api.NodeDeploymentService;
import org.smartfrog.services.cloudfarmer.api.NodeDeploymentServiceFactory;
import org.smartfrog.services.ssh.AbstractSSHComponent;
import org.smartfrog.services.ssh.SSHComponent;
import org.smartfrog.services.ssh.ScpTo;
import org.smartfrog.services.ssh.SshCommand;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.componentdescription.ComponentDescription;
import org.smartfrog.sfcore.logging.OutputStreamLog;
import org.smartfrog.sfcore.logging.LogLevel;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * This factory copies applicatiions over then starts them.
 */
public class NodeDeploymentOverSSHFactory extends AbstractSSHComponent
        implements NodeDeploymentServiceFactory, SSHComponent {

    private String destDir;
    protected JSch jschInstance;
    public static final String ATTR_DEST_DIR = "destDir";
    protected int outputLogLevel;
    public static final String ATTR_LOG_LEVEL = "logLevel";

    public NodeDeploymentOverSSHFactory() throws RemoteException {
    }

    /**
     * At startup, the jsch instance is created
     *
     * @throws SmartFrogException problems -can include a nested JSchException
     * @throws RemoteException network problems
     */
    @Override
    public void sfStart() throws SmartFrogException, RemoteException {
        super.sfStart();
        destDir = sfResolve(ATTR_DEST_DIR, "", true);
        if (!destDir.endsWith("/")) {
            destDir += "/";
        }
        outputLogLevel = sfResolve(ATTR_LOG_LEVEL,LogLevel.LOG_LEVEL_INFO, true);
        try {
            jschInstance = createJschInstance();
        } catch (JSchException e) {
            throw forward(e);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDeploymentService createInstance(String hostname) throws IOException, SmartFrogException {
        return new NodeDeploymentOverSSH(hostname);
    }

    /**
     * Create a session instance if one is needed
     *
     * @param host target host
     * @return a session
     * @throws JSchException connection problems
     */
    public synchronized Session demandCreateSession(String host) throws JSchException {
        return createSession(jschInstance, host, port);
    }


    /**
     * SSH based deployment, assumes deploy-by-copy to a specified destdir, uses a given login
     * <p/>
     * This is an inner class and it uses the parent to do the work
     */
    public class NodeDeploymentOverSSH implements NodeDeploymentService {

        private String hostname;
        private String appDir;
        private ArrayList<File> sourceFiles = new ArrayList<File>();
        private ArrayList<String> destFiles = new ArrayList<String>();


        /**
         * Create an instance
         *
         * @param hostname target host
         */
        public NodeDeploymentOverSSH(String hostname) {
            this.hostname = hostname;
        }

        private void bindAppDir(String app) {
            appDir = destDir + app;
            if (!appDir.endsWith("/")) {
                appDir += "/";
            }
        }

        /**
         * Add a source file
         *
         * @param localFilePath local filename
         * @param destFilename the destination filename
         */
        private synchronized void addSourceFile(File localFilePath, String destFilename) {
            sourceFiles.add(localFilePath);
            String destPath = appDir + destFilename;
            destFiles.add(destPath);
        }

        @Override
        public synchronized void deployApplication(String name, ComponentDescription cd)
                throws IOException, SmartFrogException {
            bindAppDir(name);
            doDeploy();
        }

        private void doDeploy() throws IOException, SmartFrogException {
            if(appDir==null || appDir.isEmpty()) {
                throw new IllegalArgumentException("appDir is not bound correctly");
            }
            Session session = null;
            try {
                session = demandCreateSession(host);
                SshCommand command = new SshCommand(sfLog(), null);
                
                OutputStreamLog outputStream = new OutputStreamLog(log, outputLogLevel);
                ArrayList<String> commandsList = new ArrayList<String>(1);
                String mkdir = "mkdir -p -m 700 " + appDir;
                commandsList.add(mkdir);
                int exitCode = command.execute(getSession(), commandsList, outputStream, timeout);
                if (exitCode!=0) {
                    throw new SmartFrogException("Error response on command "+ mkdir);
                }
                ScpTo scp = new ScpTo(sfLog());
                scp.doCopy(session, destFiles, sourceFiles);
            } catch (JSchException e) {
                log.error("Failed to upload to " + host + ": " + e, e);
                throw forward(e);
            } finally {
                endSession(session);
            }
        }

        /**
         * End a session
         *
         * @param session session, can be null
         */
        private void endSession(Session session) {
            if (session != null) {
                session.disconnect();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean terminateApplication(String name, boolean normal, String exitText)
                throws IOException, SmartFrogException {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void pingApplication(String name) throws IOException, SmartFrogException {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getServiceDescription() throws IOException, SmartFrogException {
            return "SSH to " + hostname;
        }

        @Override
        public String getApplicationDescription(String name) throws IOException, SmartFrogException {
            return "";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void terminate() throws IOException, SmartFrogException {
        }
    }

}