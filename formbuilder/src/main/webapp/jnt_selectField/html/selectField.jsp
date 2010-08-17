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


<c:set value="${currentNode.propertiesAsString}" var="props"/>

<p class="field">
<label class="left" for="${currentNode.name}">${props.label}</label>
<select name="${currentNode.name}">
    <c:forEach items="${jcr:getNodes(currentNode,'jnt:formListElement')}" var="option">
        <option value="${option.properties.value.string}" <c:if test="${not empty sessionScope.formError and sessionScope.formDatas[currentNode.name][0] eq option.properties.value.string}">selected="true"</c:if>>${option.properties.label.string}</option>
    </c:forEach>
</select>
<c:if test="${renderContext.editMode}">
<div class="formMarginLeft">
    <p>List of options</p>
    <ol>
        <c:forEach items="${jcr:getNodes(currentNode,'jnt:formListElement')}" var="option">
            <li><template:module node="${option}" template="default" editable="true"/></li>
        </c:forEach>
    </ol>
    <p>List of validation element</p>
    <ol>
    <c:forEach items="${jcr:getNodes(currentNode,'jnt:formElementValidation')}" var="formElement" varStatus="status">
        <li><template:module node="${formElement}" template="edit"/></li>
    </c:forEach>
    </ol>
        <div class="addvalidation">
        <span>Add your options/validations elements here</span>
        <template:module path="*"/>
    </div>
</div>
</c:if>
</p>
