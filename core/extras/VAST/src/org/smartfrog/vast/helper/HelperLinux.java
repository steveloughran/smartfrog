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

package org.smartfrog.vast.helper;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class HelperLinux implements Helper {
    public ArrayList<String> retrieveNICNames() {
        // the list which will be returned
        ArrayList<String> names = new ArrayList<String>();

        try {
            // execute the ifconfig command
            Process ps = Runtime.getRuntime().exec("ifconfig -a");

			// read the output and the error stream
			// doing this asynchronously prevents blocking
			// of the process
			ArrayList<String> outBuffer = new ArrayList<String>();
			ArrayList<String> errBuffer = new ArrayList<String>();
			ReaderThread rtOut = new ReaderThread(ps.getInputStream(), outBuffer);
			ReaderThread rtErr = new ReaderThread(ps.getErrorStream(), errBuffer);
			rtOut.start();
			rtErr.start();

			// wait for ifconfig to finish
            ps.waitFor();

			// wait for the output reader to finish
			while (rtOut.getState() != Thread.State.TERMINATED)
				Thread.sleep(100);

			// parse the output
            Pattern pattern = Pattern.compile("^(\\w{1,9})\\s+Link\\sencap\\:Ethernet.*$");
            for(String strLine : outBuffer) {
                Matcher matcher = pattern.matcher(strLine);
                if (matcher.matches()) {
                    names.add(matcher.group(1));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return names;
    }

    public void setNetworkAddress(String inNICName, String inIP, String inMask) {
        // check addresses
        if (Validator.isValidIP(inIP) && Validator.isValidSubnetMask(inMask)) {
            try {
                Process ps = Runtime.getRuntime().exec(String.format("ifconfig %s %s netmask %s", inNICName, inIP, inMask));

				// just for blocking prevention
				ArrayList<String> outBuffer = new ArrayList<String>();
				ArrayList<String> errBuffer = new ArrayList<String>();
				ReaderThread rtOut = new ReaderThread(ps.getInputStream(), outBuffer);
				ReaderThread rtErr = new ReaderThread(ps.getErrorStream(), errBuffer);
				rtOut.start();
				rtErr.start();

				ps.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

	public void setDefaultGateway(String inGatewayAddress, String inNICName) {
		if (Validator.isValidIP(inGatewayAddress)) {
			try {
                Process ps = Runtime.getRuntime().exec(String.format("route add default gw %s %s", inGatewayAddress, inNICName));

				// just for blocking prevention
				ArrayList<String> outBuffer = new ArrayList<String>();
				ArrayList<String> errBuffer = new ArrayList<String>();
				ReaderThread rtOut = new ReaderThread(ps.getInputStream(), outBuffer);
				ReaderThread rtErr = new ReaderThread(ps.getErrorStream(), errBuffer);
				rtOut.start();
				rtErr.start();

				ps.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
		}
	}
}
