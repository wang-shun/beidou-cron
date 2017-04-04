package com.baidu.beidou.accountmove.table.cprogroupatleft;

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

public class CprogroupAtleftImporter implements TableImporter{

//	private static final Log logger = LogFactory.getLog(CprogroupAtrightImporter.class);
	
	private FileExporter fexporter;
	private DataAccessService dataAccessService;
	private KeyMapper keyMapper;
	private IDService idService;
	
	@Override
	public void executeImport(int userId) {
		
		// read cprogroup file, for each line
		List<List<String>> groupatList = fexporter.importIn(getTableName(), userId);
		if (CollectionUtils.isEmpty(groupatList)) {
			return;
		}
		
		for (List<String> groupat : groupatList) {
			String oldAtid = groupat.get(StringUtil.getIndex("atleftid", getColumnNameArray()));
			String oldGroupid = groupat.get(StringUtil.getIndex("groupid", getColumnNameArray()));
			String oldPlanid = groupat.get(StringUtil.getIndex("planid", getColumnNameArray())); 
			String oldUserid = groupat.get(StringUtil.getIndex("userid", getColumnNameArray()));
			
			// query primary key to make sure key not processed; 
			if (keyMapper.getMappedKey(KeyMapper.AT_LEFTID, oldAtid, userId) != null) {
				continue;
			}
			
			// generate a primary key, save primary key map, exchange primary key, generate a insert sql, execute insert
			
			String newUserId = keyMapper.getMappedKey(KeyMapper.USERID,
					oldUserid, Integer.valueOf(oldUserid));
			groupat.set(StringUtil.getIndex("userid", getColumnNameArray()), newUserId);
			
			String newPlanId = keyMapper.getMappedKey(KeyMapper.PLANID,
					oldPlanid, Integer.valueOf(oldUserid));
			groupat.set(StringUtil.getIndex("planid", getColumnNameArray()), newPlanId);
			
			String newGroupId = keyMapper.getMappedKey(KeyMapper.GROUPID,
					oldGroupid, Integer.valueOf(oldUserid));
			groupat.set(StringUtil.getIndex("groupid", getColumnNameArray()), newGroupId);
			
			// generate new groupid, and replace old groupid
			long newGenerateAtleftId = idService.generateKeys(Integer.valueOf(newUserId), "cprogroupatleft", 1);
			groupat.set(StringUtil.getIndex("atleftid", getColumnNameArray()),
					String.valueOf(newGenerateAtleftId));
			
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
				params[i] = groupat.get(i);
			}
			String newAtId = dataAccessService.updateInfo(sql, params, Integer.parseInt(newUserId));
			if (newAtId != null) {
				keyMapper.addKeyMap(KeyMapper.AT_LEFTID, oldAtid, newAtId, Integer.valueOf(oldUserid));
			}else {
				keyMapper.addKeyMap(KeyMapper.AT_LEFTID, oldAtid, String.valueOf(newGenerateAtleftId), Integer.valueOf(oldUserid));
			}
		}
		
	}

	@Override
	public String getTableName() {
		return TableSchemaInfo.CPROGROUP_ATLEFT;
	}

	public String[] getColumnNameArray() {
		return TableSchemaInfo.cprogroup_atleft;
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
