package com.baidu.beidou.aot;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.aot.service.AotQtMgr;

public class ImportAotQtkrWord {
	
	private static final Log log = LogFactory.getLog(ImportAotQtkrWord.class);
	
	public static void main(String[] args) throws Exception {

		// 进行输入参数的检验
		if(args == null || args.length <4 || args[0] == null || args[1] == null || args[2] == null || args[3] == null){
			log.error("the param's num is not enough. Usage: ImportAotQtkrWord  inputFile  outputFile  relativity  minQtkrCnt");
			System.exit(1);
		}
		
		if(!new File(args[0]).exists()){
			log.error("inputfile do not exist: " + args[0]);
			System.exit(1);
		}
		
		float relativity = 0;
		int minQtkrCnt = 0;
		try{
			relativity = Float.parseFloat(args[2]);
			if(relativity < 0){
				log.error("relativity must be greater than 0");
				System.exit(1);
			}
			
			minQtkrCnt = Integer.parseInt(args[3]);
			if(minQtkrCnt <= 0){
				log.error("minQtkrCnt must be greater than 0");
				System.exit(1);
			}
		}
		catch(NumberFormatException e){
			log.error("relativity or minQtkrCnt must be number");
			System.exit(1);
		}
		
		// 处理主动推荐词
		String[] paths = new String[] { "applicationContext.xml",
				"classpath:/com/baidu/beidou/aot/applicationContext.xml"};

		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(paths);
		AotQtMgr aotQtMgr = (AotQtMgr) ctx.getBean("aotQtMgr");
		aotQtMgr.importQtkrWord(args[0], args[1], relativity, minQtkrCnt);
	}
}
