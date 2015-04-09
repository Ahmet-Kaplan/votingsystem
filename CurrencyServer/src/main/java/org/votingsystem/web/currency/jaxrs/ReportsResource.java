package org.votingsystem.web.currency.jaxrs;

import javax.ws.rs.Path;

/**
 * License: https://github.com/votingsystem/votingsystem/wiki/Licencia
 */
@Path("/reports")
public class ReportsResource {
    /*
    "/reports/week/$year/$month/$day"

        def filesService

    private static Logger reportslog = Logger.getLogger("reportsLog");
    private static Logger transactionslog = Logger.getLogger("transactionsLog");

    //main web page
    def index() {
        String weekReportsBaseDir = "${config.vs.backupCopyPath}/weekReports"
        def dir = new File(weekReportsBaseDir)
        List<DateUtils.TimePeriod> periods = []
        if(dir.exists()) {
            DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            dir.eachFileRecurse (FileType.FILES) { file ->
                if("balances.json".equals(file.getName())) {
                    String[] period = file.getParentFile().getName().split("_")
                    DateUtils.TimePeriod timePeriod = new DateUtils.TimePeriod(formatter.parse(period[0]), formatter.parse(period[1]));
                    periods.add(timePeriod)
                }
            }
        }
        render(view:'index', model: [periods:periods])
        return false
    }

    def logs() {
        if(request.contentType?.contains("json")) {
            RollingFileAppender appender = reportslog.getAppender("CooinServerReports")//logs from ServerReports.log
            File reportsFile = new File(appender.file)
            def messageJSON = JSON.parse("{\"records\":[" + reportsFile.text + "]}")
            messageJSON.numTotalRecords = messageJSON.records.length()
            render messageJSON as JSON
            return false
        } else {
            render(view:'logs')
        }
    }

    def week() {
        Calendar calendar = RequestUtils.getCalendar(params)
        DateUtils.TimePeriod timePeriod = DateUtils.getWeekPeriod(calendar)
        ReportFiles reportFiles = new ReportFiles(timePeriod, config.vs.weekReportsPath", null)
        if(request.contentType?.contains("json")) {
            if(reportFiles.jsonFile.exists()) {
                render JSON.parse(reportFiles.jsonFile.text) as JSON
            } else {
                response.status = ResponseVS.SC_NOT_FOUND
                render [:] as JSON
            }
            return false
        } else {
            if(!reportFiles.jsonFile.exists()) {
                response.status = ResponseVS.SC_PRECONDITION_FAILED
                render(view:'/error412',  model: [message:message(code:'reportsForPeriodMissingMsg',
                        args:[timePeriod.toString()])])
            } else render(view:'week',  model: [reportsFile:reportFiles.reportsFile.text, timePeriod:timePeriod])
        }
    }

    def transactionvs() {
        if(request.contentType?.contains("json")) {
            RollingFileAppender appender = transactionslog.getAppender("CooinTransactionsReports")
            File reportsFile = new File(appender.file)
            //testfile.eachLine{ line ->}
            def messageJSON = JSON.parse("{\"transactionRecords\":[" + reportsFile.text + "]}")
            if(params.transactionvsType) {
                messageJSON.transactionRecords = messageJSON["transactionRecords"].findAll() { item ->
                    if(item.type.equals(params.transactionvsType)) { return item }
                }
            }
            messageJSON.offset = 0
            messageJSON.max = messageJSON.transactionRecords.size()
            messageJSON.totalCount = messageJSON.transactionRecords.size()
            render messageJSON as JSON
            return false
        } else {
            render(view:'transactionvs')
        }
    }
     */
}
