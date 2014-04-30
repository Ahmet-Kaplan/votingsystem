<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
        <r:require modules="application"/>
		<title><g:message code="simulationWebAppCaption"/></title>
		<style type="text/css" media="screen"></style>
	</head>
	<body>
		<div class="pageContent" style="position:relative;">
			<div style="width: 50%;height: 50%;overflow: auto;margin: auto;top: 0; left: 0; bottom: 0; right: 0;">
                <a id="initElectionProtocolSimulationButton" href="${createLink(controller: 'electionProtocolSimulation', action:'inputData', absolute:true)}"
                   class="btn btn-default btn-lg" role="button" style="margin:15px 20px 0px 0px; width:400px;">
                    <g:message code="initElectionProtocolSimulationButton"/>
                </a>
                <a id="initManifestProtocolSimulationButton" href="${createLink(controller: 'manifestProtocolSimulation', action:'inputData', absolute:true)}"
                   class="btn btn-default btn-lg" role="button" style="margin:15px 20px 0px 0px; width:400px;">
                    <g:message code="initManifestProtocolSimulationButton"/>
                </a>
                <a id="initClaimProtocolSimulationButton" href="${createLink(controller: 'claimProtocolSimulation', action:'inputData', absolute:true)}"
                   class="btn btn-default btn-lg" role="button" style="margin:15px 20px 0px 0px; width:400px;">
                    <g:message code="initClaimProtocolSimulationButton"/>
                </a>
                <a id="initTimeStampProtocolSimulationButton" href="${createLink(controller: 'timeStampSimulation', action:'inputData', absolute:true)}"
                   class="btn btn-default btn-lg" role="button" style="margin:15px 20px 0px 0px; width:400px;">
                    <g:message code="initTimeStampProtocolSimulationButton"/>
                </a>
                <a id="initMultiSignProtocolSimulationButton" href="${createLink(controller: 'multiSignSimulation', action:'inputData', absolute:true)}"
                   class="btn btn-default btn-lg" role="button" style="margin:15px 20px 0px 0px; width:400px;">
                    <g:message code="initMultiSignProtocolSimulationButton"/>
                </a>
                <a id="initEncryptionProtocolSimulationButton" href="${createLink(controller: 'encryptionSimulation', action:'inputData', absolute:true)}"
                   class="btn btn-default btn-lg" role="button" style="margin:15px 20px 0px 0px; width:400px;">
                    <g:message code="initEncryptionProtocolSimulationButton"/>
                </a>
                <a id="simulationRunningButton" href="#" onclick="showSimulationRunningDialog('Mensaje de la página principal');"
                   class="btn btn-default btn-lg" role="button" style="margin:15px 20px 0px 0px; width:400px;">
                    TEST
                </a>

                <div style="margin: 30px 0 10px 0; font-weight: bold; font-size: 2em; color: #870000;">
                    <g:message code="vicketsOperationsLbl"/>
                </div>
                <a id="initUserBaseDataButton" href="${createLink(controller: 'vicket', action:'initUserBaseData', absolute:true)}"
                   class="btn btn-default btn-lg" role="button" style="margin:15px 20px 0px 0px; width:400px;">
                    <g:message code="initUserBaseDataButton"/>
                </a>
                <a id="makeDepositButton" href="${createLink(controller: 'vicket', action:'deposit', absolute:true)}"
                   class="btn btn-default btn-lg" role="button" style="margin:15px 20px 0px 0px; width:400px;">
                    <g:message code="makeDepositButton"/>
                </a>
			</div>
		</div>
	<div style="display:none;">
		<g:include view="/include/simulationRunningDialog.gsp" style="display:none;"/>
        <g:include view="/include/dialog/addClaimFieldDialog.gsp"/>
	</div>
	</body>
	<r:script>

		$(function() { });

        function openWindow(targetURL) {
            var width = 1000
            var height = 800
            var left = (screen.width/2) - (width/2);
            var top = (screen.height/2) - (height/2);
            var title = ''

            var newWindow =  window.open(targetURL, title, 'toolbar=no, scrollbars=yes, resizable=yes, '  +
                    'width='+ width +
                    ', height='+ height  +', top='+ top +', left='+ left + '');
        }

	</r:script>
</html>