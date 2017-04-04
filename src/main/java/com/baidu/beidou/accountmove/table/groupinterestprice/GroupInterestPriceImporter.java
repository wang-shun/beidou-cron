package com.baidu.beidou.accountmove.table.groupinterestprice;

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

public class GroupInterestPriceImporter implements TableImporter{

//	private static final Log logger = LogFactory.getLog(AttachInfoImporter.class);
	
	private FileExporter fexporter;
	private DataAccessService dataAccessService;
	private KeyMapper keyMapper;
	
	@Override
	public void executeImport(int userId) {
		
		// read cprogroup file, for each line
		List<List<String>> groupitList = fexporter.importIn(getTableName(), userId);
		if (CollectionUtils.isEmpty(groupitList)) {
			return;
		}
		
		for (List<String> groupit : groupitList) {
			String oldsmartid = groupit.get(StringUtil.getIndex("gip_id", getColumnNameArray()));
			String oldGroupid = groupit.get(StringUtil.getIndex("groupid", getColumnNameArray()));
			String oldPlanid = groupit.get(StringUtil.getIndex("planid", getColumnNameArray())); 
			String oldUserid = groupit.get(StringUtil.getIndex("userid", getColumnNameArray()));
			
			// query primary key to make sure key not processed; 
			if (keyMapper.getMappedKey(KeyMapper.GROUP_INTEREST_PRICE_ID, oldsmartid, userId) != null) {
				continue;
			}
			
			// deal with iid, if type=1, means iid is packid, need exchange;
			String oldType = groupit.get(StringUtil.getIndex("type", getColumnNameArray()));
			if ("1".equals(oldType)) {
			    String oldIid = groupit.get(StringUtil.getIndex("iid", getColumnNameArray()));
			    String newIid = keyMapper.getMappedKey(KeyMapper.CUSTOMINTEREST_ID,
			            oldIid, Integer.valueOf(oldUserid));
			    groupit.set(StringUtil.getIndex("iid", getColumnNameArray()), newIid);
			}
			
			// generate a primary key, save primary key map, exchange primary key, generate a insert sql, execute insert
			
			String newUserId = keyMapper.getMappedKey(KeyMapper.USERID,
					oldUserid, Integer.valueOf(oldUserid));
			groupit.set(StringUtil.getIndex("userid", getColumnNameArray()), newUserId);
			
			String newPlanId = keyMapper.getMappedKey(KeyMapper.PLANID,
					oldPlanid, Integer.valueOf(oldUserid));
			groupit.set(StringUtil.getIndex("planid", getColumnNameArray()), newPlanId);
			
			String newGroupId = keyMapper.getMappedKey(KeyMapper.GROUPID,
					oldGroupid, Integer.valueOf(oldUserid));
			groupit.set(StringUtil.getIndex("groupid", getColumnNameArray()), newGroupId);
			
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
				params[i] = groupit.get(i + 1);
			}
			String newSmartid = dataAccessService.updateInfo(sql, params, Integer.parseInt(newUserId));
			if (newSmartid != null) {
				keyMapper.addKeyMap(KeyMapper.GROUP_INTEREST_PRICE_ID, oldsmartid, newSmartid, Integer.valueOf(oldUserid));
			}
		}
		
	}

	@Override
	public String getTableName() {
		return TableSchemaInfo.GROUP_INTEREST_PRICE;
	}

	public String[] getColumnNameArray() {
		return TableSchemaInfo.group_interest_price;
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