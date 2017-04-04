package com.baidu.beidou.olap.constant;

public enum FileMeta {

    USER(0, "DailyUserStats", "UserId", "user"),
    PLAN(1, "DailyPlanStats", "UserId,PlanId", "plan"),
    GROUP(2, "DailyGroupStats", "UserId,PlanId,GroupId", "group"),
    UNIT(3, "DailyCreativeStats", "UserId,PlanId,GroupId,CreativeId", "unit"),
    DEVICE(4, "DailyDeviceStats", "UserId,PlanId,GroupId,DeviceId", "device"),
    APP(5, "DailyAppStats", "UserId,PlanId,GroupId,AppId", "app"),
    TRADE(6, "DailyTradeStats", "UserId,PlanId,GroupId,FirstTradeId,SecondTradeId", "trade"),
    REGION(7, "DailyRegionStats", "UserId,PlanId,GroupId,ProvinceId,CityId", "region"),
    SITE(8, "DailyCreativeSiteStats", "UserId,MainSiteId,SiteId", "site");
    
    public static final String BASEPATH = "/home/beidou/olapresult/";
    public static final String DEFAULT_VALUES = "Search,Click,Cost";
    
    private int id;
    private String olapTable;
    private String olapKeys;
    private String outputFile;

    FileMeta(int id, String olapTable, String keys, String outputFile) {
        this.olapTable = olapTable;
        this.id = id;
        this.olapKeys = keys;
        this.outputFile = outputFile;
    }
    
    public static FileMeta val(int num) {
        for (FileMeta type : FileMeta.values()) {
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

    public String getOutputPath() {
        return BASEPATH + outputFile;
    }
    
    public String getOlapKeys() {
        return olapKeys;
    }
}
