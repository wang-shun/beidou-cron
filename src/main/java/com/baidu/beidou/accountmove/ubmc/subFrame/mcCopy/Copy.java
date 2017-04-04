package com.baidu.beidou.accountmove.ubmc.subFrame.mcCopy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.baidu.beidou.accountmove.ubmc.conf.FileConfig;
import com.baidu.beidou.accountmove.ubmc.cprounit.service.UbmcServiceFactory;

public class Copy {
	
	static FileConfig config = null;

	/**
	 * 只有一个参数，即配置文件的位置
	 * @param args
	 */
	public static void main(String[] args) {

		if(args.length == 0){
			throw new RuntimeException("没有配置文件!");
		}
		
		config = new FileConfig(args[0]);
		String sUserId = config.getProperty("userIds");
		String succLog = config.getProperty("succLog");
		String failLog = config.getProperty("failLog");
		
		String[] ubmcServers = config.getPropertyAsArray("ubmc.servers");
		String ubmcServiceUrl = config.getProperty("ubmc.serviceUrl");
		String ubmcSysCode = config.getProperty("ubmc.sysCode");
		String ubmcProductId = config.getProperty("ubmc.prodId");
		int ubmcConnectionTimeout = config.getPropertyAsInt("ubmc.connectionTimeout");
		int ubmcReadTimeout = config.getPropertyAsInt("ubmc.readTimeout");
		
		//初始化CopyLog与UbmcService
		CopyLog.initCopyLog(succLog, failLog);
		UbmcServiceFactory.initProperties(ubmcServers, ubmcServiceUrl, ubmcSysCode, ubmcProductId, ubmcConnectionTimeout, ubmcReadTimeout);

		int[] userIds = getUserIds(sUserId);
		for (int i = 0; i < 8; i++) {
			copyByDB(i, userIds);
		}
		CopyLog.releaseMem();
	}

	private static int[] getUserIds(String sUserId) {
		sUserId = sUserId.trim();
		String [] sArray = sUserId.split(",");
		int[] userIds = new int[sArray.length];
		for(int i = 0 ; i < sArray.length ; i ++){
			userIds[i] = Integer.parseInt(sArray[i].trim());
		}
		return userIds;
	}

	private static void copyByDB(int dbIndex, int[] userIds) {
		for (int i = 0; i < 8; i++) {
			copyByTable(dbIndex, i, userIds);
		}
	}

	private static void copyByTable(int dbIndex, int tableIndex, int[] userIds) {
		List<MaterialKey> keys = get2CopyList(dbIndex, tableIndex, userIds);
		if(keys.size() == 0){
			System.out.println(" copy complete db" + dbIndex + " table unit"
					+ tableIndex);
			return;
		}
		for (MaterialKey key : keys) {
			MaterialCopy.copy(key);
		}
		System.out.println(" copy complete db" + dbIndex + " table unit"
				+ tableIndex);
	}

	private static List<MaterialKey> get2CopyList(int dbIndex, int tableIndex,
			int[] userIds) {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		List<MaterialKey> keys = new ArrayList<MaterialKey>(500000);
		try {
			conn = getConn(dbIndex);
			stmt = conn.prepareStatement(getSQL(dbIndex, tableIndex, userIds));
			rs = stmt.executeQuery();

			while (rs.next()) {

				long mcId = rs.getLong("mcid");
				int mcVersion = rs.getInt("mcversionid");
				int userId = rs.getInt("userid");
				int planId = rs.getInt("planid");
				int groupId = rs.getInt("groupid");
				int unitId = rs.getInt("unitid");

				keys.add(new MaterialKey(mcId, mcVersion, userId, planId,
						groupId, unitId));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
				}
			}
		}
		return keys;
	}

	// "select t1.id as unitId, t2.mcid as mcid, t2.mcversionid as mcversionid, t1.uid as userid, t1.pid as planid, t1.gid as groupid  from beidou.cprounitstate0 t1 join beidou.cprounitmater0 t2 on t1.id=t2.id where t1.uid>0 and t1.state!=2 limit 1;"
	private static String getSQL(int dbIndex, int tableIndex, int[] userIds) {
		StringBuilder sb = new StringBuilder();
		sb.append("select t1.id as unitId, t2.mcid as mcid, t2.mcversionid as mcversionid, t1.uid as userid, t1.pid as planid, t1.gid as groupid from beidou.cprounitstate");
		sb.append(tableIndex).append(" t1 join beidou.cprounitmater")
				.append(tableIndex)
				.append(" t2 on t1.id=t2.id where t1.uid in (");
		for (int userId : userIds) {
			if (userId % 8 != tableIndex) {
				continue;
			}

			if ((userId / 64) % 8 != dbIndex) {
				continue;
			}

			sb.append(userId).append(',');
		}
		sb.append("0)  and t1.state!=2");

		return sb.toString();
	}
	
	private static Connection getConn(int dbIndex) throws Exception {
		String user = config.getProperty("jdbc.user");
		String pass = config.getProperty("jdbc.pass");
		String url = getURL(dbIndex);
		Class.forName("com.mysql.jdbc.Driver");
		return DriverManager.getConnection(url, user, pass);
	}

	private static String getURL(int dbIndex) {
		return config.getProperty("jdbc.url" + dbIndex);
	}

}
