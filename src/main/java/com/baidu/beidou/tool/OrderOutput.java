package com.baidu.beidou.tool;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.tool.constant.SnapShotConstant;
import com.baidu.beidou.tool.mail.SnapMailManager;
import com.baidu.beidou.tool.service.SnapShotMgr;
import com.baidu.beidou.tool.vo.OrderListRow;
import com.baidu.beidou.util.FileUtils;
import com.baidu.beidou.util.LogUtils;
import com.baidu.beidou.util.MailUtils;
import com.baidu.beidou.util.ServiceLocator;

public class OrderOutput {
	private static final Log LOG = LogFactory.getLog(OrderOutput.class);

	private static SnapShotMgr snapShotMgr = null;

	private static FileUtils fileUtils = null;

	private static SnapMailManager mailManager = null;

	private static void contextInitialized() {
		
		String[] fn = new String[] {"applicationContext.xml","classpath:/com/baidu/beidou/tool/applicationContext.xml", "classpath:/com/baidu/beidou/user/applicationContext.xml"};
		
		snapShotMgr=(SnapShotMgr)ServiceLocator.getInstance(fn).factory.getBean("snapShotMgr");
		fileUtils=(FileUtils)ServiceLocator.getInstance(fn).factory.getBean("snapFileUtils");
		mailManager=(SnapMailManager)ServiceLocator.getInstance(fn).factory.getBean("mailManager");
	}

	public static void main(String[] args) throws Exception {	
		try {
			contextInitialized();
			
			LogUtils.info(LOG, "start output order");
			
			//删除文件
			snapShotMgr.deleteFiles();

			LogUtils.info(LOG, "file deleted");

			//标记失败
			snapShotMgr.sendFailedMails();

			LogUtils.info(LOG, "expire marked");

			//导出订阅
			List<OrderListRow> rows=snapShotMgr.getOutputOrders();
			
			StringBuffer content=new StringBuffer();
			for(OrderListRow row: rows){
				content.append(row.getOrderid()+"\t"+
								row.getGroupid()+"\t"+
								row.getSite()+"\t"+
								snapShotMgr.getSnapOrderCount()+"\n");
			}
			SimpleDateFormat dateformat=new SimpleDateFormat("yyyyMMdd");
			String d=dateformat.format(new Date());
			
			boolean success=fileUtils.saveFile(SnapShotConstant.DIR_ORDER+"snap_orderlist."+d+".txt", content.toString());

			if(!success){
				mailManager.sendWarningMail("snap_orderlist导出失败", "请高优先级查看");
			}else{
				success=fileUtils.buildMd5File(SnapShotConstant.DIR_ORDER+"snap_orderlist."+d+".txt", 
										"snap_orderlist."+d+".txt", 
										SnapShotConstant.DIR_ORDER+"snap_orderlist."+d+".txt.md5");
				if(!success){
					mailManager.sendWarningMail("snap_orderlist md5导出失败", "请高优先级查看");
				}else{
					LogUtils.info(LOG, "finish output");
				}
		    }
		} catch (Exception e) {
			LogUtils.error(LOG, e);
		}
		
	}
}
