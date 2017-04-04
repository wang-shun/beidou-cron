package com.baidu.beidou.util.bmqdriver.bo;

/**
 * ClassName: BmqUrlCheck
 * Function: 接受请求封装
 *
 * @author <a href="mailto:genglei01@baidu.com">耿磊</a>
 * @version 1.0.0
 * @since cpweb-325
 * @date Oct 10, 2011
 * @see 
 */
public class BmqUrlResult {
	
	// 任务id
	long taskid;
	
	// 任务类型. 
	// 0: 新url连通性及跳转检查（刚提交的url或编辑的url）
	// 1: url连通性及跳转恢复巡查(不连通的或不符合跳转限制的URL)
	// 2: url连通性拒绝巡查(连通且符合跳转限制的URL)
	// 3: url病毒木马恢复巡查（含有病毒木马的URL）
	// 4: url病毒木马拒绝巡查（不含有病毒木马的URL）
	int type;
	
	int userid;
	String url;
	
	// result：
	// 0 : URL连通性正常且符合跳转限制（对应type=0、1、2的结果）
	// 1 : URL不连通（对应type=0、1、2的结果）
	// 2 : URL跳转不符合规范（对应type=0、1、2的结果）
	// 3 : URL含有病毒木马（对应type=3、4的结果）
	// 4 : URL没有病毒木马 对应type=3、4的结果）
	int result;
	
	public long getTaskid() {
		return taskid;
	}
	public void setTaskid(long taskid) {
		this.taskid = taskid;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getUserid() {
		return userid;
	}
	public void setUserid(int userid) {
		this.userid = userid;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getResult() {
		return result;
	}
	public void setResult(int result) {
		this.result = result;
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(getClass().getName()).append("[")
				.append("taskid=").append(taskid).append(",")
				.append("type=").append(type).append(",")
				.append("userid=").append(userid).append(",")
				.append("url=").append(url).append(",")
				.append("result=").append(result)
				.append("]");
		
		return buffer.toString();
	}
}
