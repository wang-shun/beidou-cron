package com.baidu.beidou.account.mfcdriver;

import java.util.Map;

import com.baidu.beidou.account.mfcdriver.bean.response.AutoTransferResult;

/**
 * 挂接财务中心Operation服务
 * 
 * @author zhangp
 * @version 1.2.22
 */
public interface MfcOperationDriverProxy {

	/**autoProductTransfer：自动转账接口
	*@param userId: 用户ID。
	*@param appIdOut: 转出产品线ID。如果此参数为0，则转出产品线为转入产品线的上层账号。
	*@param appIdIn: 转入产品线ID。如果此参数为0，则转入产品线为转出产品线的上层账号。
	*@param amount: 金额。以元为单位。字段类型为浮点数，精度为2位小数。
	*@param params Rpc调用时需要传递的参数列表，通常是消息头。
	**/
	AutoTransferResult autoProductTransfer(String seqId, Integer userId, 
			int appIdOut, int appIdIn, double amount, Map<String, String> params);
}