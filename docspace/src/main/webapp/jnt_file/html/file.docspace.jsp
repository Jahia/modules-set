<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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

<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="acl" type="java.lang.String"--%>
<template:addResources type="css" resources="docspace.css,files.css,toggle-docspace.css"/>
<template:addResources type="css" resources="jquery.fancybox.css"/>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.js"/>
<template:addResources type="javascript"
                       resources="${url.context}/gwt/resources/${url.ckEditor}/ckeditor.js"/>

<template:addResources type="javascript" resources="jquery.cuteTime.js"/>
<template:addResources type="javascript" resources="jquery.jeditable.ckeditor.js"/>
<c:set value="${jcr:hasPermission(currentNode, 'write')}" var="hasWriteAccess"/>
<script>
    $(document).ready(function() {
        $("#ckeditorEditDescription").editable(function (value, settings) {
            var url = $(this).attr('jcr:url');
            var submitId = $(this).attr('jcr:id');
            var data = {};
            data[submitId] = value;
            data['methodToCall'] = 'put';
            $.post(url, data, null, "json");
            return(value);
        }, {
            type : 'ckeditor',
            onblur : 'ignore',
            submit : 'Ok',
            cancel : 'Cancel',
            tooltip : 'Click to edit'
        });

        $("#actions").click(function() {
            if ($(this).hasClass('delete')) {
                if (confirm("Do you REALLY want to delete this file SPACE?")) {
                    var data = {};
                    data['methodToCall'] = 'delete';
                    $.post('${url.base}${currentNode.path}', data, function () {
                        window.location.href = '${url.base}${currentNode.parent.path}.html';
                    }, "json");
                } else {
                    $(this).val("");
                }
            }
        });

        $('#publishFile').submit(function() {
            $.post('${url.base}${currentNode.path}.publishFile.do', $(this).serializeArray(), null, "json");
            return false;
        });
    });
</script>
<div><!--start grid_12-->
    <a class="docspaceBack" href="${url.base}${currentNode.parent.path}.html"><fmt:message
            key="docspace.label.back"/> ${currentNode.parent.name}</a>
    <c:if test="${hasWriteAccess}">
        <a href="#" id="actions" title="Delete" class="delete"><fmt:message key="docspace.label.file.delete"/></a>
    </c:if>
    <div class='clear'></div>
</div>
<div><!--start grid_12-->
    <div class="boxdocspace "><!--start boxdocspace -->
        <div class="boxdocspacepadding16 boxdocspacemarginbottom16">
            <div class="boxdocspace-inner">
                <div class="boxdocspace-inner-border">
                    <fmt:message key="docspace.label.download" var="i18nDownload"/>
                    <div class="imagefloatleft">
                        <div class="itemImage itemImageLeft">
							<a href="${currentNode.url}" title="${i18nDownload}&nbsp;${fn:escapeXml(currentNode.name)}"><span class="icon_large ${functions:fileIcon(currentNode.name)}_large"></span></a>
                        </div>
                        <c:if test="${currentNode.fileContent.contentType != 'application/pdf'}">
                        <div class="itemImage itemImageLeft"
                            <a href="<c:url value='${currentNode.path}.pdf' context='${url.convert}'/>" title="${i18nDownload}&nbsp;<fmt:message key='docspace.label.asPdf'/>"><span class="icon_large pdf_large"></span></a>
                        </div>
                        </c:if>
                    </div>
                    <h3><fmt:message key="docspace.label.document.name"/> <a href="${currentNode.url}" title="Download ${currentNode.name}"><img
                            title="${i18nDownload}&nbsp;${fn:escapeXml(currentNode.name)}" value="download"
                            src="${url.currentModule}/css/img/download.png"/>&nbsp;${functions:abbreviate(currentNode.name,20,30,'...')}&nbsp;${currentNode.baseVersion.name}
                    </a></h3>

                    <p class="clearMaringPadding docspacedate "><fmt:message key="label.created"/> : <fmt:formatDate
                            value="${currentNode.properties['jcr:created'].time}" pattern="yyyy/MM/dd"/>, <fmt:message
                            key="docspace.label.document.createdBy"/>&nbsp;<span class="author"><a
                            href="${url.base}/users/${currentNode.properties['jcr:createdBy'].string}.html">${currentNode.properties['jcr:createdBy'].string}</a></span>
                    </p>

                    <p class="clearMaringPadding docspacedate"><fmt:message
                            key="docspace.label.document.lastModification"/> <fmt:formatDate
                            value="${currentNode.properties['jcr:lastModified'].time}" pattern="yyyy/MM/dd"/>,
                        <fmt:message key="docspace.label.document.createdBy"/>&nbsp;<span class="author"><a
                                href="${url.base}/users/${currentNode.properties['jcr:lastModifiedBy'].string}.html">${currentNode.properties['jcr:lastModifiedBy'].string}</a></span>
                    </p>
                </div>
            </div>
        </div>
    </div>
    <div class='clear'></div>
</div>
