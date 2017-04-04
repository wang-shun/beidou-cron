/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.cprogroup.constant;

/**
 * 相似人群常量类
 * 
 * @author Wang Yu
 * 
 */
public class SimilarPeopleConstant {
    /** 相似人群开关关闭 */
    public static final int FLAG_OFF = 0;

    /** 未关联相似人群 */
    public static final String FLAG_OFF_TXT = "未关联相似人群";

    /** 相似人群开关开启 */
    public static final int FLAG_ON = 1;

    /** 已关联相似人群 */
    public static final String FLAG_ON_TXT = "已关联相似人群";

    /** 相似人群开关无效设置 */
    public static final int FLAG_ILLEGAL = -1;

    /** 关联人群数未统计 */
    public static final long COOKIE_NUM_NULL = -1L;

    /** 默认群名称 */
    public static final String NAME_DEFAULT = "相似人群";

    /** 状态 正常 */
    public static final int STAT_NORMAL = 1;

    /** 状态 已删除 */
    public static final int STAT_DEL = 0;

    /** 默认有效期 */
    public static final int ALIVEDAYS_DEFAULT = 30;
}
