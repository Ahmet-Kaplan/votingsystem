<html>
<head>
    <meta name="layout" content="main" />
</head>
<body>

<div id="contentDiv" class="pageContenDiv" style="min-height: 1000px; margin:0px auto 0px auto;">
    <div style="margin:0px 30px 0px 30px;">
        <div class="row">
            <ol class="breadcrumbVS pull-left">
                <li><a href="${grailsApplication.config.grails.serverURL}"><g:message code="homeLbl"/></a></li>
                <li><a href="${createLink(controller: 'groupVS', action: 'index')}"><g:message code="groupvsLbl"/></a></li>
                <li class="active"><g:message code="newGroupVSLbl"/></li>
            </ol>
        </div>
        <h3>
            <div class="pageHeader text-center">
                <g:message code="newGroupPageTitle"/>
            </div>
        </h3>

        <div class="text-left" style="margin:10px 0 10px 0;">
            <ul>
                <li><g:message code="signatureRequiredMsg"/></li>
                <li><g:message code="newGroupVSAdviceMsg2"/></li>
                <li><g:message code="newGroupVSAdviceMsg3"/></li>
            </ul>
        </div>

        <form id="mainForm">

            <div class="form-inline">
                <div style="margin:0px 0px 10px 0px; display: table; height: 10px;">
                    <div style="display: table-cell;padding:0px; margin:0px;">
                        <input type="text" name="subject" id="groupSubject" style="width:400px;"  required
                               title="<g:message code="subjectLbl"/>" class="form-control"
                               placeholder="<g:message code="newGroupNameLbl"/>"
                               onchange="this.setCustomValidity('')" />
                    </div>
                    <div style="width:100%;display: table-cell" class="text-right">
                        <button type="button" onclick="showAddTagDialog(3, selectedTagsMap, refreshTagsDiv)" class="btn btn-danger">
                        <g:message code="addTagLbl" /></button>
                    </div>
                </div>
                <div id="tagsDiv" style="display: none; padding:0px 0px 0px 30px;">
                    <div style=" display: table-cell; font-size: 1.1em; font-weight: bold; vertical-align: middle;">
                        <g:message code='tagsLbl'/>:</div>
                    <div id="selectedTagDiv" style="margin:0px 0px 15px 0px; padding: 5px; display: table-cell;"></div>
                </div>
            </div>

            <div style="position:relative; width:100%;">
                <votingSystem:textEditor id="editorDiv" style="height:300px; width:100%;"/>
            </div>

            <div style="position:relative; margin:10px 10px 60px 0px;height:20px;">
                <div style="position:absolute; right:0;">
                    <button type="submit" class="btn btn-default">
                        <g:message code="newGroupVSLbl"/> <i class="fa fa fa-check"></i>
                    </button>
                </div>
            </div>

        </form>
    </div>
</div>
<g:include view="/include/dialog/addTagDialog.gsp"/>
<g:include view="/include/dialog/resultDialog.gsp"/>
</body>
</html>
<asset:script>

    var selectedTagsMap = {}

    function refreshTagsDiv(selectedTags) {
        selectedTagsMap = selectedTags
        document.getElementById("selectedTagDiv").innerHTML = ''
        Object.keys(selectedTagsMap).forEach(function(entry) {
            var tagDiv = document.createElement("div");
            tagDiv.classList.add('form-group');
            tagDiv.setAttribute("style","margin: 10px 0px 5px 10px;display:inline;");
            tagDiv.innerHTML = tagTemplate.format(entry, tagMap[entry]);
            document.querySelector("#selectedTagDiv").appendChild(tagDiv);
        });

        if(Object.keys(selectedTagsMap).length == 0) document.getElementById("tagsDiv").style.display= 'none'
        else document.getElementById("tagsDiv").style.display= 'table'
    }

    $(function() {
        $('#mainForm').submit(function(event){
            event.preventDefault();
            if(!document.getElementById('groupSubject').validity.valid) {
                showResultDialog('<g:message code="dataFormERRORLbl"/>', '<g:message code="fillAllFieldsERRORLbl"/>')
                return
            }
            var editorDiv = $("#editorDiv")
            var editorContent = getEditor_editorDivData()
            if(editorContent.length == 0) {
                editorDiv.addClass( "formFieldError" );
                showResultDialog('<g:message code="dataFormERRORLbl"/>', '<g:message code="emptyDocumentERRORMsg"/>')
                return
            }

            console.log("newGroup - sendSignature ")
            var webAppMessage = new WebAppMessage(ResponseVS.SC_PROCESSING, Operation.VICKET_GROUP_NEW)
            webAppMessage.receiverName="${grailsApplication.config.VotingSystem.serverName}"
            webAppMessage.serverURL="${grailsApplication.config.grails.serverURL}"
            webAppMessage.serviceURL = "${createLink( controller:'groupVS', action:"newGroup", absolute:true)}"
            webAppMessage.signedMessageSubject = "<g:message code='newGroupVSMsgSubject'/>"
            webAppMessage.signedContent = {groupvsInfo:getEditor_editorDivData(),groupvsName:$("#groupSubject").val(),
                        tags:selectedTagsMap, operation:Operation.VICKET_GROUP_NEW}
            if(Object.keys(selectedTagsMap).length > 0) {
                var tagList = []
                Object.keys(selectedTagsMap).forEach(function(entry) {
                    tagList.push({id:entry, name:selectedTagsMap[entry]})
                })
                webAppMessage.signedContent.tags = tagList
            }

            webAppMessage.urlTimeStampServer="${grailsApplication.config.VotingSystem.urlTimeStampServer}"
            webAppMessage.callerCallback = getFnName(newGroupVSCallback)
            console.log(" - webAppMessage: " +  JSON.stringify(webAppMessage))
            VotingSystemClient.setJSONMessageToSignatureClient(webAppMessage);
        });

      });


    function newGroupVSCallback(appMessage) {
        console.log("newGroupVSCallback - message from native client: " + appMessage);
        var appMessageJSON = toJSON(appMessage)
        var callBackResult = null
        if(appMessageJSON != null) {
            var caption = '<g:message code="newGroupERRORCaption"/>'
            var msg = appMessageJSON.message
            if(ResponseVS.SC_OK == appMessageJSON.statusCode) {
                caption = '<g:message code="newGroupOKCaption"/>'
                window.location.href = appMessageJSON.URL + "?menu=" + menuType
            }
            showResultDialog(caption, msg)
        }
        window.scrollTo(0,0);
    }

</asset:script>