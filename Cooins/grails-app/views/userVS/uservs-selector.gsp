<vs:webresource dir="polymer" file="polymer.html"/>
<vs:webresource dir="core-ajax" file="core-ajax.html"/>
<vs:webresource dir="paper-input" file="paper-input.html"/>>

<polymer-element name="uservs-selector" attributes="url isSelector">
    <template>
        <style>
            /*Android browser doesn't fetch this properties from cooins.css*/
            .tableHeadervs {
                margin: 0px 0px 0px 0px; color:#6c0404; border-bottom: 1px solid #6c0404;
                background: white; font-weight: bold; padding:5px 0px 5px 0px; width: 100%;
            }
            .tableHeadervs div { text-align:center; }
            .rowvs { border-bottom: 1px solid #ccc; padding: 10px 0px 10px 0px;  width: 100%; }
            .rowvs div { text-align:center; cursor: pointer;  }
        </style>
        <core-ajax id="ajax" auto url="{{url}}" response="{{responseData}}" handleAs="json" method="get" on-core-response="{{responseDataReceived}}"
                   contentType="json"></core-ajax>
        <div layout vertical center>
            <div style="width:100%; display: {{userVSList.length == 0? 'none':'block'}};">
                <div layout horizontal center center justified class="tableHeadervs">
                    <div flex><g:message code="nifLbl"/></div>
                    <div flex><g:message code="nameLbl"/></div>
                </div>
                <div>
                    <template repeat="{{uservs in userVSList}}">
                        <div class="rowvs" layout horizontal center center justified on-click="{{showUserDetails}}">
                            <div flex>{{uservs.nif}}</div>
                            <div flex>{{uservs.name}}</div>
                        </div>
                    </template>
                </div>
            </div>

            <div class="center" id="emptySearchMsg" style="font-size: 1em; font-weight: bold;
                    display: {{responseData.userVSList.length == 0? 'block':'none'}};">
                <g:message code="emptyUserSearchResultMsg"/>
            </div>
        </div>
    </template>
    <script>
        Polymer('uservs-selector', {
            isSelector:false,
            ready: function() {
                this.url = this.url || '';
                this.userVSList = []
            },
            urlChanged:function(e) {},
            showUserDetails:function(e) {
                this.fire("user-clicked", e.target.templateInstance.model.uservs)
                this.fire('core-signal', {name: "user-clicked", data: e.target.templateInstance.model.uservs});
            },
            reset: function() {
                this.userVSList = []
            },
            responseDataReceived: function() {
                console.log(this.tagName + " - responseDataReceived - num. users: " + this.responseData.userVSList.length)
                this.userVSList = this.responseData.userVSList
            }
        });
    </script>
</polymer-element>