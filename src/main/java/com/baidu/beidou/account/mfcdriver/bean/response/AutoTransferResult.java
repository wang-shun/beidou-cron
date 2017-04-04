package com.baidu.beidou.account.mfcdriver.bean.response;

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
public class AutoTransferResult extends BaseResult{
	
	private AutoTransferDataBean data;

	public AutoTransferDataBean getData() {
		return data;
	}

	public void setData(AutoTransferDataBean data) {
		this.data = data;
	}	
}