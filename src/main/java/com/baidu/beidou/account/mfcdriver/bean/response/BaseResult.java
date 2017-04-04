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
public class BaseResult{
	
	private String version;
	
	private int status;

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	
}