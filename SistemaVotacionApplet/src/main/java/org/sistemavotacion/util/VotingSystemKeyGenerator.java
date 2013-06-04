package org.sistemavotacion.util;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import org.sistemavotacion.Contexto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author jgzornoza
 */
public enum VotingSystemKeyGenerator {
    
    INSTANCE;   
    
    private KeyPairGenerator keyPairGenerator;
    
    private static Logger logger = LoggerFactory.getLogger(
            VotingSystemKeyGenerator.class);
    
    private VotingSystemKeyGenerator() {
        try {
            keyPairGenerator  = KeyPairGenerator.getInstance(
                Contexto.SIG_NAME, Contexto.PROVIDER);
            keyPairGenerator.initialize(Contexto.KEY_SIZE, new SecureRandom());
        } catch (Exception ex) {
            LoggerFactory.getLogger(VotingSystemKeyGenerator.class).error(ex.getMessage(), ex);
        }
    }     
     
     public synchronized KeyPair genKeyPair () {
         return keyPairGenerator.genKeyPair();
     } 
}