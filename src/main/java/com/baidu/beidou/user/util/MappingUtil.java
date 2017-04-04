package com.baidu.beidou.user.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.ArrayUtils;

public class MappingUtil {

	private static Map<String,Integer> privilegeStrToInt;
	private static Map<Integer,String> privilegeIntToStr;
	private static Set<String> auditPrivilege;
	private static Map<String, String> roleSfToBd = null;
	

	/**
	 * 将权限从字符串映射到数字
	 * @param str
	 * @return
	 */
	public static int getPrivilegeInt(String str){
		Integer value=privilegeStrToInt.get(str);
		if(value==null){
			return 0;//没有该权限
		}else{
			return value;
		}
			
	}
	
	/**
	 * 将权限从数字映射到字符串
	 * @param str
	 * @return
	 */
	public static String getPrivilegeStr(int auth){
		return privilegeIntToStr.get(auth);
	}
	
	public static String[] getPrivilegeStrs(int[] auths){
		if(auths == null){
			return new String[0];
		}
		
		List<String> result = new ArrayList<String>(auths.length);
		for(int auth : auths){
			String authStr = getPrivilegeStr(auth);
			if(authStr != null){
				result.add(authStr);
			}
		}
		return result.toArray(new String[0]);
	}
	
	public static int[] getPrivilegeInts(String[] strs){
		if(strs == null){
			return new int[0];
		}
		List<Integer> result = new ArrayList<Integer>(strs.length);
		for(String auth : strs){
			int authStr = getPrivilegeInt(auth);
			if(authStr > 0){
				result.add(authStr);
			}
		}
		return ArrayUtils.toPrimitive(result.toArray(new Integer[0]));
	}
	
	/**
	 * 判断指定权限privilege是否是审核类的
	 * @return
	 */
	public static boolean isAudit(String privilege){
		return auditPrivilege.contains(privilege);
	}
	
	public void setPrivilegeStrToInt(Map<String,Integer> privilegeStrToInt) {
		MappingUtil.privilegeStrToInt = privilegeStrToInt;
		privilegeIntToStr=new HashMap<Integer,String>();
		for(Entry<String, Integer> ele : privilegeStrToInt.entrySet()){
			privilegeIntToStr.put(ele.getValue(), ele.getKey());
		}
	}

	public  void setAuditPrivilege(Set<String> auditPrivilege) {
		MappingUtil.auditPrivilege = auditPrivilege;
	}
	
	public void setRoleSfToBd(Map<String, String> roleMap){
		roleSfToBd = roleMap;
	}
	
	/**
	 * 根据shifen角色获取用户的beidou角色。
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param sfRoles
	 * @return
	 */
	public static String[] getBdRoles(String[] sfRoles){
		if(sfRoles==null||sfRoles.length == 0){
			return new String[0];
		}
		List<String> result = new ArrayList<String>();
		for(String sf : sfRoles){
			String role = roleSfToBd.get(sf);
			if(role != null){
				result.add(role);
			}
		}
		return result.toArray(new String[0]);
	}
	
	
	/**
	 * 根据beidou角色获取用户的shifen角色。
	 * 注意：普通用户角色会保护VIP角色的用户
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param bdRoles
	 * @return
	 */
	public static String[] getSfRoles(String[] bdRoles){
		if(bdRoles==null||bdRoles.length == 0){
			return new String[0];
		}
		List<String> result = new ArrayList<String>();
		for(Entry<String, String> ele : roleSfToBd.entrySet()){
			if(ArrayUtils.contains(bdRoles, ele.getValue())){
				result.add(ele.getKey());
			}
		}
		return result.toArray(new String[0]);
	}
	
	
}
