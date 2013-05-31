package org.sistemavotacion.test.dialogo;

import java.awt.Dimension;
import java.awt.Frame;
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
public class MensajeDialog extends JDialog implements HyperlinkListener {

    private static Logger logger = LoggerFactory.getLogger(MensajeDialog.class);
    
    public enum Tipo {ANULAR_VOTO("AnularVoto.html", "Anulación de voto");
        
        String pagina;
        String titulo;
        
        Tipo(String pagina, String titulo) {
            this.pagina = pagina;
            this.titulo = titulo;
        }
         
        public String getPagina() {
            return pagina;
        }
        
        public String getTitulo() {
            return titulo;
        }
    
    }
    
    public static final String TITULO_ERROR = "Error en la operación";
    public static final String TITULO_OK = "Operación finalizada con éxito";

    private Frame parentFrame;
    
    public MensajeDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        parentFrame = parent;
        setLocationRelativeTo(null);    
        initComponents();
        mensajePane.setEditable(false);
        //mensajePane.setLineWrap(true);
        //mensajePane.setWrapStyleWord(true);
        mensajePane.setContentType("text/html");
        setSize(400, 300);
        mensajePane.addHyperlinkListener(this);
    }
    
    public MensajeDialog(Frame parent, boolean modal, Dimension dimension) {
         this(parent, modal);
         setSize(dimension);
     }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPane = new javax.swing.JScrollPane();
        mensajePane = new javax.swing.JEditorPane();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        scrollPane.setHorizontalScrollBar(null);

        mensajePane.setBackground(java.awt.Color.white);
        scrollPane.setViewportView(mensajePane);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/cancel_16x16.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/sistemavotacion/test/dialogo/Bundle"); // NOI18N
        jButton1.setText(bundle.getString("MensajeDialog.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton1))
                    .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
            dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

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
            java.util.logging.Logger.getLogger(MensajeDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MensajeDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MensajeDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MensajeDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the dialog
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                MensajeDialog dialog = new MensajeDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                logger.debug("--- Mostrando diálogo ---");
                dialog.setVisible(true);
                logger.debug("--- Diálogo mostrado ---");
            }
        });
    }
    
    public void setMessage (String mensajeError, String tituloVentana) {
        if (tituloVentana != null) setTitle(tituloVentana);
        setLocationRelativeTo(null);   
        mensajePane.setText(mensajeError);
        mensajePane.updateUI();
        setVisible(true);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JEditorPane mensajePane;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables

    @Override
    public void hyperlinkUpdate(HyperlinkEvent evt) {
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            logger.debug("Pulsado enlace: " + evt.getURL()); 
            if(evt.getURL() == null && evt.getDescription() != null) {
                if(evt.getDescription().startsWith("SistemaVotacion:")) {
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
    
    private void mostrarDialogoMensaje(String url) {
        Tipo tipo = Tipo.valueOf(url.split("SistemaVotacion:")[1]);
        String theString = new Scanner(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(tipo.getPagina())).useDelimiter("\\A").next();
        MensajeDialog mensajeDialog = new MensajeDialog(MainFrame.INSTANCIA.getFrames()[0], true,
                new Dimension(800, 700));
        mensajeDialog.setMessage(theString, tipo.getTitulo());
    }
    
}
