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
public class BaseDataBean{
	
	private int code;

	private String desc;	
	
	private List<Integer> errno;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public List<Integer> getErrno() {
		return errno;
	}

	public void setErrno(List<Integer> errno) {
		this.errno = errno;
	}	
}