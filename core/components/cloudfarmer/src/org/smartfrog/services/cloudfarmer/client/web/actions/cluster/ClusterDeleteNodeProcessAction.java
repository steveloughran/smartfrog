/* (C) Copyright 2009 Hewlett-Packard Development Company, LP

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

package org.smartfrog.services.cloudfarmer.client.web.actions.cluster;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.smartfrog.services.cloudfarmer.client.web.exceptions.BadParameterException;
import org.smartfrog.services.cloudfarmer.client.web.model.cluster.ClusterController;
import org.smartfrog.services.cloudfarmer.client.web.model.cluster.HostInstance;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Delete a node
 */
@SuppressWarnings({"RefusedBequest"})
public class ClusterDeleteNodeProcessAction extends AbstractClusterAction {

    /**
     * Get the name of this action, used in logging and debugging
     *
     * @return the name
     */

    @Override
    protected String getActionName() {
        return "ClusterDeleteNode";
    }

    @SuppressWarnings({"ProhibitedExceptionDeclared"})
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                 HttpServletResponse response, ClusterController controller) throws Exception {
        String hostID = parameterToNonEmptyStringAttribute(request, ATTR_HOSTID, ATTR_HOSTID);
        log.info("Deleting a named node " + hostID);
        //refresh the lists
        controller.refreshHostList();
        HostInstance instance = controller.lookupHost(hostID);
        if (instance==null) {
            throw new BadParameterException(new ActionMessage("error.hostNotFound", hostID).toString());
        }
        String hostname = instance.getHostname();
        try {
            controller.deleteHost(hostID);
            //refresh the lists
            controller.refreshHostList();
            addClusterAttributes(request, controller);
            return success(mapping);
        } catch (Exception e) {
            addClusterAttributes(request, controller);
            return failure(request, mapping, "Failed to delete the host " + hostname + " : " + e, e);
        }

    }

}