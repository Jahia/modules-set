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
<template:addResources type="javascript" resources="jquery.min.js,jquery.validate.js,jquery.maskedinput.js"/>
<template:addResources type="css" resources="formbuilder.css"/>
<tr>
    <td>
        <fmt:formatDate value="${currentNode.properties['jcr:created'].date.time}" pattern="yyyy-MM-dd HH:mm"/>
    </td>
    <td>
        ${currentNode.properties['jcr:createdBy'].string}
    </td>
    <%--<td>--%>
        <%--${currentNode.properties['originUrl'].string}--%>
    <%--</td>--%>
    <c:forEach items="${formFields}" var="formField">
        <td>
            <c:if test="${not empty formField.value}">
                <jcr:nodePropertyRenderer node="${currentNode}"
                                          name="${formField.key}"
                                          renderer="${formField.value}"/>
            </c:if>
            <c:if test="${empty formField.value}">
                <jcr:nodeProperty node="${currentNode}" name="${formField.key}"/>
            </c:if>
        </td>
    </c:forEach>
</tr>