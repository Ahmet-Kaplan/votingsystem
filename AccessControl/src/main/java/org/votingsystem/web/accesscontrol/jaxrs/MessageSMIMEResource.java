package org.votingsystem.web.accesscontrol.jaxrs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.votingsystem.model.MessageSMIME;
import org.votingsystem.signature.smime.SMIMEMessage;
import org.votingsystem.util.ContentTypeVS;
import org.votingsystem.util.DateUtils;
import org.votingsystem.util.TypeVS;
import org.votingsystem.web.ejb.DAOBean;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * License: https://github.com/votingsystem/votingsystem/wiki/Licencia
 */
@Path("/messageSMIME")
public class MessageSMIMEResource {

    private static final Logger log = Logger.getLogger(MessageSMIMEResource.class.getSimpleName());

    @Inject DAOBean dao;

    @Path("/id/{id}") @GET
    public Object index(@PathParam("id") long id, @Context ServletContext context,
                                @Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
        String contentType = req.getContentType() != null ? req.getContentType():"";
        MessageSMIME messageSMIME = dao.find(MessageSMIME.class, id);
        if(messageSMIME == null) return Response.status(Response.Status.NOT_FOUND).entity(
                "MessageSMIME not found - id: " + id).build();
        if(contentType.contains(ContentTypeVS.TEXT.getName())) {
            return Response.ok().entity(messageSMIME.getContent()).type(ContentTypeVS.TEXT_STREAM.getName()).build();
        } else return processRequest(messageSMIME, context, req, resp);
    }


    private Object processRequest(MessageSMIME messageSMIME, @Context ServletContext context,
                                  @Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
        String contentType = req.getContentType() != null ? req.getContentType():"";
        String smimeMessageStr = Base64.getEncoder().encodeToString(messageSMIME.getContent());
        SMIMEMessage smimeMessage = messageSMIME.getSMIME();
        Date timeStampDate = null;
        Map signedContentMap;
        String viewer = "message-smime";
        if(smimeMessage.getTimeStampToken() != null) {
            timeStampDate = smimeMessage.getTimeStampToken().getTimeStampInfo().getGenTime();
        }
        signedContentMap = messageSMIME.getSignedContentMap();
        TypeVS operation = TypeVS.valueOf((String) signedContentMap.get("operation"));
        switch(operation) {
            case SEND_SMIME_VOTE:
                viewer = "message-smime-votevs";
                break;
            case CANCEL_VOTE:
                viewer = "message-smime-votevs-canceler";
                break;
            case ANONYMOUS_REPRESENTATIVE_REQUEST:
                viewer = "message-smime-representative-anonymousdelegation-request";
                break;
            case ACCESS_REQUEST:
                viewer = "message-smime-access-request";
                break;
        }
        Map model = new HashMap<>();
        model.put("operation", operation);
        model.put("smimeMessage", smimeMessageStr);
        model.put("viewer", viewer);
        model.put("signedContentMap", signedContentMap);
        model.put("timeStampDate", timeStampDate);
        if(contentType.contains("json")) {
            Map resultMap = new HashMap<>();
            resultMap.put("operation", operation);
            resultMap.put("smimeMessage", smimeMessageStr);
            resultMap.put("signedContentMap", signedContentMap);
            resultMap.put("timeStampDate", DateUtils.getISODateStr(timeStampDate));
            resultMap.put("viewer", viewer + ".jsp");
            return Response.ok().entity(new ObjectMapper().writeValueAsBytes(resultMap)).type(ContentTypeVS.JSON.getName()).build();
        } else {
            req.setAttribute("operation", operation);
            req.setAttribute("smimeMessage", smimeMessageStr);
            req.setAttribute("signedContentMap", new ObjectMapper().writeValueAsString(signedContentMap));
            req.setAttribute("timeStampDate", DateUtils.getISODateStr(timeStampDate));
            req.setAttribute("viewer",  viewer + ".jsp");
            context.getRequestDispatcher("/jsf/messageSMIME/contentViewer.jsp").forward(req, resp);
            return Response.ok().build();
        }
    }



}
