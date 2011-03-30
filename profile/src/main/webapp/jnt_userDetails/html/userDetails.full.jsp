<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ include file="../../getUser.jspf"%>

<template:addResources type="css" resources="userProfile.css"/>
<template:addCacheDependency node="${user}"/>
<jcr:nodeProperty node="${user}" name="j:publicProperties" var="publicProperties" />
<c:set var="publicPropertiesAsString" value=""/>
<c:forEach items="${publicProperties}" var="value">
    <c:set var="publicPropertiesAsString" value="${value.string} ${publicPropertiesAsString}"/>
</c:forEach>
<c:if test="${empty publicPropertiesAsString}">
    <fmt:message key="label.noPublicProperty">
        <fmt:param value="${user.name}"/>
    </fmt:message>
</c:if>
<c:if test="${!empty publicPropertiesAsString}">
    <c:set var="fields" value="${user.propertiesAsString}"/>
    <jcr:nodePropertyRenderer node="${user}" name="j:title" renderer="resourceBundle" var="title"/>
    <c:set var="person" value="${title.displayName} ${fields['j:firstName']} ${fields['j:lastName']}"/>
    <jsp:useBean id="now" class="java.util.Date"/>

    <jcr:nodeProperty node="${user}" name="j:birthDate" var="birthDate"/>
    <c:if test="${not empty birthDate}">
        <fmt:formatDate value="${birthDate.date.time}" pattern="yyyy" var="birthYear"/>
        <fmt:formatDate value="${now}" pattern="yyyy" var="currentYear"/>
    </c:if>
    <c:if test="${not empty birthDate}">
        <fmt:formatDate value="${birthDate.date.time}" pattern="dd/MM/yyyy" var="editBirthDate"/>
    </c:if>
    <fmt:formatDate value="${now}" pattern="dd/MM/yyyy" var="editNowDate"/>
    <div class="user-profile">
        <c:if test="${fn:contains(publicPropertiesAsString, 'j:picture')}">
            <div class="user-photo">
                <jcr:nodeProperty var="picture" node="${user}" name="j:picture"/>
                <c:if test="${not empty picture}">
                    <div class='image'>
                        <div class='itemImage itemImageLeft floatright'> <img src="${picture.node.thumbnailUrls['avatar_120']}" alt="${fn:escapeXml(person)}"/> </div>
                    </div>
                </c:if>
            </div>
        </c:if>
        <div class="user-body">
            <h3><c:if test="${fn:contains(publicPropertiesAsString, 'j:firstName')}">${fn:escapeXml(person)}&nbsp;</c:if></h3>
            <p class="small colorlight">
                <c:if test="${fn:contains(publicPropertiesAsString, 'j:birthDate')}">
                    <c:if test="${not empty birthDate}">
                        <fmt:formatDate value="${birthDate.date.time}" dateStyle="full" type="date"/>
                    </c:if>
                    &nbsp;</c:if>
                <c:if test="${fn:contains(publicPropertiesAsString, 'age')}">
                    <c:if test="${not empty birthDate}"> <span class="label">
          <fmt:message key="jnt_user.age"/>
          :&nbsp;</span>
                        <utility:dateDiff startDate="${birthDate.date.time}" endDate="${now}" format="years"/>
                        &nbsp;
                        <fmt:message key="jnt_user.profile.years"/>
                    </c:if>
                    &nbsp;</c:if>
            </p>
            <c:if test="${fn:contains(publicPropertiesAsString, 'j:organization')}">
                <p><span class="user-label">
        <fmt:message
                key="jnt_user.j_organization"/>
        :</span>&nbsp;${fn:escapeXml(fields['j:organization'])}</p>
            </c:if>
            <c:if test="${fn:contains(publicPropertiesAsString, 'j:function')}">
                <p><span class="user-label">
        <fmt:message
                key="jnt_user.j_function"/>
        :</span>&nbsp;${fn:escapeXml(fields['j:function'])}</p>
            </c:if>
            <c:if test="${fn:contains(publicPropertiesAsString, 'j:phoneNumber')}">
                <p><span class="user-label">
        <fmt:message

                key="jnt_user.j_phoneNumber"/>
        :</span>&nbsp;${fn:escapeXml(fields['j:phoneNumber'])}</p>
            </c:if>
            <c:if test="${fn:contains(publicPropertiesAsString, 'j:faxNumber')}">
                <p><span class="user-label">
        <fmt:message
                key="jnt_user.j_faxNumber"/>
        :</span>&nbsp;${fn:escapeXml(fields['j:faxNumber'])}</p>
            </c:if>
            <c:if test="${fn:contains(publicPropertiesAsString, 'j:email')}">
                <p><span class="user-label">
        <fmt:message key="jnt_user.j_email"/>
        :</span>&nbsp;<a
                        href="mailto:${fields['j:email']}">${fields['j:email']}</a></p>
            </c:if>
            <c:if test="${fn:contains(publicPropertiesAsString, 'j:skypeID')}">
                <p><span class="user-label">
        <fmt:message key="jnt_user.j_skypeID"/>
        :</span>&nbsp;${fields['j:skypeID']}
                    <c:if test="${not empty fields['j:skypeID']}"> <a href="skype:${fields['j:skypeID']}?call"><img
                            src="http://download.skype.com/share/skypebuttons/buttons/call_green_transparent_70x23.png"
                            style="border: none;" width="70" height="23" alt="${fields['j:skypeID']}"/></a> </c:if>
                </p>
            </c:if>
            <c:if test="${fn:contains(publicPropertiesAsString, 'j:twitterID')}">
                <p><span class="user-label">
        <fmt:message key="jnt_user.j_twitterID"/>
        :</span>&nbsp;${fields['j:twitterID']}
                    <c:if test="${not empty fields['j:twitterID']}"> <a href="http://twitter.com/${fields['j:twitterID']}" target="_blank"><img
                            src="http://twitbuttons.com/buttons/siahdesign/twit1.gif" alt="${fields['j:twitterID']}"
                            width="144" height="30"/></a> </c:if>
                </p>
            </c:if>
            <c:if test="${fn:contains(publicPropertiesAsString, 'j:linkedinID')}">
                <p><span class="user-label">
        <fmt:message key="jnt_user.j_linkedinID"/>
        :</span>&nbsp;<a
                        href="http://www.linkedin.com/in/{fields['j:linkedinID']}">${fields['j:linkedinID']}</a></p>
            </c:if>
        </div>
        <c:if test="${fn:contains(publicPropertiesAsString, 'j:about')}">
            <div class="aboutMe" >
                <h5>
                    <fmt:message key="label.aboutMe"/>
                </h5>
                <p>${fields['j:about']}</p>
            </div>
        </c:if>
    </div>
</c:if>
<c:if test="${renderContext.user.username == user.name}">
    <div class="divButtonRight"><a class="aButton" href="<c:url value='${url.base}${user.path}.user-edit-details.html'/>"><span><fmt:message key="label.editProfile"/></span></a></div>
</c:if>

