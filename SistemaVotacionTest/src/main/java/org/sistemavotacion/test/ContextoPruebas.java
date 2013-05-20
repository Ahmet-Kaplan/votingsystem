package org.sistemavotacion.test;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import javax.security.auth.x500.X500PrivateCredential;
import org.apache.log4j.PropertyConfigurator;
import org.bouncycastle.tsp.TSPAlgorithms;
import org.sistemavotacion.Contexto;
import org.sistemavotacion.modelo.ActorConIP;
import org.sistemavotacion.modelo.Evento;
import org.sistemavotacion.modelo.Usuario;
import org.sistemavotacion.seguridad.KeyStoreUtil;
import org.sistemavotacion.util.DateUtils;
import org.sistemavotacion.util.FileUtils;
import org.sistemavotacion.test.util.NifUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @author jgzornoza
* Licencia: http://bit.ly/j9jZQH
*/
public class ContextoPruebas {
    
    private static Logger logger = LoggerFactory.getLogger(ContextoPruebas.class);
    
    public static String locale = "es";
    
    public static final int KEY_SIZE = 1024;
    public static final String SIG_NAME = "RSA";
    public static final String PROVIDER = "BC";
    
    public static final String TIMESTAMP_HASH = TSPAlgorithms.SHA1;
    public static final String TIMESTAMP_VOTE_HASH = TSPAlgorithms.SHA512;
    
    public static ContextoPruebas INSTANCIA;
    private static KeyStore keyStoreRaizAutoridad;
    private static Usuario usuarioPruebas;
    private static X500PrivateCredential privateCredentialRaizAutoridad;
    private static PrivateKey privateKeyRaizAutoridad;
    private static X509Certificate certificadoRaizAutoridad;
    private static ActorConIP centroControl;
    private static ActorConIP controlAcceso;
    
    private static Evento evento = null;
    
    public static final String DNIe_SIGN_MECHANISM = "SHA1withRSA";
    public static final String VOTE_SIGN_MECHANISM = "SHA512withRSA";
    public static final String DIGEST_ALG = "SHA256";
    
    public static final String PREFIJO_USER_JKS = "usuario_"; 
    public static final String SUFIJO_USER_JKS = ".jks"; 
    public static String ROOT_ALIAS = "rootAlias"; 
    public static String END_ENTITY_ALIAS = "endEntityAlias";
    public static String PASSWORD = "PemPass"; 
    
    public static String SOLICITUD_FILE = "SolicitudAcceso_";
    public static String DATOS_VOTO = "DatosVoto_";
    public static String ANULACION_FILE = "Anulador_";
    public static String ANULACION_FIRMADA_FILE = "AnuladorFirmado_";
    public static String RECIBO_FILE = "ReciboVoto_";
    public static String VOTE_FILE = "Vote_";
    
    public static long COMIEZO_VALIDEZ_CERT = System.currentTimeMillis();
    public static final int PERIODO_VALIDEZ_ALMACEN_RAIZ = 2000000000;//En producción durará lo que dure una votación
    public static final int PERIODO_VALIDEZ_CERT = 2000000000;

    public static final int MAXIMALONGITUDCAMPO = 255;

    public static final String ASUNTO_MENSAJE_SOLICITUD_ACCESO 
            = "[SOLICITUD ACCESO]-";
    public static final String ASUNTO_MENSAJE_ANULACION_SOLICITUD_ACCESO 
            = "[ANULACION SOLICITUD ACCESO]-";
    
    public static String BASEDIR =  System.getProperty("user.home");
    public static String APPDIR =  FileUtils.BASEDIR + File.separator 
            + "DatosSimulacionVotacion"  + File.separator;    
    
    private static int idUsuario = 0;
    private static Integer numeroTotalDeVotosParaLanzar;
    private static Integer horasDuracionVotacion;
    private static Integer minutosDuracionVotacion;
    private static boolean votacionAleatoria = true;
    private static boolean simulacionConTiempos = false;
    private static ResourceBundle resourceBundle;
    private PKIXParameters sessionPKIXParams = null;

    private ContextoPruebas () { }

    public static ContextoPruebas inicializar () throws Exception {
        logger.debug(" --- inicializar --- ");
        if (INSTANCIA == null) {
            new File(APPDIR).mkdirs();
            INSTANCIA = new ContextoPruebas();
            Properties props = new Properties();
            try {
                props.load(Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("log4jSistemaVotacionTest.properties"));
            } catch (IOException ex) {
                logger.error(ex.getMessage(), ex);
            }
            PropertyConfigurator.configure(props);
            org.sistemavotacion.Contexto.inicializar();
            inicializarAutoridadCertificadora();
            usuarioPruebas = new Usuario();
            usuarioPruebas.setNif(NifUtils.getNif(1));
            usuarioPruebas.setNombre("José García");
            usuarioPruebas.setEmail("jgzornoza@gmail.com");
            usuarioPruebas.setKeyStore(inicializarCertificadoUsuario(usuarioPruebas));
            resourceBundle = ResourceBundle.getBundle("messagesTest_" + locale);
        }
        return INSTANCIA;
    }
    
    public static String getString(String key) {
        return resourceBundle.getString(key);
    }    
    
    public static String getString(String key, Object... arguments) {
        String pattern = getString(key);
        return MessageFormat.format(pattern, arguments);
    }

    private static void inicializarAutoridadCertificadora() {
        try {
            File rutaMockRaiz = File.createTempFile("MockRaiz", ".jks");
            keyStoreRaizAutoridad = KeyStoreUtil.createRootKeyStore(
                     COMIEZO_VALIDEZ_CERT, PERIODO_VALIDEZ_ALMACEN_RAIZ, 
                     PASSWORD.toCharArray(), ROOT_ALIAS, 
                     rutaMockRaiz.getAbsolutePath(), 
                     "CN=Autoridad Certificadora Pruebas - " + 
                     DateUtils.getStringFromDate(DateUtils.getTodayDate()) +
                     ", OU=DNIE_CA");
            certificadoRaizAutoridad = (X509Certificate)keyStoreRaizAutoridad.getCertificate(ROOT_ALIAS);
            privateKeyRaizAutoridad = (PrivateKey) getKeyStoreRaizAutoridad().getKey(ROOT_ALIAS, 
                PASSWORD.toCharArray());
            privateCredentialRaizAutoridad = new X500PrivateCredential(
                     getCertificadoRaizAutoridad(), privateKeyRaizAutoridad,ROOT_ALIAS);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
    
    private static KeyStore inicializarCertificadoUsuario(Usuario usuario) throws Exception {
         return KeyStoreUtil.createActorKeyStore(COMIEZO_VALIDEZ_CERT,
                PERIODO_VALIDEZ_CERT, PASSWORD.toCharArray(),
                END_ENTITY_ALIAS, privateCredentialRaizAutoridad, 
                "GIVENNAME=NombreUsuarioTest, SURNAME=AppelidosUsuarioTest, SERIALNUMBER=" + usuario.getNif()); 
    }

    /**
     * @return the controlAcceso
     */
    public static ActorConIP getControlAcceso() {
        return controlAcceso;
    }
    
    public String getUrlControlAccesoCertChain() {
        logger.debug(" --- getUrlControlAccesoCertChain --- ");
        return controlAcceso.getServerURL() + "/certificado/cadenaCertificacion";
    }
    
    public PKIXParameters getSessionPKIXParameters() 
            throws InvalidAlgorithmParameterException, Exception {
        logger.debug(" --- getSessionPKIXParameters --- ");
        if(sessionPKIXParams == null) {
            PKIXParameters params = Contexto.getHttpHelper()
                    .obtenerPKIXParametersDeServidor(getUrlControlAccesoCertChain());
            TrustAnchor anchorUserCert = 
                    new TrustAnchor(getCertificadoRaizAutoridad(), null);
            Set<TrustAnchor> anchors = new HashSet<TrustAnchor>();
            anchors.add(anchorUserCert);
            anchors.addAll(params.getTrustAnchors());
            sessionPKIXParams = new PKIXParameters(anchors);
            sessionPKIXParams.setRevocationEnabled(false);
        }
        return sessionPKIXParams;
    }
    

    /**
     * @param aActorConIP the controlAcceso to set
     */
    public static void setControlAcceso(ActorConIP aActorConIP) {
        controlAcceso = aActorConIP;
    }
           
    /**
     * @return the centroControl
     */
    public static ActorConIP getCentroControl() {
        return centroControl;
    }

    /**
     * @param aCentroControl the centroControl to set
     */
    public static void setCentroControl(ActorConIP aCentroControl) {
        centroControl = aCentroControl;
        Set<ActorConIP> centrosDeControlAsociados = ContextoPruebas.getControlAcceso().getCentrosDeControl();
        boolean newCentroControl = true;
        for(ActorConIP centroControl : centrosDeControlAsociados) {
            if(centroControl.getServerURL().trim().equals(aCentroControl.getServerURL().trim())) 
                newCentroControl= false;
        }
        if(newCentroControl) centrosDeControlAsociados.add(centroControl);
    }

    /**
     * @return the keyStoreRaizAutoridad
     */
    public static KeyStore getKeyStoreRaizAutoridad() {
        return keyStoreRaizAutoridad;
    }

    /**
     * @param aKeyStoreRaizAutoridad the keyStoreRaizAutoridad to set
     */
    public static void setKeyStoreRaizAutoridad(KeyStore aKeyStoreRaizAutoridad) {
        keyStoreRaizAutoridad = aKeyStoreRaizAutoridad;
    }

    /**
     * @return the privateCredentialRaizAutoridad
     */
    public static X500PrivateCredential getPrivateCredentialRaizAutoridad() {
        return privateCredentialRaizAutoridad;
    }

    /**
     * @param aPrivateCredentialRaizAutoridad the privateCredentialRaizAutoridad to set
     */
    public static void setPrivateCredentialRaizAutoridad(
            X500PrivateCredential aPrivateCredentialRaizAutoridad) {
        privateCredentialRaizAutoridad = aPrivateCredentialRaizAutoridad;
    }

    /**
     * @return the certificadoRaizAutoridad
     */
    public static X509Certificate getCertificadoRaizAutoridad() {
        return certificadoRaizAutoridad;
    }

    /**
     * @param aCertificadoRaizAutoridad the certificadoRaizAutoridad to set
     */
    public static void setCertificadoRaizAutoridad(X509Certificate aCertificadoRaizAutoridad) {
        certificadoRaizAutoridad = aCertificadoRaizAutoridad;
    }

    /**
     * @return the usuarioPruebas
     */
    public static Usuario getUsuarioPruebas() {
        return usuarioPruebas;
    }

    /**
     * @param aUsuarioPruebas the usuarioPruebas to set
     */
    public static void setUsuarioPruebas(Usuario aUsuarioPruebas) {
        usuarioPruebas = aUsuarioPruebas;
    }

    /**
     * @return the numeroTotalDeVotosParaLanzar
     */
    public static Integer getNumeroTotalDeVotosParaLanzar() {
        return numeroTotalDeVotosParaLanzar;
    }

    /**
     * @param aNumeroTotalDeVotosParaLanzar the numeroTotalDeVotosParaLanzar to set
     */
    public static void setNumeroTotalDeVotosParaLanzar(Integer aNumeroTotalDeVotosParaLanzar) {
        numeroTotalDeVotosParaLanzar = aNumeroTotalDeVotosParaLanzar;
    }

    /**
     * @return the horasDuracionVotacion
     */
    public static Integer getHorasDuracionVotacion() {
        return horasDuracionVotacion;
    }

    /**
     * @param aHorasDuracionVotacion the horasDuracionVotacion to set
     */
    public static void setHorasDuracionVotacion(Integer aHorasDuracionVotacion) {
        horasDuracionVotacion = aHorasDuracionVotacion;
    }

    /**
     * @return the minutosDuracionVotacion
     */
    public static Integer getMinutosDuracionVotacion() {
        return minutosDuracionVotacion;
    }

    /**
     * @param aMinutosDuracionVotacion the minutosDuracionVotacion to set
     */
    public static void setMinutosDuracionVotacion(Integer aMinutosDuracionVotacion) {
        minutosDuracionVotacion = aMinutosDuracionVotacion;
    }

    /**
     * @return the votacionAleatoria
     */
    public static boolean isVotacionAleatoria() {
        return votacionAleatoria;
    }

    /**
     * @param aVotacionAleatoria the votacionAleatoria to set
     */
    public static void setVotacionAleatoria(boolean aVotacionAleatoria) {
        votacionAleatoria = aVotacionAleatoria;
    }
    
    
    /**
     * @return the simulacionConTiempos
     */
    public static boolean isSimulacionConTiempos() {
        return simulacionConTiempos;
    }

    /**
     * @param aSimulacionConTiempos the simulacionConTiempos to set
     */
    public static void setSimulacionConTiempos(boolean aSimulacionConTiempos) {
        simulacionConTiempos = aSimulacionConTiempos;
    }

    /**
     * @return the evento
     */
    public static Evento getEvento() {
        return evento;
    }

    /**
     * @param aEvento the evento to set
     */
    public static void setEvento(Evento aEvento) {
        evento = aEvento;
    }

    public static void crearNivelesDeDirectorios (
            int numDirsPorNivel, String baseDir, int nivel) {
        if (nivel == 0) return;
        for (int i = 0; i < numDirsPorNivel; i++) {
            File file = new File(baseDir + File.separator + i);
            file.mkdirs();
            crearNivelesDeDirectorios(numDirsPorNivel, file.getAbsolutePath(), nivel-1);
        }
    }

    public static String getUserDirPath (String userNIF) {
        String resultPath = APPDIR;
        while (userNIF.length() > 0) {
            String subPath = userNIF.substring(0, 1);
            userNIF = userNIF.substring(1);
            resultPath = resultPath.concat(subPath + File.separator);
        }
        return resultPath;
    }
        
    public static String getUserKeyStorePath (String userNIF) {
        String userDirPath = getUserDirPath(userNIF);
        new File(userDirPath).mkdirs();
        return  userDirPath + PREFIJO_USER_JKS + userNIF + SUFIJO_USER_JKS;
    }

    /**
     * @return the idUsuario
     */
    public static int getIdUsuario() {
        return idUsuario;
    }

    /**
     * @param idUsuario the idUsuario to set
     */
    public static void setIdUsuario(int idUsu) {
        idUsuario = idUsu;
    }

    public static String getURLInfoServidor(String serverURL) {
        if (!serverURL.endsWith("/")) serverURL = serverURL + "/";
        return serverURL + "infoServidor/obtener";
    }

    public static String getURLEventoParaVotar(String serverURL, Long eventoId) {
        if (!serverURL.endsWith("/")) serverURL = serverURL + "/";
        return serverURL + "eventoVotacion/obtener?id=" +eventoId;
    }

    public static String getURLAnyadirCertificadoCA(String serverURL) {
        if (!serverURL.endsWith("/")) serverURL = serverURL + "/";
        return serverURL + "certificado/addCertificateAuthority";
    }

    public static String getURLAsociarActorConIP (String serverURL, ActorConIP.Tipo tipoActor) {
        if (!serverURL.endsWith("/")) serverURL = serverURL + "/";
        String sufijoURL = null;
        switch(tipoActor) {
            case CONTROL_ACCESO:
                sufijoURL = "subscripcion/guardarAsociacionConControlAcceso";
                break;
            case CENTRO_CONTROL:
                sufijoURL = "subscripcion/guardarAsociacionConCentroControl";
                break;
        }
        return serverURL + sufijoURL;
    }

    public static String getURLGuardarEventoParaVotar(String serverURL) {
        if (!serverURL.endsWith("/")) serverURL = serverURL + "/";
        return serverURL + "eventoVotacion";
    }

    public static String getURLAnulacionVoto(String serverURL) {
        if (!serverURL.endsWith("/")) serverURL = serverURL + "/";
        return serverURL + "anuladorVoto";
    }

    public static String getURLSolicitudAcceso(String serverURL) {
        if (!serverURL.endsWith("/")) serverURL = serverURL + "/";
        return serverURL + "solicitudAcceso";
    }

    public static String getURLVoto(String serverURL) {
        if (!serverURL.endsWith("/")) serverURL = serverURL + "/";
        return serverURL + "voto";
    }

    public static String getUrlTimeStampServer(String serverURL) {
        if (!serverURL.endsWith("/")) serverURL = serverURL + "/";
        return serverURL + "timeStamp";
    }

}