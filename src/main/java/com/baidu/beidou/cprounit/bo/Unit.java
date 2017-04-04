package com.baidu.beidou.cprounit.bo;


import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.baidu.beidou.cprogroup.bo.CproGroup;
import com.baidu.beidou.cproplan.bo.CproPlan;
import com.baidu.beidou.user.bo.User;

/**
 * @author liuhao
 */
public class Unit implements java.io.Serializable {

	private Long id;
	private CproGroup group;
	private CproPlan plan;
	private User user;
	private Integer state;
	private Integer submitType;
	private Integer submiter;
	private Integer changer;
	private Date subTime;
	private Date chaTime;
	private UnitMater material;
	private Date auditTime;
		
	/**
	 * 默认值为0
	 */
    private Integer helpstatus = Integer.valueOf(0);
    
	public Unit() {
	}

	public Unit(long id, CproGroup group, CproPlan plan, User user) {
		this.id = id;
		this.group = group;
		this.plan = plan;
		this.user = user;
	}

	public Unit(long id, CproGroup group, CproPlan plan, User user,
			Integer state, Integer submitType, Integer submiter,
			Integer changer, Date subTime, Date chaTime) {
		this.id = id;
		this.group = group;
		this.plan = plan;
		this.user = user;
		this.state = state;
		this.submitType = submitType;
		this.submiter = submiter;
		this.changer = changer;
		this.subTime = subTime;
		this.chaTime = chaTime;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public CproGroup getGroup() {
		return this.group;
	}

	public void setGroup(CproGroup group) {
		this.group = group;
	}

	public CproPlan getPlan() {
		return this.plan;
	}

	public void setPlan(CproPlan plan) {
		this.plan = plan;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Integer getState() {
		return this.state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Integer getSubmitType() {
		return this.submitType;
	}

	public void setSubmitType(Integer submitType) {
		this.submitType = submitType;
	}

	public Integer getSubmiter() {
		return this.submiter;
	}

	public void setSubmiter(Integer submiter) {
		this.submiter = submiter;
	}

	public Integer getChanger() {
		return this.changer;
	}

	public void setChanger(Integer changer) {
		this.changer = changer;
	}

	public Date getSubTime() {
		return this.subTime;
	}

	public void setSubTime(Date subTime) {
		this.subTime = subTime;
	}

	public Date getChaTime() {
		return this.chaTime;
	}

	public void setChaTime(Date chaTime) {
		this.chaTime = chaTime;
	}


	/**
	 * toString
	 * 
	 * @return String
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append(getClass().getName()).append("@").append(
				Integer.toHexString(hashCode())).append(" [");
		buffer.append("id").append("='").append(getId()).append("' ");
		buffer.append("state").append("='").append(getState()).append("' ");
		buffer.append("]");

		return buffer.toString();
	}

	public boolean equals(Object other) {
         if ( (this == other ) ) return true;
		 if ( (other == null ) ) return false;
		 if ( !(other instanceof Unit) ) return false;
		 Unit castOther = ( Unit ) other; 
         
		 return (this.getId().longValue()==castOther.getId().longValue());
   }

	public int hashCode() {
		int result = 17;

		result = 37 * result +  (getId() == null ? 0 : this.getId().hashCode()) ;

		return result;
	}

	/**
	 * @return the material
	 */
	public UnitMater getMaterial() {
		return material;
	}

	/**
	 * @param material
	 *            the material to set
	 */
	public void setMaterial(UnitMater material) {
		this.material = material;
	}

	/**
	 * @return the auditTime
	 */
	public Date getAuditTime() {
		return auditTime;
	}

	/**
	 * @param auditTime the auditTime to set
	 */
	public void setAuditTime(Date auditTime) {
		this.auditTime = auditTime;
	}

	/**
	 * @return the helpstatus
	 */
	public Integer getHelpstatus() {
		return helpstatus;
	}

	/**
	 * @param helpstatus the helpstatus to set
	 */
	public void setHelpstatus(Integer helpstatus) {
		this.helpstatus = helpstatus;
	}

}
