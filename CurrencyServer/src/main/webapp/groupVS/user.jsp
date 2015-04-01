<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
    <title>${msg.groupUserPageLbl}</title>

    <link href="${config.webURL}/element/reason-dialog.vsp" rel="import"/>
</head>
<style>

</style>
<body>
    <div style="max-width: 600px; margin:0px auto 0px auto;">
        <div id="messageDiv" class="text-center" style="font-size: 1.4em; color:#6c0404; font-weight: bold;"></div>
        <div id="" style="border: 1px solid #6c0404; width: 500px;margin:auto; padding: 15px;">
            <div id="" style="font-size: 1.2em; font-weight: bold;">${subscriptionMap.uservs.NIF}</div>
            <div id="nameDiv" style="font-size: 1.2em;font-weight: bold;">${subscriptionMap.uservs.name}</div>
            <div id="contentDiv" style="">${msg.subscriptionRequestDateLbl}: <span id="dateCreatedDiv"></span></div>
        </div>
        <button id="activateUserButton" type="button" class="btn btn-default" onclick="activateUser();"
                style="margin:10px 0px 0px 0px;">${msg.activateUserLbl} <i class="fa fa-thumbs-o-up"></i>
        </button>
        <button id="deActivateUserButton" type="button" class="btn btn-default" onclick="document.querySelector('#reasonDialog').toggle();"
                style="margin:10px 0px 0px 10px; ">${msg.deActivateUserLbl} <i class="fa fa-thumbs-o-down"></i>
        </button>
        <button id="makeTransactionVSButton" type="button" class="btn btn-default" onclick="makeTransactionVS();"
                style="margin:10px 0px 0px 10px; ">${msg.makeTransactionVSLbl} <i class="fa fa-money"></i>
        </button>
        <div id="receipt" style="display:none;">

        </div>
    </div>
<reason-dialog id="reasonDialog" caption="${msg.cancelSubscriptionFormCaption}" opened="false"
       messageToUser="${msg.cancelSubscriptionFormMsg}"></reason-dialog>
</body>
</html>
<script id="currencyScript">
    <g:applyCodec encodeAs="none">
        var subscriptionDataJSON = ${subscriptionMap as JSON}
    </g:applyCodec>

    document.addEventListener('polymer-ready', function() {
        document.querySelector("#reasonDialog").addEventListener('on-submit', function (e) {
            console.log("deActivateUser")
            var webAppMessage = new WebAppMessage(Operation.CURRENCY_GROUP_USER_DEACTIVATE)
            webAppMessage.serviceURL = "${config.restURL}/groupVS/deActivateUser"
            webAppMessage.signedMessageSubject = "${msg.deActivateGroupUserMessageSubject}" + " '" + subscriptionDataJSON.groupvs.name + "'"
            webAppMessage.signedContent = {operation:Operation.CURRENCY_GROUP_USER_DEACTIVATE,
                groupvs:{name:subscriptionDataJSON.groupvs.name, id:subscriptionDataJSON.groupvs.id},
                uservs:{name:subscriptionDataJSON.uservs.name, NIF:subscriptionDataJSON.uservs.NIF}, reason:e.detail}
            webAppMessage.contentType = 'application/pkcs7-signature'
            webAppMessage.setCallback(function(appMessage) {
                    console.log("deActivateUserCallback - message: " + appMessage);
                    var appMessageJSON = toJSON(appMessage)
                    var caption = '${msg.deActivateUserERRORLbl}'
                    if(ResponseVS.SC_OK == appMessageJSON.statusCode) {
                        caption = "${msg.deActivateUserOKLbl}"
                    }
                    var msg = appMessageJSON.message
                    showMessageVS(msg, caption)
                })
            VotingSystemClient.setJSONMessageToSignatureClient(webAppMessage);
        })
    });

    document.getElementById("dateCreatedDiv").innerHTML = subscriptionDataJSON.dateCreated

    <c:if test="${SubscriptionVS.State.ACTIVE.toString().equals(subscriptionMap.state)}">
        document.getElementById("messageDiv").innerHTML = "${msg.userStateActiveLbl}"
        document.getElementById("activateUserButton").style.display = 'none'
        if("admin" != menuType) document.getElementById("deActivateUserButton").style.display = 'none'
    </c:if>
    <c:if test="${SubscriptionVS.State.PENDING.toString().equals(subscriptionMap.state)}">
        document.getElementById("messageDiv").innerHTML = "${msg.userStatePendingLbl}"
        document.getElementById("makeTransactionVSButton").style.display = 'none'
    </c:if>
    <c:if test="${SubscriptionVS.State.CANCELED.toString().equals(subscriptionMap.state)}">
        document.getElementById("messageDiv").innerHTML = "${msg.userStateCancelledLbl}"
        document.getElementById("deActivateUserButton").style.display = 'none'
        document.getElementById("makeTransactionVSButton").style.display = 'none'
    </c:if>

    function activateUser () {
        console.log("activateUser")
        var webAppMessage = new WebAppMessage(Operation.CURRENCY_GROUP_USER_ACTIVATE)
        webAppMessage.serviceURL = "${config.restURL}/groupVS/activateUser"
        webAppMessage.signedMessageSubject = "${msg.activateGroupUserMessageSubject}" + " '" + subscriptionDataJSON.groupvs.name + "'"

        webAppMessage.signedContent = {operation:Operation.CURRENCY_GROUP_USER_ACTIVATE,
            groupvs:{name:subscriptionDataJSON.groupvs.name, id:subscriptionDataJSON.groupvs.id},
            uservs:{name:subscriptionDataJSON.uservs.name, NIF:subscriptionDataJSON.uservs.NIF}}
        webAppMessage.contentType = 'application/pkcs7-signature'
        webAppMessage.setCallback(function(appMessage) {
                console.log("activateUserCallback - message: " + appMessage);
                var appMessageJSON = toJSON(appMessage)
                if(appMessageJSON != null) {
                    var caption = '${msg.activateUserERRORLbl}'
                    if(ResponseVS.SC_OK == appMessageJSON.statusCode) {
                        caption = "${msg.activateUserOKLbl}"
                        document.querySelector("#messageDiv").innerHTML = "${msg.userStateActiveLbl}"
                        document.querySelector("#activateUserButton").style.display = 'none'
                        document.querySelector("#makeTransactionVSButton").style.display = 'block'
                        document.querySelector("#deActivateUserButton").style.display = 'block'
                    }
                showMessageVS(appMessageJSON.message, caption)
            })
        VotingSystemClient.setJSONMessageToSignatureClient(webAppMessage);
    }

    function makeTransactionVS () {
        console.log("makeTransactionVS - TODO -")
    }

</script>
