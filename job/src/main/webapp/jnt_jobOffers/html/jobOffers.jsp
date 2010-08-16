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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<template:addResources type="css" resources="job.css"/>
<!--start jobsSearchForm -->
<div class="jobsSearchForm">
    <form action="${url.base}${renderContext.mainResource.node.path}.html" method="get">
        <fieldset>
            <legend><fmt:message key='jnt_jobOffers.search.form.label'/></legend>
            <p class="field jobsSearchKeyword">
                <label for="jobsSearchKeyword"><fmt:message key='job.keywordSearch'/></label>
                <input type="text" name="jobsSearchKeyword" id="jobsSearchKeyword" class="field jobsSearchKeyword"
                       value="${param.jobsSearchKeyword}" tabindex="4"/>
            </p>

            <div class="divButton">
                <input type="submit" name="submit" id="submit" class="button" value="<fmt:message key="jnt_jobOffers.search.form.label"/>"
                       tabindex="5"/>
            </div>
        </fieldset>
    </form>
</div>
<jcr:jqom var="results">
    <query:selector nodeTypeName="jnt:job"/>
    <query:childNode path="${currentNode.path}"/>
    <c:if test="${not empty param.jobsSearchKeyword}">
        <query:fullTextSearch searchExpression="${param.jobsSearchKeyword}"
                              propertyName="description"/>
    </c:if>
    <query:sortBy propertyName="jcr:created" order="desc"/>
</jcr:jqom>
<!--stop jobsSearchForm -->
<jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
<c:set var="listTitle" value="${fn:escapeXml(title.string)}"/>
<c:if test="${empty listTitle}"><c:set var="listTitle"><fmt:message key="jobList"/></c:set></c:if>

<h3>${listTitle}</h3>

<table width="100%" class="table" summary="">
    <colgroup>
        <col span="1" width="60%" class="col1"/>
        <col span="1" width="20%" class="col2"/>
        <col span="1" width="20%" class="col3"/>
    </colgroup>
    <thead>
    <tr>
        <th id="job" scope="col"><fmt:message key="web_templates_jobContainer.jobtitle"/></th>
        <th id="location" scope="col"><fmt:message key="web_templates.location"/></th>
        <th id="businessUnit" scope="col"><fmt:message key="web_templates_jobContainer.businessUnit"/></th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${results.nodes}" var="job" varStatus="status">
        <template:module node="${job}" template="list"/>
        <c:if test="${status.last}">
            <template:module node="${job.placeholder}" template="list" editable="true"/>
        </c:if>
    </c:forEach>
    <c:if test="${results.nodes.size == 0 && empty param.jobsSearchKeyword}">
        <c:forEach items="${currentNode.nodes}" var="job">
            <template:module node="${job}" template="list" editable="true"/>
        </c:forEach>
    </c:if>
    <c:if test="${results.nodes.size == 0 && not empty param.jobsSearchKeyword}">
        <tr>
            <td><fmt:message key="jnt_jobOffers.no.job.offers.found"/></td>
        </tr>
    </c:if>
    </tbody>
</table>