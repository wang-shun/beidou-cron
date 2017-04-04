package com.baidu.beidou.accountmove.process;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.beidou.accountmove.dao.DataAccessService;
import com.baidu.beidou.accountmove.exporter.Exporter;
import com.baidu.beidou.accountmove.importer.Importer;
import com.baidu.beidou.accountmove.keymap.KeyMapper;

/**
 * control account move process
 * @author work
 *
 */
public class MoveProcessor {

	private static final Logger logger = LoggerFactory.getLogger(MoveProcessor.class);
	
	private Exporter exportor;
	
	private Importer importor;
	
	private KeyMapper keyMapper;
	
	private DataAccessService dataAccessService;
	
	/**
	 * move account for one account, master all process of moving
	 * @param oldUserid oldUserid
	 * @param newUserid newUserid
	 * @return move account state,success or failed
	 */
	public boolean moveAccount(int oldUserid, int newUserid) {
		
		// account check
		if (!checkAccountEmpty(newUserid)) {
			logger.warn("new userid is not empty, userid=" + newUserid);
			return true;
		}
		
		// total process execute here
		keyMapper.addKeyMap(KeyMapper.USERID, String.valueOf(oldUserid), String.valueOf(newUserid), oldUserid);
		
		processAccountExport(oldUserid);
		 
		processInfoImport(oldUserid);
		
		return true;
	}
	
	/**
	 * check the user account is empty, plan count is 0
	 * @param newUserId
	 * @return if empty, return true, else return false
	 */
	private boolean checkAccountEmpty(int newUserId) {
		String sql = "select count(planid) as plannum from beidou.cproplan where userid = " + newUserId + ";";
		List<List<String>> result = dataAccessService.queryInfo(sql, null, newUserId);
		if (result != null && result.size() == 1) {
			String planNum = result.get(0).get(0);
			if (planNum != null && Integer.parseInt(planNum) == 0) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 *  export all info from db for userId
	 * @param userId this is the old userid
	 */
	private void processAccountExport(int userId) {
		exportor.exportAccount(userId);
	}
	
	/**
	 * import each table info into db
	 * for each table: generate primary key, save primary key map, change each id from id map;
	 * 
	 * @param userId this is the old userid
	 */
	private void processInfoImport (int userId) {
		importor.importAccount(userId);
	}

	public Exporter getExportor() {
		return exportor;
	}

	public void setExportor(Exporter exportor) {
		this.exportor = exportor;
	}

	public Importer getImportor() {
		return importor;
	}

	public void setImportor(Importer importor) {
		this.importor = importor;
	}

	public KeyMapper getKeyMapper() {
		return keyMapper;
	}

	public void setKeyMapper(KeyMapper keyMapper) {
		this.keyMapper = keyMapper;
	}

    public DataAccessService getDataAccessService() {
        return dataAccessService;
    }

    public void setDataAccessService(DataAccessService dataAccessService) {
        this.dataAccessService = dataAccessService;
    }
}
