/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.rbac.jcr;

import org.jahia.services.rbac.EnforcementPolicy;
import org.jahia.services.rbac.jcr.RoleBasedAccessControlService;
import org.springframework.beans.factory.InitializingBean;

/**
 * Enables the Service RoleBasedAccessControlService by injecting the an
 * enforcement policy.
 * 
 * @author Sergiy Shyrkov
 * @since 6.5
 */
public class RoleBasedAccessControlServiceEnabler implements InitializingBean {

    private EnforcementPolicy policy = new EnforcementPolicy();

    private RoleBasedAccessControlService rbacService;

    public void afterPropertiesSet() throws Exception {
        // override the default policy
        rbacService.setEnformcementPolicy(policy);
    }

    /**
     * Injects an instance of the {@link RoleBasedAccessControlService} service.
     * 
     * @param rbacService an instance of the
     *            {@link RoleBasedAccessControlService} service
     */
    public void setRoleBasedAccessControlService(RoleBasedAccessControlService rbacService) {
        this.rbacService = rbacService;
    }

    /**
     * Injects the enforcement policy to use.
     * 
     * @param policy the policy to set
     */
    public void setEnformcementPolicy(EnforcementPolicy policy) {
        this.policy = policy;
    }

}
