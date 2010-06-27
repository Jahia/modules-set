<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ page language="java" contentType="text/html;charset=UTF-8" %>

<%@ include file="../declarations.jspf" %>
<div class="languageSwitching_horizontal_display">
    <ul>
        <c:forEach var="langCode" items="${requestScope.languageCodes}">
            <li class="${requestScope.linkDisplay}">
                <c:choose>
                    <c:when test="${requestScope.linkDisplay == 'flag'}">
                        <ui:displayLanguageFlag languageCode="${langCode}"
                                                       title="Switch to ${langCode}"
                                                       titleKey="switchTo"
                                                       onLanguageSwitch="${requestScope.onLanguageSwitch}"
                                                       redirectCssClassName="${requestScope.redirectCssClassName}"/>
                    </c:when>
                    <c:otherwise>
                        <ui:displayLanguageSwitchLink languageCode="${langCode}"
                                                             title="Switch to ${langCode}"
                                                             titleKey="switchTo"
                                                             linkKind="${requestScope.linkDisplay}"
                                                             onLanguageSwitch="${requestScope.onLanguageSwitch}"
                                                             redirectCssClassName="${requestScope.redirectCssClassName}"/>
                    </c:otherwise>
                </c:choose>
               
            </li>
        </c:forEach>
    </ul>
</div>