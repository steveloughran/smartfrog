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
package org.smartfrog.services.ssh;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import org.smartfrog.sfcore.logging.Log;

/**
 * Abstract parent class for ScpTo and ScpFrom.
 * @author Ashish Awasthi
 * @see http://www.jcraft.com/jsch/
 * 
 */
public abstract class AbsScp {

    protected final byte LINE_FEED = 0x0a;
    protected final int BUFFER_SIZE = 1024;
    protected Log log;
    
    /**
     * Constucts AbsScp.
     */
    public AbsScp(Log parentLog) {
        this.log = parentLog;
    }
    /**
     * Write acknowlegement by writing char '0' to output stream of the channel.
     * @throws IOException if unable to write 
     */
    protected void writeAck(OutputStream out) throws IOException {
        byte[] buf = new byte[1];
        buf[0] = 0;
        out.write(buf);
        out.flush();
    }
    
    /**
     * Reads server response from channel's input stream. 
     * @throws IOException if response is an error
     */
    protected void checkAck(InputStream in)throws IOException {
        int svrRsp = in.read();

        /*
         * svrRsp may be 0 for success,
         *          1 for error,
         *          2 for fatal error,
         *         -1 No Response from Server
         */  
        if (svrRsp == -1) {
            // didn't receive any response
            throw new IOException("No response from server");
        } else if (svrRsp != 0) {
            StringBuffer sb = new StringBuffer();
            int ch = in.read();
            while (ch > 0 && ch != '\n') {
                sb.append((char) ch);
                ch = in.read();
            }
            if (svrRsp == 1) {
                throw new IOException(
                        "error reported by server: "+ sb.toString());
            } else if (svrRsp == 2) {
                throw new IOException(
                        "fatal error reported by server: " + sb.toString());
            } else {
                throw new IOException("unknown response, code " + svrRsp
                                         + " message: " + sb.toString());
            }
        }
    }
    /**
     * Logs debug message
     * @param msg debug message
     
    protected void logDebugMsg(String msg) {
        if (log.isDebugEnabled()) {
            log.debug(msg);
        }
    }
    */
}

