package com.baidu.beidou.tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cprogroup.dao.CproGroupDaoOnMultiDataSource;
import com.baidu.beidou.tool.mail.SnapMailManager;
import com.baidu.beidou.user.bo.User;
import com.baidu.beidou.user.dao.UserDao;
import com.baidu.beidou.user.service.UserInfoMgr;
import com.baidu.beidou.user.vo.UserEmailInfo;
import com.baidu.beidou.util.ServiceLocator;

/**
 * 接受待删除地域id传（,分隔），查询涉及到用户和推广组信息，邮件通知，手工执行
 * 
 * @author chenlu
 * 
 */
public class SendDeletedAreaMailToUser {
	private static final Log log = LogFactory.getLog(SendDeletedAreaMailToUser.class);

	public static class DataRow {
		public String groupName;
		public String planName;
		public Integer regionNum;
	}

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Error argument");
			return;
		}
		log.info(args[0]);
		log.info(args[1]);

		String regList = args[0];
		String upgradeTime = args[1].replace("#", "年").replace("@", "月").replace("%", "日");

		String[] fn = new String[] { "applicationContext.xml", "classpath:/com/baidu/beidou/tool/applicationContext.xml", "classpath:/com/baidu/beidou/user/applicationContext.xml" };
		CproGroupDaoOnMultiDataSource groupDaoOnMultiDataSource = (CproGroupDaoOnMultiDataSource) ServiceLocator.getInstance(fn).factory.getBean("cproGroupDaoOnMultiDataSource");
		SnapMailManager mailManager = (SnapMailManager) ServiceLocator.getInstance(fn).factory.getBean("mailManager");
		UserDao userDao = (UserDao) ServiceLocator.getInstance(fn).factory.getBean("userDao");
		UserInfoMgr userInfoMgr = (UserInfoMgr) ServiceLocator.getInstance(fn).factory.getBean("userInfoMgr");

		// 记录用户id，groupid，设置信息的结果
		Map<Integer, Map<Integer, DataRow>> result = new HashMap<Integer, Map<Integer, DataRow>>();

		DataRow r = null;
		Integer userid = 0;
		Integer groupid = 0;
		for (String regId : regList.split(",")) {
			List<Map<String, Object>> res = groupDaoOnMultiDataSource.findGroupByRegId(Integer.parseInt(regId));
			for (Map<String, Object> resultRow : res) {
				userid = Integer.parseInt(resultRow.get("userid").toString());
				groupid = Integer.parseInt(resultRow.get("groupid").toString());

				if (result.get(userid) != null) {
					Map<Integer, DataRow> gRow = result.get(userid);

					if (gRow.get(groupid) != null) {
						gRow.get(groupid).regionNum -= 1;

					} else {
						r = new DataRow();
						r.planName = resultRow.get("planname").toString();
						r.groupName = resultRow.get("groupname").toString();
						String strReg = resultRow.get("reglist").toString();
						r.regionNum = strReg.split("\\|").length - 1;
						gRow.put(groupid, r);

					}

					result.put(userid, gRow);

				} else {
					r = new DataRow();
					r.planName = resultRow.get("planname").toString();
					r.groupName = resultRow.get("groupname").toString();
					String strReg = resultRow.get("reglist").toString();
					r.regionNum = strReg.split("\\|").length - 1;
					Map<Integer, DataRow> gRow = new HashMap<Integer, DataRow>();
					gRow.put(groupid, r);
					result.put(userid, gRow);
				}
			}
		}

		// Map<Integer, DataRow> gRow2 = result.get(574);

		// 发送邮件
		for (Integer uId : result.keySet()) {
			Map<Integer, DataRow> gRow = result.get(uId);

			UserEmailInfo info = userInfoMgr.getEmailInfo(uId);

			String userName = (info == null ? "" : info.getRealname());
			User accountInfo = userDao.findUserBySFId(uId);

			Map<String, Object> content = new HashMap<String, Object>();
			content.put("username", userName);
			String account = (accountInfo == null ? "" : accountInfo.getUsername());
			content.put("account", account);
			content.put("upgradeTime", upgradeTime);

			StringBuilder strMail1 = new StringBuilder();
			StringBuilder strMail2 = new StringBuilder();
			for (Integer gId : gRow.keySet()) {
				DataRow row = gRow.get(gId);
				if (row.regionNum > 0) {// 部分变化
					strMail1.append("<tr><td>").append(row.planName).append("</td><td>").append(row.groupName).append("</td></tr>");
				} else {
					strMail2.append("<tr><td>").append(row.planName).append("</td><td>").append(row.groupName).append("</td></tr>");
				}
			}

			String mailContent = "";
			if (info != null) {
				String email = info.getEmail();

				log.info("sending->userid:" + uId + " email:" + email);
				if (strMail1.length() != 0) {
					content.put("groupRow", strMail1);
					mailContent = mailManager.getHtmlMailContent("deleteAreaMail.ftl", content);
					mailManager.sendWarningMail("百度网盟推广帐户" + account + "地域设置升级通知", mailContent, email);
				}

				if (strMail2.length() != 0) {
					content.put("groupRow", strMail2);
					mailContent = mailManager.getHtmlMailContent("deleteAreaMail2.ftl", content);
					mailManager.sendWarningMail("百度网盟推广帐户" + account + "地域为空通知", mailContent, email);
				}
			}
		}
	}
}
