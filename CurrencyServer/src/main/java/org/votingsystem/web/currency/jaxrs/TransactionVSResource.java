package org.votingsystem.web.currency.jaxrs;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.votingsystem.dto.ResultListDto;
import org.votingsystem.dto.currency.TransactionVSDto;
import org.votingsystem.model.MessageSMIME;
import org.votingsystem.model.UserVS;
import org.votingsystem.model.currency.CurrencyBatch;
import org.votingsystem.model.currency.TransactionVS;
import org.votingsystem.util.DateUtils;
import org.votingsystem.util.JSON;
import org.votingsystem.util.TimePeriod;
import org.votingsystem.web.cdi.ConfigVS;
import org.votingsystem.web.currency.ejb.CurrencyBean;
import org.votingsystem.web.currency.ejb.TransactionVSBean;
import org.votingsystem.web.currency.ejb.UserVSBean;
import org.votingsystem.web.ejb.DAOBean;
import org.votingsystem.web.ejb.SignatureBean;

import javax.inject.Inject;
import javax.persistence.Query;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * License: https://github.com/votingsystem/votingsystem/wiki/Licencia
 */
@Path("/transactionVS")
public class TransactionVSResource {

    private static final Logger log = Logger.getLogger(TransactionVSResource.class.getSimpleName());

    @Inject UserVSBean serVSBean;
    @Inject TransactionVSBean transactionVSBean;
    @Inject SignatureBean signatureBean;
    @Inject CurrencyBean currencyBean;
    @Inject DAOBean dao;
    @Inject ConfigVS config;

    @Path("/id/{id}")
    @GET @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("id") long id) throws UnsupportedEncodingException, JsonProcessingException {
        TransactionVS transactionVS = dao.find(TransactionVS.class, id);
        if(transactionVS != null) {
            TransactionVSDto transactionDto = transactionVSBean.getTransactionDto(transactionVS);
            transactionDto.setReceipt(new String(transactionVS.getMessageSMIME().getContent(), "UTF-8"));
            return Response.ok().entity(JSON.getMapper().writeValueAsBytes(transactionDto)).build();
        } else return Response.status(Response.Status.NOT_FOUND).entity("ERROR - TransactionVS not found - id: " + id).build();
    }

    @Path("/")
    @GET @Produces(MediaType.APPLICATION_JSON)
    public Response index(@DefaultValue("0") @QueryParam("offset") int offset,
              @DefaultValue("100") @QueryParam("max") int max, @Context ServletContext context,
              @Context HttpServletRequest req, @Context HttpServletResponse resp) throws IOException, ServletException {
        String contentType = req.getContentType() != null ? req.getContentType():"";
        Query query = dao.getEM().createQuery("select t from TransactionVS t where t.transactionParent is null")
                .setMaxResults(max).setFirstResult(offset);
        List<TransactionVS> transactionList = query.getResultList();
        query = dao.getEM().createQuery("select COUNT(t) from TransactionVS t where t.transactionParent is null");
        long totalCount = (long) query.getSingleResult();
        List<TransactionVSDto> resultList = new ArrayList<>();
        for(TransactionVS transactionVS : transactionList) {
            resultList.add(transactionVSBean.getTransactionDto(transactionVS));
        }
        ResultListDto resultListDto = new ResultListDto(resultList, offset, max, totalCount);
        if(contentType.contains("json")) return Response.ok().entity(
                JSON.getMapper().writeValueAsBytes(resultListDto)).build();
        else {
            req.setAttribute("transactionsMap", JSON.getMapper().writeValueAsString(resultListDto));
            context.getRequestDispatcher("/transactionVS/index.xhtml").forward(req, resp);
            return Response.ok().build();
        }
    }

    @Path("/") @POST @Produces(MediaType.APPLICATION_JSON)
    public Response post(MessageSMIME messageSMIME, @Context HttpServletRequest req) throws Exception {
        ResultListDto dto = transactionVSBean.processTransactionVS(messageSMIME);
        return Response.ok().entity(JSON.getMapper().writeValueAsBytes(dto)).build();
    }

    @Path("/from/{dateFrom}/to/{dateTo}")
    @GET @Produces(MediaType.APPLICATION_JSON)
    public Response search(@DefaultValue("0") @QueryParam("offset") int offset,
                       @DefaultValue("100") @QueryParam("max") int max,
                       @QueryParam("transactionvsType") String transactionvsType,
                       @QueryParam("searchText") String searchText,
                       @PathParam("dateFrom") String dateFromStr, @PathParam("dateTo") String dateToStr,
                       @Context ServletContext context, @Context HttpServletRequest req,
                       @Context HttpServletResponse resp) throws IOException, ParseException, ServletException {
        String contentType = req.getContentType() != null ? req.getContentType():"";
        TransactionVS.Type transactionType = null;
        BigDecimal amount = null;
        Date dateFrom = DateUtils.getURLDate(dateFromStr);
        Date dateTo = DateUtils.getURLDate(dateToStr);
        try {
            if(transactionvsType != null) transactionType = TransactionVS.Type.valueOf(transactionvsType);
            else transactionType = TransactionVS.Type.valueOf(searchText);} catch(Exception ex) {}
        try {amount = new BigDecimal(searchText);} catch(Exception ex) {}
        String queryListPrefix = "select t from TransactionVS t ";
        String queryCountPrefix = "select COUNT(t) from TransactionVS t ";
        String querySufix = "where t.transactionParent is null " +
                "and t.dateCreated between :dateFrom and :dateTo and (t.type =:type or t.amount =:amount " +
                "or t.subject like :searchText or t.currencyCode like :searchText)";
        Query query = dao.getEM().createQuery(queryListPrefix + querySufix).setParameter("dateFrom", dateFrom)
                .setParameter("dateTo", dateTo).setParameter("type", transactionType).setParameter("amount", amount)
                .setParameter("searchText", searchText).setFirstResult(offset).setMaxResults(max);
        List<TransactionVS> transactionList = query.getResultList();
        query = dao.getEM().createQuery(queryCountPrefix + querySufix).setParameter("dateFrom", dateFrom)
                .setParameter("dateTo", dateTo).setParameter("type", transactionType).setParameter("amount", amount)
                .setParameter("searchText", searchText);
        long totalCount = (long) query.getSingleResult();
        List<TransactionVSDto> resultList = new ArrayList<>();
        for(TransactionVS transactionVS :  transactionList) {
            resultList.add(transactionVSBean.getTransactionDto(transactionVS));
        }
        ResultListDto resultListDto = new ResultListDto(resultList, offset, max, totalCount);
        if(contentType.contains("json")) return Response.ok().entity(
                JSON.getMapper().writeValueAsBytes(resultListDto)).build();
        else {
            req.setAttribute("transactionsMap", JSON.getMapper().writeValueAsString(resultListDto));
            context.getRequestDispatcher("/transactionVS/index.xhtml").forward(req, resp);
            return Response.ok().build();
        }
    }

    @Path("/userVS/id/{userId}/{timePeriod}") //old_url -> /userVS/$id/transacionVS/$timePeriod
    @GET @Produces(MediaType.APPLICATION_JSON)
    public Object userVS(@PathParam("userId") long userId, @PathParam("timePeriod") String lapseStr,
                         @Context ServletContext context, @Context HttpServletRequest req,
                         @Context HttpServletResponse resp) throws Exception {
        UserVS userVS = dao.find(UserVS.class, userId);
        if(userVS == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("not found - userId: " + userId).build();
        }
        TimePeriod.Lapse lapse =  TimePeriod.Lapse.valueOf(lapseStr.toUpperCase());
        TimePeriod timePeriod = DateUtils.getLapsePeriod(Calendar.getInstance(req.getLocale()).getTime(), lapse);
        return transactionVSBean.getBalancesDto(userVS, timePeriod);
    }

    @Path("/currency")
    @POST @Produces(MediaType.APPLICATION_JSON) @Consumes({"application/json"})
    public Object currency(CurrencyBatch currencyBatch) throws Exception {
        return currencyBean.processCurrencyTransaction(currencyBatch);
    }
}
