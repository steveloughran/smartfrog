/** (C) Copyright 1998-2008 Hewlett-Packard Development Company, LP

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

package org.smartfrog.vast.testing.networking.messages;

import java.net.InetAddress;
import java.util.Map;

public class RunSfScript implements VastMessage {
	private String Script;
	private Map<String, String> Env;

	public void invoke(InetAddress inFrom, MessageCallback inMessageCallback) {
		inMessageCallback.OnRunSfScript(Script, Env);
	}

	public RunSfScript(Map<String, String> env, String script) {
		Env = env;
		Script = script;
	}

	public String getScript() {
		return Script;
	}

	public void setScript(String script) {
		Script = script;
	}

	public Map<String, String> getEnv() {
		return Env;
	}

	public void setEnv(Map<String, String> env) {
		Env = env;
	}
}