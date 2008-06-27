package org.smartfrog.services.dependencies.statemodel.state;

import org.smartfrog.sfcore.common.Context;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 */
public interface StateChangeNotification{
   //child down to State, where it is handled
   public void handleStateChange();
}