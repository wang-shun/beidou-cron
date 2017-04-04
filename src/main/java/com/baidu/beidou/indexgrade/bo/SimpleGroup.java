package com.baidu.beidou.indexgrade.bo;

public class SimpleGroup {

    private Integer groupId;
    private Integer userId;

    public SimpleGroup() {
    }

    public SimpleGroup(Integer groupId, Integer userId) {
        super();
        this.groupId = groupId;
        this.userId = userId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return groupId + "\t" + userId;
    }

    /**
     * @return the groupId
     */
    public Integer getGroupId() {
        return groupId;
    }

    /**
     * @param groupId the groupId to set
     */
    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    /**
     * @return the userId
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return (31 * this.userId + this.groupId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SimpleGroup)) {
            return false;
        }
        SimpleGroup g = (SimpleGroup) obj;
        return (this.groupId.equals(g.groupId) && this.userId.equals(g.userId));
    }

}
