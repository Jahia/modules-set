<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentAliasUser" type="org.jahia.services.usermanager.JahiaUser"--%>

<c:if test="${!renderContext.loggedIn || currentAliasUser.username eq 'guest'}">
    <template:addResources type="javascript" resources="jquery.min.js"/>
    <template:addResources>
        <script type="text/javascript">
            $(document).ready(function() {
                $("#recoveryPasswordForm_${currentNode.identifier}").submit(function(event) {
                    event.preventDefault();
                    var $form = $("#recoveryPasswordForm_${currentNode.identifier}")
                    var url = $form.attr('action');

                    $form.attr('action','#')
                    var username = $form.find('input[name="username"]').val();
                    if (typeof(username) == 'undefined') {
                        return false;
                    }
					var values = $form.serializeArray();
                    $("#username_${currentNode.identifier}").attr("disabled", "disabled");
                    $.post(url, values,
                            function(data) {
                                alert(data['message']);
                            },
                            "json");
                    return false;
                });
            });
        </script>
    </template:addResources>

    <template:tokenizedForm>
        <form id="recoveryPasswordForm_${currentNode.identifier}"
              action="<c:url value='${url.base}${currentNode.properties.passChangePage.node.path}.recoverPassword.do'/>"
              method="post">
            <label for="username_${currentNode.identifier}" class="left"><fmt:message key='passwordrecovery.username' /></label>
            <input type="text" id="username_${currentNode.identifier}" name="username" class="full" />
            <input type="submit" value="<fmt:message key='passwordrecovery.recover' />" class="button" />
        </form>
    </template:tokenizedForm>
</c:if>

<c:if test="${renderContext.editMode}">
    <fmt:message key="${fn:replace(currentNode.primaryNodeTypeName, ':', '_')}" />
</c:if>
