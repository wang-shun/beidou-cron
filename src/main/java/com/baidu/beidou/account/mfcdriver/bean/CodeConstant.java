package com.baidu.beidou.account.mfcdriver.bean;

public class CodeConstant {
	//	 status字段
	public static int STATUS_OK = 0;                        // 数据正常
	public static int STATUS_NO_SERVICE = 1;                // 指定的服务不存在
	public static int STATUS_BATCH_PARAMS_TOOMANY = 2;      // 批量查询量超过阈值
	public static int STATUS_INVALID_PARAMS = 3;            // 无效的参数
	public static int STATUS_INVALID_IP = 4;                // 非法IP访问
	public static int STATUS_SERVICE_STOP = 5;              // 服务停止
	public static int STATUS_ERR_UNKNOWN = 99;              // 未知错误
    
    
    // code字段
	public static int CODE_ALL_RIGHT = 0;                   // 数据全部正确
	public static int CODE_ALL_WRONG = 1;                   // 数据全部错误
	public static int CODE_PART_RIGHT = 2;                  // 数据部分正确
    
    
    // errno字段
	public static int ERR_EMPTY = 1;                        // 数据不存在
	public static int ERR_NO_AUTH = 2;                      // 无权限
	public static int ERR_INVALID_USER = 11;                // 无效的用户
	public static int ERR_INVALID_PRODUCT = 12;             // 无效的产品线
	public static int ERR_INVALID_USERTYPE = 13;            // 无效的用户类型
	public static int ERR_NOENOUGH_FUND = 14;               // 资金不足
	public static int ERR_ONESTATION_FUND = 15;               // 一站式资金池	
}
