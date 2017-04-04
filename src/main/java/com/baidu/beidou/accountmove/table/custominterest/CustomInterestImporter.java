package com.baidu.beidou.accountmove.table.custominterest;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.baidu.beidou.accountmove.dao.DataAccessService;
import com.baidu.beidou.accountmove.dao.IDService;
import com.baidu.beidou.accountmove.keymap.KeyMapper;
import com.baidu.beidou.accountmove.table.TableImporter;
import com.baidu.beidou.accountmove.table.TableSchemaInfo;
import com.baidu.beidou.accountmove.util.FileExporter;
import com.baidu.beidou.accountmove.util.StringUtil;
import com.baidu.beidou.accountmove.util.TableNameUtil;

public class CustomInterestImporter implements TableImporter{

	private FileExporter fexporter;
	private DataAccessService dataAccessService;
	private KeyMapper keyMapper;
	private IDService idService;
	
	@Override
	public void executeImport(int userId) {
		
		// read cprogroup file, for each line
		List<List<String>> groupitList = fexporter.importIn(getTableName(), userId);
		if (CollectionUtils.isEmpty(groupitList)) {
			return;
		}
		
		for (List<String> groupit : groupitList) {
			String oldItid = groupit.get(StringUtil.getIndex("cid", getColumnNameArray()));
//			String oldGroupid = groupit.get(StringUtil.getIndex("groupid", getColumnNameArray()));
//			String oldPlanid = groupit.get(StringUtil.getIndex("planid", getColumnNameArray())); 
			String oldUserid = groupit.get(StringUtil.getIndex("userid", getColumnNameArray()));
			
			// query primary key to make sure key not processed; 
			if (keyMapper.getMappedKey(KeyMapper.CUSTOMINTEREST_ID, oldItid, userId) != null) {
				continue;
			}
			
			// generate a primary key, save primary key map, exchange primary key, generate a insert sql, execute insert
			
			String newUserId = keyMapper.getMappedKey(KeyMapper.USERID,
					oldUserid, Integer.valueOf(oldUserid));
			groupit.set(StringUtil.getIndex("userid", getColumnNameArray()), newUserId);
			
			// itid get from sequence
			long newGenerateItId = idService.generateKeys(Integer.valueOf(newUserId), "userpack", 1);
			groupit.set(StringUtil.getIndex("cid", getColumnNameArray()), String.valueOf(newGenerateItId));
			
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
				params[i] = groupit.get(i);
			}
			String newItId = dataAccessService.updateInfo(sql, params, Integer.parseInt(newUserId));
			if (newItId != null) {
				keyMapper.addKeyMap(KeyMapper.CUSTOMINTEREST_ID, oldItid, newItId, Integer.valueOf(oldUserid));
			} else {
				keyMapper.addKeyMap(KeyMapper.CUSTOMINTEREST_ID, oldItid, String.valueOf(newGenerateItId), Integer.valueOf(oldUserid));
			}
		}
		
	}

	@Override
	public String getTableName() {
		return TableSchemaInfo.CUSTOMINTEREST;
	}

	public String[] getColumnNameArray() {
		return TableSchemaInfo.custominterest;
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

	public IDService getIdService() {
		return idService;
	}

	public void setIdService(IDService idService) {
		this.idService = idService;
	}

}
