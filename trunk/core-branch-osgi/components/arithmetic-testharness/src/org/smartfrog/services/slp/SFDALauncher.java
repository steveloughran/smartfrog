package org.smartfrog.services.slp;

import org.smartfrog.sfcore.prim.*;
import org.smartfrog.sfcore.reference.*;
import org.smartfrog.sfcore.common.*;
import org.smartfrog.sfcore.parser.*;
import org.smartfrog.sfcore.componentdescription.*;

import java.rmi.*;
import java.net.*;
import java.util.*;

/** This component creates an mslp mesh-enhanced Directory agent
 *  with the properties specified in the description.
 *  A reference ('mslpRef') is added to the description after successful
 *  DA launch that can be used directly by SAs and UAs to point to this DA.
 *
 * @author Guillaume Mecheneau
 *
 * @version $Revision$ */

public class SFDALauncher extends PrimImpl implements Prim {
//  ComponentDescription requiredService;
//  Prim serviceLocator;
  da directoryAgent;

  /** If set to true, the DA simulates absence of multicast */
  boolean mcast = true;

  /** If set to true, the DA is mesh-enhanced */
  boolean mesh_enhanced = true;

  /** If set to true, turns the mslp gui on */
  boolean gui = true;

  /** The host name */
  String hname;

  /** The scopes for which this DA will be configured for its entire lifetime */
  String scopes;

  /**
   *  The standard constructor for RMI
   */
  public SFDALauncher() throws RemoteException {}

  /** Deploys an mslp Directory Agent for this host.
   * Looks for the following attributes in the description:
   *  scopes : the scopes for this DA (comma-separated list)
   *  mcast : if set to "no", simulates condition where multicast is not supported
   *  mesh : if set to "no", the DA works as a non mesh-enhanced DA
   *  gui : if set to "no", the mlp gui is turned off.
   *
   * @exception Exception error while deploying */
  public void sfDeploy() throws SmartFrogException , RemoteException{
    super.sfDeploy();
    try {
            // we need a trick to get the absolute domain name of the host
        hname = (InetAddress.getLocalHost()).getHostAddress();
        hname = (InetAddress.getByName(hname)).getHostName();
    }catch (java.net.UnknownHostException uhe) {
        throw new SmartFrogException(uhe);
    }

    // setting scope for this DA
    try {
      scopes = (String) this.sfResolve("scopes");
    } catch(SmartFrogResolutionException rex) {
      scopes = "DEFAULT";
    }
   // setting multicast simulation for this DA
     try {
      String smcast = (String) this.sfResolve("mcast");
      if (smcast != null && smcast.equalsIgnoreCase("no")) mcast = false;
    } catch (Exception e){}
    // gui or not gui, that is the question
    try {
      String sgui = (String) this.sfResolve("gui");
      if (sgui != null && sgui.equalsIgnoreCase("off")) gui = false;
    } catch (Exception e){}
    // mesh-enhancement
    try {
      String smesh = (String) this.sfResolve("mesh");
      if (smesh != null && smesh.equalsIgnoreCase("no")) mesh_enhanced = false;
    } catch (Exception e){}
    System.out.println("Launching DA for "+ hname +" with scopes " +scopes);
  // in this implementation we ignore summary and database files
    // create the DA
    directoryAgent = new da(hname, scopes, null,null,mcast, mesh_enhanced, gui);
  // add a reference in the description for direct use by other mslp agents (SA/UA)
    String mslpRef = new String(hname+" "+scopes);
    sfReplaceAttribute("mslpRef",mslpRef);
  //  System.out.println(" Adding reference 'mslpRef' to component :" + mslpRef);

  }


    public void sfTerminateWith(TerminationRecord tr) {
	System.out.println(" Stopping DA");
	directoryAgent.dispose(); 
      	directoryAgent = null;
        super.sfTerminateWith(tr);
    }








}
