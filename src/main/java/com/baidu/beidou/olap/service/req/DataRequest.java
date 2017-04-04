package com.baidu.beidou.olap.service.req;

import java.util.List;

import com.baidu.beidou.olap.constant.FileMeta;
import com.baidu.unbiz.olap.driver.bo.OlapRequest;
import com.baidu.unbiz.olap.obj.BaseItem;

/**
 * 用于输出到文件的Request
 * @author wangchongjie
 * modified by zhangxichuan
 *
 */
public class DataRequest extends AbstractOlapRequest {

    private FileMeta fileMeta;

    /**
     * getter for fileMeta
     * @return fileMeta
     */
    public FileMeta getFileMeta() {
        return fileMeta;
    }

    /**
     * setter for fileMeta
     * @param fileMeta 文件导出信息
     */
    public void setFileMeta(FileMeta fileMeta) {
        this.fileMeta = fileMeta;
    }

    /**
     * 根据DataRequest构建olap Request
     * @param dr DataRequest
     * @return OlapRequeest
     */
    public static OlapRequest<?> buildOlapRequest(DataRequest dr) {
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
        rr.setGroupByCols(dr.getFileMeta().getOlapKeys().split(","));
        rr.setBasicValueCols(FileMeta.DEFAULT_VALUES.split(","));
        rr.setTable(dr.getFileMeta().getOlapTable());
        rr.setTimeUnit(dr.getTimeUnit());
        return rr;
    }
}
