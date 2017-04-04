package com.baidu.beidou.indexgrade.olap.request;

import java.util.List;

import com.baidu.beidou.olap.service.req.AbstractOlapRequest;
import com.baidu.unbiz.olap.driver.bo.OlapRequest;
import com.baidu.unbiz.olap.obj.BaseItem;

/**
 * 用于输出到文件的Request
 * 
 * @author wangxiongjie
 *
 *
 */
public class OlapDataRequest extends AbstractOlapRequest {

    private TableMeta tableMeta;

    private String filePath;

    /**
     * @return the tableMeta
     */
    public TableMeta getTableMeta() {
        return tableMeta;
    }

    /**
     * @param tableMeta the tableMeta to set
     */
    public void setTableMeta(TableMeta tableMeta) {
        this.tableMeta = tableMeta;
    }

    /**
     * @return the filePath
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * @param filePath the filePath to set
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * 根据DataRequest构建olap Request
     * 
     * @param dr DataRequest
     * @return OlapRequeest
     */
    public static OlapRequest<?> buildOlapRequest(OlapDataRequest dr) {
        OlapRequest<?> rr = new OlapRequest<BaseItem>();
        List<Integer> userIds = dr.getUserIds();
        if (userIds != null) {
            Number[] userIdArray = new Number[userIds.size()];
            userIds.toArray(userIdArray);
            rr.setUserIds(userIdArray); // userid
        }
        rr.setFrom(dr.getFrom());
        rr.setTo(dr.getTo());
        rr.setUserIds(new Number[] {});
        // rr.setGroupByCols(dr.getTableMeta().getOlapKeys().split(","));
        rr.setBasicValueCols(dr.getTableMeta().getOlapKeys().split(","));
        rr.setTable(dr.getTableMeta().getOlapTable());
        rr.setTimeUnit(dr.getTimeUnit());
        return rr;
    }

    public enum TableMeta {

        USER(0, "DailyUserDateIndexStats", "UserId,Cost,Date"), KEYWORD(1, "DailyKeywordStats",
                "UserId,PlanId,GroupId,WordId");

        public static final String DEFAULT_VALUES = "Cost";

        private int id;
        private String olapTable;
        private String olapKeys;

        TableMeta(int id, String olapTable, String olapKeys) {
            this.olapTable = olapTable;
            this.id = id;
            this.olapKeys = olapKeys;
        }

        public static TableMeta val(int num) {
            for (TableMeta type : TableMeta.values()) {
                if (type.getId() == num) {
                    return type;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }

        public String getOlapTable() {
            return olapTable;
        }

        public String getOlapKeys() {
            return olapKeys;
        }
    }
}
