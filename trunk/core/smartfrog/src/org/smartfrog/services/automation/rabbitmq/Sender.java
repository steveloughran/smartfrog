package org.smartfrog.services.automation.rabbitmq;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import org.smartfrog.services.automation.statemodel.state.State;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.prim.TerminationRecord;

public class Sender extends State {
	String topic = "";
	String hostname = "localhost";
	int port = 0;
	String username = "";
	String password = "";
	String virtualhost = "";
	
	HashMap dataToSend = new HashMap();
	ByteArrayOutputStream bos = null;
	ObjectOutputStream oos = null;
	int epoch = -1;
	
	Connection connection = null;;
	Channel channel= null;;
	ConnectionFactory factory = null;

	
	Thread notifier = new Thread() {
		public void run() {
			while (true) 
			try {
				Thread.sleep(5000); //should be parameterised
				try {
					System.out.println(sfCompleteNameSafe() + " sending message length " + bos.toByteArray().length);
					channel.basicPublish(topic, "", null, bos.toByteArray());
				} catch (IOException e) {
			        System.out.println(sfCompleteNameSafe() + " Error sending message");		
					e.printStackTrace();
				}
			} catch (InterruptedException e) {
				return;
			} 			
		}
	};
	
	
	public Sender() throws RemoteException {
		super();
	}

	@Override
	public void sfDeploy() throws RemoteException, SmartFrogException {
		super.sfDeploy();
		topic = (String) sfResolve("topic", topic, false);
		hostname = (String) sfResolve("hostname", hostname, false);
		port = ((Integer) sfResolve("port", port, false)).intValue();
		username = (String) sfResolve("username", username, false);
		password = (String) sfResolve("password", password, false);
		virtualhost = (String) sfResolve("virtualhost", virtualhost, false);
        ConnectionFactory factory = new ConnectionFactory();

        if (hostname != "") factory.setHost(hostname);
        if (port != 0) factory.setPort(port);
        if (username != "") factory.setUsername(username);
        if (password != "") factory.setPassword(password);

        System.out.println(sfCompleteNameSafe() + " settings " + factory.getHost() + ":" + factory.getPort() + ":" + factory.getUsername() + ":" + factory.getPassword() + ":" + factory.getVirtualHost());
        try {
	        connection = factory.newConnection();
	        channel = connection.createChannel();
	        channel.exchangeDeclare(topic, "fanout");
        } catch (Exception e) {
        	System.out.println(sfCompleteNameSafe() + " error in setting up message bus access" + e);
        	e.printStackTrace();
        }
        notifier.start();
	}

	
	@Override
	public void sfTerminateWith(TerminationRecord tr) {
        try {
			channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
        try {
			connection.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
        notifier.interrupt();
        super.sfTerminateWith(tr);
	}
	
	@Override
	public void setState(HashMap data) {
		try {
			bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			try {
				dataToSend.put("epoch", new Integer(++epoch));
				dataToSend.put("state", data);
				oos.writeObject(dataToSend);
				try {
					System.out.println(sfCompleteNameSafe() + " sending message length " + bos.toByteArray().length);
					channel.basicPublish(topic, "", null, bos.toByteArray());
				} catch (IOException e) {
			        System.out.println(sfCompleteNameSafe() + " Error sending message");		
					e.printStackTrace();
				}
			} catch (IOException e1) {
		        System.out.println(sfCompleteNameSafe() + " Error serializing message");		
				e1.printStackTrace();
			}
		} catch (IOException e2) {
			System.out.println(sfCompleteNameSafe() + " Error creating output stream");
			e2.printStackTrace();
		}

	}
}




