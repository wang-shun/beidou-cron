package com.baidu.beidou.bes.user.template;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import com.baidu.beidou.bes.user.filter.UserContext;
import com.baidu.beidou.bes.user.filter.UserFilter;
import com.baidu.beidou.bes.user.po.AuditUserInfo;
import com.baidu.beidou.bes.user.service.AuditUserServiceMgr;
import com.baidu.beidou.bes.user.vo.UcUserView;
import com.baidu.beidou.bes.util.BesUtil;
import com.baidu.beidou.util.page.DataPage;
import com.baidu.beidou.util.string.StringUtil;
/**
 * 导入用户信息流程
 * 
 * @author caichao
 */
public class ImportUserExcutor {
	private static final Log log = LogFactory.getLog(ImportUserExcutor.class);
	
	private String company;
	private String adxUserAddFile;
	private String adxUserUpdateFile;
	
	private List<UserFilter> filterList;
	private AuditUserServiceMgr userServiceMgr;
	private Map<String,Integer> companyMapping;
	
	private final static String SEP = "\t";
	
	private final static Integer ADD_OPERATE = 0;
	private final static Integer UPDATE_OPERATE = 1;
	
	private final static Integer POOL_SIZE = 10;
	private ExecutorService pool = Executors.newFixedThreadPool(POOL_SIZE);
	
	public void execute(ApplicationContext ctx){
		//1：处理新增用户数据
		doAddFile(ctx);
		//2: 处理更新用户数据
		doUpdateFile(ctx);
	}
	
	
	//多线程批量处理更新
	private void doUpdateFile(ApplicationContext ctx) {
		try {
			//文件读取
			List<UcUserView> addList = convertFileToObject(adxUserUpdateFile);
			
			
			log.info("update file size : " + addList.size());
			//过滤
			addList = filterUserView(addList);
			
			//更新数据库
			dbOperate(addList, UPDATE_OPERATE,ctx);
			
		} catch(IOException e) {
			log.error("close resource fail", e);
		} catch (InterruptedException e) {
			log.error("interrupt thread fail", e);
		}
	}
	
	//多线程批量处理新增
	private void doAddFile(ApplicationContext ctx) {
		try {
			//文件读取
			List<UcUserView> addList = convertFileToObject(adxUserAddFile);
			log.info("add file size : " + addList.size());
			//过滤
			addList = filterUserView(addList);
			//新增数据库
			dbOperate(addList, ADD_OPERATE,ctx);
			
		} catch(IOException e) {
			log.error("close resource fail", e);
		} catch (InterruptedException e) {
			log.error("interrupt thread fail", e);
		}
	}
	/**
	 * 文件读取
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private List<UcUserView> convertFileToObject(String file) throws IOException{
		File addFile = new File(BesUtil.getAbsoluteFile(file, company));
		BufferedReader read = null ;
		FileInputStream inputStream = null;
		if (!addFile.exists()) {
			log.info("user file path : " + BesUtil.getAbsoluteFile(file, company)+" not exist");
			return new ArrayList<UcUserView>();
		}
		List<UcUserView> allUserView = new ArrayList<UcUserView>();
		try {
			//转化文件中数据为view对象数组
			inputStream = new FileInputStream(addFile);
			read = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
			String line = read.readLine();
			//文件生成时已对文件去空行操作
			while (!StringUtil.isEmpty(line) && line != null) {
				String[] cols = StringUtil.split(line, SEP);
				
				if (cols.length != 3) {//此处待确定uc数据格式后需修改
					line = read.readLine();
					continue;
				}
				
				UcUserView view = new UcUserView(StringUtil.convertInt(cols[0], 0),cols[1],cols[2],"备注");
				allUserView.add(view);
				line = read.readLine();
			}
		} catch(FileNotFoundException e) {
			log.error("adx_user_add file not found", e);
		} catch(IOException e){
			log.error("read file occur exception", e);
		} finally {
			if (read != null){
				read.close();
			}
			if (inputStream != null) {
				inputStream.close();
			}
		}
		
		return allUserView;
	}
	
	/**
	 * 过滤
	 * @param addList
	 * @return
	 */
	private List<UcUserView> filterUserView(List<UcUserView> viewList){
		UserContext context = new UserContext(viewList);
		
		if (!CollectionUtils.isEmpty(filterList)) {
			
			for (UserFilter filter : filterList) {
				filter.doFilter(context);
			}
		}
		return context.getUserList();
	}

	/**
	 * 操作数据库
	 * @return
	 * @throws InterruptedException 
	 */
	private void dbOperate(List<UcUserView> viewList,Integer opType,ApplicationContext ctx) throws InterruptedException {
		if (CollectionUtils.isEmpty(viewList)) {
			return ;
		}
		List<AuditUserInfo> infoList = convert(viewList);
		
		int pageNo = 1;
		int pageSize = 100;
		boolean next = false;

		do {
			DataPage<AuditUserInfo> page = DataPage.getByList(infoList, pageSize, pageNo);
			DBOperateTask task = (DBOperateTask)ctx.getBean("dbOperateTask");
			task.setViewList(page.getRecord());
			task.setOpType(opType);
			pool.execute(task);
			pageNo++;
			next = page.hasNextPage();
		} while (next);
	}
	
	public void close() throws InterruptedException{
		pool.shutdown();
		pool.awaitTermination(1, TimeUnit.HOURS);
	}
	
	private List<AuditUserInfo> convert(List<UcUserView> views) {
		if (!CollectionUtils.isEmpty(views)) {
			List<AuditUserInfo> result = new ArrayList<AuditUserInfo>(views.size());
			for (UcUserView view : views) {
				Integer userId = view.getUserId() == null ? 0 : view.getUserId();
				String name = view.getName() == null ? "" : view.getName();
				String url = view.getUrl() == null ? "" : view.getUrl();
				String memo = view.getMemo() == null ? "" : view.getMemo();
				AuditUserInfo info = new AuditUserInfo(userId, name, url, memo, companyMapping.get(company), 1, 0, "");
				result.add(info);
			}
			
			return result;
		} else {
			return new ArrayList<AuditUserInfo>(0);
		}
		
	}

	public String getAdxUserAddFile() {
		return adxUserAddFile;
	}

	public void setAdxUserAddFile(String adxUserAddFile) {
		this.adxUserAddFile = adxUserAddFile;
	}

	public String getAdxUserUpdateFile() {
		return adxUserUpdateFile;
	}

	public void setAdxUserUpdateFile(String adxUserUpdateFile) {
		this.adxUserUpdateFile = adxUserUpdateFile;
	}

	public List<UserFilter> getFilterList() {
		return filterList;
	}

	public void setFilterList(List<UserFilter> filterList) {
		this.filterList = filterList;
	}

	public AuditUserServiceMgr getUserServiceMgr() {
		return userServiceMgr;
	}

	public void setUserServiceMgr(AuditUserServiceMgr userServiceMgr) {
		this.userServiceMgr = userServiceMgr;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public Map<String, Integer> getCompanyMapping() {
		return companyMapping;
	}
	public void setCompanyMapping(Map<String, Integer> companyMapping) {
		this.companyMapping = companyMapping;
	}
}
