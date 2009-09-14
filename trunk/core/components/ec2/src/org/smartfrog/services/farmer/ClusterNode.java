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

package org.smartfrog.services.farmer;

import java.io.Serializable;

/**
 * Serializable representation of a cluster node
 */
public final class ClusterNode implements Serializable {

    private String id;
    private String hostname;
    private boolean externallyVisible;
    private String externalHostname;
    private String role;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public boolean isExternallyVisible() {
        return externallyVisible;
    }

    public void setExternallyVisible(boolean externallyVisible) {
        this.externallyVisible = externallyVisible;
    }

    public String getExternalHostname() {
        return externalHostname;
    }

    public void setExternalHostname(String externalHostname) {
        this.externalHostname = externalHostname;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClusterNode that = (ClusterNode) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    /**
     * Hash code comes from the ID
     * @return the hash code's ID
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    /**
     * {@inheritDoc}
     * @return a clone of this instance.
     *
     * @throws CloneNotSupportedException as the signature demands it. 
     * @see Cloneable
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "ClusterInstance{" +
                "id='" + id + '\'' +
                ", hostname='" + hostname + '\'' +
                ", externallyVisible=" + externallyVisible +
                ", externalHostname='" + externalHostname + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
