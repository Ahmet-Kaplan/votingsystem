

        <style type="text/css" media="screen">
         .controllerInfoHeader {
	     	color: #4c4c4c;
	     }
	     .params_result_div {
	     	margin:0px 0px 0px 20px;
	     }
	     .serviceURLS {
	     	margin:10px 0px 20px 200px;
	     	color: #4c4c4c;
	     }
        </style>

<h3 class="controllerInfoHeader"><u>Solicitud de copias de seguridad</u></h3>

 Servicios que gestiona solicitudes de copias de seguridad.
 
  

<div>

	<HR>
	
		
		
			<p>
				- <u>GET</u> - 
				<a href="${request.scheme}://${request.serverName}:${request.serverPort}${request.getContextPath()}/solicitudCopia/index">/solicitudCopia/index</a><br/>
				
	  <br/>
			</p>
			<div class="params_result_div">
			
			
			
			
			
			
			
			
				
			
			
			 
			
			
				<p><b>Respuesta:</b><br/>Información sobre los servicios que tienen como url base '/solicitudCopia'.</p>
			
			</div>
		<HR>
	
		
		
			<p>
				- <u>GET</u> - 
				<a href="${request.scheme}://${request.serverName}:${request.serverPort}${request.getContextPath()}/solicitudCopia/obtener">/solicitudCopia/obtener</a><br/>
				
	  Servicio que proporciona la copia de seguridad a partir de la URL que se envía
	  al solicitante en el mail de confirmación que recibe al enviar la solicitud.
	  
	  <br/>
			</p>
			<div class="params_result_div">
			
			
			
			
			
			
				<p>
					<b>Parámetros:</b><br/>
					
						<u>id</u>: Obligatorio. El identificador de la solicitud de copia de seguridad la base de datos.<br/>
					
				</p>
			
			
			
				
			
			
			 
			
			
				<p><b>Respuesta:</b><br/>Archivo zip con la copia de seguridad.</p>
			
			</div>
		<HR>
	
		
		
			<p>
				- <u>GET</u> - 
				<a href="${request.scheme}://${request.serverName}:${request.serverPort}${request.getContextPath()}/solicitudCopia/obtenerSolicitud">/solicitudCopia/obtenerSolicitud</a><br/>
				
	  Servicio que proporciona copias de las solicitudes de copias de seguridad recibidas.
	 
	  <br/>
			</p>
			<div class="params_result_div">
			
			
			
			
			
			
				<p>
					<b>Parámetros:</b><br/>
					
						<u>id</u>: Obligatorio. El identificador de la solicitud de copia de seguridad la base de datos.<br/>
					
				</p>
			
			
			
				
			
			
			 
			
			
				<p><b>Respuesta:</b><br/>El PDF en el que se solicita la copia de seguridad.</p>
			
			</div>
		<HR>
	
		
		
			<p>
				- <u>POST</u> - 
				<a href="${request.scheme}://${request.serverName}:${request.serverPort}${request.getContextPath()}/solicitudCopia/validarSolicitud">/solicitudCopia/validarSolicitud</a><br/>
				
	  Servicio que recibe solicitudes de copias de seguridad
	 
	  <br/>
			</p>
			<div class="params_result_div">
			
			
			
			
			
			
				<p>
					<b>Parámetros:</b><br/>
					
						<u>signedPDF</u>: Archivo PDF con los datos de la copia de seguridad.<br/>
					
				</p>
			
			
			
				
			
			
			 
			
			
				<p><b>Respuesta:</b><br/>Si todo va bien devuelve un código de estado HTTP 200. Y el solicitante recibirá un
	          email con información para poder obtener la copia de seguridad.</p>
			
			</div>
		<HR>
	

</div>