<link rel="import" href="${resource(dir: '/bower_components/polymer', file: 'polymer.html')}">
<link rel="import" href="${resource(dir: '/bower_components/votingsystem-dialog', file: 'votingsystem-dialog.html')}">
<link rel="import" href="<g:createLink  controller="element" params="[element: '/messageSMIME/message-smime']"/>">

<polymer-element name="message-smime-dialog" attributes="transactionvsURL opened">
    <template>
        <votingsystem-dialog id="xDialog" class="vicketTransactionDialog" on-core-overlay-open="{{onCoreOverlayOpen}}">
        <g:include view="/include/styles.gsp"/>
        <core-ajax id="ajax" auto url="{{transactionvsURL}}" response="{{transactionvs}}" handleAs="json" method="get" contentType="json"></core-ajax>
            <div style="display:{{isProcessing? 'block':'none'}}">
                <div layout vertical center center-justified  style="text-align:center;min-height: 150px;">
                    <progress style="margin:20px auto;"></progress>
                </div>
            </div>
            <div>
                <message-smime id="transactionViewer"></message-smime>
            </div>
        </votingsystem-dialog>
    </template>
    <script>
        Polymer('message-smime-dialog', {
            transactionvsURL:null,
            isProcessing:true,
            ready: function() {
                console.log(this.tagName + " - " + this.id + " - ready")
                this.isClientToolConnected = window['isClientToolConnected']
            },
            transactionvsChanged:function() {
                this.transactionvsURL = ""
                this.$.transactionViewer.signedDocument = this.transactionvs.signedContentMap
                this.$.transactionViewer.timeStampDate = this.transactionvs.timeStampDate
                this.$.transactionViewer.smimeMessage = this.transactionvs.smimeMessage
                this.isProcessing = false
                this.$.xDialog.opened = true
            },
            show: function(transactionvsURL) {
                this.isProcessing = true
                this.transactionvsURL = transactionvsURL
                console.log(this.tagName + " - show - transactionvsURL:" + this.transactionvsURL)
                this.$.xDialog.opened = true
            }
        });
    </script>
</polymer-element>