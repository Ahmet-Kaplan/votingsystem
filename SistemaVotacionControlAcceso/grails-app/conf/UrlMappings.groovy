/**
* @author jgzornoza
* Licencia: http://bit.ly/j9jZQH
*/
class UrlMappings {

	static mappings = {
		
		/*"/applet/appletFirma.jnlp" {
			controller = "applet"
			action = "jnlpCliente"
		}*/
		
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/"(view:"/index")
		"400"(controller:"error400", action:"procesar")
		"500"(controller:"error500", action:"procesar")
	}
}