package org.sistemavotacion.test.panel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.sistemavotacion.modelo.ActorConIP;
import org.sistemavotacion.modelo.Evento;
import org.sistemavotacion.modelo.Respuesta;
import org.sistemavotacion.test.ContextoPruebas;
import org.sistemavotacion.test.MainFrame;
import org.sistemavotacion.test.dialogo.*;
import org.sistemavotacion.test.simulacion.Votacion;
import org.sistemavotacion.test.tarea.LanzadorWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @author jgzornoza
* Licencia: https://github.com/jgzornoza/SistemaVotacion/blob/master/licencia.txt
*/
public class VotacionesPanel extends JPanel 
    implements LanzadorWorker, HyperlinkListener {
    
    private static Logger logger = LoggerFactory.getLogger(VotacionesPanel.class);
    
    public enum Estado {SIMULACION, RECOGIDA_DATOS}
    
    private Border normalTextBorder;
    private Evento evento;
    private Estado estado = Estado.RECOGIDA_DATOS;
    private String erroresSolicitudes;
    private String erroresEnVotos;
    private Votacion votacion;
    public static VotacionesPanel INSTANCIA;
    private HashMap<String, ActorConIP> hashMapActores = null;
    
    /**
     * Creates new form VotacionesPanel
     */
    public VotacionesPanel() {
        initComponents();
        normalTextBorder = new JTextField().getBorder();
        urlCentroControlPanel.setVisible(false);
        publicacionConvocatoriaPanel.setVisible(false);
        datosSimulacionPanel.setVisible(false);
        asuntoConvocatoriaLabel.setVisible(false);
        lanzarSimulacionButton.setVisible(false);
        contadorPanel.setVisible(false);
        erroresSolicitudesButton.setVisible(false);
        erroresVotosButton.setVisible(false);
        anularVotosButton.setVisible(false);
        INSTANCIA = this;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        publicacionConvocatoriaPanel = new javax.swing.JPanel();
        mensajePublicacionLabel = new javax.swing.JLabel();
        publicarButton = new javax.swing.JButton();
        datosSimulacionPanel = new javax.swing.JPanel();
        infoEventoButton = new javax.swing.JButton();
        datosSimulacionButton = new javax.swing.JButton();
        asuntoConvocatoriaLabel = new javax.swing.JLabel();
        lanzarSimulacionButton = new javax.swing.JButton();
        erroresSolicitudesButton = new javax.swing.JButton();
        erroresVotosButton = new javax.swing.JButton();
        contadorPanel = new javax.swing.JPanel();
        contadorSolicitudesLabel = new javax.swing.JLabel();
        contadorSolicitudesErrorLabel = new javax.swing.JLabel();
        contadorVotosLabel = new javax.swing.JLabel();
        contadorVotosValidadosLabel = new javax.swing.JLabel();
        contadorVotosErrorLabel = new javax.swing.JLabel();
        timePanel = new javax.swing.JPanel();
        timeLabel = new javax.swing.JLabel();
        digitalClockPanel = new org.sistemavotacion.test.panel.DigitalClockPanel();
        anularVotosButton = new javax.swing.JButton();
        centroControlPanel = new javax.swing.JPanel();
        urlCentroControlPanel = new javax.swing.JPanel();
        centroControlLabel = new javax.swing.JLabel();
        centrosDeControlComboBox = new javax.swing.JComboBox();
        infoServidorButton = new javax.swing.JButton();
        asociarCentroControlButton = new javax.swing.JButton();

        mensajePublicacionLabel.setText("Para hacer las pruebas tiene que publicar la convocatoria");

        publicarButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/publish.png"))); // NOI18N
        publicarButton.setText("Publicar convocatoria de elección");
        publicarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                publicarButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout publicacionConvocatoriaPanelLayout = new javax.swing.GroupLayout(publicacionConvocatoriaPanel);
        publicacionConvocatoriaPanel.setLayout(publicacionConvocatoriaPanelLayout);
        publicacionConvocatoriaPanelLayout.setHorizontalGroup(
            publicacionConvocatoriaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(publicacionConvocatoriaPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(publicacionConvocatoriaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mensajePublicacionLabel)
                    .addComponent(publicarButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        publicacionConvocatoriaPanelLayout.setVerticalGroup(
            publicacionConvocatoriaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(publicacionConvocatoriaPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mensajePublicacionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(publicarButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        infoEventoButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/information-white.png"))); // NOI18N
        infoEventoButton.setText("Información de la convocatoria");
        infoEventoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                infoEventoButtonActionPerformed(evt);
            }
        });

        datosSimulacionButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/edit_16x16.png"))); // NOI18N
        datosSimulacionButton.setText("Introducir datos de la simulación");
        datosSimulacionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                datosSimulacionButtonActionPerformed(evt);
            }
        });

        asuntoConvocatoriaLabel.setText(" ");

        lanzarSimulacionButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/gnome-run.png"))); // NOI18N
        lanzarSimulacionButton.setText("Lanzar simulación");
        lanzarSimulacionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lanzarSimulacionButtonActionPerformed(evt);
            }
        });

        erroresSolicitudesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/error.png"))); // NOI18N
        erroresSolicitudesButton.setText("Errores en solicitudes de acceso");
        erroresSolicitudesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                erroresSolicitudesButtonActionPerformed(evt);
            }
        });

        erroresVotosButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/error.png"))); // NOI18N
        erroresVotosButton.setText("Errores en votos");
        erroresVotosButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                erroresVotosButtonActionPerformed(evt);
            }
        });

        contadorPanel.setBackground(java.awt.Color.white);
        contadorPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        contadorSolicitudesLabel.setText("<html><b>Lanzada solicitud: </b>0</html>");

        contadorSolicitudesErrorLabel.setText("<html><b>Solicitudes con error: </b>0</html>");

        contadorVotosLabel.setText("<html><b>Lanzado voto: </b>0</html>");

        contadorVotosValidadosLabel.setText("<html><b>Recolectado voto: </b>0</html>\"");

        contadorVotosErrorLabel.setText("<html><b>Votos con error: </b>0</html>\"");

        timePanel.setBackground(java.awt.Color.white);

        timeLabel.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
        timeLabel.setText("Duración simulacion:");

        javax.swing.GroupLayout timePanelLayout = new javax.swing.GroupLayout(timePanel);
        timePanel.setLayout(timePanelLayout);
        timePanelLayout.setHorizontalGroup(
            timePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, timePanelLayout.createSequentialGroup()
                .addComponent(timeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(digitalClockPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(24, Short.MAX_VALUE))
        );
        timePanelLayout.setVerticalGroup(
            timePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, timePanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(timePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(timeLabel)
                    .addComponent(digitalClockPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout contadorPanelLayout = new javax.swing.GroupLayout(contadorPanel);
        contadorPanel.setLayout(contadorPanelLayout);
        contadorPanelLayout.setHorizontalGroup(
            contadorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contadorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(contadorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(contadorSolicitudesLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
                    .addGroup(contadorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(contadorVotosValidadosLabel, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(contadorVotosLabel, javax.swing.GroupLayout.Alignment.LEADING))
                    .addComponent(contadorSolicitudesErrorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
                    .addComponent(timePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(contadorVotosErrorLabel))
                .addContainerGap(703, Short.MAX_VALUE))
        );
        contadorPanelLayout.setVerticalGroup(
            contadorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, contadorPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(timePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contadorSolicitudesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contadorSolicitudesErrorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contadorVotosLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contadorVotosValidadosLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contadorVotosErrorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
        );

        anularVotosButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/gtk-cancel.png"))); // NOI18N
        anularVotosButton.setText("Anular votos");
        anularVotosButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                anularVotosButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout datosSimulacionPanelLayout = new javax.swing.GroupLayout(datosSimulacionPanel);
        datosSimulacionPanel.setLayout(datosSimulacionPanelLayout);
        datosSimulacionPanelLayout.setHorizontalGroup(
            datosSimulacionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(datosSimulacionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(datosSimulacionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(contadorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(datosSimulacionPanelLayout.createSequentialGroup()
                        .addComponent(infoEventoButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(asuntoConvocatoriaLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(datosSimulacionPanelLayout.createSequentialGroup()
                        .addGroup(datosSimulacionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(datosSimulacionPanelLayout.createSequentialGroup()
                                .addComponent(erroresSolicitudesButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(erroresVotosButton))
                            .addGroup(datosSimulacionPanelLayout.createSequentialGroup()
                                .addComponent(datosSimulacionButton)
                                .addGap(18, 18, 18)
                                .addComponent(lanzarSimulacionButton)
                                .addGap(18, 18, 18)
                                .addComponent(anularVotosButton)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        datosSimulacionPanelLayout.setVerticalGroup(
            datosSimulacionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(datosSimulacionPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(datosSimulacionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(infoEventoButton)
                    .addComponent(asuntoConvocatoriaLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(datosSimulacionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(datosSimulacionButton)
                    .addComponent(lanzarSimulacionButton)
                    .addComponent(anularVotosButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contadorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(datosSimulacionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(erroresSolicitudesButton)
                    .addComponent(erroresVotosButton)))
        );

        centroControlPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        centroControlLabel.setText("Centro de Control asociado:");
        centroControlLabel.setToolTipText("Para hacer las pruebas de carga debe proporcionar la URL de un Centro de Control arrancado en modo TEST");

        centrosDeControlComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "" }));
        centrosDeControlComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                centrosDeControlComboBoxActionPerformed(evt);
            }
        });

        infoServidorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/information-white.png"))); // NOI18N
        infoServidorButton.setText("Información del servidor");
        infoServidorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                infoServidorButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout urlCentroControlPanelLayout = new javax.swing.GroupLayout(urlCentroControlPanel);
        urlCentroControlPanel.setLayout(urlCentroControlPanelLayout);
        urlCentroControlPanelLayout.setHorizontalGroup(
            urlCentroControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(urlCentroControlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(centroControlLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(centrosDeControlComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(infoServidorButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        urlCentroControlPanelLayout.setVerticalGroup(
            urlCentroControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(urlCentroControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(centrosDeControlComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(centroControlLabel)
                .addComponent(infoServidorButton))
        );

        asociarCentroControlButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/pair_16x16.png"))); // NOI18N
        asociarCentroControlButton.setText("Asociar Centro de Control");
        asociarCentroControlButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                asociarCentroControlButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout centroControlPanelLayout = new javax.swing.GroupLayout(centroControlPanel);
        centroControlPanel.setLayout(centroControlPanelLayout);
        centroControlPanelLayout.setHorizontalGroup(
            centroControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(centroControlPanelLayout.createSequentialGroup()
                .addComponent(urlCentroControlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 47, Short.MAX_VALUE)
                .addComponent(asociarCentroControlButton)
                .addContainerGap())
        );
        centroControlPanelLayout.setVerticalGroup(
            centroControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, centroControlPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(centroControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(urlCentroControlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(asociarCentroControlButton))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(publicacionConvocatoriaPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(datosSimulacionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(centroControlPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(centroControlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(publicacionConvocatoriaPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(datosSimulacionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void infoServidorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_infoServidorButtonActionPerformed
        logger.debug("infoServidorButtonActionPerformed ");
        ActorConIP selectedControlCenter  = 
                hashMapActores.get((String)centrosDeControlComboBox.getSelectedItem());
        InfoServidorDialog infoServidorDialog = new InfoServidorDialog(
                MainFrame.INSTANCIA.getFrames()[0], false, selectedControlCenter);
                infoServidorDialog.setVisible(true);
    }//GEN-LAST:event_infoServidorButtonActionPerformed

    private void asociarCentroControlButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_asociarCentroControlButtonActionPerformed
        AsociarCentroControlDialog asociarCentroControlDialog = 
                new AsociarCentroControlDialog(MainFrame.INSTANCIA.getFrames()[0], false);
        asociarCentroControlDialog.setVisible(true);        
    }//GEN-LAST:event_asociarCentroControlButtonActionPerformed

    private void centrosDeControlComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_centrosDeControlComboBoxActionPerformed

    }//GEN-LAST:event_centrosDeControlComboBoxActionPerformed

    private void publicarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_publicarButtonActionPerformed
        CrearVotacionDialog crearVotacionDialog = new CrearVotacionDialog(
                MainFrame.INSTANCIA.getFrames()[0], false);
        crearVotacionDialog.setVisible(true);
    }//GEN-LAST:event_publicarButtonActionPerformed

    private void infoEventoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_infoEventoButtonActionPerformed
        InfoEventoDialog infoEventoDialog = new InfoEventoDialog(
                MainFrame.INSTANCIA.getFrames()[0], false, evento);
        infoEventoDialog.setVisible(true);
    }//GEN-LAST:event_infoEventoButtonActionPerformed

    private void datosSimulacionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_datosSimulacionButtonActionPerformed
        DatosSimulacionDialog datosSimulacionDialog = new DatosSimulacionDialog(
                MainFrame.INSTANCIA.getFrames()[0], false, evento);
        datosSimulacionDialog.setVisible(true);
    }//GEN-LAST:event_datosSimulacionButtonActionPerformed

    private void lanzarSimulacionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lanzarSimulacionButtonActionPerformed
        switch(estado) {
            case RECOGIDA_DATOS:
                estado = Estado.SIMULACION;
                datosSimulacionButton.setEnabled(false);
                lanzarSimulacionButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/loading.gif")));
                lanzarSimulacionButton.setText("Parar simulación");
                erroresSolicitudesButton.setVisible(false);
                erroresVotosButton.setVisible(false);
                actualizarContadorSolicitudes(0);
                actualizarContadorSolicitudesError(0);
                actualizarContadorVotosLanzados(0);
                actualizarContadorVotosValidados(0);
                actualizarContadorVotosError(0);
                votacion = new Votacion(evento);
                votacion.lanzarVotacion();
                digitalClockPanel.start(DigitalClockPanel.Mode.STOPWATCH);
                break;
            case SIMULACION:
                Object[] opciones = {"Seguir con la simulación",
                        "Cancelar simulación"};
                int n = JOptionPane.showOptionDialog(MainFrame.INSTANCIA.getFrames()[0],
                    "¿Seguro que desea cancelar la ejecución?",
                    "Confirmar operación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    opciones,
                    opciones[1]);
                logger.debug("JOptionPane - n: " + n); 
                if (n == 0) {//Seguir con la simulación
                    logger.debug("Seguir con la simulación");
                } else if (n == 1) {//Cancelar simulación
                    logger.debug("Cancelar simulación");
                }
                estado = Estado.RECOGIDA_DATOS;
                datosSimulacionButton.setEnabled(true);
                lanzarSimulacionButton.setIcon(null);
                lanzarSimulacionButton.setText("Lanzar simulación");
                lanzarSimulacionButton.setIcon(new ImageIcon(getClass().getResource("/images/gnome-run.png")));
                if(votacion != null) votacion.finalizarVotacion();
                digitalClockPanel.stop();
                break;
        }
        contadorPanel.setVisible(true);
        MainFrame.INSTANCIA.packMainFrame();
    }//GEN-LAST:event_lanzarSimulacionButtonActionPerformed

    private void erroresSolicitudesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_erroresSolicitudesButtonActionPerformed
        InfoErroresDialog infoErroresDialog = new InfoErroresDialog(
                MainFrame.INSTANCIA.getFrames()[0], false, 
                "Errores en las solicitudes de acceso", erroresSolicitudes);
        infoErroresDialog.setVisible(true);
    }//GEN-LAST:event_erroresSolicitudesButtonActionPerformed

    private void erroresVotosButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_erroresVotosButtonActionPerformed
        InfoErroresDialog infoErroresDialog = new InfoErroresDialog(
                MainFrame.INSTANCIA.getFrames()[0], false, 
                "Errores en los votos", erroresEnVotos);
        infoErroresDialog.setVisible(true);
    }//GEN-LAST:event_erroresVotosButtonActionPerformed

    private void anularVotosButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_anularVotosButtonActionPerformed
        AnularVotosDialog anularVotosDialog = new AnularVotosDialog(
                MainFrame.INSTANCIA.getFrames()[0], false, evento);
        anularVotosDialog.setVisible(true);
    }//GEN-LAST:event_anularVotosButtonActionPerformed

    public void setControlAcceso(ActorConIP controlAcceso) {
        if(controlAcceso.getCentrosDeControl() != null  &&
                controlAcceso.getCentrosDeControl().size() >0) {
            hashMapActores = new HashMap<String, ActorConIP>();
            ArrayList centrosControlList = new ArrayList();
            Set<ActorConIP> centrosControl = controlAcceso.getCentrosDeControl();
            for(ActorConIP centroControl : centrosControl) {
                centrosControlList.add(centroControl.getServerURL());
                hashMapActores.put(centroControl.getServerURL(), centroControl);
            }
            ArrayListComboBoxModel comboBoxModel = new ArrayListComboBoxModel(centrosControlList);
            centrosDeControlComboBox.setModel(comboBoxModel);
            centrosDeControlComboBox.setSelectedIndex(0);
            ActorConIP centroControlSelected = hashMapActores.get((String)
                    centrosDeControlComboBox.getSelectedItem());
            ContextoPruebas.setCentroControl(centroControlSelected);
            urlCentroControlPanel.setVisible(true);
            publicacionConvocatoriaPanel.setVisible(true);
        }
    }
    
    public void cargarEvento(Evento evento) {
        this.evento = evento;
        asociarCentroControlButton.setVisible(false);
        centrosDeControlComboBox.setEnabled(false);
        publicacionConvocatoriaPanel.setVisible(false);
        asuntoConvocatoriaLabel.setText("<html><b>Asunto de la convocatoria: </b>"
                + evento.getAsunto() + "</html>");
        asuntoConvocatoriaLabel.setVisible(true);
        datosSimulacionPanel.setVisible(true);
        MainFrame.INSTANCIA.packMainFrame();
    }
    
    @Override
    public void process(List<String> messages, SwingWorker worker) { }

    @Override
    public void mostrarResultadoOperacion(SwingWorker worker) {
        logger.debug("mostrarResultadoOperacion - Codigo estado respuesta: " + worker.getState()); 
    }
    
    public void prepararPanelParaLanzarSimulacion() {
        lanzarSimulacionButton.setVisible(true);
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent evt) {
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            logger.debug("Pulsado enlace: " + evt.getURL()); 
            if( !java.awt.Desktop.isDesktopSupported() ) {
                logger.debug("La aplicación no tiene acceso al entorno de escritorio");
                return;
            }
            java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
            try {
                desktop.browse(evt.getURL().toURI());
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }
    
    
    public class ArrayListComboBoxModel extends AbstractListModel implements ComboBoxModel {
        
        private String selectedItem;
        private ArrayList<String> centrosControl;

        public ArrayListComboBoxModel(ArrayList<String> centrosControl) {
            this.centrosControl = centrosControl;
        }

        public Object getSelectedItem() {
            return selectedItem;
        }

        public void setSelectedItem(Object newValue) {
            selectedItem = (String)newValue;
        }

        public int getSize() {
            return centrosControl.size();
        }

        public Object getElementAt(int i) {
            return centrosControl.get(i);
        }
    }
    
    public void mostrarResultadosSimulacion(String resultadosSimulacion, 
            String erroresSolicitudes, String erroresEnVotos) {
        logger.debug("mostrarResultadosSimulacion - erroresSolicitudes: " + erroresSolicitudes);
        digitalClockPanel.stop();
        datosSimulacionButton.setEnabled(true);
        lanzarSimulacionButton.setIcon(null);
        lanzarSimulacionButton.setText("Lanzar simulación");
        anularVotosButton.setVisible(true);
        estado = Estado.RECOGIDA_DATOS;
        if(erroresSolicitudes != null) {
            erroresSolicitudesButton.setVisible(true);
            this.erroresSolicitudes = erroresSolicitudes;
        }
        if(erroresEnVotos != null) {
            erroresVotosButton.setVisible(true);
            this.erroresEnVotos = erroresEnVotos;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                MainFrame.INSTANCIA.packMainFrame();
            }
        }).start();
        
    }
    
    public void actualizarContadorSolicitudes(final int numSolicitud) {
        if(SwingUtilities.isEventDispatchThread()) {
            contadorSolicitudesLabel.setText("<html><b>Lanzada solicitud: </b>"
                     + numSolicitud + "</html>");
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        contadorSolicitudesLabel.setText("<html><b>Lanzada solicitud: </b>"
                        + numSolicitud + "</html>");
                    }
                });
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }
    
    public void actualizarContadorSolicitudesError(final int numSolicitud) {
        if(SwingUtilities.isEventDispatchThread()) {
            contadorSolicitudesErrorLabel.setText(
                    "<html><b>Solicitudes con error: </b>"
                     + numSolicitud + "</html>");
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        contadorSolicitudesErrorLabel.setText(
                        "<html><b>Solicitudes con error: </b>"
                        + numSolicitud + "</html>");
                    }
                });
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }
    
    public void actualizarContadorVotosLanzados(final int numVoto) {
        if(SwingUtilities.isEventDispatchThread()) {
            contadorVotosLabel.setText("<html><b>Lanzado voto: </b>"
                     + numVoto + "</html>");
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        contadorVotosLabel.setText("<html><b>Lanzado voto: </b>"
                     + numVoto + "</html>");
                    }
                });
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }
    
    public void actualizarContadorVotosValidados(final int numVoto) {
        if(SwingUtilities.isEventDispatchThread()) {
            contadorVotosValidadosLabel.setText("<html><b>Recolectado voto: </b>"
                     + numVoto + "</html>");
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        contadorVotosValidadosLabel.setText("<html><b>Recolectado voto: </b>"
                        + numVoto + "</html>");
                    }
                });
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }
    
    public void actualizarContadorVotosError(final int numVoto) {
        if(SwingUtilities.isEventDispatchThread()) {
            contadorVotosErrorLabel.setText("<html><b>Votos con error: </b>"
                     + numVoto + "</html>");
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        contadorVotosErrorLabel.setText("<html><b>Votos con error: </b>"
                        + numVoto + "</html>");
                    }
                });
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton anularVotosButton;
    private javax.swing.JButton asociarCentroControlButton;
    private javax.swing.JLabel asuntoConvocatoriaLabel;
    private javax.swing.JLabel centroControlLabel;
    private javax.swing.JPanel centroControlPanel;
    private javax.swing.JComboBox centrosDeControlComboBox;
    private javax.swing.JPanel contadorPanel;
    private javax.swing.JLabel contadorSolicitudesErrorLabel;
    private javax.swing.JLabel contadorSolicitudesLabel;
    private javax.swing.JLabel contadorVotosErrorLabel;
    private javax.swing.JLabel contadorVotosLabel;
    private javax.swing.JLabel contadorVotosValidadosLabel;
    private javax.swing.JButton datosSimulacionButton;
    private javax.swing.JPanel datosSimulacionPanel;
    private org.sistemavotacion.test.panel.DigitalClockPanel digitalClockPanel;
    private javax.swing.JButton erroresSolicitudesButton;
    private javax.swing.JButton erroresVotosButton;
    private javax.swing.JButton infoEventoButton;
    private javax.swing.JButton infoServidorButton;
    private javax.swing.JButton lanzarSimulacionButton;
    private javax.swing.JLabel mensajePublicacionLabel;
    private javax.swing.JPanel publicacionConvocatoriaPanel;
    private javax.swing.JButton publicarButton;
    private javax.swing.JLabel timeLabel;
    private javax.swing.JPanel timePanel;
    private javax.swing.JPanel urlCentroControlPanel;
    // End of variables declaration//GEN-END:variables
}
