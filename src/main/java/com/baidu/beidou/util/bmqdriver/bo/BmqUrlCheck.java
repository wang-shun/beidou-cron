package com.baidu.beidou.util.bmqdriver.bo;

/**
 * ClassName: BmqUrlCheck
 * Function: 发送请求封装
 *
 * @author <a href="mailto:genglei01@baidu.com">耿磊</a>
 * @version 1.0.0
 * @since cpweb-325
 * @date Oct 10, 2011
 * @see 
 */
public class BmqUrlCheck {
	
	// 任务id，可以考虑使用物料id
	long taskid;
	
	// 任务类型. 
	// 0: 新url连通性及跳转检查（刚提交的url或编辑的url）
	// 1: url连通性及跳转恢复巡查(不连通的或不符合跳转限制的URL)
	// 2: url连通性拒绝巡查(连通且符合跳转限制的URL)
	// 3: url病毒木马恢复巡查（含有病毒木马的URL）
	// 4: url病毒木马拒绝巡查（正常的URL）
	int type;
	
	int userid;
	String url;
	
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
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(getClass().getName()).append("[")
				.append("taskid=").append(taskid).append(",")
				.append("type=").append(type).append(",")
				.append("userid=").append(userid).append(",")
				.append("url=").append(url)
				.append("]");
		
		return buffer.toString();
	}
}
