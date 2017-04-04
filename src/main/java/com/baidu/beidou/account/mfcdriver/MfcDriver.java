package com.baidu.beidou.account.mfcdriver;

import java.util.List;

import com.baidu.beidou.account.mfcdriver.bean.response.AutoTransferResult;
import com.baidu.beidou.account.mfcdriver.bean.response.BeidouVipCacheResult;
import com.baidu.beidou.account.mfcdriver.bean.response.UserAccountResult;
import com.baidu.beidou.account.mfcdriver.bean.response.UserProductBalanceResult;

/**
 * 挂接admaker需要访问的服务接口代理
 * 
 * @author yanjie
 * @version 1.2.3
 */
public interface MfcDriver {
	/**
	 * getUserProductBalance($userIds, $products,  $opuid = 0)
	*	$userIds : array, 查询的用户ID数组。每个用户ID均是无符号整型。
	*	$products: array, 查询的产品线ID数组。每个产品线ID均是无符号整型。
	*	$opuid: int ,操作人的ID。默认为0。为0时，财务中心不验证操作人ID是否可管辖查询用户；
	*		非0时，财务中心将验证操作人ID是否可管辖查询用户。出于性能考虑，调用方最好自己验证权限，不传递此参数或者参数设为0.字段类型为无符号整型。
	**/	
	UserProductBalanceResult getUserProductBalance(List<Integer> userIds, List<Integer> products, int opuid);
	
	/**
	 * 投资额查询接口
	 *	getUserProductInvest($userIds, $products, $opuid = 0)
	 *	格式与余额查询接口相同。
	 *	投资额为产品线上的历史总现金，总优惠，总补偿之和。
	**/	
	UserProductBalanceResult getUserProductInvest(List<Integer> userIds, List<Integer> products, int opuid);
	
	/**autoProductTransfer($seqId, $userId, $appIdOut, $appIdIn, $amount)
	*$seqId : 序列号。调用方保证24小时内此ID不重复使用。字段类型为字符串，长度限制在64个字符以内。
	*$userId: 用户ID。
	*$appIdOut: 转出产品线ID。如果此参数为0，则转出产品线为转入产品线的上层账号。
	*$appIdIn: 转入产品线ID。如果此参数为0，则转入产品线为转出产品线的上层账号。
	*$amount: 金额。以元为单位。字段类型为浮点数，精度为2位小数。
	**/
	AutoTransferResult autoProductTransfer(String seqId, Integer userId, int appIdOut, int appIdIn, double amount);
	
	
	/**
	 * getUserAccount($userId, $products, $forceMaster)
     * 查询普通用户、代子、代理商、大客户在产品线的余额、消费额、总投资现金。
     *
     * 业务逻辑：普通用户、代理商、大客户直接根据资金表获取金额；代子的余额、
     *      消费额直接根据资金表获取金额，总投资现金为""，从而与页面显示
     *      逻辑一致。
     * 
     * 可能错误：参数错误；用户不存在；用户类型错误；产品线ID无效。

	 * @param userId: 用户ID。
	 * @param products: array, 查询的产品线ID数组。每个产品线ID均是无符号整型。
	 * @param forceMaster: 值为"iamsure"时表示强制读主库
	 * @return      
	 * @since
	 */
	UserAccountResult getUserAccount(Integer userId, List<Integer> products, String forceMaster);
	
	/**
	 * statBeidouVipCache()
     * 分用户统计待加入beidou账面的缓存金额
	 * @return      
	 * @since
	 */
	BeidouVipCacheResult statBeidouVipCache();
	
}
