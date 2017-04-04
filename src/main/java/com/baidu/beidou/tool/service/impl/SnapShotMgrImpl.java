package com.baidu.beidou.tool.service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cprogroup.dao.CproGroupDaoOnMultiDataSource;
import com.baidu.beidou.cprounit.service.CproUnitMgr;
import com.baidu.beidou.tool.bo.SnapShot;
import com.baidu.beidou.tool.bo.SnapShotFile;
import com.baidu.beidou.tool.constant.SnapShotConstant;
import com.baidu.beidou.tool.dao.SnapShotDao;
import com.baidu.beidou.tool.dao.SnapShotFileDao;
import com.baidu.beidou.tool.mail.SnapMailManager;
import com.baidu.beidou.tool.service.SnapShotMgr;
import com.baidu.beidou.tool.vo.OrderListRow;
import com.baidu.beidou.user.bo.User;
import com.baidu.beidou.user.dao.UserDao;
import com.baidu.beidou.util.BnsUtils;
import com.baidu.beidou.util.FileUtils;
import com.baidu.beidou.util.LogUtils;
import com.baidu.beidou.util.ThreadContext;

public class SnapShotMgrImpl implements SnapShotMgr {
	
	private static final Log LOG = LogFactory.getLog(SnapShotMgrImpl.class);
	
	private SnapShotDao snapShotDao;
	
	private SnapShotFileDao snapShotFileDao;
	
	private Integer expireDays;
	
	private FileUtils fileUtil;
	
	private SnapMailManager mailManager;
	
	private CproUnitMgr unitMgr;
	
	private Integer maxOutput;
	
	private UserDao userDao;
	
	private CproGroupDaoOnMultiDataSource cproGroupDaoOnMultiDataSource;
	
	private Map<String, String> snapServer;
	
	private Map<String, String> imageServer;

	private Map<String, String> adsServer;

	private String snapUrl;
	
	private int downLoadLimit;
	
	private int allowDownload;

	private String downLoadPrefix;
	
	private int snapLimit;
	
	private String warningMail;
	
	private int snapOrderCount;

	public int getSnapLimit() {
		return snapLimit;
	}

	public void setSnapLimit(int snapLimit) {
		this.snapLimit = snapLimit;
	}

	public int getAllowDownload() {
		return allowDownload;
	}

	public void setAllowDownload(int allowDownload) {
		this.allowDownload = allowDownload;
	}

	public String getSnapUrl() {
		return snapUrl;
	}

	public void setSnapUrl(String snapUrl) {
		this.snapUrl = snapUrl;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public Integer getMaxOutput() {
		return maxOutput;
	}

	public void setMaxOutput(Integer maxOutput) {
		this.maxOutput = maxOutput;
	}

	public CproUnitMgr getUnitMgr() {
		return unitMgr;
	}

	public void setUnitMgr(CproUnitMgr unitMgr) {
		this.unitMgr = unitMgr;
	}

	public SnapMailManager getMailManager() {
		return mailManager;
	}

	public void setMailManager(SnapMailManager mailManager) {
		this.mailManager = mailManager;
	}

	public FileUtils getFileUtil() {
		return fileUtil;
	}

	public void setFileUtil(FileUtils fileUtil) {
		this.fileUtil = fileUtil;
	}

	public SnapShotDao getSnapShotDao() {
		return snapShotDao;
	}

	public void setSnapShotDao(SnapShotDao snapShotDao) {
		this.snapShotDao = snapShotDao;
	}

	public SnapShotFileDao getSnapShotFileDao() {
		return snapShotFileDao;
	}

	public void setSnapShotFileDao(SnapShotFileDao snapShotFileDao) {
		this.snapShotFileDao = snapShotFileDao;
	}

	public boolean deleteFiles() {
				// 得到待删除的文件
		List<SnapShotFile> files=snapShotFileDao.getDeletedFile();
		
		for(SnapShotFile file: files){
			String path=SnapShotConstant.DIR_SNAPSHOT+file.getFileName();
			// 删除文件实体
			boolean success=fileUtil.removeFile(path);
			if(success){//更新数据库delete_time
				snapShotFileDao.updateDeleteTime(file.getId());
				
			}else{//记录错误日志
				LogUtils.error(LOG, "delete file fail:"+path+" of snap shot "+file.getId());
			}
		}
		
		return false;
	}

	public List<OrderListRow> getOutputOrders() {
		// TODO Auto-generated method stub
		//得到所有订阅
		List<SnapShot> list=snapShotDao.getOutputSnap();
		
		List<OrderListRow> orders=new ArrayList<OrderListRow>();
		//一组为单位叠加，如果超过限制，剩余的加权重，发邮件告知
		
		List<SnapShot> filtedList=new ArrayList<SnapShot>();
		
		Map<Integer, Integer> groupSetting=new HashMap<Integer, Integer>();		
		//记录了*的记录位置
		Map<Integer, ArrayList<Integer>> allSitePos=new HashMap<Integer, ArrayList<Integer>>();		
		//记录了需要删除的*的记录位置
		List<Integer> allSitePosRm=new ArrayList<Integer>();
		
		//先处理域名重复的问题，只要考虑同时有*的情况需要过滤掉
		
		int iPos=0;
		for(SnapShot snap: list){
			if(snap.getSitetype()==SnapShotConstant.SET_THE_SITE){
				groupSetting.put(snap.getGroupid(), 1);//设置了指定站点
				filtedList.add(snap);
				iPos+=1;
				//如果之前已经有了*，需要覆盖
				if(allSitePos.get(snap.getGroupid())!=null){
					allSitePosRm.addAll(allSitePos.get(snap.getGroupid()));
				}
			}else if(snap.getSitetype()==SnapShotConstant.SET_ALL_SITE){
				if(groupSetting.get(snap.getGroupid())==null){//如果已经设置过了指定站点，舍弃*
					filtedList.add(snap);
					//记录一下，以后可能会覆盖
					ArrayList<Integer> posList;
					if(allSitePos.get(snap.getGroupid())==null){
						posList=new ArrayList<Integer>();
					}else{
						posList=allSitePos.get(snap.getGroupid());
					}
					posList.add(iPos);
					allSitePos.put(snap.getGroupid(), posList);
					
					iPos+=1;
				}
			}
		}
		//开始处理list，遇到需要略过的*的站点跳过
		iPos=0;
		
		List<Integer> resetIds=new ArrayList<Integer>();
		
		for(SnapShot snap: filtedList){
			if(allSitePosRm.contains(iPos)){
				iPos+=1;
				continue;//为*的站点，直接略过
			}
				
			int groupid=snap.getGroupid();
			
			if(orders.size()>=this.maxOutput){
				List<Integer> upIds=new ArrayList<Integer>();
				for(int i=iPos; i<filtedList.size();i++){
					upIds.add(filtedList.get(i).getId());
				}
				
				//加权重，发邮件
				snapShotDao.upSnapPriority(upIds);
				
				String strids="";
				for(int i=0;i<upIds.size();i++){
					strids+=upIds.get(i)+",";
				}
				
				mailManager.sendWarningMail("截图数量超过"+this.maxOutput+",部分延时处理", strids);
												
				break;
			}
			
			//1.0.40改为直接通过groupid来导出订单
			
			iPos+=1;
			
			OrderListRow row=new OrderListRow();
			row.setGroupid(groupid);
			row.setOrderid(snap.getId());
			if(snap.getSitetype()==SnapShotConstant.SET_ALL_SITE){
				row.setSite("*");
			}else if(snap.getSitetype()==SnapShotConstant.SET_THE_SITE){
				if(snap.getSite()!=null){
					row.setSite(snap.getSite().toLowerCase());
				}else{
					row=null;
				}
			}
			
			if(row!=null){
				orders.add(row);
			}
			
			if(snap.getPriority()>0){
				resetIds.add(snap.getId());
			}
			
		}	
		
		if(resetIds.size()>0){
			snapShotDao.resetSnapPriority(resetIds);
		}
		return orders;
	}

	public boolean sendFailedMails() {
		//找出超过周期的订阅，设置为失败
		List<SnapShot> list=snapShotDao.setExpireFailedStatus(this.expireDays);
		
		for(SnapShot snap: list){
			// 广告库路由threadlocal中放入userId
			ThreadContext.putUserId(snap.getUserid());
			//发邮件
			boolean success=mailManager.sendFailMail(snap);
			//更新发邮件的时间
			if(success){
				snapShotFileDao.updateEmailStatus(snap.getId());
			}
		}
		return true;
	}

	public Integer getExpireDays() {
		return expireDays;
	}

	public void setExpireDays(Integer expireDays) {
		this.expireDays = expireDays;
	}

	public boolean dealSnapShot(Integer orderid, String tu, String site, String ads, String day) {
		// TODO Auto-generated method stub
		int snapId=orderid;

		SnapShot snapObj=snapShotDao.getSnapShot(snapId);
		
		//放入threadcontext中userid做路由
		ThreadContext.putUserId(snapObj.getUserid());
		
		boolean success;
		
		if(snapObj==null){
			LogUtils.error(LOG, "snap shot not found "+ snapId+" orderId "+orderid);
			mailManager.sendWarningMail("snap shot not found "+ snapId+" orderId "+orderid, "请高优先级查看");
			return false;
		}else if(snapObj.getStatus()!=SnapShotConstant.STATUS_DEAL){
			LogUtils.info(LOG, "snap shot already captured "+ snapId+" omit orderId "+orderid);
			return true; //已经处理过了，直接跳过
		}else{
			//
			success=true;

			if(success){//如果成功，抓页面
				LogUtils.info(LOG, "start to put ads. orderid="+orderid+",tu="+tu+",site="+site+",ads="+ads);
				
				//把ad文件推到windows上
				boolean adsSuccess;
				
				adsSuccess=fileUtil.uploadFile(adsServer.get("server"), 
						Integer.parseInt(adsServer.get("port")),
						adsServer.get("user"), adsServer.get("passwd"), 
						adsServer.get("path")+"/"+day,
						adsServer.get("path")+"/"+day+"/"+orderid+".ads",
						SnapShotConstant.DIR_AD+"/"+day+"/"+ads+".ads");
				
				if(adsSuccess){
					LogUtils.info(LOG, "put ads success. orderid="+orderid+",tu="+tu+",site="+site+",ads="+ads);
					
					int retry=1;
					
					while(retry>=0){

						String img=this.sendSnapRequest(tu, day+"/"+orderid, site);
						
						LogUtils.info(LOG, "call param tu="+tu+", ad="+day+"/"+orderid+", site="+site);
						
						if(img!=null && !img.equals("")){
							
							img=img.trim();
							
							if(img.equals("NO_AD")){
								LogUtils.warn(LOG, "no ad code found on "+orderid+" site "+site);
								mailManager.sendWarningMail("no ad found on "+orderid+" site "+site, "请查看",this.warningMail);
								break;//no retry
							}else if(img.equals("FORBIDON")){
								LogUtils.error(LOG, "ip not allow to snap "+orderid+" site "+site);
								mailManager.sendWarningMail("ip not allow to snap "+orderid+" site "+site, "请高优先级查看");
								break;//no retry
							}else if(img.equals("CONFLICT")){
								LogUtils.error(LOG, "snap service busy "+orderid+" site "+site);
								mailManager.sendWarningMail("snap service busy "+orderid+" site "+site, "请高优先级查看");
								retry-=1;
								try {
									Thread.sleep(60000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}//睡60秒看有无新任务
							}else{
								
								boolean imgSuccess;
								
								fileUtil.checkFileExist(SnapShotConstant.DIR_SNAPSHOT+"/"+snapObj.getUserid(), "test");
										
								String fileName=String.valueOf(new Date().getTime())+"_"+snapObj.getId()+".png";
								imgSuccess=fileUtil.downloadFile(imageServer.get("server"), 
										Integer.parseInt(imageServer.get("port")),
										imageServer.get("user"), imageServer.get("passwd"), 
										imageServer.get("path")+"/"+img,
										SnapShotConstant.DIR_SNAPSHOT+"/"+snapObj.getUserid()+"/"+fileName);
								
								//保存数据库，发送邮件
								if(imgSuccess){
									LogUtils.info(LOG, "get snap image success "+orderid+" "+fileName);
									int size=fileUtil.getFileSize(SnapShotConstant.DIR_SNAPSHOT+snapObj.getUserid()+"/"+fileName);
									
									String token=fileUtil.makeFileMd5(SnapShotConstant.DIR_SNAPSHOT+snapObj.getUserid()+"/"+fileName);
									
									SnapShotFile snapFile=new SnapShotFile();
									snapFile.setId(snapObj.getId());
									snapFile.setUserid(snapObj.getUserid());
									snapFile.setFileName(snapObj.getUserid()+"/"+fileName);
									snapFile.setCreatetime(new Date());
									
									String domain="";
									
									Pattern p = Pattern.compile("^http://([^/]+).*",Pattern.CASE_INSENSITIVE);
									Matcher matcher = p.matcher(site);
									matcher.find();
									domain=matcher.group(1);
									
									snapFile.setSnapDomain(domain);
									
									snapFile.setSize(size);
									snapFile.setAllowDownTimes(3);
									snapFile.setToken(token);
									snapFile.setTokenCreateTime(new Date());
									
									boolean saveSuccess=false;
			
									saveSuccess=this.saveResult(snapFile);
									
									if(saveSuccess){
										
										String link=this.downLoadPrefix+"?userId="+snapObj.getUserid()+"&token="+token+"&snapshotId="+snapObj.getId();
										
										boolean mailSuccess=false;
										
										if(size<2000000){
											User user=userDao.findUserBySFId(snapObj.getUserid());
				
											//发出邮件
											String groupName = cproGroupDaoOnMultiDataSource.findGroupNameByGroupId(snapObj.getGroupid(), snapObj.getUserid());
											if(groupName == null){
												LogUtils.error(LOG, "group not found "+snapObj.getGroupid()+" snapid "+snapObj.getId());
											}else{
												String attachName = "["+user.getUsername()+"]["+groupName+"]["+domain+"].png";
												
		/*										System.out.println(attachName);
												try{
													System.out.println(new String(attachName.getBytes("GBK"), "UTF-8"));
													attachName=new String(attachName.getBytes("UTF-8"), "GBK");
												}catch(Exception e){
													LogUtils.error(LOG, "attach name convert error "+attachName);
												}
												System.out.println(attachName);
												System.out.println("哈哈.txt");
		*/										
												
												String filePath=fileUtil.getBaseDir()+SnapShotConstant.DIR_SNAPSHOT+snapFile.getFileName();
												
												mailSuccess=this.mailManager.sendSuccessMail(snapObj, link, attachName, filePath);									
											}
										}else{
											mailSuccess=mailManager.sendSuccessMail(snapObj, link, null, null);
										}
										
										//更新发邮件的时间
										if(mailSuccess){
											snapShotFileDao.updateEmailStatus(snapObj.getId());
										}
		
									}
									
								}else{
									LogUtils.error(LOG, "get snap image error "+orderid+" "+fileName);							
									mailManager.sendWarningMail("get snap image error "+orderid+" "+fileName, "请高优先级查看");
								}
								break;//no retry
							}
						}else{
							break;//no retry
						}
					}
				}else{
					LogUtils.error(LOG, "put ads error "+orderid);							
					mailManager.sendWarningMail("put ads error "+orderid, "请高优先级查看");
				}
			}
		}
			
		return success;
	}
	
	public boolean downloadIndex(String day){
		String server = BnsUtils.getBnsServerByName(snapServer.get("serverBnsName"));
		return fileUtil.downloadFileCheckMd5(server, 
				Integer.parseInt(snapServer.get("port")),
				snapServer.get("user"), snapServer.get("passwd"), 
				snapServer.get("path")+"/"+day+"/snap_index.txt",
				snapServer.get("path")+"/"+day+"/snap_index.txt.md5",
				SnapShotConstant.DIR_TMP+"snap_index.txt",
				SnapShotConstant.DIR_TMP+"snap_index.txt.md5");
	}
	
	public String sendSnapRequest(String tu, String orderid, String site){
		
		HttpConnectionManagerParams connectionParams = new HttpConnectionManagerParams();
		connectionParams.setConnectionTimeout(60 * 1000);

		HttpConnectionManager manager = new SimpleHttpConnectionManager();
		manager.setParams(connectionParams);
		
		HttpClient client = new HttpClient(manager);

		NameValuePair[] param = new NameValuePair[3];
		param[0] = new NameValuePair("url", site);
		param[1] = new NameValuePair("tu", tu);
		param[2] = new NameValuePair("ad", orderid);

		PostMethod method = new PostMethod(this.snapUrl);
		method.setRequestBody(param);
		try {
			int state = client.executeMethod(method);
			if (state == HttpStatus.SC_OK) {
				// String response = method.getResponseBodyAsString();
				StringBuilder response = new StringBuilder();
				BufferedReader in = new BufferedReader(new InputStreamReader(
						method.getResponseBodyAsStream()));
				String line = "";
				for (; (line = in.readLine()) != null;) {
					response.append(line);
				}

				line = response.toString().trim();
				
				return line;
			}
		}catch(Exception e){
			LogUtils.error(LOG, "send snap request fail "+orderid+" "+tu+" "+site+" "+e.getMessage());
			mailManager.sendWarningMail("send snap request fail "+orderid+" "+tu+" "+site, e.getMessage());
		}
		return null;
		
	}

	public Map<String, String> getSnapServer() {
		return snapServer;
	}

	public void setSnapServer(Map<String, String> snapServer) {
		this.snapServer = snapServer;
	}

	public Map<String, String> getImageServer() {
		return imageServer;
	}

	public void setImageServer(Map<String, String> imageServer) {
		this.imageServer = imageServer;
	}

	public boolean saveResult(SnapShotFile snapFile) {
		snapShotFileDao.saveSnapFile(snapFile);
		if(snapShotDao.getSnapCount(snapFile.getUserid(), SnapShotConstant.STATUS_SUCCESS)>=this.snapLimit){
			snapShotDao.deleteFirst(snapFile.getUserid());
		}
		snapShotDao.saveStatus(SnapShotConstant.STATUS_SUCCESS, snapFile.getId());
		return true;
	}

	public CproGroupDaoOnMultiDataSource getCproGroupDaoOnMultiDataSource() {
		return cproGroupDaoOnMultiDataSource;
	}

	public void setCproGroupDaoOnMultiDataSource(CproGroupDaoOnMultiDataSource cproGroupDaoOnMultiDataSource) {
		this.cproGroupDaoOnMultiDataSource = cproGroupDaoOnMultiDataSource;
	}

	public int getDownLoadLimit() {
		return downLoadLimit;
	}

	public void setDownLoadLimit(int downLoadLimit) {
		this.downLoadLimit = downLoadLimit;
	}

	public String getDownLoadPrefix() {
		return downLoadPrefix;
	}

	public void setDownLoadPrefix(String downLoadPrefix) {
		this.downLoadPrefix = downLoadPrefix;
	}

	public Map<String, String> getAdsServer() {
		return adsServer;
	}

	public void setAdsServer(Map<String, String> adsServer) {
		this.adsServer = adsServer;
	}

	public String getWarningMail() {
		return warningMail;
	}

	public void setWarningMail(String warningMail) {
		this.warningMail = warningMail;
	}

	public int getSnapOrderCount() {
		return snapOrderCount;
	}

	public void setSnapOrderCount(int snapOrderCount) {
		this.snapOrderCount = snapOrderCount;
	}

	public String mergeLine(String line) {
		String[] r=line.split("\t");
		String snapIdWithNumId = r[0];
		int snapId=0;
		if (snapIdWithNumId.contains(SnapShotConstant.SNAPID_NUMID_SPLITTER)) {
			snapId=Integer.parseInt(snapIdWithNumId.split(SnapShotConstant.SNAPID_NUMID_SPLITTER)[0]);
		} else {
			snapId=Integer.parseInt(snapIdWithNumId);
		}

		SnapShot snapObj=snapShotDao.getSnapShot(snapId);
		
		//判断是否需要抓取ads文件
		if(snapObj==null){
			LogUtils.error(LOG, "snap shot not found "+ snapId);
			mailManager.sendWarningMail("snap shot not found "+ snapId, "请高优先级查看");
			return null;
		}else if(snapObj.getStatus()!=SnapShotConstant.STATUS_DEAL){
			LogUtils.info(LOG, "snap shot already captured "+ snapId+" omit ");
			return null; //已经处理过了，直接跳过
		}else{
			boolean success=false;

            Date myDate=new Date();
            SimpleDateFormat formatter = new SimpleDateFormat ("HHmmss");
            String strDate = formatter.format(myDate);

			String newName=snapIdWithNumId+"_"+strDate;

			SimpleDateFormat dateformat=new SimpleDateFormat("yyyyMMdd");
			String day=dateformat.format(new Date());
			
			String server = BnsUtils.getBnsServerByName(snapServer.get("serverBnsName"));
			success=fileUtil.downloadFileCheckMd5(server,
					Integer.parseInt(snapServer.get("port")),
					snapServer.get("user"), snapServer.get("passwd"), 
					snapServer.get("path")+"/"+day+"/"+snapIdWithNumId+".ads", 
					snapServer.get("path")+"/"+day+"/"+snapIdWithNumId+".ads.md5", 
					SnapShotConstant.DIR_AD+"/"+day+"/"+newName+".ads",
					SnapShotConstant.DIR_AD+"/"+day+"/"+newName+".ads.md5");

			if(!success){
				LogUtils.error(LOG, "ads md5 error "+ snapIdWithNumId);
				mailManager.sendWarningMail("ads md5 error "+ snapId, "请高优先级查看");
				return null;
			}else{
				//给ads文件改名
				return 	line+"\t"+newName;
			}
		}
	}
}
