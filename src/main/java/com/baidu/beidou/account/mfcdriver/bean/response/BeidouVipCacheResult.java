package com.baidu.beidou.account.mfcdriver.bean.response;

import java.util.List;

/**
 * array (
 *		"version"  => "v1",
 *		"status" => 0,
 *		"data" => array(
 *            array("uid" => userId,
 *                  "cnt" => 待加资金),
 *            array("uid" => userId,
 *                  "cnt" => 待加资金),
 *      )
 *  )
*/

public class BeidouVipCacheResult extends BaseResult{
	
	private List<BeidouVipCacheDataBean> data;

	public List<BeidouVipCacheDataBean> getData() {
		return data;
	}

	public void setData(List<BeidouVipCacheDataBean> data) {
		this.data = data;
	}	
}