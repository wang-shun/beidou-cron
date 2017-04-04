package com.baidu.beidou.cprounit.service.syncubmc;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cprounit.service.UnitBeanUtils;
import com.baidu.beidou.cprounit.service.syncubmc.vo.AdmakerUnitMaterCheckSet;
import com.baidu.beidou.cprounit.service.syncubmc.vo.LogCheckPrinter;
import com.baidu.beidou.cprounit.service.syncubmc.vo.UnitMaterCheckView;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestLite;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseImageUnit;

/**
 * ClassName: CheckAndFixAdmakerMaterMgr Function: 校验beidou.cprounitmater[0-7]中所有物料，查看ubmc是否存在该物料
 * 
 * @author doreen
 * @version cpweb-567
 * @date May 13, 2013
 */
public class CheckAndFixAdmakerMaterMgr extends SyncBaseMgr {

    private static final Log log = LogFactory.getLog(CheckAndFixAdmakerMaterMgr.class);

    private LogCheckPrinter logCheckPrinter = null;
    private int maxMaterNumSelect = 0;
    private int dbIndex = 0;
    private int dbSlice = 0;
    private AdmakerUnitMaterCheckSet admakerUnitMaterSet = null;

    public void checkAdmakerMater(int maxMaterNumSelect, int maxThread, PrintWriter errorWriter, PrintWriter logWriter,
            PrintWriter invalidWriter, String dbFileName, int dbIndex, int dbSlice) {

        log.info("begin to check the material of db and ubmc, in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
        logCheckPrinter = new LogCheckPrinter(errorWriter, logWriter, invalidWriter);
        this.maxMaterNumSelect = maxMaterNumSelect;
        this.dbIndex = dbIndex;
        this.dbSlice = dbSlice;

        admakerUnitMaterSet = new AdmakerUnitMaterCheckSet(dbFileName);

        // 多线程处理
        log.info("begin to create " + maxThread + " threads to work");
        ExecutorService pool = Executors.newFixedThreadPool(maxThread);
        long time1 = System.currentTimeMillis();
        for (int times = 0; times < maxThread; times++) {
            Runnable worker = this.createCheckTask();
            pool.execute(worker);
        }

        pool.shutdown();
        try {
            pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("An error has occurred while the pool is executing...");
        } catch (Exception e) {
            log.error("An error has occurred while the pool is executing...");
        }
        long time2 = System.currentTimeMillis();
        log.info("check the material of db and ubmc, in dbIndex=" + dbIndex + ", dbSlice=" + this.dbSlice + ", use "
                + (time2 - time1) + " ms, unitTotalNum=" + admakerUnitMaterSet.getTotal());

        admakerUnitMaterSet.closeFile();
        log.info("end to check the material of db and ubmc, in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
    }

    protected Runnable createCheckTask() {
        return new Runnable() {
            public void run() {
                while (true) {
                    try {
                        long time1 = System.currentTimeMillis();
                        List<UnitMaterCheckView> unitList = admakerUnitMaterSet.getNextList(maxMaterNumSelect);

                        if (CollectionUtils.isEmpty(unitList)) {
                            logCheckPrinter.log("[INFO][index=" + dbIndex + "], [unitList.size=0], [checkNum=0]");
                            break;
                        }

                        List<UnitMaterCheckView> units = new LinkedList<UnitMaterCheckView>();
                        List<RequestBaseMaterial> requests = new LinkedList<RequestBaseMaterial>();

                        for (UnitMaterCheckView unit : unitList) {
                            RequestBaseMaterial request = null;

                            Long mcId = unit.getMcId();
                            Integer mcVersionId = unit.getMcVersionId();

                            if (mcId != null && mcId > 0) {
                                request = new RequestLite(mcId, mcVersionId);
                                requests.add(request);
                                units.add(unit);
                            } else {
                                log.error("mcId is null[mater_id=" + unit.getId() + ", userId=" + unit.getUserId()
                                        + ", mcId=" + unit.getMcId() + ", versionId=" + unit.getMcVersionId() + "]");

                                logCheckPrinter.error(unit.getId() + "\t" + unit.getUserId() + "\t" + unit.getMcId()
                                        + "\t" + unit.getMcVersionId() + "\t" + 1);
                                continue;
                            }
                        }
                        logCheckPrinter.log("[INFO][units.size=" + units.size() + "], [requests.size="
                                + requests.size() + "]");

                        if (CollectionUtils.isNotEmpty(requests)) {
                            List<ResponseBaseMaterial> result = ubmcService.get(requests, true);

                            if (CollectionUtils.isEmpty(result) || result.size() != units.size()) {
                                log.error("resultInsert size[" + result.size() + "] != request size[" + units.size()
                                        + "]");
                            } else {
                                for (int index = 0; index < units.size(); index++) {
                                    UnitMaterCheckView unit = units.get(index);
                                    ResponseBaseMaterial response = result.get(index);

                                    if (response == null) {
                                        log.error("ubmc get unit failed[id=" + unit.getId() + ", userId="
                                                + unit.getUserId() + ", mcId=" + unit.getMcId() + ", versionId="
                                                + unit.getMcVersionId() + "]");

                                        logCheckPrinter.error(unit.getId() + "\t" + unit.getUserId() + "\t"
                                                + unit.getMcId() + "\t" + unit.getMcVersionId() + "\t" + 2);
                                        continue;
                                    }

                                    if (!(response instanceof ResponseImageUnit)) {
                                        log.error("unit from ubmc is not  image or flash " + unit.getId() + ", userId="
                                                + unit.getUserId() + ", mcId=" + unit.getMcId() + ", versionId="
                                                + unit.getMcVersionId() + "]");

                                        logCheckPrinter.error(unit.getId() + "\t" + unit.getUserId() + "\t"
                                                + unit.getMcId() + "\t" + unit.getMcVersionId() + "\t" + 3);
                                    } else {

                                        int compareCode =
                                                UnitBeanUtils.compareAdmakerMaterialFromDbToUbmc(unit, response);
                                        if (compareCode == 0) {
                                            continue;
                                        } else {
                                            compareCode = compareCode + 3;
                                            logCheckPrinter.invalid(unit.getId() + "\t" + unit.getUserId() + "\t"
                                                    + unit.getMcId() + "\t" + unit.getMcVersionId() + "\t"
                                                    + unit.getWuliaoType() + "\t" + unit.getState() + "\t"
                                                    + compareCode);
                                        }
                                    }
                                }
                            }
                        }

                        logCheckPrinter.log("[INFO][index=" + dbIndex + "], [unitList.size=" + unitList.size() + "]");

                        long time2 = System.currentTimeMillis();
                        log.info("[TASK]check the admaker material of db and ubmc, in dbIndex=" + dbIndex
                                + ", dbSlice=" + dbSlice + ", use " + (time2 - time1) + " ms, unitNum="
                                + unitList.size());
                    } catch (Exception e) {
                        log.error("check the admaker material of db and ubmc failed dbIndex=" + dbIndex + ", dbSlice="
                                + dbSlice, e);
                    }
                }
            }
        };
    }

}
