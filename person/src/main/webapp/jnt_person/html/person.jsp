<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<template:addResources type="css" resources="person.css"/>
<script type="text/javascript">
    function ShowHideLayer(boxID) {
	/* Obtain reference for the selected boxID layer and its button */
	var box = document.getElementById("collapseBox"+boxID);

	/* If the selected box is currently invisible, show it */
	if(box.style.display == "none" || box.style.display=="") {
		box.style.display = "block";
	}
	/* otherwise hide it */
	else {
		box.style.display = "none";
	}
}
</script>
<div class="peopleListItem">
    <jcr:nodeProperty var="picture" node="${currentNode}" name="picture"/>
    <c:if test="${not empty picture}">
        <div class="peoplePhoto"><img src="${picture.node.thumbnailUrls['thumbnail']}" alt="${currentNode.properties.lastname.string} picture">
       </div>
    </c:if>
    <div class="peopleBody">
        <h4>${currentNode.properties.firstname.string}&nbsp;${currentNode.properties.lastname.string}</h4>

        <p class="peopleFonction">${currentNode.properties.function.string}</p>

        <p class="peopleBusinessUnit">${currentNode.properties.businessUnit.string}</p>

        <p class="peopleEmail"><a href='mailto:${currentNode.properties.email.string}'>${currentNode.properties.email.string}</a></p>

        <div class="peopleAction">
			<a class="peopleEnlarge" href="${picture.node.url}" rel="facebox"> <fmt:message key='FullSizePicture'/></a>
            <a class="peopleBiographiy" href="javascript:;" onclick="ShowHideLayer('${currentNode.identifier}');"><fmt:message
                    key='jahia.people.biography'/></a>
            <a class="peopleBiographiy" href="${url.base}${currentNode.path}.vcf"><fmt:message
                    key='jahia.people.vcard'/></a>
        </div>
         <div id="collapseBox${currentNode.identifier}" class="collapsible" >
            <jcr:nodeProperty node="${currentNode}" name="biography"/>
        </div>
        <!--stop collapsible -->
        <div class="clear"></div>
    </div>
    <!--stop peopleBody -->
    <div class="clear"></div>
</div>
