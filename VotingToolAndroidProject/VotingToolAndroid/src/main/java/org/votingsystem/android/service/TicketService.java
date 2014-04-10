package org.votingsystem.android.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.bouncycastle2.asn1.DERTaggedObject;
import org.bouncycastle2.asn1.DERUTF8String;
import org.bouncycastle2.util.encoders.Base64;
import org.bouncycastle2.x509.extension.X509ExtensionUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.votingsystem.android.AppContextVS;
import org.votingsystem.android.R;
import org.votingsystem.android.callable.MessageTimeStamper;
import org.votingsystem.android.callable.SMIMESignedSender;
import org.votingsystem.android.callable.SignedMapSender;
import org.votingsystem.android.contentprovider.TicketContentProvider;
import org.votingsystem.android.contentprovider.TransactionVSContentProvider;
import org.votingsystem.android.contentprovider.Utils;
import org.votingsystem.model.ActorVS;
import org.votingsystem.model.ContentTypeVS;
import org.votingsystem.model.ContextVS;
import org.votingsystem.model.CurrencyData;
import org.votingsystem.model.CurrencyVS;
import org.votingsystem.model.ResponseVS;
import org.votingsystem.model.TicketAccount;
import org.votingsystem.model.TicketServer;
import org.votingsystem.model.TicketVS;
import org.votingsystem.model.TransactionVS;
import org.votingsystem.model.TypeVS;
import org.votingsystem.signature.smime.SMIMEMessageWrapper;
import org.votingsystem.signature.util.CertUtil;
import org.votingsystem.signature.util.Encryptor;
import org.votingsystem.util.DateUtils;
import org.votingsystem.util.HttpHelper;
import org.votingsystem.util.ObjectUtils;
import org.votingsystem.util.StringUtils;
import org.votingsystem.util.TimestampException;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author jgzornoza
 * Licencia: https://github.com/jgzornoza/SistemaVotacion/wiki/Licencia
 */
public class TicketService extends IntentService {

    public static final String TAG = TicketService.class.getSimpleName();

    public TicketService() { super(TAG); }

    private AppContextVS contextVS;

    @Override protected void onHandleIntent(Intent intent) {
        contextVS = (AppContextVS) getApplicationContext();
        final Bundle arguments = intent.getExtras();
        TypeVS operationType = (TypeVS)arguments.getSerializable(ContextVS.TYPEVS_KEY);
        String serviceCaller = arguments.getString(ContextVS.CALLER_KEY);
        Uri uriData =  arguments.getParcelable(ContextVS.URI_KEY);;
        BigDecimal amount = (BigDecimal) arguments.getSerializable(ContextVS.VALUE_KEY);
        CurrencyVS currencyVS = (CurrencyVS) arguments.getSerializable(ContextVS.CURRENCY_KEY);
        ResponseVS responseVS = null;
        switch(operationType) {
            case TICKET_USER_INFO:
                responseVS = updateUserInfo();
                responseVS.setTypeVS(operationType);
                responseVS.setServiceCaller(serviceCaller);
                contextVS.sendBroadcast(responseVS);
                break;
            case TICKET_REQUEST:
                responseVS = ticketRequest(amount, currencyVS);
                responseVS.setTypeVS(operationType);
                responseVS.setServiceCaller(serviceCaller);
                contextVS.showNotification(responseVS);
                contextVS.sendBroadcast(responseVS);
                break;
            case TICKET_SEND:
                amount = new BigDecimal(uriData.getQueryParameter("amount"));
                currencyVS = CurrencyVS.valueOf(uriData.getQueryParameter("currency"));
                String subject = uriData.getQueryParameter("subject");
                String receptor = uriData.getQueryParameter("receptor");
                String IBAN = uriData.getQueryParameter("IBAN");
                responseVS = ticketSend(amount, currencyVS, subject, receptor, IBAN);
                responseVS.setTypeVS(operationType);
                responseVS.setServiceCaller(serviceCaller);
                contextVS.showNotification(responseVS);
                contextVS.sendBroadcast(responseVS);
                break;
            case TICKET_CANCEL:
                Integer ticketCursorPosition = arguments.getInt(ContextVS.ITEM_ID_KEY);
                Cursor cursor = getContentResolver().query(TicketContentProvider.CONTENT_URI,
                        null, null, null, null);
                cursor.moveToPosition(ticketCursorPosition);
                byte[] serializedTicket = cursor.getBlob(cursor.getColumnIndex(
                        TicketContentProvider.SERIALIZED_OBJECT_COL));
                Long ticketId = cursor.getLong(cursor.getColumnIndex(TicketContentProvider.ID_COL));
                try {
                    TicketVS ticketVS = (TicketVS) ObjectUtils.deSerializeObject(serializedTicket);
                    ticketVS.setLocalId(ticketCursorPosition.longValue());
                    responseVS = cancelTicket(ticketVS);
                    if(ResponseVS.SC_OK == responseVS.getStatusCode()) {
                        ticketVS.setCancellationReceipt(responseVS.getSmimeMessage());
                        ticketVS.setState(TicketVS.State.CANCELLED);
                        ContentValues values = TicketContentProvider.populateTicketContentValues(
                                contextVS, ticketVS);
                        getContentResolver().update(TicketContentProvider.getTicketURI(ticketId),
                                values, null, null);
                        responseVS.setCaption(getString(R.string.ticket_cancellation_msg_subject));
                        responseVS.setNotificationMessage(getString(R.string.ticket_cancellation_msg));
                        responseVS.setIconId(R.drawable.accept_22);
                    } else {
                        responseVS.setCaption(getString(R.string.ticket_cancellation_error_msg_subject));
                        responseVS.setNotificationMessage(getString(R.string.ticket_cancellation_error_msg_subject));
                        responseVS.setIconId(R.drawable.cancel_22);
                        if(responseVS.getContentType() == ContentTypeVS.JSON_SIGNED_AND_ENCRYPTED) {
                            SMIMEMessageWrapper signedMessage = responseVS.getSmimeMessage();
                            Log.d(TAG + ".cancelTicket(...)", "error JSON response: " + signedMessage.getSignedContent());
                            JSONObject jsonResponse = new JSONObject(signedMessage.getSignedContent());
                            TypeVS operation = TypeVS.valueOf(jsonResponse.getString("operation"));
                            if(TypeVS.TICKET_CANCEL == operation) {
                                ticketVS.setCancellationReceipt(responseVS.getSmimeMessage());
                                ticketVS.setState(TicketVS.State.LAPSED);
                                ticketVS.setLocalId(ticketId);
                                TicketContentProvider.updateTicket(contextVS, ticketVS);
                                responseVS.setCaption(getString(R.string.ticket_cancellation_msg_subject));
                                responseVS.setNotificationMessage(getString(R.string.ticket_cancellation_msg));
                                responseVS.setIconId(R.drawable.accept_22);
                            }
                        }
                    }
                    responseVS.setServiceCaller(serviceCaller);
                    contextVS.showNotification(responseVS);
                    contextVS.sendBroadcast(responseVS);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break;
        }
    }

    private ResponseVS cancelTicket(TicketVS ticket) {
        ResponseVS responseVS = getTicketServer();
        if(ResponseVS.SC_OK != responseVS.getStatusCode()) return responseVS;
        TicketServer ticketServer = (TicketServer) responseVS.getData();
        Map ticketCancellationDataMap = new HashMap();
        ticketCancellationDataMap.put("UUID", UUID.randomUUID().toString());
        ticketCancellationDataMap.put("operation", TypeVS.TICKET_CANCEL.toString());
        ticketCancellationDataMap.put("hashCertVSBase64", ticket.getHashCertVSBase64());
        ticketCancellationDataMap.put("originHashCertVS", ticket.getOriginHashCertVS());
        ticketCancellationDataMap.put("ticketCertSerialNumber", ticket.getCertificationRequest().
                getCertificate().getSerialNumber().longValue());
        String textToSing = new JSONObject(ticketCancellationDataMap).toString();
        SMIMESignedSender signedSender = new SMIMESignedSender(contextVS.getUserVS().getNif(),
                ticketServer.getNameNormalized(), ticketServer.getTicketCancelServiceURL(),
                textToSing, ContentTypeVS.JSON_SIGNED_AND_ENCRYPTED,
                getString(R.string.ticket_cancellation_msg_subject), ticketServer.getCertificate(),
                (AppContextVS)getApplicationContext());
        responseVS = signedSender.call();
        return responseVS;
    }

    private ResponseVS cancelTickets(Collection<TicketVS> sendedTickets) {
        Log.d(TAG + ".cancelTickets(...)", "cancelTickets");
        TicketServer ticketServer = contextVS.getTicketServer();
        ResponseVS responseVS = null;
        try {
            List<String> cancellationList = new ArrayList<String>();
            for(TicketVS ticket : sendedTickets) {
                Map ticketCancellationDataMap = new HashMap();
                ticketCancellationDataMap.put("UUID", UUID.randomUUID().toString());
                ticketCancellationDataMap.put("operation", TypeVS.TICKET_CANCEL.toString());
                ticketCancellationDataMap.put("hashCertVSBase64", ticket.getHashCertVSBase64());
                ticketCancellationDataMap.put("originHashCertVS", ticket.getOriginHashCertVS());
                ticketCancellationDataMap.put("ticketCertSerialNumber", ticket.getCertificationRequest().
                        getCertificate().getSerialNumber().longValue());
                String textToSing = new JSONObject(ticketCancellationDataMap).toString();
                responseVS = contextVS.signMessage(ticketServer.getNameNormalized(), textToSing,
                        getString(R.string.ticket_cancellation_msg_subject));
                cancellationList.add(new String(Base64.encode(responseVS.getSmimeMessage().getBytes())));
            }
            Map requestMap = new HashMap();
            requestMap.put("ticketCancellationList", cancellationList);
            byte[] messageToSend = Encryptor.encryptToCMS(new JSONObject(requestMap).toString().getBytes(),
                    ticketServer.getCertificate());
            responseVS = HttpHelper.sendData(messageToSend, ContentTypeVS.JSON_ENCRYPTED,
                    ticketServer.getTicketBatchCancellationServiceURL());
        } catch(Exception ex) {
            ex.printStackTrace();
            String message = ex.getMessage();
            if(message == null || message.isEmpty()) message = getString(R.string.exception_lbl);
            responseVS = ResponseVS.getExceptionResponse(getString(R.string.exception_lbl),
                    message);
        } finally {
            return responseVS;
        }
    }

    private ResponseVS ticketSend(BigDecimal requestAmount, CurrencyVS currencyVS,
            String subject, String receptor, String IBAN) {
        ResponseVS responseVS = null;
        String message = null;
        String caption = null;
        byte[] decryptedMessageBytes = null;
        Integer iconId = R.drawable.cancel_22;
        Map <Long,TicketVS> sendedTicketsMap = new HashMap<Long, TicketVS>();
        try {
            CurrencyData availableCurrencyData = Utils.getCurrencyData(contextVS, currencyVS);
            BigDecimal available = availableCurrencyData.getCashBalance();
            if(available.compareTo(requestAmount) < 0) {
                throw new Exception(getString(R.string.insufficient_cash_msg, currencyVS.toString(),
                        requestAmount.toString(), available.toString()));
            }
            responseVS = getTicketServer();
            if(ResponseVS.SC_OK != responseVS.getStatusCode()) return responseVS;
            TicketServer ticketServer = (TicketServer) responseVS.getData();

            BigDecimal ticketAmount = new BigDecimal(10);
            int numTickets = requestAmount.divide(ticketAmount).intValue();
            List<TicketVS> ticketsToSend = new ArrayList<TicketVS>();
            for(int i = 0; i < numTickets; i++) {
                ticketsToSend.add(availableCurrencyData.getTicketList().remove(0));
            }
            Map mapToSend = new HashMap();
            mapToSend.put("receptor", receptor);
            mapToSend.put("operation", TypeVS.TICKET.toString());
            mapToSend.put("subject", subject);
            mapToSend.put("IBAN", IBAN);
            mapToSend.put("currency", currencyVS.toString());
            mapToSend.put("amount", ticketAmount.toString());

            List<String> smimeTicketList = new ArrayList<String>();

            for(TicketVS ticketVS : ticketsToSend) {
                mapToSend.put("UUID", UUID.randomUUID().toString());
                String textToSign = new JSONObject(mapToSend).toString();
                SMIMEMessageWrapper smimeMessage = ticketVS.getCertificationRequest().genMimeMessage(
                        ticketVS.getHashCertVSBase64(), StringUtils.getNormalized(receptor),
                        textToSign, subject, null);

                smimeMessage.getSigner().getCertificate().getSerialNumber();
                MessageTimeStamper timeStamper = new MessageTimeStamper(smimeMessage, contextVS);
                responseVS = timeStamper.call();
                if(ResponseVS.SC_OK != responseVS.getStatusCode())
                    throw new TimestampException(responseVS.getMessage());

                smimeTicketList.add(new String(Base64.encode(smimeMessage.getBytes())));
                sendedTicketsMap.put(ticketVS.getCertificationRequest().getCertificate().
                        getSerialNumber().longValue(), ticketVS);
            }
            KeyPair keyPair = sendedTicketsMap.values().iterator().next().
                    getCertificationRequest().getKeyPair();
            String publicKeyStr = new String(Base64.encode(keyPair.getPublic().getEncoded()));
            if(ResponseVS.SC_OK == responseVS.getStatusCode()) {
                mapToSend.put("amount", requestAmount.toString());
                mapToSend.put("currency", currencyVS.toString());
                mapToSend.put("operation", TypeVS.TICKET_SEND.toString());
                mapToSend.put("tickets", smimeTicketList);
                mapToSend.put("publicKey", publicKeyStr);
                String textToSign = new JSONObject(mapToSend).toString();
                byte[] messageToSend = Encryptor.encryptToCMS(textToSign.getBytes(),
                        ticketServer.getCertificate());
                responseVS = HttpHelper.sendData(messageToSend, ContentTypeVS.JSON_ENCRYPTED,
                        ticketServer.getTicketBatchServiceURL());
                if(responseVS.getContentType()!= null && responseVS.getContentType().isEncrypted()) {
                    decryptedMessageBytes = Encryptor.decryptCMS(keyPair.getPrivate(),
                            responseVS.getMessageBytes());
                    if(ResponseVS.SC_OK == responseVS.getStatusCode()) {
                        JSONObject jsonResponse = new JSONObject(new String(decryptedMessageBytes));
                        JSONArray jsonArray = jsonResponse.getJSONArray("tickets");
                        Date dateCreated = null;
                        for(int i = 0; i < jsonArray.length(); i ++) {
                            SMIMEMessageWrapper smimeMessage = new SMIMEMessageWrapper(null,
                                    new ByteArrayInputStream(Base64.decode(jsonArray.getString(i))), null);
                            dateCreated = smimeMessage.getSigner().getTimeStampToken().
                                    getTimeStampInfo().getGenTime();
                            TicketVS sendedTicket = sendedTicketsMap.get(smimeMessage.getSigner().getCertificate().
                                    getSerialNumber().longValue());
                            sendedTicket.setReceiptBytes(smimeMessage.getBytes());
                            sendedTicket.setState(TicketVS.State.EXPENDED);
                            TicketContentProvider.updateTicket(contextVS, sendedTicket);
                        }
                        List<TicketVS> ticketList = new ArrayList<TicketVS>();
                        ticketList.addAll(sendedTicketsMap.values());
                        TransactionVS ticketSendTransaction = new TransactionVS(TransactionVS.Type.
                                TICKET_SEND, dateCreated, ticketList, requestAmount, currencyVS);
                        TransactionVSContentProvider.addTransaction(contextVS,
                                ticketSendTransaction, contextVS.getCurrentWeekLapseId());
                    }
                }
            }
        } catch(TimestampException tex) {
            responseVS.setStatusCode(ResponseVS.SC_ERROR_TIMESTAMP);
            responseVS.setCaption(getString(R.string.timestamp_service_error_caption));
            responseVS.setNotificationMessage(tex.getMessage());
        } catch(Exception ex) {
            ex.printStackTrace();
            cancelTickets(sendedTicketsMap.values());
            message = ex.getMessage();
            if(message == null || message.isEmpty()) message = getString(R.string.exception_lbl);
            responseVS = ResponseVS.getExceptionResponse(getString(R.string.exception_lbl),
                    message);
        } finally {
            if(ResponseVS.SC_OK != responseVS.getStatusCode() &&
                    ResponseVS.SC_ERROR_REQUEST_REPEATED == responseVS.getStatusCode()) {
                Log.d(TAG + ".cancelTicketRepeated(...)", "cancelTicketRepeated");
                try {
                    JSONObject responseJSON = new JSONObject(new String(decryptedMessageBytes, ContextVS.UTF_8));
                    String base64EncodedTicketRepeated = responseJSON.getString("messageSMIME");
                    SMIMEMessageWrapper smimeMessage = new SMIMEMessageWrapper(null,
                            new ByteArrayInputStream(Base64.decode(base64EncodedTicketRepeated)), null);
                    Long repeatedCertSerialNumber = smimeMessage.getSigner().getCertificate().
                            getSerialNumber().longValue();
                    TicketVS repeatedTicket = sendedTicketsMap.get(repeatedCertSerialNumber);
                    repeatedTicket.setState(TicketVS.State.EXPENDED);
                    TicketContentProvider.updateTicket(contextVS, repeatedTicket);
                } catch(Exception ex) {
                    ex.printStackTrace();
                    responseVS = ResponseVS.getExceptionResponse(getString(R.string.exception_lbl),
                            ex.getMessage());
                }
                caption = getString(R.string.ticket_send_error_caption);
                message = getString(R.string.ticket_repeated_error_msg);
            } else if(ResponseVS.SC_ERROR_REQUEST_REPEATED == responseVS.getStatusCode()) {
                caption = getString(R.string.ticket_send_error_caption);
                message = getString(R.string.ticket_expended_send_error_msg);
            } else {
                for(TicketVS ticket:sendedTicketsMap.values())
                    TicketContentProvider.updateTicket(contextVS, ticket);

                iconId = R.drawable.euro_24;
                caption = getString(R.string.ticket_send_ok_caption);
                message = getString(R.string.ticket_send_ok_msg, requestAmount.toString(),
                        currencyVS.toString(), subject, receptor);
            }
            responseVS.setIconId(iconId);
            responseVS.setCaption(caption);
            responseVS.setNotificationMessage(message);
            return responseVS;
        }
    }

    private ResponseVS ticketRequest(BigDecimal requestAmount, CurrencyVS currencyVS) {
        ResponseVS responseVS = getTicketServer();
        if(ResponseVS.SC_OK != responseVS.getStatusCode()) return responseVS;
        TicketServer ticketServer = (TicketServer) responseVS.getData();
        Map<String, TicketVS> ticketsMap = new HashMap<String, TicketVS>();
        String caption = null;
        String message = null;
        int iconId = R.drawable.cancel_22;
        try {
            BigDecimal numTickets = requestAmount.divide(new BigDecimal(10));
            BigDecimal ticketsValue = new BigDecimal(10);
            List<TicketVS> ticketList = new ArrayList<TicketVS>();
            for(int i = 0; i < numTickets.intValue(); i++) {
                TicketVS ticketVS = new TicketVS(ticketServer.getServerURL(),
                        ticketsValue, currencyVS, TypeVS.TICKET);
                ticketList.add(ticketVS);
                ticketsMap.put(ticketVS.getHashCertVSBase64(), ticketVS);
            }

            String messageSubject = getString(R.string.ticket_request_msg_subject);
            String fromUser = contextVS.getUserVS().getNif();

            Map requestTicketMap = new HashMap();
            requestTicketMap.put("numTickets", numTickets.intValue());
            requestTicketMap.put("ticketValue", ticketsValue.intValue());

            List ticketsMapList = new ArrayList();
            ticketsMapList.add(requestTicketMap);

            Map smimeContentMap = new HashMap();
            smimeContentMap.put("totalAmount", requestAmount.toString());
            smimeContentMap.put("currency", currencyVS.toString());
            smimeContentMap.put("tickets", ticketsMapList);
            smimeContentMap.put("UUID", UUID.randomUUID().toString());
            smimeContentMap.put("serverURL", contextVS.getTicketServer().getServerURL());
            smimeContentMap.put("operation", TypeVS.TICKET_REQUEST.toString());
            JSONObject requestJSON = new JSONObject(smimeContentMap);

            String requestDataFileName = ContextVS.TICKET_REQUEST_DATA_FILE_NAME + ":" +
                    ContentTypeVS.JSON_SIGNED_AND_ENCRYPTED.getName();

            List<Map> ticketCSRList = new ArrayList<Map>();
            for(TicketVS ticket : ticketList) {
                Map csrTicketMap = new HashMap();
                csrTicketMap.put("currency", currencyVS.toString());
                csrTicketMap.put("ticketValue", ticketsValue);
                csrTicketMap.put("csr", new String(ticket.getCertificationRequest().getCsrPEM(),"UTF-8"));
                ticketCSRList.add(csrTicketMap);
            }
            Map csrRequestMap = new HashMap();
            csrRequestMap.put("ticketCSR", ticketCSRList);
            JSONObject csrRequestJSON = new JSONObject(csrRequestMap);

            byte[] encryptedCSRBytes = Encryptor.encryptMessage(csrRequestJSON.toString().getBytes(),
                    ticketServer.getCertificate());
            String csrFileName = ContextVS.CSR_FILE_NAME + ":" + ContentTypeVS.ENCRYPTED.getName();
            Map<String, Object> mapToSend = new HashMap<String, Object>();
            mapToSend.put(csrFileName, encryptedCSRBytes);

            KeyStore.PrivateKeyEntry keyEntry = contextVS.getUserPrivateKey();
            SignedMapSender signedMapSender = new SignedMapSender(fromUser,
                    ticketServer.getNameNormalized(),
                    requestJSON.toString(), mapToSend, messageSubject, null,
                    ticketServer.getTicketRequestServiceURL(),
                    requestDataFileName, ContentTypeVS.JSON_SIGNED_AND_ENCRYPTED,
                    ticketServer.getCertificate(),
                    keyEntry.getCertificate().getPublicKey(), keyEntry.getPrivateKey(),
                    (AppContextVS)getApplicationContext());
            responseVS = signedMapSender.call();
            if(ResponseVS.SC_OK == responseVS.getStatusCode()) {
                JSONObject issuedTicketsJSON = new JSONObject(new String(
                        responseVS.getMessageBytes(), "UTF-8"));

                JSONArray transactionsArray = issuedTicketsJSON.getJSONArray("transactionList");
                for(int i = 0; i < transactionsArray.length(); i++) {
                    TransactionVS transaction = TransactionVS.parse(transactionsArray.getJSONObject(i));
                    TransactionVSContentProvider.addTransaction(contextVS, transaction, null);
                }

                JSONArray issuedTicketsArray = issuedTicketsJSON.getJSONArray("issuedTickets");
                Log.d(TAG + "ticketRequest(...)", "Num IssuedTickets: " + issuedTicketsArray.length());
                if(issuedTicketsArray.length() != ticketList.size()) {
                    Log.e(TAG + "ticketRequest(...)", "ERROR - Num tickets requested: " +
                            ticketList.size() + " - num. tickets received: " +
                            issuedTicketsArray.length());
                }
                for(int i = 0; i < issuedTicketsArray.length(); i++) {
                    Collection<X509Certificate> certificates = CertUtil.fromPEMToX509CertCollection(
                            issuedTicketsArray.getString(i).getBytes());
                    if(certificates.isEmpty()) throw new Exception (" --- missing certs --- ");
                    X509Certificate x509Certificate = certificates.iterator().next();
                    byte[] ticketExtensionValue = x509Certificate.getExtensionValue(ContextVS.TICKET_OID);
                    DERTaggedObject ticketCertDataDER = (DERTaggedObject)
                            X509ExtensionUtil.fromExtensionValue(ticketExtensionValue);
                    JSONObject ticketCertData = new JSONObject(((DERUTF8String)
                            ticketCertDataDER.getObject()).toString());
                    String hashCertVS = ticketCertData.getString("hashCertVS");
                    TicketVS ticket = ticketsMap.get(hashCertVS);
                    ticket.setState(TicketVS.State.OK);
                    ticket.getCertificationRequest().initSigner(issuedTicketsArray.getString(i).getBytes());
                }
                TicketContentProvider.insertTickets(contextVS, ticketsMap.values());
                caption = getString(R.string.ticket_request_ok_caption);
                message = getString(R.string.ticket_request_ok_msg, requestAmount.toString(),
                        currencyVS.toString());
                iconId = R.drawable.euro_24;
            } else {
                caption = getString(R.string.ticket_request_error_caption);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            message = ex.getMessage();
            if(message == null || message.isEmpty()) message = getString(R.string.exception_lbl);
            responseVS = ResponseVS.getExceptionResponse(getString(R.string.exception_lbl),
                    message);
        } finally {
            responseVS.setIconId(iconId);
            responseVS.setCaption(caption);
            responseVS.setNotificationMessage(message);
            return responseVS;
        }
    }

    private ResponseVS updateUserInfo() {
        ResponseVS responseVS = getTicketServer();
        if(ResponseVS.SC_OK != responseVS.getStatusCode()) return responseVS;
        TicketServer ticketServer = (TicketServer) responseVS.getData();
        Map mapToSend = new HashMap();
        mapToSend.put("NIF", contextVS.getUserVS().getNif());
        mapToSend.put("operation", TypeVS.TICKET_USER_INFO.toString());
        mapToSend.put("UUID", UUID.randomUUID().toString());
        String msgSubject = getString(R.string.ticket_user_info_request_msg_subject);
        try {
            JSONObject userInfoRequestJSON = new JSONObject(mapToSend);
            SMIMESignedSender smimeSignedSender = new SMIMESignedSender(contextVS.getUserVS().getNif(),
                    ticketServer.getNameNormalized(), ticketServer.getUserInfoServiceURL(),
                    userInfoRequestJSON.toString(), ContentTypeVS.JSON_SIGNED_AND_ENCRYPTED,
                    msgSubject, ticketServer.getCertificate(), contextVS);
            responseVS = smimeSignedSender.call();

            Calendar currentLapseCalendar = DateUtils.getMonday(Calendar.getInstance());
            String currentLapseStr = DateUtils.getDirPath(currentLapseCalendar.getTime());

            if(ResponseVS.SC_OK == responseVS.getStatusCode()) {
                String responseStr = responseVS.getMessage();

                JSONObject responseJSON = new JSONObject(responseStr);
                Date requestDate = DateUtils.getDateFromString(responseJSON.getString("date"));

                Iterator weeksIterator = responseJSON.keys();
                TicketAccount ticketAccount = null;
                while(weeksIterator.hasNext()) {
                    String keyStr = (String) weeksIterator.next();
                    if(currentLapseStr.equals(keyStr)) {
                        ticketAccount = TicketAccount.parse(responseJSON.getJSONObject(keyStr));
                        ticketAccount.setWeekLapse(currentLapseCalendar.getTime());
                        break;
                    }
                }
                if(ticketAccount != null) {
                    ticketAccount.setLastRequestDate(requestDate);
                } else Log.d(TAG + "updateUserInfo(...)", "Current week data not found");
                TransactionVSContentProvider.setTicketAccount(contextVS, ticketAccount);
            } else {
                responseVS.setCaption(getString(R.string.error_lbl));
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            String message = ex.getMessage();
            if(message == null || message.isEmpty()) message = getString(R.string.exception_lbl);
            responseVS = ResponseVS.getExceptionResponse(getString(R.string.exception_lbl),
                    message);
        } finally {
            return responseVS;
        }
    }

    private ResponseVS getTicketServer() {
        ResponseVS responseVS = null;
        TicketServer ticketServer = contextVS.getTicketServer();
        if(ticketServer != null) {
            responseVS = new ResponseVS(ResponseVS.SC_OK);
        } else {
            try {
                responseVS = HttpHelper.getData(ActorVS.getServerInfoURL(contextVS.getTicketServerURL()),
                        ContentTypeVS.JSON);
                if (ResponseVS.SC_OK == responseVS.getStatusCode()) {
                    ticketServer = (TicketServer) ActorVS.parse(new JSONObject(responseVS.getMessage()));
                    contextVS.setTicketServer(ticketServer);
                }
            } catch(Exception ex) {
                ex.printStackTrace();
                String message = ex.getMessage();
                if(message == null || message.isEmpty()) message = getString(R.string.exception_lbl);
                responseVS = ResponseVS.getExceptionResponse(getString(R.string.exception_lbl),
                        message);
            }
        }
        responseVS.setData(ticketServer);
        return responseVS;
    }

}