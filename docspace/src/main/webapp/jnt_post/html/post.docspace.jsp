<%@ page contentType="text/html; UTF-8" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jcr:nodeProperty node="${currentNode}" name="jcr:createdBy" var="createdBy"/>
<jcr:nodeProperty node="${currentNode}" name="content" var="content"/>
<jcr:node var="userNode" path="/users/${createdBy.string}"/>
<li class="docspaceitemcomment">
    <!--<span class="public floatright"><input name="" type="checkbox" value=""/> <fmt:message key="docspace.label.public"/></span>-->

    <div class="image">
        <div class="itemImage itemImageLeft">

            <jcr:nodeProperty var="picture" node="${userNode}" name="j:picture"/>
            <c:if test="${not empty picture}">
                <a href="${url.base}${renderContext.site.path}/users/${createdBy.string}.html"><img
                        src="${picture.node.thumbnailUrls['avatar_60']}"
                        alt="${userNode.properties.title.string} ${userNode.properties.firstname.string} ${userNode.properties.lastname.string}"
                        width="60"
                        height="60"/></a>
            </c:if>
            <c:if test="${empty picture}"><a href="${url.base}${renderContext.site.path}/users/${createdBy.string}.html"><img alt=""
                                                                                                    src="${url.currentModule}/css/img/userbig.png"/></a></c:if>
        </div>
    </div>

    <h5 class="title"><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h5>
    <jcr:nodeProperty node="${currentNode}" name="jcr:lastModified" var="lastModified"/>
    <span class="docspacedate timestamp"><fmt:formatDate value="${lastModified.time}"
                                                         pattern="yyyy/MM/dd HH:mm"/></span>

    <p>
        <span class="author">
        <a href="${url.base}${renderContext.site.path}/users/${createdBy.string}.html">${createdBy.string}</a>:&nbsp;</span>
        ${content.string}
    </p>

    <div class='clear'></div>
</li>