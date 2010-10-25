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

package org.jahia.params.valves;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerLDAPProvider;
import org.jahia.services.usermanager.JahiaUserManagerProvider;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.List;

/**
 * User: toto
 * Date: 15 d�c. 2004
 * Time: 13:03:08
 */
public class LemonLdapAuthValveImpl extends BaseAuthValve {
    private static final transient Logger logger = Logger
            .getLogger(LemonLdapAuthValveImpl.class);

    public LemonLdapAuthValveImpl() {
    }

    public void invoke(Object context, ValveContext valveContext) throws PipelineException {

        AuthValveContext authContext = (AuthValveContext) context;
        HttpServletRequest request = authContext.getRequest();
        String auth = request.getHeader("Authorization");
        if (auth != null) {
            try {
                logger.debug("Header found : "+auth);
                auth = auth.substring(6).trim();
                Base64 decoder = new Base64();
                String cred = new String(decoder.decode(auth.getBytes("UTF-8")),"UTF-8");
                int colonInd = cred.indexOf(':');
                String dn = cred.substring(0,colonInd);
                String prof = cred.substring(colonInd+1);

                logger.debug("Looking for dn "+dn);

                List<? extends JahiaUserManagerProvider> v = ServicesRegistry.getInstance().getJahiaUserManagerService().getProviderList();
                for (Iterator<? extends JahiaUserManagerProvider> iterator = v.iterator(); iterator.hasNext();) {
                    JahiaUserManagerProvider userManagerProviderBean = iterator.next();
                    if (userManagerProviderBean.getClass().getName().equals(JahiaUserManagerLDAPProvider.class.getName())) {
                        JahiaUserManagerLDAPProvider jahiaUserManagerLDAPProvider = (JahiaUserManagerLDAPProvider)userManagerProviderBean;
                        JahiaUser jahiaUser = jahiaUserManagerLDAPProvider.lookupUserFromDN(dn);
                        if (jahiaUser != null) {
                            if (isAccounteLocked(jahiaUser)) {
                                logger.debug("Login failed. Account is locked for user " + dn);
                                return;
                            }
                            logger.debug("DN found in ldap provider, user authenticated");
                            authContext.getSessionFactory().setCurrentUser(jahiaUser);
                            request.setAttribute("lemonldap.profile", prof);
                        }
                        return;
                    }
                }
            } catch (Exception e) {
                logger.debug("Exception thrown",e);
            }
        } else {
            logger.debug("No authorization header found");
        }
        valveContext.invokeNext(context);
    }

    public void initialize() {
    }
}
