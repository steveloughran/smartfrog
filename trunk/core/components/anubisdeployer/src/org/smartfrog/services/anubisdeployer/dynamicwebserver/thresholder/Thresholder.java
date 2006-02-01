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

package org.smartfrog.services.anubisdeployer.dynamicwebserver.thresholder;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * <p>
 * Description: Thresholder interface.
 * </p>
 *
 */

public interface Thresholder extends Remote {
    public final String LOGTO = "logTo";
    public final String UPPERTHRESHOLD = "upperThreshold";
    public final String LOWERTHRESHOLD = "lowerThreshold";
    public final String POLLFREQUENCY = "pollFrequency";
    public final String REPEATMEASURES = "repeatMeasures";
    public final String STABILIZATIONMEASURES = "stabilizationMeasures";
    public final String MININSTANCES = "minInstances";
    public final String MAXINSTANCES = "maxInstances";

    public final String TEMPLATE = "template";
    public final String SERVERS = "servers";
    public final String BALANCER = "balancer";
    public final String DATASOURCENAME = "dataSourceName";
    public final String DATASOURCE = "dataSource";
    public final String ISAUTO = "isAuto";

    public int upperThreshold() throws RemoteException;

    public void setUpperThreshold(int threshold) throws RemoteException;

    public int lowerThreshold() throws RemoteException;

    public void setLowerThreshold(int threshold) throws RemoteException;
}
