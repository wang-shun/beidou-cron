package com.baidu.beidou.accountmove.table.appexclude;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.baidu.beidou.accountmove.dao.DataAccessService;
import com.baidu.beidou.accountmove.keymap.KeyMapper;
import com.baidu.beidou.accountmove.table.TableImporter;
import com.baidu.beidou.accountmove.table.TableSchemaInfo;
import com.baidu.beidou.accountmove.util.FileExporter;
import com.baidu.beidou.accountmove.util.StringUtil;
import com.baidu.beidou.accountmove.util.TableNameUtil;

public class AppExcludeImporter implements TableImporter{

//	private static final Log logger = LogFactory.getLog(AttachInfoImporter.class);
	
	private FileExporter fexporter;
	private DataAccessService dataAccessService;
	private KeyMapper keyMapper;
	
	@Override
	public void executeImport(int userId) {
		
		// read cprogroup file, for each line
		List<List<String>> appExcludeList = fexporter.importIn(getTableName(), userId);
		if (CollectionUtils.isEmpty(appExcludeList)) {
			return;
		}
		
		for (List<String> appEx : appExcludeList) {
			String oldAppExid = appEx.get(StringUtil.getIndex("id", getColumnNameArray()));
			String oldGroupid = appEx.get(StringUtil.getIndex("groupid", getColumnNameArray()));
			String oldPlanid = appEx.get(StringUtil.getIndex("planid", getColumnNameArray())); 
			String oldUserid = appEx.get(StringUtil.getIndex("userid", getColumnNameArray()));
			
			// query primary key to make sure key not processed; 
			if (keyMapper.getMappedKey(KeyMapper.APP_EXCLUDE_ID, oldAppExid, userId) != null) {
				continue;
			}
			
			// generate a primary key, save primary key map, exchange primary key, generate a insert sql, execute insert
			
			String newUserId = keyMapper.getMappedKey(KeyMapper.USERID,
					oldUserid, Integer.valueOf(oldUserid));
			appEx.set(StringUtil.getIndex("userid", getColumnNameArray()), newUserId);
			
			String newPlanId = keyMapper.getMappedKey(KeyMapper.PLANID,
					oldPlanid, Integer.valueOf(oldUserid));
			appEx.set(StringUtil.getIndex("planid", getColumnNameArray()), newPlanId);
			
			String newGroupId = keyMapper.getMappedKey(KeyMapper.GROUPID,
					oldGroupid, Integer.valueOf(oldUserid));
			appEx.set(StringUtil.getIndex("groupid", getColumnNameArray()), newGroupId);
			
//			// itid get from sequence
//			String newGenerateUnitId = keyMapper.getMappedKey(KeyMapper.UNITID, oldAttachInfoid, userId);
//			groupit.set(StringUtil.getIndex("id", getColumnNameArray()), newGenerateUnitId);
			
			String[] colNames = Arrays.copyOfRange(getColumnNameArray(), 1,
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
				params[i] = appEx.get(i + 1);
			}
			String newAppExid = dataAccessService.updateInfo(sql, params, Integer.parseInt(newUserId));
			if (newAppExid != null) {
				keyMapper.addKeyMap(KeyMapper.APP_EXCLUDE_ID, oldAppExid, newAppExid, Integer.valueOf(oldUserid));
			}
		}
		
	}

	@Override
	public String getTableName() {
		return TableSchemaInfo.APP_EXCLUDE;
	}

	public String[] getColumnNameArray() {
		return TableSchemaInfo.app_exclude;
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
