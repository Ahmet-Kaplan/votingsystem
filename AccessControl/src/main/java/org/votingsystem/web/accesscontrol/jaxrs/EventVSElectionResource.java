package org.votingsystem.web.accesscontrol.jaxrs;

import org.votingsystem.dto.MessageDto;
import org.votingsystem.dto.ResultListDto;
import org.votingsystem.dto.voting.EventVSDto;
import org.votingsystem.dto.voting.EventVSStatsDto;
import org.votingsystem.model.MessageSMIME;
import org.votingsystem.model.voting.EventVS;
import org.votingsystem.model.voting.EventVSElection;
import org.votingsystem.throwable.ValidationExceptionVS;
import org.votingsystem.util.JSON;
import org.votingsystem.util.MediaTypeVS;
import org.votingsystem.web.accesscontrol.ejb.EventVSBean;
import org.votingsystem.web.accesscontrol.ejb.EventVSElectionBean;
import org.votingsystem.web.ejb.DAOBean;
import org.votingsystem.web.util.ConfigVS;

import javax.inject.Inject;
import javax.persistence.Query;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static java.text.MessageFormat.format;

/**
 * License: https://github.com/votingsystem/votingsystem/wiki/Licencia
 */
@Path("/eventVSElection")
public class EventVSElectionResource {

    private static Logger log = Logger.getLogger(EventVSElectionResource.class.getSimpleName());

    @Inject DAOBean dao;
    @Inject EventVSBean eventVSBean;
    @Inject EventVSElectionBean eventVSElectionBean;
    @Inject ConfigVS config;

    @Transactional
    @Path("/id/{id}") @GET
    public Response getById (@PathParam("id") long id, @Context ServletContext context, @Context HttpServletRequest req,
                           @Context HttpServletResponse resp) throws ValidationExceptionVS, IOException, ServletException {
        String contentType = req.getContentType() != null ? req.getContentType():"";
        List<EventVS.State> inList = Arrays.asList(EventVS.State.ACTIVE, EventVS.State.PENDING, EventVS.State.CANCELED,
                EventVS.State.TERMINATED);
        Query query = dao.getEM().createQuery("select e from EventVSElection e where e.state in :inList and " +
                "e.id =:id").setParameter("inList", inList).setParameter("id", id);
        EventVSElection eventVS =  dao.getSingleResult(EventVSElection.class, query);
        if(eventVS == null) return Response.status(Response.Status.NOT_FOUND).entity("ERROR - EventVSElection not found - " +
                "eventId: " + id).build();
        eventVSBean.checkEventVSDates(eventVS);
        EventVSDto eventVSDto = new EventVSDto(eventVS, config.getServerName(), config.getContextURL());
        if(contentType.contains("json")) {
            return Response.ok().entity(JSON.getMapper().writeValueAsBytes(eventVSDto)).type(MediaTypeVS.JSON).build();
        } else {
            req.setAttribute("eventDto", JSON.getMapper().writeValueAsString(eventVSDto));
            context.getRequestDispatcher("/eventVSElection/eventVSElection.xhtml").forward(req, resp);
            return Response.ok().build();
        }
    }

    @Transactional
    @Path("/") @GET
    public Response index (@QueryParam("eventVSState") String eventVSStateReq,
                         @DefaultValue("0") @QueryParam("offset") int offset,
                         @DefaultValue("50") @QueryParam("max") int max, @Context ServletContext context,
                         @Context HttpServletRequest req, @Context HttpServletResponse resp) throws ValidationExceptionVS,
            IOException, ServletException {
        String contentType = req.getContentType() != null ? req.getContentType():"";
        List<EventVS.State> inList = Arrays.asList(EventVS.State.ACTIVE);
        if(eventVSStateReq != null) {
            try {
                EventVS.State eventVSState = EventVS.State.valueOf(eventVSStateReq);
                if(eventVSState == EventVS.State.TERMINATED) {
                    inList = Arrays.asList(EventVS.State.TERMINATED,EventVS.State.CANCELED);
                } else if(eventVSState != EventVS.State.DELETED_FROM_SYSTEM) inList = Arrays.asList(eventVSState);
            } catch(Exception ex) {}
        }
        Query query = dao.getEM().createQuery("select count(e) from EventVSElection e where e.state in :inList")
                .setParameter("inList", inList);
        Long totalCount = (Long) query.getSingleResult();
        query = dao.getEM().createQuery("select e from EventVSElection e where e.state in :inList order by e.dateBegin desc")
                .setParameter("inList", inList).setFirstResult(offset).setMaxResults(max);
        List<EventVSElection> resultList = query.getResultList();
        List<EventVSDto> eventVSListDto = new ArrayList<>();
        for(EventVSElection eventVSElection : resultList) {
            eventVSBean.checkEventVSDates(eventVSElection);
            eventVSListDto.add(new EventVSDto(eventVSElection, config.getServerName(), config.getContextURL()));
        }
        ResultListDto<EventVSDto> resultListDto = new ResultListDto<>(eventVSListDto, offset, max, totalCount);
        if(contentType.contains("json")){
            return Response.ok().entity(JSON.getMapper().writeValueAsBytes(resultListDto))
                    .type(MediaTypeVS.JSON).build();
        } else {
            req.setAttribute("resultListDto", JSON.getMapper().writeValueAsString(resultListDto));
            context.getRequestDispatcher("/eventVSElection/index.xhtml").forward(req, resp);
            return Response.ok().build();
        }
    }

    @Path("/") @POST
    public Response save(MessageSMIME messageSMIME, @Context ServletContext context,
                         @Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
        EventVSElection response = eventVSElectionBean.saveEvent(messageSMIME);
        resp.setHeader("eventURL", format("{0}/eventVSElection/id/{1}", config.getRestURL(), response.getId()));
        return Response.ok().entity(response.getPublishRequestSMIME().getContent()).type(MediaTypeVS.JSON_SIGNED).build();
    }

    @Path("/cancel") @POST
    public Response cancelled(MessageSMIME messageSMIME) throws Exception {
        MessageSMIME response = eventVSBean.cancelEvent(messageSMIME);
        return Response.ok().entity(JSON.getMapper().writeValueAsBytes(MessageDto.OK(null))).type(MediaTypeVS.JSON).build();
    }

    @Transactional
    @Path("/id/{id}/stats") @GET
    public Response stats(@PathParam("id") long id, @Context ServletContext context,
                          @Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
        String contentType = req.getContentType() != null ? req.getContentType():"";
        EventVSElection eventVS = dao.find(EventVSElection.class, id);
        if(eventVS == null) return Response.status(Response.Status.NOT_FOUND).entity(
                "ERROR - EventVSElection not found - eventId: " + id).build();
        EventVSStatsDto statsDto = eventVSElectionBean.getStats(eventVS);
        if(contentType.contains("json")) {
            return Response.ok().entity(JSON.getMapper().writeValueAsBytes(statsDto)).type(MediaTypeVS.JSON).build();
        } else {
            req.setAttribute("statsDto", JSON.getMapper().writeValueAsString(statsDto));
            context.getRequestDispatcher("/eventVSElection/stats.xhtml").forward(req, resp);
            return Response.ok().build();
        }
    }

    @Transactional
    @Path("/id/{id}/checkDates") @GET
    public Response checkDates(@PathParam("id") long id) throws Exception {
        EventVS eventVS = dao.find(EventVS.class, id);
        if(eventVS == null) return Response.status(Response.Status.NOT_FOUND).entity("ERROR - EventVSClaim not found - " +
                "eventId: " + id).build();
        eventVSBean.checkEventVSDates(eventVS);
        return Response.ok().build();
    }

    @Transactional
    @Path("/id/{id}/publishRequest") @GET
    public Response publishRequest(@PathParam("id") long id, @Context ServletContext context,
                                   @Context HttpServletRequest req, @Context HttpServletResponse resp) throws Exception {
        EventVSElection eventVS = dao.find(EventVSElection.class, id);
        if(eventVS == null) return Response.status(Response.Status.NOT_FOUND).entity("ERROR - EventVSElection not found - " +
                "eventId: " + id).build();
        return Response.ok().entity(eventVS.getPublishRequestSMIME().getContent()).type(MediaTypeVS.JSON_SIGNED).build();
    }

}
