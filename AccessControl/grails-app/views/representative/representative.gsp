<html>
<head>
    <g:render template="/template/pagevs"/>
    <link rel="import" href="<g:createLink  controller="element" params="[element: '/representative/representative-info']"/>">
</head>
<body>
    <votingsystem-innerpage-signal title="<g:message code="representativeLbl"/>"></votingsystem-innerpage-signal>
    <div class="pageContentDiv">
        <representative-info id="representative" representative="${representativeMap as grails.converters.JSON}"></representative-info>
    </div>
</body>
</html>
<asset:script>
</asset:script>