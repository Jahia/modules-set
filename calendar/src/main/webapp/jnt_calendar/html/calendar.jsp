<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="propertyDefinition" type="org.jahia.services.content.nodetypes.ExtendedPropertyDefinition"--%>
<%--@elvariable id="type" type="org.jahia.services.content.nodetypes.ExtendedNodeType"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<jsp:useBean id="datas" class="java.util.LinkedHashMap"/>
<template:addResources type="css" resources="fullcalendar.css"/>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="fullcalendar.js"/>

<c:set var="linked" value="${uiComponents:getBindedComponent(currentNode, renderContext, 'j:bindedComponent')}"/>

<c:forEach items="${linked.nodes}" var="linkedChild" varStatus="status">
    <fmt:formatDate pattern="yyyy-MM-dd" value="${linkedChild.properties[currentNode.properties.startDateProperty.string].date.time}" var="startDate"/>
    <c:choose>
        <c:when test="${empty datas[startDate]}">
            <c:set target="${datas}" property="${startDate}" value="1"/>
        </c:when>
        <c:otherwise>
            <c:set target="${datas}" property="${startDate}" value="${datas[startDate]+1}"/>
        </c:otherwise>
    </c:choose>
</c:forEach>
<template:addResources type="inlinejavascript" key="${renderContext.mainResource.node.identifier}">
    $(document).ready(function() {
    // page is now ready, initialize the calendar...
    $('#calendar${currentNode.identifier}').fullCalendar({
    <c:if test="${not empty param.calStartDate}">
        year : ${fn:substring(param.calStartDate,0,4)},
        month : (${fn:substring(param.calStartDate,5,7)}-1),
        date : ${fn:substring(param.calStartDate,8,10)},
    </c:if>
    events: [
    <c:forEach items="${datas}" var="data" varStatus="status">
        <c:if test="${not status.first}">,</c:if>
        {
        title : '${data.value}',
        start : '${data.key}',
        url : '${url.base}${renderContext.mainResource.node.path}.html?filter={name:"${currentNode.properties.startDateProperty.string}",value:"${data.key}",op:"eq",uuid:"${linked.identifier}",format:"yyyy-MM-dd",type:"date"}&calStartDate=${data.key}'
        }
    </c:forEach>
    ]
    })
    });
</template:addResources>
<div class="calendar" id="calendar${currentNode.identifier}"></div>
<template:linker property="j:bindedComponent" />
