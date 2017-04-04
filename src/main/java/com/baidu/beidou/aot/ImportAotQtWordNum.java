package com.baidu.beidou.aot;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.aot.service.AotQtMgr;

public class ImportAotQtWordNum {
	
	private static final Log log = LogFactory.getLog(ImportAotQtWordNum.class);

	public static void main(String[] args) throws Exception {
		
		// 进行输入参数的检验
		if(args == null || args.length <2 || args[0] == null || args[1] == null){
			log.error("the param's num is not enough. Usage: ImportAotQtWordNum  inputFile  outputFile");
			System.exit(1);
		}
		
		if(!new File(args[0]).exists()){
			log.error("inputfile do not exist: " + args[0]);
			System.exit(1);
		}
		
		int batchGroupPerUser = 10;
		try{
			if(args.length > 2){
				int tmpBatchGroupPerUser = Integer.parseInt(args[2]);
				
				if(tmpBatchGroupPerUser <= 0){
					log.error("batchGroupPerUser must be greater than 0");
					System.exit(1);
				}
				
				batchGroupPerUser = tmpBatchGroupPerUser;
			}
		}
		catch(NumberFormatException e){
			log.error("batchGroupPerUser must be number");
			System.exit(1);
		}
		
		
		// 计算QT推广组的有效词数量
		String[] paths = new String[] { "applicationContext.xml",
				"classpath:/com/baidu/beidou/aot/applicationContext.xml" };

		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(paths);
		AotQtMgr aotQtMgr = (AotQtMgr) ctx.getBean("aotQtMgr");

		aotQtMgr.importQtWordNum(args[0], args[1], batchGroupPerUser);

	}
}
