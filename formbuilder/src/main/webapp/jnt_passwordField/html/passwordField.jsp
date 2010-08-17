<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set value="${currentNode.propertiesAsString}" var="props"/>

<p class="field">
    <label class="left" for="${currentNode.name}">${props.label}</label>
    <input type="password" name="${currentNode.name}" value=""/>
</p>