<div style="display:none;">
    <div id="addVoteOptionDialog" title="<g:message code="addOptionLbl"/>"
         style="display:none;padding:30px 20px 30px 20px; display: table; margin:auto;">
        <label for="newOptionText" style=""><g:message code="pollOptionContentMsg"/></label>
        <form id="addVoteOptionForm">
            <input type="text" id="newOptionText" style="width:350px; margin:10px auto 0px auto;"
                   oninvalid="this.setCustomValidity('<g:message code="emptyFieldLbl"/>')"
                   onchange="this.setCustomValidity('')"
                   class="text ui-widget-content ui-corner-all" required/>
            <input id="submitOption" type="submit" style="display:none;">
        </form>
    </div>
</div>
<r:script>
var callerCallback

$('#addVoteOptionForm').submit(function(event){	
	event.preventDefault();
	console.log("addVoteOptionForm - addVoteOptionForm")
	$("#addVoteOptionDialog").dialog( "close" );
	if(!document.getElementById('newOptionText').validity.valid) {
		$("#newOptionText").addClass( "formFieldError" );
		showResultDialog('<g:message code="dataFormERRORLbl"/>', 
			'<g:message code="emptyFieldMsg"/>',function() {
				$("#addVoteOptionDialog").dialog("open")
   			})
	} else {
   		if(callerCallback != null) callerCallback($("#newOptionText").val())
   		else console.log("addVoteOptionDialog - missing callerCallback")
		$("#newOptionText").removeClass( "formFieldError" );
	}
})

function showAddVoteOptionDialog(callback) {
	$("#addVoteOptionDialog").dialog("open");
	callerCallback = callback	
}

$("#addVoteOptionDialog").dialog({
  	width: 450, autoOpen: false, modal: true,
    buttons: [{text:"<g:message code="acceptLbl"/>",
          	icons: { primary: "ui-icon-check"},
          	click:function() {
          		$("#submitOption").click() 	   	    							   	   			   				
       		}}, {
     		text:"<g:message code="cancelLbl"/>",
            	icons: { primary: "ui-icon-closethick"},
          	click:function() {
        		if(callerCallback != null) callerCallback(null)
        		else console.log("addVoteOptionDialog - missing callerCallback")
  				$(this).dialog( "close" );
      	 	}}],
    show: { effect: "fade", duration: 100 },
    hide: { effect: "fade", duration: 100 },
    open: function( event, ui ) {
 		$("#newOptionText").val("")
	  }
  });
</r:script>