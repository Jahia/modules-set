<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<ul>
<c:forEach items="${currentNode.nodes}" var="subchild" varStatus="status">
    <!-- <div class="forum-box forum-box-style${(status.index mod 2)+1}"> -->
        <li><template:module node="${subchild}" view="default"/>  </li>
   <!-- </div>  -->
</c:forEach></ul>
