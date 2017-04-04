package com.baidu.beidou.cprogroup.bo;

import java.util.Date;

public class CproQTKeyword implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer keywordid;
	private Integer wordid;
	private Integer planid;
	private Integer userid;
	private Integer groupid;
	private String keyword;
	private Integer adduser;
	private Date addtime;
	
	public CproQTKeyword(){
	}
	
	public CproQTKeyword(Integer keywordid, Integer wordid, Integer groupid, String keyword){
		this.keywordid = keywordid;
		this.wordid = wordid;
		this.groupid = groupid;
		this.keyword = keyword;
	}
	
	public CproQTKeyword(Integer keywordid, Integer wordid, Integer groupid, String keyword,
			Integer userid, Integer planid, Integer adduser, Date addtime){
		this.keywordid = keywordid;
		this.wordid = wordid;
		this.groupid = groupid;
		this.keyword = keyword;
		this.userid = userid;
		this.planid = planid;
		this.addtime = addtime;
		this.adduser = adduser;
	}
	
	public Integer getKeywordid() {
		return keywordid;
	}
	public void setKeywordid(Integer keywordid) {
		this.keywordid = keywordid;
	}
	public Integer getWordid() {
		return wordid;
	}
	public void setWordid(Integer wordid) {
		this.wordid = wordid;
	}
	public Integer getPlanid() {
		return planid;
	}
	public void setPlanid(Integer planid) {
		this.planid = planid;
	}
	public Integer getUserid() {
		return userid;
	}
	public void setUserid(Integer userid) {
		this.userid = userid;
	}
	public Integer getGroupid() {
		return groupid;
	}
	public void setGroupid(Integer groupid) {
		this.groupid = groupid;
	}
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public Integer getAdduser() {
		return adduser;
	}
	public void setAdduser(Integer adduser) {
		this.adduser = adduser;
	}
	public Date getAddtime() {
		return addtime;
	}
	public void setAddtime(Date addtime) {
		this.addtime = addtime;
	}
	
	@Override
	public int hashCode(){
		return getKeywordid() == null ? 0 : getKeywordid();
	}
	
	@Override
    public String toString() {
  	  	StringBuffer buffer = new StringBuffer();

        buffer.append(getClass().getName()).append("@").append(Integer.toHexString(hashCode())).append(" [");
        buffer.append("keywordId").append("='").append(getKeywordid()).append("' ");			
        buffer.append("]");
        
        return buffer.toString();
    }
}
