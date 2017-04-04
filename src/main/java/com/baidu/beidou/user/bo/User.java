package com.baidu.beidou.user.bo;
// Generated 2008-7-9 20:51:54 by Hibernate Tools 3.2.1.GA



/**
 * @author zengyunfeng
 */
public class User  implements java.io.Serializable {


     private Integer id;
     private Integer userid;
     private String username;
     private Integer ushifenstatid;
     private Integer ustate;
     private Integer balancestat;
//     private UserInfo userInfo;

    public User() {
    }

	
    public User(Integer userid) {
        this.userid = userid;
    }
   
    public Integer getId() {
        return this.id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getUserid() {
        return this.userid;
    }
    
    public void setUserid(Integer userid) {
        this.userid = userid;
    }
    public String getUsername() {
        return this.username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    public Integer getUshifenstatid() {
        return this.ushifenstatid;
    }
    
    public void setUshifenstatid(Integer ushifenstatid) {
        this.ushifenstatid = ushifenstatid;
    }
    public Integer getUstate() {
        return this.ustate;
    }
    
    public void setUstate(Integer ustate) {
        this.ustate = ustate;
    }

    /**
     * toString
     * @return String
     */
     public String toString() {
	  StringBuffer buffer = new StringBuffer();

      buffer.append(getClass().getName()).append("@").append(Integer.toHexString(hashCode())).append(" [");
      buffer.append("id").append("='").append(getId()).append("' ");			
      buffer.append("userid").append("='").append(getUserid()).append("' ");			
      buffer.append("username").append("='").append(getUsername()).append("' ");			
      buffer.append("ushifenstatid").append("='").append(getUshifenstatid()).append("' ");			
      buffer.append("ustate").append("='").append(getUstate()).append("' ");			
      buffer.append("balancestat").append("='").append(getBalancestat()).append("' ");			
      buffer.append("]");
      
      return buffer.toString();
     }

   public boolean equals(Object other) {
         if ( (this == other ) ) return true;
		 if ( (other == null ) ) return false;
		 if ( !(other instanceof User) ) return false;
		 User castOther = ( User ) other; 
         
		 return ( (this.getId()==castOther.getId()) || ( this.getId()!=null && castOther.getId()!=null && this.getId().equals(castOther.getId()) ) );
   }
   
   public int hashCode() {
         int result = 17;
         
         result = 37 * result + ( getId() == null ? 0 : this.getId().hashCode() );
         
         return result;
   }

public Integer getBalancestat() {
	return balancestat;
}


public void setBalancestat(Integer balancestat) {
	this.balancestat = balancestat;
}   


}


