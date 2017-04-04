package com.baidu.beidou.util.cdndriver.bo;

import java.util.Map;

public class CdnQueryResponseInfo {
	
	public static final String DELETE_FLAG = "deleted";
	
	public static final String NOT_DELETE_FLAG = "not_deleted";
	
	private String result = null;
	
	private String[] responseless_nodes = null;
	
	private Map<String, CdnQueryResponseItemInfo> responses = null;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String[] getResponseless_nodes() {
		return responseless_nodes;
	}

	public void setResponseless_nodes(String[] responselessNodes) {
		responseless_nodes = responselessNodes;
	}

	public Map<String, CdnQueryResponseItemInfo> getResponses() {
		return responses;
	}

	public void setResponses(Map<String, CdnQueryResponseItemInfo> responses) {
		this.responses = responses;
	}

	
}
