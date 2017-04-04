package com.baidu.beidou.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.tool.constant.SnapShotConstant;
import com.baidu.beidou.tool.mail.SnapMailManager;
import com.baidu.beidou.tool.service.SnapShotMgr;
import com.baidu.beidou.util.FileUtils;
import com.baidu.beidou.util.LogUtils;
import com.baidu.beidou.util.ServiceLocator;

public class DealSnapShot {
	private static final Log LOG = LogFactory.getLog(DealSnapShot.class);

	private static SnapShotMgr snapShotMgr = null;

	private static FileUtils fileUtils = null;

	private static SnapMailManager mailManager = null;
	
	private static int lineNum=-1;

	private static void contextInitialized() {
		String[] fn = new String[] {"applicationContext.xml","classpath:/com/baidu/beidou/tool/applicationContext.xml", "classpath:/com/baidu/beidou/user/applicationContext.xml"};
		snapShotMgr=(SnapShotMgr)ServiceLocator.getInstance(fn).factory.getBean("snapShotMgr");
		fileUtils=(FileUtils)ServiceLocator.getInstance(fn).factory.getBean("snapFileUtils");
		mailManager=(SnapMailManager)ServiceLocator.getInstance(fn).factory.getBean("mailManager");
	}

	public static void main(String[] args) throws Exception {
		
		contextInitialized();
		
		LogUtils.info(LOG, "start deal");
		
		String alarm_date="init";
		
		while(true){
						
			String d=""; //运行那一天的截图
			
			if(args.length>0){
				d=args[0];
			}else{//默认是今天
				SimpleDateFormat dateformat=new SimpleDateFormat("yyyyMMdd");
				d=dateformat.format(new Date());
			}
			
			Calendar cal = Calendar.getInstance();

			if(!alarm_date.equals(d)){
				alarm_date="";
			}

			List<String> lines=fileUtils.readFileLines(SnapShotConstant.DIR_AD+d+"/snap.data");
			if(lines.size()!=0){//里面的值是处理到的行数
				lineNum=Integer.parseInt(lines.get(0));
			}
			
			String path=fileUtils.getBaseDir()+SnapShotConstant.DIR_AD+d+"/snap_index.txt";	
			
			if(new File(path).exists()){
				FileReader myFileReader=new FileReader(path);
				BufferedReader myBufferedReader=new BufferedReader(myFileReader);
				String line;
		
				int index=0;
				while((line=myBufferedReader.readLine())!=null)
				{
					if(index<lineNum){
						index+=1;
						continue;
					}
					
					//如果23:50还有任务，报警，但是任务继续处理
					if(cal.get(Calendar.HOUR_OF_DAY)==23 && 
							cal.get(Calendar.MINUTE)>=50 &&
							alarm_date.equals("")){
						mailManager.sendWarningMail("截图任务仍在执行，跨天时剩余任务将抛弃", "请高优先级查看"+line);
						alarm_date=d;
					}

					//处理当前行
					String[] r=line.split("\t");
					try{
		
						if(r.length==4){
							String snapIdWithNumId = r[0];
							int snapId=0;
							if (snapIdWithNumId.contains(SnapShotConstant.SNAPID_NUMID_SPLITTER)) {
								snapId=Integer.parseInt(snapIdWithNumId.split(SnapShotConstant.SNAPID_NUMID_SPLITTER)[0]);
							} else {
								snapId=Integer.parseInt(snapIdWithNumId);
							}
							snapShotMgr.dealSnapShot(snapId, 
													r[1],
													r[2],
													r[3],
													d);
						}else{
							LogUtils.error(LOG, "snap_index文件格式错误");				
							mailManager.sendWarningMail("snap_index文件格式错误", "请高优先级查看"+line);
						}
						
					}catch(Exception e){
						e.printStackTrace();
						LogUtils.error(LOG, "ERROR deal snap "+r[0]+" "+e.getMessage());				
					}
					
					//写行数
					index+=1;
					fileUtils.saveFile(SnapShotConstant.DIR_AD+d+"/snap.data", String.valueOf(index));

									
				}
				
				myBufferedReader.close();
				myFileReader.close();
			}

			//一天结束了，可以退出
			if(cal.get(Calendar.HOUR_OF_DAY)==23 && 
					cal.get(Calendar.MINUTE)>=55){
				LogUtils.info(LOG, "deal snap exit normally");				
				return;
			}

			Thread.sleep(60000);//睡60秒看有无新任务
		}
	}
}
