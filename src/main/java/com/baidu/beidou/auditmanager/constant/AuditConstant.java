package com.baidu.beidou.auditmanager.constant;

import java.util.HashMap;
import java.util.Map;

import com.baidu.beidou.auditmanager.vo.Reason;
import com.baidu.beidou.util.akadriver.constant.Constant;

public class AuditConstant {
	// 拒绝理由集合
	public static Map<Integer, Reason> reasonMap = new HashMap<Integer, Reason>();
	
	// aka轮巡相关拒绝拒绝理由：3,4,24
	public final static Map<Integer, Integer> patrolRefuseReasonMap
			= new HashMap<Integer, Integer>();;
	static {
		patrolRefuseReasonMap.put(Constant.BLACK_AUDIT_BIT, 3);
		patrolRefuseReasonMap.put(Constant.BRAND_AUDIT_BIT, 4);
		patrolRefuseReasonMap.put(Constant.COMPETITIVE_AUDIT_BIT, 24);
	}
	
	// 网页跳转拒绝理由id：26
	public final static int URL_JUMP_CHECK_REFUSE_ID = 26;
	
	// 网页跳转消费者的startpoint，分新建URL和轮巡URL两种
	// 变量名一样，但存储变量所在文件不一样
	public static String START_POINT = "startpoint";
	
	public static int AUDIT_PASS = 0;
	public static int AUDIT_REFUSE = 1;
	
	// 轮巡拒绝开关：1表示打开，0表示关闭
	// 如果开关打开，则对触犯规则的物料进行下线处理；否则不做处理
	public static int PATROL_REFUSE = 1;
	public static int PATROL_NOT_REFUSE = 0;
	
	// 即时拒绝开关：1表示打开，0表示关闭
	// 如果开关打开，则对触犯规则的物料进行下线处理；否则不做处理
	public static int INSTANT_REFUSE = 1;
	public static int INSTANT_NOT_REFUSE = 0;
	
	public static int AUDIT_USER_ROLE_KA=1;
	public static int AUDIT_USER_ROLE_SME=0;
	
	public static Map<Integer, Reason> getReasonMap() {
		return reasonMap;
	}
	public static void setReasonMap(Map<Integer, Reason> reasonMap) {
		AuditConstant.reasonMap = reasonMap;
	}
}
