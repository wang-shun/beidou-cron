package com.baidu.beidou.cprogroup.util;

import com.baidu.beidou.cprogroup.constant.CproGroupConstant;

/**
 * 推广组定向类型工具类
 * 
 * @author Wang Yu
 * 
 */
public class TargettypeUtil {

    private static final int KT_TARGET_MASK = CproGroupConstant.GROUP_TARGET_TYPE_CT
            | CproGroupConstant.GROUP_TARGET_TYPE_QT | CproGroupConstant.GROUP_TARGET_TYPE_HCT;

    private static final int ALL_TARGET_MASK = CproGroupConstant.GROUP_TARGET_TYPE_CT
            | CproGroupConstant.GROUP_TARGET_TYPE_QT | CproGroupConstant.GROUP_TARGET_TYPE_HCT
            | CproGroupConstant.GROUP_TARGET_TYPE_RT | CproGroupConstant.GROUP_TARGET_TYPE_VT
            | CproGroupConstant.GROUP_TARGET_TYPE_IT | CproGroupConstant.GROUP_TARGET_TYPE_PACK
            | CproGroupConstant.GROUP_TARGET_TYPE_AT_RIGHT | CproGroupConstant.GROUP_TARGET_TYPE_AT_LEFT;

    /**
     * 获取推广组是否属于某定向类型
     * 
     * @param targetType 推广组定向类型
     * @param mask 定向类型值
     * @return 结果值
     */
    private static int getTargetByMask(int targetType, int mask) {
        if (!isValid(targetType)) {
            return CproGroupConstant.GROUP_TARGET_TYPE_NONE;
        }

        return targetType & mask;
    }

    /**
     * 判断KT RT VT是否共存
     * 
     * @param kt kt值
     * @param rt rt值
     * @param vt vt值
     * @return 是否共存
     */
    private static boolean isValidKtRtVt(int kt, int rt, int vt) {
        return !(kt > 0 && rt > 0) && !(kt > 0 && vt > 0) && !(rt > 0 && vt > 0);
    }

    /**
     * 定向类型值是否有效， 此方法不能调用getTargetByMask方法，否则会死循环
     * 
     * @param targetType 推广组定向类型
     * @return 是否有效
     */
    public static boolean isValid(int targetType) {
        // 没有选定向方式时，最小值为0；所有定向方式都选中时，最大值为ALL_TARGET_MASK
        if (targetType < 0 || targetType > ALL_TARGET_MASK) {
            return false;
        }

        // KT、RT、VT不能两两共存
        if (!isValidKtRtVt(targetType & KT_TARGET_MASK, targetType & CproGroupConstant.GROUP_TARGET_TYPE_RT, targetType
                & CproGroupConstant.GROUP_TARGET_TYPE_VT)) {
            return false;
        }

        return true;
    }

    /**
     * 是否KT定向
     * 
     * @param targetType 推广组定向类型
     * @return 是否KT
     */
    public static boolean hasKT(int targetType) {
        return getTargetByMask(targetType, KT_TARGET_MASK) > 0;
    }

    /**
     * 是否VT定向
     * 
     * @param targetType 推广组定向类型
     * @return 是否VT
     */
    public static boolean hasVT(int targetType) {
        return getTargetByMask(targetType, CproGroupConstant.GROUP_TARGET_TYPE_VT) > 0;
    }

}
