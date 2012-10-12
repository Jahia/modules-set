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

package org.jahia.modules.formbuilder.flow;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.render.URLResolverFactory;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.*;

public class CustomFormFlowHandler implements Serializable {

    private static final long serialVersionUID = -4761267217553714173L;
    
    private String workspace;
    private Locale locale;
    private String formUuid;
    private List<String> fieldSets;
    private int currentFieldSetIndex;

    public void init(JCRNodeWrapper form, HttpServletRequest request) {
        try {
            HashMap<String, List<String>> formDatas = new HashMap<String, List<String>>();
            request.getSession(true).setAttribute("formDatas", formDatas);
            formDatas.put("jcrNodeType", Arrays.asList("jnt:responseToForm"));

            formUuid = form.getIdentifier();
            fieldSets = new ArrayList<String>();
            NodeIterator ni = form.getNode("fieldsets").getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
                if (next.isNodeType("jnt:fieldset")) {
                    fieldSets.add(next.getIdentifier());
                }
            }
            currentFieldSetIndex = 0;

            workspace = form.getSession().getWorkspace().getName();
            locale = form.getSession().getLocale();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    public void goToPreviousFieldSet() {
        if (currentFieldSetIndex > 0) {
            currentFieldSetIndex --;
        }
    }

    public JCRNodeWrapper getPreviousFieldSet() {
        try {
            if (currentFieldSetIndex > 0) {
                return JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale).getNodeByIdentifier(fieldSets.get(currentFieldSetIndex-1));
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void goToNextFieldSet() {
        if (currentFieldSetIndex < fieldSets.size() - 1) {
            currentFieldSetIndex ++;
        }
    }

    public JCRNodeWrapper getNextFieldSet() {
        try {
            if (currentFieldSetIndex < fieldSets.size() - 1) {
                return JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale).getNodeByIdentifier(fieldSets.get(currentFieldSetIndex+1));
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JCRNodeWrapper getCurrentFieldSet() {
        try {
            if (fieldSets.size() > 0) {
                return JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale).getNodeByIdentifier(fieldSets.get(currentFieldSetIndex));
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveValues(HttpServletRequest request) {
        Map<String,List<String>> formDatas = (Map<String, List<String>>) request.getSession().getAttribute("formDatas");

        try {
            JCRNodeWrapper fieldSet = JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale).getNodeByIdentifier(fieldSets.get(currentFieldSetIndex));
            NodeIterator ni =  fieldSet.getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper formElement = (JCRNodeWrapper) ni.next();
                if (formElement.isNodeType("jnt:formElement") && request.getParameterValues(formElement.getName()) != null) {
                    formDatas.put(formElement.getName(), Arrays.asList(request.getParameterValues(formElement.getName())));
                }
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    public String executeActions(HttpServletRequest request) {
        try {
            JCRNodeWrapper action = JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale).getNodeByIdentifier(formUuid).getNode("action");

            NodeIterator n = action.getNodes();
            while (n.hasNext()) {
                JCRNodeWrapper actionNode = (JCRNodeWrapper) n.next();
                String actionName = actionNode.getProperty("j:action").getString();
                ActionResult r = callAction(request, actionName);
            }
        } catch (RepositoryException e) {

        }
        return "ok";
    }

    public ActionResult callAction(HttpServletRequest request, String actionName) {
        try {
            Action action = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getActions().get(actionName);
            RenderContext renderContext = (RenderContext) request.getAttribute("renderContext");
            Map<String,List<String>> formDatas = (Map<String, List<String>>) request.getSession().getAttribute("formDatas");
            Resource mainResource = (Resource) request.getAttribute("currentResource");
            Resource resource = new Resource(mainResource.getNode().getNode("responses"), mainResource.getTemplateType(), actionName, Resource.CONFIGURATION_PAGE);
            URLResolver mainResolver = (URLResolver) request.getAttribute("urlResolver");
            String urlPathInfo = StringUtils.substringBefore(mainResolver.getUrlPathInfo(), mainResolver.getPath()) + resource.getNode().getPath();
            if (!actionName.equals("default")) {
                urlPathInfo += "."+ actionName + ".do";
            } else {
                urlPathInfo += "/*";
            }

            URLResolverFactory f = (URLResolverFactory) SpringContextSingleton.getBean("urlResolverFactory");
            URLResolver resolver = f.createURLResolver(urlPathInfo, request.getServerName(), request);
            return action.doExecute(request, renderContext, resource, JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale), formDatas, resolver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

