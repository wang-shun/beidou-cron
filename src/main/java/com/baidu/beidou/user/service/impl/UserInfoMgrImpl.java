/**
 * 2009-12-15 上午11:06:51
 * @author zengyunfeng
 */
package com.baidu.beidou.user.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.dozer.Mapper;

import com.baidu.beidou.user.constant.UserCacheConstant;
import com.baidu.beidou.user.constant.UserConstant;
import com.baidu.beidou.user.driver.DrucDriver;
import com.baidu.beidou.user.driver.SfDrmDriver;
import com.baidu.beidou.user.driver.SfDrmDriverProxy;
import com.baidu.beidou.user.driver.vo.DrmUserAcct;
import com.baidu.beidou.user.driver.vo.DrmUserInfo;
import com.baidu.beidou.user.driver.vo.DrmUserTrade;
import com.baidu.beidou.user.driver.vo.ShifenEsbResult;
import com.baidu.beidou.user.driver.vo.UcRole;
import com.baidu.beidou.user.service.UserInfoMgr;
import com.baidu.beidou.user.util.MappingUtil;
import com.baidu.beidou.user.vo.CustomerInfo;
import com.baidu.beidou.user.vo.GlobalAuditerListCache;
import com.baidu.beidou.user.vo.NameStatusInfo;
import com.baidu.beidou.user.vo.UserEmailInfo;
import com.baidu.beidou.util.BeidouConfig;
import com.baidu.beidou.util.memcache.BeidouCacheInstance;
import com.baidu.beidou.util.memcache.BeidouMemcacheClient;
import com.baidu.beidou.util.service.impl.BaseAuthenticateRpcServiceImpl;

/**
 * @author zengyunfeng
 * 
 */
public class UserInfoMgrImpl extends BaseAuthenticateRpcServiceImpl implements
		UserInfoMgr {

	private static final int MAX_COUNT_ONCE = 10000; // 获取销售管辖的用户一次最大批量
	private static final int USER_ROLE_PAGE_SIZE = 1000; // 获取用户角色一次最大批量

	private Mapper mapper = null;

	/**
	 * 用户操作权限的缓存失效时间
	 */
	private int USER_OP_AUTH_EXPIRE = 60;
	/**
	 * 用于权限的用户管辖关系缓存失效时间
	 */
	private int USER_RELATION_AUTH_EXPIRE = 60;

	/**
	 * 用于用户列表的用户管辖关系缓存失效时间
	 */
	private int USER_RELATION_USERLIST_EXPIRE = 1800;

	/**
	 * 审核员类型id缓存的失效时间
	 */
	private int AUDITER_TYPE_LIST_EXPIRE = 10 * 60;

	/**
	 * 审核员名缓存的失效时间
	 */
	private int AUDITER_NAME_EXPIRE = 10 * 60;

	/**
	 * 大客户和vip客户的用户缓存失效时间
	 */
	private int USER_TYPE_HEAVY_EXPIRE = 60 * 60;
	
	/**
	 * 当管辖用户数量小于MAXNUM_PRINT_UC_LOG时，将管理员管辖的所有用户打印出来
	 */
	private final int MAXNUM_PRINT_UC_LOG = 50;

	private DrucDriver drucDriver;
	private SfDrmDriverProxy sfDrmDriverProxy;
	
	public UserInfoMgrImpl(String userName, String password) {
		super(userName, password);
		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.baidu.beidou.user.service.UserInfoMgr#checkOutuserCanBeManaged(int,
	 *      int)
	 */
	public boolean checkOutuserCanBeManaged(final int userId, final int parentId) {
		BeidouMemcacheClient client = BeidouCacheInstance.getInstance();
		int[] userList = (int[]) client
				.memcacheRandomGet(UserCacheConstant.USER_RELATION_AUTH_KEY
						+ parentId);
		if (ArrayUtils.contains(userList, userId)) {
			return true;
		}
		
		/**
		 * modified by hanxu 2011-01-17, cpweb-207  1.0.48   
		 * 修复bug：为网盟专员添加账户时出现丢户（UC主从库同步延迟导致）
		 */
		final String[] ucGroupTypeArr = getUcGroupType(parentId);
		
		Boolean result = drucDriver.checkOutuserCanBeManaged(parentId,
				userId, ucGroupTypeArr,
				BeidouConfig.UC_POST_APPID);
		
		// 打印日志
		LOG.info(new StringBuilder("UC-LOG checkOutuserCanBeManaged ").append(result)
				.append(" parentId=").append(parentId).append(" userId=").append(userId));
		
		if (result == null || result) {
			// 更新缓存
			userList = ArrayUtils.add(userList, userId);
			client.memcacheSet(UserCacheConstant.USER_RELATION_AUTH_KEY
					+ parentId, userList, USER_RELATION_AUTH_EXPIRE);
		}
		return result;
	}

	public int[] getDirectAdmin(final int userId) {
		/**
		 * modified by hanxu 2011-01-17, cpweb-207  1.0.48   
		 * 修复bug：为网盟专员添加账户时出现丢户（UC主从库同步延迟导致）
		 */
		// 修复bug：审核员受到不属于管辖用户的审核邮件
		final String[] ucGroupTypeArr = new String[]{BeidouConfig.UC_BEIDOU_GROUPTYPE, BeidouConfig.UC_SHIFEN_GETLEADER_GROUPTYPE};
		int[]leaderIds = drucDriver.getLeaderUcidsByOutucidAndGroupType(userId, ucGroupTypeArr);

		// 打印日志
		StringBuilder sb = new StringBuilder("UC-LOG getLeaderUcidsByOutucidAndGroupType size=").append(leaderIds==null ? 0 : leaderIds.length)
			.append(" userId=").append(userId);
		if(!ArrayUtils.isEmpty(leaderIds) && leaderIds.length<=MAXNUM_PRINT_UC_LOG){
			sb.append(" ").append(Arrays.deepToString(ArrayUtils.toObject(leaderIds)));
		}
		LOG.info(sb);
		
		if(ArrayUtils.isEmpty(leaderIds)){
			return new int[0];
		}
		List<Integer> idList = new ArrayList<Integer>();
		Map<Integer, String[]> roleMap = getUserRolesBatch(leaderIds);
		
		// 只有二级管理员或网盟专员才能通过
		for(int id : leaderIds){
			String[] roleArr = roleMap.get(id);
			if(!ArrayUtils.isEmpty(roleArr) 
				&& !ArrayUtils.contains(roleArr, UserConstant.USER_ROLE_SALER_FIRST)
				&& (ArrayUtils.contains(roleArr, UserConstant.USER_ROLE_SALER_SECOND) 
						|| ArrayUtils.contains(roleArr, UserConstant.USER_ROLE_WANGMENG))){
				
				idList.add(id);
			}
		}
		
		// 打印日志
		sb = new StringBuilder("UC-LOG getLeaderUcidsByOutucidAndGroupType filtered size=").append(idList.size())
			.append(" userId=").append(userId);
		if(!CollectionUtils.isEmpty(idList) && idList.size()<=MAXNUM_PRINT_UC_LOG){
			sb.append(" ").append(Arrays.deepToString(idList.toArray(new Integer[0])));
		}
		LOG.info(sb);
		
		return ArrayUtils.toPrimitive(idList.toArray(new Integer[0]));
	}

	public List<UserEmailInfo> getDirectAdminInfo(int userId) {
		int[] admins = getDirectAdmin(userId);
		if (admins == null || admins.length == 0) {
			return new ArrayList<UserEmailInfo>();
		}
		return this.getEmailInfo(admins);
	}

	public String getDirectAdminEmails(int userId) {
		int[] admins = getDirectAdmin(userId);
		StringBuilder result = new StringBuilder();
		for (int admin : admins) {
			UserEmailInfo ue = getEmailInfo(admin);
			if (ue != null && !StringUtils.isBlank(ue.getEmail())) {
				result.append(';').append(ue.getEmail());
			}
		}

		return result.toString();
	}

	public UserEmailInfo getEmailInfo(int userId) {
		List<UserEmailInfo> emailInfos = getEmailInfo(new int[] { userId });
		if (emailInfos != null && emailInfos.size() > 0) {
			return emailInfos.get(0);
		} else
			return null;
	}

	public String getEmailByUserid(int userId) {
		List<UserEmailInfo> emailInfos = getEmailInfo(new int[] { userId });
		if (emailInfos != null && emailInfos.size() > 0) {
			return emailInfos.get(0).getEmail();
		} else
			return null;
	}

	public int getSumOfOutUcidsManaged(final int userId) {
		BeidouMemcacheClient client = BeidouCacheInstance.getInstance();
		int[] userList = (int[]) client
				.memcacheGet(UserCacheConstant.USER_RELATION_USERLIST_KEY
						+ userId);
		if (userList != null) {
			return userList.length;
		}
		
		/**
		 * modified by hanxu 2011-01-17, cpweb-207  1.0.48   
		 * 修复bug：为网盟专员添加账户时出现丢户（UC主从库同步延迟导致）
		 */
		final String[] ucGroupTypeArr = getUcGroupType(userId);
		
		Integer count = drucDriver.getSumOfOutUcidsManaged(userId,
				ucGroupTypeArr, BeidouConfig.UC_POST_APPID);
		
		// 打印日志
		LOG.info(new StringBuilder("UC-LOG getSumOfOutUcidsManaged count=").append(count).append(" userId=").append(userId));
		
		if (count == null || count <= 0) {
			return 0;
		}
		return count;
	}

	public int[] getOutUcidsManagerdByPage(final int userId, final int offset,
			final int limit) {
		BeidouMemcacheClient client = BeidouCacheInstance.getInstance();
		int[] userList = (int[]) client
				.memcacheGet(UserCacheConstant.USER_RELATION_USERLIST_KEY
						+ userId);
		if (userList != null) {
			return ArrayUtils.subarray(userList, offset, offset + limit);
		}
		
		/**
		 * modified by hanxu 2011-01-17, cpweb-207  1.0.48   
		 * 修复bug：为网盟专员添加账户时出现丢户（UC主从库同步延迟导致）
		 */
		final String[] ucGroupTypeArr = getUcGroupType(userId);
		
		userList = drucDriver.getOutUcidsManaged(userId,
				ucGroupTypeArr, BeidouConfig.UC_POST_APPID,
				limit, offset);
		
		// 打印日志
		LOG.info(new StringBuilder("UC-LOG getOutUcidsManaged size=").append(userList==null ? 0 : userList.length)
				.append(" userId=").append(userId).append(" limit=").append(limit).append(" offset=").append(offset));
		
		if (userList == null) {
			return new int[0];
		}
		return userList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.baidu.beidou.user.service.UserInfoMgr#getOutUcidsManaged(int)
	 */
	public int[] getOutUcidsManaged(final int userId) {
		BeidouMemcacheClient client = BeidouCacheInstance.getInstance();
		int[] userList = (int[]) client
				.memcacheGet(UserCacheConstant.USER_RELATION_USERLIST_KEY
						+ userId);
		if (userList != null) {
			return userList;
		}
		
		/**
		 * modified by hanxu 2011-01-17, cpweb-207  1.0.48   
		 * 修复bug：为网盟专员添加账户时出现丢户（UC主从库同步延迟导致）
		 */
		final String[] ucGroupTypeArr = getUcGroupType(userId);
		
		// 向UC查询
		Integer count = drucDriver.getSumOfOutUcidsManaged(userId,
				ucGroupTypeArr, BeidouConfig.UC_POST_APPID);
		
		// 打印日志
		LOG.info(new StringBuilder("UC-LOG getSumOfOutUcidsManaged count=").append(count).append(" userId=").append(userId));
		
		if (count == null || count <= 0) {
			return new int[0];
		}
		HashSet<Integer> result = new HashSet<Integer>(count);
		for (int index = 0; index < count; index += MAX_COUNT_ONCE) {
			final int offset = index;
			final int limit;
			if (offset + MAX_COUNT_ONCE > count) {
				limit = count - offset;
			} else {
				limit = MAX_COUNT_ONCE;
			}
			int[] curUserIds = drucDriver.getOutUcidsManaged(userId,
					ucGroupTypeArr,
					BeidouConfig.UC_POST_APPID, limit, offset);
			
			// 打印日志
			LOG.info(new StringBuilder("UC-LOG getOutUcidsManaged size=").append(curUserIds==null ? 0 : curUserIds.length)
					.append(" userId=").append(userId).append(" limit=").append(limit).append(" offset=").append(offset));
			
			CollectionUtils.addAll(result, ArrayUtils.toObject(curUserIds));
		}
		userList = ArrayUtils.toPrimitive(result.toArray(new Integer[0]));
		client.memcacheSetBig(UserCacheConstant.USER_RELATION_USERLIST_KEY
				+ userId, userList, USER_RELATION_USERLIST_EXPIRE);
		return ArrayUtils.toPrimitive(result.toArray(new Integer[0]));
	}

	public String[] getUserAuthsFromUc(final int userId) {
		BeidouMemcacheClient client = BeidouCacheInstance.getInstance();
		int[] authList = null;

		String[] result = drucDriver.getAuthTagsByUcidAndAppid(userId,
				BeidouConfig.UC_POST_APPID);
		authList = MappingUtil.getPrivilegeInts(result);
		client.memcacheSet(UserCacheConstant.USER_AUTH_KEY + userId, authList,
				USER_OP_AUTH_EXPIRE);
		return MappingUtil.getPrivilegeStrs(authList);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.baidu.beidou.user.service.UserInfoMgr#getUserAuths(int)
	 */
	public String[] getUserAuths(final int userId) {
		BeidouMemcacheClient client = BeidouCacheInstance.getInstance();
		int[] authList = (int[]) client
				.memcacheRandomGet(UserCacheConstant.USER_AUTH_KEY + userId);
		if (authList != null) {
			return MappingUtil.getPrivilegeStrs(authList);
		}

		return this.getUserAuthsFromUc(userId);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.baidu.beidou.user.service.UserInfoMgr#getUserRoles(int)
	 */
	public String[] getUserRoles(final int userId) {
		String[] result = drucDriver.getRoleTagsByUcidAndAppid(userId,
				BeidouConfig.UC_POST_APPID);
		return MappingUtil.getBdRoles(result);
	}
	
	/**
	 * isUserSem: 根据userId判断用户是否是大客户
	 * 是大客户返回true，不是管理员返回false
	 * @version 2.0.41
	 * @author genglei01
	 * @date Oct 12, 2011
	 */
	public boolean isUserSem(Integer userId) {
		int[] userList = getAllSemUsers();
		
		for (int index = 0; index < userList.length; index++) {
			if (userList[index] == userId) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * getAllSemUsers: 从UC获取所有大客户的集合
	 * 
	 * @version 2.0.41
	 * @author genglei01
	 * @date Oct 12, 2011
	 */
	public int[] getAllSemUsers() {
		BeidouMemcacheClient client = BeidouCacheInstance.getInstance();
		int[] userList = null;
		userList = (int[]) client
				.memcacheGet(UserCacheConstant.GLOBAL_USER_ROLE_SEM);
		if (userList != null) {
			return userList;
		}
		// 从UC重新获取
		userList = getAllClientCustomersFromUc();
		client.memcacheSetBig(UserCacheConstant.GLOBAL_USER_ROLE_SEM, 
				userList, USER_TYPE_HEAVY_EXPIRE);
		return userList;
	}

	/**
	 * @return the uSER_RELATION_AUTH_EXPIRE
	 */
	public int getUSER_RELATION_AUTH_EXPIRE() {
		return USER_RELATION_AUTH_EXPIRE;
	}

	/**
	 * @param user_relation_auth_expire
	 *            the uSER_RELATION_AUTH_EXPIRE to set
	 */
	public void setUSER_RELATION_AUTH_EXPIRE(int user_relation_auth_expire) {
		USER_RELATION_AUTH_EXPIRE = user_relation_auth_expire;
	}

	/**
	 * @return the uSER_RELATION_USERLIST_EXPIRE
	 */
	public int getUSER_RELATION_USERLIST_EXPIRE() {
		return USER_RELATION_USERLIST_EXPIRE;
	}

	/**
	 * @param user_relation_userlist_expire
	 *            the uSER_RELATION_USERLIST_EXPIRE to set
	 */
	public void setUSER_RELATION_USERLIST_EXPIRE(
			int user_relation_userlist_expire) {
		USER_RELATION_USERLIST_EXPIRE = user_relation_userlist_expire;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.baidu.beidou.user.service.UserInfoMgr#getInnerUserName(int[])
	 */
	public HashMap<Integer, String> getUserNameFromDrm(final int[] userId) {
		if (userId == null || userId.length < 1) {
			return new HashMap<Integer, String>();
		}
		final HashMap<Integer, String> result = new HashMap<Integer, String>(
				userId.length, 1);
		int max_size = 500;
		for (int index = 0; index < userId.length; index += max_size) {
			final int offset = index;
			final int limit;
			if (offset + max_size > userId.length) {
				limit = userId.length - offset;
			} else {
				limit = max_size;
			}
			DrmUserAcct[] usrname = this.getUserAcctFromDrm(ArrayUtils
					.subarray(userId, offset, offset + limit));
			if (usrname == null || usrname.length == 0) {
				continue;
			}
			for (DrmUserAcct ui : usrname) {
				result.put(ui.getUserid(), ui.getUsername());
			}
		}
		return result;
	}

	/**
	 * @param user_op_auth_expire
	 *            the uSER_OP_AUTH_EXPIRE to set
	 */
	public void setUSER_OP_AUTH_EXPIRE(int user_op_auth_expire) {
		USER_OP_AUTH_EXPIRE = user_op_auth_expire;
	}

	/**
	 * 过滤掉状态为filterStatus的用户
	 * 
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param userId
	 * @param filterStatus
	 * @return
	 */
	private List<Integer> filterUserSFStatus(int[] userIds, int[] filterStatus) {
		if (userIds == null || userIds.length < 1) {
			return new ArrayList<Integer>(0);
		}
		List<Integer> result = new ArrayList<Integer>(userIds.length);
		Map<Integer, Integer> status = this.getUserShifenStatus(userIds);
		for (Entry<Integer, Integer> entry : status.entrySet()) {
			if (!ArrayUtils.contains(filterStatus, entry.getValue())) {
				result.add(entry.getKey());
			}
		}
		return result;
	}

	private int[] getAllAuditerIdsFromUc() {
		// 向UC请求
		// 查询具有审核角色的审核员
		final String[] roles = MappingUtil
				.getSfRoles(new String[] { UserConstant.USER_ROLE_AUDITER });

		int[] allAuditer = drucDriver.getUcidsByRoletags(roles,
				BeidouConfig.UC_POST_APPID);
		int[] filterStatus = new int[] { UserConstant.SHIFEN_STATE_REFUSE,
				UserConstant.SHIFEN_STATE_CLOSE };
		// 过滤审核员的shifen状态
		List<Integer> allAuditerList = filterUserSFStatus(allAuditer,
				filterStatus);
		allAuditer = ArrayUtils.toPrimitive(allAuditerList
				.toArray(new Integer[0]));

		return allAuditer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.baidu.beidou.user.service.UserInfoMgr#getAllAuditerFromUc()
	 */
	public GlobalAuditerListCache getAllAuditerFromUc() {
		BeidouMemcacheClient client = BeidouCacheInstance.getInstance();
		GlobalAuditerListCache auditList = null;
		auditList = new GlobalAuditerListCache();
		// 向UC请求
		// 查询分配审核权限的审核员
		int[] allAuditer = drucDriver.getUcidsByAuthtags(
				new String[] { UserConstant.PRIVILEGE_ASSIGN_AUDIT },
				BeidouConfig.UC_POST_APPID);
		int[] filterStatus = new int[] { UserConstant.SHIFEN_STATE_REFUSE,
				UserConstant.SHIFEN_STATE_CLOSE };
		// 过滤审核员的shifen状态
		List<Integer> allAuditerList = filterUserSFStatus(allAuditer,
				filterStatus);
		if (CollectionUtils.isEmpty(allAuditerList)) {
			// 没有审核员
			return auditList;
		}
		allAuditer = ArrayUtils.toPrimitive(allAuditerList
				.toArray(new Integer[0]));

		// 查询分配审核权限的大客户审核员
		int[] keyAuditer = drucDriver.getUcidsByAuthtags(new String[] {
				UserConstant.PRIVILEGE_ASSIGN_AUDIT,
				UserConstant.PRIVILEGE_AUDIT_HEAVY },
				BeidouConfig.UC_POST_APPID);
		List<Integer> keyAuditerList = filterUserSFStatus(keyAuditer,
				filterStatus);
		keyAuditer = ArrayUtils.toPrimitive(keyAuditerList
				.toArray(new Integer[0]));

		if (keyAuditer == null || keyAuditer.length < 1) {
			auditList.setKeyAuditer(new int[0]);
			auditList.setNormalAuditer(allAuditer);
		} else {
			List<Integer> normalAuditer = new ArrayList<Integer>(
					allAuditer.length - keyAuditer.length);
			for (int id : allAuditer) {
				if (!ArrayUtils.contains(keyAuditer, id)) {
					normalAuditer.add(id);
				}
			}
			auditList.setKeyAuditer(keyAuditer);
			auditList.setNormalAuditer(ArrayUtils.toPrimitive(normalAuditer
					.toArray(new Integer[0])));
		}
		client.memcacheSet(UserCacheConstant.GLOBAL_AUDITER_TYPE_LIST,
				auditList, AUDITER_TYPE_LIST_EXPIRE);
		return auditList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.baidu.beidou.user.service.UserInfoMgr#getAuditer(boolean)
	 */
	public int[] getAuditerFromUc(boolean isHeavy) {
		GlobalAuditerListCache cache = getAllAuditerFromUc();
		if (cache == null) {
			return new int[0];
		}
		if (isHeavy) {
			return cache.getKeyAuditer();
		} else {
			return cache.getNormalAuditer();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.baidu.beidou.user.service.UserInfoMgr#getAuditer(boolean)
	 */
	public int[] getAuditer(boolean isHeavy) {
		BeidouMemcacheClient client = BeidouCacheInstance.getInstance();
		GlobalAuditerListCache auditList = (GlobalAuditerListCache) client
				.memcacheGet(UserCacheConstant.GLOBAL_AUDITER_TYPE_LIST);
		if (auditList == null) {
			return getAuditerFromUc(isHeavy);
		}
		if (isHeavy) {
			return auditList.getKeyAuditer();
		} else {
			return auditList.getNormalAuditer();
		}
	}

	/**
	 * getAllAuditer:
	 * 
	 * @return
	 * @since int[]
	 */
	private int[] getAllAuditerIds() {
		int[] result = getAllAuditerIdsFromUc();
		if (result == null) {
			return new int[0];
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	/**
	 * @modified by genglei01,
	 * @beidou-core 1.0.22
	 */
	public Map<Integer, String> getAuditerName() {
		BeidouMemcacheClient client = BeidouCacheInstance.getInstance();
		HashMap<Integer, String> auditerNames = (HashMap<Integer, String>) client
				.memcacheGet(UserCacheConstant.GLOBAL_AUDITER_NAME);
		if (auditerNames == null) {
			// 获取所有审核员的username
			int[] auditers = this.getAllAuditerIds();
			auditerNames = this.getUserNameFromDrm(auditers);
			client.memcacheSet(UserCacheConstant.GLOBAL_AUDITER_NAME,
					auditerNames, AUDITER_NAME_EXPIRE);
		}

		return auditerNames;
	}

	/**
	 * 从UC中获取所有大客户和VIP用户id，
	 * 
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param isHeavy
	 * @return
	 */
	private int[] getAllHeavyCustomersFromUc() {
		final String[] roles = MappingUtil.getSfRoles(new String[] {
				UserConstant.USER_ROLE_CUSTOMER_HEAVY,
				UserConstant.USER_ROLE_CUSTOMER_VIP });

		return drucDriver.getUcidsByRoletags(roles,
				BeidouConfig.UC_POST_APPID);

	}

	public int[] getAllVipsFromUc() {
		final String[] roles = MappingUtil
				.getSfRoles(new String[] { UserConstant.USER_ROLE_CUSTOMER_VIP });
		return drucDriver.getUcidsByRoletags(roles,
				BeidouConfig.UC_POST_APPID);

	}

	public int[] getAllClientCustomersFromUc() {
		final String[] roles = MappingUtil
				.getSfRoles(new String[] { UserConstant.USER_ROLE_CUSTOMER_HEAVY });
		return drucDriver.getUcidsByRoletags(roles,
				BeidouConfig.UC_POST_APPID);

	}

	public int[] getAllSalerFromUc(){
		final String[] roles = MappingUtil.getSfRoles(new String[]{
				UserConstant.USER_ROLE_SALER_FIRST, 
				UserConstant.USER_ROLE_SALER_SECOND});
		return getUcIdsByRoletags(roles);
	}

	private int[] getUcIdsByRoletags(final String[] roles){
		if(roles==null || roles.length ==0){
			return new int[0];
		}
					return drucDriver.getUcidsByRoletags(roles,
							BeidouConfig.UC_POST_APPID);

	}

	@SuppressWarnings("unchecked")
	public List<String> getAuditerName(boolean isHeavy) {
		BeidouMemcacheClient client = BeidouCacheInstance.getInstance();
		HashMap<Integer, String> auditerNames = (HashMap<Integer, String>) client
				.memcacheGet(UserCacheConstant.GLOBAL_AUDITER_NAME);
		int[] auditers = this.getAuditer(isHeavy);
		if (auditerNames == null) {
			// 获取所有审核员的username
			int[] opAuditers = this.getAuditer(!isHeavy);
			auditerNames = this.getUserNameFromDrm(ArrayUtils.addAll(auditers,
					opAuditers));
			client.memcacheSet(UserCacheConstant.GLOBAL_AUDITER_NAME,
					auditerNames, AUDITER_NAME_EXPIRE);
		}
		List<String> result = new ArrayList<String>(auditers.length);
		for (int id : auditers) {
			result.add(auditerNames.get(id));
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.baidu.beidou.user.service.UserInfoMgr#getAllHeavyCustomers()
	 */
	public int[] getAllHeavyCustomers() {
		BeidouMemcacheClient client = BeidouCacheInstance.getInstance();
		int[] userList = null;
		userList = (int[]) client
				.memcacheGet(UserCacheConstant.GLOBAL_USER_TYPE_HEAVY);
		if (userList != null) {
			return userList;
		}
		// 从UC重新获取
		userList = getAllHeavyCustomersFromUc();
		client.memcacheSetBig(UserCacheConstant.GLOBAL_USER_TYPE_HEAVY,
				userList, USER_TYPE_HEAVY_EXPIRE);
		return userList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.baidu.beidou.user.service.UserInfoMgr#getUserNameAndShifenStatus(int)
	 */
	public NameStatusInfo getUserNameAndShifenStatus(final int userId) {

		DrmUserAcct[] result = this.getUserAcctFromDrm(new int[] { userId });
		if (result.length == 0) {
			return null;
		}
		NameStatusInfo statusInfo = new NameStatusInfo();
		statusInfo.setUserId(result[0].getUserid());
		statusInfo.setSfStatus(result[0].getUstatus());
		statusInfo.setUserName(result[0].getUsername());
		statusInfo.setUlevelid(result[0].getPosition());
		return statusInfo;
	}

	private DrmUserAcct[] getUserAcctFromDrm(final int[] userIds) {
		if (userIds == null || userIds.length == 0) {
			return new DrmUserAcct[0];
		}
		ShifenEsbResult<DrmUserAcct[]> result = sfDrmDriverProxy
				.getUserAcctBatch(userIds, getHeaders());
		if (result == null || result.getFlag() != SfDrmDriver.RESULT_FLAG_OK
				|| result.getData() == null) {
			LOG.error("获取用户[" + userIds + "]的用户名和密码发生错误");
			return new DrmUserAcct[0];
		}
		return result.getData();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.baidu.beidou.user.service.UserInfoMgr#getUserShifenStatus(int[])
	 */
	public Map<Integer, Integer> getUserShifenStatus(int[] userIds) {
		DrmUserAcct[] accts = this.getUserAcctFromDrm(userIds);
		Map<Integer, Integer> result = new HashMap<Integer, Integer>(
				userIds.length, 1f);
		if (accts == null || accts.length == 0) {
			return result;
		}
		for (DrmUserAcct user : accts) {
			result.put(user.getUserid(), user.getUstatus());
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.baidu.beidou.user.service.UserInfoMgr#getCustomerInfo(int)
	 */
	public CustomerInfo getCustomerInfo(final int userId) {
		ShifenEsbResult<DrmUserInfo[]> result = sfDrmDriverProxy
				.getUserInfoBatch(new int[] { userId }, getHeaders());
		if (result == null || result.getFlag() != SfDrmDriver.RESULT_FLAG_OK
				|| result.getData() == null) {
			LOG.error("获取用户" + userId + "信息发生错误");
			return null;
		} else if (result.getData().length == 0) {
			return null;
		}

		DrmUserInfo info = result.getData()[0];
		CustomerInfo customerInfo = mapper.map(info, CustomerInfo.class);
		return customerInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.baidu.beidou.user.service.UserInfoMgr#getInnerInfor(int[])
	 */
	public List<UserEmailInfo> getEmailInfo(final int[] userId) {
		if (userId == null || userId.length == 0) {
			return new ArrayList<UserEmailInfo>();
		}
		ShifenEsbResult<DrmUserInfo[]> result = sfDrmDriverProxy
				.getUserInfoBatch(userId, getHeaders());
		if (result == null || result.getFlag() != SfDrmDriver.RESULT_FLAG_OK
				|| result.getData() == null) {
			LOG.error("获取用户" + userId + "信息发生错误");
			return new ArrayList<UserEmailInfo>(0);
		}
		ArrayList<UserEmailInfo> list = new ArrayList<UserEmailInfo>(0);
		UserEmailInfo emailInfo = null;
		for (DrmUserInfo info : result.getData()) {
			emailInfo = new UserEmailInfo();
			emailInfo.setUserId(info.getUserid());
			emailInfo.setRealname(info.getRealname());
			emailInfo.setEmail(info.getEmail());
			list.add(emailInfo);
		}
		return list;
	}

	/**
	 * @return the encoding
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * @param encoding
	 *            the encoding to set
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	private int getSumOfOutUcidsManagedFromUc(final int userId){
		/**
		 * modified by hanxu 2011-01-17, cpweb-207  1.0.48   
		 * 修复bug：为网盟专员添加账户时出现丢户（UC主从库同步延迟导致）
		 */
		final String[] ucGroupTypeArr = getUcGroupType(userId);
		
		Integer count = drucDriver.getSumOfOutUcidsManaged(userId,
				ucGroupTypeArr,
				BeidouConfig.UC_POST_APPID);
		if (count == null || count <= 0) {
			return 0;
		}
		return count;
	}



	public int[] getOutUcidsManagedFromUc(final int userId) {
		int count = getSumOfOutUcidsManagedFromUc(userId);
		if (count <= 0) {
			return new int[0];
		}
		HashSet<Integer> result = new HashSet<Integer>(count);
		for (int index = 0; index < count; index += MAX_COUNT_ONCE) {
			final int offset = index;
			final int limit;
			if (offset + MAX_COUNT_ONCE > count) {
				limit = count - offset;
			} else {
				limit = MAX_COUNT_ONCE;
			}
			int[] curUserIds = getOutUcidsManagerdByPageFromUc(userId, offset, limit);
			CollectionUtils.addAll(result, ArrayUtils.toObject(curUserIds));
		}
		return ArrayUtils.toPrimitive(result.toArray(new Integer[0]));
	}


	private int[] getOutUcidsManagerdByPageFromUc(final int userId, final int offset, final int limit){
		/**
		 * modified by hanxu 2011-01-17, cpweb-207  1.0.48   
		 * 修复bug：为网盟专员添加账户时出现丢户（UC主从库同步延迟导致）
		 */
		final String[] ucGroupTypeArr = getUcGroupType(userId);
		
		int[] userList = drucDriver.getOutUcidsManaged(
							userId,
							ucGroupTypeArr,
							BeidouConfig.UC_POST_APPID, limit,
							offset);
		if(userList == null){
			return new int[0];
		}
		return  userList;
	}
	


	public GlobalAuditerListCache getAllAuditers() {
		BeidouMemcacheClient client = BeidouCacheInstance.getInstance();
		GlobalAuditerListCache auditList = (GlobalAuditerListCache) client
				.memcacheGet(UserCacheConstant.GLOBAL_AUDITER_TYPE_LIST);
		if (auditList == null) {
			return getAllAuditerFromUc();
		}
		return auditList;
	}
	
	public Map<Integer, String[]> getUserRolesBatch(final int[] userIds) {
		if (userIds == null || userIds.length == 0) {
			return new HashMap<Integer, String[]>();
		}
		Map<Integer, String[]> userRoleMap = new HashMap<Integer, String[]>(
				userIds.length);

		int fromIndex = 0;
		for (; fromIndex < userIds.length; fromIndex += USER_ROLE_PAGE_SIZE) {
			final int start = fromIndex;
			UcRole[] result = drucDriver.getRoleTagByUcids(ArrayUtils
					.subarray(userIds, start, start + USER_ROLE_PAGE_SIZE),
					BeidouConfig.UC_POST_APPID);
			if (result == null) {
				continue;
			}
			for (UcRole role : result) {
				String[] userRole = userRoleMap.get(role.getUcid());
				String[] bdRole = MappingUtil.getBdRoles(new String[] { role
						.getRoletag() });
				if (bdRole != null && bdRole.length != 0) {
					userRole = (String[]) ArrayUtils.addAll(userRole, bdRole);
					userRoleMap.put(role.getUcid(), userRole);
				}
			}
		}

		return userRoleMap;
	}

	public Map<Integer, String> getUserTradeFromDrm(final int[] userids) {
		Map<Integer, String> map = new HashMap<Integer, String>();

		if (userids == null || userids.length == 0) {
			return map;
		}
		ShifenEsbResult<DrmUserTrade[]> result = sfDrmDriverProxy.getUserTrade(userids, getHeaders());
		if (result == null || result.getFlag() != SfDrmDriver.RESULT_FLAG_OK
				|| result.getData() == null) {
			LOG.error("获取用户" + userids + "信息发生错误");
			return map;
		}

		for (DrmUserTrade info : result.getData()) {
			if (info.getT1_name() != null && info.getT2_name() != null) {
				map.put(info.getUserId(), info.getT2_name() + "-"
						+ info.getT1_name());
			} else if (info.getT1_name() != null) {
				map.put(info.getUserId(), info.getT1_name());
			} else if (info.getT2_name() != null) {
				map.put(info.getUserId(), info.getT2_name());
			}

		}

		return map;
	}
	
	/**
	 * 判断userId的角色，如果是网盟专员则取beidou + shifen分组，否则取shifen分组
	 * 
	 * * modified by hanxu 2011-01-17, cpweb-207  1.0.48   
	 * 修复bug：为网盟专员添加账户时出现丢户（UC主从库同步延迟导致）
	 * 
	 * @param userId
	 * @return
	 */
	private String[] getUcGroupType(int userId){
		
		// 获取userid的role信息
		String[] result = getUserRoles(userId);
		
		// 判断是beidou分组还是shifen分组
		String ucGroupStr = BeidouConfig.UC_SHIFEN_GROUPTYPE;
		if(!ArrayUtils.isEmpty(result) && ArrayUtils.contains(result, UserConstant.USER_ROLE_WANGMENG)){
			ucGroupStr = BeidouConfig.UC_BEIDOU_GROUPTYPE + "," + BeidouConfig.UC_SHIFEN_GROUPTYPE;
		}
		
		return ucGroupStr.split(",");
	}

	/**
	 * @param user_type_heavy_expire
	 *            the uSER_TYPE_HEAVY_EXPIRE to set
	 */
	public void setUSER_TYPE_HEAVY_EXPIRE(int user_type_heavy_expire) {
		USER_TYPE_HEAVY_EXPIRE = user_type_heavy_expire;
	}

	/**
	 * @param auditer_type_list_expire
	 *            the aUDITER_TYPE_LIST_EXPIRE to set
	 */
	public void setAUDITER_TYPE_LIST_EXPIRE(int auditer_type_list_expire) {
		AUDITER_TYPE_LIST_EXPIRE = auditer_type_list_expire;
	}

	/**
	 * @param auditer_name_expire
	 *            the aUDITER_NAME_EXPIRE to set
	 */
	public void setAUDITER_NAME_EXPIRE(int auditer_name_expire) {
		AUDITER_NAME_EXPIRE = auditer_name_expire;
	}


	/**
	 * @return the mapper
	 */
	public Mapper getMapper() {
		return mapper;
	}

	/**
	 * @param mapper
	 *            the mapper to set
	 */
	public void setMapper(Mapper mapper) {
		this.mapper = mapper;
	}

	public DrucDriver getDrucDriverProxy() {
		return drucDriver;
	}

	public void setDrucDriver(DrucDriver drucDriver) {
		this.drucDriver = drucDriver;
	}

	public SfDrmDriverProxy getSfDrmDriverProxy() {
		return sfDrmDriverProxy;
	}

	public void setSfDrmDriverProxy(SfDrmDriverProxy sfDrmDriverProxy) {
		this.sfDrmDriverProxy = sfDrmDriverProxy;
	}

	public int getMAX_COUNT_ONCE() {
		return MAX_COUNT_ONCE;
	}

	public int getUSER_ROLE_PAGE_SIZE() {
		return USER_ROLE_PAGE_SIZE;
	}

	public int getUSER_OP_AUTH_EXPIRE() {
		return USER_OP_AUTH_EXPIRE;
	}

	public int getAUDITER_TYPE_LIST_EXPIRE() {
		return AUDITER_TYPE_LIST_EXPIRE;
	}

	public int getAUDITER_NAME_EXPIRE() {
		return AUDITER_NAME_EXPIRE;
	}

	public int getUSER_TYPE_HEAVY_EXPIRE() {
		return USER_TYPE_HEAVY_EXPIRE;
	}

}
