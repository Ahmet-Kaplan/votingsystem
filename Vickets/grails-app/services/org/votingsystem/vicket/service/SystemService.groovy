package org.votingsystem.vicket.service

import grails.transaction.Transactional
import org.votingsystem.model.UserVS
import org.votingsystem.vicket.model.UserVSAccount
import org.votingsystem.model.VicketTagVS
import org.votingsystem.vicket.util.IbanVSUtil

@Transactional
class SystemService {

    private static final CLASS_NAME = SystemService.class.getSimpleName()

    private UserVS systemUser
    private VicketTagVS wildTag
    private Locale defaultLocale
    def grailsApplication

    public synchronized Map init() throws Exception {
        log.debug("init")
        systemUser = UserVS.findWhere(type:UserVS.Type.SYSTEM)
        if(!systemUser) {
            systemUser = new UserVS(nif:grailsApplication.config.VotingSystem.systemNIF, type:UserVS.Type.SYSTEM,
                    name:grailsApplication.config.VotingSystem.serverName).save()
            systemUser.setIBAN(IbanVSUtil.getInstance().getIBAN(systemUser.id))
            systemUser.save()
            wildTag = VicketTagVS(name:VicketTagVS.WILDTAG).save()
            new UserVSAccount(currencyCode: Currency.getInstance('EUR').getCurrencyCode(), userVS:systemUser, balance:BigDecimal.ZERO,
                    IBAN:systemUser.getIBAN(), tag:wildTag).save()

        }
        return [systemUser:systemUser]
    }

    public void updateTagBalance(BigDecimal amount, String currencyCode, VicketTagVS tag) {
        UserVSAccount tagAccount = UserVSAccount.findWhere(userVS:getSystemUser(), tag:tag, currencyCode:currencyCode,
                state: UserVSAccount.State.ACTIVE)
        if(!tagAccount) throw new Exception("THERE'S NOT ACTIVE SYSTEM ACCOUNT FOR TAG '${tag.name}' and currency '${currencyCode}'")
        tagAccount.balance = tagAccount.balance.add(amount)
        tagAccount.save()
    }

    public Locale getDefaultLocale() {
        if(defaultLocale == null) defaultLocale = new Locale(grailsApplication.config.VotingSystem.defaultLocale)
        return defaultLocale
    }

    public UserVS getSystemUser() {
        if(!systemUser) systemUser = init().systemUser
        return systemUser;
    }

    public VicketTagVS getWildTag() {
        if(!wildTag) wildTag = VicketTagVS.findWhere(name:VicketTagVS.WILDTAG)
        return wildTag
    }
}
