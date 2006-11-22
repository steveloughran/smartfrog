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
package org.smartfrog.services.management;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javax.swing.*;

import org.smartfrog.sfcore.logging.LogFactory;
import org.smartfrog.sfcore.logging.LogSF;

import org.smartfrog.services.display.Display;
import org.smartfrog.services.display.SFDisplay;
import org.smartfrog.services.display.WindowUtilities;
import org.smartfrog.sfcore.common.Context;
import org.smartfrog.sfcore.common.SmartFrogCoreKeys;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.prim.TerminationRecord;
import org.smartfrog.sfcore.processcompound.SFProcess;
import org.smartfrog.sfcore.processcompound.ProcessCompound;

import org.smartfrog.sfcore.common.ExitCodes;
import org.smartfrog.sfcore.common.OrderedHashtable;
import org.smartfrog.sfcore.componentdescription.ComponentDescription;
import org.smartfrog.sfcore.reference.Reference;


/**
 *  Management Console component. It can be deployed as a normat SmartFrog
 *  component or it can be started as a separate console.
 */
public class SFDeployDisplay extends SFDisplay implements ActionListener {
   protected JButton refresh = new JButton();
   private JPanel panelTree = null;
   private JTree tree = null;
   private JScrollPane scrollPaneTree = null;

   final JCheckBoxMenuItem jCheckBoxMenuItemShowCDasChild = new JCheckBoxMenuItem();
   final JMenuItem jMenuScriptingPanel = new JMenuItem();

   static final String scriptingPanelName = "Scripting:";
   /**
    * Constructs SFDeployDisplay object
    *
    *@throws  RemoteException  If network or RMI error
    */
   public SFDeployDisplay() throws RemoteException {
      super();
   }


   /**
    *  The main program for the SFDeployDisplay class
    *
    *@param  args           The command line arguments
    *@throws  Exception  If any error
    */
   public static void main(String[] args) throws Exception {
      //Init system
      org.smartfrog.SFSystem.initSystem();
      //Logger.log("Starting management window...");
      LogSF sflog = sfLogStatic();
      sflog.out("Starting management window...");

      String nameDisplay = "sfManagementConsole";
      int height = 440;
      int width = 600;
      OptionSet opts = new OptionSet(args);

      if (opts.errorString != null) {
         exitWith(opts.errorString, opts.exitCode);
      }

      final boolean showRootProcess = opts.showRootProcess;
      final boolean showCDasChild = opts.showCDasChild;
      final boolean showScripting = opts.showScripting;
      final String hostname = opts.host;
      final int port = opts.port;
      String positionDisplay = opts.windowPosition;

      try {
         startConsole(nameDisplay, height, width, positionDisplay,
               showRootProcess,showCDasChild, showScripting, hostname, port, true);
      } catch (java.net.UnknownHostException uex) {
         exitWith("Error: Unknown host.", ExitCodes.EXIT_ERROR_CODE_GENERAL);
      } catch (java.rmi.ConnectException cex) {
         exitWith("Error: " + cex.getMessage(), ExitCodes.EXIT_ERROR_CODE_GENERAL);
      } catch (Exception e) {
         sfLogStatic().error("Error in SFDeployDisplay.main():" + e,e);
         exitWith("Error in SFDeployDisplay.main():" + e, ExitCodes.EXIT_ERROR_CODE_GENERAL);
      }

      sflog.out("Running.");
   }


   /**
    * Starts the console
    *
    *@param  nameDisplay       name of the display
    *@param  height            height of the window
    *@param  width             width of the window
    *@param  positionDisplay   position  of display
    *@param  showRootProcess   boolean to enable display of root process
    *@param  showCDasChild   boolean to enable display of CDs as children
    *@param  hostname          host name
    *@param  port              port
    *@param  shouldSystemExit  boolean to indicate exit at close of window
    *@return                   Display object
    *@throws  Exception     In case of any error
    *
    */


   public static Display startConsole(String nameDisplay, int height,
         int width, String positionDisplay, final boolean showRootProcess, final boolean showCDasChild, final boolean showScripting,
         final String hostname, final int port, boolean shouldSystemExit)
          throws Exception {
      final JButton refreshButton = new JButton();
      JMenu jMenuMng = new JMenu();
      final JCheckBoxMenuItem jCheckBoxMenuItemShowRootProcessPanel = new JCheckBoxMenuItem();
      final JCheckBoxMenuItem jCheckBoxMenuItemShowCDasChild = new JCheckBoxMenuItem();
      final JMenuItem jMenuScriptingPanel = new JMenuItem();
      String infoConnection = ("sfManagementConsole connecting to " +
            hostname + ":" + port);
      //Logger.log(infoConnection);
      sfLogStatic().out(infoConnection);
      nameDisplay = nameDisplay + " [" + "sfManagementConsole connected to " +
            hostname + ":" + port + "]";

      if (showRootProcess) {
         sfLogStatic().warn(" showing rootProcess");
         //Logger.log(" showing rootProcess");
      } else {
         //System.out.println("");
      }

      final Display newDisplay;

      if (org.smartfrog.services.display.WindowUtilities.areGraphicsAvailable()) {
         newDisplay = new Display(nameDisplay, null);

         addFrogIcon(newDisplay);

         newDisplay.setShouldSystemExit(shouldSystemExit);
         newDisplay.setVisible(false);
         newDisplay.setSize(width, height);
         newDisplay.setAskSaveChanges(false);
         org.smartfrog.services.display.WindowUtilities.setPositionDisplay(null, newDisplay, positionDisplay);

         // Button for Refresh view ...
         refreshButton.setText("Refresh");
         refreshButton.setActionCommand("refreshButton");
         refreshButton.addActionListener(
            new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                  //System.out.println("ActionEvent SFDEployDisplay: "+ e);
                  if ((e.getActionCommand()).equals("refreshButton")) {
                     //preserve scripting panel
                     JTabbedPane scriptingTab = initScriptingTabbedPane (newDisplay.tabPane,false);
                     try {
                      newDisplay.cleanAddedPanels();
                     } catch (Throwable thr1) {
                        if (LogFactory.getLog("SFManagementConsole").isErrorEnabled()){
                          LogFactory.getLog("SFManagementConsole").error(thr1);
                        }
                     }
                     try {
                        addProcessesPanels(newDisplay, jCheckBoxMenuItemShowRootProcessPanel.isSelected(), //showRootProcess,
                              jCheckBoxMenuItemShowCDasChild.isSelected(),hostname, port);
                         //Add scripting panel
                         if (scriptingTab!=null) newDisplay.tabPane.add(scriptingPanelName,scriptingTab);
                     } catch (Throwable ex) {
                        if (LogFactory.getLog("SFManagementConsole").isErrorEnabled()){
                          LogFactory.getLog("SFManagementConsole").error(ex);
                        }
                        //exitWith("Error in SFDeployDisplay.refresh():" + ex, ExitCodes.EXIT_ERROR_CODE_GENERAL);
                     }
                  }
               }
            });
         newDisplay.mainToolBar.add(refreshButton);
         newDisplay.showToolbar(true);
         // Add deployTreePanel menu items
         jMenuMng.setText("Mng. Console");
         newDisplay.jMenuBarDisplay.add(jMenuMng);
         jCheckBoxMenuItemShowRootProcessPanel.setSelected(showRootProcess);
         jCheckBoxMenuItemShowRootProcessPanel.setText("Show rootProcess");
         jCheckBoxMenuItemShowRootProcessPanel.setAccelerator(javax.swing.KeyStroke.getKeyStroke(77, java.awt.event.KeyEvent.CTRL_MASK | java.awt.event.KeyEvent.ALT_MASK, false));
         jCheckBoxMenuItemShowRootProcessPanel.addActionListener(new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                            refreshButton.doClick();
               }
         });
         jMenuMng.add(jCheckBoxMenuItemShowRootProcessPanel);

         jCheckBoxMenuItemShowCDasChild.setSelected(showCDasChild);
         jCheckBoxMenuItemShowCDasChild.setText("Show show CD as child");
         jCheckBoxMenuItemShowCDasChild.addActionListener(new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                            refreshButton.doClick();
               }
         });
         jMenuMng.add(jCheckBoxMenuItemShowCDasChild);

         jMenuScriptingPanel.setText("Add Scripting ...");
         jMenuScriptingPanel.addActionListener(new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                   try {
                      addScriptingPanel (newDisplay.tabPane, null,null,hostname,port );
                   } catch (Exception e1) {
                       if (sfLogStatic().isErrorEnabled()){ sfLogStatic().error(e1);}
                       WindowUtilities.showError(newDisplay,e1.toString());
                   }
                  //refreshButton.doClick();
               }
         });
         jMenuMng.add(jMenuScriptingPanel);

         newDisplay.setVisible(true);
         addProcessesPanels(newDisplay, showRootProcess, showCDasChild, hostname, port);
         if (showScripting) addScriptingPanel (newDisplay.tabPane, null,null,hostname,port );
         

         return newDisplay;
      }

      return null;
   }

    /**
     * Add Frog Icon
     * @param newDisplay Display Object
     */
   private static void addFrogIcon(Display newDisplay) {
       String imagesPath = SFDeployDisplay.class.getPackage().getName()+".";
       imagesPath = imagesPath.replace('.', '/');
       imagesPath = imagesPath+"frogb.gif";
       newDisplay.setIconImage(Display.createImage(imagesPath));
   }


   /**
    *  Prints given error string and exits system
    *
    *@param  str  string to print on out
    * @param exitCode exit code
    */
   public static void exitWith(String str, int exitCode) {
      if (str != null) {
         System.err.println(str);
      }

      System.exit(exitCode);
   }


   // Used in Main
   /**
    *  Adds a feature to the ProcessesPanels attribute of the SFDeployDisplay
    *  class
    *
    *@param  display              The feature to be added to the ProcessesPanels
    *      attribute
    *@param  addRootProcessPanel  The feature to be added to the ProcessesPanels
    *      attribute
    * @param  showCDasChild   boolean to enable display of CDs as children
    *@param  hostname             The feature to be added to the ProcessesPanels
    *      attribute
    *@param  port                 The feature to be added to the ProcessesPanels
    *      attribute
    *@throws  Exception        If any error
    */
   public static void addProcessesPanels(Display display, boolean addRootProcessPanel,boolean showCDasChild, String hostname, int port)
          throws Exception {
      int indexPanel = 0;

      // Adding pannels
      JPanel deployPanel = null;
      JPanel deployLocalPPanel = null;
      if (addRootProcessPanel) {
         //Add rootProcessPanel
         deployPanel = new DeployTreePanel(SFProcess.getRootLocator().getRootProcessCompound(InetAddress.getByName(hostname), port), false, true,showCDasChild);
         deployPanel.setEnabled(true);
         display.tabPane.add(deployPanel, "rootProcess", indexPanel++);
         //Add Local Process Panel
          if (SFProcess.getProcessCompound()!=null && (!SFProcess.getProcessCompound().sfIsRoot())) {
            String processName = "localProcess";
            try {
                processName = SFProcess.getProcessCompound().sfCompleteName().toString();
            } catch (Exception ex) { sfLogStatic().ignore(ex);}
            deployLocalPPanel = new DeployTreePanel(SFProcess.getProcessCompound(),false, true,showCDasChild);
            deployLocalPPanel.setEnabled(true);
            display.tabPane.add(deployLocalPPanel, processName, indexPanel++);
          }

      }

      Context context = SFProcess.getRootLocator().getRootProcessCompound(InetAddress.getByName(hostname), port).sfContext();
      java.util.Enumeration keys = context.keys();
      Object key = "";
      Object value = "";

      while (keys.hasMoreElements()) {
         key = keys.nextElement();

         //out.println("* " + key + ": " + (context.get((String)key)).toString());
         value = context.get((String) key);

         //System.out.println("* " + key + ": " + value.toString());
         if (value instanceof Prim) {
            if (((Prim) value).sfParent() == null) {
               deployPanel = new DeployTreePanel(value,false, false, showCDasChild);
               deployPanel.setEnabled(true);
               display.tabPane.add(deployPanel, key, indexPanel++);
            }
         }
      }

      display.tabPane.setSelectedIndex(0);
   }

    private static LogSF sfLogStatic() {
        return LogFactory.getLog("sfManagementConsole");
    }

    /**
     * This will create a new panel with the console for the scripting engine and will declare serveral objects in the
     * scripting engine: rootProcess, localProcess, sfObj and the string provided by sfObjName pointing to the sfObj.
     * @param tabPane display tabPane where to add the panel
     * @param sfObjName  Name used to identify the object
     * @param sfObj Object to be linked in the scripting engine
     * @param hostname where to locate the root process compound
     * @param port port where to locate the root process
     * @throws Exception
     */
   public static void addScriptingPanel (JTabbedPane tabPane, String sfObjName, Object sfObj, String hostname, int port) throws Exception {


      JPanel scriptingPanel = new JPanel();

      JTabbedPane scriptingTabPane = null;



      try {
        Class jConsoleClass = Class.forName("bsh.util.JConsole");
        Class intepreterClass = Class.forName("bsh.Interpreter");
        Class jConsoleInterface = Class.forName ("bsh.ConsoleInterface");
        //Gets parameters for constructor if any
        //Object[] jConsoleparameters = [""];

        // get the right constructor method method
        Constructor jConsoleConstructor = jConsoleClass.getConstructor(null);
        Class[] classParameters = {jConsoleInterface};
        Constructor interpreterConstructor = intepreterClass.getConstructor(classParameters);
        // Invoke the constructors
        Object jConsoleObject = jConsoleConstructor.newInstance(null);
        Object[] parameters = {jConsoleObject};
        Object interpreterObject = interpreterConstructor.newInstance(parameters);
        // Invoke method set("prim",this);
        //java.lang.String string, java.lang.Object object
        classParameters = new Class[]{String.class, Object.class};
        Method method = intepreterClass.getMethod("set",classParameters);
        ProcessCompound rootProcess = SFProcess.getRootLocator().getRootProcessCompound(InetAddress.getByName(hostname), port);
        method.invoke(interpreterObject, new Object[] {"bsh.system.shutdownOnExit",new Boolean(false)});  
        method.invoke(interpreterObject, new Object[]{"rootProcess",rootProcess});
        method.invoke(interpreterObject, new Object[]{"localProcess",SFProcess.getProcessCompound()});
        String panelName="";
        if (sfObj!=null) {
           method.invoke(interpreterObject, new Object[]{"sfObj",sfObj});
           method.invoke(interpreterObject, new Object[]{sfObjName,sfObj});
           panelName = sfObjName;
        } else {
            panelName = "rootProcess";
        }

        new Thread((Runnable) interpreterObject).start(); // start a thread to call the run() method
        scriptingTabPane = initScriptingTabbedPane(tabPane,true);
        scriptingTabPane.add((Component)jConsoleObject, panelName);
        scriptingTabPane.setSelectedIndex(0);
      } catch (Throwable thr){
          if (sfLogStatic().isErrorEnabled()){ sfLogStatic().error(thr);}
          WindowUtilities.showError(scriptingTabPane, thr.toString());

      }
   }

    /**
     *
     * @param tabPane Tabbed pane where to add the scripting tabbed pane.
     * @param createIfMissing
     * @return  if not createIfMissing then it returns null, otherwise it will always return a tabbed pane
     */
    private static JTabbedPane initScriptingTabbedPane(JTabbedPane tabPane, boolean createIfMissing) {
        JTabbedPane scriptingTabPane;

        int tabIndex = tabPane.indexOfTab(scriptingPanelName);
        if (tabIndex <= -1){
           if (!(createIfMissing)) return null; 
           scriptingTabPane = new JTabbedPane();
           tabPane.add(scriptingPanelName,scriptingTabPane);
        } else {
            scriptingTabPane = (JTabbedPane) tabPane.getComponentAt(tabIndex);
        }
        return scriptingTabPane;
    }

    /**
    * Deploys display component.
    *
    *@throws  SmartFrogException  If unable to deploy the component
    *@throws  RemoteException     If RMI or network error
    */
   public synchronized void sfDeploy() throws SmartFrogException, RemoteException {
//      try {
         super.sfDeploy();
         boolean isObjCopy = false;
         Object root = null;
         boolean isPC = false;
         String name =  "Deployed System ...";
         root = sfResolve ("root",false);
         if (root==null)  {
           // We add the new Tree component here
           root = this.sfResolveWithParser(SmartFrogCoreKeys.SF_ROOT);
         }

         if (root instanceof ComponentDescription) {
            try {
                name = ((ComponentDescription)root).sfCompleteName().toString();
            } catch (Exception ex){ sfLog().ignore(ex);}

            Object value2 = sfResolve("root",false);
            if (root != value2){
                isObjCopy = true;
            }

         } else if (root instanceof Prim) {
             try {
                name = sfResolve(SmartFrogCoreKeys.SF_PROCESS, sfProcessName, false);
            } catch (Exception ex){ sfLog().ignore(ex);}
            if (root instanceof ProcessCompound){
                isPC =true;
            }
         }


         //root= new CompoundImpl();
         //System.out.println("Root: "+root.toString());
         this.panelTree = new DeployTreePanel(root,isObjCopy, isPC,true);
         this.panelTree.setEnabled(true);
         addFrogIcon(display);
         display.tabPane.add(panelTree, name, 0);

         // Button for Refresh view ...
         refresh.setText("Refresh");
         refresh.setActionCommand("refreshButton");
         refresh.addActionListener(this);
         display.mainToolBar.add(this.refresh);
         JMenu jMenuMng = new JMenu();

         jMenuMng.setText("Mng. Console");
         display.jMenuBarDisplay.add(jMenuMng);
         jCheckBoxMenuItemShowCDasChild.setSelected(true);
         jCheckBoxMenuItemShowCDasChild.setText("Show show CD as child");
         jCheckBoxMenuItemShowCDasChild.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refresh();
            }
          });
         jMenuMng.add(jCheckBoxMenuItemShowCDasChild);

         jMenuScriptingPanel.setText("Add Scripting ...");
         jMenuScriptingPanel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    addScriptingPanel (display.tabPane, "sfManagementConsole", this,"localhost",3800 );
                } catch (Exception e1) {
                    if (sfLogStatic().isErrorEnabled()){ sfLogStatic().error(e1); }
                    WindowUtilities.showError(display,e1.toString());
                }
                refresh();
            }
          });
         jMenuMng.add(jMenuScriptingPanel);
         display.showToolbar(true);
      //end panelTree example
   }


   /**
    * Starts the display component.
    *
    *@throws  SmartFrogException  If unable to start the component
    *@throws  RemoteException     If RMI or network error
    */
   public synchronized void sfStart() throws SmartFrogException, RemoteException {
      super.sfStart();
   }


   /**
    * Terminates the SFDeployDisplay component
    *
    *@param  t  The reason why it was terminated
    */
   public synchronized void sfTerminateWith(TerminationRecord t) {
      this.display.dispose();
      super.sfTerminateWith(t);
   }


   /**
    *  Main processing method for the SFDeployDisplay object
    */
   public void run() { }


   /**
    * Method of interface PrintMsgInt
    *
    *@param  msg  message to print
    */
   public void printMsg(String msg) {
      super.printMsg(msg);

      // We print in the output
      ((DeployTreePanel) panelTree).add(msg);
   }


   /**
    *  Refreshes the display panel.
    */
   public void refresh() {
      try {
         boolean isObjCopy = false;
         Object root = null;
         root = sfResolve ("root",false);
         if (root==null)  {
           // We add the new Tree component here
           root = this.sfResolveWithParser(SmartFrogCoreKeys.SF_ROOT);
         }

         if (root instanceof ComponentDescription) {
            Object value2 = sfResolve("root",false);
            if (root!=value2){
                isObjCopy = true;
            }
         }
         //System.out.println("Refreshing info");
         ((DeployTreePanel) panelTree).setModel(root,isObjCopy);
         ((DeployTreePanel) panelTree).showCDasChild(jCheckBoxMenuItemShowCDasChild.isSelected());
         ((DeployTreePanel) panelTree).refresh();
      } catch (Throwable ex) {
         if (sfLogStatic().isIgnoreEnabled()){
           sfLogStatic().ignore("Failure refresh() SFDeployDisplay!",ex);
         }
      }
   }


   /**
    * Interface Method.
    *
    *@param  e  action event
    */
   public void actionPerformed(ActionEvent e) {
      //Logger.log("ActionEvent SFDEployDisplay: "+ e);
      if (sfLogStatic().isTraceEnabled()){
        sfLogStatic().trace("ActionEvent SFDEployDisplay: "+ e);
      }
      if ((e.getActionCommand()).equals("refreshButton")) {
         refresh(e);
      }

   }


   /**
    * Interface Method.
    *
    *@param  e  acrtion event
    */
   private void refresh(ActionEvent e) {
      this.refresh();
   }

}
