package com.baidu.beidou.olap.service.req;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.baidu.unbiz.olap.constant.SortOrder;
import com.baidu.unbiz.olap.obj.Pair;

/**
 * 并行抓取请求通用抽象类
 * 
 * @author wangchongjie
 * @fileName AbstractOlapRequest.java
 * @dateTime 2014-11-21 下午7:50:48
 */
public class AbstractOlapRequest {

    protected Date from;
    protected Date to;
    protected int timeUnit;
    protected List<Pair<String, SortOrder>> orderPairs;
    // 北斗四层级字段
    protected int userId;
    protected List<Integer> planIds;
    protected List<Integer> groupIds;
    protected List<Long> unitIds;
    // 多用户查询使用
    protected List<Integer> userIds;

    public List<Integer> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Integer> userIds) {
        this.userIds = userIds;
    }

    public List<Integer> getPlanIds() {
        return planIds;
    }

    public void setPlanIds(List<Integer> planIds) {
        this.planIds = planIds;
    }

    public List<Integer> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(List<Integer> groupIds) {
        this.groupIds = groupIds;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public int getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(int timeUnit) {
        this.timeUnit = timeUnit;
    }

    public List<Pair<String, SortOrder>> getOrderPairs() {
        return orderPairs;
    }

    public void setOrderPairs(List<Pair<String, SortOrder>> orderPairs) {
        this.orderPairs = orderPairs;
    }

    public void setOrderPairs(Pair<String, SortOrder>...orderPairs) {
        this.orderPairs = new ArrayList<Pair<String, SortOrder>>();
        this.orderPairs.addAll(Arrays.asList(orderPairs));
    }

    public List<Long> getUnitIds() {
        return unitIds;
    }

    public void setUnitIds(List<Long> unitIds) {
        this.unitIds = unitIds;
    }

    public Long getUnitId() {
        if (this.unitIds == null) {
            return null;
        }
        return this.unitIds.get(0);
    }

    public void setUnitId(Long unitId) {
        if (this.unitIds == null) {
            this.unitIds = new ArrayList<Long>();
        }
        if (unitId != null) {
            this.unitIds.add(unitId);
        }
    }
}
