package org.jahia.modules.poll;

import org.jahia.bin.Action;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.rules.BackgroundAction;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;


public class EndDatePollAction implements BackgroundAction {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(EndDatePollAction.class);

    private String name;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void executeBackgroundAction(JCRNodeWrapper node) {
        try {
            node.setProperty("status","closed");
            node.getSession().save();
        } catch (RepositoryException e) {
            logger.error("Cannot store poll status",e);
        }
    }
}
