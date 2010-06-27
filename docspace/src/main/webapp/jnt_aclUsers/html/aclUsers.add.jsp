<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<c:if test="${jcr:hasPermission(currentNode, 'write')}">
    <c:set var="aclNode" value="${uiComponents:getBindedComponent(currentNode, renderContext)}"/>
    <c:if test="${not empty aclNode}">
        <template:addResources type="css" resources="jquery.autocomplete.css"/>
        <template:addResources type="css" resources="thickbox.css"/>
        <template:addResources type="css" resources="searchusers.css"/>
        <template:addResources type="javascript" resources="jquery.min.js"/>
        <template:addResources type="javascript" resources="jquery.ajaxQueue.js"/>
        <template:addResources type="javascript" resources="jquery.autocomplete.js"/>
        <template:addResources type="javascript" resources="jquery.bgiframe.min.js"/>
        <template:addResources type="javascript" resources="thickbox-compressed.js"/>
        <script type="text/javascript">
            $(document).ready(function() {

                /**
                 * As any property can match the query, we try to intelligently display properties that either matched or make
                 * sense to display.
                 * @param node
                 */
                function getUserNameText(node) {
                    if ((node["j:firstName"] || node["j:lastName"]) && (node["j:firstName"] != '' || node["j:lastName"] != '')) {
                        return node["j:firstName"] + ' ' + node["j:lastName"];
                    } else if (node["j:nodename"] != null) {
                        return node["j:nodename"];
                    }
                    return "node is not a user";
                }

                $("#searchUser").autocomplete("${url.find}", {
                    dataType: "json",
                    cacheLength: 1,
                    parse: function parse(data) {
                        return $.map(data, function(row) {
                            return {
                                data: row,
                                value: getUserNameText(row),
                                result: getUserNameText(row)
                            }
                        });
                    },
                    formatItem: function(item) {
                        return getUserNameText(item);
                    },
                    extraParams: {
                        query : "SELECT * FROM [jnt:user] AS user WHERE user.[j:nodename] LIKE '%{$q}%' OR user.[j:lastName] LIKE '%{$q}%' OR user.[j:firstName] LIKE '%{$q}%'",
                        language : "JCR-SQL2",
                        removeDuplicatePropValues : "true"
                    }
                });
            });
        </script>
        <form method="post" action="${url.base}${aclNode.path}.setAcl.do" class="userssearchform">

            <jcr:nodeProperty name="jcr:title" node="${aclNode}" var="title"/>
            <c:if test="${not empty title.string}">
                <label class="addUsers" for="searchUser">${fn:escapeXml(title.string)}:&nbsp;</label>
            </c:if>
            <fmt:message key='search.users.defaultText' var="startSearching"/>
            <input type="text" name="user" id="searchUser" value="${startSearching}"
                   onfocus="if(this.value==this.defaultValue)this.value='';"
                   onblur="if(this.value=='')this.value=this.defaultValue;" class="text-input"/>
            <input class="addusersubmit" type="submit" title="<fmt:message key='search.submit'/>"/>
            <input type="hidden" name="acl" value="rew--"/>
        </form>
        <br class="clear"/>
    </c:if>
</c:if>
<template:linker path="*"/>