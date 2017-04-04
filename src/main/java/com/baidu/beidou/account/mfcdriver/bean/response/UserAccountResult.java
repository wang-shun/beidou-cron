package com.baidu.beidou.account.mfcdriver.bean.response;

import java.util.List;

/**
 * array (
 *		"version"  => "v1",
 *		"status" => 0,
 *		"data" => array(
 *            array("productid" => 产品线1id,
 *                  "balance" => 产品线1余额,
 *                  "consume" => 产品线1消费额, 
 *                  "invest" => 产品线1投资额),
 *            array("productid" => 产品线2id,
 *                  "balance" => 产品线2余额,
 *                  "consume" => 产品线2消费额, 
 *                  "invest" => 产品线2投资额),
 *         )
 *  )
*/

public class UserAccountResult extends BaseResult{
	
	private List<UserAccountDataBean> data;

	public List<UserAccountDataBean> getData() {
		return data;
	}

	public void setData(List<UserAccountDataBean> data) {
		this.data = data;
	}	
}