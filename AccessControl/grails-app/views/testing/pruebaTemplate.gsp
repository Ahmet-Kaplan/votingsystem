<!DOCTYPE html>
<html>
<head>
  	<title>pruebaTemplate</title>
   	<r:require modules="application"/>
    <style>
	  	textarea { }
	  	input[id="asunto"] { }
  	</style>
  	<r:script>
	  	$(document).ready(function(){
	  		$('#testForm').submit(function(event){event.preventDefault();});
	
		  	$("#submitButton").click(function(){});
	  	});
  	</r:script>
</head>
<body>
	<form id="testForm" style="display:block;margin:20px auto 30px auto; width:40%;">
		    <label for="one">URL: </label>
		    	<input type="text" id="urlCentroControl">
		    <button id="submitButton">Submit</button>
	</form>
	<div><input id="date" type="date" name="date" /></div>
</body>
</html>