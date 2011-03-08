<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<template:addResources type="css" resources="faq.css"/>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="acl" type="java.lang.String"--%>
<template:addResources type="css" resources="summary.css"/>
<c:set var="bindedComponent"
       value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>
<jcr:nodeProperty node="${currentNode}" name="startDate" var="startDate"/>
<jcr:nodeProperty node="${currentNode}" name="endDate" var="endDate"/>

<div class="summary">
    <h3><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h3>
    <ol>

        <c:forEach items="${bindedComponent.nodes}" var="subchild">
            <c:choose>
                <c:when test="${not empty startDate && empty endDate}">
                    <c:if test="${subchild.properties['jcr:created'].time > startDate.time}">
                        <template:module node="${subchild}" template="hidden.summaryEl"/>
                    </c:if>
                </c:when>
                <c:when test="${empty endDate && not empty endDate}">
                    <c:if test="${subchild.properties['jcr:created'].time > startDate.time}">
                        <template:module node="${subchild}" template="hidden.summaryEl"/>
                    </c:if>
                </c:when>
                <c:when test="${not empty endDate && not empty endDate}">
                    <c:if test="${subchild.properties['jcr:created'].time > startDate.time && subchild.properties['jcr:created'].time < endDate.time}">
                        <template:module node="${subchild}" template="hidden.summaryEl"/>
                    </c:if>
                </c:when>
                <c:otherwise>
                    <template:module node="${subchild}" template="hidden.summaryEl"/>
                </c:otherwise>
            </c:choose>
        </c:forEach>
    </ol>
    <c:if test="${renderContext.editMode}"><p><fmt:message key="label.warningList"/></p></c:if>
</div>
