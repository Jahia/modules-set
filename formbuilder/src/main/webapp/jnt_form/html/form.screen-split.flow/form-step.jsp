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
<jcr:node var="actionNode" path="${currentNode.path}/action"/>
<jcr:node var="fieldsetsNode" path="${currentNode.path}/fieldsets"/>
<jcr:node var="formButtonsNode" path="${currentNode.path}/formButtons"/>

<c:if test="${not renderContext.editMode}">

<template:addCacheDependency node="${actionNode}"/>
<template:addCacheDependency node="${fieldsetsNode}"/>
<template:addCacheDependency node="${formButtonsNode}"/>
<c:set var="writeable" value="${currentResource.workspace eq 'live'}" />
<c:if test='${not writeable}'>
    <c:set var="disabled" value='disabled="true"' scope="request" />
</c:if>
<c:if test="${not renderContext.editMode}">
    <template:addResources>
        <script type="text/javascript">
            $(document).ready(function() {
                $("\#${currentNode.name}").validate({
                    rules: {
                        <c:forEach items="${jcr:getNodes(currentFieldSet,'jnt:formElement')}" var="formElement" varStatus="status">
                        <c:set var="validations" value="${jcr:getNodes(formElement,'jnt:formElementValidation')}"/>
                        <c:if test="${fn:length(validations) > 0}">
                        '${formElement.name}' : {
                            <c:forEach items="${jcr:getNodes(formElement,'jnt:formElementValidation')}" var="formElementValidation" varStatus="val">
                            <template:module node="${formElementValidation}" view="default" editable="true"/><c:if test="${not val.last}">,</c:if>
                            </c:forEach>
                        }<c:if test="${not status.last}">,</c:if>
                        </c:if>
                        </c:forEach>
                    },formId : "${currentNode.name}"
                });
            });
        </script>
    </template:addResources>
</c:if>
<c:set var="displayCSV" value="false"/>
<c:set var="action" value="${url.base}${currentNode.path}/responses/*"/>
<c:if test="${not empty actionNode.nodes}">
    <c:if test="${fn:length(actionNode.nodes) > 1}">
        <c:set var="action" value="${url.base}${currentNode.path}/responses.chain.do"/>
        <c:set var="chainActive" value=""/>
        <c:forEach items="${actionNode.nodes}" var="node" varStatus="stat">
            <c:if test="${jcr:isNodeType(node, 'jnt:defaultFormAction')}"><c:set var="displayCSV" value="true"/></c:if>
            <c:if test="${jcr:isNodeType(node, 'jnt:redirectFormAction')}"><c:set var="hasRedirect" value="true"/></c:if>
            <c:set var="chainActive" value="${chainActive}${node.properties['j:action'].string}"/>
            <c:if test="${not stat.last}"><c:set var="chainActive" value="${chainActive},"/></c:if>
        </c:forEach>
    </c:if>
    <c:if test="${fn:length(actionNode.nodes) eq 1}">
        <c:forEach items="${actionNode.nodes}" var="node">
            <c:if test="${jcr:isNodeType(node, 'jnt:defaultFormAction')}"><c:set var="displayCSV" value="true"/></c:if>
            <c:if test="${node.properties['j:action'].string != 'default'}">
                <c:set var="action" value="${url.base}${currentNode.path}/responses.${node.properties['j:action'].string}.do"/>
            </c:if>
        </c:forEach>
    </c:if>
</c:if>

<h2><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h2>


<div class="intro">
    ${currentNode.properties['j:intro'].string}
</div>

<div class="Form FormBuilder">

        <template:tokenizedForm>
            <form action="${flowExecutionUrl}" method="post" id="${currentNode.name}">
                <template:module node="${currentFieldSet}" editable="true"/>


                <div class="divButton">
                    <%--<button id="cancel" type="submit" name="_eventId_cancel">Cancel</button>--%>
                    <c:if test="${not empty previousFieldSet}">
                        <button id="previous" type="submit" name="_eventId_previous">&lt;&lt; Previous</button>
                    </c:if>
                    <c:if test="${not empty nextFieldSet}">
                        <button id="next" type="submit" name="_eventId_next">Next &gt;&gt;</button>
                    </c:if>
                    <c:if test="${empty nextFieldSet}">
                        <button id="finish" type="submit" name="_eventId_finish">Finish &gt;&gt;</button>
                    </c:if>
                    <%--<c:forEach items="${formButtonsNode.nodes}" var="formButton">--%>
                        <%--<template:module node="${formButton}" editable="true"/>--%>
                    <%--</c:forEach>--%>
                </div>
                <div class="validation"></div>
            </form>
        </template:tokenizedForm>
</div>
<br/><br/>
</c:if>
<c:if test="${renderContext.editMode}">
    <template:include view="default"/>
</c:if>
