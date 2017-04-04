package com.baidu.beidou.accountmove.table.groupatrightinfo;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.accountmove.dao.DataAccessService;
import com.baidu.beidou.accountmove.keymap.KeyMapper;
import com.baidu.beidou.accountmove.table.TableImporter;
import com.baidu.beidou.accountmove.table.TableSchemaInfo;
import com.baidu.beidou.accountmove.util.FileExporter;
import com.baidu.beidou.accountmove.util.StringUtil;
import com.baidu.beidou.accountmove.util.TableNameUtil;

public class GroupAtrightInfoImporter implements TableImporter{

	private static final Log logger = LogFactory.getLog(GroupAtrightInfoImporter.class);
	
	private FileExporter fexporter;
	private DataAccessService dataAccessService;
	private KeyMapper keyMapper;
	
	@Override
	public void executeImport(int userId) {
		
		// read cprogroup file, for each line
		List<List<String>> groupatinfoList = fexporter.importIn(getTableName(), userId);
		if (CollectionUtils.isEmpty(groupatinfoList)) {
			return;
		}
		
		for (List<String> groupatinfo : groupatinfoList) {
			String oldGroupid = groupatinfo.get(StringUtil.getIndex("groupid", getColumnNameArray()));
			String oldPlanid = groupatinfo.get(StringUtil.getIndex("planid", getColumnNameArray())); 
			String oldUserid = groupatinfo.get(StringUtil.getIndex("userid", getColumnNameArray()));
			
			// query primary key to make sure key not processed; 
			if (keyMapper.getMappedKey(KeyMapper.GROUPID, oldGroupid, userId) == null) {
				continue;
			}
			
			// generate a primary key, save primary key map, exchange primary key, generate a insert sql, execute insert
			
			String newUserId = keyMapper.getMappedKey(KeyMapper.USERID,
					oldUserid, Integer.valueOf(oldUserid));
			groupatinfo.set(StringUtil.getIndex("userid", getColumnNameArray()), newUserId);
			
			String newPlanId = keyMapper.getMappedKey(KeyMapper.PLANID,
					oldPlanid, Integer.valueOf(oldUserid));
			groupatinfo.set(StringUtil.getIndex("planid", getColumnNameArray()), newPlanId);
			
			String newGroupId = keyMapper.getMappedKey(KeyMapper.GROUPID,
					oldGroupid, Integer.valueOf(oldUserid));
			groupatinfo.set(StringUtil.getIndex("groupid", getColumnNameArray()), newGroupId);
			
			String[] colNames = Arrays.copyOfRange(getColumnNameArray(), 0,
					getColumnNameArray().length);
			// insert into beidou.cproplan (planname,...,plan_type)values(?,...,?);
			String sql = "insert into "
					+ TableNameUtil.getActualTableName(getTableName(), Integer.parseInt(newUserId)) + " ("
					+ StringUtil.getStringFromArray(colNames, ",")
					+ ") values("
					+ StringUtil.getRepetitionString("?", ",", colNames.length)
					+ ");";
			Object[] params = new Object[colNames.length];
			for (int i = 0; i < colNames.length; i++) {
				params[i] = groupatinfo.get(i);
			}
			String newGId = dataAccessService.updateInfo(sql, params, Integer.parseInt(newUserId));
			if (newGId == null) {
				logger.error("import at right info failed, old groupid is: " + oldGroupid);
			}
		}
		
	}

	@Override
	public String getTableName() {
		return TableSchemaInfo.GROUP_ATRIGHT_INFO;
	}

	public String[] getColumnNameArray() {
		return TableSchemaInfo.group_atright_info;
	}
	
	public FileExporter getFexporter() {
		return fexporter;
	}

	public void setFexporter(FileExporter fexporter) {
		this.fexporter = fexporter;
	}

	public DataAccessService getDataAccessService() {
		return dataAccessService;
	}

	public void setDataAccessService(DataAccessService dataAccessService) {
		this.dataAccessService = dataAccessService;
	}

	public KeyMapper getKeyMapper() {
		return keyMapper;
	}

	public void setKeyMapper(KeyMapper keyMapper) {
		this.keyMapper = keyMapper;
	}

}
