

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

<h3 class="controllerInfoHeader"><u>Validación de solicitudes de certificación</u></h3>

 Servicios relacionados con validación y firma de certificados.
 
  

<div>

	<HR>
	
		
		
			<p>
				- <u>POST</u> - 
				<a href="${request.scheme}://${request.serverName}:${request.serverPort}${request.getContextPath()}/csr/anular">/csr/anular</a><br/>
				
	  (SERVICIO DISPONIBLE SOLO EN ENTORNOS DE PRUEBAS).
	  
	  Servicio que anula solicitudes de certificación de usuario.<br/>
	  
	  TODO - IMPLEMETAR. Hacer las validaciones sólo sobre solicitudes 
	  firmadas electrónicamente por personal dado de alta en la base de datos.
	  
	  <br/>
			</p>
			<div class="params_result_div">
			
			
			
			
			
			
				<p>
					<b>Parámetros:</b><br/>
					
						<u>userIfo</u>: Documento JSON con los datos del usuario 
	  <code>{deviceId:"000000000000000", telefono:"15555215554", nif:"1R" }</code><br/>
					
				</p>
			
			
			
				
			
			
			 
			
			
				<p><b>Respuesta:</b><br/>Si todo es correcto devuelve un código de estado HTTP 200.</p>
			
			</div>
		<HR>
	
		
		
			<p>
				- <u>POST</u> - 
				<a href="${request.scheme}://${request.serverName}:${request.serverPort}${request.getContextPath()}/csr/guardarValidacion">/csr/guardarValidacion</a><br/>
				
	  (SERVICIO DISPONIBLE SOLO EN ENTORNOS DE PRUEBAS).
	 
	  Servicio que firma solicitudes de certificación de usuario.<br/>
	 
	  TODO - Hacer las validaciones sólo sobre solicitudes firmadas electrónicamente
	  por personal dado de alta en la base de datos.
	 
	  <br/>
			</p>
			<div class="params_result_div">
			
			
			
			
			
			
				<p>
					<b>Parámetros:</b><br/>
					
						<u>archivoFirmado</u>: Archivo firmado en formato SMIME en cuyo contenido 
	  se encuentran los datos de la solicitud que se desea validar.
	  <code>{deviceId:"000000000000000", telefono:"15555215554", nif:"1R" }</code><br/>
					
				</p>
			
			
			
				
			
			
			 
			
			
				<p><b>Respuesta:</b><br/>Si todo es correcto devuelve un código de estado HTTP 200.</p>
			
			</div>
		<HR>
	
		
		
			<p>
				- <u>GET</u> - 
				<a href="${request.scheme}://${request.serverName}:${request.serverPort}${request.getContextPath()}/csr/index">/csr/index</a><br/>
				
	  <br/>
			</p>
			<div class="params_result_div">
			
			
			
			
			
			
			
			
				
			
			
			 
			
			
				<p><b>Respuesta:</b><br/>Información sobre los servicios que tienen como url base '/csr'.</p>
			
			</div>
		<HR>
	
		
		
			<p>
				- <u>GET</u> - 
				<a href="${request.scheme}://${request.serverName}:${request.serverPort}${request.getContextPath()}/csr/obtener">/csr/obtener</a><br/>
				
	  Servicio que devuelve las solicitudes de certificados firmadas una vez que
	  se ha validado la identidad del usuario.
	 
	  <br/>
			</p>
			<div class="params_result_div">
			
			
			
			
			
			
				<p>
					<b>Parámetros:</b><br/>
					
						<u>idSolicitudCSR</u>: Identificador de la solicitud de certificación enviada previamente por el usuario.<br/>
					
				</p>
			
			
			
				
			
			
			 
			
			
				<p><b>Respuesta:</b><br/>Si el sistema ha validado al usuario devuelve la solicitud de certificación firmada.</p>
			
			</div>
		<HR>
	
		
		
			<p>
				- <u>POST</u> - 
				<a href="${request.scheme}://${request.serverName}:${request.serverPort}${request.getContextPath()}/csr/solicitar">/csr/solicitar</a><br/>
				
	  (SERVICIO DISPONIBLE SOLO EN ENTORNOS DE PRUEBAS)<br/>
	  Servicio para la creación de certificados de usuario.
	  
	  <br/>
			</p>
			<div class="params_result_div">
			
			
			
			
			
			
				<p>
					<b>Parámetros:</b><br/>
					
						<u>csr</u>: Solicitud de certificación con los datos de usuario.<br/>
					
				</p>
			
			
			
				
			
			
			 
			
			
				<p><b>Respuesta:</b><br/>Si todo es correcto devuelve un código de estado HTTP 200 y el identificador 
	          de la solicitud en la base de datos.</p>
			
			</div>
		<HR>
	
		
		
			<p>
				- <u>POST</u> - 
				<a href="${request.scheme}://${request.serverName}:${request.serverPort}${request.getContextPath()}/csr/validar">/csr/validar</a><br/>
				
	  (SERVICIO DISPONIBLE SOLO EN ENTORNOS DE PRUEBAS).
	  
	  Servicio que firma solicitudes de certificación de usuario.<br/>
	  
	  <br/>
			</p>
			<div class="params_result_div">
			
			
			
			
			
			
				<p>
					<b>Parámetros:</b><br/>
					
						<u>userIfo</u>: Documento JSON con los datos del usuario 
	  <code>{deviceId:"000000000000000", telefono:"15555215554", nif:"1R" }</code><br/>
					
				</p>
			
			
			
				
			
			
			 
			
			
				<p><b>Respuesta:</b><br/>Si todo es correcto devuelve un código de estado HTTP 200.</p>
			
			</div>
		<HR>
	

</div>