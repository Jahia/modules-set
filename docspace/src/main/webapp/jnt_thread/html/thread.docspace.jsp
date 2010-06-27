<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<template:addResources type="javascript" resources="jquery.min.js,jquery.cuteTime.js"/>
<script>
    function initCuteTime() {
        $('.timestamp').cuteTime({ refresh: 60000 });
    }
    $(document).ready(function () {
        $('.timestamp').cuteTime({ refresh: 60000 });
    });
</script>

<ul class="docspacelistcomment" id="${currentNode.UUID}">
    <c:set var="currentList" value="${currentNode.nodes}" scope="request"/>
    <c:set var="listTotalSize" value="${fn:length(currentNode.nodes)}" scope="request"/>
    <template:option node="${currentNode}" nodetype="jmix:pager" template="hidden.init"/>
    <template:option node="${currentNode}" nodetype="jmix:pager" template="hidden.end">
        <template:param name="callback" value="initCuteTime();"/>
    </template:option>
    <c:forEach items="${currentList}" var="subchild" varStatus="status" begin="${begin}" end="${end}">
        <template:module node="${subchild}" template="docspace"/>
    </c:forEach>
    <template:option node="${currentNode}" nodetype="jmix:pager" template="hidden.end"/>
</ul>
<template:removePager id="${currentNode.identifier}"/>

