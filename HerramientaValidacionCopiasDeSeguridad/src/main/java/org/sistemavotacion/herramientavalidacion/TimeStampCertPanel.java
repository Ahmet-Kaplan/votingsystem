package org.sistemavotacion.herramientavalidacion;

import java.awt.Frame;
import java.security.cert.X509Certificate;
import javax.swing.JFrame;
import org.sistemavotacion.smime.CMSUtils;

/**
* @author jgzornoza
* Licencia: https://github.com/jgzornoza/SistemaVotacion/wiki/Licencia
*/
public class TimeStampCertPanel extends javax.swing.JPanel {

    private X509Certificate certificate = null;

    public TimeStampCertPanel(X509Certificate certificate, boolean isSigner) {
        this.certificate = certificate;
        initComponents();
        if(!isSigner) {
            signerLabel.setVisible(false);
        }
        certEditorPane.setEditable(false);
        certEditorPane.setContentType("text/html");
        certEditorPane.setText(CMSUtils.obtenerInfoCertificado(certificate));
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
        certEditorPane = new javax.swing.JEditorPane();
        pemCertDataButton = new javax.swing.JButton();
        signerLabel = new javax.swing.JLabel();

        scrollPane.setViewportView(certEditorPane);

        pemCertDataButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/images/signature-ok_16x16.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/sistemavotacion/herramientavalidacion/Bundle"); // NOI18N
        pemCertDataButton.setText(bundle.getString("TimeStampCertPanel.pemCertDataButton.text")); // NOI18N
        pemCertDataButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pemCertDataButtonActionPerformed(evt);
            }
        });

        signerLabel.setFont(new java.awt.Font("DejaVu Sans", 3, 15)); // NOI18N
        signerLabel.setForeground(new java.awt.Color(35, 113, 30));
        signerLabel.setText(bundle.getString("TimeStampCertPanel.signerLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(signerLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pemCertDataButton))
            .addComponent(scrollPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pemCertDataButton)
                    .addComponent(signerLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void pemCertDataButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pemCertDataButtonActionPerformed
        Frame frame;
        Frame[] frames = JFrame.getFrames();
        if(frames.length == 0 || frames[0] == null) frame = 
                new javax.swing.JFrame();
        else frame = frames[0];
        PEMCertDialog pemCertDialog = new PEMCertDialog(frame, true, certificate);
        pemCertDialog.setVisible(true);
    }//GEN-LAST:event_pemCertDataButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane certEditorPane;
    private javax.swing.JButton pemCertDataButton;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JLabel signerLabel;
    // End of variables declaration//GEN-END:variables
}
