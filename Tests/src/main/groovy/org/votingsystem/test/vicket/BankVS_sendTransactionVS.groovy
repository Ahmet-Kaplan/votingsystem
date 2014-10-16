package org.votingsystem.test.vicket

import net.sf.json.JSONSerializer
import org.apache.log4j.Logger
import org.votingsystem.model.*
import org.votingsystem.signature.smime.SMIMEMessage
import org.votingsystem.test.util.SignatureService
import org.votingsystem.test.util.TestUtils
import org.votingsystem.test.util.TransactionVSPlan
import org.votingsystem.test.util.TransactionVSUtils
import org.votingsystem.util.ExceptionVS
import org.votingsystem.util.HttpHelper
import org.votingsystem.vicket.model.TransactionVS

Logger log = TestUtils.init(BankVS_sendTransactionVS.class, [:])

VicketServer vicketServer = TestUtils.fetchVicketServer(ContextVS.getInstance().config.vicketServerURL)
ContextVS.getInstance().setDefaultServer(vicketServer)

TransactionVSPlan transactionVSPlan = new TransactionVSPlan(
        TestUtils.getFileFromResources("bankVS_transactionPlan.json"), vicketServer)

String messageSubject = "TEST_BANKVS_SEND_TRANSACTIONVS";
for(TransactionVS transactionVS : transactionVSPlan.getBankVSTransacionList()) {
    SignatureService signatureService = SignatureService.getUserVSSignatureService(
            transactionVS.fromUserVS.nif, UserVS.Type.BANKVS)
    SMIMEMessage smimeMessage = signatureService.getTimestampedSignedMimeMessage(transactionVS.fromUserVS.nif,
            vicketServer.getNameNormalized(), JSONSerializer.toJSON(
            TransactionVSUtils.getBankVSTransactionVS(transactionVS)).toString(), messageSubject)
    ResponseVS responseVS = HttpHelper.getInstance().sendData(smimeMessage.getBytes(), ContentTypeVS.JSON_SIGNED,
            vicketServer.getTransactionVSServiceURL())
    if(ResponseVS.SC_OK != responseVS.statusCode) throw new ExceptionVS(responseVS.getMessage())
}

TestUtils.finish();