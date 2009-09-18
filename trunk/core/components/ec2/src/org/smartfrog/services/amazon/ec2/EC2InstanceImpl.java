/* (C) Copyright 2008 Hewlett-Packard Development Company, LP

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


package org.smartfrog.services.amazon.ec2;

import com.xerox.amazonws.ec2.EC2Exception;
import com.xerox.amazonws.ec2.InstanceType;
import com.xerox.amazonws.ec2.ReservationDescription;
import com.xerox.amazonws.ec2.TerminatingInstanceDescription;
import org.smartfrog.sfcore.common.SmartFrogDeploymentException;
import org.smartfrog.sfcore.common.SmartFrogException;
import org.smartfrog.sfcore.common.SmartFrogLifecycleException;
import org.smartfrog.sfcore.prim.TerminationRecord;
import org.smartfrog.sfcore.utils.WorkflowThread;

import java.rmi.RemoteException;
import java.util.List;

/**
 * Component to deploy an instance
 */

public class EC2InstanceImpl extends EC2ComponentImpl implements EC2Instance {

    private String instanceType;
    private String imageID;
    private boolean shutdown;
    private String userData;
    private ReservationDescription reservation;
    //minimum number of instances
    private int minCount;
    //max number
    private int maxCount;
    private InstanceList instances = InstanceList.EMPTY_LIST;
    private final Object reservationComplete = new Object();
    /**
     * Error message: {@value}
     */
    public static final String ERROR_NO_IMAGE = "No image to deploy";
    /**
     * Error message: {@value}
     */
    public static final String ERROR_UNRECOGNISED_IMAGE_TYPE = "Unrecognised image type: ";

    /**
     * switch to turn EC2 support off. Used when getting workflow right {@value}
     */
    private static final boolean EC2_ENABLED = true;
    public static final String WARNING_THE_WORKER_IS_STILL_BUSY =
            "Warning: the worker is still busy -the request to create instances may still be active";


    public EC2InstanceImpl() throws RemoteException {
    }

    /**
     * Can be called to start components. Subclasses should override to provide functionality Do not block in this call,
     * but spawn off any main loops!
     *
     * @throws SmartFrogException failure while starting
     * @throws RemoteException In case of network/rmi error
     */
    @Override
    public synchronized void sfStart()
            throws SmartFrogException, RemoteException {
        super.sfStart();
        minCount = sfResolve(ATTR_MIN_COUNT, 0, true);
        maxCount = sfResolve(ATTR_MAX_COUNT, 0, true);
        instanceType = sfResolve(ATTR_INSTANCETYPE, "", true);
        imageID = sfResolve(ATTR_IMAGEID, "", true);
        userData = sfResolve(ATTR_USER_DATA, "", true);
        shutdown = sfResolve(ATTR_SHUTDOWN, false, true);
        if (imageID.length() == 0) {
            throw new SmartFrogLifecycleException(ERROR_NO_IMAGE);
        }
        deployWorker(new Ec2InstanceThread());
    }

    /**
     * Provides hook for subclasses to implement useful termination behavior. Deregisters component from local process
     * compound (if ever registered)
     *
     * @param status termination status
     */
    @Override
    protected synchronized void sfTerminateWith(TerminationRecord status) {
        if (shutdown) {
            terminateDeployedInstances();
        }
        super.sfTerminateWith(status);
    }

    /**
     * Terminate any deployed instances
     */
    protected void terminateDeployedInstances() {
        InstanceList list = instances;
        if (list != null) {
            try {
                sfLog().info("About to terminate " + list.size() + " instances");
                List<TerminatingInstanceDescription> tid = list.terminate();
                logTerminationInfo(tid);
            } catch (EC2Exception e) {
                sfLog().warn("When terminating", e);
            }
        } else {
            sfLog().info("No instances to terminate");
            if (getWorker() != null) {
                sfLog().warn(WARNING_THE_WORKER_IS_STILL_BUSY);
            }
        }
    }

    protected void reserve(ReservationDescription reservationInfo) {
        reservation = reservationInfo;
    }

    /**
     * Callback from the worker when the reservation is finished
     *
     * @throws SmartFrogException any SmartFrog problems
     * @throws RemoteException networking
     */
    protected void reservationCompleted()
            throws SmartFrogException, RemoteException {
        sfReplaceAttribute(ATTR_INSTANCE, getId());
        sfReplaceAttribute(ATTR_INSTANCES, instances.listInstanceIDs());
        logInstances(instances);
        workCompleted();
        //notify anything listening
        synchronized (reservationComplete) {
            reservationComplete.notifyAll();
        }
    }

    /**
     * Thread to create the instance
     */
    private class Ec2InstanceThread extends WorkflowThread {


        /**
         * Create a basic thread. Notification is bound to a local notification object.
         */
        private Ec2InstanceThread() {
            super(EC2InstanceImpl.this, true);
        }

        /**
         * If this thread was constructed using a separate {@link Runnable} run object, then that <code>Runnable</code>
         * object's <code>run</code> method is called; otherwise, this method does nothing and returns. <p> Subclasses
         * of <code>Thread</code> should override this method.
         *
         * @throws Throwable if anything went wrong
         */
        @Override
        public void execute() throws Throwable {
            InstanceType size = InstanceType.getTypeFromString(instanceType);
            if (size == null) {
                throw new SmartFrogDeploymentException(
                        ERROR_UNRECOGNISED_IMAGE_TYPE + instanceType);
            }
            sfLog().info("Deploying a " + instanceType + " image ID " + imageID);
            if (EC2_ENABLED) {
                try {
                    reservation = getEc2binding().runInstances(
                            imageID,
                            minCount,
                            maxCount,
                            null,
                            userData,
                            null,
                            true,
                            size);
                    sfLog().info("Reserved an instance:" + reservation);
                    instances = InstanceList.listInstances(getEc2binding(), reservation);

                } catch (EC2Exception e) {
                    throw new SmartFrogEC2Exception("Failed to reserve an instance of " +
                            imageID +
                            " on " +
                            instanceType,
                            e);
                }
            } else {
                sfLog().info("Skipping EC2 deployment as it has been disabled");
            }
            reservationCompleted();
        }

        /**
         * decide whether we passed or failed based on the termination record
         *
         * @param tr the termination record about to be passed up
         */
        @Override
        protected void aboutToTerminate(TerminationRecord tr) {
            workCompleted(tr.getCause());
        }
    }

    private class Ec2ConsoleThread extends WorkflowThread {

        /**
         * Create a basic thread. Notification is bound to a local notification object.
         *
         * @param workflowTermination is workflow termination expected
         */
        private Ec2ConsoleThread(boolean workflowTermination) {
            super(EC2InstanceImpl.this, workflowTermination);
        }
    }

}
