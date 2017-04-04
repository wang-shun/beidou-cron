package com.baidu.beidou.accountmove.table.vturl;

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

public class VturlImporter implements TableImporter{

	private FileExporter fexporter;
	private DataAccessService dataAccessService;
	private KeyMapper keyMapper;
	private IDService idService;
	
	@Override
	public void executeImport(int userId) {
		
		// read cprogroup file, for each line
		List<List<String>> vturlList = fexporter.importIn(getTableName(),
				userId);
		if (CollectionUtils.isEmpty(vturlList)) {
			return;
		}

		for (List<String> vtUrl : vturlList) {
			String oldId = vtUrl.get(StringUtil.getIndex("id",
					TableSchemaInfo.vturl));
			String oldPid = vtUrl.get(StringUtil.getIndex("pid",
					TableSchemaInfo.vturl));			
			String oldUserid = vtUrl.get(StringUtil.getIndex("userid",
					TableSchemaInfo.vturl));

			// query primary key to make sure key not processed;
			if (keyMapper.getMappedKey(KeyMapper.VTURL_ID, oldId, userId) != null) {
				continue;
			}

			// generate a primary key, save primary key map, exchange primary
			// key, generate a insert sql, execute insert

			String newUserId = keyMapper.getMappedKey(KeyMapper.USERID,
					oldUserid, Integer.valueOf(oldUserid));
			vtUrl.set(StringUtil.getIndex("userid", TableSchemaInfo.vturl),
					newUserId);

			String newPid = keyMapper.getMappedKey(KeyMapper.VTPEOPLE_ID,
					oldPid, Integer.valueOf(oldUserid));
			if (newPid == null) {
			    continue;
			}
			vtUrl.set(StringUtil.getIndex("pid", TableSchemaInfo.vturl),
					newPid);

			String[] colNames = Arrays.copyOfRange(TableSchemaInfo.vturl, 1,
					TableSchemaInfo.vturl.length);

			String sql = "insert into "
					+ TableNameUtil.getActualTableName(getTableName(),
							Integer.parseInt(newUserId)) + " ("
					+ StringUtil.getStringFromArray(colNames, ",")
					+ ") values("
					+ StringUtil.getRepetitionString("?", ",", colNames.length)
					+ ");";
			Object[] params = new Object[colNames.length];
			for (int i = 0; i < colNames.length; i++) {
				params[i] = vtUrl.get(i+1);
			}

			String newVturlId = dataAccessService.updateInfo(sql, params,
					Integer.parseInt(newUserId));
			if (newVturlId != null) {
				keyMapper.addKeyMap(KeyMapper.VTURL_ID, oldId, newVturlId,
						Integer.valueOf(oldUserid));
			}
		}
		
	}

	@Override
	public String getTableName() {
		return TableSchemaInfo.VTURL;
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
