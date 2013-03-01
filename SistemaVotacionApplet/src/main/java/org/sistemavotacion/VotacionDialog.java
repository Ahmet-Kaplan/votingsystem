package org.sistemavotacion;

import static org.sistemavotacion.Contexto.*;

import java.awt.Desktop;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.security.KeyStore;
import java.text.MessageFormat;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.SwingWorker;
import javax.swing.text.html.parser.ParserDelegator;
import org.sistemavotacion.dialogo.MensajeDialog;
import org.sistemavotacion.dialogo.PasswordDialog;
import org.sistemavotacion.modelo.Evento;
import org.sistemavotacion.modelo.Operacion;
import org.sistemavotacion.modelo.ReciboVoto;
import org.sistemavotacion.modelo.Respuesta;
import org.sistemavotacion.seguridad.PKCS10WrapperClient;
import org.sistemavotacion.smime.SMIMEMessageWrapper;
import org.sistemavotacion.smime.SignedMailGenerator;
import org.sistemavotacion.util.FileUtils;
import org.sistemavotacion.util.StringUtils;
import org.sistemavotacion.util.VotacionHelper;
import org.sistemavotacion.worker.EnviarSolicitudControlAccesoWorker;
import org.sistemavotacion.worker.VotingSystemWorkerListener;
import org.sistemavotacion.worker.NotificarVotoWorker;
import org.sistemavotacion.worker.TimeStampWorker;
import org.sistemavotacion.worker.VotingSystemWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @author jgzornoza
* Licencia: https://raw.github.com/jgzornoza/SistemaVotacionAppletFirma/master/licencia.txt
*/
public class VotacionDialog extends JDialog implements VotingSystemWorkerListener {

    private static Logger logger = LoggerFactory.getLogger(VotacionDialog.class);
    
    private static final int TIMESTAMP_ACCESS_REQUEST = 0;
    private static final int TIMESTAMP_VOTE           = 1;
    private static final int ACCESS_REQUEST_WORKER    = 2;
    private static final int NOTIFICAR_VOTO_WORKER    = 3;
    

    private volatile boolean mostrandoPantallaEnvio = false;
    private Frame parentFrame;
    private SwingWorker tareaEnEjecucion;
    private Evento votoEvento;
    private AppletFirma appletFirma;
    private File directorioArchivoVoto;
    private SMIMEMessageWrapper timeStampedDocument;
    PKCS10WrapperClient pkcs10WrapperClient = null;
    
    public VotacionDialog(java.awt.Frame parent, boolean modal, final AppletFirma appletFirma) {
        super(parent, modal);
        this.parentFrame = parent;
        this.appletFirma = appletFirma;
        //parentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);       
        initComponents();
        votoEvento = appletFirma.getOperacionEnCurso().getEvento();
        addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                logger.debug("FirmaDialog window closed event received");
                appletFirma.cancelarOperacion();
            }

            public void windowClosing(WindowEvent e) {
                logger.debug("FirmaDialog window closing event received");
            }
        });
        //Bug similar to -> http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6993691
        ParserDelegator workaround = new ParserDelegator();
        messageLabel.setText(getMensajeVotacion(votoEvento.getAsunto(), 
                        votoEvento.getOpcionSeleccionada().getContenido()));
        setTitle(appletFirma.getOperacionEnCurso().
                getTipo().getCaption());
        progressBarPanel.setVisible(false);
        pack();
        logger.info("Inicializado dialogo voto");
    }
    
    public void mostrarPantallaEnvio (boolean visibility) {
        logger.debug("mostrarPantallaEnvio - " + visibility);
        mostrandoPantallaEnvio = visibility;
        progressBarPanel.setVisible(visibility);
        enviarButton.setVisible(!visibility);
        confirmacionPanel.setVisible(!visibility);
        if (mostrandoPantallaEnvio) cerrarButton.setText(getString("cancelar"));
        else cerrarButton.setText(getString("cerrar"));
        pack();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cerrarButton = new javax.swing.JButton();
        confirmacionPanel = new javax.swing.JPanel();
        verDocumentoButton = new javax.swing.JButton();
        messageLabel = new javax.swing.JLabel();
        progressBarPanel = new javax.swing.JPanel();
        progressLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        enviarButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        cerrarButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/cancel_16x16.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/sistemavotacion/Bundle"); // NOI18N
        cerrarButton.setText(bundle.getString("VotacionDialog.cerrarButton.text")); // NOI18N
        cerrarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cerrarButtonActionPerformed(evt);
            }
        });

        confirmacionPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        verDocumentoButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/fileopen16x16.png"))); // NOI18N
        verDocumentoButton.setText(bundle.getString("VotacionDialog.verDocumentoButton.text")); // NOI18N
        verDocumentoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                verDocumentoButtonActionPerformed(evt);
            }
        });

        messageLabel.setText(bundle.getString("VotacionDialog.messageLabel.text")); // NOI18N

        javax.swing.GroupLayout confirmacionPanelLayout = new javax.swing.GroupLayout(confirmacionPanel);
        confirmacionPanel.setLayout(confirmacionPanelLayout);
        confirmacionPanelLayout.setHorizontalGroup(
            confirmacionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(confirmacionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(confirmacionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(verDocumentoButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(messageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        confirmacionPanelLayout.setVerticalGroup(
            confirmacionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(confirmacionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(messageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(verDocumentoButton)
                .addContainerGap())
        );

        progressLabel.setText(bundle.getString("VotacionDialog.progressLabel.text")); // NOI18N

        progressBar.setIndeterminate(true);

        javax.swing.GroupLayout progressBarPanelLayout = new javax.swing.GroupLayout(progressBarPanel);
        progressBarPanel.setLayout(progressBarPanelLayout);
        progressBarPanelLayout.setHorizontalGroup(
            progressBarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(progressBarPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(progressBarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(progressLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        progressBarPanelLayout.setVerticalGroup(
            progressBarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(progressBarPanelLayout.createSequentialGroup()
                .addComponent(progressLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        enviarButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/signature-ok_16x16.png"))); // NOI18N
        enviarButton.setText(bundle.getString("VotacionDialog.enviarButton.text")); // NOI18N
        enviarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enviarButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(progressBarPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(enviarButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cerrarButton))
                    .addComponent(confirmacionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(progressBarPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(confirmacionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(enviarButton)
                    .addComponent(cerrarButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cerrarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cerrarButtonActionPerformed
        logger.debug("cerrarButtonActionPerformed - mostrandoPantallaEnvio: " + mostrandoPantallaEnvio);
        if (mostrandoPantallaEnvio) {
            if (tareaEnEjecucion != null) tareaEnEjecucion.cancel(true);
            mostrarPantallaEnvio(false);
            return;
        }
        dispose();
    }//GEN-LAST:event_cerrarButtonActionPerformed

    private void enviarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enviarButtonActionPerformed
        String password = null;
        if (Contexto.getDNIePassword() == null) {
            PasswordDialog dialogoPassword = new PasswordDialog (parentFrame, true);
            dialogoPassword.setVisible(true);
            password = dialogoPassword.getPassword();
            if (password == null) return;
        }
        final String finalPassword = password;
        Runnable runnable = new Runnable() {
            public void run() {  
                lanzarVoto(finalPassword);    
            }
        };
        new Thread(runnable).start();
        mostrarPantallaEnvio(true);
        progressLabel.setText("<html>" + getString("progressLabel") + "</html>");
        pack();       
    }//GEN-LAST:event_enviarButtonActionPerformed

    private void verDocumentoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verDocumentoButtonActionPerformed
        if (!Desktop.isDesktopSupported()) {
            logger.debug("No hay soporte de escritorio");
        }
        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.BROWSE)) {
            logger.debug("No se puede editar archivos");
        }
        try {
            File documento = new File(FileUtils.APPTEMPDIR +
                    appletFirma.getOperacionEnCurso().getTipo().
                    getNombreArchivoEnDisco());
            documento.deleteOnExit();
            FileUtils.copyStreamToFile(new ByteArrayInputStream(
                    votoEvento.obtenerSolicitudAccesoJSONStr().getBytes()), documento);
            logger.info("documento.getAbsolutePath(): " + documento.getAbsolutePath());
            desktop.open(documento);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }//GEN-LAST:event_verDocumentoButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JButton cerrarButton;
    javax.swing.JPanel confirmacionPanel;
    javax.swing.JButton enviarButton;
    javax.swing.JLabel messageLabel;
    javax.swing.JProgressBar progressBar;
    javax.swing.JPanel progressBarPanel;
    javax.swing.JLabel progressLabel;
    javax.swing.JButton verDocumentoButton;
    // End of variables declaration//GEN-END:variables

    
    private void lanzarVoto(String password) {
        KeyStore keyStore = null;
        try {
            File accessRequest = File.createTempFile(
                NOMBRE_ARCHIVO_SOLICITUD_ACCESO_TIMESTAMPED, SIGNED_PART_EXTENSION);
            accessRequest = VotacionHelper.obtenerSolicitudAcceso(
                    votoEvento, password, accessRequest);
            //No se hace la comprobación antes porque no hay usuario en contexto
            //hasta que no se firma al menos una vez
            votoEvento.setUsuario(Contexto.getUsuario());
            keyStore = Contexto.getKeyStoreVoto(votoEvento, Contexto.getUsuario().getNif());
            pkcs10WrapperClient = Contexto.initPkcs10WrapperClient(votoEvento);
            if (keyStore != null) {
                pkcs10WrapperClient.initSigner(keyStore);
                logger.debug("El usuario '" + Contexto.getUsuario().getNif() +
                    "' ya había solicitado un certificado para el evento '" +
                        votoEvento.getAsunto() + "'. Enviando voto a Centro de Control");
                notificarCentroControl(pkcs10WrapperClient, votoEvento);
            } else {
                Contexto.getInstancia().setVoto(votoEvento, Contexto.getUsuario().getNif());
                directorioArchivoVoto = new File (Contexto.
                        getRutaArchivosVoto(votoEvento, Contexto.getUsuario().getNif()));
                directorioArchivoVoto.mkdirs();
                File accessRequestCopy = new File (directorioArchivoVoto.getAbsolutePath() 
                    + File.separator + Contexto.NOMBRE_ARCHIVO_SOLICITUD_ACCESO);
                FileUtils.copyFileToFile(accessRequest, accessRequestCopy);
                if(IS_TIME_STAMPED_SIGNATURE) {
                    setTimeStampedDocument(accessRequest, TIMESTAMP_ACCESS_REQUEST, TIMESTAMP_DNIe_HASH);
                } else {
                    tareaEnEjecucion = new EnviarSolicitudControlAccesoWorker(
                            ACCESS_REQUEST_WORKER, accessRequest, 
                        votoEvento.getUrlSolicitudAcceso(), pkcs10WrapperClient, this);
                    tareaEnEjecucion.execute();
                }
            }
        } catch (Exception ex) {
            mostrarPantallaEnvio(false);
            logger.error(ex.getMessage(), ex);
            MensajeDialog mensajeDialog = new MensajeDialog(parentFrame,true);
            String errorLanzandoVotoMsg = 
                    getString("errorLanzandoVotoMsg");
            mensajeDialog.setMessage(errorLanzandoVotoMsg + " - " 
                    + ex.getMessage(), errorLanzandoVotoMsg);
        }
    }
    
        
    private void notificarCentroControl (PKCS10WrapperClient pkcs10WrapperClient, 
            Evento votoEvento) {
        progressLabel.setText("<html><b>" + 
                getString("notificandoCentroControlLabel") +"</b></html>");
        mostrarPantallaEnvio(true);
        String votoJSON = votoEvento.obtenerVotoJSONStr();
        File votoFirmado = new File (
                directorioArchivoVoto.getAbsolutePath() 
                + File.separator + getString("TIMESTAMPED_VOTE_SMIME_FILE"));
        try {
            votoFirmado = pkcs10WrapperClient.genSignedFile(votoEvento.getHashCertificadoVotoBase64(), 
                    StringUtils.getCadenaNormalizada(votoEvento.getCentroControl().getNombre()),
                    votoJSON, getString("asuntoVoto"), null, 
                    SignedMailGenerator.Type.USER, votoFirmado);
            if(IS_TIME_STAMPED_SIGNATURE) {
                setTimeStampedDocument(votoFirmado, TIMESTAMP_VOTE, TIMESTAMP_VOTE_HASH);
            } else {
                tareaEnEjecucion = new NotificarVotoWorker(NOTIFICAR_VOTO_WORKER,
                    votoEvento, votoEvento.getUrlRecolectorVotosCentroControl(), 
                    votoFirmado, this);
                tareaEnEjecucion.execute();
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            MensajeDialog errorDialog = new MensajeDialog(parentFrame, true);
            errorDialog.setMessage(ex.getMessage(), getString("errorLbl"));
        }
    }
    
    private void setTimeStampedDocument(File document, int timeStampOperation, 
            String timeStamprequestAlg) {
        if(document == null) return;
        final Operacion operacion = appletFirma.getOperacionEnCurso();
        try {
            timeStampedDocument = new SMIMEMessageWrapper(null,document);
            new TimeStampWorker(timeStampOperation, operacion.getUrlTimeStampServer(),
                    this, timeStampedDocument.getTimeStampRequest(timeStamprequestAlg)).execute();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            MensajeDialog errorDialog = new MensajeDialog(parentFrame, true);
            errorDialog.setMessage(ex.getMessage(), getString("errorLbl"));
        }
    }
    
    @Override  public void process(List<String> messages) {
        logger.debug(" - process: " + messages.toArray().toString());
        progressLabel.setText(messages.toArray().toString());
    }
    
    @Override public void showResult(VotingSystemWorker worker) {
        mostrarPantallaEnvio(false);
        logger.debug("showResult - worker: " + worker.getClass() 
                + " - statusCode:" + worker.getStatusCode());
        switch(worker.getId()) {
            case TIMESTAMP_ACCESS_REQUEST:
                if(Respuesta.SC_OK == worker.getStatusCode()) {
                    try {
                        tareaEnEjecucion = new EnviarSolicitudControlAccesoWorker(
                                ACCESS_REQUEST_WORKER, timeStampedDocument.
                                setTimeStampToken((TimeStampWorker)worker), votoEvento.
                                getUrlSolicitudAcceso(), pkcs10WrapperClient, this);
                        tareaEnEjecucion.execute();
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                        MensajeDialog errorDialog = new MensajeDialog(parentFrame, true);
                        errorDialog.setMessage(ex.getMessage(), getString("errorLbl"));
                    }
                } else {
                    MensajeDialog errorDialog = new MensajeDialog(parentFrame, true);
                    errorDialog.setMessage(worker.getMessage(), getString("errorLbl"));
                }
                break;
            case ACCESS_REQUEST_WORKER:
                if (Respuesta.SC_OK == worker.getStatusCode()) {  
                    notificarCentroControl(((EnviarSolicitudControlAccesoWorker)worker).
                        getPKCS10WrapperClient(), votoEvento);
                } else {
                    appletFirma.responderCliente(worker.getStatusCode(), worker.getMessage());
                    dispose();
                }
                break;
            case TIMESTAMP_VOTE:
                if(Respuesta.SC_OK == worker.getStatusCode()) {
                    try {
                        tareaEnEjecucion = new NotificarVotoWorker(
                            NOTIFICAR_VOTO_WORKER, votoEvento, 
                            votoEvento.getUrlRecolectorVotosCentroControl(), 
                            timeStampedDocument.setTimeStampToken((TimeStampWorker)worker), this);
                            tareaEnEjecucion.execute();
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                        MensajeDialog errorDialog = new MensajeDialog(parentFrame, true);
                        errorDialog.setMessage(ex.getMessage(), getString("errorLbl"));
                    }
                } else {
                    MensajeDialog errorDialog = new MensajeDialog(parentFrame, true);
                    errorDialog.setMessage(worker.getMessage(), getString("errorLbl"));
                }
                break;
            case NOTIFICAR_VOTO_WORKER:
                ReciboVoto recibo = new ReciboVoto();
                if (Respuesta.SC_OK == worker.getStatusCode()) {   
                    recibo = ((NotificarVotoWorker)worker).getReciboVoto();
                    appletFirma.responderCliente(worker.getStatusCode(), null);
                } else {
                    logger.error("Esto no debería pasar nunca!!! -- Error enviando voto: " +
                            worker.getMessage());
                    appletFirma.responderCliente(Operacion.SC_ERROR_ENVIO_VOTO, null);
                }
                recibo.setVoto(votoEvento);
                VotacionHelper.addRecibo(votoEvento.getHashCertificadoVotoBase64(), recibo);
                dispose();
                break;
        }

    } 
    
    private String getMensajeVotacion(String asunto, String opcionSeleccionada) {
        String pattern = getString("mensajeVotacion");
        return MessageFormat.format(pattern, asunto, opcionSeleccionada);
    }

}