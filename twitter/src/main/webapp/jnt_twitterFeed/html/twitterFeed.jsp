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
<template:addResources type="javascript" resources="widget.js"/>
<c:choose>
    <c:when test="${not empty currentNode.properties['tweetWidth'].string && currentNode.properties['autoWidth'].boolean}">
        <c:set var="width" value="auto"/>
    </c:when>
    <c:when test="${not empty currentNode.properties['tweetWidth'].string && !currentNode.properties['autoWidth'].boolean}">
        <c:set var="width" value="${currentNode.properties['tweetWidth'].string}"/>
    </c:when>
    <c:otherwise>
        <c:set var="width" value="auto"/>
    </c:otherwise>
</c:choose>
<c:set var="height" value="${currentNode.properties['tweetHeight'].string}"/>
<c:set var="rpp" value="${currentNode.properties['numberOfTweets'].string}"/>
<c:set var="shellBackground" value="${currentNode.properties['shellBackgroundColor'].string}"/>
<c:set var="shellColor" value="${currentNode.properties['shellTextColor'].string}"/>
<c:set var="tweetsBackground" value="${currentNode.properties['tweetBackgroundColor'].string}"/>
<c:set var="tweetsColor" value="${currentNode.properties['tweetTextColor'].string}"/>
<c:set var="tweetsLinks" value="${currentNode.properties['tweetLinksColor'].string}"/>
<c:set var="scrollbar" value="${currentNode.properties['includeScrollbar'].boolean}"/>
<c:set var="avatars" value="${currentNode.properties['showAvatars'].boolean}"/>
<c:set var="hashtags" value="${currentNode.properties['showHashtags'].boolean}"/>
<c:set var="timestamps" value="${currentNode.properties['showTimestamps'].boolean}"/>
<c:set var="live" value="${currentNode.properties['pollForNewResults'].boolean}"/>
<c:if test="${!renderContext.editMode}">
    <script type="text/javascript">
        new TWTR.Widget({
            version: 2,
            type: 'profile',
            rpp: '${not empty rpp ? rpp : '5'}',
            interval: 6000,
            width: '${not empty width ? width : 'auto'}',
            height: '${not empty height ? height : '300'}',
            theme: {
                shell: {
                    background: '${shellBackground}',
                    color: '${shellColor}'
                },
                tweets: {
                    background: '${tweetsBackground}',
                    color: '${tweetsColor}',
                    links: '${tweetsLinks}'
                }
            },
            features: {
                scrollbar: ${scrollbar},
                loop: false,
                live: ${live},
                hashtags: ${hashtags},
                timestamp: ${timestamps},
                avatars: ${avatars},
                behavior: 'all'
            }
        }).render().setUser('${currentNode.properties["userAccount"].string}').start();
    </script>
</c:if>
<c:if test="${renderContext.editMode}">
    <div style="width:${width}px; height:${height + 50}px; border:5px solid ${shellBackground}; background-color:${tweetsBackground}">
        <p style="margin:10px;"><fmt:message key="feed.messageEditMode"/></p>
        <p style="margin:10px;"><fmt:message key="feed.messageCurrent"/> ${currentNode.properties["userAccount"].string}</p>
    </div>
</c:if>