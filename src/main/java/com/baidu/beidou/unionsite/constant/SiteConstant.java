/**
 * 2009-4-22 上午03:35:03
 */
package com.baidu.beidou.unionsite.constant;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baidu.beidou.unionsite.exception.ErrorFormatException;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class SiteConstant {
    
    //----------------------------->add by liangshimu,2010-05-22
    /** 性别属性中的最小值 */
    public static final int MIN_GENDER_VALUE = 1;
    /** 性别属性中的最大值 */
    public static final int MAX_GENDER_VALUE = 2;
    /** 年龄属性中的最小值 */
    public static final int MIN_AGE_VALUE = 1;
    /** 年龄属性中的最大值 */
    public static final int MAX_AGE_VALUE = 7;
    /** 学历属性中的最小值 */
    public static final int MIN_EDU_VALUE = 1;
    /** 学历属性中的最大值 */
    public static final int MAX_EDU_VALUE = 8;
    
    //<-----------------------------add by liangshimu,2010-05-22
    
    //----------------------------->add by liangshimu,2010-05-22
    //IP统计区间的枚举值及意义。
    /** 0-1万 */
    public static final int CONSTANT_IP_LEVEL_1 = 1; 
    /** 1-5万 */
    public static final int CONSTANT_IP_LEVEL_2 = 2; 
    /** 5-10万 */
    public static final int CONSTANT_IP_LEVEL_3 = 3; 
    /** 10万以上 */
    public static final int CONSTANT_IP_LEVEL_4 = 4; 
    
    //UV统计区间的枚举值及意义。
    /** 0-1万 */
    public static int CONSTANT_UV_LEVEL_1 = 1; 
    /** 1-5万 */
    public static int CONSTANT_UV_LEVEL_2 = 2; 
    /** 5-10万 */
    public static int CONSTANT_UV_LEVEL_3 = 3; 
    /** 10万以上 */
    public static int CONSTANT_UV_LEVEL_4 = 4; 
    
    //网站热度枚举值及意义
    /** 竞争较少 */
    public static final byte CONSTANT_CMP_LEVEL_1 = 1;
    /** 竞争比较缓和 */
    public static final byte CONSTANT_CMP_LEVEL_2 = 2;
    /** 竞争度一般 */
    public static final byte CONSTANT_CMP_LEVEL_3 = 3;
    /** 竞争比较激烈 */
    public static final byte CONSTANT_CMP_LEVEL_4 = 4;
    /** 竞争非常激烈 */
    public static final byte CONSTANT_CMP_LEVEL_5 = 5;
    //<----------------------------- end
    /**
     * getIPLevelEnumValue:将IP的具体的访问量数据值映射到区间枚举值
     *
     * @param amount IP访问量
     * @return      访问量所属区间枚举值
    */
    public static byte getIPLevelEnumValue(int amount) {
        if ( amount <= 10000 ) {
            return 1;
        } else if ( amount <= 50000 ) {
            return 2;
        } else if ( amount <= 100000 ) {
            return 3;
        } else {
            return 4;
        }
    }
    /**
     * getUVLevelEnumValue:将UV的具体的访问量数据值映射到区间枚举值
     *
     * @param amount 用户访问量
     * @return 访问量所属区间枚举值
    */
    public static byte getUVLevelEnumValue(int amount) {
        //算法同IP访问量
        return getIPLevelEnumValue(amount);
    }
    
    
    /**
     * 站点黑名单，在黑名单中的站点及其子域名不进入beidou站点全库
     */
    public static List<String> BLACK_SITE = new ArrayList<String>();

    public static boolean isBlackSite(String site) {
        if (site == null || site.length() == 0) {
            return false;
        }
        int size = BLACK_SITE.size();
        for (int index = 0; index < size; index++) {
            String bkSite = BLACK_SITE.get(index);
            if (bkSite.equals(site) || site.endsWith("." + bkSite)) {
                return true;
            }
        }
        return false;
    }

    //固定广告所支持的图片尺寸
    public static Map<Integer, int[]> FIXED_AD_SIZE = new HashMap<Integer, int[]>(30);
    
    //悬浮广告所支持的图片尺寸
    public static Map<Integer, int[]> FLOW_AD_SIZE = new HashMap<Integer, int[]>(10);

    //贴片广告所支持的图片尺寸
    public static Map<Integer, int[]> FILM_AD_SIZE = new HashMap<Integer, int[]>(10);
    /**
     * 判断某一个尺寸是否为北斗系统中(固定广告图片尺寸集中)的尺寸类型
     * 
     * @author zengyunfeng
     * @param size
     * @return 返回尺寸id, 0:表示没有该尺寸
     * 
     * mod by zhuqian @beidou1.2.24
     */
    public static int isSupportFixedAdSize(int[] size) {
        if (size == null || size.length != 2) {
            return 0;
        }
        return readFromSizeMap(size[0], size[1], FIXED_AD_SIZE);
    }

    /**
     * 判断某一个尺寸是否为北斗系统中(悬浮广告图片尺寸集中)的尺寸类型
     * 
     * @author zhuqian added @beidou1.2.24
     * @param size
     * @return 返回尺寸id, 0:表示没有该尺寸
     */
    public static int isSupportFlowAdSize(int[] size){
        if (size == null || size.length != 2) {
            return 0;
        }
        return readFromSizeMap(size[0], size[1], FLOW_AD_SIZE);
    }
    
    /**
     * 判断某一个尺寸是否为北斗系统中(贴片广告图片尺寸集中)的尺寸类型
     * 
     * @author zhuqian added @beidou1.2.24
     * @param size
     * @return 返回尺寸id, 0:表示没有该尺寸
     */
    public static int isSupportFilmAdSize(int[] size){
        if (size == null || size.length != 2) {
            return 0;
        }
        return readFromSizeMap(size[0], size[1], FILM_AD_SIZE);
    }
    
    private static int readFromSizeMap(int width, int height, Map<Integer, int[]> sizeMap){
        
        for (Map.Entry<Integer, int[]> cur : sizeMap.entrySet()) {
            if (cur.getValue().length != 2) {
                continue;
            }
            if (cur.getValue()[0] == width && cur.getValue()[1] == height) {
                return cur.getKey();
            }
        }   
        return 0;
    }
    
    public static final int[] fixedSizeTypes = { 0, 1, 2 }; // 固定广告所支持的图片类型
    public static final int[] flowSizeTypes = { 3, 4, 5 };  // 悬浮广告所支持的图片类型
    public static final int[] filmSizeTypes = { 6 };        // 贴片广告所支持的图片类型
    
    //以下编码方式已不再使用，mod by zhuqian @beidou1.2.24
    @Deprecated
    public static final int TEXT_AD = 1;    // 只支持文字广告
    @Deprecated
    public static final int TEXTPIC_AD = 2; // 支持文字和图片广告
    @Deprecated
    public static final int PIC_AD = 3;     // 只支持图片广告
    
    //网站支持的物料类型，二进制编码，由低位到高位，分别为：文字、图片(广义，包括img和flash)
    public static final int WL_TEXT_FLAG = 1;       //支持文字广告物料(...0001)
    public static final int WL_PIC_FLAG = 2;        //支持图片广告物料(...0010)
    public static final int WL_FULL_SUPPORT = 3;    //支持全部广告物料(...0011)
    public static final int WL_ZERO_SUPPORT = 0;    //不支持任何广告物料(...0000)
    
    //网站支持的展现类型，二进制编码，有低位到高位，分别为：固定、悬浮、贴片
    public static final int DISP_FIXED_FLAG = 1;    //支持广告的固定展现方式(...0001)
    public static final int DISP_FLOW_FLAG = 2;     //支持广告的悬浮展现方式(...0010)
    public static final int DISP_FILM_FLAG = 4;     //支持广告的贴片展现方式(...0100)
    public static final int DISP_FULL_FLAG = 7;     //支持广告的所有展现方式(...0111)
        
    public static boolean bitOp_supports(int type, int bitFlag){
        if((type & bitFlag) > 0){
            return true;
        }
        return false;
    }
    
    public static boolean bitOp_supportsMorethan(int type, int bitFlag){
        if((type ^ bitFlag) > 0){
            return true;
        }
        return false;
    }
    
    public static boolean bitOp_supportsOnly(int type, int bitFlag){
        if((type & (~bitFlag)) > 0){
            return false;
        }
        return true;
    }
    
    public static int bitOp_OR(int b1, int b2){
        return b1 | b2;
    }
    
    public static final byte MAINDOMAIN = 0; // 主域
    public static final byte SECONDDOMAIN = 1; // 非主域
    public static final int[] thruputIndex = new int[] { 1, 100000, 1000000,
            10000000 };
    public static final int[] sizeThruputIndex = new int[] { 1, 10000, 50000,
            100000 };
    
    //贴片尺寸流量分为4个级别：0-50万；50-100万；100-1000万；1000万以上
    public static final int[] filmSizeThruputIndex = new int[] {1, 500000, 1000000, 10000000};
    
    public static final String SIZETHRUPUTSPLITER = "|";
    /**
     * 推广组配置的投放站点和分类的分隔符
     */
    public final static String SITE_SEPERATOR_REX = "\\|";

    public static byte getSiteThruputType(long srchs) {
        int index = 0;
        for (index = 0; index < thruputIndex.length; index++) {
            if (srchs < thruputIndex[index]) {
                return (byte) index;
            }
        }
        return (byte) index;
    }

    private static byte getSizeThruputType(int srchs) {
        int index = 0;
        for (index = 0; index < sizeThruputIndex.length; index++) {
            if (srchs < sizeThruputIndex[index]) {
                return (byte) index;
            }
        }
        return (byte) index;
    }

    private static byte getFilmSizeThruputType(int srchs){
        int index = 0;
        
        //如果没有流量，则thruput值为0
        if(srchs < filmSizeThruputIndex[0]){
            return (byte) 0;
        }
        
        for(index = 1; index < filmSizeThruputIndex.length; index++){
            if (srchs < filmSizeThruputIndex[index]) {
                //在原有的尺寸流量标尺后追加Index
                return (byte) (index + sizeThruputIndex.length);
            }
        }
        return (byte) (index + sizeThruputIndex.length);
    }
    
    public static String getSizeThruputType(int maxSizeId,
            Map<Integer, Integer> sizeFlow) {
        StringBuilder result = new StringBuilder();
        for (int id = 1; id <= maxSizeId; id++) {
            Integer value = null;
            if (sizeFlow != null) {
                value = sizeFlow.get(id);
            }
            if (value == null) {
                result.append(0).append(SIZETHRUPUTSPLITER);
            } else {
                // 如果是贴片广告的尺寸，则使用贴片的尺寸流量标尺, added by zhuqian @beidou1.2.33
                if (SiteConstant.FILM_AD_SIZE.keySet().contains(id)) {
                    result.append(getFilmSizeThruputType(value)).append(
                            SIZETHRUPUTSPLITER);
                } else {
                    result.append(getSizeThruputType(value)).append(
                            SIZETHRUPUTSPLITER);
                }
            }
        }
        result.delete(result.length() - SIZETHRUPUTSPLITER.length(), result
                .length());
        return result.toString();
    }

    // 5: 5星; 4: 4星;3: 3星; 2: 2星级
    /**
     * 网站等级
     */
    public static final byte[] SCALE = new byte[] { 5, 4, 3, 2 };

    /**
     * 网站热度
     */
    public static final byte[] COMPETE_LEVEL = new byte[] { 5, 4, 3, 2, 1 };

    /**
     * 以下为用户状态，推广计划状态，推广组状态，用于统计有效的推广组
     */
    // =========================user type==========================
    public static final int BEIDOU_USERTYPE_NORMAL = 0;
    public static final int BEIDOU_USERTYPE_TEST = 1;

    // =======================user state===========================
    public static final int BEIDOU_USERSTATE_OPEN = 0;

    public static final int BEIDOU_USERSTATE_CLOSE = 1;

    public static final int BEIDOU_USERSTATE_DELETE = 2;

    public static final int SHIFEN_USERSTATE_INEFFECTIVE = 1;
    public static final int SHIFEN_USERSTATE_NORMAL = 2;
    public static final int SHIFEN_USERSTATE_NOMONEY = 3;
    public static final int SHIFEN_USERSTATE_REFUSED = 4;
    public static final int SHIFEN_USERSTATE_UNAUDITED = 6;
    public static final int SHIFEN_USERSTATE_BAN = 7;

    public static final int SHIFEN_TRANSFER_STATE_NORMAL = 0;

    /**
     * 推广计划状态-有效
     */
    public final static int PLAN_STATE_NORMAL = 0;

    /**
     * 推广组状态-有效
     */
    public final static int GROUP_STATE_NORMAL = 0;

    //站点失效状态：七天平均状态
    /** 有效：表示七天平均数据里面还有该网站信息 */
    public static final int SITE_VALID = 1;
    public static final int SITE_INVALID = 0;
    public static final int SITE_DEALING = 2;
    
    

    //站点当前失效状态：当前是否还有效
    /** 当前已经失效 */
    public static final int SITE_CURRENT_INVALID = SITE_INVALID;
    /** 当前有效 */
    public static final int SITE_CURRENT_VALID = SITE_VALID;
    /** 处理中 */
    public static final int SITE_CURRENT_DEALING = SITE_DEALING;

	/**
     * 人群属性总cookie过滤阈值，包含性别、年龄和学历
     */
    public static final int CROWDS_FILTER_THRESHOLD = 5;
    
    public static final String WWW_PREFIX = "www.";
    
    
    /**
     * 站点数据被过滤掉到原因
     * @see com.baidu.beidou.unionsite.dao.impl.SiteStatFileDaoImpl.java#readRecord(BufferedReader, int)
     */
    //throw new ErrorFormatException("site stat file with record='"
	//		+ line + "'");
    // 行所包含到域不足7个
    public static final int FILTER_OUT_REASON_FIELD_NUMBER_TOO_SMALL = 1;
    
	//throw new ErrorFormatException("wuliao is not valid in record='"
	//		+ line + "'");
    // 物料类型在系统支持的范围之外
	public static final int FILTER_OUT_REASON_WULIAO_TYPE_INVALID = 2;
	
    //throw new ErrorFormatException("[displayType=" + displayType
	//		+ "] wuliao should support pic in record='" + line + "'");
    // 行包含悬浮、贴片流量数据，但不支持图片物料类型
    public static final int FILTER_OUT_REASON_DISP_FILM_AND_FLOW_NOT_SUPPORT_PIC_WULIAO_TYPE = 3;
    
    // throw new ErrorFormatException(
	//		"error format in site stat file with record='" + line
	//				+ "'");
    // 行支持图片物料类型，但域数量小于9
    public static final int FILTER_OUT_REASON_SUPPORT_PIC_WULIAP_TYPE_BUT_FILED_LESS_THAN_9 = 4;
    
    //throw new ErrorFormatException(
	//		"error format in site stat file with record='" + line
	//				+ "'");
    // 尺寸号与{长*宽}数据数量不匹配
    public static final int FILTER_OUT_REASON_SIZE_AND_SIZEFLOW_LIST_NUMBER_NOT_MATCH = 5;
    
    //throw new ErrorFormatException(
	//		"error format in site stat file with record='"
	//				+ line + "'");
    // {长*宽}数据不合法，应该包含两个数字，*号分隔
    public static final int FILTER_OUT_REASON_SIZEFLOW_DELEMETER_BY_STAR_NOT_HAVE_TWO_FIELD = 6;
    
    //throw new ErrorFormatException("invalid displayType="
	//		+ displayType + "]");
    // 展现类型不合法，北斗不支持
    public static final int FILTER_OUT_REASON_DISP_INVALID = 7;
    
    //throw new ErrorFormatException("invalid displayType="
	//		+ displayType + "]");
    // 北斗中找不到{长*宽}数据对应到任何尺寸
    public static final int FILTER_OUT_REASON_NONE_SIZEFLOW_SUPPORT = 8;
    
}
