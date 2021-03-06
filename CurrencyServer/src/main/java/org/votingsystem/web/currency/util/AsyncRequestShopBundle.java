package org.votingsystem.web.currency.util;

import org.votingsystem.dto.currency.TransactionVSDto;
import org.votingsystem.model.TagVS;
import org.votingsystem.model.currency.Currency;
import org.votingsystem.signature.smime.SMIMEMessage;
import org.votingsystem.util.ContextVS;

import javax.ws.rs.container.AsyncResponse;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * License: https://github.com/votingsystem/votingsystem/wiki/Licencia
 */
public class AsyncRequestShopBundle {

    private TransactionVSDto transactionDto;
    private Map<String, Currency> currencyMap;
    private AsyncResponse asyncResponse;

    public AsyncRequestShopBundle(TransactionVSDto dto, AsyncResponse asyncResponse) {
        this.transactionDto = dto;
        this.asyncResponse = asyncResponse;
    }

    public AsyncResponse getAsyncResponse() {
        return asyncResponse;
    }

    public void setAsyncResponse(AsyncResponse asyncResponse) {
        this.asyncResponse = asyncResponse;
    }

    public String addHashCertVS (String currencyServerURL, String hashCertVS) throws Exception {
        if(currencyMap == null) currencyMap = new HashMap<>();
        Currency currency =  new  Currency(currencyServerURL,
                transactionDto.getAmount(), transactionDto.getCurrencyCode(),
                transactionDto.isTimeLimited(), hashCertVS,
                new TagVS(transactionDto.getTagName()));
        currencyMap.put(hashCertVS, currency);
        return new String(currency.getCertificationRequest().getCsrPEM());
    }

    public Currency getCurrency(String hashCertVS) {
        return currencyMap.get(hashCertVS);
    }

    public TransactionVSDto getTransactionDto() {
        return transactionDto;
    }

    public TransactionVSDto getTransactionDto(SMIMEMessage smimeMessage) throws Exception {
        transactionDto.setMessageSMIME(Base64.getEncoder().encodeToString(smimeMessage.getBytes()));
        return transactionDto;
    }

    public void setTransactionDto(TransactionVSDto transactionDto) {
        this.transactionDto = transactionDto;
    }
}
