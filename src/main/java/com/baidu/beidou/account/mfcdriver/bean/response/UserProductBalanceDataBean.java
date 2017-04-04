package com.baidu.beidou.account.mfcdriver.bean.response;

import java.util.List;

/**
 * array (
*		“version”  => “v1”,
*		“status” => 0,
*		“data” => array(
*			“code” => 0,
*			“result” => array(
*				array(用户1产品1余额，用户1产品2余额..),
*				array(用户2产品1余额，用户2产品2余额…),
*				…
*			),
*			“errno” => array(
*				第一个结果状态，第二个结果状态…
*			)
*		)
*)
*/
public class UserProductBalanceDataBean extends BaseDataBean {
	
	private List<double[]> result;

	public List<double[]> getResult() {
		return result;
	}

	public void setResult(List<double[]> result) {
		this.result = result;
	}	
}