
package com.baidu.beidou.accountmove.ubmc.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.accountmove.ubmc.util.exception.ConfigureException;
import com.baidu.beidou.accountmove.ubmc.util.exception.RpcServiceException;
import com.baidu.beidou.accountmove.ubmc.util.rpc.McpackRpcProxyWithHeaderProperty;
import com.baidu.rpc.client.ProxyFactory;
import com.baidu.rpc.exception.ExceptionHandler;


/**
 * ClassName:CommonMcpackProxyFactory
 * Function:通用的基于JDK的Mcpack客户端调用方式
 * 	<P>只依赖于MCPACK
 *
 * @author   <a href="mailto:liangshimu@baidu.com">梁时木</a>
 * @created  2010-7-30
 * @version  $Id: Exp $
 */
public class CommonMcpackProxyFactory {
	
	protected Log log = LogFactory.getLog(getClass());
	
	/** 代理接口的后缀名 */
	public static final String PROXY_INTERFACE_SUFFIX = "Proxy";

    /** 服务列表，非空，由server+url生成 */
    protected String[] services;
    /** 配置的服务器列表，含端口 */
    protected String[] servers;
    
    /** 协议前缀 */
    protected String protocol = "http://";
    
    /** 调用的Url */
    protected String serviceUrl;
    /** 编码 */
    protected String encoding = "UTF-8";
    /** 重试次数，会取min(services.length, retryTimes)的较小值来重试 */
    protected int retryTimes = 3;
    /** 用户名 */
//    protected String userName = "";
    /** 密码 */
//    protected String password = "";

    /**
     * <ul>说明：通常可以定义三个接口和一个实现类：其中1是必须的，2、3、4则视情况而定，
     * 3、4通常是对1中的方法的二次封装。
     *   <li>1、原始接口(*Driver)；
     *   <li>2、原始接口代理(*DriverProxy)；
     *   <li>3、暴露给用户的接口(*Service)；
     *   <li>4、暴露给用户的接口实现（*ServiceImpl）；
     * </ul>
     * 
     * <ul>几者关系：
     *   <li>1、2两个个接口必须处于同一个包下；
     *   <li>2的名称是在1的基础上+“Proxy”，主要用于向后端发送消息头；
     *   <li>1、2接口中的方法名相同，2比1多面一个参数，该参数为Map&ltString, String&gt，且放在方法的最后；
     *   <li>3是暴露给用户调用的方法，通常是对1的二次封装；
     *   <li>4是3的具体实现，通常调用1(或者2)来实现McPack调用；
     *   <li>典型的例子如：原始接口AmDriver,代理接口AmDriverProxy；暴露接口AmService；实现类AmServiceImpl；
     *   <li>如果1不是很复杂，则可以将1、3合并；
     *   <li>如果1不需要发送消息头(hasHeaders=false)则可以将1，2合并。
     * </ul>
     */
    protected Class serviceInterface;
    /** 出错后是否直接退出,默认是false */
    protected boolean errorExit = false;
    
    /** 连接超时，毫秒数 */
    private int connectionTimeout;
    /** 读超时，毫秒数 */
    private int readTimeout;
    
    /** 是否需要消息头，默认为不需要
     *  hasHeaders=true表示该接口需要向服务器端发送消息头，因此需要定义相应的*Proxy；
     *  反之不用定义*Proxy；
     */
    private boolean hasHeaders = false;	
    
    /**
     * <p>Function: 实现重试功能
     * <p>关于接口定义说明：
     * <p>可以定义两个接口：1、原始接口；2、原始接口代理；
     * <p>1和2有三点关系：
     * <p>1、2个接口处于同一个包下；
     * <p>2、2的名称是在1的基础上+“Proxy”；
     * <p>3、接口中的方法名相同，2比1多面一个参数，该参数为Map<String, String>，且放在方法的最后；
     * <p>
     * <p>典型的例子如：原始接口AmDriver,代理接口AmDriverProxy；
     */

    class RandomSelectorProxy implements InvocationHandler {
        
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            
        	final Map<String, String> lastParameter ;//最后一个参数，用于传递消息头和参数等
        	final Class targetClass;//待调用的接口；
        	Method targetMethod;//待调用的方法
        	Object[] targetArgs;//待传入参数
        	Class[] targetArgClasses;//待传入参数的类型；
        	
        	if(hasHeaders) {
        		//如果需要发送消息头则需要定义Proxy，走该分支。
        	
	        	String targetClassName = method.getDeclaringClass().getName();
	        	int proxyIndex = targetClassName.lastIndexOf(PROXY_INTERFACE_SUFFIX);//以Proxy结尾
	        	if(proxyIndex < 1){
	        		throw new ConfigureException(targetClassName + " must be end with '" + PROXY_INTERFACE_SUFFIX + "'");
	        	}
	        	targetClassName = targetClassName.substring(0, proxyIndex);
	        	targetClass = Class.forName(targetClassName);
	        	
	            //args中包括至少一个参数，且最后一个参数为Map<String, String>：表示参数传递（如消息头等）。
	            if (args.length < 1) {
	            	log.error(method.getClass().getName() + "." + method.getName() + "'s params must not be empty");
	            	throw new ConfigureException(" args must not be empty");
	            } else {
	            	Object lastParameterTmp = args[args.length - 1];
	            	if (lastParameterTmp != null && ! (lastParameterTmp instanceof Map) ) {
	            		throw new ConfigureException("last arg must be type of java.util.Map");
	            	} else {
	            		lastParameter = (Map<String, String>) lastParameterTmp;
	            	}
	            	Class[] clazz = method.getParameterTypes();
	        		targetArgClasses = new Class[args.length - 1];
	        		targetArgs = new Object[args.length - 1];
	        		for (int i = 0; i < args.length - 1; i++) {
	        			targetArgClasses[i] = clazz[i];
	        			targetArgs[i] = args[i];
	        		}
	        		targetMethod =  targetClass.getDeclaredMethod(method.getName(), targetArgClasses);
	            }
        	} else {
        		//不需要定义Proxy来发送消息头
        		
        		targetClass = method.getDeclaringClass();
        		targetMethod = method;
        		targetArgs = args;
        		lastParameter = null;
        	}
        	
            //具体调用时实现重试功能
            List<ExtServiceInvoker> serviceInvokers = new ArrayList<ExtServiceInvoker>(services.length);
            for(int i=0; i<services.length; i++ ){
                final String serviceUrl = services[i];
                ExtServiceInvoker service = new ExtServiceInvoker(){

                    public Object getInvoker() throws RpcServiceException {
                    	McpackRpcProxyWithHeaderProperty proxy = new McpackRpcProxyWithHeaderProperty(
                                serviceUrl, encoding, new ExceptionHandler());
                    	if(hasHeaders) {
                    		proxy.addHeaderProperties(lastParameter);
                    	}
                        if(connectionTimeout > 0) {
                        	proxy.setConnectTimeout(connectionTimeout);
                        }

                        if(readTimeout > 0) {
                        	proxy.setReadTimeout(readTimeout);
                        }
                        return ProxyFactory.createProxy( targetClass, proxy);
                    }
                    
                };
                serviceInvokers.add(service);
            }
            ExtServiceSelector serviceSelector = new ExtRandomServiceSelector(
                    serviceInvokers, retryTimes, targetMethod, targetArgs);
            return serviceSelector.invoke(errorExit);
        }
    }

    public Object getObject() throws Exception {
        //生成一个执行代理
        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{serviceInterface}, new RandomSelectorProxy());
    }

    public Class getObjectType() {
        return getServiceInterface();
    }

    public boolean isSingleton() {
        return true;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }
/*
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }*/

    public Class getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Class serviceInterface) {
        if (serviceInterface == null || !serviceInterface.isInterface()) {
            throw new IllegalArgumentException("'serviceInterface' must be an interface");
        }
        this.serviceInterface = serviceInterface;
    }

    public String[] getServices() {
        return services;
    }

    public void setServices(String[] services) {
        if (services == null || services.length < 1) {
            throw new IllegalArgumentException("'services' must be an nonempty array");
        }
        this.services = services;
    }

    public boolean isErrorExit() {
        return errorExit;
    }

    public void setErrorExit(boolean errorExit) {
        this.errorExit = errorExit;
    }

	public String[] getServers() {
		return servers;
	}

	public void setServers(String[] servers) {
		this.servers = servers;
	}

	public String getServiceUrl() {
		return serviceUrl;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public void afterPropertiesSet() throws Exception {
    	if (services == null || services.length < 1) {
    		
    		if(servers == null || servers.length < 1) {
    			
    			throw new IllegalArgumentException("either servers or services is required!");
    		}
    		services = new String[servers.length];
    		for (int i = 0; i < servers.length; i++) {
    			services[i] = protocol + servers[i] + serviceUrl;
    		}
    	}
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public boolean hasHeaders() {
		return hasHeaders;
	}

	public void setHasHeaders(boolean needHeaders) {
		this.hasHeaders = needHeaders;
	}
    
}

