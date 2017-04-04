package com.baidu.beidou.accountmove.table.vtcode;

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

public class VtcodeImporter implements TableImporter{

	private FileExporter fexporter;
	private DataAccessService dataAccessService;
	private KeyMapper keyMapper;
	private IDService idService;
	
	@Override
	public void executeImport(int userId) {
		
		// read cprogroup file, for each line
		List<List<String>> vtcodeList = fexporter.importIn(getTableName(),
				userId);
		if (CollectionUtils.isEmpty(vtcodeList)) {
			return;
		}

		for (List<String> vtCode : vtcodeList) {
			String oldJsid = vtCode.get(StringUtil.getIndex("jsid",
					TableSchemaInfo.vtcode));
			String oldUserid = vtCode.get(StringUtil.getIndex("userid",
					TableSchemaInfo.vtcode));

			// query primary key to make sure key not processed;
			if (keyMapper.getMappedKey(KeyMapper.JSID, oldJsid, userId) != null) {
				continue;
			}

			// generate a primary key, save primary key map, exchange primary
			// key, generate a insert sql, execute insert

			String newUserId = keyMapper.getMappedKey(KeyMapper.USERID,
					oldUserid, Integer.valueOf(oldUserid));
			vtCode.set(StringUtil.getIndex("userid", TableSchemaInfo.vtcode),
					newUserId);

			// jsid get from sequence
			long newGenerateJsId = idService.generateKeys(
					Integer.valueOf(newUserId), "vtcode", 1);
			vtCode.set(StringUtil.getIndex("jsid", TableSchemaInfo.vtcode),
					String.valueOf(newGenerateJsId));

			String[] colNames = Arrays.copyOfRange(TableSchemaInfo.vtcode, 0,
					TableSchemaInfo.vtcode.length);
			// insert into beidou.cproplan
			// (planname,...,plan_type)values(?,...,?);
			String sql = "insert into "
					+ TableNameUtil.getActualTableName(getTableName(),
							Integer.parseInt(newUserId)) + " ("
					+ StringUtil.getStringFromArray(colNames, ",")
					+ ") values("
					+ StringUtil.getRepetitionString("?", ",", colNames.length)
					+ ");";
			Object[] params = new Object[colNames.length];
			for (int i = 0; i < colNames.length; i++) {
				params[i] = vtCode.get(i);
			}

			String newJsId = dataAccessService.updateInfo(sql, params,
					Integer.parseInt(newUserId));
			if (newJsId != null) {
				keyMapper.addKeyMap(KeyMapper.JSID, oldJsid, newJsId,
						Integer.valueOf(oldUserid));
			} else {
				keyMapper.addKeyMap(KeyMapper.JSID, oldJsid,
						String.valueOf(newGenerateJsId),
						Integer.valueOf(oldUserid));
			}
		}
	}

	@Override
	public String getTableName() {
		return TableSchemaInfo.VTCODE;
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
