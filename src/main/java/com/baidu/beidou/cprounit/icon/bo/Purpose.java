package com.baidu.beidou.cprounit.icon.bo;
/**
 * 系统图标的推广目的和一级行业的映射数据库对象
 * @author tiejing
 *
 */
public class Purpose {
  
  /**推广目的id*/
  private Integer purposeId;
  /**推广目的名称*/
  private String purposeName;
  /**一级行业Id*/
  private Integer firstTradeId;
  
  
  /**
   * toString
   * @return String
   */
   public String toString() {
	  StringBuffer buffer = new StringBuffer();

    buffer.append(getClass().getName()).append("@").append(Integer.toHexString(hashCode())).append(" [");
    buffer.append("purposeId").append("='").append(getPurposeId()).append("' ");
    buffer.append("purposeName").append("='").append(getPurposeName()).append("' ");
    buffer.append("firstTradeId").append("='").append(getFirstTradeId()).append("' ");
    buffer.append("]");
    
    return buffer.toString();
   }

 public boolean equals(Object other) {
       if ( (this == other ) ) return true;
		 if ( (other == null ) ) return false;
		 if ( !(other instanceof Purpose) ) return false;
		 Purpose castOther = ( Purpose ) other; 
       
		 return ((this.getFirstTradeId()==castOther.getFirstTradeId()) 
				  &&
				 (this.getPurposeId()==castOther.getPurposeId()));
 }
 
 public int hashCode() {
       int result = 17;
       result = 37 * result + (int) this.getFirstTradeId()+this.getPurposeId();
       return result;
 }
 
	public String getPurposeName() {
		return purposeName;
	}
	public Integer getPurposeId() {
		return purposeId;
	}

	public void setPurposeId(Integer purposeId) {
		this.purposeId = purposeId;
	}

	public Integer getFirstTradeId() {
		return firstTradeId;
	}

	public void setFirstTradeId(Integer firstTradeId) {
		this.firstTradeId = firstTradeId;
	}

	public void setPurposeName(String purposeName) {
		this.purposeName = purposeName;
	}
}
