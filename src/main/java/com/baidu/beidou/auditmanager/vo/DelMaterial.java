package com.baidu.beidou.auditmanager.vo;
// Generated 2008-7-11 22:45:03 by Hibernate Tools 3.2.1.GA


import java.util.Date;

/**
 * @author zengyunfeng
 */
public class DelMaterial  implements java.io.Serializable {


     private long id;
     private long unitid;
     private long wid;
     private Date deltime;
     private int userId;
     
     /**
      * 挂接dr-mc区分物料类型为：0:临时物料，1:正式物料，2:历史物料
      * @version1.2.3 
      */
    private int mctype;
     
    private Long mcId = 0L; // UBMC物料ID
    private Integer mcVersionId = 0; // UBMC物料ID指定的版本ID
     

    public DelMaterial() {
    }

	
    public DelMaterial(long id) {
        this.id = id;
    }

    public DelMaterial(long unitid, long wid, int mctype, Date deltime, Integer userid) {
        this.unitid = unitid;
        this.wid = wid;
        this.deltime = deltime;
        this.mctype = mctype;
        this.userId = userid;
     }
    
    public DelMaterial(long id, long unitid, long wid, int mctype, Date deltime, Integer userId) {
       this.id = id;
       this.unitid = unitid;
       this.wid = wid;
       this.deltime = deltime;
       this.mctype = mctype;
       this.userId = userId;
    }
    
    /**
     * ubmc迁移，新增mcId和mcVersionId字段，以便清理ubmc中物料
     */
    public DelMaterial(Date deltime, int userId, long mcId, int mcVersionId) {
        this.unitid = 0L;
        this.wid = 0L;
        this.deltime = deltime;
        this.mctype = 0;
        this.userId = userId;
        this.mcId = mcId;
        this.mcVersionId = mcVersionId;
    }

    /**
     * ubmc迁移，新增mcId和mcVersionId字段，以便清理ubmc中物料
     */
    public DelMaterial(long id, Date deltime, int userId, long mcId, int mcVersionId) {
        this.id = id;
        this.unitid = 0L;
        this.wid = 0L;
        this.deltime = deltime;
        this.mctype = 0;
        this.userId = userId;
        this.mcId = mcId;
        this.mcVersionId = mcVersionId;
    }
   
    public long getId() {
        return this.id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    public long getUnitid() {
        return this.unitid;
    }
    
    public void setUnitid(long unitid) {
        this.unitid = unitid;
    }
    public long getWid() {
        return this.wid;
    }
    
    public void setWid(long wid) {
        this.wid = wid;
    }
    public Date getDeltime() {
        return this.deltime;
    }
    
    public void setDeltime(Date deltime) {
        this.deltime = deltime;
    }
    
    /**
     * @return the mctype
     */
    public int getMctype() {
        return mctype;
    }


    /**
     * @param mctype the mctype to set
     */
    public void setMctype(int mctype) {
        this.mctype = mctype;
    }


    public int getUserId() {
        return userId;
    }


    public void setUserId(int userId) {
        this.userId = userId;
    } 
    
    public Long getMcId() {
        return mcId;
    }

    public void setMcId(Long mcId) {
        this.mcId = mcId;
    }

    public Integer getMcVersionId() {
        return mcVersionId;
    }

    public void setMcVersionId(Integer mcVersionId) {
        this.mcVersionId = mcVersionId;
    }

    /**
     * toString
     * @return String
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getClass().getName()).append("@").append(Integer.toHexString(hashCode())).append(" [");
        buffer.append("id").append("='").append(getId()).append("' ");
        buffer.append("unitid").append("='").append(getUnitid()).append("' ");
        buffer.append("wid").append("='").append(getWid()).append("' ");
        buffer.append("mctype").append("='").append(getMctype()).append("' ");
        buffer.append("userId").append("='").append(getUserId()).append("' ");
        buffer.append("mcId").append("='").append(getMcId()).append("' ");
        buffer.append("mcVersionId").append("='").append(getMcVersionId()).append("' ");
        buffer.append("]");
        return buffer.toString();
    }

    public boolean equals(Object other) {
        if ( (this == other ) ) {
            return true;
        }
		if ( (other == null ) ) {
		    return false;
		}
		if ( !(other instanceof DelMaterial) ) {
		    return false;
		}
		DelMaterial castOther = ( DelMaterial ) other; 
         
		return (this.getId() == castOther.getId());
    }
   
    public int hashCode() {
        int result = 17;
         
        result = 37 * result + (int) this.getId();
         
         
         
        return result;
    }  


}


