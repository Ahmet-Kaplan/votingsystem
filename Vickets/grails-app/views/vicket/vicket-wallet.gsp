<link rel="import" href="${resource(dir: '/bower_components/polymer', file: 'polymer.html')}">
<link rel="import" href="${resource(dir: '/bower_components/paper-slider', file: 'paper-slider.html')}">
<link rel="import" href="${resource(dir: '/bower_components/paper-dropdown-menu', file: 'paper-dropdown-menu.html')}">
<link rel="import" href="${resource(dir: '/bower_components/core-tooltip', file: 'core-tooltip.html')}">
<link rel="import" href="<g:createLink  controller="element" params="[element: '/vicket/vicket-wallet-tag-group']"/>">


<polymer-element name="vicket-wallet"  on-core-select="{{selectAction}}">
    <template>
        <g:include view="/include/styles.gsp"/>
        <style>
            body /deep/ paper-dropdown-menu.narrow { max-width: 200px; width: 300px; }
            .green-slider paper-slider::shadow #sliderKnobInner,
            .green-slider paper-slider::shadow #sliderKnobInner::before,
            .green-slider paper-slider::shadow #sliderBar::shadow #activeProgress {
                background-color: #0f9d58;
            }
            .messageToUser {font-weight: bold;margin:10px auto 30px auto;
                background: #f9f9f9;padding:10px 20px 10px 20px; max-width:400px;
            }
        </style>
        <div vertical layout>
            <template if="{{messageToUser}}">
                <div style="color: {{status == 200?'#388746':'#ba0011'}};">
                    <div class="messageToUser">
                        <div  layout horizontal center center-justified style="margin:0px 10px 0px 0px;">
                            <core-icon icon="{{status == 200?'check':'error'}}" style="fill:{{status == 200?'#388746':'#ba0011'}};"></core-icon>
                            <div id="messageToUser">{{messageToUser}}</div>
                        </div>
                        <paper-shadow z="1"></paper-shadow>
                    </div>
                </div>
            </template>
        </div>
        <template repeat="{{tag in tagArray}}">
            <vicket-wallet-tag-group tag={{tag}} vicketArray="{{tagGroups[tag]}}"></vicket-wallet-tag-group>
        </template>
    </template>
    <script>
        Polymer('vicket-wallet', {
            selectedTags: [],
            currencyCode:null,
            vicketsWalletArray:[],
            tagGroups:{},
            tagArray:[],
            messageToUser:null,

            ready: function() {
                console.log(this.tagName + " - ready")
                var webAppMessage = new WebAppMessage(ResponseVS.SC_PROCESSING, Operation.WALLET_OPEN)
                webAppMessage.setCallback(function(appMessage) {
                    var appMessageJSON = JSON.parse(appMessage)
                    if(ResponseVS.SC_OK == appMessageJSON.statusCode) {
                        this.loadWallet(appMessageJSON.message)
                    } else {
                        var caption = '<g:message code="vicketRequestERRORCaption"/>'
                        showMessageVS(appMessageJSON.message, caption)
                    }
                }.bind(this))
                VotingSystemClient.setJSONMessageToSignatureClient(webAppMessage);
            },
            loadWallet:function(vicketsWalletArray) {
                console.log(this.tagName + " - loadWallet")
                this.walletStr = JSON.stringify(vicketsWalletArray)
                this.tagGroups = {}
                this.vicketsWalletArray = vicketsWalletArray
                for(vicketIdx in this.vicketsWalletArray) {
                    var vicket = this.vicketsWalletArray[vicketIdx]
                    if(this.tagGroups[vicket.tag]) this.tagGroups[vicket.tag].push(vicket)
                    else this.tagGroups[vicket.tag] = [vicket]
                }
                this.tagArray = Object.keys(this.tagGroups)
            },
            valueChanged:function() {
                this.amountValue = this.value * 10;
            }
        });
    </script>
</polymer-element>