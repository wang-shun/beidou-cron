package com.baidu.beidou.cprounit.task.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cprounit.service.syncubmc.CheckAdmakerFixUpdateMgr;
import com.baidu.beidou.cprounit.service.syncubmc.CheckAdmakerFlashVersionMaterMgr;
import com.baidu.beidou.cprounit.service.syncubmc.CheckAdmakerUpdateMgr;
import com.baidu.beidou.cprounit.service.syncubmc.CheckAndFixAdmakerMaterMgr;
import com.baidu.beidou.cprounit.service.syncubmc.CheckAndFixMaterMgr;
import com.baidu.beidou.cprounit.service.syncubmc.CheckImageUpdateMgr;
import com.baidu.beidou.cprounit.service.syncubmc.CheckMaterFilterMgr;
import com.baidu.beidou.cprounit.service.syncubmc.CheckMaterMd5Mgr;
import com.baidu.beidou.cprounit.service.syncubmc.CheckUnitAllMgr;
import com.baidu.beidou.cprounit.service.syncubmc.CheckUnitImageMgr;
import com.baidu.beidou.cprounit.service.syncubmc.CheckUnitTextMgr;
import com.baidu.beidou.cprounit.service.syncubmc.FixMaterUrlMgr;
import com.baidu.beidou.cprounit.service.syncubmc.RecompileAdmakerFlashMaterMgr;
import com.baidu.beidou.cprounit.service.syncubmc.RecompileTargetMaterMgr;
import com.baidu.beidou.cprounit.task.CheckUbmcMaterTask;

/**
 * ClassName: CheckUbmcMaterTaskImpl
 * Function: 校验UBMC中物料
 *
 * @author genglei
 * @version cpweb-567
 * @date May 13, 2013
 */
public class CheckUbmcMaterTaskImpl implements CheckUbmcMaterTask {
	
	private static final Log log = LogFactory.getLog(CheckUbmcMaterTaskImpl.class);
	
	private PrintWriter errorWriter = null;
	private PrintWriter logWriter = null;
	private PrintWriter invalidWriter = null;
	
	private CheckUnitTextMgr checkUnitTextMgr = null;
	private CheckUnitImageMgr checkUnitImageMgr = null;
	private CheckImageUpdateMgr checkImageUpdateMgr = null;
	private CheckUnitAllMgr checkUnitAllMgr = null;
	private CheckAdmakerUpdateMgr checkAdmakerUpdateMgr = null;
	private CheckMaterMd5Mgr checkMaterMd5Mgr = null;
	private CheckAdmakerFixUpdateMgr checkAdmakerFixUpdateMgr = null;
	private CheckMaterFilterMgr checkMaterFilterMgr = null;
	private RecompileAdmakerFlashMaterMgr recompileAdmakerFlashMaterMgr = null;
	private CheckAdmakerFlashVersionMaterMgr checkAdmakerFlashVersionMaterMgr = null;
	private FixMaterUrlMgr fixMaterUrlMgr = null;
	private CheckAndFixMaterMgr checkAndFixMaterMgr = null;
	private RecompileTargetMaterMgr recompileTargetMaterMgr = null;
	private CheckAndFixAdmakerMaterMgr checkAndFixAdmakerMaterMgr = null;
	
	public void checkText(int maxMaterNumSelect, String errorFileName, String logFileName,
			String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice) {
		log.info("initialize to open error file and log file...");
		init(errorFileName, logFileName, invalidFileName);
		
		log.info("begin to check text mater...");
		checkUnitTextMgr.checkMater(maxMaterNumSelect, maxThread, errorWriter, logWriter, 
				invalidWriter, dbFileName, dbIndex, dbSlice);
		log.info("stop to check text mater...");
		
		closeFile();
	}
	
	public void checkImage(int maxMaterNumSelect, String errorFileName, String logFileName,
			String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice) {
		log.info("initialize to open error file and log file...");
		init(errorFileName, logFileName, invalidFileName);
		
		log.info("begin to check image mater...");
		checkUnitImageMgr.checkMater(maxMaterNumSelect, maxThread, errorWriter, logWriter, 
				invalidWriter, dbFileName, dbIndex, dbSlice);
		log.info("stop to check image mater...");
		
		closeFile();
	}
	
	public void checkUpdate(int maxMaterNumSelect, String errorFileName, String logFileName,
			String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice) {
		log.info("initialize to open error file and log file...");
		init(errorFileName, logFileName, invalidFileName);
		
		log.info("begin to check and update...");
		checkImageUpdateMgr.checkMater(maxMaterNumSelect, maxThread, errorWriter, logWriter, 
				invalidWriter, dbFileName, dbIndex, dbSlice);
		log.info("stop to check and update...");
		
		closeFile();
	}
	
	public void checkAll(int maxMaterNumSelect, String errorFileName, String logFileName,
			String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice) {
		log.info("initialize to open error file and log file...");
		init(errorFileName, logFileName, invalidFileName);
		
		log.info("begin to check all material...");
		checkUnitAllMgr.checkMater(maxMaterNumSelect, maxThread, errorWriter, logWriter, 
				invalidWriter, dbFileName, dbIndex, dbSlice);
		log.info("stop to check all material...");
		
		closeFile();
	}
	
	public void checkAdmakerUpdate(int maxMaterNumSelect, String errorFileName, String logFileName,
			String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice) {
		log.info("initialize to open error file and log file...");
		init(errorFileName, logFileName, invalidFileName);
		
		log.info("begin to check admaker material, and update drmc material, and update database...");
		checkAdmakerUpdateMgr.checkMater(maxMaterNumSelect, maxThread, errorWriter, logWriter, 
				invalidWriter, dbFileName, dbIndex, dbSlice);
		log.info("stop to check admaker material, and update drmc material, and update database...");
		closeFile();
	}
	
	public void checkAdmakerFixUpdate(int maxMaterNumSelect, String errorFileName, String logFileName,
			String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice) {
		log.info("initialize to open error file and log file...");
		init(errorFileName, logFileName, invalidFileName);
		
		log.info("begin to check admaker fix material, and update ubmc material...");
		checkAdmakerFixUpdateMgr.checkMater(maxMaterNumSelect, maxThread, errorWriter, logWriter, 
				invalidWriter, dbFileName, dbIndex, dbSlice);
		log.info("stop to check admaker fix material, and update ubmc material...");
		closeFile();
	}
	
	public void checkMaterMd5(int maxMaterNumSelect, String errorFileName, String logFileName,
			String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice) {
		log.info("initialize to open error file and log file...");
		init(errorFileName, logFileName, invalidFileName);
		
		log.info("begin to check material md5, and update database...");
		checkMaterMd5Mgr.checkMater(maxMaterNumSelect, maxThread, errorWriter, logWriter, 
				invalidWriter, dbFileName, dbIndex, dbSlice);
		log.info("stop to check material md5, and update database...");
		closeFile();
	}
	
	public void checkMaterFilter(int maxMaterNumSelect, String errorFileName, String logFileName,
			String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice) {
		log.info("initialize to open error file and log file...");
		init(errorFileName, logFileName, invalidFileName);
		
		log.info("begin to filter special char, update ubmc material and update database...");
		checkMaterFilterMgr.checkMater(maxMaterNumSelect, maxThread, errorWriter, logWriter, 
				invalidWriter, dbFileName, dbIndex, dbSlice);
		log.info("stop to filter special char, update ubmc material and update database...");
		closeFile();
	}
	
	public void checkAdmakerRecompile(int maxMaterNumSelect, String errorFileName, String logFileName,
			String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice) {
		log.info("initialize to open error file and log file...");
		init(errorFileName, logFileName, invalidFileName);
		
		log.info("begin to recompile admaker mater, and update ubmc material...");
		recompileAdmakerFlashMaterMgr.checkMater(maxMaterNumSelect, maxThread, errorWriter, logWriter, 
				invalidWriter, dbFileName, dbIndex, dbSlice);
		log.info("stop to recompile admaker mater, and update ubmc material...");
		closeFile();
	}
	
	public void checkAdmakerVersion(int maxMaterNumSelect, String errorFileName, String logFileName,
			String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice) {
		log.info("initialize to open error file and log file...");
		init(errorFileName, logFileName, invalidFileName);
		
		log.info("begin to check admaker mater version, and update ubmc material...");
		checkAdmakerFlashVersionMaterMgr.checkMater(maxMaterNumSelect, maxThread, errorWriter, logWriter, 
				invalidWriter, dbFileName, dbIndex, dbSlice);
		log.info("stop to check admaker mater version, and update ubmc material...");
		closeFile();
	}
	
	public void fixMaterWirelessUrl(int maxMaterNumSelect, String errorFileName, String logFileName,
			String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice) {
		log.info("initialize to open error file and log file...");
		init(errorFileName, logFileName, invalidFileName);
		
		log.info("begin to update ubmc material, fix wireless url...");
		fixMaterUrlMgr.checkMater(maxMaterNumSelect, maxThread, errorWriter, logWriter, 
				invalidWriter, dbFileName, dbIndex, dbSlice);
		log.info("stop to update ubmc material, fix wireless url...");
		closeFile();
	}
	
	public void checkMaterial(int maxMaterNumSelect, String errorFileName, String logFileName,
			String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice) {
		log.info("initialize to open error file and log file...");
		init(errorFileName, logFileName, invalidFileName);
		
		log.info("begin to check the material of db and ubmc...");
		checkAndFixMaterMgr.checkMater(maxMaterNumSelect, maxThread, errorWriter, logWriter, 
				invalidWriter, dbFileName, dbIndex, dbSlice);
		log.info("stop to check the material of db and ubmc...");
		closeFile();
	}
	
	public void fixMaterial(int maxMaterNumSelect, String errorFileName, String logFileName,
			String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice) {
		log.info("initialize to open error file and log file...");
		init(errorFileName, logFileName, invalidFileName);
		
		log.info("begin to fix the material of db and ubmc...");
		checkAndFixMaterMgr.fixMater(maxMaterNumSelect, maxThread, errorWriter, logWriter, 
				invalidWriter, dbFileName, dbIndex, dbSlice);
		log.info("stop to fix the material of db and ubmc...");
		closeFile();
	}
	
	public void recompileTargettedMaterial(int maxMaterNumSelect, String errorFileName, String logFileName,
			String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice) {
		log.info("initialize to open error file and log file...");
		init(errorFileName, logFileName, invalidFileName);
		
		log.info("begin to recompile the targetted material of db and ubmc...");
		this.recompileTargetMaterMgr.checkMater(maxMaterNumSelect, maxThread, errorWriter, logWriter, 
				invalidWriter, dbFileName, dbIndex, dbSlice);
		log.info("stop to recompile the targetted material of db and ubmc...");
		closeFile();
	}
	
    public void checkAdmakerMaterial(int maxMaterNumSelect, String errorFileName, String logFileName,
            String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice) {
        log.info("initialize to open error file and log file...");
        init(errorFileName, logFileName, invalidFileName);
        
        log.info("begin to check the admaker material of db and ubmc...");
        checkAndFixAdmakerMaterMgr.checkAdmakerMater(maxMaterNumSelect, maxThread, errorWriter, logWriter, 
                invalidWriter, dbFileName, dbIndex, dbSlice);
        log.info("stop to check the admaker material of db and ubmc...");
        closeFile();
    }
    
	private void init(String errorFileName, String logFileName, String invalidFileName) {
		try {
			errorWriter = new PrintWriter(new File(errorFileName), "GBK");
			logWriter = new PrintWriter(new File(logFileName), "GBK");
			invalidWriter = new PrintWriter(new File(invalidFileName), "GBK");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void closeFile() {
		try {
			errorWriter.flush();
			errorWriter.close();
			
			logWriter.flush();
			logWriter.close();
			
			invalidWriter.flush();
			invalidWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (errorWriter != null) {
				try {
					errorWriter.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if (logWriter != null) {
				try {
					logWriter.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if (invalidWriter != null) {
				try {
					invalidWriter.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public CheckUnitTextMgr getCheckUnitTextMgr() {
		return checkUnitTextMgr;
	}

	public void setCheckUnitTextMgr(CheckUnitTextMgr checkUnitTextMgr) {
		this.checkUnitTextMgr = checkUnitTextMgr;
	}

	public CheckUnitImageMgr getCheckUnitImageMgr() {
		return checkUnitImageMgr;
	}

	public void setCheckUnitImageMgr(CheckUnitImageMgr checkUnitImageMgr) {
		this.checkUnitImageMgr = checkUnitImageMgr;
	}

	public CheckImageUpdateMgr getCheckImageUpdateMgr() {
		return checkImageUpdateMgr;
	}

	public void setCheckImageUpdateMgr(CheckImageUpdateMgr checkImageUpdateMgr) {
		this.checkImageUpdateMgr = checkImageUpdateMgr;
	}

	public CheckUnitAllMgr getCheckUnitAllMgr() {
		return checkUnitAllMgr;
	}

	public void setCheckUnitAllMgr(CheckUnitAllMgr checkUnitAllMgr) {
		this.checkUnitAllMgr = checkUnitAllMgr;
	}

	public CheckAdmakerUpdateMgr getCheckAdmakerUpdateMgr() {
		return checkAdmakerUpdateMgr;
	}

	public void setCheckAdmakerUpdateMgr(CheckAdmakerUpdateMgr checkAdmakerUpdateMgr) {
		this.checkAdmakerUpdateMgr = checkAdmakerUpdateMgr;
	}

	public CheckMaterMd5Mgr getCheckMaterMd5Mgr() {
		return checkMaterMd5Mgr;
	}

	public void setCheckMaterMd5Mgr(CheckMaterMd5Mgr checkMaterMd5Mgr) {
		this.checkMaterMd5Mgr = checkMaterMd5Mgr;
	}

	public CheckAdmakerFixUpdateMgr getCheckAdmakerFixUpdateMgr() {
		return checkAdmakerFixUpdateMgr;
	}

	public void setCheckAdmakerFixUpdateMgr(
			CheckAdmakerFixUpdateMgr checkAdmakerFixUpdateMgr) {
		this.checkAdmakerFixUpdateMgr = checkAdmakerFixUpdateMgr;
	}

	public CheckMaterFilterMgr getCheckMaterFilterMgr() {
		return checkMaterFilterMgr;
	}

	public void setCheckMaterFilterMgr(CheckMaterFilterMgr checkMaterFilterMgr) {
		this.checkMaterFilterMgr = checkMaterFilterMgr;
	}

	public RecompileAdmakerFlashMaterMgr getRecompileAdmakerFlashMaterMgr() {
		return recompileAdmakerFlashMaterMgr;
	}

	public void setRecompileAdmakerFlashMaterMgr(
			RecompileAdmakerFlashMaterMgr recompileAdmakerFlashMaterMgr) {
		this.recompileAdmakerFlashMaterMgr = recompileAdmakerFlashMaterMgr;
	}

	public CheckAdmakerFlashVersionMaterMgr getCheckAdmakerFlashVersionMaterMgr() {
		return checkAdmakerFlashVersionMaterMgr;
	}

	public void setCheckAdmakerFlashVersionMaterMgr(
			CheckAdmakerFlashVersionMaterMgr checkAdmakerFlashVersionMaterMgr) {
		this.checkAdmakerFlashVersionMaterMgr = checkAdmakerFlashVersionMaterMgr;
	}

	public FixMaterUrlMgr getFixMaterUrlMgr() {
		return fixMaterUrlMgr;
	}

	public void setFixMaterUrlMgr(FixMaterUrlMgr fixMaterUrlMgr) {
		this.fixMaterUrlMgr = fixMaterUrlMgr;
	}

	public CheckAndFixMaterMgr getCheckAndFixMaterMgr() {
		return checkAndFixMaterMgr;
	}

	public void setCheckAndFixMaterMgr(CheckAndFixMaterMgr checkAndFixMaterMgr) {
		this.checkAndFixMaterMgr = checkAndFixMaterMgr;
	}

	public RecompileTargetMaterMgr getRecompileTargetMaterMgr() {
		return recompileTargetMaterMgr;
	}

	public void setRecompileTargetMaterMgr(
			RecompileTargetMaterMgr recompileTargetMaterMgr) {
		this.recompileTargetMaterMgr = recompileTargetMaterMgr;
	}

    public void setCheckAndFixAdmakerMaterMgr(CheckAndFixAdmakerMaterMgr checkAndFixAdmakerMaterMgr) {
        this.checkAndFixAdmakerMaterMgr = checkAndFixAdmakerMaterMgr;
    }
	
}
