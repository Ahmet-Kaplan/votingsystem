package vickets

import org.votingsystem.util.DateUtils
import org.votingsystem.vicket.model.Vicket

class ChangeWeekLapseJob {
    static triggers = {
        //http://quartz-scheduler.org/documentation/quartz-1.x/tutorials/crontrigger
        //0 15 10 ? * MON	Fire at 10:15am every Monday
        cron name:'cronTrigger', startDelay:10000, cronExpression: '0 0 0 ? * MON' //00:00am every Monday
        //simple repeatInterval: 10000l // execute job once in 10 seconds
    }

    def BalanceService

    def execute() {
        checkCancelledVickets();
        BalanceService.initWeekPeriod(Calendar.getInstance())
    }


    private checkCancelledVickets() {
        Calendar calendar = Calendar.getInstance()
        Vicket.withTransaction {
            log.debug("checkCancelledVickets ${calendar.getTime()}")
            def criteriaVicket = Vicket.createCriteria().scroll {
                le("validTo", calendar.getTime())
                eq("state", Vicket.State.OK)
            }
            while (criteriaVicket.next()) {
                Vicket vicket = (Vicket) criteriaVicket.get(0);
                vicket.state = Vicket.State.LAPSED
                vicket.save();
                log.debug("checkCancelledVickets - LAPSED ticked ${vicket.id}")
            }
        }
    }

}
