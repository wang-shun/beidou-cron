package com.baidu.beidou.accountmove.dao.impl;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.accountmove.dao.DataAccessService;
import com.baidu.beidou.accountmove.dao.IDService;
import com.baidu.beidou.accountmove.dao.UbmcMaterial;
import com.baidu.beidou.accountmove.ubmc.conf.FileConfig;
import com.baidu.beidou.accountmove.ubmc.cprounit.service.UbmcServiceFactory;
import com.baidu.beidou.accountmove.ubmc.subFrame.mcCopy.CopyLog;
import com.baidu.beidou.accountmove.ubmc.subFrame.mcCopy.MaterialCopy;
import com.baidu.beidou.accountmove.ubmc.subFrame.mcCopy.MaterialKey;
import com.baidu.beidou.action.SequenceIdService;
import com.baidu.beidou.vo.Response;

public class IDServiceImpl implements IDService {

	private static final Log logger = LogFactory.getLog(IDServiceImpl.class);
	
	private FileConfig config;
	private MaterialCopy materialCopy;
	
	private DataAccessService addbDataAccessService;
	private DataAccessService xdbDataAccessService;
	private DataAccessService capdbDataAccessService;
	
	private SequenceIdService sequenceIdService;
	
	private Set<String> addbSequenceName;
	private Set<String> capdbSequenceName;
	private Set<String> sequenceServerName;
	
	public void init(){
		// initial ubmc driver
//		String sUserId = config.getProperty("userIds");
		String succLog = config.getProperty("succLog");
		String failLog = config.getProperty("failLog");
		
		String[] ubmcServers = config.getPropertyAsArray("ubmc.servers");
		String ubmcServiceUrl = config.getProperty("ubmc.serviceUrl");
		String ubmcSysCode = config.getProperty("ubmc.sysCode");
		String ubmcProductId = config.getProperty("ubmc.prodId");
		int ubmcConnectionTimeout = config.getPropertyAsInt("ubmc.connectionTimeout");
		int ubmcReadTimeout = config.getPropertyAsInt("ubmc.readTimeout");
		CopyLog.initCopyLog(succLog, failLog);
		UbmcServiceFactory.initProperties(ubmcServers, ubmcServiceUrl, ubmcSysCode, ubmcProductId, ubmcConnectionTimeout, ubmcReadTimeout);
		this.materialCopy = new MaterialCopy();
	}
	
	@Override
	public long generateKeys(int userId, String sequenceName, int step) {
		
		long result = 0;
		if (addbSequenceName.contains(sequenceName)) {
			result = addbDataAccessService.generateKeys(userId, sequenceName, step);
		} else if (capdbSequenceName.contains(sequenceName)) {
			result = capdbDataAccessService.generateKeys(userId, sequenceName, step);
		} else if (sequenceServerName.contains(sequenceName)) {
			result = getNextSequenceServiceId(sequenceName); // vtcode jsid
		} else{
			logger.error("you give wrong sequenceName");
		}
		return result;
	}

	@Override
	public UbmcMaterial generateUbmcMaterial(int userId, long mcId, int version) {
		MaterialKey materialKey = new MaterialKey(mcId, version);
		
		MaterialKey result = null;
		int tryTime = 0;
		while (result == null && tryTime < retryTime) {
		    if (tryTime != 0) {
		        try {
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    // do nothing
                }
		    }
		    result = materialCopy.copyMaterial(materialKey);
		    tryTime ++ ;
		}
		
		if(result == null){
			return null;
		}
		UbmcMaterial ubmc = new UbmcMaterial(result.getMcId(), result.getMcVersion());
		return ubmc;
	}

	@Override
	public UbmcMaterial generateGroupUbmcMaterial(int userId, long mcId, int version, int srcGroupId, int destGroupId) {
		MaterialKey materialKey = new MaterialKey(mcId, version);
		
		MaterialKey result = null;
        int tryTime = 0;
        while (result == null && tryTime < retryTime) {
            if (tryTime != 0) {
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
            
            result = materialCopy.copyGroupMaterialKey(materialKey, srcGroupId, destGroupId);
            tryTime ++ ;
        }
		
		if(result == null){
			return null;
		}
		
		UbmcMaterial ubmc = new UbmcMaterial(result.getMcId(), result.getMcVersion());
		return ubmc;
	}
	
	public DataAccessService getAddbDataAccessService() {
		return addbDataAccessService;
	}

	public void setAddbDataAccessService(DataAccessService addbDataAccessService) {
		this.addbDataAccessService = addbDataAccessService;
	}

	public DataAccessService getXdbDataAccessService() {
		return xdbDataAccessService;
	}

	public void setXdbDataAccessService(DataAccessService xdbDataAccessService) {
		this.xdbDataAccessService = xdbDataAccessService;
	}

	public DataAccessService getCapdbDataAccessService() {
		return capdbDataAccessService;
	}

	public void setCapdbDataAccessService(DataAccessService capdbDataAccessService) {
		this.capdbDataAccessService = capdbDataAccessService;
	}

	public Set<String> getAddbSequenceName() {
		return addbSequenceName;
	}

	public void setAddbSequenceName(Set<String> addbSequenceName) {
		this.addbSequenceName = addbSequenceName;
	}

	public Set<String> getCapdbSequenceName() {
		return capdbSequenceName;
	}

	public void setCapdbSequenceName(Set<String> capdbSequenceName) {
		this.capdbSequenceName = capdbSequenceName;
	}

	public SequenceIdService getSequenceIdService() {
		return sequenceIdService;
	}

	public void setSequenceIdService(SequenceIdService sequenceIdService) {
		this.sequenceIdService = sequenceIdService;
	}

	public Set<String> getSequenceServerName() {
		return sequenceServerName;
	}

	public void setSequenceServerName(Set<String> sequenceServerName) {
		this.sequenceServerName = sequenceServerName;
	}

	public FileConfig getConfig() {
		return config;
	}

	public void setConfig(FileConfig config) {
		this.config = config;
	}

	@Override
	public Long[] getNextKeywordIdBatch(int userId, String sequenceName,
			int step) {
		Long[] result = capdbDataAccessService.getNextKeywordIdBatch(userId, sequenceName, step);
		return result;
	}

	
    private long getNextSequenceServiceId(String typeName) {
        Response res = sequenceIdService.getJson(typeName, 1);
        if (res.getStatus() != 0) {
            logger.error("query nextid status error!" + res.getErrmsg());
        } else {
            long[] ids = res.getIds();
            if (ids != null && ids.length == 1) {
                return ids[0];
            } else {
            	logger.error("query nextid ids error!" + res.getIds());
            }
        }
        return 0;
    }
	
    private final int retryTime = 5;
    
}
