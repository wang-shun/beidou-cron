package com.baidu.beidou.util.cdndriver.bo;

import com.baidu.beidou.util.cdndriver.protocol.CdnNsHead;


public class CdnPurgeRequestInfo extends AbstractCdnRequestInfo {

	private String[] uris;
	
	public static AbstractCdnRequestInfo buildCdnReqInfo(String[] uriArr){
		if(uriArr == null || uriArr.length < 1){
			return null;
		}
		
		CdnPurgeRequestInfo cdnReqInfo = new CdnPurgeRequestInfo();
		cdnReqInfo.setProvider(CdnNsHead.PROVIDER);
		cdnReqInfo.setUris(uriArr);
		
		return cdnReqInfo;
	}

	public String[] getUris() {
		return uris;
	}

	public void setUris(String[] uris) {
		this.uris = uris;
	}

}
