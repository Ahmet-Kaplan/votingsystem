package org.votingsystem.web.currency.ejb;

import org.votingsystem.model.TagVS;
import org.votingsystem.model.UserVS;
import org.votingsystem.model.currency.Currency;
import org.votingsystem.model.currency.CurrencyAccount;
import org.votingsystem.util.ContextVS;
import org.votingsystem.util.FileUtils;
import org.votingsystem.web.cdi.ConfigVS;
import org.votingsystem.web.ejb.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.persistence.Query;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
@Startup
public class StartupBean implements StartupVS {

    private static Logger log = Logger.getLogger(StartupBean.class.getSimpleName());

    @Inject DAOBean dao;
    @Inject IBANBean ibanBean;
    @Inject ConfigVS config;
    @Inject TimeStampBean timeStampBean;
    @Inject SignatureBean signatureBean;
    @Inject SubscriptionVSBean subscriptionVSBean;
    @Inject BalancesBean balancesBean;

    @PostConstruct
    public void initialize() throws Exception {
        log.info("initialize");
        ContextVS.init();
        Query query = dao.getEM().createNamedQuery("findUserByType").setParameter("type", UserVS.Type.SYSTEM);
        UserVS systemUser = dao.getSingleResult(UserVS.class, query);
        if(systemUser == null) { //First time run
            systemUser = dao.persist(new UserVS(config.getSystemNIF(), UserVS.Type.SYSTEM, config.getServerName()));
            systemUser.setIBAN(ibanBean.getIBAN(systemUser.getId()));
            dao.merge(systemUser);
            TagVS wildTag = dao.persist(new TagVS(TagVS.WILDTAG));
            URL res = res = Thread.currentThread().getContextClassLoader().getResource("defaultTags.txt");
            String[] defaultTags = FileUtils.getStringFromInputStream(res.openStream()).split(",");
            for(String tag: defaultTags) {
                TagVS newTagVS =  dao.persist(new TagVS(tag));
                dao.persist(new CurrencyAccount(systemUser, BigDecimal.ZERO,
                        java.util.Currency.getInstance("EUR").getCurrencyCode(), newTagVS));
            }
        }
        timeStampBean.init();
        signatureBean.init();
    }

    //@Schedule(dayOfWeek = "Mon", hour="0")
    public void initWeekPeriod() throws IOException {
        checkCancelledCooins();
        balancesBean.initWeekPeriod(Calendar.getInstance());
    }

    private void checkCancelledCooins() {
        Date date = new Date();
        log.info("checkCancelledCooins - date: " + date);
        Query query = dao.getEM().createQuery("select c from Currency c where c.validTo <=:validTo and c.state =:state")
                .setParameter("validTo", date).setParameter("state", Currency.State.OK);
        List<Currency> currencyList = query.getResultList();
        for(Currency currency :currencyList) {
            dao.merge(currency.setState(Currency.State.LAPSED));
            log.log(Level.FINE, "LAPSED currency id: " + currency.getId() + " - value: " + currency.getAmount());
        }
    }

    @PreDestroy private void shutdown() { log.info(" --------- shutdown ---------");}

    @Override public void mainServletInitialized() throws Exception { }

}
