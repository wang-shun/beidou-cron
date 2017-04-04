package com.baidu.beidou.accountmove.ubmc.cprounit.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.accountmove.ubmc.cprounit.ubmcdriver.UbmcDriverProxy;
import com.baidu.beidou.accountmove.ubmc.util.McpackProxyFactoryBean;

/**
 * Function: UbmcService工厂生成器
 * 
 * @ClassName: UbmcServiceFactory
 * @author genglei01
 * @date Jan 14, 2015 2:58:21 PM
 */
public class UbmcServiceFactory {
	
	public static void initProperties(String[] ubmcServers, String ubmcServiceUrl, String ubmcSysCode, String ubmcProductId, int ubmcConnectionTimeout, int ubmcReadTimeout){
		servers = ubmcServers;
		serviceUrl = ubmcServiceUrl;
		sysCode = ubmcSysCode;
		prodId = ubmcProductId;
		connectionTimeout = ubmcConnectionTimeout;
		readTimeout = ubmcReadTimeout;
	}
    
    protected final static Log LOG = LogFactory.getLog(UbmcServiceFactory.class);
    
    private static UbmcServiceImpl factory = null;
    
    /**
     * 以下配置为线上最新配置，如有需要可进行相应修改
     */
    private static String[] servers = new String[]{"10.26.3.90:8080", "10.36.3.159:8080"};
    private static String serviceUrl = "/api/UbmcBatchService";
    private static String sysCode = "c40e5484f8ff74e954096bb42a108270";
    private static String prodId = "3";
    private static int connectionTimeout = 3000;
    private static int readTimeout = 4000;
    
    public static UbmcServiceImpl getInstance() {
        if (factory == null) {
            try {
                McpackProxyFactoryBean mcpackProxy = new McpackProxyFactoryBean();
                mcpackProxy.setServiceInterface(UbmcDriverProxy.class);
                mcpackProxy.setServiceUrl(serviceUrl);
                mcpackProxy.setServers(servers);
                mcpackProxy.setEncoding("UTF-8");
                mcpackProxy.setRetryTimes(3);
                mcpackProxy.setConnectionTimeout(connectionTimeout);
                mcpackProxy.setReadTimeout(readTimeout);
                mcpackProxy.setHasHeaders(true);
                mcpackProxy.afterPropertiesSet();
                
                UbmcDriverProxy ubmcDriverProxy = (UbmcDriverProxy) mcpackProxy.getObject();
                factory = new UbmcServiceImpl(sysCode, prodId);
                factory.setUbmcDriverProxy(ubmcDriverProxy);
            } catch (Exception e) {
                LOG.error("cannot construct ubmcService", e);
            }
        }
        
        return factory;
    }
    
    public static void main(String[] args) {
        UbmcServiceImpl ubmcService = UbmcServiceFactory.getInstance();
        String tmpUrl = ubmcService.getTmpUrl(1078227785L, 1);
        System.out.println(tmpUrl);
    }

}
