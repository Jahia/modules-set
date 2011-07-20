<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<template:addResources type="css" resources="job.css"/>
<template:addResources type="javascript" resources="job-collapse.js"/>
<c:set var="values" value="${currentNode.propertiesAsString}"/>
<div class="spacer">
    <h2>${fn:escapeXml(values['jcr:title'])}</h2>

    <div class="jobListItem">
        <div class="jobInfo">
            <p class="jobLocation">
                <span class="jobLabel"><fmt:message key="jnt_job.location"/>: </span>
                <span class="jobtxt">${fn:escapeXml(values.town)},&nbsp;<jcr:nodePropertyRenderer node="${currentNode}" name="country" renderer="flagcountry"/></span>
            </p>

            <p class="jobType">
                <span class="jobLabel"><fmt:message key="jnt_job.contract"/>: </span>
                <span class="jobtxt"><jcr:nodePropertyRenderer node="${currentNode}" name="contract" renderer="resourceBundle"/></span>
            </p>

            <p class="jobBusinessUnit">
                <span class="jobLabel"><fmt:message key="jnt_job.businessUnit"/>: </span>
                <span class="jobtxt">${fn:escapeXml(values.businessUnit)}</span>
            </p>

            <p class="jobReference">
                <span class="jobLabel"><fmt:message key="jnt_job.reference"/>: </span>
                <span class="jobtxt">${fn:escapeXml(values.reference)}</span>
            </p>

            <p class="educationLevel">
                <span class="jobLabel"><fmt:message key="jnt_job.educationLevel"/>: </span>
                <span class="jobtxt">${fn:escapeXml(values.educationLevel)}</span>
            </p>
        </div>
        <p class="jobDescription">
            <span class="jobLabel"><fmt:message key="jnt_job.description"/>:</span>
            <span class="jobtxt">${values.description}</span>
        </p>

        <p class="jobSkills">
            <span class="jobLabel"><fmt:message key="jnt_job.skills"/>:</span>
            <span class="jobtxt">${values.skills}</span>
        </p>

        <c:set var="writeable" value="${currentResource.workspace eq 'live'}" />
        <c:if test='${writeable}'>
        <div class="jobAction">
            <a onclick="ShowHideLayer('${currentNode.identifier}'); return false;" href="#apply"
               class="jobApply">Apply</a>

            <div class="clear"></div>
        </div>

        <div class="collapsible" id="collapseBox${currentNode.identifier}">
            <div class="Form jobsApplyForm">
                <template:tokenizedForm>
                <form action="<c:url value='${url.base}${currentNode.path}/*'/>" method="post">
                    <input type="hidden" name="jcrNodeType" value="jnt:jobApplication"/>
                    <input type="hidden" name="jcrRedirectTo" value="<c:url value='${url.base}${renderContext.mainResource.node.path}'/>"/>
                    <input type="hidden" name="jcrNewNodeOutputFormat" value="html"/>
                    <fieldset>
                        <p class="field">
                            <label class="left" for="job-application-firstname"><fmt:message key="jnt_jobApplication.firstname"/>:</label>
                            <input type="text" name="firstname" id="job-application-firstname" ${disabled}/>
                        </p>

                        <p class="field">
                            <label class="left" for="job-application-lastname"><fmt:message key="jnt_jobApplication.lastname"/>:</label>
                            <input type="text" name="lastname" id="job-application-lastname" ${disabled}/>
                        </p>

                        <p class="field">
                            <label class="left" for="job-application-email"><fmt:message key="jnt_jobApplication.email"/>:</label>
                            <input type="text" name="email" id="job-application-email" ${disabled}/>
                        </p>

                        <p class="field">
                            <label class="left" for="job-application-text"><fmt:message key="jnt_jobApplication.text"/>:</label>
                            <textarea name="text" id="job-application-text" ${disabled}></textarea>
                        </p>
                    <div class="formMarginLeft">
                    	<input type="submit" class="button" value="Apply" ${disabled}/>
                    </div>
                    </fieldset>

                </form>
                </template:tokenizedForm>
            </div>
        </div>
        </c:if>

        <c:if test="${jcr:hasPermission(currentNode,'jcr:read_default')}">
            <template:addResources type="javascript" resources="jquery.min.js"/>
            <fieldset>
                <legend><fmt:message key="label.results"/></legend>

                <div id="results-${currentNode.identifier}" >
                </div>
            </fieldset>
            <script type="text/javascript">
                $('#results-${currentNode.identifier}').load('<c:url value="${url.baseLive}${currentNode.path}.results.html.ajax"/>');
            </script>
        </c:if>

    </div>
</div>