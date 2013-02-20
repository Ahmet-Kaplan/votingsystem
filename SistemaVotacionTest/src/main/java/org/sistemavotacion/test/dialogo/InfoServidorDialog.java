package org.sistemavotacion.test.dialogo;

import java.awt.Frame;
import javax.swing.JDialog;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.sistemavotacion.modelo.ActorConIP;
import org.sistemavotacion.test.json.Formateadora;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @author jgzornoza
* Licencia: https://github.com/jgzornoza/SistemaVotacion/blob/master/licencia.txt
*/
public class InfoServidorDialog extends JDialog implements HyperlinkListener  {
    
    private static Logger logger = LoggerFactory.getLogger(InfoServidorDialog.class);    
    
    ActorConIP actorConIP;
    Frame parentFrame;

    /**
     * Creates new form InfoServidorDialog
     */
    public InfoServidorDialog(java.awt.Frame parent, boolean modal, ActorConIP actorConIP) {
        super(parent, modal);
        initComponents();
        setLocationRelativeTo(null);
        if(actorConIP == null) return;
        StringBuilder tituloPanelHTML = new StringBuilder("<html><b>");
        StringBuilder tituloPanel = null;
        if(actorConIP.getTipo() == ActorConIP.Tipo.CONTROL_ACCESO) {
            tituloPanelHTML.append("Control de Acceso: </b>");
            tituloPanel = new StringBuilder("Control de Acceso - ");
        } else if(actorConIP.getTipo() == ActorConIP.Tipo.CENTRO_CONTROL) {
            tituloPanelHTML.append("Centro de Control: </b>");
            tituloPanel = new StringBuilder("Centro de Control - ");
        }
        tituloPanelHTML.append(actorConIP.getNombre() + "</html>");
        tituloPanel.append(actorConIP.getNombre());
        nombreServidorLabel.setText(tituloPanelHTML.toString());
        setTitle(tituloPanel.toString());
        editorPane.addHyperlinkListener(this);
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        editorPane.setText(Formateadora.obtenerInfoServidor(actorConIP));
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

        nombreServidorLabel = new javax.swing.JLabel();
        cerrarButton = new javax.swing.JButton();
        scrollPane = new javax.swing.JScrollPane();
        editorPane = new javax.swing.JEditorPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        nombreServidorLabel.setText(" ");

        cerrarButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/cancel_16x16.png"))); // NOI18N
        cerrarButton.setText("Cerrar");
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
                        .addGap(0, 361, Short.MAX_VALUE)
                        .addComponent(cerrarButton))
                    .addComponent(nombreServidorLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE)
                    .addComponent(scrollPane))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(nombreServidorLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
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
            java.util.logging.Logger.getLogger(InfoServidorDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(InfoServidorDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(InfoServidorDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(InfoServidorDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the dialog
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                InfoServidorDialog dialog = new InfoServidorDialog(new javax.swing.JFrame(), true, null);
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
    private javax.swing.JLabel nombreServidorLabel;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables

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
}
