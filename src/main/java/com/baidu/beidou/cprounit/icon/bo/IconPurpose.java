package com.baidu.beidou.cprounit.icon.bo;
/**
 * 推广目的
 * @author tiejing
 *
 */
public class IconPurpose {
	/**推广目的ID*/
    private Integer purposeId;
    /**推广目的名称*/
	private String purposeName;
	
	public Integer getPurposeId() {
		return purposeId;
	}
	public void setPurposeId(Integer purposeId) {
		this.purposeId = purposeId;
	}

	
	public String getPurposeName() {
		return purposeName;
	}
	public void setPurposeName(String purposeName) {
		this.purposeName = purposeName;
	}
	/**
     * toString
     * @return String
     */
     public String toString() {
	  StringBuffer buffer = new StringBuffer();

      buffer.append(getClass().getName()).append("@").append(Integer.toHexString(hashCode())).append(" [");
      buffer.append("id").append("='").append(getPurposeId()).append("' ");			
      buffer.append("name").append("='").append(getPurposeName()).append("' ");
      buffer.append("]");
      
      return buffer.toString();
     }

   public boolean equals(Object other) {
         if ( (this == other ) ) return true;
		 if ( (other == null ) ) return false;
		 if ( !(other instanceof IconPurpose) ) return false;
		 IconPurpose castOther = ( IconPurpose ) other; 
         
		 return (this.getPurposeId()==castOther.getPurposeId());
   }
   
   public int hashCode() {
         int result = 17;
         result = 37 * result + (int) this.getPurposeId();
         return result;
   }
   
}
