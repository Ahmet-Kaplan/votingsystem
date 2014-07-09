<link rel="import" href="${resource(dir: '/bower_components/votingsystem-input', file: 'votingsystem-input.html')}">
<link rel="import" href="${resource(dir: '/bower_components/core-icon', file: 'core-icon.html')}">
<link rel="import" href="${resource(dir: '/bower_components/core-icon-button', file: 'core-icon-button.html')}">
<link rel="import" href="${resource(dir: '/bower_components/votingsystem-user-box', file: 'votingsystem-user-box.html')}">
<link rel="import" href="${resource(dir: '/bower_components/votingsystem-select-tag-dialog', file: 'votingsystem-select-tag-dialog.html')}">

<g:include view="/include/search-user.gsp"/>

<polymer-element name="votingsystem-deposit-dialog" attributes="caption opened serviceURL">
<template>
    <core-overlay id="coreOverlay" flex vertical class="card" opened="{{opened}}" layered="true" transition="test"
                  sizingTarget="{{$.container}}" style="position:absolute; top:10px;background: #f9f9f9;">
        <div id="container" style="width:400px; padding:0px 0px 15px 0px; border: 1px solid #ccc;">
            <div layout horizontal style="padding: 0px 10px 0px 20px;" >
                <h3 id="caption" flex style="color: #6c0404; font-weight: bold;"></h3>
                <core-icon-button icon="close" style="fill:#6c0404;" on-tap="{{toggle}}"></core-icon-button>
            </div>
            <div class="center card" style="font-weight: bold;color: {{status == 200?'#388746':'#ba0011'}};
            margin:10px; border: 1px solid #ccc; background: #f9f9f9;padding:10px 20px 10px 20px; display:{{messageToUser == null?'none':'block'}};">
                <div  layout horizontal center center-justified style="margin:0px 10px 0px 0px;">
                    <div id="messageToUser"></div>
                    <core-icon icon="{{status == 200?'check':'error'}}" style="fill:{{status == 200?'#388746':'#ba0011'}};"></core-icon></div>
                </div>
            <div layout vertical style="padding: 5px 20px 0px 20px;">
                <votingsystem-input id="amount" floatinglabel label="<g:message code="amountLbl"/> (EUR)"
                                    validate="^[0-9]*$" error="<g:message code="onlyNumbersErrorLbl"/>" style="display: inline;" required>
                </votingsystem-input>
                <votingsystem-input id="depositSubject" floatinglabel multiline
                                    label="<g:message code="subjectLbl"/>" required></votingsystem-input>
                <div  layout horizontal id="tagDataDiv" style="width:100%;margin:15px 0px 15px 0px; border: 1px solid #ccc;
                font-size: 1.1em; display: none; padding: 5px;">
                    <div style="margin:0px 10px 0px 0px; padding:5px;">
                        <div id="tagDataDivMsg" style="font-size: 0.9em;display: {{selectedTags.length == 0? 'block':'none'}};">
                            <g:message code="depositWithTagAdvertMsg"/>
                        </div>
                        <div id="tagDataDivMsg" style="font-weight:bold;display: {{selectedTags.length == 0? 'none':'block'}};">
                            <g:message code="selectedTagsLbl"/>
                        </div>
                        <div id="selectedTagDivDepositDialog" style="display: {{selectedTags.length == 0? 'none':'block'}};">
                            <template repeat="{{tag in selectedTags}}">
                                <button data-tagId='{{tag.id}}' on-click="{{removeTag}}" type="button" class="btn btn-default"
                                        style="margin:7px 10px 0px 0px;">{{tag.name}}  <i class="fa fa-minus-circle"></i>
                                </button>
                            </template>
                        </div>
                    </div>
                    <div class="button raised accept" on-click="{{toggleTagDialog}}"
                         style="border: 1px solid #ccc; margin:0px 0px 0px 5px;">
                        <div id="selectTagButton" class="center" fit><g:message code="addTagLbl" /></div>
                        <paper-ripple fit></paper-ripple>
                    </div>
                </div>
                <div>{{selectReceptorMsg}}</div>
                <votingsystem-user-box flex id="receptorBox" boxCaption="<g:message code="receptorLbl"/>"></votingsystem-user-box>

                <div id="receptorPanelDiv">
                    <div layout horizontal id="searchPanel" style="margin:15px auto 0px auto;width: 100%;">
                        <input id="userSearchInput" type="text" class="form-control" style="width:220px; display: inline;"
                               placeholder="<g:message code="enterReceptorDataMsg"/>">
                        <div class="button raised accept" on-click="{{searchUser}}" style="border: 1px solid #ccc; margin:0px 0px 0px 5px;">
                            <div class="center" fit><g:message code="userSearchLbl" /></div>
                        </div>
                    </div>
                    <search-user id="userSearchList"></search-user>
                </div>
                <div layout horizontal style="margin:10px 20px 0px 0px;">
                    <div flex></div>
                    <div class="button raised accept" on-click="{{submitForm}}" style="border: 1px solid #ccc;">
                        <div class="center" fit><g:message code="acceptLbl"/></div>
                    </div>
                </div>
            </div>
        </div>
    </core-overlay>
    <votingsystem-select-tag-dialog id="tagDialog" caption="<g:message code="addTagDialogCaption"/>"
                                    serviceURL="<g:createLink controller="vicketTagVS" action="index" />"></votingsystem-select-tag-dialog>
</template>
<script>
    Polymer('votingsystem-deposit-dialog', {
        operation:null,
        maxNumberTags:3,
        fromUserName:null,
        fromUserIBAN:null,
        dateValidTo:null,
        groupId:null,
        selectedTags: [],

        ready: function() {
            this.randomStr = Math.random().toString(36).substring(7)
            window[this.randomStr] = this
            this.callbackFunction = "window['" + this.randomStr + "'].setClientToolMessage"
            var depositDialog = this
            this.$.userSearchList.addEventListener('user-clicked', function (e) {
                depositDialog.$.receptorBox.addUser(e.detail)
            })

            this.$.tagDialog.addEventListener('tag-selected', function (e) {
                console.log("==== tag-selected: " + JSON.stringify(e.detail))
                depositDialog.selectedTags = e.detail
            })

            this.$.userSearchInput.onkeypress = function(event){
                var chCode = ('charCode' in event) ? event.charCode : event.keyCode;
                if (chCode == 13) {
                    depositDialog.searchUser()
                }
            }
        },

        openedChanged: function() {
            this.$.userSearchInput.value = ""
            this.setMessage(200, null)
            this.$.receptorBox.removeUsers()
            this.$.userSearchList.reset()
            this.$.tagDialog.reset()
            this.async(function() {this.$.coreOverlay.style.top = "50px" }, null, 1);
            this.$.coreOverlay.style.top = "10px"
        },

        toggleTagDialog: function() {
            this.$.tagDialog.show(this.maxNumberTags, this.selectedTags)
        },

        removeTag: function(e) {
            var tagToDelete = e.target.templateInstance.model.tag
            for(tagIdx in this.selectedTags) {
                if(tagToDelete.id == this.selectedTags[tagIdx].id) {
                    this.selectedTags.splice(tagIdx, 1)
                }
            }
        },

        toggle: function() {
            this.$.coreOverlay.toggle()
        },

        submitForm: function () {
            console.log(" ======= submitForm: " + this.$.amount.invalid)
            switch(this.operation) {
                case Operation.VICKET_DEPOSIT_FROM_GROUP_TO_MEMBER:
                    if(this.$.receptorBox.getUserList().length == 0){
                        this.setMessage(500, "<g:message code='receptorMissingErrorLbl'/>")
                        return false
                    }
                    break;
                case Operation.VICKET_DEPOSIT_FROM_GROUP_TO_MEMBER_GROUP:
                    if(this.$.receptorBox.getUserList().length == 0){
                        this.setMessage(500, "<g:message code='receptorMissingErrorLbl'/>")
                        return false
                    }
                    break;
                case Operation.VICKET_DEPOSIT_FROM_GROUP_TO_ALL_MEMBERS:
                    break;
            }
            if(this.$.amount.invalid) this.setMessage(500, "<g:message code='emptyFieldMsg'/>")
            if(this.$.depositSubject.invalid) this.setMessage(500, "<g:message code='emptyFieldMsg'/>")
            this.setMessage(200, null)
            var webAppMessage = new WebAppMessage(ResponseVS.SC_PROCESSING, this.operation)
            webAppMessage.receiverName="${grailsApplication.config.VotingSystem.serverName}"
            webAppMessage.serverURL="${grailsApplication.config.grails.serverURL}"
            webAppMessage.serviceURL = "${createLink( controller:'transaction', action:"deposit", absolute:true)}"
            webAppMessage.signedMessageSubject = "<g:message code='depositFromGroupMsgSubject'/>"
            webAppMessage.signedContent = {operation:this.operation, subject:this.$.depositSubject.value,
                toUserIBAN:this.toUserIBAN(), amount: this.$.amount.value, currency:"EUR", fromUser:this.fromUserName,
                fromUserIBAN:this.fromUserIBAN, validTo:this.dateValidTo }

            if(this.selectedTags.length > 0) {
                var tagList = []
                for(tagIdx in this.selectedTags) {
                    tagList.push({id:this.selectedTags[tagIdx].id, name:this.selectedTags[tagIdx].name});
                }
                webAppMessage.signedContent.tags = tagList
            }
            webAppMessage.urlTimeStampServer="${grailsApplication.config.VotingSystem.urlTimeStampServer}"
            webAppMessage.callerCallback = this.callbackFunction
            console.log(" - webAppMessage: " +  JSON.stringify(webAppMessage) + " - callerCallback: " + this.callbackFunction)
            VotingSystemClient.setJSONMessageToSignatureClient(webAppMessage);
        },

        toUserIBAN: function () {
            var receptorList = this.$.receptorBox.getUserList()
            var result = []
            for(userIdx in receptorList) {
                result.push(receptorList[userIdx].IBAN);
            }
            return result
        },
        searchUser: function() {
            var textToSearch = this.$.userSearchInput.value
            if(textToSearch.trim() == "") return
            var targetURL
            if(this.groupId != null) targetURL = "${createLink(controller: 'userVS', action: 'searchGroup')}?searchText=" + textToSearch + "&groupId=" + this.groupId
            else targetURL = "${createLink(controller: 'userVS', action: 'search')}?searchText=" + textToSearch
            this.$.userSearchList.url = targetURL
        },

        /*This method is called from JavaFX client. So we put referencens on global window */
        setClientToolMessage:function(appMessage) {
            console.log(this.tagName + " - " + this.id + " - setClientToolMessage: " + appMessage);
            var appMessageJSON = JSON.parse(appMessage)
            if(appMessageJSON != null) window[this.randomStr].setMessage(appMessageJSON.statusCode, appMessageJSON.message)
        },
        setMessage:function(status, message) {
            console.log("=== status: " + status, "===== message: " + message)
            this.status = status
            this.messageToUser = message
            this.$.messageToUser.innerHTML = message
        },
        show:function(operation, fromUser, fromIBAN, validTo, targetGroupId) {
            this.operation = operation
            this.fromUserName = fromUser
            this.fromUserIBAN = fromIBAN
            this.dateValidTo = validTo
            this.groupId = targetGroupId
            var selectReceptorMsg
            switch(operation) {
                case Operation.VICKET_DEPOSIT_FROM_GROUP_TO_MEMBER:
                    this.$.caption.innerHTML = fromUser + "<br/><div style='font-weight: normal;'><g:message code='vicketDepositFromGroupToMember'/></div>"
                    this.selectReceptorMsg = '<g:message code="selectReceptorMsg"/>'
                    break;
                case Operation.VICKET_DEPOSIT_FROM_GROUP_TO_MEMBER_GROUP:
                    this.$.caption.innerHTML = fromUser + "<br/><div style='font-weight: normal;'><g:message code='vicketDepositFromGroupToMemberGroup'/></div>"
                    this.selectReceptorMsg = '<g:message code="selectReceptorsMsg"/>'
                    document.getElementById('receptorPanelDiv').style.display = 'block'
                    break;
                case Operation.VICKET_DEPOSIT_FROM_GROUP_TO_ALL_MEMBERS:
                    this.$.caption.innerHTML = fromUser + "<br/><div style='font-weight: normal;'><g:message code='vicketDepositFromGroupToAllMembers'/></div>"
                    this.selectReceptorMsg = '<g:message code="depositToAllGroupMembersMsg"/>'
                    break;
            }
            this.selectedTags = []
            this.$.coreOverlay.opened = true
        }
    });
</script>
</polymer-element>
