/** (C) Copyright 1998-20017 Hewlett-Packard Development Company, LP

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 This library is free software; you can redistribute it and/or
 modify it under the terms of the Apache License, Version 2.0
 License as published by the Apache Software Foundation at
 http://www.apache.org/licenses/.

 For more information: www.smartfrog.org

 */

package org.smartfrog;

import org.smartfrog.sfcore.componentdescription.ComponentDescription;
import org.smartfrog.sfcore.componentdescription.ComponentDescriptionImpl;
import org.smartfrog.sfcore.logging.LogSF;
import org.smartfrog.sfcore.logging.LogFactory;


/**
 * Version class provides version and copyright strings for SmartFrog System.
 */
public class Version {

    /** SmartFrog attribute name. Value = {@value} */
    final static String ATR_NAME = "name";
    /** SmartFrog attribute name. Value = {@value} */
    final static String ATR_MAJOR_RELEASE = "majorRelease";
    /** SmartFrog attribute name. Value = {@value} */
    final static String ATR_MINOR_RELEASE = "minorRelease";
    /** SmartFrog attribute name. Value = {@value} */
    final static String ATR_BUILD = "build";
    /** SmartFrog attribute name. Value = {@value} */
    final static String ATR_STATUS = "status";
    /** SmartFrog attribute name. Value = {@value} */
    final static String ATR_LICENSE = "license";
    /** SmartFrog attribute name. Value = {@value} */
    final static String COPYRIGHT = "copyright";
    /** SmartFrog attribute name. Value = {@value} */
    final static String ATR_BUILD_DATE = "buildDate";

    /** SmartFrog attribute name. Value = {@value}
     * Used to determine the minimun core version compatible with this instance*/
    final static String ATR_MIN_CORE_VERSION = "majorRelease";
    /** SmartFrog attribute name. Value = {@value}
     * Used to determine the maximum core version compatible with this instance*/
    final static String ATR_MAX_CORE_VERSION = "majorRelease";

    /** SmartFrog attribute name. Value = {@value} */
    final static String ATR_BUILD_OSVERSION = "buildOSVersion";
    /** SmartFrog attribute name. Value = {@value} */
    final static String ATR_BUILD_OSNAME = "buildOSName";
    /** SmartFrog attribute name. Value = {@value} */
    final static String ATR_BUILD_OSARCH = "buildOSArch";
    /** SmartFrog attribute name. Value = {@value} */
    final static String ATR_BUILD_JAVAVENDOR = "buildJavaVendor";
    /** SmartFrog attribute name. Value = {@value} */
    final static String ATR_BUILD_JAVAVERSION = "buildJavaVersion";

    // Dont' change this. MODIFY version.sf in same package!!!!!!!!!!!!!!!!!!!
    private static String name=        "SmartFrog";
    private static String majorRelease="3";
    private static String minorRelease="4";
    private static String build=       "17"; // odd numbers are development versions
    private static String status=      ""; //alpha, beta, stable
    private static String license=      "Apache v2.0";

    private static String minCoreVersion = null;

    private static String maxCoreVersion = null;

    private static String buildDate= "buildDate";

    private static String buildOSName ="@buildOSName@";
    private static String buildOSVersion = "@buildOSVersion@";
    private static String buildOSArch = "@buildOSArch@";
    private static String buildJavaVersion = "@buildJavaVersion@";
    private static String buildJavaVendor = "@buildJavaVendor@";

    private static String versionString = name+" "+majorRelease+"."+minorRelease+"."+build+ status+" ("+buildDate+")";

    private static String versionStringForRelease = name+" "+majorRelease+"."+minorRelease+"."+build + status+" ("+buildDate+")";


    // Don't change this. MODIFY version.sf in same package!!!!!!!!!!!!!!!!!!!
    /** The copyright String for the SmartFrog system. */
    private static String copyright = "(C) Copyright 1998-2008 HP Development Company, LP";

    private static boolean initialized=false;

    private Version(){
        init();
    }

    private synchronized void  init() {
        if (initialized) return;
        try {
            //Check Class and read configuration...NOT including system.properties
            ComponentDescription classComponentDescription = ComponentDescriptionImpl.getClassComponentDescription(this, false, null);

            name = classComponentDescription.sfResolve(ATR_NAME, name , false);
            majorRelease = classComponentDescription.sfResolve(ATR_MAJOR_RELEASE, majorRelease , false);
            minorRelease = classComponentDescription.sfResolve(ATR_MINOR_RELEASE, minorRelease , false);
            build = classComponentDescription.sfResolve(ATR_BUILD, build , false);
            status = classComponentDescription.sfResolve(ATR_STATUS, status , false);
            license = classComponentDescription.sfResolve(ATR_LICENSE, license , false);
            copyright = classComponentDescription.sfResolve(COPYRIGHT, copyright , false);
            minCoreVersion = classComponentDescription.sfResolve(ATR_MIN_CORE_VERSION, minCoreVersion , false);
            maxCoreVersion = classComponentDescription.sfResolve(ATR_MAX_CORE_VERSION, maxCoreVersion , false);
            buildDate = classComponentDescription.sfResolve(ATR_BUILD_DATE, buildDate , false);
            buildOSName = classComponentDescription.sfResolve(ATR_BUILD_OSNAME, buildOSName , false);
            buildOSVersion = classComponentDescription.sfResolve(ATR_BUILD_OSVERSION, buildOSVersion , false);
            buildOSArch = classComponentDescription.sfResolve(ATR_BUILD_OSARCH, buildOSArch , false);
            buildJavaVersion = classComponentDescription.sfResolve(ATR_BUILD_JAVAVERSION, buildOSArch , false);
            buildJavaVendor = classComponentDescription.sfResolve(ATR_BUILD_JAVAVENDOR, buildJavaVendor , false);

            //new created
            String newStatus=null;
            if (!status.trim().equals("")){
                newStatus=""+status;
            } else newStatus="";
            versionString = name+" "+majorRelease+"."+minorRelease+"."+build+newStatus+" ("+buildDate+")";
            versionStringForRelease =  majorRelease+"."+minorRelease+"."+build+newStatus;

            initialized=true;

        } catch (Exception ex) {
            if (sfGetProcessLog().isWarnEnabled())
                sfGetProcessLog().warn("Error during init of "+ this.getClass().toString()+"", ex);
        }
    }

    /**
     *
     * @return String Complete Version String
     */
    public static String versionString(){
        //init();
        if (!initialized) new Version();

        return versionString;
    }

    /**
     *
     * @return String Complete Version String
     */
    public static String versionStringforrelease(){
        //init();
        if (!initialized) new Version();

        return versionStringForRelease;
    }

    /**
     * Major release number.
     */
    public static String majorRelease(){
        if (!initialized) new Version();
        return majorRelease;
    }

    /**
     *Minor release number.
     */
    public static String minorRelease(){
        if (!initialized) new Version();
        return minorRelease;
    }
    /**
     * Build number.
     */
    public static String build(){
        if (!initialized) new Version();
        return build;
    }

    /**
     * Build number.
     */
    public static String buildDate(){
        if (!initialized) new Version();
        return buildDate;
    }

    /**
     * Status [alpha, beta, (Empty when statable)].
     */
    public static String status(){
        if (!initialized) new Version();
        return status;
    }

    public static String copyright(){
        //init();
        if (!initialized) new Version();
        return copyright;
    }

    /**
     * Min Core compatible version
     * If null, it is considred compatible with all
     */
    public static String minCoreVersion(){
        if (!initialized) new Version();
        return minCoreVersion;
    }

    /**
     * Max Core compatible version
     * If null, it is considered compatible with all version numbers bigger
     * than ours
     */
    public static String maxCoreVersion(){
        if (!initialized) new Version();
        return maxCoreVersion;
    }


    /**
     * Checks is version provides is compatible with this version.
     * @param version String Version has to be of the form: MajorRelease.MinorRelease.Build_status.
     * @return boolean
     */
    public static boolean compatible(String version){
        if (!initialized) new Version();
        String majorRelease = version.substring(0,version.indexOf('.'));
        String cutVersion = version.substring(majorRelease.indexOf('.'));
        String minorRelease = cutVersion.substring(0,cutVersion.indexOf('.'));
        cutVersion = version.substring(majorRelease.indexOf('.'));
        String build = cutVersion.substring(0,cutVersion.indexOf('_'));

        boolean compatible = true;
        compatible = checkMaxVersion(majorRelease, minorRelease, build);
        compatible = checkMinVersion(majorRelease, minorRelease, build);
        return compatible;
    }


    //@todo review this matching method.
    private static boolean checkMaxVersion (String majorReleaseN, String minorReleaseN, String buildN){
        if (majorRelease==null) return true;
        if (!(Integer.parseInt(majorRelease) < (Integer.parseInt(majorReleaseN)))){return false;}
        if (!(Integer.parseInt(majorRelease) == (Integer.parseInt(majorReleaseN)))){
            if (!(Integer.parseInt(minorRelease) < (Integer.parseInt(minorReleaseN)))){return false;}
            if (!(Integer.parseInt(minorRelease) == (Integer.parseInt(minorReleaseN)))){
                if (!(Integer.parseInt(build) < (Integer.parseInt(buildN)))){return false;}
                if (!(Integer.parseInt(build) == (Integer.parseInt(buildN)))){
                    return true; // All numbers are equal :-)
                }
            }
        }

        return true;
    }


    //@todo review this matching method.
    private static boolean checkMinVersion (String majorReleaseN, String minorReleaseN, String buildN){
        if (majorRelease==null) return true;
        if (!(Integer.parseInt(majorRelease) > (Integer.parseInt(majorReleaseN)))){return false;}
        if (!(Integer.parseInt(majorRelease) == (Integer.parseInt(majorReleaseN)))){
            if (!(Integer.parseInt(minorRelease) > (Integer.parseInt(minorReleaseN)))){return false;}
            if (!(Integer.parseInt(minorRelease) == (Integer.parseInt(minorReleaseN)))){
                if (!(Integer.parseInt(build) > (Integer.parseInt(buildN)))){return false;}
                if (!(Integer.parseInt(build) == (Integer.parseInt(buildN)))){
                    return true; // All numbers are equal :-)
                }
            }
        }
        return true;
    }


    /**
     *  To get the sfCore logger
     * @return Logger implementing LogSF and Log
     */
    private LogSF sfGetProcessLog() {
       LogSF sflog  =  LogFactory.sfGetProcessLog();
       return sflog;
    }

    /**
     * Method invoked to print the version info.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {
    if ((args.length > 0) && (args[0].equals("-b")))
        System.out.print(versionStringforrelease());
    else
        System.out.print(versionString());
    }
}
