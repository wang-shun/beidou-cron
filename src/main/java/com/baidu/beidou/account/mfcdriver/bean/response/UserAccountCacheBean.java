package com.baidu.beidou.account.mfcdriver.bean.response;


/**
 * @author hanxu03
 *
 * 2013-6-24
 */
public class UserAccountCacheBean extends BaseDataBean {
	
	/**
	 * 所有用户的待加款项数据，第一维是用户级别，第二维是资金池级别，第三维是待加款级别
	 * 
     * @param  array   $userIds    用户ID数组，例如[5, 252, 2021]
     * @param  array   $accountIds 资金池类型ID数组，例如[1, 2, 3, 7]，其中1表示技术资金池，2表示广告资金池，3表示增值资金池，7表示网盟专属资金池。
     * @param  integer $opuid      操作人ID，可以不设置或者设置为0。
     * @return array               返回标准map，'data'数据为：
     *     array(
     *         "code" => 0,
     *         "result" => array(
     *             array(                                               // 用户1
     *                 array(                                           // 用户1的资金池1
     *                     array(                                       // 用户1的资金池1的第1条待加款项
     *                         "fund" => 123.45,                        //   总金额（=现金额+优惠额）
     *                         "cash" => 120.00,                        //   现金额
     *                         "bonus" => 3.45,                         //   优惠额
     *                         "orderrow" => 54321,                     //   订单行
     *                         "cachetime" => "YYYY-mm-dd HH:ii:ss",    //   进入缓存时间
     *                     ),
     *                     array("fund"=>xxx,"cash"=>xxx,"bonus"=>xxx,"orderrow"=>xxx,"cachetime"=>xxx),
     *                                                                  // 用户1的资金池1的第2条待加款项
     *                 ),
     *                 array(                                           // 用户1的资金池2
     *                     array("fund"=>xxx,"cash"=>xxx,"bonus"=>xxx,"orderrow"=>xxx,"cachetime"=>xxx),
     *                                                                  // 用户1的资金池2的第1条待加款项
     *                     ...
     *                 ),
     *             ),
     *             array(...),                                          // 用户2
     *             ...
     *         ),
     *         "errno" => array(
     *             0,                                                   // 用户1成功（0：成功，1：失败）
     *             0,                                                   // 用户2成功
     *             ...
     *         )
     *     )
     */
	FundToBeAddBean[][][] result;

	public FundToBeAddBean[][][] getResult() {
		return result;
	}

	public void setResult(FundToBeAddBean[][][] result) {
		this.result = result;
	}

}
