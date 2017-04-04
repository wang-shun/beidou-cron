package com.baidu.beidou.accountmove.ubmc.util;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.accountmove.ubmc.util.exception.RpcServiceException;

/**
 * Function: 扩展原来的RandomServiceSelector，以实现动态调用Service
 *				该类要实现以下两个功能：负载均衡和互备。
 * @author   <a href="mailto:liangshimu@baidu.com">梁时木</a>
 * @created  2010-7-30
 * @since    Cpweb-155
 * @version  $Id: Exp $
 */
public class ExtRandomServiceSelector implements ExtServiceSelector{
	private static final Log LOG = LogFactory.getLog(ExtRandomServiceSelector.class);
	
	private List<ExtServiceInvoker> random = null;
	private int retryTime = 0;
	
	private Method method;
	private Object[]args;
	
	private Random randomer = new Random();

	
	/**
	 * @param serviceList  服务列表
	 * @param retryTimes   重试次数
	 * @param method   方法入口
	 * @param args 方法参数
	 */
	public ExtRandomServiceSelector(List<ExtServiceInvoker> serviceList,
			int retryTimes, Method method, Object[]args) {
	    if (CollectionUtils.isEmpty(serviceList)) {
	    	throw new IllegalArgumentException("serviceList should not be empty!");
	    }
        random = serviceList;
        if (retryTimes < 1) {
            this.retryTime = 1;
        } else {
            this.retryTime = retryTimes;
        }
		this.method = method;
		this.args = args;
	}

	public Object invoke(boolean errorExit) {
		//需要实现负载均衡和互备功能
		
		if (random == null) {
			return null;
		}
		//1、生成一个随机数
		int start = Math.abs(randomer.nextInt()) % random.size();
		for (int retry = 0; retry < retryTime && retry < random.size(); retry++) {
			ExtServiceInvoker service = random.get(start);
			
			//2、以1生成的数为基准，+1生成下一个数，但不能超出random.size()的界限
			start++;
			start %= random.size();
			try {
				return method.invoke(service.getInvoker(), args);
			} catch (RpcServiceException e) {
			    printErrMessage(e);
				if (errorExit) {
					return null;
				}
				continue;
			} catch (Exception e) {
                printErrMessage(e);
				if (errorExit) {
					throw new RuntimeException(e);
				}
				continue;
			}
		}
		return null;
	}
	
	private void printErrMessage(Throwable e) {
	    if (e == null) return;
	    Throwable baseCause = e;
        while (baseCause.getCause() != null) {
            baseCause = baseCause.getCause();
        }
        //"o"=origin, "d"=direct, "e"=exception, "m"=message
        String errMsg = "RPC invoke error,Target=["  
                + method.getDeclaringClass().getCanonicalName() + "#" + method.getName() 
                + "()], oe=[" + baseCause.getClass().getCanonicalName() 
                + "], om=[" + baseCause.getMessage() 
                + "], de=[" + e.getClass().getCanonicalName() 
                + "], dm=[" + e.getMessage() + "]";
        LOG.error(errMsg, e);
	}
	
	public static void main(String[] args) {
		int start = 2;
		start++;
		start %= 3;
		System.out.println(start);
		start++;
		start %= 3;
		System.out.println(start);
		start++;
		start %= 3;
		System.out.println(start);
		start++;
		start %= 3;
		System.out.println(start);
		start++;
		start %= 3;
		System.out.println(start);
		start++;
		start %= 3;
		System.out.println(start);
	}
}
