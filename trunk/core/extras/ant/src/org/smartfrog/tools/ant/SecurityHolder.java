/** (C) Copyright 2004 Hewlett-Packard Development Company, LP

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


package org.smartfrog.tools.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.SignJar;
import org.apache.tools.ant.types.Reference;

/**
 * Holder class for security for any task that needs it. This
 * keeps all the logic in one place. Simply provide delegate methods
 * to {@link #addSecurity(org.smartfrog.tools.ant.Security)} amd
 * {@link #setSecurityRef(org.apache.tools.ant.types.Reference)}.
 * Call {@link #getSecurity(org.apache.tools.ant.Task)} to get the
 * security reference.
 * Date: 19-Apr-2004
 * Time: 17:11:46
 */
public class SecurityHolder {

    /**
     * optional security element
     */
    private Security security;


    /**
     * reference to security
     */
    private Reference securityRef;

    /**
     * set a reference to the security types
     *
     * @param securityRef
     */
    public void setSecurityRef(Reference securityRef) {
        this.securityRef = securityRef;
    }

    /**
     * set a security definition
     *
     * @param security
     */
    public void addSecurity(Security security) {
        if (this.security != null || this.securityRef != null) {
            throw new BuildException("Multiple security settings");
        }
        this.security = security;
    }

    /**
     * get any security options for this task
     *
     * @param owner owner task
     * @return a security object or null for none defined
     */
    public Security getSecurity(Task owner) {
        if (security != null) {
            return security;
        }
        if (securityRef != null) {
            return Security.resolveReference(owner.getProject(), securityRef);
        }
        return null;
    }

    /**
     * apply whatever security settings are needed
     */
    public boolean applySecuritySettings(SmartFrogTask task) {
        Security sec = getSecurity(task);
        if (sec == null) {
            return false;
        }
        //hand off to the task
        sec.applySecuritySettings(task);
        return true;

    }

}
