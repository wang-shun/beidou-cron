/**
 * 2009-4-27 下午03:00:27
 */
package com.baidu.beidou.unionsite.bo;

import java.io.Serializable;
import java.util.Date;

/**
 * 统计后的数据
 * @author zengyunfeng
 * @version 1.0.7
 */
public class BDSiteBo implements Serializable {

	private static final long serialVersionUID = -7302712740847483036L;
	private int siteid;
	private UnionSiteIndex site = null;
	private SiteStatBo stat = null;
	private QValue qValue = null;

	private int parentid;
	private byte thruputtype;
	private String sizethruput;
	private int score;	//added by lvzichan，网站得分
	/**
	 * 网站的加入时间，在Cpweb-168中加入 
	 *  */
	private Date joinTime;
	
	/** 当前截图名 */
	private String snapshot;
	
	/** 当前是否有效，由于之前的valid只能表示七天前失效的情况，因此该字段用于标识七天内是否已经失效 */
	private boolean currentValid;
	
	/**
	 * @return the site
	 */
	public UnionSiteIndex getSite() {
		return site;
	}

	/**
	 * @param site
	 *            the site to set
	 */
	public void setSite(UnionSiteIndex site) {
		this.site = site;
	}

	/**
	 * @return the stat
	 */
	public SiteStatBo getStat() {
		return stat;
	}

	/**
	 * @param stat
	 *            the stat to set
	 */
	public void setStat(SiteStatBo stat) {
		this.stat = stat;
	}

	/**
	 * @return the qValue
	 */
	public QValue getQValue() {
		return qValue;
	}

	/**
	 * @param value
	 *            the qValue to set
	 */
	public void setQValue(QValue value) {
		qValue = value;
	}

	/**
	 * @return the siteid
	 */
	public int getSiteid() {
		return siteid;
	}

	/**
	 * @param siteid the siteid to set
	 */
	public void setSiteid(int siteid) {
		this.siteid = siteid;
	}


	/**
	 * @return the parentid
	 */
	public int getParentid() {
		return parentid;
	}

	/**
	 * @param parentid the parentid to set
	 */
	public void setParentid(int parentid) {
		this.parentid = parentid;
	}

	/**
	 * @return the thruputtype
	 */
	public byte getThruputtype() {
		return thruputtype;
	}

	/**
	 * @param thruputtype the thruputtype to set
	 */
	public void setThruputtype(byte thruputtype) {
		this.thruputtype = thruputtype;
	}

	/**
	 * @return the sizethruput
	 */
	public String getSizethruput() {
		return sizethruput;
	}

	/**
	 * @param sizethruput the sizethruput to set
	 */
	public void setSizethruput(String sizethruput) {
		this.sizethruput = sizethruput;
	}

    public Date getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(Date joinTime) {
        this.joinTime = joinTime;
    }

	public String getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(String snapshot) {
		this.snapshot = snapshot;
	}

	public boolean isCurrentValid() {
		return currentValid;
	}

	public void setCurrentValid(boolean currentValid) {
		this.currentValid = currentValid;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

}
