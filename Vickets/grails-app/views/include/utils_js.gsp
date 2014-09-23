<script>
    window['serverURL'] = "${grailsApplication.config.grails.serverURL}"
    window.CKEDITOR_BASEPATH = '${grailsApplication.config.grails.serverURL}/bower_components/ckeditor/';

    function WebAppMessage(statusCode, operation) {
        this.statusCode = statusCode
        this.operation = operation
        this.caption;
        this.message;
        this.subject ;
        this.signedContent;
        this.serviceURL;
        this.documentURL;
        this.receiverName = "${grailsApplication.config.VotingSystem.serverName}";
        this.serverURL = "${grailsApplication.config.grails.serverURL}";
        this.urlTimeStampServer = "${grailsApplication.config.VotingSystem.urlTimeStampServer}"
        this.objectId = Math.random().toString(36).substring(7);
    }

    WebAppMessage.prototype.setCallback = function(callbackFunction) {
        window[this.objectId] = callbackFunction;
    }

    //http://jsfiddle.net/cckSj/5/
    Date.prototype.getElapsedTime = function() {
        // time difference in ms
        var timeDiff = this - new Date();

        if(timeDiff <= 0) {
            return "<g:message code='timeFinsishedLbl'/>"
        }

        // strip the miliseconds
        timeDiff /= 1000;

        // get seconds
        var seconds = Math.round(timeDiff % 60);

        // remove seconds from the date
        timeDiff = Math.floor(timeDiff / 60);

        // get minutes
        var minutes = Math.round(timeDiff % 60);

        // remove minutes from the date
        timeDiff = Math.floor(timeDiff / 60);

        // get hours
        var hours = Math.round(timeDiff % 24);

        // remove hours from the date
        timeDiff = Math.floor(timeDiff / 24);

        // the rest of timeDiff is number of days
        var resultStr
        var days = timeDiff;
        if(days > 0) {
            resultStr = days + " " + "<g:message code="daysLbl"/>" + " " + "<g:message code="andLbl"/>" + " " + hours + " " + "<g:message code="hoursLbl"/>"
        } else if (hours > 0) {
            resultStr = hours + " " + "<g:message code="hoursLbl"/>" + " " + "<g:message code="andLbl"/>" + " " + minutes + " " + "<g:message code="minutesLbl"/>"
        } else if (minutes > 0) {
            resultStr = minutes + " " + "<g:message code="minutesLbl"/>" + " " + "<g:message code="andLbl"/>" + " " + seconds + " " + "<g:message code="secondsLbl"/>"
        }
        return resultStr
    };

    function getTransactionVSDescription(transactionType) {
        var transactionDescription = "transactionDescription"
        switch(transactionType) {
            case 'VICKET_REQUEST':
                transactionDescription = "<g:message code="selectVicketRequestLbl"/>"
                break;
            case 'VICKET_SEND':
                transactionDescription = "<g:message code="selectVicketSendLbl"/>"
                break;
            case 'VICKET_CANCELLATION':
                transactionDescription = "<g:message code="selectVicketCancellationLbl"/>"
                break;
            case 'INIT_PERIOD':
                transactionDescription = "<g:message code="vicketInitPeriodLbl"/>"
                break;
            case 'FROM_BANKVS':
                transactionDescription = "<g:message code="bankVSInputLbl"/>"
                break;
            case 'VICKET_DEPOSIT_FROM_GROUP_TO_MEMBER':
                transactionDescription = "<g:message code="vicketDepositFromGroupToMember"/>"
                break;
            case 'VICKET_DEPOSIT_FROM_GROUP_TO_MEMBER_GROUP':
                transactionDescription = "<g:message code="vicketDepositFromGroupToMemberGroup"/>"
                break;
            case 'VICKET_DEPOSIT_FROM_GROUP_TO_ALL_MEMBERS':
                transactionDescription = "<g:message code="vicketDepositFromGroupToAllMembers"/>"
                break;
        }
        return transactionDescription
    }

    window._originalAlert = window.alert;
    window.alert = function(text) {
        if (document.querySelector("#_votingsystemMessageDialog") != null && typeof
                document.querySelector("#_votingsystemMessageDialog").setMessage != 'undefined'){
            document.querySelector("#_votingsystemMessageDialog").setMessage(text,
                    "<g:message code="messageLbl"/>")
        }  else {
            console.log('utils_js.gsp - votingsystem-message-dialog not found');
            window._originalAlert(text);
        }
    }
</script>