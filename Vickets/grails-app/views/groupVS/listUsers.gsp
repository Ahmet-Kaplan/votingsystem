<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main" />
</head>
<body>
<div class="pageContenDiv">
    <div class="row" style="max-width: 1300px; margin: 0px auto 0px auto;">
        <ol class="breadcrumbVS pull-left">
            <ol class="breadcrumbVS pull-left">
                <li><a href="${grailsApplication.config.grails.serverURL}"><g:message code="homeLbl"/></a></li>
                <li><a href="${createLink(controller: 'groupVS', action: 'index')}"><g:message code="groupvsLbl"/></a></li>
                <li class="active"><g:message code="groupvsUserListLbl"/></li>
            </ol>
        </ol>
    </div>
    <p id="pageInfoPanel" class="text-center" style="margin: 20px auto 20px auto; font-size: 1.3em;
        background-color: #f9f9f9; max-width: 1000px; padding: 10px; display: none;"></p>

    <h3><div class="pageHeader text-center">
        <g:message code="groupvsUserListPageHeader"/> '${subscriptionMap?.groupName}'</div>
    </h3>
    <g:include view="/include/user-list.gsp"/>
    <user-list id="userList" url="${createLink(controller: 'groupVS', action: 'listUsers')}/${subscriptionMap?.id}"
               userURLPrefix="user" menuType="${params.menu}"></user-list>
</div>
</body>
</html>
<asset:script>

    $(function() {
        $("#navBarSearchInput").css( "visibility", "visible" );
        $("#advancedSearchButton").css( "display", "none" );

    })

    function processUserSearch(textToSearch) {
        $("#pageInfoPanel").text("<g:message code="searchResultLbl"/> '" + textToSearch + "'")
        $('#pageInfoPanel').css("display", "block")
        document.querySelector("#userList").url = targetURL + "?searchText=" + textToSearch
    }

</asset:script>