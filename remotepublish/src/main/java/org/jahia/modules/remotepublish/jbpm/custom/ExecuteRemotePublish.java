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

package org.jahia.modules.remotepublish.jbpm.custom;

import org.apache.log4j.Logger;
import org.jahia.modules.remotepublish.RemotePublicationService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.workflow.WorkflowVariable;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.activity.ExternalActivityBehaviour;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Publish custom activity for jBPM workflow
 * <p/>
 * Publish the current node
 */
public class ExecuteRemotePublish implements ExternalActivityBehaviour {
    private static final long serialVersionUID = 1L;
    private static transient Logger logger = Logger.getLogger(ExecuteRemotePublish.class);

    public void execute(ActivityExecution execution) throws Exception {
        List<WorkflowVariable> wfVar = (List<WorkflowVariable>) execution.getVariable("remotePublicationToExecute");
        if (wfVar != null) {
            String id = wfVar.get(0).getValue();
            String workspace = (String) execution.getVariable("workspace");
            Locale locale = (Locale) execution.getVariable("locale");

            JCRNodeWrapper node = JCRSessionFactory.getInstance().getCurrentUserSession().getNodeByUUID(id);
            RemotePublicationService.getInstance().executeRemotePublication(node);
            execution.takeDefaultTransition();
        }
    }

    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception {
    }

}