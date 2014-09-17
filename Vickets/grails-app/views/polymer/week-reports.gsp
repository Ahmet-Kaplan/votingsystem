<link rel="import" href="${resource(dir: '/bower_components/core-ajax', file: 'core-ajax.html')}">
<link rel="import" href="<g:createLink  controller="polymer" params="[element: '/polymer/uservs-balance']"/>">
<link rel="import" href="<g:createLink  controller="polymer" params="[element: '/polymer/balance-details']"/>">
<link rel="import" href="${resource(dir: '/bower_components/core-signals', file: 'core-signals.html')}">

<polymer-element name="week-reports" attributes="url params-data">
    <template>
        <style no-shim>
            .sectionHeader {
                text-align: center;
                color: #6c0404;
                font-size: 1.2em;
                font-weight: bold;
                margin:20px 0px 0px 0px;
            }
        </style>
        <core-ajax id="ajax" auto url="{{url}}" response="{{weekReport}}" handleAs="json" contentType="json"
                   on-core-complete="{{ajaxComplete}}" on-core-response="{{coreResponse}}"/>
        <core-signals on-core-signal-uservs-balance-show-details="{{showBalanceDetails}}"
                      on-core-signal-balance-details-closed="{{closeBalanceDetails}}"></core-signals>

            <div layout horizontal center center-justified>
                <template bind="{{weekReport.userBalances}}">
                    <div>
                        <uservs-balance class="_uservs" balance="{{systemBalance}}"></uservs-balance>

                    </div>
                </template>
            </div>

            <div class="sectionHeader"><g:message code="bankVSsLbl"/></div>
            <div layout flex horizontal wrap around-justified>
                <template repeat="{{weekReport.userBalances.bankVS}">
                    <uservs-balance class="_vicketSource" balabankVStSource}}"></user-bbankVS            </template>
            </div>

            <div class="sectionHeader"><g:message code="groupsLbl"/></div>
            <div layout flex horizontal wrap around-justified>
                <template repeat="{{group in weekReport.userBalances.groupBalanceList}}">
                    <uservs-balance class="_group" balance="{{group}}"></uservs-balance>
                </template>
            </div>

            <div class="sectionHeader"><g:message code="usersLbl"/></div>
            <div layout flex horizontal wrap around-justified>
                <template repeat="{{user in weekReport.userBalances.userBalanceList}}">
                    <uservs-balance class="_uservs" balance="{{user}}"></uservs-balance>
                </template>
            </div>

            <section>
                <balance-details id="balanceDetails"></balance-details>
            </section>

    </template>

    <script>
        Polymer('week-reports', {
            ready: function() {
                console.log(this.tagName + " - ready - url: " + this.url)
                this.$.ajax.url = this.url
            },
            weekReportChanged: function() {
                console.log(this.tagName + " - weekReportChanged")

                this.weekReport.userBalances.userBalanceList.forEach(function(entry) {
                    //console.log(" entry: " + JSON.stringify(entry))
                })
                // `async` lets the main loop resume and perform tasks, like DOM updates, then it calls your callback
                this.async(function() { this.calculateStatistics()});
            },
            ajaxComplete:function(e) {
                console.log(this.tagName + " - ajaxComplete - " + e.detail.xhr.status + " - id: " + e.target.id)

            },
            coreResponse:function(xhr) {
                console.log(this.tagName + " - coreResponse")
            },
            calculateStatistics:function() {
                var userBalances = this.shadowRoot.querySelectorAll("uservs-balance._uservs")
                var groupBalances = this.shadowRoot.querySelectorAll("uservs-balance._group")
                var vicketSourceBalances = this.shbankVSrySelectorAll("uservs-balance._vicketSource")

            },
            showBalanceDetails:function(e, detail, sender) {
                this.$.balanceDetails.balance = detail
                this.$.balanceDetails.opened = true

            },
            closeBalanceDetails:function(e, detail, sender) {
                this.page = 0;
            }

        });
    </script>
</polymer-element>