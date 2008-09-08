package org.smartfrog.vast.testing.shared;

import java.rmi.RemoteException;

public interface ScriptAction extends SUTAction {
	public static final String ATTR_PROCESS_NAME = "ProcessName";
	public static final String ATTR_SCRIPT_NAME = "ScriptName";

	public String getScriptName() throws RemoteException;

	public String getProcessName() throws RemoteException;
}