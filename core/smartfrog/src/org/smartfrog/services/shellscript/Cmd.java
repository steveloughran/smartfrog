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

import java.util.Vector;
import java.io.File;
import org.smartfrog.sfcore.componentdescription.ComponentDescription;
import org.smartfrog.sfcore.componentdescription.ComponentDescriptionImpl;
import org.smartfrog.sfcore.common.SmartFrogException;

// RumCmd class - Data holder for Process parameters

 public class Cmd {

     /** String name for optional attribute. Value {@value}. */
     public final static String ATR_CMD = "cmd";
     /** String name for optional attribute. Value {@value}. */
     public final static String ATR_ENVP = "envProperties";
     /** String name for optional attribute. Value {@value}. */
     public final static String ATR_DIR = "dir";

     /** String name for optional attribute. Value {@value}. */
     public final static String ATR_LINE_SEPARATOR = "lineSeparator";

     /** String name for optional attribute. Value {@value}. */
     public static final String ATR_ECHO_CMD = "echoCmd";
     /** String name for optional attribute. Value {@value}. */
     public static final String ATR_EXIT_ERROR_CMD = "echoErrorCmd";

     /** String name for optional attribute. Value {@value}. */
     public static final String ATR_FILTERS_OUT = "filtersOut";
     /** String name for optional attribute. Value {@value}. */
     public static final String ATR_FILTERS_ERR = "filtersErr";

     // Data needed for runTime Exec.

     private String cmdarray[] = null;
     private String envp[] = null;
     private File dir = null;

     // Stream Filters configuration
     private FilterListener foutL = null;
     private String filtersOut[]=null;
     private FilterListener ferrL = null;
     private String filtersErr[]=null;

     /** String name for line return. */
     private String lineSeparator = System.getProperty("line.separator");

     private String echoCommand ="echo";

     private String exitErrorCommand=null; // Unix= $?, Windows "%ERRORLEVEL%";

     public Cmd() {

     }


     public Cmd(ComponentDescription cd) throws SmartFrogException {
          try {
              this.setCmdArray(cd.sfResolve(ATR_CMD, cmdarray, false));
              this.setEnvp( cd.sfResolve(ATR_ENVP, envp, false));
              this.setEchoCommand( cd.sfResolve(ATR_ECHO_CMD, echoCommand, false));
              this.setFile(cd.sfResolve(ATR_DIR, dir, false));
              this.setExitErrorCommand( cd.sfResolve(ATR_EXIT_ERROR_CMD, exitErrorCommand, false));
              this.filtersOut = cd.sfResolve(ATR_FILTERS_OUT, filtersOut, false);
              this.filtersErr = cd.sfResolve(ATR_FILTERS_ERR, filtersErr, false);
          } catch (Exception ex) {
              throw SmartFrogException.forward("Failed to create CMD", ex);
          }
     }

     public Cmd(String cmdarray[], String envp[], File dir) {
         this.cmdarray = cmdarray;
         this.dir = dir;
         this.envp = envp;
     }

     public Cmd setCmdArray(String[] cmdarray) {
         this.cmdarray = cmdarray;
         return this;
     }

     public Cmd setCmdArray(Vector cmdarray) {
         this.cmdarray = new String[cmdarray.size()];
         cmdarray.copyInto(this.cmdarray);
         return this;
     }

     public Cmd setEnvp(String[] envp) {
         this.envp = envp;
         return this;
     }

     public Cmd setEnvp(Vector envp) {
         this.envp = new String[envp.size()];
         envp.copyInto(this.envp);
         return this;
     }

     public Cmd setFile(File dir) {
         this.dir = dir;
         return this;
     }

     public Cmd setLineSeparator(String lineSeparator){
        this.lineSeparator=lineSeparator;
        return this;
     }

     public Cmd setFilterOutListener(FilterListener foutL, String filtersOut[]) {
         this.foutL = foutL;
         this.filtersOut = filtersOut;
         return this;
     }

     public Cmd setFilterErrListener(FilterListener ferrL, String filtersErr[]) {
         this.ferrL = ferrL;
         this.filtersErr = filtersErr;
         return this;
     }

     public Cmd setEchoCommand(String echoCommand){
        if (echoCommand==null) {
          this.echoCommand = echoCommand;
        } else {
          this.echoCommand = echoCommand.trim();
        }
        return this;
     }

     public Cmd setExitErrorCommand(String exitErrorCommand){
        if (exitErrorCommand==null) {
          this.exitErrorCommand = exitErrorCommand;
        } else {
          this.exitErrorCommand = exitErrorCommand.trim();
        }
        return this;
     }


     public String[] getCmdArray() {
         return cmdarray;
     }

     public String[] getEnvp() {
         return envp;
     }

     public File getFile() {
         return dir;
     }

     public String getLineSeparator() {
         return this.lineSeparator;
     }

     public String[] getFiltersOut() {
              return filtersOut;
     }

     public String[] getFiltersErr() {
              return filtersErr;
     }

     public FilterListener getFilterErrListener() {
              return this.ferrL;
     }

     public FilterListener getFilterOutListener() {
              return this.foutL;
     }

     public String getEchoCommand() {
         return this.echoCommand;
     }

     public String getExitErrorCommand() {
              return this.exitErrorCommand;
     }


     public String toString(){
         StringBuffer str = new StringBuffer();
         str.append("Cmd: ");
         Vector v = new Vector(java.util.Arrays.asList(getCmdArray()));
         str.append(v.toString());
         if (envp !=null){
             str.append(", envp: ");
             str.append(this.getEnvp().toString());
         }
         if (dir !=null){
             str.append(", dir: ");
             str.append(this.getFile().toString());
         }
         if (filtersOut !=null){
             str.append(", filtersOutputStream: ");
             v = new Vector(java.util.Arrays.asList(getFiltersOut()));
             str.append(v.toString());
         }
         if (filtersErr !=null){
             str.append(", filtersErrorStream: ");
             v = new Vector(java.util.Arrays.asList(getFiltersErr()));
             str.append(v.toString());
         }

         if (echoCommand !=null){
             str.append(", echoCommand: ");
             str.append(this.echoCommand);
         }

         if (exitErrorCommand !=null){
             str.append(", exitErrorCommand: ");
             str.append(this.exitErrorCommand);
         }

         return str.toString();
     }

 }
