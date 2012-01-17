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

package org.jahia.modules.bookmarks;

import org.jahia.ajax.gwt.helper.ContentManagerHelper;
import org.jahia.bin.ActionResult;
import org.jahia.bin.Action;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.json.JSONObject;

import javax.jcr.PathNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: May 12, 2010
 * Time: 4:06:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class AddAction extends Action {
    private ContentManagerHelper contentManager;
    final String bookmarkPath = "bookmarks";

    public void setContentManager(ContentManagerHelper contentManager) {
        this.contentManager = contentManager;
    }

    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        // test if bookmark node is present
        JCRSessionWrapper jcrSessionWrapper = JCRSessionFactory.getInstance().getCurrentUserSession(resource.getWorkspace(), resource.getLocale());
        JCRNodeWrapper userBookmarks = null;
        String userPath = renderContext.getUser().getLocalPath();
        try {
            userBookmarks = jcrSessionWrapper.getNode(userPath + "/" + bookmarkPath);
        } catch (PathNotFoundException pnf) {
            userBookmarks =  contentManager.addNode(jcrSessionWrapper.getNode(userPath), bookmarkPath, "jnt:bookmarks", null, null,Locale.getDefault());
            userBookmarks.saveSession();
        }
        if (userBookmarks != null &&  !contentManager.checkExistence(userBookmarks.getPath() + "/" + req.getParameter("jcr:title").replace(" ","-"), jcrSessionWrapper, Locale.getDefault())) {
            JCRNodeWrapper bookmark = contentManager.addNode(userBookmarks, req.getParameter("jcr:title").replace(" ","-"), "jnt:bookmark", null, null, Locale.getDefault());
            bookmark.setProperty("date", new GregorianCalendar());
            if (req.getParameter("url") != null) { bookmark.setProperty("url", req.getParameter("url")); }
            bookmark.setProperty("jcr:title", req.getParameter("jcr:title"));
            bookmark.saveSession();
        }
        return new ActionResult(HttpServletResponse.SC_OK, null, new JSONObject());
    }
}
