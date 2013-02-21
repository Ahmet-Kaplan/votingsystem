package org.sistemavotacion.test.dialogo;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import org.sistemavotacion.modelo.OpcionEvento;
import org.sistemavotacion.test.ContextoPruebas;
import org.sistemavotacion.test.MainFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jgzornoza
 */
public class OpcionVotacionDialog extends javax.swing.JDialog implements KeyListener {
    
    private static Logger logger = LoggerFactory.getLogger(OpcionVotacionDialog.class);
        
    private OpcionEvento opcionEvento;
    int maximaLongitudCampo = ContextoPruebas.MAXIMALONGITUDCAMPO;
    private Border normalTextBorder;
    
    
    public OpcionVotacionDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setLocationRelativeTo(null);  
        normalTextBorder = textArea.getBorder();
        setTitle("Añadir opción de votación");
        textArea.addKeyListener(this);
        mostrarMensajeUsuario(null);
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

        mensajePanel = new javax.swing.JPanel();
        mensajeLabel = new javax.swing.JLabel();
        closeLabel = new javax.swing.JLabel();
        cerrarButton = new javax.swing.JButton();
        anyadirButton = new javax.swing.JButton();
        scrollPane = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();
        msgLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        mensajePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        mensajeLabel.setFont(new java.awt.Font("DejaVu Sans", 1, 15)); // NOI18N
        mensajeLabel.setForeground(java.awt.Color.red);
        mensajeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        mensajeLabel.setText("Hola");

        closeLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        closeLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/close.gif"))); // NOI18N
        closeLabel.setText(" ");
        closeLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                closeLabelcloseMensajeUsuario(evt);
            }
        });

        javax.swing.GroupLayout mensajePanelLayout = new javax.swing.GroupLayout(mensajePanel);
        mensajePanel.setLayout(mensajePanelLayout);
        mensajePanelLayout.setHorizontalGroup(
            mensajePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mensajePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mensajeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(closeLabel))
        );
        mensajePanelLayout.setVerticalGroup(
            mensajePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mensajePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mensajeLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(mensajePanelLayout.createSequentialGroup()
                .addComponent(closeLabel)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        cerrarButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/cancel_16x16.png"))); // NOI18N
        cerrarButton.setText("Cerrar");
        cerrarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cerrarButtonActionPerformed(evt);
            }
        });

        anyadirButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/accept_16x16.png"))); // NOI18N
        anyadirButton.setText("Añadir");
        anyadirButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                anyadirButtonActionPerformed(evt);
            }
        });

        textArea.setColumns(20);
        textArea.setRows(5);
        scrollPane.setViewportView(textArea);

        msgLabel.setText("Contenido que figurará en la opción:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mensajePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(anyadirButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cerrarButton))
                    .addComponent(scrollPane)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(msgLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(mensajePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(msgLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cerrarButton)
                    .addComponent(anyadirButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeLabelcloseMensajeUsuario(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeLabelcloseMensajeUsuario
        mostrarMensajeUsuario(null);
    }//GEN-LAST:event_closeLabelcloseMensajeUsuario

    private void cerrarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cerrarButtonActionPerformed
        opcionEvento = null;
        this.dispose();
    }//GEN-LAST:event_cerrarButtonActionPerformed

    private void anyadirButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_anyadirButtonActionPerformed
        if (!validarFormulario()) {
            pack();
            return;
        }
        opcionEvento = new OpcionEvento();
        opcionEvento.setContenido(textArea.getText());
        this.dispose();
    }//GEN-LAST:event_anyadirButtonActionPerformed

    public void mostrarMensajeUsuario(String mensaje) {
        if(mensaje == null) {
            mensajePanel.setVisible(false);
        }else {
            mensajeLabel.setText(mensaje);
            mensajePanel.setVisible(true);
        }
        pack();
    }
    
    @Override
    public void keyTyped(KeyEvent ke) { }

    @Override
    public void keyPressed(KeyEvent ke) {
        int key = ke.getKeyCode();
        if (key == KeyEvent.VK_ENTER) {
            Toolkit.getDefaultToolkit().beep();
            anyadirButton.doClick();
        }
    }

    @Override
    public void keyReleased(KeyEvent ke) { }
        
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(OpcionVotacionDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(OpcionVotacionDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(OpcionVotacionDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(OpcionVotacionDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                OpcionVotacionDialog dialog = new OpcionVotacionDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    
    /**
     * @return the opcionEvento
     */
    public OpcionEvento getOpcionEvento() {
        return opcionEvento;
    }

    /**
     * @param opcionEvento the opcionEvento to set
     */
    public void setOpcionEvento(OpcionEvento opcionEvento) {
        this.opcionEvento = opcionEvento;
    }
    
    public boolean validarFormulario() {
        boolean errores = false;
        logger.debug(" - validarFormulario - textArea.getText(): " + textArea.getText());
        
        if (textArea.getText().trim() == null ||
                "".equals(textArea.getText().trim())) {
            mostrarMensajeUsuario("<html>El campo no puede ir vacío</html>");
            textArea.setBorder(new LineBorder(Color.RED,2));
            errores = true;
        } else {
            if (textArea.getText().length() > maximaLongitudCampo) {
                mostrarMensajeUsuario("<html>El campo no puede tener "
                        + "una tamaño de más de " + maximaLongitudCampo +
                        " caracteres</html>");
                errores = true;
            }
        }
        if (errores) {
            return false;
        } else mostrarMensajeUsuario(null);
            textArea.setBorder(normalTextBorder);
        return true;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton anyadirButton;
    private javax.swing.JButton cerrarButton;
    private javax.swing.JLabel closeLabel;
    private javax.swing.JLabel mensajeLabel;
    private javax.swing.JPanel mensajePanel;
    private javax.swing.JLabel msgLabel;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTextArea textArea;
    // End of variables declaration//GEN-END:variables
}
