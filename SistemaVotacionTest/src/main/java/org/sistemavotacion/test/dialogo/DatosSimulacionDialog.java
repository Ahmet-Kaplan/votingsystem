package org.sistemavotacion.test.dialogo;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import net.miginfocom.swing.MigLayout;
import org.sistemavotacion.modelo.Evento;
import org.sistemavotacion.modelo.OpcionEvento;
import org.sistemavotacion.test.ContextoPruebas;
import org.sistemavotacion.test.modelo.UserBaseData;
import org.sistemavotacion.test.panel.OpcionVotacionPanel;
import org.sistemavotacion.test.panel.VotacionesPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @author jgzornoza
* Licencia: https://github.com/jgzornoza/SistemaVotacion/wiki/Licencia
*/
public class DatosSimulacionDialog extends JDialog implements KeyListener {

    private static Logger logger = LoggerFactory.getLogger(DatosSimulacionDialog.class);    
        
    private Evento evento = null;
    private Border normalTextBorder;
    private UserBaseData userBaseData = null;
    private List<OpcionVotacionPanel> panelesOpciones = new ArrayList<OpcionVotacionPanel>();

    public DatosSimulacionDialog(Frame parent, boolean modal, 
            Evento evento, UserBaseData userBaseData) {
        super(parent, modal);
        initComponents();
        setLocationRelativeTo(null);
        this.evento = evento;
        this.userBaseData = userBaseData;                    
        if(evento != null) {
            asuntoConvocatoriaLabel.setText("<html><b>Asunto de la convocatoria: </b>"
                + evento.getAsunto() + "<html>");
        }
        opcionesPanel.setVisible(false);
        tiempoPanel.setVisible(false);
        numVotosButtonGroup.add(votosAleatoriosRadioButton);
        numVotosButtonGroup.add(numVotosManualRadioButton);
        tiempoVotacionButtonGroup.add(tiempoMaquinaRadioButton);
        tiempoVotacionButtonGroup.add(introduccionManualTiempoRadioButton);
        normalTextBorder = userBaseEditorPane.getBorder();
        votosAleatoriosRadioButton.setSelected(true);
        tiempoMaquinaRadioButton.setSelected(true);
        opcionesPanel.setLayout(new MigLayout());
        List<OpcionEvento> opciones = evento.getOpciones();
        for(OpcionEvento opcion:opciones) {
            OpcionVotacionPanel opcionVotacionPanel = new OpcionVotacionPanel(opcion);
            opcionesPanel.add(opcionVotacionPanel, "wrap");
            panelesOpciones.add(opcionVotacionPanel);
        }
        validacionPanel.setVisible(false);
        if(userBaseData.isVotacionAleatoria()) {
            votosAleatoriosRadioButton.setSelected(true);
        } else {
            numVotosManualRadioButton.setSelected(true);
            opcionesPanel.setVisible(true);
        }
        if(userBaseData.isSimulacionConTiempos()) {
            introduccionManualTiempoRadioButton.setSelected(true);
            tiempoPanel.setVisible(true);
            horasComboBox.setSelectedIndex(userBaseData.getHorasDuracionVotacion());
            minutosComboBox.setSelectedIndex(userBaseData.getMinutosDuracionVotacion());
        } else {
            tiempoMaquinaRadioButton.setSelected(true);
        }
        userBaseEditorPane.setContentType("text/html");
        userBaseEditorPane.setEditable(false); 
        userBaseEditorPane.setText(userBaseData.toHtml());
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

        numVotosButtonGroup = new javax.swing.ButtonGroup();
        tiempoVotacionButtonGroup = new javax.swing.ButtonGroup();
        asuntoConvocatoriaLabel = new javax.swing.JLabel();
        valorVotosPanel = new javax.swing.JPanel();
        votosAleatoriosRadioButton = new javax.swing.JRadioButton();
        opcionesPanel = new javax.swing.JPanel();
        numVotosManualRadioButton = new javax.swing.JRadioButton();
        tiempoVotacionPanel = new javax.swing.JPanel();
        tiempoMaquinaRadioButton = new javax.swing.JRadioButton();
        introduccionManualTiempoRadioButton = new javax.swing.JRadioButton();
        tiempoPanel = new javax.swing.JPanel();
        duracionSimulacionLabel = new javax.swing.JLabel();
        horasComboBox = new javax.swing.JComboBox();
        horasLabel = new javax.swing.JLabel();
        minutosComboBox = new javax.swing.JComboBox();
        minutosLabel = new javax.swing.JLabel();
        cerrarButton = new javax.swing.JButton();
        aceptarButton = new javax.swing.JButton();
        validacionPanel = new javax.swing.JPanel();
        mensajeValidacionLabel = new javax.swing.JLabel();
        closePanelLabel = new javax.swing.JLabel();
        userBasePanel = new javax.swing.JPanel();
        userBaseScrollPane = new javax.swing.JScrollPane();
        userBaseEditorPane = new javax.swing.JEditorPane();
        userBaseLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/sistemavotacion/test/dialogo/Bundle"); // NOI18N
        setTitle(bundle.getString("DatosSimulacionDialog.title")); // NOI18N

        asuntoConvocatoriaLabel.setText(bundle.getString("DatosSimulacionDialog.asuntoConvocatoriaLabel.text")); // NOI18N
        asuntoConvocatoriaLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        valorVotosPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        votosAleatoriosRadioButton.setText(bundle.getString("DatosSimulacionDialog.votosAleatoriosRadioButton.text")); // NOI18N
        votosAleatoriosRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                votosAleatoriosRadioButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout opcionesPanelLayout = new javax.swing.GroupLayout(opcionesPanel);
        opcionesPanel.setLayout(opcionesPanelLayout);
        opcionesPanelLayout.setHorizontalGroup(
            opcionesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        opcionesPanelLayout.setVerticalGroup(
            opcionesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 66, Short.MAX_VALUE)
        );

        numVotosManualRadioButton.setText(bundle.getString("DatosSimulacionDialog.numVotosManualRadioButton.text")); // NOI18N
        numVotosManualRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                numVotosManualRadioButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout valorVotosPanelLayout = new javax.swing.GroupLayout(valorVotosPanel);
        valorVotosPanel.setLayout(valorVotosPanelLayout);
        valorVotosPanelLayout.setHorizontalGroup(
            valorVotosPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(valorVotosPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(valorVotosPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(opcionesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(valorVotosPanelLayout.createSequentialGroup()
                        .addGroup(valorVotosPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(votosAleatoriosRadioButton)
                            .addComponent(numVotosManualRadioButton))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        valorVotosPanelLayout.setVerticalGroup(
            valorVotosPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(valorVotosPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(votosAleatoriosRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(numVotosManualRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(opcionesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tiempoVotacionPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        tiempoMaquinaRadioButton.setText(bundle.getString("DatosSimulacionDialog.tiempoMaquinaRadioButton.text")); // NOI18N
        tiempoMaquinaRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tiempoMaquinaRadioButtonActionPerformed(evt);
            }
        });

        introduccionManualTiempoRadioButton.setText(bundle.getString("DatosSimulacionDialog.introduccionManualTiempoRadioButton.text")); // NOI18N
        introduccionManualTiempoRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                introduccionManualTiempoRadioButtonActionPerformed(evt);
            }
        });

        duracionSimulacionLabel.setText(bundle.getString("DatosSimulacionDialog.duracionSimulacionLabel.text")); // NOI18N

        horasComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));

        horasLabel.setText(bundle.getString("DatosSimulacionDialog.horasLabel.text")); // NOI18N

        minutosComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));

        minutosLabel.setText(bundle.getString("DatosSimulacionDialog.minutosLabel.text")); // NOI18N

        javax.swing.GroupLayout tiempoPanelLayout = new javax.swing.GroupLayout(tiempoPanel);
        tiempoPanel.setLayout(tiempoPanelLayout);
        tiempoPanelLayout.setHorizontalGroup(
            tiempoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tiempoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(duracionSimulacionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(horasComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(horasLabel)
                .addGap(18, 18, 18)
                .addComponent(minutosComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(minutosLabel)
                .addGap(0, 57, Short.MAX_VALUE))
        );
        tiempoPanelLayout.setVerticalGroup(
            tiempoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tiempoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tiempoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(duracionSimulacionLabel)
                    .addComponent(horasComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(horasLabel)
                    .addComponent(minutosComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(minutosLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout tiempoVotacionPanelLayout = new javax.swing.GroupLayout(tiempoVotacionPanel);
        tiempoVotacionPanel.setLayout(tiempoVotacionPanelLayout);
        tiempoVotacionPanelLayout.setHorizontalGroup(
            tiempoVotacionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tiempoVotacionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tiempoVotacionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(tiempoMaquinaRadioButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(introduccionManualTiempoRadioButton)
                    .addComponent(tiempoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        tiempoVotacionPanelLayout.setVerticalGroup(
            tiempoVotacionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tiempoVotacionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tiempoMaquinaRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(introduccionManualTiempoRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(tiempoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        cerrarButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/cancel_16x16.png"))); // NOI18N
        cerrarButton.setText(bundle.getString("DatosSimulacionDialog.cerrarButton.text")); // NOI18N
        cerrarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cerrarButtonActionPerformed(evt);
            }
        });

        aceptarButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/accept_16x16.png"))); // NOI18N
        aceptarButton.setText(bundle.getString("DatosSimulacionDialog.aceptarButton.text")); // NOI18N
        aceptarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aceptarButtonActionPerformed(evt);
            }
        });

        validacionPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        mensajeValidacionLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        closePanelLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/close.gif"))); // NOI18N
        closePanelLabel.setText(bundle.getString("DatosSimulacionDialog.closePanelLabel.text")); // NOI18N
        closePanelLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                closePanelLabelcloseMensajeUsuario(evt);
            }
        });

        javax.swing.GroupLayout validacionPanelLayout = new javax.swing.GroupLayout(validacionPanel);
        validacionPanel.setLayout(validacionPanelLayout);
        validacionPanelLayout.setHorizontalGroup(
            validacionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(validacionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mensajeValidacionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 380, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(closePanelLabel))
        );
        validacionPanelLayout.setVerticalGroup(
            validacionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mensajeValidacionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
            .addGroup(validacionPanelLayout.createSequentialGroup()
                .addComponent(closePanelLabel)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        userBasePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        userBaseScrollPane.setViewportView(userBaseEditorPane);

        userBaseLabel.setText(bundle.getString("DatosSimulacionDialog.userBaseLabel.text")); // NOI18N

        javax.swing.GroupLayout userBasePanelLayout = new javax.swing.GroupLayout(userBasePanel);
        userBasePanel.setLayout(userBasePanelLayout);
        userBasePanelLayout.setHorizontalGroup(
            userBasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(userBasePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(userBasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(userBaseScrollPane)
                    .addGroup(userBasePanelLayout.createSequentialGroup()
                        .addComponent(userBaseLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        userBasePanelLayout.setVerticalGroup(
            userBasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, userBasePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(userBaseLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(userBaseScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(valorVotosPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tiempoVotacionPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(aceptarButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cerrarButton))
                    .addComponent(asuntoConvocatoriaLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(validacionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(userBasePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(validacionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(asuntoConvocatoriaLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(userBasePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(valorVotosPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tiempoVotacionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cerrarButton)
                    .addComponent(aceptarButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void votosAleatoriosRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_votosAleatoriosRadioButtonActionPerformed
        opcionesPanel.setVisible(false);
        pack();
    }//GEN-LAST:event_votosAleatoriosRadioButtonActionPerformed

    private void introduccionManualTiempoRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_introduccionManualTiempoRadioButtonActionPerformed
        tiempoPanel.setVisible(true);
        pack();
    }//GEN-LAST:event_introduccionManualTiempoRadioButtonActionPerformed

    private void numVotosManualRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numVotosManualRadioButtonActionPerformed
        opcionesPanel.setVisible(true);
        pack();
    }//GEN-LAST:event_numVotosManualRadioButtonActionPerformed

    private void cerrarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cerrarButtonActionPerformed
        dispose();
    }//GEN-LAST:event_cerrarButtonActionPerformed

    private void tiempoMaquinaRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tiempoMaquinaRadioButtonActionPerformed
        tiempoPanel.setVisible(false);
        pack();
    }//GEN-LAST:event_tiempoMaquinaRadioButtonActionPerformed

    private void closePanelLabelcloseMensajeUsuario(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closePanelLabelcloseMensajeUsuario
        mostrarMensajeUsuario(null);
        pack();
    }//GEN-LAST:event_closePanelLabelcloseMensajeUsuario

    private void aceptarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aceptarButtonActionPerformed
        if(!validarFormulario()) return;
        userBaseData.setVotacionAleatoria(votosAleatoriosRadioButton.isSelected());
        userBaseData.setSimulacionConTiempos(introduccionManualTiempoRadioButton.isSelected());
        if(numVotosManualRadioButton.isSelected()) {
            List<OpcionEvento> opciones = new ArrayList<OpcionEvento>();
            for(OpcionVotacionPanel opcionVotacionPanel : panelesOpciones) {
                OpcionEvento opcion = opcionVotacionPanel.getOpcion();
                opciones.add(opcion);
            }
            evento.setOpciones(opciones);
        }
        if(introduccionManualTiempoRadioButton.isSelected()) {
            userBaseData.setHorasDuracionVotacion(horasComboBox.getSelectedIndex());
            userBaseData.setMinutosDuracionVotacion(minutosComboBox.getSelectedIndex());
        }
        logger.debug("Hora: " + userBaseData.getHorasDuracionVotacion() + 
                " - Minuto: " + userBaseData.getMinutosDuracionVotacion());
        
        userBaseData.setEvento(evento);
        ContextoPruebas.setEvento(evento);
        ContextoPruebas.setUserBaseData(userBaseData);
        VotacionesPanel.INSTANCIA.prepararPanelParaLanzarSimulacion();
        dispose();
    }//GEN-LAST:event_aceptarButtonActionPerformed

    public boolean validarFormulario() {
        boolean errores = false;
        opcionesPanel.setBorder(normalTextBorder);
        tiempoPanel.setBorder(normalTextBorder);
        if(numVotosManualRadioButton.isSelected()) {
            Integer sumaVotosOpciones = 0;
            for(OpcionVotacionPanel opcionVotacionPanel : panelesOpciones) {
                OpcionEvento opcion = opcionVotacionPanel.getOpcion();
                if(opcion == null) {
                    errores = true;
                    opcionVotacionPanel.setError(true);
                    mensajeValidacionLabel.setText(opcionVotacionPanel.getMensajeError());
                    break;
                } else {
                    opcionVotacionPanel.setError(false);
                    sumaVotosOpciones += opcion.getNumeroVotos();
                }
            }
        }
        validacionPanel.setVisible(errores);
        pack();
        return !errores;
    }
        
        
    public void mostrarMensajeUsuario(String mensaje) {
        if(mensaje == null) {
            validacionPanel.setVisible(false);
        }else {
            mensajeValidacionLabel.setText(mensaje);
            validacionPanel.setVisible(true);
        }
        pack();
    }
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aceptarButton;
    private javax.swing.JLabel asuntoConvocatoriaLabel;
    private javax.swing.JButton cerrarButton;
    private javax.swing.JLabel closePanelLabel;
    private javax.swing.JLabel duracionSimulacionLabel;
    private javax.swing.JComboBox horasComboBox;
    private javax.swing.JLabel horasLabel;
    private javax.swing.JRadioButton introduccionManualTiempoRadioButton;
    private javax.swing.JLabel mensajeValidacionLabel;
    private javax.swing.JComboBox minutosComboBox;
    private javax.swing.JLabel minutosLabel;
    private javax.swing.ButtonGroup numVotosButtonGroup;
    private javax.swing.JRadioButton numVotosManualRadioButton;
    private javax.swing.JPanel opcionesPanel;
    private javax.swing.JRadioButton tiempoMaquinaRadioButton;
    private javax.swing.JPanel tiempoPanel;
    private javax.swing.ButtonGroup tiempoVotacionButtonGroup;
    private javax.swing.JPanel tiempoVotacionPanel;
    private javax.swing.JEditorPane userBaseEditorPane;
    private javax.swing.JLabel userBaseLabel;
    private javax.swing.JPanel userBasePanel;
    private javax.swing.JScrollPane userBaseScrollPane;
    private javax.swing.JPanel validacionPanel;
    private javax.swing.JPanel valorVotosPanel;
    private javax.swing.JRadioButton votosAleatoriosRadioButton;
    // End of variables declaration//GEN-END:variables

    
    @Override
    public void keyTyped(KeyEvent ke) { }

    @Override
    public void keyPressed(KeyEvent ke) {
        int key = ke.getKeyCode();
        if (key == KeyEvent.VK_ENTER) {
            Toolkit.getDefaultToolkit().beep();
            aceptarButton.doClick();
        }
    }

    @Override
    public void keyReleased(KeyEvent ke) { }
    
}