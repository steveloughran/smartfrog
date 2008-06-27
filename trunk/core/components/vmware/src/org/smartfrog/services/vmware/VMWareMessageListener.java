/** (C) Copyright 2007 Hewlett-Packard Development Company, LP

 Disclaimer of Warranty

 The Software is provided "AS IS," without a warranty of any kind. ALL
 EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 PARTICULAR PURPOSE, OR NON-INFRINGEMENT, ARE HEREBY
 EXCLUDED. SmartFrog is not a Hewlett-Packard Product. The Software has
 not undergone complete testing and may contain errors and defects. It
 may not function properly and is subject to change or withdrawal at
 any time. The user must assume the entire risk of using the
 Software. No support or maintenance is provided with the Software by
 Hewlett-Packard. Do not install the Software if you are not accustomed
 to using experimental software.

 Limitation of Liability

 TO THE EXTENT NOT PROHIBITED BY LAW, IN NO EVENT WILL HEWLETT-PACKARD
 OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR
 FOR SPECIAL, INDIRECT, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES,
 HOWEVER CAUSED REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF
 OR RELATED TO THE FURNISHING, PERFORMANCE, OR USE OF THE SOFTWARE, OR
 THE INABILITY TO USE THE SOFTWARE, EVEN IF HEWLETT-PACKARD HAS BEEN
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGES. FURTHERMORE, SINCE THE
 SOFTWARE IS PROVIDED WITHOUT CHARGE, YOU AGREE THAT THERE HAS BEEN NO
 BARGAIN MADE FOR ANY ASSUMPTIONS OF LIABILITY OR DAMAGES BY
 HEWLETT-PACKARD FOR ANY REASON WHATSOEVER, RELATING TO THE SOFTWARE OR
 ITS MEDIA, AND YOU HEREBY WAIVE ANY CLAIM IN THIS REGARD.

 */

package org.smartfrog.services.vmware;

import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.smartfrog.services.xmpp.LocalXmppPacketHandler;
import org.smartfrog.services.xmpp.MonitoringConstants;
import org.smartfrog.services.xmpp.XMPPEventExtension;
import org.smartfrog.services.xmpp.XmppListener;
import org.smartfrog.services.xmpp.XmppListenerImpl;
import org.smartfrog.sfcore.common.SmartFrogDeploymentException;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.prim.Prim;
import org.smartfrog.sfcore.prim.PrimImpl;
import org.smartfrog.sfcore.prim.TerminationRecord;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Calendar;

/**
 * Component that listens for VMWARE messages
 */
public class VMWareMessageListener extends PrimImpl implements LocalXmppPacketHandler, Prim {

    /**
     * Reference to the VMWare Server manager module.
     */
    private VMWareServerManager manager;

    /**
     * Reference to the XMPP listener.
     */
    private XmppListenerImpl refXmppListener;
    public static final String ATTR_LISTENER = "listener";
    public static final String VMRESPONSE = "vmresponse";
    public static final String VMNAME = "vmname";
    public static final String VMCMD = "vmcmd";

    /**
     * Constructor.
     * @throws RemoteException In case of network/rmi error
     */
    public VMWareMessageListener() throws RemoteException {

    }

    /**
     * Called after instantiation for deployment purposes. Heart monitor is
     * started and if there is a parent the deployed component is added to the
     * heartbeat. Subclasses can override to provide additional deployment
     * behavior.
     * Attributees that require injection are handled during sfDeploy().
     *
     * @throws SmartFrogException error while deploying
     * @throws RemoteException In case of network/rmi error
     */
    public synchronized void sfDeploy() throws SmartFrogException, RemoteException {
        super.sfDeploy();

        sfLog().info("VMWare Message Listener deployed.");
    }

    /**
     * Can be called to start components. Subclasses should override to provide
     * functionality Do not block in this call, but spawn off any main loops!
     *
     * @throws SmartFrogException failure while starting
     * @throws RemoteException In case of network/rmi error
     */
     public synchronized void sfStart() throws SmartFrogException, RemoteException {
        super.sfStart();

        // get the reference to the vmware server manager
        manager = (VMWareServerManager)sfResolve("vmServerManager", manager, true);

        // get the reference to the xmpp message listener
        XmppListener xmppListener = (XmppListener) sfResolve(ATTR_LISTENER, refXmppListener, true);
        if(!(xmppListener instanceof XmppListenerImpl)) {
            throw new SmartFrogDeploymentException("The XmppListener referenced by "+ATTR_LISTENER+" must be in the same process");
        }
        refXmppListener = (XmppListenerImpl) xmppListener;

        // register this listener to the xmpp message listener
        refXmppListener.registerPacketHandler(this);

        sfLog().info("VMWare Message Listener started.");
    }

    /**
     * Provides hook for subclasses to implement useful termination behavior.
     * Deregisters component from local process compound (if ever registered)
     *
     * @param status termination status
     */
    protected synchronized void sfTerminateWith(TerminationRecord status) {
        super.sfTerminateWith(status);

        sfLog().info("VMWareMessageListener going down.");

        // unregister this listener from the xmpp message listener
        refXmppListener.unregisterPacketHandler(this);
    }

    public void sfTerminate(TerminationRecord status) {
        super.sfTerminate(status);

        sfLog().info("VMWareMessageListener going down.");

        // unregister this listener from the xmpp message listener
        refXmppListener.unregisterPacketHandler(this);
    }

    /**
     * get a message filter object
     * @return a filter that only accepts messages in the vmware namespace
     */
    public PacketFilter getFilter() {
        return new VMWareMessageFilter();
    }

    private boolean isManagerLive() {
        if(manager==null) {
            return false;
        }
        Prim managerPrim =  manager;
        try {
            return managerPrim.sfIsStarted();
        } catch (RemoteException e) {
            return false;
        }
    }
    /**
     * {@inheritDoc}
     * @param packet packet to process
     */
    public void processPacket(Packet packet) {
        sfLog().info("VMWare Message Listener: Received packet: " + packet.getFrom() + ": " + packet.toXML());

        // get the extension
        XMPPEventExtension ext = (XMPPEventExtension) packet.getExtension(XMPPEventExtension.rootElement, XMPPEventExtension.namespace);
        if (ext != null)
        {
            // print the content of the property bag
            sfLog().info("Printing content of received property bag:");
            for (String key : ext.getPropertyBag().keySet()) {
                sfLog().info(String.format("%s = %s", key, ext.getPropertyBag().get(key)));
            }

            // use the property bag
            String command = ext.getPropertyBag().get(VMCMD);
            String strName = ext.getPropertyBag().get(VMNAME);

            if (command != null) {
                // extension for the response message
                XMPPEventExtension response = new XMPPEventExtension();

                response.setMessageType(MonitoringConstants.VM_MESSAGE);
                response.setTimestamp(String.format("%d", Calendar.getInstance().getTimeInMillis()));
                try {
                    response.setHost(InetAddress.getLocalHost().getHostName());
                } catch (UnknownHostException e) {
                    response.setHost(packet.getTo());
                }

                // fill the response bag
                response.getPropertyBag().put(VMCMD, command);
                response.getPropertyBag().put(VMNAME, strName);

                if (!isManagerLive()) {
                    response.getPropertyBag().put(VMRESPONSE, "No VMWareServerManager running");
                }
                else {
                    try {
                        if (command.equals("start")) {
                            // attempt to start the machine
                            response.getPropertyBag().put(VMRESPONSE, manager.startVM(strName));
                        }
                        else if (command.equals("stop")) {
                            response.getPropertyBag().put(VMRESPONSE, manager.shutDownVM(strName));
                        }
                        else if (command.equals("suspend")) {
                            response.getPropertyBag().put(VMRESPONSE, manager.suspendVM(strName));
                        }
                        else if (command.equals("reset")) {
                            response.getPropertyBag().put(VMRESPONSE, manager.resetVM(strName));
                        }
                        else if (command.equals("list")) {
                            // count the machines
                            int i = 0;
                            for (VMWareImageModule mod : manager.getControlledMachines()) {
                                // and the display name
                                try {
                                    response.getPropertyBag().put("list_" + i + "_vmname", mod.getAttribute("displayName"));
                                } catch (SmartFrogException e) {
                                    response.getPropertyBag().put("list_" + i + "_vmname", "Could not retreive displayName.");
                                }

                                // and the power state
                                try {
                                    response.getPropertyBag().put("list_" + i + "_vmstate", manager.convertPowerState(mod.getPowerState()));
                                } catch (SmartFrogException e) {
                                    response.getPropertyBag().put("list_" + i + "_vmstate", "Could not retreive power state.");
                                }

                                i++;
                            }
                            response.getPropertyBag().put("list_count", String.format("%d", i));
                        }
                        else if (command.equals("powerstate")) {
                            response.getPropertyBag().put(VMRESPONSE, manager.convertPowerState(manager.getPowerState(strName)));
                        }
                        else if (command.equals("toolsstate")) {
                            response.getPropertyBag().put(VMRESPONSE, manager.convertToolsState(manager.getToolsState(strName)));
                        }
                        else if (command.equals("create")) {
                            // get additional attributes
                            String  name    = ext.getPropertyBag().get("create_name"),
                                    master  = ext.getPropertyBag().get("create_master"),
                                    user    = ext.getPropertyBag().get("create_user"),
                                    pass    = ext.getPropertyBag().get("create_pass");

                            // create a vmware from a master model
                            response.getPropertyBag().put(VMRESPONSE, manager.createCopyOfMaster(master, name, user, pass));
                            response.getPropertyBag().put(VMNAME, name);
                        }
                        else if (command.equals("delete")) {
                            // delete a vmware
                            response.getPropertyBag().put(VMRESPONSE, manager.deleteCopy(strName));
                        }
                        else if (command.equals("getmasters")) {
                            // list the master copies
                            response.getPropertyBag().put(VMRESPONSE, manager.getMasterImages());
                        }
                        else if (command.equals("rename")) {
                            // get the new name
                            String newName = ext.getPropertyBag().get("rename_name");

                            // rename the vm
                            response.getPropertyBag().put(VMRESPONSE, manager.renameVM(strName, newName));

                            // correct response
                            response.getPropertyBag().put(VMNAME, newName);

                            response.getPropertyBag().put("rename_old_name", strName);
                        }
                        else if (command.equals("getattribute")) {
                            // get the key
                            String key = ext.getPropertyBag().get("getattrib_key");

                            // get the attribute
                            response.getPropertyBag().put(VMRESPONSE, manager.getVMAttribute(strName, key));
                        }
                        else if (command.equals("setattribute")) {
                            // get the key and the new value
                            String  key = ext.getPropertyBag().get("setattrib_key"),
                                    value = ext.getPropertyBag().get("setattrib_value");

                            if (key.equals("displayName")) {
                                response.getPropertyBag().put(VMRESPONSE, "Please use the rename command to rename a VM.");
                            }
                            else {
                                // set the attribute
                                response.getPropertyBag().put(VMRESPONSE, manager.setVMAttribute(strName, key, value));
                            }
                        }
                        else if (command.equals("executeinguest")) {
                            String  strCommand = ext.getPropertyBag().get("exec_cmd"),
                                    strParameters = ext.getPropertyBag().get("exec_param");
                            boolean bNoWait = (ext.getPropertyBag().get("exec_nowait") != null);

                            response.getPropertyBag().put(VMRESPONSE, manager.executeInGuestOS(strName, strCommand, strParameters, bNoWait));
                        }
                        else if (command.equals("waitfortools")) {
                            int iTimeout = Integer.parseInt(ext.getPropertyBag().get("wait_timeout"));
                            response.getPropertyBag().put(VMRESPONSE, manager.waitForTools(strName, iTimeout));
                        }
                        else if (command.equals("takesnapshot")) {
                            String  strSnapName = ext.getPropertyBag().get("tsnap_name"),
                                    strSnapDesc = ext.getPropertyBag().get("tsnap_desc");
                            boolean bIncMem = Boolean.parseBoolean(ext.getPropertyBag().get("tsnap_incmem"));

                            response.getPropertyBag().put(VMRESPONSE, manager.takeSnapshot(strName, strSnapName, strSnapDesc, bIncMem));
                        }
                        else if (command.equals("reverttosnapshot")) {
                            String strSnapName = ext.getPropertyBag().get("rsnap_name");
                            if (strSnapName != null)
                                response.getPropertyBag().put(VMRESPONSE, manager.revertVMToSnapshot(strName, strSnapName));
                            else
                                response.getPropertyBag().put(VMRESPONSE, manager.revertVMToSnapshot(strName));
                        }
                        else if (command.equals("setguestcred")) {
                            String  strUser = ext.getPropertyBag().get("setgcred_user"),
                                    strPass = ext.getPropertyBag().get("setgcred_pass");

                            response.getPropertyBag().put(VMRESPONSE, manager.setGuestOSCredentials(strName, strUser, strPass));
                        }
                        else if(command.equals("deletesnapshot")) {
                            boolean bDelChildren = Boolean.parseBoolean(ext.getPropertyBag().get("dsnap_delchild"));
                            String strSnapName = ext.getPropertyBag().get("dsnap_name");
                            if (strSnapName != null)
                                response.getPropertyBag().put(VMRESPONSE, manager.deleteVMSnapshot(strName, strSnapName, bDelChildren));
                            else
                                response.getPropertyBag().put(VMRESPONSE, manager.deleteVMSnapshot(strName, bDelChildren));
                        }
    //                    else if (command.equals("copyhosttoguest")) {
    //                        String strSrc = ext.getPropertyBag().get("source");
    //                        String strDest = ext.getPropertyBag().get("dest");
    //                        response.getPropertyBag().put(VMRESPONSE, manager.copyFileFromHostToGuestOS(strName, strSrc, strDest));
    //                    }
                        // set the name of this module

                    } catch (Exception e) {
                        ProcessException(command, response, e);
                    }
                }

                // send the message
                sfLog().info("sending message: " + response);
                refXmppListener.sendMessage(packet.getFrom(), "", "AE", response);
            }
        }
    }

    private void ProcessException(String command, XMPPEventExtension newExt, Throwable thrown) {
        sfLog().error("Failing command "+ command, thrown);
        newExt.getPropertyBag().put(VMRESPONSE, thrown.toString());
    }
}