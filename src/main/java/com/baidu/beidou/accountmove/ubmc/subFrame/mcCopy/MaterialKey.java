package com.baidu.beidou.accountmove.ubmc.subFrame.mcCopy;

public class MaterialKey {

    private long mcId;
    private int mcVersion;

    private int userId;
    private int planId;
    private int groupId;
    private int unitId;

    public long getMcId() {
        return mcId;
    }

    public void setMcId(long mcId) {
        this.mcId = mcId;
    }

    public int getMcVersion() {
        return mcVersion;
    }

    public void setMcVersion(int mcVersion) {
        this.mcVersion = mcVersion;
    }

    public MaterialKey(long mcId, int mcVersion) {
        this.mcId = mcId;
        this.mcVersion = mcVersion;
    }

    public MaterialKey(long mcId, int mcVersion, int userId, int planId, int groupId, int unitId) {
        super();
        this.mcId = mcId;
        this.mcVersion = mcVersion;
        this.userId = userId;
        this.planId = planId;
        this.groupId = groupId;
        this.unitId = unitId;
    }

    public MaterialKey(MaterialKey oldKey, long newMcId, int newMcVersion) {
        this.mcId = newMcId;
        this.mcVersion = newMcVersion;
        this.userId = oldKey.getUserId();
        this.planId = oldKey.getPlanId();
        this.groupId = oldKey.getGroupId();
        this.unitId = oldKey.getUnitId();
    }

    @Override
    public int hashCode() {
        return Long.valueOf(mcId).hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null)
            return false;

        if (!(obj instanceof MaterialKey)) {
            return false;
        }

        MaterialKey key = (MaterialKey) obj;
        return key.mcId == this.mcId && key.mcVersion == this.mcVersion;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[userId=").append(userId)
                .append(", unitId=").append(unitId)
                .append(", mcId=").append(mcId)
                .append(", mcVersion=").append(mcVersion).append("]");
        return builder.toString();
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getPlanId() {
        return planId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getUnitId() {
        return unitId;
    }

    public void setUnitId(int unitId) {
        this.unitId = unitId;
    }

}
