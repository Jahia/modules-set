package org.jahia.modules.formbuilder.actions;

import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * User: toto
 * Date: 11/8/11
 * Time: 3:29 PM
 */
public class RedirectAction extends Action {
    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        JCRNodeWrapper node = renderContext.getMainResource().getNode();
        JCRNodeWrapper actionNode = null;
        NodeIterator nodes = node.getParent().getNode("action").getNodes();
        while (nodes.hasNext()) {
            JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) nodes.nextNode();
            if (nodeWrapper.isNodeType("jnt:redirectFormAction")) {
                actionNode = nodeWrapper;
            }
        }
        if (actionNode != null && actionNode.hasProperty("node")) {
            Node n = actionNode.getProperty("node").getNode();
            return new ActionResult(HttpServletResponse.SC_OK, n.getPath());
        }
        return new ActionResult(HttpServletResponse.SC_OK);
    }
}
