package com.baidu.beidou.bes.user.constant;

import java.util.HashMap;
import java.util.Map;
/**
 * 常量信息
 * 
 * @author caichao
 */
public class UserConstant {
	
	//审核状态
	public final static int AUDIT_SUCCESS = 0;//审核通过
	public final static int UNPUSH_AUDIT = 1;//未推送审核
	public final static int AUDITING = 2;//审核中
	public final static int AUDIT_FAILT = 3;//审核失败
	
	public final static int PUSH_AUDIT_FAIL = 4;//推送失败，原因很多，详情查看对应的error_code,reason字段

	//api 返回结果码
	public final static int SUCCESS = 0;//全部成功
	public final static int FAIL = 1;//全部失败
	public final static int PART_SUCCESS = 2;//部分执行成功
	public final static int SYS_FAIL = 3;//系统认证失败或者系统内部错误
	
	public final static String SUC_CODE = "0";
	public final static String SYS_ERROR_CODE = "3";
	
	//审核结果信息
	public final static String PASS = "通过";
	public final static String UNPASS = "不通过";
	public final static String WAIT_AUDIT = "待通过";
	
	//adx
	public final static int TENCENT = 1;
	public final static int SOHU = 2;
	//腾讯api 错误码信息
	public final static Map<Integer,String> errorCodeMap = new HashMap<Integer,String>();
	
	static {
		errorCodeMap.put(500, "客户名称重复");
		errorCodeMap.put(503, "客户不能修改");
		errorCodeMap.put(504, "客户行业不合法");
		errorCodeMap.put(505, "客户URL为空或者是 URL不合法");
		errorCodeMap.put(506, "客户vocation为空或者时vocation不合法");
		errorCodeMap.put(507, "客户area为空");
		errorCodeMap.put(508, "客户qualification_class不合法");
		errorCodeMap.put(509, "内部错误，更新DB的过程中发生错误");
		errorCodeMap.put(510, "客户qualification_files不合法");
		errorCodeMap.put(511, "客户file_name不合法");
		errorCodeMap.put(512, "客户file_url不合法");
		errorCodeMap.put(513, "不支持的客户资质文件的格式,目前支持的文件格式:jpg,jpeg,gif,png");
		errorCodeMap.put(514, "内部错误，文件移动失败，可能是文件过大");
		errorCodeMap.put(515, "客户name为空");
		errorCodeMap.put(516, "客户memo为空");
		errorCodeMap.put(517, "一天内重复提交相同的客户资质信息内容");
	}
}
