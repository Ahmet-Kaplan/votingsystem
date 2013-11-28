<!DOCTYPE html>
<html>
<head>
  	<title><g:message code="manifestProtocolSimulationCaption"/></title>
   	<r:require modules="application"/>
   	<r:require modules="textEditorPC"/>
	<r:layoutResources />
</head>
<div id="manifestProtocolSinulationDataDialog" title="<g:message code="initManifestProtocolSimulationButton"/>"
	style="padding:10px 20px 20px 20px; margin:0px 0px 0px 0px;overflow: hidden; position:relative;">
	<div class="errorMsgWrapper" style="display:none;"></div>
    <div style="display:block;overflow: hidden; position: relative; margin:0px 0px 1px 0px; padding: 0 0 10px 0; ">
        <div style="margin:10px 0px 0px 0px; display: inline; position: absolute; right: 0px; left: 0px;">
            <p style="text-align: center;font-weight: bold; font-size: 1.4em; color: #48802c;">
                <g:message code="initManifestProtocolSimulationMsg"/>
            </p>
        </div>
        <votingSystem:simpleButton id="testButton"
            style="margin:0px 20px 0px 0px; padding: 0px 10px 0px 10px; float:right; display: inline;">
            Test
        </votingSystem:simpleButton>
    </div>
  	<div id="formDataDiv">
   		<form id="manifestProtocolSinulationDataForm">
			<input type="hidden" autofocus="autofocus" />
			<input id="resetManifestProtocolSinulationDataForm" type="reset" style="display:none;">

            <div style="display: block;">
                <label><g:message code="numRequestsProjectedLbl"/></label>
                <input type="number" id="numRequestsProjected" min="1" value="1" required
                       style="width:110px;margin:0px 20px 0px 3px;"
                       title="<g:message code="numRequestsProjectedLbl"/>"
                       placeholder="<g:message code="numRequestsProjectedLbl"/>"
                       oninvalid="this.setCustomValidity('<g:message code="emptyFieldLbl"/>')"
                       onchange="this.setCustomValidity('')">
                <label><g:message code="maxPendingResponsesLbl"/></label>
                <input type="number" id="maxPendingResponses" min="1" value="10" required
                       style="width:110px;margin:10px 20px 0px 3px;"
                       title="<g:message code="maxPendingResponsesLbl"/>"
                       placeholder="<g:message code="maxPendingResponsesLbl"/>"
                       oninvalid="this.setCustomValidity('<g:message code="emptyFieldLbl"/>')"
                       onchange="this.setCustomValidity('')">
            </div>

            <div style="margin:10px auto 0px auto; width:100%; height:30px; position: relative; overflow: hidden;">
                <label><g:message code="eventStateOnFinishLbl"/></label>
                <select id="eventStateOnFinishSelect" style="margin:0px 20px 0px 0px;"
                        title="<g:message code="setEventStateLbl"/>">
                    <option value=""> - <g:message code="eventAsDateRangeLbl"/> - </option>
                    <option value="CANCELLED" style="color:#cc1606;"> - <g:message code="eventCancelledLbl"/> - </option>
                    <option value="DELETED" style="color:#cc1606;"> - <g:message code="eventDeletedLbl"/> - </option>
                </select>
                <votingSystem:datePicker id="dateFinish" title="${message(code:'dateFinishLbl')}"
                    placeholder="${message(code:'dateFinishLbl')}"
                    oninvalid="this.setCustomValidity('${message(code:'emptyFieldLbl')}')"
                    onchange="this.setCustomValidity('')"></votingSystem:datePicker>
            </div>

            <div style="margin:10px 0px 10px 0px">
                <input type="text" name="subject" id="subject" style="width:350px"  required
                    title="<g:message code="subjectLbl"/>"
                    placeholder="<g:message code="subjectLbl"/>"/>
                <input type="url" id="accessControlURL" style="width:300px; margin:0px 0px 0px 20px;" required
                       value="http://192.168.1.20:8080/AccessControl"
                       title="<g:message code="accessControlURLMsg"/>"
                       placeholder="<g:message code="accessControlURLMsg"/>"
                       oninvalid="this.setCustomValidity('<g:message code="emptyFieldLbl"/>')"
                       onchange="this.setCustomValidity('')"/>
            </div>

            <votingSystem:textEditorPC id="manifestEditorDiv" style="height:300px;"/>

            <div id="backupDiv" style="margin:10px 0px 10px 10px; overflow: hidden; height: 50px; display: table;">
                <div class="checkBox" style="display:table-cell;vertical-align: middle;">
                    <input type="checkbox" id="requestBackup"/><label for="requestBackup"><g:message code="requestBackupLbl"/></label>
                </div>
                <div id="emailDiv" style="display:table-cell;vertical-align: middle;">

                </div>
            </div>
            <div style="position: relative; overflow:hidden; ">
                <votingSystem:simpleButton id="submitButton" isButton='true'
                    style="margin:15px 20px 20px 0px;padding:2px 5px 2px 0px; height:30px; width:400px; float:right;">
                    <g:message code="initManifestProtocolSimulationButton"/>
                </votingSystem:simpleButton>
            </div>

   		</form>

  	</div>
</div>

<div id="simulationListenerDiv" style="display: none;">
    <g:include view="/include/listenSimulation.gsp"/>
</div>

<template id="emailTemplate" style="display:none;">
    <input type="email" id="emailRequestBackup" style="width:300px;" required
           title="<g:message code="emailRequestBackupMsg"/>"
           placeholder="<g:message code="emailLbl"/>"
           oninvalid="this.setCustomValidity('<g:message code="emptyFieldLbl"/>')"
           onchange="this.setCustomValidity('')">
</template>
</html> 
<r:script>

$("#requestBackup").click(function () {
	if($("#requestBackup").is(':checked')) {
		$('#emailDiv').append($('#emailTemplate').html());
	} else {
		$('#emailDiv').html("");
	}
})

var isFormView = true

$("#testButton").click(function () {
    if(isFormView) {
        $('#formDataDiv').fadeOut()
        $('#simulationListenerDiv').fadeIn()
        $('#pageTitle').text('<g:message code="listeningManifestProtocolSimulationMsg"/>' + ": '" + $('#subject').val() + "'")
        isFormView = false;
    } else {
        $('#simulationListenerDiv').fadeOut()
        $('#formDataDiv').fadeIn()
        showEditor_manifestEditorDiv()
        SimulationService.close()
        $('#pageTitle').text('<g:message code="initManifestProtocolSimulationMsg"/>')
        isFormView = true;
    }

});


var manifestEditorDiv = $("#manifestEditorDiv")
dateFinish    = $("#dateFinish")
allFields = $( [] ).add(dateFinish).add(manifestEditorDiv);

var callerCallback

showEditor_manifestEditorDiv()


function showManifestProtocolSinulationDataDialog(callback) {
	$("#manifestProtocolSinulationDataDialog").dialog("open");
	callerCallback = callback	
}

$('#manifestProtocolSinulationDataForm').submit(function(event){
	event.preventDefault();	  


 	allFields.removeClass("formFieldError");   
 	$(".errorMsgWrapper").fadeOut() 
	hideEditor_manifestEditorDiv() 
	if(!isValidForm()) {
		showEditor_manifestEditorDiv()
		return false
	}

	var dateBeginStr = new Date().format()
	var event = {subject:$('#subject').val(),
	        content:manifestEditorDivContent.trim(),
	        dateBegin:dateBeginStr,
	        dateFinish:dateFinish.datepicker("getDate").format()}
	
	 var simulationData = {service:"manifestSimulationService", status:"INIT_SIMULATION",
	         accessControlURL:$('#accessControlURL').val(),
			 maxPendingResponses: $('#maxPendingResponses').val(), 
			 numRequestsProjected: $('#numRequestsProjected').val(),
			 dateBeginDocument: dateBeginStr, 
			 dateFinishDocument: dateFinish.datepicker("getDate").format(),
			 whenFinishChangeEventStateTo:$( "#eventStateOnFinishSelect option:selected").val(), 
			 backupRequestEmail:$('#emailRequestBackup').val(), 
			 event:event}

    $('#formDataDiv').fadeOut()
    $('#simulationListenerDiv').fadeIn()
     showSimulationProgress(simulationData)
	return false
});

function isValidForm() {
	if(!document.getElementById('accessControlURL').validity.valid) {
		$("#accessControlURL").addClass("formFieldError");
		showResultDialog('<g:message code="dataFormERRORLbl"/>', 
			'<g:message code="emptyFieldLbl"/>', function() {
			$("#addControlCenterDialog").dialog("open")
		})
		return false
	}	      
	var accessControlURL = $('#accessControlURL').val()
	var suffix = "/"
	if((accessControlURL.indexOf(suffix, accessControlURL.length - suffix.length) == -1)) {
		accessControlURL = accessControlURL + "/"
	}
	accessControlURL = accessControlURL + "serverInfo"
	if(accessControlURL.indexOf("http://") != 0) {
		accessControlURL = "http://" + accessControlURL
	}

	if(dateFinish.datepicker("getDate") === null) {
		dateFinish.addClass( "formFieldError" );
		showErrorMsg('<g:message code="emptyFieldMsg"/>')
		return false
	}

	if(dateFinish.datepicker("getDate") < new Date()) {
		showErrorMsg('<g:message code="dateFinishBeforeTodayERRORMsg"/>') 
		dateFinish.addClass("formFieldError");
		return false
	}

	if('' == manifestEditorDivContent.trim()) {
		showErrorMsg('<g:message code="eventContentEmptyERRORMsg"/>') 
		manifestEditorDiv.addClass("formFieldError");
		return false
	}
	return true
}

function showErrorMsg(errorMsg) {
	$("#manifestProtocolSinulationDataDialog .errorMsgWrapper").html('<p>' + errorMsg + '<p>')
	$("#manifestProtocolSinulationDataDialog .errorMsgWrapper").fadeIn()
}
	
</r:script>
	<r:layoutResources />