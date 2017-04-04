package com.baidu.beidou.cprogroup.bo;

import java.util.Date;

/**
 * 相似人群BO
 * 
 * @author Wang Yu
 * 
 */
public class SimilarPeople {
    private int groupId;

    private long pid; // 人群ID

    private long hpid;

    private String name;

    private int stat;

    private int alivedays;

    private long cookienum; // 关联人群数

    private int userid;

    private Date activetime;

    private Date addtime;

    private Date modtime;

    private int adduser;

    private int moduser;

    /**
     * getGroupId
     * 
     * @return groupId
     */
    public int getGroupId() {
        return groupId;
    }

    /**
     * setGroupId
     * 
     * @param groupId groupId
     */
    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    /**
     * groupId
     * 
     * @return pid
     */
    public long getPid() {
        return pid;
    }

    /**
     * setPid
     * 
     * @param pid pid
     */
    public void setPid(long pid) {
        this.pid = pid;
    }

    /**
     * getHpid
     * 
     * @return hpid
     */
    public long getHpid() {
        return hpid;
    }

    /**
     * setHpid
     * 
     * @param hpid hpid
     */
    public void setHpid(long hpid) {
        this.hpid = hpid;
    }

    /**
     * getName
     * 
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * setName
     * 
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * getStat
     * 
     * @return stat
     */
    public int getStat() {
        return stat;
    }

    /**
     * setStat
     * 
     * @param stat stat
     */
    public void setStat(int stat) {
        this.stat = stat;
    }

    /**
     * getAlivedays
     * 
     * @return alivedays
     */
    public int getAlivedays() {
        return alivedays;
    }

    /**
     * setAlivedays
     * 
     * @param alivedays alivedays
     */
    public void setAlivedays(int alivedays) {
        this.alivedays = alivedays;
    }

    /**
     * getCookienum
     * 
     * @return cookienum
     */
    public long getCookienum() {
        return cookienum;
    }

    /**
     * setCookienum
     * 
     * @param cookienum cookienum
     */
    public void setCookienum(long cookienum) {
        this.cookienum = cookienum;
    }

    /**
     * getUserid
     * 
     * @return userid
     */
    public int getUserid() {
        return userid;
    }

    /**
     * setUserid
     * 
     * @param userid userid
     */
    public void setUserid(int userid) {
        this.userid = userid;
    }

    /**
     * getActivetime
     * 
     * @return activetime
     */
    public Date getActivetime() {
        return activetime;
    }

    /**
     * setActivetime
     * 
     * @param activetime activetime
     */
    public void setActivetime(Date activetime) {
        this.activetime = activetime;
    }

    /**
     * getAddtime
     * 
     * @return addtime
     */
    public Date getAddtime() {
        return addtime;
    }

    /**
     * setAddtime
     * 
     * @param addtime addtime
     */
    public void setAddtime(Date addtime) {
        this.addtime = addtime;
    }

    /**
     * getModtime
     * 
     * @return modtime
     */
    public Date getModtime() {
        return modtime;
    }

    /**
     * setModtime
     * 
     * @param modtime modtime
     */
    public void setModtime(Date modtime) {
        this.modtime = modtime;
    }

    /**
     * getAdduser
     * 
     * @return adduser
     */
    public int getAdduser() {
        return adduser;
    }

    /**
     * setAdduser
     * 
     * @param adduser adduser
     */
    public void setAdduser(int adduser) {
        this.adduser = adduser;
    }

    /**
     * getModuser
     * 
     * @return moduser
     */
    public int getModuser() {
        return moduser;
    }

    /**
     * setModuser
     * 
     * @param moduser moduser
     */
    public void setModuser(int moduser) {
        this.moduser = moduser;
    }
}
