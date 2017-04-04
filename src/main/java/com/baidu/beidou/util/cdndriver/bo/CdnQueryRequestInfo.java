package com.baidu.beidou.util.cdndriver.bo;

import com.baidu.beidou.util.cdndriver.protocol.CdnNsHead;

public class CdnQueryRequestInfo extends AbstractCdnRequestInfo {
	
	private String id;
	
	public static AbstractCdnRequestInfo buildCdnReqInfo(String accessId){
		if(accessId == null){
			return null;
		}
		
		CdnQueryRequestInfo cdnReqInfo = new CdnQueryRequestInfo();
		cdnReqInfo.setProvider(CdnNsHead.PROVIDER);
		cdnReqInfo.setId(accessId);
		
		return cdnReqInfo;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
