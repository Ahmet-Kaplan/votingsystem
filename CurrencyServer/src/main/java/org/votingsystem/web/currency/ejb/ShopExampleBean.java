package org.votingsystem.web.currency.ejb;

import org.votingsystem.dto.MessageDto;
import org.votingsystem.dto.currency.CurrencyCertExtensionDto;
import org.votingsystem.dto.currency.TransactionResponseDto;
import org.votingsystem.dto.currency.TransactionVSDto;
import org.votingsystem.model.ResponseVS;
import org.votingsystem.model.currency.Currency;
import org.votingsystem.signature.smime.SMIMEMessage;
import org.votingsystem.signature.util.CertUtils;
import org.votingsystem.throwable.ExceptionVS;
import org.votingsystem.util.ContextVS;
import org.votingsystem.util.JSON;
import org.votingsystem.util.MediaTypeVS;
import org.votingsystem.web.currency.util.AsyncRequestShopBundle;
import org.votingsystem.web.util.ConfigVS;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * License: https://github.com/votingsystem/votingsystem/wiki/Licencia
 */
@Stateless
public class ShopExampleBean {

    private static Logger log = Logger.getLogger(ShopExampleBean.class.getName());

    private static final Map<String, AsyncRequestShopBundle> transactionRequestMap = new HashMap<>();

    @Inject ConfigVS config;


    public void putTransactionRequest(String sessionId, TransactionVSDto transactionRequest) {
        log.info("putTransactionRequest - sessionId $sessionId");
        transactionRequestMap.put(sessionId, new AsyncRequestShopBundle(transactionRequest, null));
    }

    public AsyncRequestShopBundle getRequestBundle(String sessionId) {
        if(transactionRequestMap.containsKey(sessionId)) {
            transactionRequestMap.get(sessionId).getAsyncResponse().setTimeout(180, TimeUnit.SECONDS);
            return transactionRequestMap.get(sessionId);
        } else return null;
    }

    public TransactionVSDto getTransactionRequest(String sessionId) {
        if(transactionRequestMap.containsKey(sessionId)) {
            transactionRequestMap.get(sessionId).getAsyncResponse().setTimeout(180, TimeUnit.SECONDS);
            return transactionRequestMap.get(sessionId).getTransactionDto();
        } else return null;
    }

    public Set<String> getSessionKeys() {
        return transactionRequestMap.keySet();
    }

    public int bindContext(String sessionId, AsyncResponse asyncResponse) {
        if(transactionRequestMap.get(sessionId) != null) {
            transactionRequestMap.get(sessionId).setAsyncResponse(asyncResponse);
            return ResponseVS.SC_OK;
        } else return ResponseVS.SC_ERROR;
    }

    public void sendResponse(String sessionId, TransactionResponseDto responseDto) throws Exception{
        log.info("sendResponse");
        SMIMEMessage smimeMessage = responseDto.getSmime();
        AsyncRequestShopBundle requestBundle = transactionRequestMap.remove(sessionId);
        if(requestBundle != null) {
            try {
                if(responseDto.getCurrencyChangeCert() != null) {
                    X509Certificate currencyCert = CertUtils.fromPEMToX509Cert(responseDto.getCurrencyChangeCert().getBytes());
                    CurrencyCertExtensionDto certExtensionDto = CertUtils.getCertExtensionData(CurrencyCertExtensionDto.class,
                            currencyCert, ContextVS.CURRENCY_OID);
                    Currency currency = requestBundle.getCurrency(certExtensionDto.getHashCertVS());
                    currency.initSigner(responseDto.getCurrencyChangeCert().getBytes());
                    log.info("TODO - currency OK save to wallet");
                }
                requestBundle.getTransactionDto().validateReceipt(smimeMessage, true);
                MessageDto<TransactionVSDto> messageDto = MessageDto.OK("OK");
                messageDto.setData(requestBundle.getTransactionDto());
                requestBundle.getAsyncResponse().resume(Response.ok().entity(JSON.getMapper()
                        .writeValueAsBytes(messageDto)).type(MediaTypeVS.JSON).build());
            } catch (Exception ex) {
                log.log(Level.SEVERE, ex.getMessage(), ex);
                requestBundle.getAsyncResponse().resume(Response.status(ResponseVS.SC_OK).entity(
                        JSON.getMapper().writeValueAsBytes(MessageDto.ERROR(ex.getMessage()))).build());
            }
        } else throw new ExceptionVS("transactionRequest with sessionId:" + sessionId + " has expired");
    }

}