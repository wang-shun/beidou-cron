/**
 * 2009-12-14 下午10:13:44
 * @author zengyunfeng
 */
package com.baidu.beidou.user.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baidu.beidou.user.vo.CustomerInfo;
import com.baidu.beidou.user.vo.GlobalAuditerListCache;
import com.baidu.beidou.user.vo.NameStatusInfo;
import com.baidu.beidou.user.vo.UserEmailInfo;

/**
 * 获取用户信息的接口
 * @author zengyunfeng
 *
 */
public interface UserInfoMgr {

	/**
	 * 通过传入的内外用户id，判断是否存在管辖关系
	 * TODO:
	 * 从缓存中查询，如果缓存中没有，向UC查询，并更新缓存。
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param userId 用户ucid
	 * @param parentId 销售管理员id
	 * @return 管辖关系是否成立。 true:有管辖关系； false:没有管辖关系
	 */
	public boolean checkOutuserCanBeManaged(int userId,int parentId);
	
	/**
	 * 通过传入的外部用户ucid，获取其直系管理员id
	 * TODO:
	 * 向UC查询
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param userId
	 * @return
	 */
	public int[] getDirectAdmin(int userId);
	
	public List<UserEmailInfo> getDirectAdminInfo(int userId);
	
	public String getDirectAdminEmails(int userId);
	
	/**
	 * 通过传入的用户id，获取其管辖的所有外部用户id
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param userId
	 * @return
	 */
	public int[] getOutUcidsManaged(int userId);
	
	/**
	 * 取userid管辖的外部用户个数。
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param userId
	 * @return
	 */
	public int getSumOfOutUcidsManaged(final int userId);
	
	/**
	 * 通过传入的用户id，获取其管辖的所有外部用户id
	 * 分页处理
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param userId
	 * @return
	 */
	public int[] getOutUcidsManagerdByPage(final int userId, final int offset, final int limit);
	
	/**
	 * 通过传入的用户id，获取其管辖的所有外部用户id
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param userId
	 * @return
	 */
	public int[] getOutUcidsManagedFromUc(int userId);
	
	/**
	 * 获取内部用户的shifen状态
	 * 用于分配审核员时进行判断
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param userIds 待查询的用户shifen状态。
	 * @return 返回已userId为key, shifen状态为value的Map
	 */
	public Map<Integer, Integer> getUserShifenStatus(int[] userIds);
	
	/**
	 * 根据用户id获取用户的用户名和shifen状态<br>
	 * 直接向UC查询
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param userId
	 * @return
	 */
	public NameStatusInfo getUserNameAndShifenStatus(int userId);
	
	/**
	 * 获取指定用户的信息
	 * TODO:
	 * 直接向DRM发送请求
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param userId
	 * @return
	 */
	public CustomerInfo getCustomerInfo(int userId);
	
	/**
	 * 批量获取用户的邮件信息：用户id，用户realname, 用户email
	 * TODO:
	 * 直接向DRM发送请求。
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param userId
	 * @return
	 */
	public List<UserEmailInfo> getEmailInfo(int[] userId);
	
	public UserEmailInfo getEmailInfo(int userId);
	
	public String getEmailByUserid(int userId);
	
	/**
	 * 获取用户的角色
	 * 
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param userId
	 * @return 用户的beidou角色
	 */
	public String[] getUserRoles(int userId);
	
	/**
	 * isUserSem: 根据userId判断用户是否是大客户
	 * 是大客户返回true，不是管理员返回false
	 * @version 2.0.41
	 * @author genglei01
	 * @date Oct 12, 2011
	 */
	public boolean isUserSem(Integer userId);
	
	/**
	 * getAllSemUsers: 从UC获取所有大客户的集合
	 * 
	 * @version 2.0.41
	 * @author genglei01
	 * @date Oct 12, 2011
	 */
	public int[] getAllSemUsers();
	
	/**
	 * 批量查询用户的角色
	 * 没有使用缓存，直接向UC查询
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param userIds
	 * @return
	 */
	public Map<Integer, String[]> getUserRolesBatch(int[] userIds);
	
	/**
	 * 直接从UC获取用户权限
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param userId
	 * @return
	 */
	public String[] getUserAuthsFromUc(final int userId);
	
	/**
	 * 获取用户的权限，缓存中有就取缓存中数据
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param userId
	 * @return
	 */
	public String[] getUserAuths(int userId);
	
	/**
	 * 批量获取用户的用户名
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param userId
	 * @return
	 */
	public HashMap<Integer, String> getUserNameFromDrm(int[] userId);
	
	/**
	 * 直接从UC中获取所有的审核员
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param isHeavy
	 * @return
	 */
	public int[] getAuditerFromUc(boolean isHeavy);
	
	/**
	 * 直接从UC中获取所有的审核员
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @return
	 */
	public GlobalAuditerListCache getAllAuditerFromUc();
	
	/**
	 * 获取指定类型的所有审核员
	 * 首先从cache中获取，如果向UC中获取，则需要过滤shifenstatus
	 * @version 1.2.18
 	 * @author zengyunfeng
	 * @param isHeavy 是否为大客户审核员
	 * @return
	 */
	public int[] getAuditer(boolean isHeavy);
	
	/**
	 * 获取所有的审核员
	 * @return
	 */
	public GlobalAuditerListCache getAllAuditers();
	
	/**
	 * 获取所有审核员的用户名
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param isHeavy
	 * @return
	 */
	public List<String> getAuditerName(boolean isHeavy);
	
	/**
	 * 获取所有审核员的用户名
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @return
	 */
	public Map<Integer, String> getAuditerName() ;
	
	/**
	 * 获取所有的所有的大客户和vip客户
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @return 
	 */
	public int[] getAllHeavyCustomers() ;
	
	/**
	 * 获取所有的大客户
	 * @author zengyunfeng
	 * @return
	 */
	public int[] getAllClientCustomersFromUc();
	
	/**
	 * 获取所有的VIP客户
	 * @return
	 */
	public int[] getAllVipsFromUc();
	
	/**
	 * 获取所有的销售管理员
	 * @author zengyunfeng
	 * @return
	 */
	public int[] getAllSalerFromUc();
	
}

