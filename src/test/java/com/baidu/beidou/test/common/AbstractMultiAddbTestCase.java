/**
 * beidou-core-535#com.baidu.beidou.test.common.BaseTestCase.java
 * 下午2:58:26 created by Darwin(Tianxin)
 */
package com.baidu.beidou.test.common;

import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import com.baidu.beidou.util.ThreadContext;

/**
 * 测试广告库的多数据源的testcase的基类
 * @author Darwin(Tianxin)
 */
public abstract class AbstractMultiAddbTestCase {

	//系统初始化操作在第0个切片上
	static {
		ThreadContext.putUserId(1);
	}

	/**
	 * 加载context文件
	 */
	protected static ApplicationContext CTX;

	static{
		if(CTX == null)
			try {
				CTX = new ClassPathXmlApplicationContext("classpath:applicationContext-test.xml");
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
	}

	/**
	 * 从context中获取一个bean
	 * @param beanClass
	 * @return
	 * 下午3:43:36 created by Darwin(Tianxin)
	 */
	@SuppressWarnings("unchecked")
	public final <T> T getBean(Class<T> beanClass) {
		return (T) CTX.getBeansOfType(beanClass).values().iterator().next();
	}
	
	@Test
	public void test(){
	}
}
