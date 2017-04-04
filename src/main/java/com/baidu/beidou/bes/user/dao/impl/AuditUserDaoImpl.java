package com.baidu.beidou.bes.user.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.derby.client.am.Types;

import com.baidu.beidou.bes.user.constant.UserConstant;
import com.baidu.beidou.bes.user.dao.AuditUserDao;
import com.baidu.beidou.bes.user.po.AuditUserInfo;
import com.baidu.beidou.util.dao.GenericDaoImpl;
import com.baidu.beidou.util.dao.GenericRowMapping;
/**
 * one_adx库DAO
 * 
 * @author caichao
 */
public class AuditUserDaoImpl extends GenericDaoImpl implements AuditUserDao {
	private static final Log log = LogFactory.getLog(AuditUserDaoImpl.class);
	
	private GenericRowMapping<AuditUserInfo> auditUserMapping = new GenericRowMapping<AuditUserInfo>(){
		public AuditUserInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
			AuditUserInfo info = new AuditUserInfo();
			info.setUserId(rs.getInt("userid"));
			info.setName(rs.getString("name"));
			info.setCompany(rs.getInt("company"));
			info.setUrl(rs.getString("url"));
			info.setMemo(rs.getString("memo"));
			return info;
		}
	};
	
	private GenericRowMapping<AuditUserInfo> userResultMapping = new GenericRowMapping<AuditUserInfo>() {
		
		@Override
		public AuditUserInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
			AuditUserInfo info = new AuditUserInfo();
			info.setUserId(rs.getInt("userid"));
			info.setName(rs.getString("name"));
			info.setCompany(rs.getInt("company"));
			return info;
		}
	};
	
	
	@Override
	public List<AuditUserInfo> getAuditUserList(Integer company) {
		
		String sql = "select userid,name,company,url,memo from one_adx.audituser where company=? and audit_status=?";
		List<AuditUserInfo> infoList = super.findBySql(auditUserMapping, sql, new Object[]{company,1}, new int[]{Types.INTEGER,Types.INTEGER});
		
		return infoList;
	}

	

	@Override
	public void updateAuditStatus(List<AuditUserInfo> users,Integer company,Integer auditStatus) {
		if (CollectionUtils.isEmpty(users)){
			return;
		}
		StringBuffer  sql = new StringBuffer();
		sql.append("update one_adx.audituser")
		   .append(" set audit_status =?,mtime=now() where company = ? and userid in(");
		
		for (AuditUserInfo info : users) {
			sql.append(info.getUserId()).append(",");
		}
		sql.deleteCharAt(sql.length()-1).append(")");
		
		super.executeBySql(sql.toString(), new Object[]{auditStatus,company},
				new int[]{Types.INTEGER,Types.INTEGER});
		
	}
	
	
	
	@Override
	public List<AuditUserInfo> getHasPushedUser(Integer company) {
		String sql = "select userid,name,company from audituser"
		+ " where company=? and audit_status in (?,?)";
		
		List<AuditUserInfo> infoList = super.findBySql(userResultMapping, sql, new Object[]{company,UserConstant.AUDITING,UserConstant.PUSH_AUDIT_FAIL}, new int[]{Types.INTEGER,Types.INTEGER,Types.INTEGER});
		
		return infoList;
	}

	@Override
	public void insertAuditUser(List<AuditUserInfo> users) {
		StringBuffer sql = new StringBuffer();
		sql.append("insert into one_adx.audituser")
		.append("(userid,name,url,memo,company,audit_status,error_code,reason,ctime,mtime) values");
		for (AuditUserInfo info : users) {
			sql.append("(")
				.append(info.getUserId()).append(",")
				.append("'"+info.getName()+"'").append(",")
				.append("'"+info.getUrl()+"'").append(",")
				.append("'"+info.getMemo()+"'").append(",")
				.append(info.getCompany()).append(",")
				.append(1).append(",")
				.append(0).append(",")
				.append("'"+info.getReason()+"'").append(",")
				.append("now()").append(",")
				.append("now()")
				.append("),");
			
		}
		sql = sql.deleteCharAt(sql.length()-1);
		super.executeBySql(sql.toString(), null);
	}
	
	//update 数据少，此处就一条条执行
	@Override
	public void updateAuditUser(List<AuditUserInfo> users) {
		String sql = "update one_adx.audituser set name=?,url=?,memo=?," +
				"audit_status=?,error_code=?,reason=?,mtime=now() where userid=? and company=?";
		for (AuditUserInfo info : users) {
			super.executeBySql(sql.toString(), new Object[]{info.getName(),info.getUrl(),info.getMemo()
				,1,info.getErrorCode(),info.getReason(),info.getUserId(),info.getCompany()}, 
				new int[]{Types.VARCHAR,Types.VARCHAR,Types.VARCHAR,Types.INTEGER,Types.INTEGER,Types.VARCHAR,Types.INTEGER,Types.INTEGER});
		}
	}

	@Override
	public void updateAuditStatus(Map<Integer, List<Integer>> failMap,Integer company, int auditStatus) {
		for (Map.Entry<Integer,List<Integer>> entry : failMap.entrySet()) {
			StringBuffer sql = new StringBuffer();
			sql.append("update one_adx.audituser")
			.append(" set audit_status =").append(auditStatus)
			.append(" , error_code =").append(entry.getKey())
			.append(",reason ='").append(UserConstant.errorCodeMap.get(entry.getKey()))
			.append("',mtime=now()")
			.append(" where company = ").append(company)
			.append(" and userid in(");
			for (Integer userid : entry.getValue()) {
				sql.append(userid).append(",");
			}
		
			sql.deleteCharAt(sql.length()-1).append(")");
			super.executeBySql(sql.toString(),null,null);
		}
	}

	@Override
	public void updateAuditPass(List<Integer> userIds,Integer company, Integer auditStatus) {
		StringBuffer  sql = new StringBuffer();
		sql.append("update one_adx.audituser")
		   .append(" set audit_status =?,error_code=0,reason='',mtime=now() where company = ? and userid in(");
		
		for (Integer userid : userIds) {
			sql.append(userid).append(",");
		}
		
		sql.deleteCharAt(sql.length()-1).append(")");
		
		super.executeBySql(sql.toString(), new Object[]{auditStatus,company},
				new int[]{Types.INTEGER,Types.INTEGER});
	}

	@Override
	public void updateAuditUnPass(Map<Integer, String> map,Integer company, Integer auditStatus) {
		String sql = "update one_adx.audituser  set audit_status=?" +
				",reason=?,error_code=0,mtime=now() where userid=? and company=?";
		for (Map.Entry<Integer, String> entry : map.entrySet()) {
			super.executeBySql(sql, new Object[]{auditStatus,entry.getValue(),entry.getKey(),company},
					new int[]{Types.INTEGER,java.sql.Types.VARCHAR,Types.INTEGER,Types.INTEGER});
		}
	}
}
