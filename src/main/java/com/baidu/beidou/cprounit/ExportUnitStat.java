package com.baidu.beidou.cprounit;

import java.util.Date;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;

import com.baidu.beidou.olap.constant.FileMeta;
import com.baidu.beidou.olap.service.OlapStatService;
import com.baidu.beidou.olap.service.req.DataRequest;
import com.baidu.unbiz.common.DateUtil;
import com.baidu.unbiz.common.logger.LoggerFactory;
import com.baidu.unbiz.olap.constant.OlapConstants;

/**
 * Export unit stat data from olap engine
 * 
 * @author zhangxichuan
 */
public class ExportUnitStat {

    private static final Logger LOG = LoggerFactory.getLogger(ExportUnitStat.class);

    private static DataRequest requestInit() {
        Date date = DateUtil.getCurrentDate();
        FileMeta fileMeta = FileMeta.UNIT;
        DataRequest req = new DataRequest();
        req.setFrom(date);
        req.setTo(date);
        req.setTimeUnit(OlapConstants.TU_NONE);
        req.setFileMeta(fileMeta);
        return req;
    }

    public static void main(String[] args) {
        LOG.info("ExportUnitStat.java started at " + DateUtil.getCurrentDatetime());
        ApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "applicationContext-olap.xml" });
        LOG.info("ApplicationContext launched.");
        OlapStatService service = context.getBean("olapStatServiceImpl", OlapStatService.class);
        DataRequest req = requestInit();
        String filePath = null;
        if (args.length > 0) {
            filePath = args[0];
        } else {
            FastDateFormat sd = FastDateFormat.getInstance("yyyyMMdd");
            filePath = req.getFileMeta().getOutputPath() + "_"
                            + sd.format(DateUtil.addDays(DateUtil.getCurrentDate(), -1));
        }
        service.fetchOlapData(req, filePath);
        LOG.info("ExportUnitStat.java finished at " + DateUtil.getCurrentDatetime());
    }
}
