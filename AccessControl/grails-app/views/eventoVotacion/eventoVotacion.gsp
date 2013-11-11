<%@ page import="grails.converters.JSON" %>
<%@ page import="org.votingsystem.accesscontrol.model.*" %>
<%
	def messageToUser = null
	def eventClass = null
	if(Evento.Estado.FINALIZADO.toString().equals(eventMap?.estado)) {
		messageToUser =  message(code: 'eventFinishedLbl')
		eventClass = "eventFinishedBox"
	} else if(Evento.Estado.PENDIENTE_COMIENZO.toString().equals(eventMap?.estado)) {
		messageToUser = message(code: 'eventPendingLbl')
		eventClass = "eventPendingBox"
	} else if(Evento.Estado.CANCELADO.toString().equals(eventMap?.estado)) {
		messageToUser = message(code: 'eventCancelledLbl')
		eventClass = "eventFinishedBox"
	}
%>
<html>
<head><meta name="layout" content="main" /></head>
<body>

	<g:if test="${messageToUser != null}">
		<div id="eventMessagePanel" class="eventMessagePanel">
			<p class="messageContent">
				${messageToUser}
			</p>
		</div>
	</g:if>

	<div class="publishPageTitle" style="margin:0px 0px 0px 0px;">
		<p style="margin: 0px 0px 0px 0px; text-align:center;">
			${eventMap?.asunto}
		</p>
	</div>
	
	<div style="display:inline-block; width:100%; font-size:0.8em;">
		<div style="display:inline;margin:0px 20px 0px 20px;">
			<b><g:message code="dateLimitLbl"/>: </b>${eventMap?.fechaFin}
		</div>
		<g:if test="${Evento.Estado.ACTIVO.toString() == eventMap?.estado ||
			Evento.Estado.PENDIENTE_COMIENZO.toString()}">			
			<div id="adminDocumentLink" class="appLink" style="float:right;margin:0px 20px 0px 0px;">
				<g:message code="adminDocumentLinkLbl"/>
			</div>
		</g:if>
	</div>

	<div class="eventPageContentDiv">
		<div style="width:100%;position:relative;">
			<div class="eventContentDiv">${eventMap?.contenido}</div>
		</div>
		
		<div style="width:100%;position:relative;margin:0px 0px 0px 0px;">
			<div id="eventAuthorDiv"><b>
				<g:message code="publisshedByLbl"/>:</b>${eventMap?.usuario}
			</div>
		</div>
	
		<div class="eventOptionsDiv">
			<fieldset id="fieldsBox" style="">
				<legend id="fieldsLegend"><g:message code="pollFieldLegend"/></legend>
				<div id="fields" style="width:100%;">
					<g:if test="${Evento.Estado.ACTIVO.toString() == eventMap?.estado}">
						<g:each in="${eventMap?.opciones}">
							<div class="voteOptionButton button_base" 
								style="width: 90%;margin: 10px auto 0px auto;"
								optionId = "${it.id}" optionContent="${it.contenido}">
								${it.contenido}
							</div>
						</g:each>
					</g:if>
					<g:if test="${Evento.Estado.CANCELADO.toString() == eventMap?.estado ||
						Evento.Estado.FINALIZADO.toString() == eventMap?.estado ||
						Evento.Estado.PENDIENTE_COMIENZO.toString() == eventMap?.estado}">			
						<g:each in="${eventMap?.opciones}">
							<div class="voteOption" style="width: 90%;margin: 10px auto 0px auto;">
								 - ${it.contenido}
							</div>
						</g:each>
					</g:if>
				</div>
			</fieldset>
		</div>
	</div>

	<g:render template="/template/signatureMechanismAdvert"/>

<g:include view="/include/dialog/confirmOptionDialog.gsp"/>
<g:include view="/include/dialog/adminDocumentDialog.gsp"/>

</body>
</html>
<r:script>
<g:applyCodec encodeAs="none">
		var votingEvent = ${eventMap as JSON} 
		var selectedOption
		$(function() {
			if(${messageToUser != null?true:false}) { 
				$("#eventMessagePanel").addClass("${eventClass}");
			}

	  		$(".voteOptionButton").click(function () { 
	  			$("#optionSelectedDialogMsg").text($(this).attr("optionContent"))
	  			selectedOption = {id:$(this).attr("optionId"), 
	   			contenido:$(this).attr("optionContent")}
	  			console.log(" - selectedOption: " +  JSON.stringify(selectedOption))
	  			$("#confirmOptionDialog").dialog("open");
	  		});
		
	  		$("#adminDocumentLink").click(function () {
    			showAdminDocumentDialog(adminDocumentCallback)
		   	})

		 });
		         
		function sendVote() {
			console.log("sendVote")
		   	var webAppMessage = new WebAppMessage(
			    	StatusCode.SC_PROCESANDO, 
			    	Operation.ENVIO_VOTO_SMIME)
		   	webAppMessage.nombreDestinatarioFirma="${grailsApplication.config.VotingSystem.serverName}"
			webAppMessage.urlServer="${grailsApplication.config.grails.serverURL}"
			votingEvent.urlSolicitudAcceso = "${grailsApplication.config.grails.serverURL}/solicitudAcceso"
			votingEvent.urlRecolectorVotosCentroControl = "${eventMap?.centroControl?.serverURL}/voto"
			votingEvent.opcionSeleccionada = selectedOption
			webAppMessage.evento = votingEvent
			webAppMessage.urlTimeStampServer = "${createLink(controller:'timeStamp', absolute:true)}"
			//console.log(" - webAppMessage: " +  JSON.stringify(webAppMessage))
			votingSystemClient.setMessageToSignatureClient(webAppMessage, sendVoteCallback); 
		}

		function sendVoteCallback(appMessage) {
			console.log("sendVoteCallback - message from native client: " + appMessage);
			var appMessageJSON = toJSON(appMessage)
			if(appMessageJSON != null) {
				$("#workingWithAppletDialog").dialog("close");
				caption = '<g:message code="voteERRORCaption"/>'
				var msgTemplate = "<g:message code='voteResultMsg'/>"
				var msg
				if(StatusCode.SC_OK == appMessageJSON.statusCode) { 
					caption = "<g:message code='voteOKCaption'/>"
					msg = msgTemplate.format(
							'<g:message code="voteResultOKMsg"/>',
							appMessageJSON.message);
				} else if(StatusCode.SC_ERROR_VOTO_REPETIDO == appMessageJSON.statusCode) {
					msgTemplate =  "<g:message code='accessRequestRepeatedMsg'/>" 
					msg = msgTemplate.format(
						msgTemplate1.format('${eventMap?.asunto}'), 
						appMessageJSON.message);
				} else msg = appMessageJSON.message
				showResultDialog(caption, msg)
			}
		}

		function adminDocumentCallback(appMessage) {
			console.log("adminDocumentCallback - message from native client: " + appMessage);
			var appMessageJSON = toJSON(appMessage)
			if(appMessageJSON != null) {
				$("#workingWithAppletDialog").dialog("close");
				var callBack
				if(StatusCode.SC_OK == appMessageJSON.statusCode) { 
					caption = "<g:message code='operationOKCaption'/>"
					msgTemplate = "<g:message code='documentCancellationOKMsg'/>"
					msg = msgTemplate.format('${eventMap?.asunto}');
					callBack = function() {
						window.location.href = "${createLink(controller:'eventoReclamacion')}/" + claimEvent.id;
					}
				}
				showResultDialog(caption, msg, callBack)
			}
		}
</g:applyCodec>
</r:script>