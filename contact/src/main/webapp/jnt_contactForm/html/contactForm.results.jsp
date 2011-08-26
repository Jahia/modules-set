<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<ul>
<c:forEach items="${jcr:getChildrenOfType(currentNode, 'jnt:contact')}" var="subchild" varStatus="status">
    <li><template:module node="${subchild}" view="default"/></li>
</c:forEach></ul>
<a href="<c:url value='${url.base}${currentNode.path}.csv'/>" target="_new"><fmt:message key="label.exportCSV"/></a>
