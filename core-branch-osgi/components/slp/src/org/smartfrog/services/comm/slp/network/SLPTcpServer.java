/*
 Service Location Protocol - SmartFrog components.
 Copyright (C) 2004 Glenn Hisdal <ghisdal(a)c2i.net>
 
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
 
 This library was originally developed by Glenn Hisdal at the 
 European Organisation for Nuclear Research (CERN) in Spring 2004. 
 The work was part of a master thesis project for the Norwegian 
 University of Science and Technology (NTNU).
 
 For more information: http://home.c2i.net/ghisdal/slp.html 
 */

package org.smartfrog.services.comm.slp.network;

import org.smartfrog.services.comm.slp.ServiceLocationException;
import org.smartfrog.services.comm.slp.agents.SLPMessageCallbacks;
import org.smartfrog.services.comm.slp.util.SLPInputStream;
import org.smartfrog.services.comm.slp.util.SLPOutputStream;
import org.smartfrog.services.comm.slp.messages.SLPMessageHeader;

import java.net.*;
import java.io.*;


public class SLPTcpServer extends Thread {
    private ServerSocket serverSocket = null;
    private SLPMessageCallbacks agent;
    private boolean isRunning = true;
    
    public SLPTcpServer(InetAddress address, int port, SLPMessageCallbacks a) throws IOException {
        int backlog = 5;
        agent = a;
        //System.out.println("Creating serversocket: " + address.toString() + " - " + port);
        serverSocket = new ServerSocket(port, backlog, address);
    }
    
    public void run() {
        while(isRunning) {
            Socket s = null;
            try {
                s = serverSocket.accept();
            }catch(Exception e) {
                //ioe.printStackTrace();
            }
            if(s != null) {
                SLPTcpRequestHandler handler = new SLPTcpRequestHandler(s, agent);
                //System.out.println("TCPServer: Received request - Starting handler...");
                handler.start();
            }
        }
        try {
            serverSocket.close();
        }catch(Exception e) {
            //e.printStackTrace();
        }
    }
    
    public void stopThread() {
        isRunning = false;
        try {
            serverSocket.close();
        }catch(Exception e) { }
    }
}

class SLPTcpRequestHandler extends Thread {
    private Socket socket;
    private InputStream istream;
    private OutputStream ostream;
    private SLPMessageCallbacks agent;
    
    SLPTcpRequestHandler(Socket s, SLPMessageCallbacks a) {
        socket = s;
        agent = a;
    }
    
    public void run() {
        try {
            istream = socket.getInputStream();
            ostream = socket.getOutputStream();
            SLPInputStream sis = new SLPInputStream(istream);
            int version = sis.readByte();
            int function = sis.readByte();
            //System.out.println("TCP request: v="+version+", f="+function);
            SLPMessageHeader msgReply = agent.handleNonReplyMessage(function, sis, false);
            SLPOutputStream sos = new SLPOutputStream(new ByteArrayOutputStream());
            msgReply.toOutputStream(sos);
            ostream.write(sos.getByteArray());
        }catch(IOException e) {
            //e.printStackTrace();
        }catch(ServiceLocationException e) {
            //e.printStackTrace();
        }
    }
}

