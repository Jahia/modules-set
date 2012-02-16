<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:useBean id="now" class="java.util.Date"/>
<template:addResources type="javascript" resources="jquery.min.js,poll.js"/>
<template:addResources type="css" resources="poll.css"/>

<template:addResources>

<script type="text/javascript">
    jQuery.ajaxSettings.traditional = true;
    $.ajaxSetup({
        accepts: {
            script: "application/json"
        },
        cache:false
    });

    if (getCookie('poll${currentNode.identifier}') == 'true') {
        displayResults("<c:url value='${url.base}${currentNode.path}'/>", '${currentNode.identifier}');
    }

    <c:if test="${not renderContext.editMode}">
        <c:if test="${now.time > currentNode.properties.endDate.time.time}">
            displayResults("<c:url value='${url.base}${currentNode.path}'/>", '${currentNode.identifier}');
        </c:if>
    </c:if>
</script>
</template:addResources>
<div class=poll>

    <h3>
        ${currentNode.propertiesAsString['question']}
    </h3>

    <div id="pollForm${currentNode.identifier}">
        <c:if test="${not renderContext.editMode}">
        <div id="formContainer_${currentNode.identifier}">
            <form id="form_${currentNode.identifier}" name="form_${currentNode.identifier}" method="post" >
                <input type="hidden" name="jcrReturnContentType" value="json"/>
                </c:if>
                <c:if test="${renderContext.editMode}">
                <div class="addanswers">
                    <span><fmt:message key="jnt_poll.addAnswers"/></span>
                    </c:if>

                    <template:list path="answers" listType="jnt:answersList" editable="true"/>

                    <c:if test="${renderContext.editMode}">
                </div>
                </c:if>

                <c:if test="${not renderContext.editMode}">
                <div class="validation"></div>
                <input class="button" type="button" value="Vote" onclick="doVote($('${currentNode.identifier}_voteAnswer').value, '<c:url value="${url.base}${currentNode.path}"/>','${currentNode.identifier}');" />
            </form>
        </div>
        </c:if>
    </div>

    <div id="stats_${currentNode.identifier}">

    </div>
</div>
