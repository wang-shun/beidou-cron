package com.baidu.beidou.account.service;

import java.util.List;
import java.util.Map;

/**
 * 挂接财务中心所需服务
 * 
 * @author zhangp
 * @version 1.2.22
 */
public interface MfcService {
	/**
	 * getUserProductBalance($userIds, $products,  $opuid = 0)
	*	$userIds : array, 查询的用户ID数组。每个用户ID均是无符号整型。
	*	$products: array, 查询的产品线ID数组。每个产品线ID均是无符号整型。
	*	$opuid: int ,操作人的ID。默认为0。为0时，财务中心不验证操作人ID是否可管辖查询用户；
	*		非0时，财务中心将验证操作人ID是否可管辖查询用户。出于性能考虑，调用方最好自己验证权限，不传递此参数或者参数设为0.字段类型为无符号整型。
	* 分页逻辑包含在底层实现中
	* 返回结果中为二维数组，一维是用户，另一维度是产品线。顺序和输入参数的顺序严格保持一致。
	* 处理逻辑如下：如果顺序没有对应上，则部分包装成null；如果部分正确，正确结果肯定大于等于0，如果后台发现数据错误，则数据包装成“null”。对于使用方只需考虑对null的处理逻辑即可
	**/	
	double[][] getUserProductBalance(List<Integer> userIds, List<Integer> products, int opuid);
	
	/**
	 * 投资额查询接口
	 *	getUserProductInvest($userIds, $products, $opuid = 0)
	 *	格式与余额查询接口相同。
	 *	投资额为产品线上的历史总现金，总优惠，总补偿之和。
	 * 分页逻辑包含在底层实现中
	* 返回结果中为二维数组，一维是用户，另一维度是产品线。顺序和输入参数的顺序严格保持一致。
	* 处理逻辑如下：如果顺序没有对应上，则部分包装成null；如果部分正确，正确结果肯定大于等于0，如果后台发现数据错误，则数据包装成“null”。对于使用方只需考虑对null的处理逻辑即可
	**/	
	double[][] getUserProductInvest(List<Integer> userIds, List<Integer> products, int opuid);
	
	/**autoProductTransfer($seqId, $userId, $appIdOut, $appIdIn, $amount)
	*$userId: 用户ID。
	*$appIdOut: 转出产品线ID。如果此参数为0，则转出产品线为转入产品线的上层账号。
	*$appIdIn: 转入产品线ID。如果此参数为0，则转入产品线为转出产品线的上层账号。
	*$amount: 金额。以元为单位。字段类型为浮点数，精度为2位小数。
	*CodeConstant.STATUS_OK = 0
	 * CodeConstant.STATUS_NO_SERVICE = 1;
	 * CodeConstant.ERR_NOENOUGH_FUND = 14; 
	**/
	int autoProductTransfer(Integer userId, int appIdOut, int appIdIn, double amount);
	
	/**
	 * 
	 * 获取用户账户信息
	 * 
	 * 返回值描述如下：
	 * 数组行数n由products的长度决定
     * [n][0] 产品线n余额,
     * [n][1] 产品线n消费额, 
     * [n][2] 产品线n投资额),

	 * @param userId 用户ID
	 * @param products 产品ID数组
	 * @param forceMaster 是否强制读主库
	 * @return      
	 * @since
	 */
	double[][] getUserAccount(Integer userId, List<Integer> products, boolean forceMaster);
	
	
	/**
	 * 
	 * 分用户统计待加入beidou账面的缓存金额
	 * 返回值描述如下：
	 * Map的Key为用户ID
	 * Map的Value为对应用户的缓存余额
	 * @return      
	 * @since
	 */
	Map<Integer,Double> statBeidouVipCache();
	
	/**分用户查询用户资金池的待加款信息（一站式新增）
	 * @return
	 */
	Map<Integer,Double> getHeaverUserAccountCache(List<Integer> userIds, int opuid);
}