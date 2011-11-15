<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="formbuilder" uri="http://www.jahia.org/tags/formbuilder" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<c:set value="${formbuilder:getFormFields(currentNode.parent)}" var="formFields" scope="request"/>
<template:include view="hidden.header"/>
<table class="table">
    <tr>
        <th><fmt:message key="label.date"/></th>
        <th><fmt:message key="label.user"/></th>
        <%--<th><fmt:message key="label.url"/></th>--%>
        <c:forEach items="${formFields}" var="formField">
        <th>${formField.key}</th>
    </c:forEach>
    </tr>

    <c:forEach items="${moduleMap.currentList}" var="subResponseNode" begin="${moduleMap.begin}" end="${moduleMap.end}">
        <template:module node="${subResponseNode}" view="default"/>
    </c:forEach>
</table>
<h2><fmt:message key="label.download"/> <a href="<c:url value='${url.base}${currentNode.path}.csv'/>">CSV</a></h2>
<template:include view="hidden.footer"/>
