<!DOCTYPE HTML>
<html>
<head>
    <meta name="layout" content="simplePage" />
    <!--https://github.com/josdejong/jsoneditor-->
    <link rel="stylesheet" type="text/css" href="${resource(dir: '/bower_components/jsoneditor', file: 'jsoneditor.min.css')}">
    <script type="text/javascript" src="${resource(dir: '/bower_components/jsoneditor', file: 'jsoneditor.min.js')}"></script>
    <meta name="viewport" content="width=device-width, initial-scale=1">
</head>

<body>
    <div layout flex horizontal wrap around-justified>
        <div layout vertical>
            <votingsystem-button onclick="sendDepositFromVicketSource()" style="margin: 0px 0px 0px 5px;">
                Deposit from Vicket Source
            </votingsystem-button>
            <div id="depositFromVicketSource" style="width: 500px; height: 300px;"></div>
        </div>

        <div layout vertical>
            <div layout horizontal center center-justified>
                <votingsystem-button style="margin: 0px 0px 0px 5px;">Deposit </votingsystem-button>
                - http://vickets:8086/Vickets/transaction/deposit
            </div>
            <div id="depositEditor" style="width: 500px; height: 300px;"></div>
        </div>

        <div layout vertical>
            New Vicket Source
            <div id="newVicketSourceEditor" style="width: 500px; height: 400px;"></div>
        </div>

    </div>
</body>
</html>
<asset:script>
    //date -> 'yyyy/MM/dd HH:mm:ss'
    var depositFromVicketSource = new JSONEditor(document.querySelector("#depositFromVicketSource"));
    var signedContent
    var jsonDepositFromVicketSource = {operation:"VICKET_DEPOSIT_FROM_VICKET_SOURCE",
        signedMessageSubject:"Deposit from Vicket Source",
        signedContent:{operation:"VICKET_DEPOSIT_FROM_VICKET_SOURCE", fromUser: "Implantación proyecto Vickets",
            fromUserIBAN:"ES1877777777450000000050", toUserIBAN:"ES8378788989450000000015",
            amount:"30000", tags:[{name:"HIDROGENO"}], validTo:"2014/09/21 00:00:00", subject:"Ingreso viernes 25", currency:"EUR" },
        serviceURL:"${createLink( controller:'transactionVS', action:"deposit", absolute:true)}",
        serverURL:"${grailsApplication.config.grails.serverURL}",
        urlTimeStampServer:"${grailsApplication.config.VotingSystem.urlTimeStampServer}",
        }
    //fromUserIBAN -> IBAN external to Vicket System controlled by VicketSource
    //if validTo present -> change to limited in time Vickets
    depositFromVicketSource.set(jsonDepositFromVicketSource);


    function sendDepositFromVicketSource() {
        console.log("depositFromVicketSource")
        var webAppMessage = depositFromVicketSource.get();
        webAppMessage.statusCode = ResponseVS.SC_PROCESSING
        var objectId = Math.random().toString(36).substring(7)
        window[objectId] = {setClientToolMessage: function(appMessage) {
            console.log("sendDepositFromVicketSource - message: " + appMessage);
            appMessageJSON = JSON.parse(appMessage)
            showMessageVS(JSON.stringify(appMessageJSON.message), "sendDepositFromVicketSource " + appMessageJSON.statusCode)
        }}
        webAppMessage.callerCallback = objectId
        VotingSystemClient.setJSONMessageToSignatureClient(webAppMessage);
    }



    var depositEditor = new JSONEditor(document.querySelector("#depositEditor"));
    var jsonDepositEditor = {  "operation": "VICKET_DEPOSIT_FROM_GROUP_TO_MEMBER", "amount": "10", "fromUser":
        "Cheques comida &apos;proyecto Vickets&apos;", "subject": "Transacción 'sábado' \"21 junio\" a 20C",
        "fromUserIBAN": "ES8978788989450000000004", "toUserIBAN":  [ "ES9478788989450000000011"], "currency": "EUR",
        "validTo": "2014/06/23 00:00:00"}
    depositEditor.set(jsonDepositEditor);



    var vicketSourceEditor = new JSONEditor(document.querySelector("#newVicketSourceEditor"));
    var certChain = "-----BEGIN CERTIFICATE-----\n" +
            "MIICuzCCAiSgAwIBAgIGAUZWGs81MA0GCSqGSIb3DQEBBQUAMD4xLDAqBgNVBAMM\n" +
            "I1ZvdGluZyBTeXN0ZW0gQ2VydGlmaWNhdGUgQXV0aG9yaXR5MQ4wDAYDVQQLDAVD\n" +
            "ZXJ0czAeFw0xNDA2MDEwNjI1MzNaFw0xNTAxMTgxNzU4NTNaMDoxEjAQBgNVBAUT\n" +
            "CTkwMDAwMDAwQjEkMCIGA1UEAxMbVm90aW5nIFN5c3RlbSBWaWNrZXQgU2VydmVy\n" +
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDAStOOwaNJ5egUrspY5tnT7asX\n" +
            "dcbwtOOFRrHFQJVUAry9CQBDBMl1UWhQ6dDyza/Etucn0RNKwe0V/YujVtALlSv8\n" +
            "PjSiEeC/MVXLIcF3VycydB6JO9Tb1jyt7edw2XawsOp6geGGbuCR2fLgn0bs3US4\n" +
            "6TyS+Yv4PAVh7m6AzQIDAQABo4HHMIHEMG0GA1UdIwRmMGSAFPDThkkuJ1MxJINP\n" +
            "guo7UNa0tkYpoUKkQDA+MSwwKgYDVQQDDCNWb3RpbmcgU3lzdGVtIENlcnRpZmlj\n" +
            "YXRlIEF1dGhvcml0eTEOMAwGA1UECwwFQ2VydHOCCFz8lN5fdijOMB0GA1UdDgQW\n" +
            "BBR3dG6BiPLQOll9QIJLirtT6tglJzAMBgNVHRMBAf8EAjAAMA4GA1UdDwEB/wQE\n" +
            "AwIFoDAWBgNVHSUBAf8EDDAKBggrBgEFBQcDCDANBgkqhkiG9w0BAQUFAAOBgQAx\n" +
            "XjUMuqVL7SYYlGyhJt61MW39O5jG6eXkDvbrBvvpi3In1wUQheh8SXws8BrsHyGx\n" +
            "iDMkmC/CTxCxAz9ASd+LxFWw/vfZ1NCd1muYkEpX7TPk07sNDGkF0K0wCBic6o2t\n" +
            "t3UeDZwwOEWugyyp2cDDI/16ApdAsorhs0urCo21dg==\n" +
            "-----END CERTIFICATE-----"
    var jsonVicketSourceEditor = {
        operation:"VICKET_SOURCE_NEW", info:"Información de la nueva fuente de Vickets", IBAN:"ES8378788989450000000015",
        certChainPEM:certChain,

    };
    vicketSourceEditor.set(jsonVicketSourceEditor);
</asset:script>
<asset:deferredScripts/>