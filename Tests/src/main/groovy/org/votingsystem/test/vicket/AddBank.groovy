package org.votingsystem.test.vicket

import net.sf.json.JSONSerializer
import org.apache.log4j.Logger
import org.votingsystem.model.ContentTypeVS
import org.votingsystem.model.ContextVS
import org.votingsystem.model.ResponseVS
import org.votingsystem.model.UserVS
import org.votingsystem.model.VicketServer
import org.votingsystem.signature.smime.SMIMEMessage
import org.votingsystem.test.util.SignatureVSService
import org.votingsystem.test.util.TestHelper
import org.votingsystem.util.DateUtils
import org.votingsystem.util.HttpHelper


Logger logger = TestHelper.init(VicketRequest.class)

Map requestDataMap = [info:"Voting System Test Bank - " + DateUtils.getDayWeekDateStr(Calendar.getInstance().getTime()),
        certChainPEM:new String(ContextVS.getInstance().getResourceBytes("./certs/Cert_BankVS_03455543T.pem"),"UTF-8"),
        IBAN:"ES1877777777450000000050", operation:"BANKVS_NEW", UUID:UUID.randomUUID().toString()]

VicketServer vicketServer = TestHelper.fetchVicketServer()
ContextVS.getInstance().setDefaultServer(vicketServer)
SignatureVSService superUserSignatureService = SignatureVSService.getUserVSSignatureVSService("./certs/Cert_UserVS_07553172H.jks")
UserVS fromUserVS = superUserSignatureService.getUserVS()

String messageSubject = "TEST_ADD_BANKVS";
SMIMEMessage smimeMessage = superUserSignatureService.getTimestampedSignedMimeMessage(fromUserVS.nif,
        vicketServer.getNameNormalized(), JSONSerializer.toJSON(requestDataMap).toString(), messageSubject)
ResponseVS responseVS = HttpHelper.getInstance().sendData(smimeMessage.getBytes(), ContentTypeVS.JSON_SIGNED,
        vicketServer.getNewBankServiceURL())
logger.debug("statusCode: " + responseVS.getStatusCode() + " - message: " + responseVS.getMessage())

System.exit(0)


