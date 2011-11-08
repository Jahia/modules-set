package org.jahia.modules.formbuilder.taglib;

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 11/7/11
 * Time: 6:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class FormFunctions {

    public static Map<String,String> getFormFields(JCRNodeWrapper formNode) throws RepositoryException {
        Map<String, String> m = new LinkedHashMap<String, String>();
        
        NodeIterator ni = formNode.getNode("fieldsets").getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper fieldSet = (JCRNodeWrapper) ni.next();
            List<JCRNodeWrapper> fields = JCRContentUtils.getChildrenOfType(fieldSet, "jnt:formElement");
            for (JCRNodeWrapper field : fields) {
                if (field.isNodeType("jnt:automaticList")) {
                    if (field.hasProperty("type")) {
                        String[] renderers = field.getProperty("type").getString().split("=");
                        String renderer = null;
                        if (renderers.length>0) {
                            renderer = renderers[1];
                        }
                        m.put(field.getName(), renderer);
                    }
                } else if (field.isNodeType("jnt:htmlInput")) {
                    String html = field.getProperty("html").getString();
                    Source source = new Source(html);
                    List<StartTag> inputTags = source.getAllStartTags();
                    for (StartTag inputTag : inputTags) {
                        if ((inputTag.getName().equalsIgnoreCase("input") || inputTag.getName().equalsIgnoreCase("select") || inputTag.getName().equalsIgnoreCase("textarea"))
                                && inputTag.getAttributeValue("name") != null) {
                            m.put(inputTag.getAttributeValue("name"), null);
                        }
                    }
                } else {
                    m.put(field.getName(), null);
                }
            }
        }

        return m;
    }

}
