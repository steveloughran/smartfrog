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
package org.smartfrog.services.deployapi.transport.wsrf;

import nu.xom.Element;
import nu.xom.Attribute;

import javax.xml.namespace.QName;

import org.smartfrog.services.deployapi.binding.XomHelper;
import org.smartfrog.services.deployapi.system.Constants;
import org.smartfrog.services.deployapi.transport.faults.FaultRaiser;
import org.smartfrog.services.deployapi.notifications.EventSubscription;
import org.smartfrog.services.deployapi.notifications.Event;
import org.smartfrog.services.deployapi.notifications.AbstractEventSubscription;
import org.smartfrog.services.deployapi.alpineclient.model.NotifySession;
import org.smartfrog.services.deployapi.engine.Application;
import org.smartfrog.projects.alpine.wsa.AlpineEPR;
import org.smartfrog.projects.alpine.om.base.SoapElement;
import org.smartfrog.projects.alpine.faults.AlpineRuntimeException;
import org.smartfrog.projects.alpine.xmlutils.XsdUtils;
import org.smartfrog.projects.alpine.transport.TransmitQueue;
import org.smartfrog.projects.alpine.transport.DirectExecutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.UUID;
import java.net.URL;
/*
<wsnt:Subscribe>
<wsnt:ConsumerReference>
wsa:endpointReference
</wsnt: ConsumerReference>
<wsnt:TopicExpression dialect = “xsd:anyURI”>
{any}
</wsnt:TopicExpression>
<wsnt:UseNotify> xsd:boolean </wsnt:UseNotify>?
<wsnt:Precondition> wsrp:QueryExpression </Precondition>?
<wsnt:Selector> wsrp:QueryExpression </wsnt:Selector>?
<wsnt:SubscriptionPolicy> {any} </wsnt:SubscriptionPolicy>?
<wsnt:InitialTerminationTime>
xsd:dateTime
</wsnt:InitialTerminationTime>?
</wsnt: Subscribe>
*/

/**
 * created 26-Sep-2006 16:35:14.
 * This represents a WSNT subscription. It contains a static WS resource properties map
 * as well as parsed data.
 */

public class NotificationSubscription extends AbstractEventSubscription
        implements EventSubscription, WSRPResourceSource, WSNConstants {

    private static final Log log= LogFactory.getLog(NotificationSubscription.class);
    private PropertyMap resources = new PropertyMap();
    private String id = UUID.randomUUID().toString();
    private QName topic;
    private AlpineEPR callback;
    private boolean useNotifyMessage;
    private String precondition;
    private String selector;
    private Element subscriptionPolicy;
    private String terminationTime;
    private String subscriptionURL;
    private SoapElement subscriptionEndpointer;
    private AlpineEPR subscriptionEPR;
    private NotifySession session;
    private int NOTIFY_TIMEOUT;
    private AlpineRuntimeException lastError;

    public NotificationSubscription() {
    }

    /**
     * @param request incoming request
     * @throws org.smartfrog.services.deployapi.transport.faults.BaseException
     *
     * @throws AlpineRuntimeException ifthere is trouble parsing/validating the address
     */
    public NotificationSubscription(Element request) {
        parse(request);
    }


    /**
     * check on ID only for equality
     *
     * @param o
     * @return
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final NotificationSubscription that = (NotificationSubscription) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    /**
     * use ID for hash code
     *
     * @return
     */
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    public String toString() {
        return callback == null ? "unbound notification" : callback.toString();
    }

    private void addWsntResource(String name, Element value) {
        if (value != null) {
            resources.addStaticProperty(
                    new QName(Constants.WSRF_WSNT_NAMESPACE, name, "wsnt"),
                    value);
        }
    }

    private void addWsntResource(String name, String value) {
        resources.addStaticProperty(
                new QName(Constants.WSRF_WSNT_NAMESPACE, name, "wsnt"),
                value);
    }

    /**
     * Parse the request
     *
     * @param request
     * @throws org.smartfrog.services.deployapi.transport.faults.BaseException
     *
     * @throws AlpineRuntimeException ifthere is trouble parsing/validating the address
     */
    private void parse(Element request) {
        resources.addStaticProperty(Constants.PROPERTY_MUWS_RESOURCEID,
                XomHelper.makeResourceId(getId()));
        if (!WSNConstants.SUBSCRIBE.equals(request.getLocalName())) {
            throw FaultRaiser.raiseBadArgumentFault("wrong element: " + request);
        }
        Element endref = XomHelper.getElement(request, "wsnt:" + CONSUMER_REFERENCE, true);
        addWsntResource(CONSUMER_REFERENCE, endref);
        callback = new AlpineEPR(endref, Constants.WS_ADDRESSING_NAMESPACE);
        callback.validate();
        Element topicExpression =
                XomHelper.getElement(request, "wsnt:" + TOPIC_EXPRESSION, true);
        String dialect =
                XomHelper.getAttributeValue(topicExpression, Constants.WSRF_WSNT_NAMESPACE, DIALECT, true);
        if (!dialect.equals(WSNConstants.SIMPLE_DIALECT)) {
            throw FaultRaiser.raiseBadArgumentFault("Unsupported Dialect " + dialect);
        }
        addWsntResource(TOPIC_EXPRESSION, topicExpression);
        //get the qname from the topic.
        String qnameExpresion = topicExpression.getValue();
        topic = XsdUtils.resolveQName(topicExpression, qnameExpresion, false);
        String useNotify = XomHelper.getElementValue(request, "wsnt:" + USE_NOTIFY, false);
        if (useNotify != null) {
            useNotifyMessage = XomHelper.getXsdBoolValue(useNotify);
        } else {
            useNotifyMessage=true;
        }
        addWsntResource(USE_NOTIFY, Boolean.toString(useNotifyMessage));

        subscriptionPolicy = XomHelper.getElement(request, "wsnt:" + SUBSCRIPTION_POLICY, false);
        addWsntResource(SUBSCRIPTION_POLICY, subscriptionPolicy);
        String termTime = XomHelper.getElementValue(request, "wsnt:" + INITIAL_TERMINATION_TIME, false);
        //todo: act on the term time.

        //create the session
        session = new NotifySession(callback, true, new TransmitQueue(new DirectExecutor()));
    }

    /*
        <wsnt:SubscribeResponse>
    <wsnt:SubscriptionReference>
    <wsa:Address> Address of Subscription Manager </wsa:Address>
    <wsa:ReferenceProperties>
    Subscription Identifier
    </wsa:ReferenceProperties>
    …
    </wsnt:SubscriptionReference>
    …
    </wsnt:SubscribeResponse>
    */

    /**
     * Create a subscription response, which is an EPR in a different namespace
     *
     * @return
     */
    public SoapElement createSubscribeResponse() {
        SoapElement root = XomHelper.wsntElement(SUBSCRIBE_RESPONSE);
        SoapElement address = subscriptionEPR.toXomInNewNamespace(SUBSCRIPTION_REFERENCE,
                Constants.WSRF_WSNT_NAMESPACE, "wsnt",
                Constants.WS_ADDRESSING_NAMESPACE, "wsa");
        root.addOrReplaceChild(address);
        return root;
    }

    /**
     * Something happened to this job
     *
     * @param event the event of interest
     */
    public void event(Event event) {
        //send the event to the callback
        SoapElement request;
        Application application = event.application;
        SoapElement coreEvent = createMuwsManagementEvent(null, null, application.getId());
        if (isUseNotifyMessage()) {
            request = createNotificationMessage(subscriptionEPR, coreEvent);
        } else {
            request = coreEvent;
        }
        try {
            session.invokeBlocking(null,request);
        } catch (AlpineRuntimeException e) {
            lastError = e;
            //if anything went wrong, log and unsubscribe us.
            log.error("Failed to post the event to "+callback,e);
        }
    }


    /**
     * Create a new muws event, to which you can append data.
     * The event is valid according to muws-p1.xsd.
     *
     * @param idurl  event ID URL; set to null to have one made ip
     * @param source any source XML; again, optional.
     * @param resourceID id of the resource
     * @return an event ready to be sent or to have more data appended.
     */
    protected SoapElement createMuwsManagementEvent(String idurl, Element source, String resourceID) {
        SoapElement event = new SoapElement("muws-p1-xs:"+ MUWS_MANAGEMENT_EVENT, Constants.MUWS_P1_NAMESPACE);
        SoapElement eventID = new SoapElement("muws-p1-xs:"+ MUWS_EVENT_ID, Constants.MUWS_P1_NAMESPACE);
        if (idurl == null) {
            idurl = "http://example.org/uri/" + UUID.randomUUID().toString();
        }
        eventID.appendChild(idurl);
        event.appendChild(eventID);
        SoapElement sourceComponent = new SoapElement("muws-p1-xs:"+ MUWS_SOURCE_COMPONENT, Constants.MUWS_P1_NAMESPACE);
        SoapElement sourceResource = new SoapElement("muws-p1-xs:"+ MUWS_RESOURCE_ID, Constants.MUWS_P1_NAMESPACE);
        sourceResource.appendChild(resourceID);
        sourceComponent.appendChild(sourceResource);
        if (source != null) {
            sourceComponent.appendChild(source);
        }
        event.appendChild(sourceComponent);
        return event;
    }

    protected SoapElement createNotificationMessage(AlpineEPR producer, Element message) {
        SoapElement notificationMessage = new SoapElement("wsnt:"+ WSNT_NOTIFICATION_MESSAGE, Constants.WSRF_WSNT_NAMESPACE);
        SoapElement topicElt = new SoapElement("wsnt:"+ WSNT_TOPIC, Constants.WSRF_WSNT_NAMESPACE);
        topicElt.addAttribute(
                new Attribute("wsnt:" + DIALECT, Constants.WSRF_WSNT_NAMESPACE, WSNConstants.SIMPLE_DIALECT));
        topicElt.appendQName(topic);
        notificationMessage.appendChild(topicElt);
        if (producer != null) {
            notificationMessage.appendChild(
                    producer.toXomInNewNamespace(WSNT_PRODUCER_REFERENCE, Constants.WSRF_WSNT_NAMESPACE,
                            "wsnt", Constants.WS_ADDRESSING_2004_NAMESPACE, "wsa2004"));
        }
        if (message != null) {
            SoapElement messageElt = new SoapElement("wsnt:"+ WSNT_MESSAGE, Constants.WSRF_WSNT_NAMESPACE);
            notificationMessage.appendChild(
                    messageElt);
            messageElt.appendChild(message);
        }
        return notificationMessage;
    }


    public String getId() {
        return id;
    }

    /**
     * Probe the event for still being valid
     *
     * @return true if the event is still valid; false if it has expired and should be deleted
     */
    public boolean probe() {
        //todo: expiry
        return lastError!=null;
    }


    /**
     * Get the last error received on a post.
     * @return
     */
    public AlpineRuntimeException getLastError() {
        return lastError;
    }

    public QName getTopic() {
        return topic;
    }

    public AlpineEPR getCallback() {
        return callback;
    }

    public boolean isUseNotifyMessage() {
        return useNotifyMessage;
    }

    public String getPrecondition() {
        return precondition;
    }

    public String getSelector() {
        return selector;
    }

    public Element getSubscriptionPolicy() {
        return subscriptionPolicy;
    }

    public String getTerminationTime() {
        return terminationTime;
    }


    public String getSubscriptionURL() {
        return subscriptionURL;
    }

    public void setSubscriptionURL(String subscriptionURL) {
        this.subscriptionURL = subscriptionURL;
        subscriptionEPR = new AlpineEPR(subscriptionURL);
        subscriptionEndpointer = subscriptionEPR.toXomInNewNamespace(
                Constants.ENDPOINT_REFERENCE,
                Constants.WS_ADDRESSING_NAMESPACE, "wsa",
                Constants.WS_ADDRESSING_NAMESPACE, "wsa");
    }


    /**
     * Get a property value
     *
     * @param property
     * @return null for no match;
     * @throws org.smartfrog.services.deployapi.transport.faults.BaseException
     *          if they feel like it
     */
    public List<Element> getProperty(QName property) {
        //delegate to the map
        return resources.getProperty(property);
    }

    /**
     * Create a subscription URL and set our URL to it. R
     *
     * @param baseURL
     * @return the new URL
     */
    public String createSubscriptionURL(URL baseURL) {
        setSubscriptionURL(baseURL + "?" + Constants.SUBSCRIPTION_ID_PARAM + "=" + id);
        return subscriptionURL;
    }

    public Element getSubscriptionEndpointer() {
        return subscriptionEndpointer;
    }

    public AlpineEPR getSubscriptionEPR() {
        return subscriptionEPR;
    }

    /**
     * Get the subscription ID from the query
     *
     * @param to endpoint to talk to
     * @return the trimmed string containing the subscription
     * @throws org.smartfrog.services.deployapi.transport.faults.BaseException
     *          if not found
     */
    public static String extractSubscriptionIDFromAddress(AlpineEPR to) {
        String value=to.lookupQuery(Constants.SUBSCRIPTION_ID_PARAM);
        if(value==null) {
            String message = "Didn't parameter query (" +
                    Constants.SUBSCRIPTION_ID_PARAM+
                    ") in " +
                    to.toString();
            throw FaultRaiser.raiseBadArgumentFault(message);

        }
        return value;
    }

}
