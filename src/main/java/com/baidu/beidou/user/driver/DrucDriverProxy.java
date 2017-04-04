/**
 * 2009-12-14 下午09:28:36
 * @author zengyunfeng
 */
package com.baidu.beidou.user.driver;

import java.util.Map;

import com.baidu.beidou.user.driver.vo.UcRole;
/**
 * @author zengyunfeng
 * @deprecated 该类当前没有用。因为不用向UC发送任务消息头
 */
public interface DrucDriverProxy {

	/**
	 * 通过传入的内外用户ucid，判断是否在Group中是否存在管辖关系
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param intInnerUcid 内部用户ucid
	 * @param intOuterUcid 外部用户ucid
	 * @param strGroupType 分组类型
	 * @return 管辖关系是否成立。 true:有管辖关系； false:没有管辖关系
	 */
	public Boolean checkOutuserCanBeManaged(int intInnerUcid,int intOuterUcid, String strGroupType, int intAppid, Map<String, String> params);
	
	/**
	 * 通过传入的内部用户ucid, 获取其GroupType类型下的外部用户ID列表
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param intInnerUcid 内部用户ucid
	 * @param strGroupType 分组类型
	 * @param intAppid 产品线Appid
	 * @param intLimit 每次获取的个数
	 * @param intOffset 每次获取的偏移量
	 * @return 外部客户ucids列表 
	 */
	public int[] getOutUcidsManaged(int intInnerUcid, String strGroupType, int intAppid, int intLimit, int intOffset, Map<String, String> params);
	
	/**
	 * 通过传入的内部用户ucid，获取其GroupType类型下的外部用户总数
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param intInnerUcid 内部用户ucid
	 * @param strGroupType 分组类型
	 * @param intAppid 产品线Appid
	 * @return 外部客户ucid总数 
	 */
	public Integer getSumOfOutUcidsManaged(int intInnerUcid,String strGroupType, int intAppid, Map<String, String> params);
	
	/**
	 * 获取用户分组关联的Leader Ucids
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param intOuterUcid
	 * @param strGroupType
	 * @return 分组的Leader Ucids
	 */
	public int[] getLeaderUcidsByOutucidAndGroupType(int intOuterUcid, String strGroupType, Map<String, String> params);
	
	/**
	 * 通过传入的用户ucid，获得全部的权限Tag
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param intUcid
	 * @param intAppid
	 * @return 拥有的权限Tag数组 
	 */
	public String[] getAuthTagsByUcidAndAppid(int intUcid, int intAppid, Map<String, String> params);
	
	/**
	 * 通过传入的用户ucid，获得全部的角色Tag
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param intUcid
	 * @param intAppid
	 * @return 拥有的角色Tag数组
	 */
	public String[] getRoleTagsByUcidAndAppid(int intUcid, int intAppid, Map<String, String> params);
	
	/**
	 * 批量获取用户角色
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param ucids
	 * @param intAppid
	 * @return
	 */
	public UcRole[] getRoleTagByUcids(int[] ucids, int intAppid, Map<String, String> params);
	
	/**
	 * 通过传入的开通角色的角色Tag，获得全部开通的用户
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param arrRoleTags
	 * @param intAllow
	 * @param intAppid
	 * @return 所有的ucid数组 Array ucids
	 */
	public int[] getUcidsByRoletags(String[] arrRoleTags, int intAppid, Map<String, String> params);
	
	/**
	 * 通过传入的开通角色的权限Tag，获得全部开通的用户
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param arrAuthTags
	 * @return 所有的ucid数组 Array ucids
	 */
	public int[] getUcidsByAuthtags(String[] arrAuthTags, int intAppid, Map<String, String> params);
	
	
}
