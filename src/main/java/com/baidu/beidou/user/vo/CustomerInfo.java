package com.baidu.beidou.user.vo;


/**
 * 用户信息，包括：用户id、注册时间、修改时间、联系人姓名、电子邮件、网站名称、网站URL、公司名称、联系电话、传真号码、所在行业、资质行业、通信地址、邮政编码
 * @author zengyunfeng
 *
 */
public class CustomerInfo {

	private Integer userid;
    private String email;
    private String realname;
    private String company;
    private String website;
    private String phone;
    private String address;
    private String postcode;
    
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getPostcode() {
		return postcode;
	}
	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}
	public String getRealname() {
		return realname;
	}
	public void setRealname(String realname) {
		this.realname = realname;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String sitename) {
		this.company = sitename;
	}
	public Integer getUserid() {
		return userid;
	}
	public void setUserid(Integer userid) {
		this.userid = userid;
	}
	public String getWebsite() {
		return website;
	}
	public void setWebsite(String website) {
		this.website = website;
	}
}
