/**
 * 2009-8-20 下午02:29:53
 * @author zengyunfeng
 */
package com.baidu.beidou.util;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.exception.RpcServiceException;

/**
 * @author zengyunfeng
 * 
 */
public class RandomServiceSelector<T> implements ServiceSelector<T> {
	private static final Log LOG = LogFactory.getLog(RandomServiceSelector.class);
	
	private Iterator<ServiceInvoker<T>> random = null;
	private int retryTime = 0;

	public RandomServiceSelector(List<ServiceInvoker<T>> serviceList,
			int retryTimes) {
		if (serviceList != null) {
			random = serviceList.listIterator(new Random().nextInt(serviceList
					.size()));
		}
		if (retryTimes < 1) {
			this.retryTime = 1;
		} else {
			this.retryTime = retryTimes;
		}
	}

	public T invoke(boolean errorExit) {
		if (random == null) {
			return null;
		}
		for (int retry = 0; retry < retryTime; retry++) {
			while (random.hasNext()) {
				ServiceInvoker<T> service = random.next();
				try {
					T result = service.invoke();
					return result;
				} catch (RpcServiceException e) {
					if (errorExit) {
						return null;
					}
					continue;
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
					throw new RuntimeException(e);
				}
			}
		}
		return null;
	}
}
