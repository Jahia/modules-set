package org.jahia.modules.rating.actions;

import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.bin.Render;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.json.JSONException;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * User: rincevent
 * Date: 9/14/11   ;
 * Time: 2:15 PM
 */
public class RateContent extends Action {
    JCRTemplate jcrTemplate;

    public void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, final Resource resource, JCRSessionWrapper session, final Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        return (ActionResult) jcrTemplate.doExecuteWithSystemSession(null,session.getWorkspace().getName(),session.getLocale(),new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JCRNodeWrapper node = session.getNodeByUUID(resource.getNode().getIdentifier());
                if (!node.isNodeType("jmix:rating")) {
                    session.checkout(node);
                    node.addMixin("jmix:rating");
                    session.save();
                }
                List<String> values = parameters.get("j:lastVote");
                node.setProperty("j:lastVote", values.get(0));
                node.setProperty("j:nbOfVotes",node.getProperty("j:nbOfVotes").getLong()+1);
                node.setProperty("j:sumOfVotes",node.getProperty("j:sumOfVotes").getLong()+ Long.valueOf(values.get(0)));
                session.save();
                try {
                    return new ActionResult(HttpServletResponse.SC_OK, node.getPath(), Render.serializeNodeToJSON(node));
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (JSONException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                return null;
            }
        });
    }
}
