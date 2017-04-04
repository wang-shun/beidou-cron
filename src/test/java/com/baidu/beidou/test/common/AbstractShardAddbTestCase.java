/**
 * beidou-core-535#com.baidu.beidou.test.common.BaseTestCase.java
 * 下午2:58:26 created by Darwin(Tianxin)
 */
package com.baidu.beidou.test.common;

import javax.sql.DataSource;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.util.ThreadContext;

/**
 * 测试广告库的多数据源的testcase的基类
 * @author Darwin(Tianxin)
 */
@ContextConfiguration(locations = { "classpath:applicationContext-cron-test.xml" })
@TransactionConfiguration(transactionManager="addbTransactionManager")
public abstract class AbstractShardAddbTestCase  extends AbstractTransactionalJUnit4SpringContextTests {
	
	//系统初始化操作在第0个切片上
	static {
		ThreadContext.putUserId(1);
	}
	
	@Autowired
    public void setDataSource(@Qualifier("addbMultiDataSource") DataSource dataSource) {
        super.setDataSource(dataSource);
    }
	
	@BeforeTransaction
	public void go2Shard(){
		int shard = getShard();
		if(shard < 8){
			shard = (shard << 6) + 2;
		}
		ThreadContext.putUserId(shard);
	}
	
	@AfterTransaction
	public void clearShard(){
		ThreadContext.putUserId(null);
	}

	/**
	 * 本case在哪个切片上面跑。
	 * @return	返回值小于8，认为是切片号，否则认为是userId
	 * 下午3:07:08 created by Darwin(Tianxin)
	 */
	public abstract int getShard();
	
	@Test
	public void test(){
	}
}
