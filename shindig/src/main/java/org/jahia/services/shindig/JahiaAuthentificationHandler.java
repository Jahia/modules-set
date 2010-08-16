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

package org.jahia.services.shindig;

import org.apache.log4j.Logger;
import org.apache.shindig.auth.AuthenticationHandler;
import org.apache.shindig.auth.SecurityToken;
import org.jahia.bin.Jahia;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.JahiaUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Shindig authentification handler for Jahia.
 *
 * @author loom
 *         Date: Jun 24, 2010
 *         Time: 11:15:14 AM
 */
public class JahiaAuthentificationHandler implements AuthenticationHandler {

    private static final transient Logger logger = Logger.getLogger(JahiaAuthentificationHandler.class);


    public static final String AUTHENTICATION_HANDLER_NAME = "Jahia";

    public String getName() {
        return AUTHENTICATION_HANDLER_NAME;
    }

    public SecurityToken getSecurityTokenFromRequest(HttpServletRequest request) throws InvalidAuthenticationException {
        JahiaUser jahiaUser = null;
        if (Jahia.isInitiated()) {
            HttpSession session = request.getSession(false);
            if (session !=null) {
                jahiaUser = (JahiaUser) session.getAttribute(ProcessingContext.SESSION_USER);
            }
            if (jahiaUser != null) {
                jahiaUser =
                        ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(jahiaUser.getUserKey());
            }
        }
        if (jahiaUser != null) {
            return new JahiaSecurityToken(jahiaUser);
        }
        return null;
    }

    public String getWWWAuthenticateHeader(String realm) {
        return null;
    }
}
