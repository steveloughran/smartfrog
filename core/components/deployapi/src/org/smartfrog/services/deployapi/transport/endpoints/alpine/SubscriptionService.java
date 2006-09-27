/** (C) Copyright 2006 Hewlett-Packard Development Company, LP

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
package org.smartfrog.services.deployapi.transport.endpoints.alpine;

import org.smartfrog.services.deployapi.transport.endpoints.alpine.WsrfHandler;
import org.smartfrog.services.deployapi.transport.wsrf.NotificationSubscription;
import org.smartfrog.services.deployapi.transport.wsrf.WSRPResourceSource;
import org.smartfrog.services.deployapi.transport.faults.FaultRaiser;
import org.smartfrog.projects.alpine.core.MessageContext;
import org.smartfrog.projects.alpine.core.EndpointContext;
import org.smartfrog.projects.alpine.om.soap11.MessageDocument;
import org.smartfrog.projects.alpine.faults.ServerException;
import org.smartfrog.projects.alpine.wsa.AlpineEPR;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import nu.xom.Element;

import java.util.HashMap;
import java.lang.ref.WeakReference;

/**
 * created 27-Sep-2006 13:18:32
 */

public class SubscriptionService extends WsrfHandler {

    private Log log= LogFactory.getLog(SubscriptionService.class);

    /**
     * Use a weak reference hashmap, so that when a subscription is destroyed
     * it is automatically purged here.
     */
    private HashMap<String, WeakReference<NotificationSubscription>> subscriptions=new HashMap();

    /**
     * Add a subscription
     * @param sub
     */
    public AlpineEPR add(NotificationSubscription sub) {
        subscriptions.put(sub.getId(), new WeakReference<NotificationSubscription>(sub));
        //return a new address
        return null;
    }

    public void remove(String key) {
        subscriptions.remove(key);
    }

    public void remove(NotificationSubscription sub) {
        remove(sub.getId());
    }

    public NotificationSubscription lookup(String key) {
        WeakReference<NotificationSubscription> ref = subscriptions.get(key);
        NotificationSubscription sub = ref.get();
        if(sub==null) {
            log.info("Purging subscription data for "+key);
            remove(key);
        }
        return sub;
    }

    /*
    Handle <Pause> -> Pause operation could not be performed on the Subscription
    */
    @Override
    public void process(MessageContext messageContext, EndpointContext endpointContext) {
        //hand off WSRF processing to the superclass
        super.process(messageContext, endpointContext);
        if (messageContext.isProcessed()) {
            //exit immediately if processing took place.
            return;
        }

        MessageDocument response = messageContext.createResponse();
        Element payload = messageContext.getRequest().getBody().getFirstChildElement();
        if (payload == null) {
            throw new ServerException("Empty SOAP message");
        }


        NotificationSubscription sub=lookupSubscription(messageContext);

        //TODO: handle WSNT operations

        //this is the pivot point; declare ourselves finished
        messageContext.setProcessed(true);
//        processor.process(request, response);
    }

    /**
     * Return the resource source for this message, which is whatever
     * is subscribed to here
     *
     * @param message
     * @return the source of resources. Return null for no match in that context
     * @throws org.smartfrog.services.deployapi.transport.faults.BaseException
     *
     */
    public WSRPResourceSource retrieveResourceSource(MessageContext message) {
        return lookupSubscription(message);
    }

    private NotificationSubscription lookupSubscription(MessageContext message) {
        MessageDocument request = message.getRequest();
        AlpineEPR to = getTo(request);
        String id = NotificationSubscription.extractSubscriptionIDFromQuery(to.getAddress());
        NotificationSubscription sub = lookup(id);
        if (sub == null) {
            FaultRaiser.raiseBadArgumentFault("No subscription of that address");
        }
        return sub;
    }

}
