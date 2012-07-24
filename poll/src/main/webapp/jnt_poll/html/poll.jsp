<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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
<jsp:useBean id="now" class="java.util.Date"/>
<template:addResources type="javascript" resources="jquery.min.js,poll.js"/>
<template:addResources type="css" resources="poll.css"/>
<jcr:node path="${renderContext.user.localPath}" var="user"/>
<template:addResources>

<script type="text/javascript">
    jQuery.ajaxSettings.traditional = true;
    $.ajaxSetup({
        accepts: {
            script: "application/json"
        },
        cache:false
    });

    if (getCookie('poll${user.identifier}${currentNode.identifier}') == 'true') {
        displayResults("<c:url value='${url.base}${currentNode.path}'/>", '${currentNode.identifier}');
    }

    <c:if test="${not renderContext.editMode}">
        <c:if test="${currentNode.properties.status.string eq 'closed'}">
            displayResults("<c:url value='${url.base}${currentNode.path}'/>", '${currentNode.identifier}');
        </c:if>
    </c:if>
</script>
</template:addResources>
<div class=poll>
    <h3>
        ${fn:escapeXml(currentNode.propertiesAsString['question'])}
    </h3>

    <c:if test="${currentNode.properties.status.string ne 'closed'}">
    <div class="pollForm${currentNode.identifier}">
      <c:choose>
        <c:when test="${renderContext.editMode}">
          <div class="addanswers">
            <span><fmt:message key="jnt_poll.addAnswers"/></span>
            <template:list path="answers" listType="jnt:answersList" editable="true"/>
          </div>
        </c:when>
        <c:otherwise>
          <div id="formContainer_${currentNode.identifier}">
            <template:tokenizedForm>
              <form id="tokenform_${currentNode.identifier}" name="tokenform_${currentNode.identifier}" method="post" action="<c:url value="${url.base}${currentNode.path}"/>.pollVote.do">
              </form>
            </template:tokenizedForm>  
            <form id="form_${currentNode.identifier}" name="form_${currentNode.identifier}" method="post" >
                <input type="hidden" name="jcrReturnContentType" value="json"/>        
                <template:list path="answers" listType="jnt:answersList" editable="true"/>
                <div class="validation"></div>
                <input class="button" type="button" value="<fmt:message key='jnt_poll.vote'/>" onclick="this.disabled = true;doVote($('${currentNode.identifier}_voteAnswer').value, '<c:url value="${url.base}${currentNode.path}"/>','${currentNode.identifier}', '<c:url value="${url.context}${url.base}${renderContext.site.path}"/>','${user.identifier}');" />
            </form>

          </div>
        </c:otherwise>
      </c:choose>   
    </div>
    </c:if>

    <div class="stats_${currentNode.identifier}">

    </div>
</div>
