/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.modules.userregistration.actions;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.bin.Jahia;
import org.jahia.bin.Render;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.mail.MailService;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLGenerator;
import org.jahia.services.render.URLResolver;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static javax.servlet.http.HttpServletResponse.SC_OK;

/**
 * Action handler for sending a password recovery e-mail. The e-mail body contains a link to a page with the
 * password recovery form.
 *
 * @author : qlamerand
 * @since : JAHIA 6.6
 */
public class RecoverPassword extends Action {

    private static final Logger logger = LoggerFactory.getLogger(RecoverPassword.class);

    private JahiaUserManagerService userManagerService;

    private MailService mailService;

    private String templatePath;


    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource,
                                  JCRSessionWrapper session, Map<String, List<String>> parameters,
                                  URLResolver urlResolver) throws Exception {
        String username = getParameter(parameters, "username");
        if (StringUtils.isEmpty(username)) {
            return ActionResult.BAD_REQUEST;
        }

        if (req.getSession().getAttribute("passwordRecoveryAsked") != null) {
            JSONObject json = new JSONObject();
            String message = JahiaResourceBundle.getString("JahiaUserRegistration", "passwordrecovery.mail.alreadysent",
                    resource.getLocale(), "Jahia User Registration");
            json.put("message", message);
            return new ActionResult(HttpServletResponse.SC_OK, null, json);
        }

        JahiaUser user = userManagerService.lookupUser(username);
        if (user == null ) {
            JSONObject json = new JSONObject();
            String message = JahiaResourceBundle.getString("JahiaUserRegistration", "passwordrecovery.username.invalid",
                    resource.getLocale(), "Jahia User Registration");
            json.put("message", message);
            return new ActionResult(SC_OK, null, json);
        }

        String to = user.getProperty("j:email");
        if (to == null || !MailService.isValidEmailAddress(to, false)) {
            JSONObject json = new JSONObject();
            String message = JahiaResourceBundle.getString("JahiaUserRegistration", "passwordrecovery.mail.invalid",
                    resource.getLocale(), "Jahia User Registration");
            json.put("message", message);
            return new ActionResult(SC_OK, null, json);
        }
        String from = SettingsBean.getInstance().getMail_from();

        String authKey = DigestUtils.md5Hex(username + System.currentTimeMillis());
        req.getSession().setAttribute(authKey, user.getLocalPath());

        Map<String,Object> bindings = new HashMap<String,Object>();
        bindings.put("link", req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() +
                Jahia.getContextPath() + Render.getRenderServletPath() + "/live/"
                + resource.getLocale().getLanguage() + resource.getNode().getPath() + ".html?key=" + authKey);
        bindings.put("user", user);

        mailService.sendMessageWithTemplate(templatePath, bindings, to, from, null, null, resource.getLocale(), "Jahia User Registration");
        req.getSession().setAttribute("passwordRecoveryAsked", true);

        JSONObject json = new JSONObject();
        String message = JahiaResourceBundle.getString("JahiaUserRegistration", "passwordrecovery.mail.sent",
                resource.getLocale(), "Jahia User Registration");
        json.put("message", message);
        return new ActionResult(HttpServletResponse.SC_ACCEPTED, null, json);
    }

    public void setUserManagerService(JahiaUserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }

    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }
}
