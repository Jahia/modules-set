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
<template:addResources type="javascript" resources="jquery.js"/>
<template:addResources type="javascript" resources="jquery.juitter.js"/>
<template:addResources type="css" resources="twitter.css"/>

<script type="text/javascript">
    $(function() {
        if(!$("#myJuitterContainer").length){
            var container = $('<div></div>').attr('id','myJuitterContainer');
            $('#juitterFeed').append(container);
        }
        $.Juitter.start({
            searchType:"fromUser",
            placeHolder:"myJuitterContainer",
            searchObject:"${fn:replace(currentNode.properties.userName.string, ' ', ',')}",
            live:"live-${currentNode.properties.timeUpdate.long}",
            loadMSG: "Loading messages...",
            total: ${currentNode.properties.numberOfTweets.string},
            readMore: "Read it on Twitter",
            nameUser:"image"
        });

        $('#aUserSearch').click(function(){
            $.Juitter.start({
            searchType:"fromUser",
            placeHolder:"myJuitterContainer",
            searchObject:"${fn:replace(currentNode.properties.userName.string, ' ', ',')}",
            live:"live-${currentNode.properties.timeUpdate.long}",
            loadMSG: "Loading messages...",
            total: ${currentNode.properties.numberOfTweets.string},
            readMore: "Read it on Twitter",
            nameUser:"image"
            });
    });
    });


</script>

<div id="myContentFeed">
    <div id="juitterFeed">
        <p><fmt:message key="jnt_twitterFeed"/>:<a id="aUserSearch" href="#">${currentNode.properties.userName.string}</a></p>
    </div>
</div>