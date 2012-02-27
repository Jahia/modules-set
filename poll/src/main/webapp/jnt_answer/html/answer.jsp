<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="answerId" value="${currentNode.parent.parent.name}_voteAnswer_${currentNode.UUID}"/>
<span class="poll_answer"><input type="radio" id="${answerId}" name="voteAnswer" value="${currentNode.UUID}" /><label for="${answerId}"> ${fn:escapeXml(currentNode.properties.label.string)}</label></span>
<br />