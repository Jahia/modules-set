<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="acl" type="java.lang.String"--%>
<jcr:nodeProperty node="${currentNode}" name="j:nbOfVotes" var="nbVotes"/>
<jcr:nodeProperty node="${currentNode}" name="j:sumOfVotes" var="sumVotes"/>
<c:set var="positiveVote" value="0"/>
<c:set var="negativeVote" value="0"/>
<c:if test="${nbVotes.long > 0}">
    <c:if test="${sumVotes.long > 0}">
        <c:set var="positiveVote" value="${((sumVotes.long)+(nbVotes.long - sumVotes.long)/2)}"/>
        <c:set var="negativeVote" value="${(nbVotes.long - sumVotes.long)/2}"/>
    </c:if>
    <c:if test="${sumVotes.long == 0}">
        <c:set var="positiveVote" value="${nbVotes.long/2}"/>
        <c:set var="negativeVote" value="${nbVotes.long/2}"/>
    </c:if>
    <c:if test="${sumVotes.long < 0}">
        <c:set var="positiveVote" value="${(nbVotes.long + sumVotes.long)/2}"/>
        <c:set var="negativeVote" value="${((-sumVotes.long)+(nbVotes.long + sumVotes.long)/2)}"/>
    </c:if>
</c:if>
<a title="Vote +1" href="#"
   onclick="document.getElementById('jahia-forum-post-vote-${currentNode.identifier}').submit();"><span>+1 (<fmt:formatNumber value="${positiveVote}" pattern="##"/> Good)</span></a>
<a title="Vote -1" href="#"
   onclick="var voteForm=document.getElementById('jahia-forum-post-vote-${currentNode.identifier}'); voteForm.elements['j:lastVote'].value='-1'; voteForm.submit();"><span>-1 (<fmt:formatNumber value="${negativeVote}" pattern="##"/>  Bad)</span></a>