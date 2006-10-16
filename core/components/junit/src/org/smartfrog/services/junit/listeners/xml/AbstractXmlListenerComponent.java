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

package org.smartfrog.services.junit.listeners.xml;

import org.smartfrog.services.filesystem.FileSystem;
import org.smartfrog.services.junit.TestListener;
import org.smartfrog.services.junit.TestSuite;
import org.smartfrog.services.junit.listeners.html.OneHostXMLListener;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.common.SmartFrogInitException;
import org.smartfrog.sfcore.common.SmartFrogResolutionException;
import org.smartfrog.sfcore.logging.Log;
import org.smartfrog.sfcore.prim.PrimImpl;
import org.smartfrog.sfcore.utils.ComponentHelper;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;

/**
 */
public abstract class AbstractXmlListenerComponent extends PrimImpl
        implements XmlListenerFactory {
    protected Log log;
    protected ComponentHelper helper = new ComponentHelper(this);
    protected String outputDir;
    protected String preamble;
    protected boolean useHostname;
    /**
     * mapping of suite to file
     */
    private HashMap<String, String> testFiles = new HashMap<String, String>();
    protected String suffix = ".xml";
    private boolean useProcessname;

    protected AbstractXmlListenerComponent() throws RemoteException {
    }

    /**
     * add a mapping of suite to file
     *
     * @param suitename
     * @param xmlFilename
     */
    protected synchronized void addMapping(String hostname,String suitename, String xmlFilename) {
        if (getMapping(suitename) != null) {
            log.warn("A suite called " +
                    suitename
                    + " exists; its output will be overwritten");
        }
        testFiles.put(suitename, xmlFilename);
    }

    /**
     * thread-safe accessor to the suite-file mapping
     *
     * @param suitename suite to lookup
     * @return absolute path of the output file, or null for no mapping.
     */
    private synchronized String getMapping(String suitename) {
        return testFiles.get(suitename);
    }

    /**
     * map from a test suite name to a filename
     *
     * @param hostname
     * @param suitename test suite
     * @return name of output file, or null for no match
     * @throws java.rmi.RemoteException
     */
    public String lookupFilename(String hostname,
                                 String suitename) throws RemoteException {
        return getMapping(suitename);
    }

    /**
     * work out the output dir
     *
     * @return the dir that output is in
     * @throws org.smartfrog.sfcore.common.SmartFrogResolutionException if it is not specified
     * @throws java.rmi.RemoteException
     */
    protected String lookupOutputDir() throws SmartFrogResolutionException,
            RemoteException {
        String out = FileSystem.lookupAbsolutePath(this,
                XmlListenerFactory.ATTR_OUTPUT_DIRECTORY,
                null,
                null,
                true,
                null);
        return out;
    }

    /**
     * Called after instantiation for deployment purposed. Heart monitor is
     * started and if there is a parent the deployed component is added to the
     * heartbeat. Subclasses can override to provide additional deployment
     * behavior.
     *
     * @throws org.smartfrog.sfcore.common.SmartFrogException
     *                                  error while deploying
     * @throws java.rmi.RemoteException In case of network/rmi error
     */
    public synchronized void sfDeploy() throws SmartFrogException,
            RemoteException {
        super.sfDeploy();
        log = helper.getLogger();
    }

    /**
     * Can be called to start components. Subclasses should override to provide
     * functionality Do not block in this call, but spawn off any main loops!
     *
     * @throws org.smartfrog.sfcore.common.SmartFrogException
     *                                  failure while starting
     * @throws java.rmi.RemoteException In case of network/rmi error
     */
    public synchronized void sfStart() throws SmartFrogException,
            RemoteException {
        super.sfStart();
        outputDir = lookupOutputDir();
        File destDir = new File(outputDir);
        destDir.mkdirs();
        if (!destDir.exists()) {
            throw new SmartFrogInitException(
                    "Unable to create destination directory " + destDir);
        }
        preamble = sfResolve(XmlListenerFactory.ATTR_PREAMBLE, (String) null, false);
        useHostname = sfResolve(XmlListenerFactory.ATTR_USE_HOSTNAME, true, true);
        useProcessname = sfResolve(XmlListenerFactory.ATTR_USE_PROCESSNAME, true, true);
        suffix = sfResolve(XmlListenerFactory.ATTR_SUFFIX, suffix, false);
        log.info("output dir is " + outputDir
                + "; hostname=" + useHostname
                +" ; useProcessname="
                +useProcessname);
        log.info("preamble is " + preamble != null ? preamble : "(undefined)");
    }

    /**
     * bind to a caller
     *
     * @param suite
     * @param hostname  name of host
     * @param processname
     * @param suitename name of test suite
     * @param timestamp start timestamp (UTC)
     * @return a session ID to be used in test responses
     */
    public TestListener listen(TestSuite suite, 
                               String hostname,
                               String processname,
                               String suitename,
                               long timestamp) throws RemoteException,
            SmartFrogException {
        if (suitename == null && "".equals(suitename)) {
            throw new SmartFrogException(
                    "Test suite must be named for XML exporting");
        }

        File destDir = new File(outputDir);
        if (useHostname) {
            destDir = new File(destDir, hostname);
        }
        if (useProcessname) {
            destDir = new File(destDir, processname);
        }

        String outputFile = suitename + suffix;
        File destFile = new File(destDir, outputFile);
        log.info("Recording tests to " + destFile);
        String destpath = destFile.getAbsolutePath();
        addMapping(hostname, suitename, destpath);
        if (suite != null) {
            //set the absolute path of the file
            log.debug(
                    "Setting " +
                    XmlListener.ATTR_FILE +
                    "attribute on test suite");
            suite.sfReplaceAttribute(XmlListener.ATTR_FILE,
                    destpath);
        }

        try {
            Date start = new Date(timestamp);
            OneHostXMLListener xmlLog;
            xmlLog = createNewSingleHostListener(hostname, 
                destFile, 
                processname, 
                suitename, 
                start);
            xmlLog.open();
            return xmlLog;
        } catch (IOException e) {
            throw SmartFrogException.forward("Failed to open ", e);
        }
    }

    /**
     * Create the listener
     * @param hostname
     * @param destFile
     * @param processname
     * @param suitename
     * @param start
     * @return
     * @throws IOException
     */
    protected abstract OneHostXMLListener createNewSingleHostListener(String hostname,
                                                                      File destFile,
                                                                      String processname,
                                                                      String suitename,
                                                                      Date start) throws
            IOException;
}
