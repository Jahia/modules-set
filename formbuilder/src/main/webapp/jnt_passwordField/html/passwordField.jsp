<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<p class="field">
    <label class="left" for="${currentNode.name}">${currentNode.properties['jcr:title'].string}</label>
    <input ${disabled} type="password" name="${currentNode.name}" value=""/>
</p>