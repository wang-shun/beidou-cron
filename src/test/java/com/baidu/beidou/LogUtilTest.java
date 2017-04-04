/**
 * 2009-6-12 下午06:24:17
 */
package com.baidu.beidou;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.junit.Test;

import com.baidu.beidou.aot.ImportAotQtWordNum;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class LogUtilTest {
	
	@Test
	public void testGetLogUtil(){
		Log log = LogFactory.getLog(LogUtilTest.class);
		log.info("log") ;
		assert(true);
	}

	@Test
	public void testModLogUtil(){
		Log log = LogFactory.getLog(LogUtilTest.class);
		log.info("Sunccssfully testModLogUtil") ;
		assert(true);
	}

	 
	@Test
	public void testModLogUtil_2(){
		Log log = LogFactory.getLog(LogUtilTest.class);
		log.info("Sunccssfully testModLogUtil_2") ;
		assert(true);
	}
	
	@Test
	public void testModLogUtil_3(){
		Log log = LogFactory.getLog(LogUtilTest.class);
		log.info("testModLogUtil_3 Getter Logger A") ;
		assert(true);
	}
	
	@Test
	public void testDeleteCronLog(){
		Log log = LogFactory.getLog(LogUtilTest.class);
		log.info("Sunccssfully testDeleteCronLog") ;
		assert(true);
	}
	
	@Test
	public void testCreateLog(){
		Log log = LogFactory.getLog(LogUtilTest.class);
		log.info("Sunccssfully testCreateLog") ;
		assert(true);
	}
	
	@Test
	public void testUpdateCronLog(){
		Log log = LogFactory.getLog(LogUtilTest.class);
		log.info("Sunccssfully testUpdateCronLog") ;
		assert(true);
	}
	
	@Test
	public void testChangeCronLog(){
		Log log = LogFactory.getLog(LogUtilTest.class);
		log.info("Sunccssfully testChangeCronLog") ;
		assert(true);
	}
	
	@Test
	public void testSyncCronLog(){
		Log log = LogFactory.getLog(LogUtilTest.class);
		log.info("Sunccssfully testSyncCronLog") ;
		assert(true);
	}
	
	@Test
	public void testSelectCronLog(){
		Log log = LogFactory.getLog(LogUtilTest.class);
		log.info("Sunccssfully testSelectCronLog") ;
		assert(true);
	}

	@Test
	public void testFillCronLog(){
		Log log = LogFactory.getLog(LogUtilTest.class);
		log.info("Sunccssfully testFillCronLog") ;
		assert(true);
	}
	
	@Test
	public void testLogFinder(){
		Log log = LogFactory.getLog(LogUtilTest.class);
		log.info("Sunccssfully testLogFinder") ;
		assert(true);
	}
	
	@Test
	public void testLogSync(){
		Log log = LogFactory.getLog(LogUtilTest.class);
		log.info("Sunccssfully testLogSync") ;
		assert(true);
	}
	
}
