package com.baidu.beidou.account.mfcdriver;

import com.baidu.beidou.account.mfcdriver.bean.response.AutoTransferResult;


/**
 * 挂接财务中心所需服务
 * 
 * @author zhangp
 * @version 1.2.22
 */
public interface MfcOperationDriver {

	/**autoProductTransfer($seqId, $userId, $appIdOut, $appIdIn, $amount)
	*$userId: 用户ID。
	*$appIdOut: 转出产品线ID。如果此参数为0，则转出产品线为转入产品线的上层账号。
	*$appIdIn: 转入产品线ID。如果此参数为0，则转入产品线为转出产品线的上层账号。
	*$amount: 金额。以元为单位。字段类型为浮点数，精度为2位小数。
	**/
	AutoTransferResult autoProductTransfer(String seqId, Integer userId, int appIdOut, int appIdIn, double amount);
}