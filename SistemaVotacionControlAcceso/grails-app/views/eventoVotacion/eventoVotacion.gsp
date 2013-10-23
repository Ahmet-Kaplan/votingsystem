<%@ page import="grails.converters.JSON" %>
<%@ page import="org.sistemavotacion.controlacceso.modelo.*" %>
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
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<meta name="layout" content="main" />
	<script type="text/javascript">
		var votingEvent = ${eventMap as JSON} 
		var selectedOption
		var pendingOperation
		$(function() {
		  		$(".voteOptionButton").click(function () { 
		  			$("#optionSelectedDialogMsg").text($(this).attr("optionContent"))
		  			selectedOption = {id:$(this).attr("optionId"), 
		   			contenido:$(this).attr("optionContent")}
		  			console.log(" - selectedOption: " +  JSON.stringify(selectedOption))
		  			$("#confirmOptionDialog").dialog("open");
		  		});
		
		  		$("#adminDocumentLink").click(function () {
		  			$("#adminDocumentDialog").dialog("open");
		   	})
		
		  		$("#selectDeleteDocument").click(function () {
		  			if($("#selectCloseDocument").is(':checked')) {
		  				$("#selectCloseDocument").prop('checked', false);
		   		}
		   	})
		   	
		  		$("#selectCloseDocument").click(function () {
		  			if($("#selectDeleteDocument").is(':checked')) {
		  				$("#selectDeleteDocument").prop('checked', false);
		   		}
		   	})
		
		
			if(DocumentState.PENDIENTE_COMIENZO == '${eventMap?.estado}') {
			$("#eventMessagePanel").find('.messageContent').text("<g:message code='eventPendingLbl'/>")
			$("#eventMessagePanel").css("border-color", "#fba131")
			$("#eventMessagePanel").css("color", "#fba131")
			$("#eventMessagePanel").fadeIn(1000)
		
		} else if(DocumentState.FINALIZADO == '${eventMap?.estado}') {
			$("#adminDocumentLink").css("display", "none")
			$("#eventMessagePanel").find('.messageContent').text("<g:message code='eventFinishedLbl'/>")
			$("#eventMessagePanel").css("border-color", "#cc1606")
			$("#eventMessagePanel").css("color", "#cc1606")
			$("#eventMessagePanel").fadeIn(1000)
			
		} else if(DocumentState.CANCELADO == '${eventMap?.estado}') {
					$("#adminDocumentLink").css("display", "none")
					$("#eventMessagePanel").find('.messageContent').text("<g:message code='eventCancelledLbl'/>")
					$("#eventMessagePanel").css("border-color", "#cc1606")
					$("#eventMessagePanel").css("color", "#cc1606")
					$("#eventMessagePanel").addClass("eventMessageCancelled");
					$("#eventMessagePanel").fadeIn(1000)
				}
		 });
		         
		
		function sendVote() {
			console.log("sendVote")
		   	var webAppMessage = new WebAppMessage(
			    	StatusCode.SC_PROCESANDO, 
			    	Operation.ENVIO_VOTO_SMIME)
		   	webAppMessage.nombreDestinatarioFirma="${grailsApplication.config.SistemaVotacion.serverName}"
		webAppMessage.urlServer="${grailsApplication.config.grails.serverURL}"
		votingEvent.urlSolicitudAcceso = "${grailsApplication.config.grails.serverURL}/solicitudAcceso"
		votingEvent.urlRecolectorVotosCentroControl = "${eventMap?.centroControl?.serverURL}/voto"
		votingEvent.opcionSeleccionada = selectedOption
		webAppMessage.evento = votingEvent
		webAppMessage.urlTimeStampServer = "${createLink(controller:'timeStamp', absolute:true)}"
			pendingOperation = Operation.ENVIO_VOTO_SMIME
			//console.log(" - webAppMessage: " +  JSON.stringify(webAppMessage))
			votingSystemApplet.setMessageToSignatureClient(JSON.stringify(webAppMessage)); 
		}
		
		function setMessageFromSignatureClient(appMessage) {
			console.log("setMessageFromSignatureClient - message from native client: " + appMessage);
			$("#loadingVotingSystemAppletDialog").dialog("close");
			if(appMessage != null) {
				votingSystemAppletLoaded = true;
				var appMessageJSON
				if( Object.prototype.toString.call(appMessage) == '[object String]' ) {
					appMessageJSON = JSON.parse(appMessage);
				} else {
					appMessageJSON = appMessage
				} 
				var statusCode = appMessageJSON.codigoEstado
				if(StatusCode.SC_PROCESANDO == statusCode){
					$("#loadingVotingSystemAppletDialog").dialog("close");
					$("#workingWithAppletDialog").dialog("open");
				} else {
					$("#workingWithAppletDialog" ).dialog("close");
					var caption
					var msgTemplate
					var msg = appMessageJSON.mensaje
					if(Operation.ENVIO_VOTO_SMIME == pendingOperation) {
						caption = '<g:message code="voteERRORCaption"/>'
						msgTemplate = "<g:message code='voteResultMsg'/>"
						if(StatusCode.SC_OK == statusCode) { 
							caption = "<g:message code='voteOKCaption'/>"
							msg = msgTemplate.format(
									'<g:message code="voteResultOKMsg"/>',
									appMessageJSON.mensaje);
						} else if(StatusCode.SC_ERROR_VOTO_REPETIDO == statusCode) {
							var msgTemplate1 =  "<g:message code='accessRequestRepeatedMsg'/>" 
							msg = msgTemplate.format(
									msgTemplate1.format('${eventMap?.asunto}'), 
						appMessageJSON.mensaje);
			}
		} else if(Operation.CANCELAR_EVENTO == pendingOperation) {
			if(StatusCode.SC_OK == statusCode) { 
				caption = "<g:message code='operationOKCaption'/>"
				msgTemplate = "<g:message code='documentCancellationOKMsg'/>"
				msg = msgTemplate.format('${eventMap?.asunto}');
						} else {
							caption = "<g:message code='operationERRORCaption'/>"
						}
					}
					showResultDialog(caption, msg)
				}
			}
		}
	</script>
</head>
<body>

	<div id="eventMessagePanel" class="eventMessagePanel" style="display:none;">
		<p class="messageContent"></p>
	</div>

	<div class="publishPageTitle" style="margin:0px 0px 0px 0px;">
		<p style="margin: 0px 0px 0px 0px; text-align:center;">
			${eventMap?.asunto}
		</p>
	</div>
	
	<div style="display:inline-block; width:100%; font-size:0.8em;">
		<div style="display:inline;margin:0px 20px 0px 20px;">
			<b><g:message code="dateLimitLbl"/>: </b>${eventMap?.fechaFin}
		</div>
		<div id="adminDocumentLink" class="appLink" style="float:right;margin:0px 20px 0px 0px;">
			<g:message code="adminDocumentLinkLbl"/>
		</div>
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

		
	<div class="userAdvert">
		<ul>
			<li><g:message code="dniConnectedMsg"/></li>
			<li><g:message code="appletAdvertMsg"/></li>
			<li><g:message code="javaInstallAdvertMsg"/></li>
		</ul>
	</div>		

<g:include controller="gsp" action="index" params="[pageName:'confirmOptionDialog']"/>   
<g:include controller="gsp" action="index" params="[pageName:'adminDocumentDialog']"/> 	
</body>
</html>