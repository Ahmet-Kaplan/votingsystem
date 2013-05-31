package org.sistemavotacion.test.dialogo;

import java.awt.Dimension;
import java.awt.Frame;
import java.io.File;
import java.util.Scanner;
import javax.swing.JDialog;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.sistemavotacion.test.MainFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @author jgzornoza
* Licencia: https://github.com/jgzornoza/SistemaVotacion/wiki/Licencia
*/
public class InfoErroresDialog extends JDialog implements HyperlinkListener  {
    
    private static Logger logger = LoggerFactory.getLogger(InfoErroresDialog.class);    
    
    Frame parentFrame;

    /**
     * Creates new form InfoErroresDialog
     */
    public InfoErroresDialog(java.awt.Frame parent, boolean modal, 
            String tipoError, String mensajeErrores) {
        super(parent, modal);
        initComponents();
        setLocationRelativeTo(null);
        editorPane.addHyperlinkListener(this);
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        tipoErrorLabel.setText("<html><b>" + tipoError + "</b></html>");
        editorPane.setText(mensajeErrores);
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

        tipoErrorLabel = new javax.swing.JLabel();
        cerrarButton = new javax.swing.JButton();
        scrollPane = new javax.swing.JScrollPane();
        editorPane = new javax.swing.JEditorPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/sistemavotacion/test/dialogo/Bundle"); // NOI18N
        setTitle(bundle.getString("InfoErroresDialog.title")); // NOI18N
        setPreferredSize(new java.awt.Dimension(600, 450));

        tipoErrorLabel.setText(bundle.getString("InfoErroresDialog.tipoErrorLabel.text")); // NOI18N

        cerrarButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/cancel_16x16.png"))); // NOI18N
        cerrarButton.setText(bundle.getString("InfoErroresDialog.cerrarButton.text")); // NOI18N
        cerrarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cerrarButtonActionPerformed(evt);
            }
        });

        scrollPane.setBackground(java.awt.Color.white);

        editorPane.setBackground(java.awt.Color.white);
        scrollPane.setViewportView(editorPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(cerrarButton))
                    .addComponent(tipoErrorLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scrollPane))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tipoErrorLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cerrarButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cerrarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cerrarButtonActionPerformed
        dispose();
    }//GEN-LAST:event_cerrarButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(InfoErroresDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(InfoErroresDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(InfoErroresDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(InfoErroresDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the dialog
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                InfoErroresDialog dialog = new InfoErroresDialog(new javax.swing.JFrame(), true, null, null);
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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cerrarButton;
    private javax.swing.JEditorPane editorPane;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JLabel tipoErrorLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void hyperlinkUpdate(HyperlinkEvent evt) {
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                logger.debug("Pulsado enlace: " + evt.getURL()); 
                if(evt.getURL() == null && evt.getDescription() != null) {
                    if(evt.getDescription().startsWith("SistemaVotacion:File:")) {
                        File file = new File(
                                evt.getDescription().split("SistemaVotacion:File:")[1]);
                        mostrarExploradorArchivos(file);
                        return;
                    } else if(evt.getDescription().startsWith("SistemaVotacion:")) {
                        mostrarDialogoMensaje(evt.getDescription());
                        return;
                    }
                }
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
    }
    
    private void mostrarExploradorArchivos(File file) {
        if( !java.awt.Desktop.isDesktopSupported() ) {
            logger.debug("La aplicación no tiene acceso al entorno de escritorio");
            return;
        }
        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
        try {
            desktop.open(file);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
    
    private void mostrarDialogoMensaje(String url) {
        MensajeDialog.Tipo tipo = MensajeDialog.Tipo.valueOf(url.split("SistemaVotacion:")[1]);
        String theString = new Scanner(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(tipo.getPagina())).useDelimiter("\\A").next();
        MensajeDialog mensajeDialog = new MensajeDialog(MainFrame.INSTANCIA.getFrames()[0], true,
                new Dimension(800, 700));
        mensajeDialog.setMessage(theString, tipo.getTitulo());
    }
}
