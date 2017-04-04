/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.code.bo;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Created by hewei18 on 2016-03-29.
 */
public class Interest {
    private int interestId;
    private String name;
    private int parentId;
    private int orderId;
    private int type;
    private boolean selectable;

    public Interest() {
    }

    public Interest(int interestId, String name, int parentId, int orderId, int type) {
        this.interestId = interestId;
        this.name = name;
        this.parentId = parentId;
        this.orderId = orderId;
        this.type = type;
    }

    public int getInterestId() {
        return interestId;
    }

    public void setInterestId(int interestId) {
        this.interestId = interestId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Interest interest = (Interest) o;

        if (interestId != interest.interestId) {
            return false;
        }
        if (parentId != interest.parentId) {
            return false;
        }
        if (orderId != interest.orderId) {
            return false;
        }
        if (type != interest.type) {
            return false;
        }
        return !(name != null ? !name.equals(interest.name) : interest.name != null);

    }

    @Override
    public int hashCode() {
        int result = interestId;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + parentId;
        result = 31 * result + orderId;
        result = 31 * result + type;
        return result;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("interestId", interestId)
                .append("name", name)
                .append("parentId", parentId)
                .append("orderId", orderId)
                .append("type", type)
                .toString();
    }
}
