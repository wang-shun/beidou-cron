/**
 * PrivilegeConstant.java
 * 2008-6-27
 */
package com.baidu.beidou.user.constant;

/**
 * @author zengyunfeng
 *
 */
public class UserConstant {

	/**
	 * 普通广告主角色
	 */
	public static final int ROLE_NORMAL = 0;
	/**
	 * 销售管理员角色
	 */
	public static final int ROLE_SALER = 1;
	/**
	 * 审核管理员
	 */
	public static final int ROLE_AUDITER = 2;
	
	/**
	 * HttpSession中存放登录用户的key
	 */
	public static final String USER_KEY = "UserConstant.VISITOR";
	
	/**
	 * 用户状态：开通
	 */
	public static final int USER_STATE_NORMAL = 0;
	
	/**
	 * 用户状态：关闭
	 */
	public static final int USER_STATE_CLOSED = 1;
	
	/**
	 * 用户状态：删除
	 */
	public static final int USER_STATE_DELETED = 2;
	
	/**
	 * 用户显示状态： 账户开通
	 */
	public static final int USER_VIEW_STATE_NORMAL = 0;
	
	/**
	 * 用户显示状态: 金额为0
	 */
	public static final int USER_VIEW_STATE_ZERO = 1;
	
	/**
	 * 用户显示状态： 账户关闭
	 */
	public static final int USER_VIEW_STATE_CLOSED = 2;
	
	/**
	 * 用户显示状态: 删除
	 */
	public static final int USER_VIEW_STATE_DELETED = 3;
	
	public static final String [] USER_VIEW_STATE_NAME = {"账户开通","金额为0","账户关闭","账户关闭"};
	
	
	/**
	 * shifen用户状态：暂未生效
	 */
	public static final int SHIFEN_STATE_DISABLE = 1;
	
	/**
	 * shifen用户状态：正常生效
	 */
	public static final int SHIFEN_STATE_NORMAL = 2;
	
	/**
	 * shifen用户状态：账面为零
	 */
	public static final int SHIFEN_STATE_ZERO = 3;
	
	/**
	 * shifen用户状态：被拒绝
	 */
	public static final int SHIFEN_STATE_REFUSE = 4;
	
	/**
	 * shifen用户状态：需管理员审核
	 */
	public static final int SHIFEN_STATE_AUDITING = 6;
	
	/**
	 * shifen用户状态：被禁用
	 */
	public static final int SHIFEN_STATE_CLOSE = 7;
	
	
	/**
	 * 客户级别：普通客户
	 */
	public static final int USER_LEVEL_NORMAL = 10101;
	
	/**
	 * 客户级别：大客户
	 */
	public static final int USER_LEVEL_CLIENT = 10104;
	
	/**
	 * 测试用户
	 */
	public static final int USER_TYPE_TEST = 1;
	
	
	/**
	 * 分配审核任务权限
	 */
	public static final String PRIVILEGE_ASSIGN_AUDIT="Beidou_AssignAudit";
	
	/**
	 * 大客户审核员
	 */
	public static final String PRIVILEGE_AUDIT_HEAVY="Beidou_Audit_Heavy";
	
	/**
	 * 新用户角色
	 */
	public static final String USER_ROLE_SYS_MANAGER="BEIDOU_SYS_MANAGER";//系统管理员
	public static final String USER_ROLE_SALER_FIRST="BEIDOU_SALER_FIRST";//一级销售
	public static final String USER_ROLE_SALER_SECOND="BEIDOU_SALER_SECOND";//二级销售
	public static final String USER_ROLE_AUDITER="BEIDOU_AUDITER";//审核员
	public static final String USER_ROLE_SALER_SUPER="BEIDOU_SALER_SUPER";//超级销售
	public static final String USER_ROLE_CLIENT_ADMIN="BEIDOU_CLIENT_ADMIN";//一级客服管理员
	public static final String USER_ROLE_CUSTOMER_HEAVY="BEIDOU_CUSTOMER_HEAVY";//大客户
	public static final String USER_ROLE_CUSTOMER_VIP="BEIDOU_CUSTOMER_VIP";//vip客户
	public static final String USER_ROLE_CUSTOMER_NORMAL="BEIDOU_CUSTOMER_NORMAL";//普通客户
	public static final String USER_ROLE_WANGMENG="BEIDOU_WANGMENG";//网盟专员
}
