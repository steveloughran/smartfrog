/** (C) Copyright 2007 Hewlett-Packard Development Company, LP

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
package org.smartfrog.avalanche.client.sf.rpm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.smartfrog.sfcore.logging.LogSF;
import org.smartfrog.services.filesystem.FileSystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;

/**
 * @author sandya
 * 
 * Contains RPM utilities to Install, Un-install and Upgrade RPM packages
 */
public class RPMUtils {
	private LogSF log;
	private Runtime rt = null;

	/**
	 * Constructor which initialises a Runtime object
     * @param log the log to use for messages
	 */
	public RPMUtils(LogSF log) {
        this.log=log;
        rt = Runtime.getRuntime();
	}

	/**
	 * Installs the RPM package without any install-options
	 * @param rpmPackage - full path of RPM Package to be installed
	 * @return boolean - returns true if Package is installed successfully
	 * 						else return false
	 * @throws IOException if an i/o error occurs 
	 */
	public boolean InstallPackage(String rpmPackage) throws IOException {
		String installOptions = "";
		return (InstallPackage(rpmPackage, installOptions));
	}
	
	/**
	 * Installs the RPM package in the install path provided.
	 * Note: Only relocatable RPM packages can use this method.
	 * @param rpmPackage - full path of RPM Package to be installed
	 * @param installPath - location where the package need to be installed
	 * @return boolean - returns true if Package is installed successfully
	 * 						else return false
	 * @throws IOException if an i/o error occurs 
	 */
	public boolean InstallPackage(String rpmPackage, File installPath)
			throws IOException {
		String installOptions = null;
		
		if (installPath.exists() && installPath.isDirectory()
				&& installPath.canWrite()) {
			String path = installPath.getCanonicalPath();
			installOptions = "--prefix " + path;
		}
		
		return InstallPackage(rpmPackage, installOptions);				
	}
	
	/**
	 * Installs RPM Package with the given install-options.
	 * @param rpmPackage - RPM package to be installed
	 * @param installOptions - RPM install options
	 * @return boolean - returns true if Package is installed successfully
	 * 						else return false
	 * @throws IOException if i/o error occurs 
	 */
	public boolean InstallPackage(String rpmPackage, String installOptions)
			throws IOException {
		String line;

        File rpmFile = new File(rpmPackage);
        if (!rpmFile.exists()) {
            log.error("The file " + rpmPackage + " does not exist.");
            return false;
        }
        if (!rpmFile.isFile()) {
            log.error(rpmPackage + " is not a file");
            return false;
        }

        /*
		// Get the file extension to check if it is .rpm file
		String fileName = rpmFile.getName();
        String extn = null;
        int whereExtn = fileName.lastIndexOf('.');
        if ( 0 < whereExtn && whereExtn <= fileName.length()-2)
                extn = fileName.substring(whereExtn+1);

        if (!extn.equals("rpm")) {
        	log.error("The file " + rpmPackage + " is not RPM file.");
        	return false;
        }a
        */

        String command = "rpm -i ";
        if (installOptions != null) {
            command = "rpm -i " + installOptions;
        }

        String cmd = command + " " + rpmPackage;
        boolean error = executeRpmCommand(cmd);
        if (!error) {
            log.info("The RPM package " + rpmPackage + " is successfully installed.");
        }
        return !error;

	}

    private boolean executeRpmCommand(String cmd) throws IOException {
        String line;
        boolean error = false;
        Process p = null;
        p = rt.exec(cmd);
        BufferedReader cmdError = null;
        try {
            cmdError = new BufferedReader(new InputStreamReader(p
                    .getErrorStream()));
            while ((line = cmdError.readLine()) != null) {
                error = true;
                log.error(line);
            }
        } finally {
            FileSystem.close(cmdError);
        }
        BufferedReader cmdOutput = null;
        if (!error) {
            try {
                cmdOutput = new BufferedReader(new InputStreamReader(p
                        .getInputStream()));
                while ((line = cmdOutput.readLine()) != null) {
                    error = true;
                    log.error(line);
                }
            } finally {
                FileSystem.close(cmdOutput);
            }
        }
        return error;
    }

    /*
      * Un-installs the RPM package with out any erase-options
      * @param packageName - RPM package name to be un-installed
      * @return boolean - returns true if package is un-installed
      * 						successfully else returns false
      * @throws IOException if i/o error occurs
      */
	public boolean UninstallPackage(String packageName) throws IOException {
		String eraseOptions = "";
		
		return UninstallPackage(packageName, eraseOptions);
	}
	
	/*
	 * Un-installs the RPM Package with erase-options
	 * @param packageName - RPM package name to be un-installed
	 * @param eraseOptions - erase options to be used while un-installing
	 * @return boolean - returns true if package is un-installed 
	 * 						successfully else returns false
	 * @throws IOException if i/o error occurs
	 */
	public boolean UninstallPackage(String packageName, String eraseOptions)
			throws IOException {
		Process p = null;
		String line;

		String command = null;
		if (eraseOptions.length() != 0)
			command = "rpm -e " + eraseOptions + packageName;
		else
			command = "rpm -e " + packageName;

        boolean result = executeRpmCommand(command);
        if(result) {
		    log.info("The RPM package " + packageName + " is successfully un-installed");
        }
        return result;
	}
	
	/*
	 * Upgrades the RPM package with out any upgrade-options. If a 
	 * previous version of the package is not found, it installs the 
	 * given package otherwise it removes all the older versions of 
	 * the package before upgrading.
	 * @param rpmPackage - Upgrades using the RPM package
	 * @return boolean - returns true if package is successfully upgraded
	 * 						else returns false
	 * @throws IOException if i/o exception occurs 
	 */
	public boolean UpgradePackage(String rpmPackage) throws IOException {
		String upgradeOptions = "";
		
		return UpgradePackage(rpmPackage, upgradeOptions);
	}
	
	/* Upgrades the RPM package with the given upgrade-options. If a 
	 * previous version of the package is not found, it installs the 
	 * given package otherwise it removes all the older versions of 
	 * the package before upgrading.
	 * @param rpmPackage - Upgrades using the RPM package
	 * @return boolean - returns true if package is successfully upgraded
	 * 						else returns false
	 * @throws IOException if i/o exception occurs
	 */
	public boolean UpgradePackage(String rpmPackage, String upgradeOptions)
			throws IOException {
		Process p = null;
		String line;
		boolean error = false;
				
		File rpmFile = new File(rpmPackage);
		if (!rpmFile.exists()) {
            throw new FileNotFoundException("No such file "+rpmFile);
		}

        if (!rpmFile.isFile()) {
            throw new FileNotFoundException("Not an RPM file " + rpmFile);
		}
		
		/*
		// Get the file extension to check if it is .rpm file
		String fileName = rpmFile.getName();
        String extn = null;
        int whereExtn = fileName.lastIndexOf('.');
        if ( 0 < whereExtn && whereExtn <= fileName.length()-2)
                extn = fileName.substring(whereExtn+1);
        
        if (!extn.equals("rpm")) {
        	log.error("The file " + rpmPackage + " is not RPM file.");
        	return false;
        }
        */
        
        String command = "rpm -U ";
		if (upgradeOptions.length() != 0) 
			command = "rpm -U " + upgradeOptions;
		
		String cmd = command + rpmPackage;		
		error=executeRpmCommand(cmd);

		if (!error) {
			log.info("The RPM package " + rpmPackage + " is successfully upgraded");
			return true;
		}
		else
			return false;				
	}
		
}
