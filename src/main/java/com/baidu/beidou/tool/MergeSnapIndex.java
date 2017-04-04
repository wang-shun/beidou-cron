package com.baidu.beidou.tool;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

public class MergeSnapIndex {
	private static final Log LOG = LogFactory.getLog(MergeSnapIndex.class);

	private static SnapShotMgr snapShotMgr = null;
	private static FileUtils fileUtils = null;

	private static SnapMailManager mailManager = null;

	private static void contextInitialized() {
		String[] fn = new String[] {"applicationContext.xml","classpath:/com/baidu/beidou/tool/applicationContext.xml","classpath:/com/baidu/beidou/user/applicationContext.xml"};
		snapShotMgr=(SnapShotMgr)ServiceLocator.getInstance(fn).factory.getBean("snapShotMgr");
		fileUtils=(FileUtils)ServiceLocator.getInstance(fn).factory.getBean("snapFileUtils");
		mailManager=(SnapMailManager)ServiceLocator.getInstance(fn).factory.getBean("mailManager");
	}

	public static void main(String[] args) throws Exception {
		
		try {
			String d=""; //运行那一天的截图
			
			if(args.length>0){
				d=args[0];
			}else{//默认是今天
				SimpleDateFormat dateformat=new SimpleDateFormat("yyyyMMdd");
				d=dateformat.format(new Date());
			}

			contextInitialized();
			LogUtils.info(LOG, "file merge start "+d);

			//下载snap_index文件
			boolean success=snapShotMgr.downloadIndex(d);
			
			if(!success){
				LogUtils.error(LOG, "index file md5 error "+d);
				mailManager.sendWarningMail("snap index file md5 error", "请高优先级查看");
				return;
			}
			
			//读tmp文件和源文件
			List<String> tmpLines=fileUtils.readFileLines(SnapShotConstant.DIR_TMP+"snap_index.txt");
			LogUtils.info(LOG, "tmp file line count "+tmpLines.size());
			
			//过滤模板信息
			List<String> newLines=new ArrayList<String>();
			
			List<String> errLines=new ArrayList<String>();
			
			for(String line: tmpLines){
				String[] r=line.split("\t");
				if(r.length!=3){
					LogUtils.info(LOG, "合并snap_index文件出错：格式错误"+line);
					errLines.add(line);
				}else{
					newLines.add(line);
				}
			}
			
			if(errLines.size()>0){
				String str="";
				for(String l: errLines){
					str+=l+"<br>";
				}
				mailManager.sendWarningMail("合并snap_index失败: orderid格式错误", str);			
			}
			
			LogUtils.info(LOG, "filter tmp line count "+newLines.size());
			
			//保证文件存在
			fileUtils.checkFileExist(SnapShotConstant.DIR_AD+d, "snap_index.txt");
			//diff文件
			List<String> appendLines=new ArrayList<String>();
			List<String> lines=fileUtils.readFileLines(SnapShotConstant.DIR_AD+d+"/snap_index.txt");
			for(String l: newLines){
				boolean skip=false;
				for(String old: lines){
					if(old.startsWith(l)){
						skip=true;
						break;
					}
				}
				if(!skip){
					String newLine=snapShotMgr.mergeLine(l);
					if(newLine!=null){
						appendLines.add(newLine);
					}
				}
			}
			LogUtils.info(LOG, "after diff line count "+appendLines.size());
			
			//追加
			StringBuffer sb=new StringBuffer();
			for(String line: appendLines){
				sb.append(line+"\n");
			}
			success=fileUtils.appendContent(SnapShotConstant.DIR_AD+d+"/snap_index.txt", sb.toString());
			if(!success){
				mailManager.sendWarningMail("合并snap_index失败", "请高优先级查看");
			}
			LogUtils.info(LOG, "finish merge "+d);
		} catch (Exception e) {
			LogUtils.error(LOG, e);
		}
		
		
	}

}
