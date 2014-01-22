<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
			<r:require modules="application"/>
		<title><g:message code="simulationWebAppCaption"/></title>
        <r:external uri="/images/euro_16.png"/>
		<style type="text/css" media="screen"></style>
	</head>
	<body>
		<div class="pageContent" style="position:relative;">
			<div style="width: 50%;height: 50%;overflow: auto;margin: auto;top: 0; left: 0; bottom: 0; right: 0;">

                <votingSystem:simpleButton id="initUserBaseDataButton"
                        onclick="openWindow('${createLink(controller: 'ticket', action:'initUserBaseData', absolute:true)}')"
                        style="margin:15px 20px 0px 0px; width:400px;">
                    <g:message code="initUserBaseDataButton"/>
                </votingSystem:simpleButton>

                <votingSystem:simpleButton id="makeDepositButton"
                        onclick="openWindow('${createLink(controller: 'ticket', action:'deposit', absolute:true)}')"
                        style="margin:15px 20px 0px 0px; width:400px;">
                    <g:message code="makeDepositButton"/>
                </votingSystem:simpleButton>

				<votingSystem:simpleButton id="simulationRunningButton" style="margin:15px 20px 0px 0px; width:400px;">
						TEST
				</votingSystem:simpleButton>
				
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
