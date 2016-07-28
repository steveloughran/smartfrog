package org.smartfrog.services.automation.rabbitmq;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;


import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import org.smartfrog.services.automation.statemodel.state.State;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.prim.TerminationRecord;

public class Receiver extends State {
	String topic = "";
	String hostname = "localhost";
	int port = 0;
	String username = "";
	String password = "";
	String virtualhost = "";
	String queue = "default";
	
	int epoch = -1;
	
	Connection connection = null;;
	Channel channel= null;;
	ConnectionFactory factory = null;

	public Receiver() throws RemoteException {
		super();
	}

	@Override
	public void sfDeploy() throws RemoteException, SmartFrogException {
		super.sfDeploy();
		topic = (String) sfResolve("topic", topic, false);
		queue = (String) sfResolve("queue", queue, false);
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

        System.out.println(sfCompleteNameSafe() + "settings " + factory.getHost() + ":" + factory.getPort() + ":" + factory.getUsername() + ":" + factory.getPassword() + ":" + factory.getVirtualHost());
        try {
	        connection = factory.newConnection();
	        channel = connection.createChannel();
	        channel.exchangeDeclare(topic, "fanout");
	        channel.queueDeclare(queue, true, false, false, null);
	        channel.queueBind(queue, topic, "");
        } catch (Exception e) {
        	System.out.println(sfCompleteNameSafe() + "error in setting up message bus access" + e);
        	e.printStackTrace();
        }
        System.out.println(sfCompleteNameSafe() + " channel set up");

        boolean autoAck = false;
        try {
			channel.basicConsume(queue, autoAck, sfCompleteNameSafe().toString(),
			     new DefaultConsumer(channel) {
			         @Override
			         public void handleDelivery(String consumerTag,
			                                    Envelope envelope,
			                                    AMQP.BasicProperties properties,
			                                    byte[] body)
			             throws IOException
			         {
			        	
			             try {
							 //System.out.println(sfCompleteNameSafe() + " handler called");
							 String routingKey = envelope.getRoutingKey();
							 String contentType = properties.getContentType();
							 long deliveryTag = envelope.getDeliveryTag();
							 // (process the message components here ...)
							 ByteArrayInputStream b = new ByteArrayInputStream(body);
							 ObjectInputStream o = new ObjectInputStream(b);
							 try {
								Object h = o.readObject(); //don't throw an exception by assuming it is a HashMap
								if (h instanceof HashMap) { // do nothing if not
									HashMap hm = (HashMap)h;
									System.out.println(sfCompleteNameSafe() + " save state " + hm.toString());
									if (((Integer)hm.get("epoch")) != epoch) {
										//System.out.println(sfCompleteNameSafe() + " epoch changed, saving state");
										epoch = (Integer)hm.get("epoch");
										saveState((HashMap)hm.get("state"));
									} else {
										//System.out.println(sfCompleteNameSafe() + " no epoch change");
									}
								}
							} catch (ClassNotFoundException e) {
							    System.out.println(sfCompleteNameSafe() + " error reading message");
								e.printStackTrace();
							}
							 channel.basicAck(deliveryTag, false);
						} catch (NullPointerException e) {
							System.out.println(sfCompleteNameSafe() + " null pointer");
						}
			         }
			     });
		} catch (IOException e) {
			e.printStackTrace();
		}
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
        super.sfTerminateWith(tr);
	}
	
	@Override
	public void setState(HashMap data) {
	}
}




