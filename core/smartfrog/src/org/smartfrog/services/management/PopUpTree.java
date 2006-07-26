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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JMenuItem;

import javax.swing.JPopupMenu;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import java.awt.Frame;

import org.smartfrog.sfcore.prim.TerminationRecord;

import org.smartfrog.sfcore.prim.Prim;

import java.rmi.RemoteException;


import org.smartfrog.sfcore.componentdescription.ComponentDescription;
import org.smartfrog.sfcore.componentdescription.ComponentDescriptionImpl;
import org.smartfrog.services.display.WindowUtilities;


/**
 * Popup Tree UI component.
 */
public class PopUpTree extends JComponent implements ActionListener {
    /** Popup Tree. */
    JPopupMenu popupTree = new JPopupMenu();
    /** Tree component. */
    Component tempComp = null;
    /** x coordinate. */
    int tempX = 0;
    /** y coordinate. */
    int tempY = 0;
    /** Parent panel. */
    DeployTreePanel parent = null;

    /** Item for Tree popup menu - add attribute. */
    JMenuItem menuItemAddAttribute = new JMenuItem();

    //   JMenuItem menuItemModifyAttribute = new JMenuItem();
    //   JMenuItem menuItemRemoveAttribute = new JMenuItem();
    /** Item for Tree popup menu - normal terminate. */
    JMenuItem menuItemTerminateNormal = new JMenuItem();
    /** Item for Tree popup menu - abnormal terminate . */
    JMenuItem menuItemTerminateAbnormal = new JMenuItem();
    /** Item for Tree popup menu - Dterminate. */
    JMenuItem menuItemDTerminate = new JMenuItem();
    /** Item for Tree popup menu - detach. */
    JMenuItem menuItemDetach = new JMenuItem();

    /** Item for Tree popup menu - detach. */
    JMenuItem menuItemDumpContext = new JMenuItem();

    /** Item for Tree popup menu - sfParentageChanged. */
    JMenuItem menuItemParentageChanged = new JMenuItem();
    /**
     *  Constructs PopUpTree object
     */
    public PopUpTree() {
        popupInit();
    }

    /**
     *  Initializes popup tree
     */
    void popupInit() {
        // Tree: options
        menuItemAddAttribute.setText("Add Attribute");

        //      menuItemRemoveAttribute.setText("Remove Attribute");
        //      menuItemModifyAttribute.setText("Modify Attribute");
        menuItemDetach.setText("Detach Component");
        menuItemTerminateNormal.setText("Terminate Component - NORMAL");
        menuItemTerminateAbnormal.setText("Terminate Component - ABNORMAL");
        menuItemDTerminate.setText("Detach and Terminate Comp");
        menuItemDumpContext.setText("Component Diag. Report");
        menuItemParentageChanged.setText("sfParentageChanged()");

        // Tree: options
        //      popupTree.add(menuItemAddAttribute);
        //      popupTree.add(menuItemRemoveAttribute);
        //      popupTree.add(menuItemModifyAttribute);
        popupTree.add(menuItemTerminateNormal);
        popupTree.add(menuItemTerminateAbnormal);
        popupTree.add(menuItemDTerminate);
        popupTree.add(menuItemDetach);

        popupTree.add(menuItemDumpContext);

        popupTree.add(menuItemParentageChanged);

        // Add action listeners for tree popup
        menuItemAddAttribute.addActionListener(this);

        //      menuItemRemoveAttribute.addActionListener(this);
        //      menuItemModifyAttribute.addActionListener(this);
        menuItemTerminateNormal.addActionListener(this);
        menuItemTerminateAbnormal.addActionListener(this);
        menuItemDTerminate.addActionListener(this);
        menuItemDetach.addActionListener(this);

        menuItemDumpContext.addActionListener(this);

        menuItemParentageChanged.addActionListener(this);
    }

    /**
     *  Gets the popupMenu attribute of the PopUpTree object
     *
     *@return    The popupMenu value
     */
    public JPopupMenu getPopupMenu() {
        return popupTree;
    }

    /**
     * Displays the popup tree.
     *
     *@param  comp    Component to be displayed
     *@param  x       x coordinate
     *@param  y       y coordinate
     *@param  parent  parent of this component
     */
    public void show(Component comp, int x, int y, DeployTreePanel parent) {
        tempComp = comp;
        tempX = x;
        tempY = y;
        if ( getNode() instanceof Prim){
            menuItemDetach.setVisible(true);
            menuItemTerminateNormal.setVisible(true);
            menuItemTerminateAbnormal.setVisible(true);
            menuItemDTerminate.setVisible(true);
            menuItemDumpContext.setVisible(true);
            menuItemParentageChanged.setVisible(true);
        }else if  (getNode()instanceof ComponentDescription){
            menuItemDetach.setVisible(false);
            menuItemTerminateNormal.setVisible(false);
            menuItemTerminateAbnormal.setVisible(false);
            menuItemDTerminate.setVisible(false);
            menuItemDumpContext.setVisible(true);
            menuItemParentageChanged.setVisible(true);
        }
        popupTree.show(comp, x, y);
        this.parent = parent;

    }

    /**
     * Interface Method
     *
     *@param  e  Action event
     */
    public void actionPerformed(ActionEvent e) {
        String path;
        Object source = e.getSource();
        TreePath tpath = ((JTree) tempComp).getPathForLocation(tempX, tempY);

        Object node = getNode();

        path = treePath2Path(tpath);

        //System.out.println("Tree PopUp(source): "+e.getSource()+", Path:
        //"+path);
        // Launch it
       if (source == menuItemAddAttribute) {
           addAttrib();
       } else if (source == menuItemTerminateNormal) {
           terminate(node, TerminationRecord.NORMAL , "Console Management Action");
       } else if (source == menuItemTerminateAbnormal) {
           terminate(node, TerminationRecord.ABNORMAL, "Console Management Action");
            // Entry selected in the tree
        } else if (source == menuItemDTerminate) {
            dTerminate(node, TerminationRecord.NORMAL , "Console Management Action");
            // Entry selected in the tree
        } else if (source == menuItemDetach) {
            detach(node);
            // Entry selected in the tree
        } else if (source == menuItemParentageChanged) {
            if (node instanceof Prim){
                try {
                    ((Prim)node).sfParentageChanged();
                } catch (RemoteException ex1) {
                    //ex1.printStackTrace();
                }
            } else if (node instanceof ComponentDescription){
                ((ComponentDescriptionImpl)node).sfParentageChanged();
            }
            // Entry selected in the tree
        } else if (source == menuItemDumpContext) {
            StringBuffer message=new StringBuffer();
            String name = "error";
            if (node instanceof Prim) {
                try {
                    Prim objPrim = ((Prim)node);
                    message.append(objPrim.sfDiagnosticsReport());
                    name = ((Prim)objPrim).sfCompleteName().toString();
                } catch (Exception ex) {
                    message.append("\n Error: "+ex.toString());
                }
            } else {
                try {
                    ComponentDescription objCD = ((ComponentDescription)node);
                    message.append(((ComponentDescriptionImpl)objCD).sfDiagnosticsReport());
                    name = ((ComponentDescription)objCD).sfCompleteName().toString();
                } catch (Exception ex) {
                    message.append("\n Error: "+ex.toString());
                }

            }
            modalDialog("Context info for "+ name ,  message.toString(), "", source);
        }

    }

    /**
     * Get Node
     * @return  Object
     */
    private Object getNode() {
        TreePath tpath = ((JTree) tempComp).getPathForLocation(tempX, tempY);
        Object node = (((DeployEntry) (tpath.getLastPathComponent())).getEntry());
        return node;
    }

    /**
     * Converts tree path to path
     *@param  tpath  Tree path object
     *@return        path
     */
    private String treePath2Path(TreePath tpath) {
        String path = "";
        path = tpath.toString();
        path = path.substring(1, path.length() - 1);
        path = path.replace(',', '.');
        path = removeSpaces(path);

        //System.out.println("TreePath: "+path);
        return path;
    }

    /**
     * Removes spaces from input string.
     *
     *@param  string  Input string
     *@return         string without spaces
     */
    public static String removeSpaces(String string) {
        //Save the search string as a StringBuffer object so
        //we can take advantage of the replace capabilities
        StringBuffer s = new StringBuffer();

        //loop through the original string
        int thisCharacter;

        for (int i = 0; i < string.length(); i++) {
            thisCharacter = (char) string.charAt(i);
            if ((char) string.charAt(i) != ' ') {
                s.append((char) thisCharacter);
            }
        }

        return s.toString();
    }

    /**
     *  Adds a feature to the Attrib attribute of the PopUpTree object
     */
    void addAttrib() {
        System.out.println("ADD ATTRIBUTE! @Todo Complete!!!!!!!!!!1");
    }

    /**
     * Terminates the deploy management
     *
     *@param  obj  SF Component
     * @param type error type
     * @param reason cause
     */
    void terminate(Object obj, String type, String reason) {
        //System.out.println("Terminating: "+obj.toString());
        if (obj instanceof Prim) {
            String name ="";
            try {
                name = ((Prim)obj).sfCompleteName().toString();
                org.smartfrog.services.management.DeployMgnt.terminate((Prim) obj, type, reason);
            } catch (Exception ex){
               WindowUtilities.showError(this,"Problem when trying to Terminate '"+name+"'. \n"+ex.toString());
               ex.printStackTrace();
            }
        }
    }

    /**
     * Detaches and terminates the deploy management
     *
     * @param  obj  SF Component
     * @param type error type
     * @param reason cause
     */
    void dTerminate(Object obj, String type, String reason) {
        //System.out.println("Detatching and Terminating: "+obj.toString());
        String name = "";
        if (obj instanceof Prim) {
            try {
                name = ((Prim)obj).sfCompleteName().toString();
                org.smartfrog.services.management.DeployMgnt.dTerminate((Prim) obj, type, reason);
                parent.refresh();
            } catch (Exception ex){
               WindowUtilities.showError(this,"Problem when trying to Detach and Terminate '"+name+"'. \n"+ex.toString());
               ex.printStackTrace();
            }
        }
    }

    /**
     *  Detaches the component from deploy management
     *
     *@param  obj  SF component
     */
    void detach(Object obj) {
        //System.out.println("Detatching: "+obj.toString());
        if (obj instanceof Prim) {
            String name ="";
            try {
                name = ((Prim)obj).sfCompleteName().toString();
                org.smartfrog.services.management.DeployMgnt.detach((Prim) obj);
                parent.refresh();
            }catch (Exception ex){
              WindowUtilities.showError(this,"Problem when trying to Detach and Terminate '"+name+"'. \n"+ex.toString());
              ex.printStackTrace();
            }
            // Refresh Console.
            // To do: automatic Refresh ;-)
        }
    }


    /**
     * Prepares option dialog box
     *
     *@param  title    title displayed on the dialog box
     *@param  message  message to be displayed
     *@param defaultValue default value
     */
    private void modalDialog(String title, String message,
            String defaultValue, Object source) {
        /**
         *  Scrollpane to hold the display's screen.
         */
        JScrollPane scrollPane = new JScrollPane();
        /**
         *  Display's screen object.
         */
        JTextArea screen = new JTextArea(message);
        Frame parentFrame = new Frame();
        JDialog pane = new JDialog(parentFrame,title,true);
        pane.setSize(600,400);
        pane.setResizable(true);
        pane.getContentPane().add(scrollPane);
        scrollPane.getViewport().add(screen, null);
        pane.show(true);
    }

}
