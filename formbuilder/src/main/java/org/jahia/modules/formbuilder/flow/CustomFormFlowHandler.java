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

