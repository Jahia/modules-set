<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>

<c:set var="userPath" value='<%=request.getSession().getAttribute(request.getParameter("key"))%>' />

<c:if test="${not empty userPath}">
    <template:addResources type="javascript" resources="jquery.min.js"/>
    <template:addResources>
        <script type="text/javascript">
            $(document).ready(function() {
                $("#changePasswordForm_${currentNode.identifier}").submit(function(event) {
                    event.preventDefault();
                    var $form = $(this);
                    var url = $form.attr('action');

                    var password = $form.find('input[name="password"]').val();
                    if (password == '') {
                        alert("<fmt:message key='org.jahia.admin.userMessage.specifyPassword.label'/>");
                        return false;
                    }
                    var passwordconfirm = $form.find('input[name="passwordconfirm"]').val();
                    if (passwordconfirm != password) {
                        alert("<fmt:message key='org.jahia.admin.userMessage.passwdNotMatch.label'/>");
                        return false;
                    }
                    $.post(url, $form.serializeArray(),
                            function(data) {
                                alert(data['errorMessage']);
                                if (data['result'] == 'success') {
                                    $form.remove();
                                }
                            },
                            "json");
                });
            });
        </script>
    </template:addResources>

    <template:tokenizedForm>
        <form id="changePasswordForm_${currentNode.identifier}"
              action="<c:url value='${url.base}${userPath}.unauthenticatedChangePassword.do'/>"
              method="post">
            <input type="hidden" name="authKey" value="${param.key}" />
            <p class="field">
                <label for="password_${currentNode.identifier}" class="left"><fmt:message key="label.password" /></label>
                <input type="password" id="password_${currentNode.identifier}" name="password" class="full" />
            </p>
            <p class="field">
                <label for="passwordconfirm_${currentNode.identifier}" class="left"><fmt:message key="label.comfirmPassword" /></label>
                <input type="password" id="passwordconfirm_${currentNode.identifier}" name="passwordconfirm" class="full" />
            </p>
            <p class="field">
                <input type="submit" value="<fmt:message key='label.ok' />" class="button" />
            </p>
        </form>
    </template:tokenizedForm>
</c:if>

<c:if test="${renderContext.editMode}">
    <fmt:message key="${fn:replace(currentNode.primaryNodeTypeName, ':', '_')}" />
</c:if>
