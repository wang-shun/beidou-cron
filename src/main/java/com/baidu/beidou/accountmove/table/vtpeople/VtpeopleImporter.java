package com.baidu.beidou.accountmove.table.vtpeople;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

public class VtpeopleImporter implements TableImporter{

	private FileExporter fexporter;
	private DataAccessService dataAccessService;
	private KeyMapper keyMapper;
	private IDService idService;
	
	@Override
	public void executeImport(int userId) {
		
		// read cprogroup file, for each line
		List<List<String>> vtpeopleList = fexporter.importIn(getTableName(), userId);
		if (CollectionUtils.isEmpty(vtpeopleList)) {
			return;
		}
		
		for (List<String> vtpeople : vtpeopleList) {
			String oldId = vtpeople.get(StringUtil.getIndex("id", getColumnNameArray()));
			String oldPid = vtpeople.get(StringUtil.getIndex("pid", getColumnNameArray()));
			String oldJsid = vtpeople.get(StringUtil.getIndex("jsid", getColumnNameArray())); 
			String oldUserid = vtpeople.get(StringUtil.getIndex("userid", getColumnNameArray()));
			
			// query primary key to make sure key not processed; 
			if (keyMapper.getMappedKey(KeyMapper.VTPEOPLE_ID, oldId, userId) != null) {
				continue;
			}
			
			// generate a primary key, save primary key map, exchange primary key, generate a insert sql, execute insert
			
			String newUserId = keyMapper.getMappedKey(KeyMapper.USERID,
					oldUserid, Integer.valueOf(oldUserid));
			vtpeople.set(StringUtil.getIndex("userid", getColumnNameArray()), newUserId);
			
			String newJsid = keyMapper.getMappedKey(KeyMapper.JSID,
					oldJsid, Integer.valueOf(oldUserid));
			vtpeople.set(StringUtil.getIndex("jsid", getColumnNameArray()), newJsid);
			
			// itid get from sequence
			long newGenerateId = idService.generateKeys(Integer.valueOf(newUserId), "userpack", 1);
			vtpeople.set(StringUtil.getIndex("id", getColumnNameArray()), String.valueOf(newGenerateId));
			
			long newGeneratePid = idService.generateKeys(Integer.valueOf(newUserId), "dmpGroupId", 1);
			vtpeople.set(StringUtil.getIndex("pid", getColumnNameArray()), String.valueOf(newGeneratePid));
			
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
				params[i] = vtpeople.get(i);
			}
			dataAccessService.updateInfo(sql, params, Integer.parseInt(newUserId));
			keyMapper.addKeyMap(KeyMapper.VTPEOPLE_ID, oldPid, String.valueOf(newGeneratePid), Integer.valueOf(oldUserid));
		}
		
	}

	@Override
	public String getTableName() {
		return TableSchemaInfo.VTPEOPLE;
	}

	public String[] getColumnNameArray() {
		return TableSchemaInfo.vtpeople;
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

	public static <T extends Object> List<List<T>> doPage(List<T> list, int pageSize) {
		if (CollectionUtils.isEmpty(list)) {
			return Collections.emptyList();
		}

		int pageNum = (list.size() / pageSize) + 1;
		if ((list.size() % pageSize) == 0) {
			pageNum -= 1;
		}
		List<List<T>> result = new ArrayList<List<T>>(pageNum);
		for (int i = 0; i < pageNum; i++) {
			int from = i * pageSize;
			int to = (i + 1) * pageSize;
			if (to > list.size()) {
				to = list.size();
			}
			List<T> item = list.subList(from, to);
			result.add(item);
		}

		return result;
	}
	
	
}
