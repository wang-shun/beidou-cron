package com.baidu.beidou.cprounit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.cprounit.bo.Unit;
import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.constant.CproUnitConstant.DRMC_CHECKCODEMAT_ERRORINFO;
import com.baidu.beidou.cprounit.service.CproUnitMgr;
import com.baidu.beidou.cprounit.service.CproUnitWriteMgr;
import com.baidu.beidou.cprounit.service.UbmcService;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestImageUnitWithData;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseImageUnit;
import com.baidu.beidou.util.MD5;
import com.baidu.beidou.util.ThreadContext;

/**
 * @author liuhao05
 * @time 2012.07.02
 */
public class ReplaceDrmcMater {

	private static final Log log = LogFactory.getLog(ReplaceDrmcMater.class);

	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			log.error("Number of args must be 3");
			return;
		}
		
		// 1. spring配置导入，以便获取相关Service的bean
		String[] paths = new String[] { "applicationContext.xml","classpath:/com/baidu/beidou/user/applicationContext.xml" };
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(paths);
		CproUnitMgr unitMgr = (CproUnitMgr) ctx.getBean("unitMgr");
		CproUnitWriteMgr unitWriteMgr = (CproUnitWriteMgr) ctx.getBean("unitWriteMgr");
		UbmcService ubmcService = (UbmcService) ctx.getBean("ubmcService");

		String csvPath = args[0];
		String imagesPath = args[1];
		String logInfoPath = args[2];

		try {
			// 2. 读取csv文件内容
			File csv = new File(csvPath);
			
			File logInfo = new File(logInfoPath);
			BufferedReader br = new BufferedReader(new FileReader(csv));
			BufferedWriter bw = new BufferedWriter(new FileWriter(logInfo,true));
			String line = null;
			Integer userid = null;
			Integer unitid = null;
			String suffix = null;
			String[] csvLine = null;
			SimpleDateFormat fm=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String date = fm.format(new Date());
			bw.write("BeginTime : "+date);
			bw.newLine();
			while (br.ready()) {
				int wuliaoType = CproUnitConstant.MATERIAL_TYPE_PICTURE;
				try {
					line = br.readLine();
					csvLine = line.split(",");
					userid = Integer.parseInt(csvLine[0]);
					unitid = Integer.parseInt(csvLine[1]);
					suffix = "." + csvLine[2];
					
					if (csvLine[2].equalsIgnoreCase("swf")) {
						wuliaoType = CproUnitConstant.MATERIAL_TYPE_FLASH;
					}
				} catch (Exception ex) {
					log.error(DRMC_CHECKCODEMAT_ERRORINFO.ERROR_READINGCSV);
					bw.write(csvLine[1] + ",failed," + DRMC_CHECKCODEMAT_ERRORINFO.ERROR_READINGCSV);
					bw.newLine();
					continue;
				}
				
				ThreadContext.putUserId(userid);
				
				Unit unit = unitMgr.findUnitById(userid, new Long(unitid));
				if ((unit != null) && ( unit.getState() == CproUnitConstant.UNIT_STATE_NORMAL || unit.getState() == CproUnitConstant.UNIT_STATE_PAUSE )) {
					byte[] data = readDataFromImage(imagesPath + "/" + unitid + suffix);
					String fileSrcMd5 = MD5.getMd5(data);
					unit.getMaterial().setData(data);
					
					// 3. 获取物料内容后，插入UBMC
					List<RequestBaseMaterial> requests = new LinkedList<RequestBaseMaterial>();
					RequestBaseMaterial request = new RequestImageUnitWithData(unit.getMaterial().getMcId(), 
							unit.getMaterial().getMcVersionId(), wuliaoType, 
							unit.getMaterial().getTitle(), unit.getMaterial().getShowUrl(), 
							unit.getMaterial().getTargetUrl(), unit.getMaterial().getWirelessShowUrl(), 
							unit.getMaterial().getWirelessTargetUrl(), CproUnitConstant.DRMC_CHECKCODEMAT_WIDTH, 
							CproUnitConstant.DRMC_CHECKCODEMAT_HEIGHT, data, fileSrcMd5, 
							null, null, null, null);
					requests.add(request);
					List<ResponseBaseMaterial> result = ubmcService.update(requests);
					if (CollectionUtils.isEmpty(result) || result.get(0) == null) {
						bw.write(unitid + ",failed," + DRMC_CHECKCODEMAT_ERRORINFO.ERROR_DRMCTMP);
						bw.newLine();
						log.error(DRMC_CHECKCODEMAT_ERRORINFO.ERROR_DRMCTMP);
						continue;
					}
					
					ResponseBaseMaterial response = result.get(0);
					if (response == null || !(response instanceof ResponseImageUnit)) {
						bw.write(unitid + ",failed," + DRMC_CHECKCODEMAT_ERRORINFO.ERROR_DRMCACTIVE);
						bw.newLine();
						log.error(DRMC_CHECKCODEMAT_ERRORINFO.ERROR_DRMCACTIVE);
						continue;
					}
					ResponseImageUnit responseImage = (ResponseImageUnit)response;
					
					unit.getMaterial().setFileSrc(responseImage.getFileSrc());
					unit.getMaterial().setFileSrcMd5(fileSrcMd5);
					unit.getMaterial().setHeight(responseImage.getHeight());
					unit.getMaterial().setWidth(responseImage.getWidth());
					unit.getMaterial().setMcId(responseImage.getMcId());
					unit.getMaterial().setMcVersionId(responseImage.getVersionId());
					unit.getMaterial().setUbmcsyncflag(CproUnitConstant.UBMC_SYNC_FLAG_YES);
					
					if (wuliaoType == CproUnitConstant.MATERIAL_TYPE_FLASH) {
						unit.getMaterial().setWuliaoType(CproUnitConstant.MATERIAL_TYPE_FLASH);
						unit.getMaterial().setPlayer(7);
					} else {
						unit.getMaterial().setWuliaoType(CproUnitConstant.MATERIAL_TYPE_PICTURE);
						unit.getMaterial().setPlayer(0);
					}
					unit.setChaTime(new Date());
					
					// 5. 将创意信息写入db
					unitWriteMgr.modUnitInfo(userid, unit);						
					bw.write(unitid + ",success");
					bw.newLine();
				} else {
					bw.write(unitid + ",failed," + DRMC_CHECKCODEMAT_ERRORINFO.ERROR_UNITERROR);
					bw.newLine();
					log.error(DRMC_CHECKCODEMAT_ERRORINFO.ERROR_UNITERROR);
				}
			}
			date = fm.format(new Date());
			bw.write("EndTime : "+date);
			bw.newLine();
			bw.flush();
			bw.close();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * readDataFromImage: 获取图片二进制内容
	 * @version cpweb-587
	 * @author genglei01
	 * @date Apr 22, 2013
	 */
	public static byte[] readDataFromImage(String path) {
		byte[] data = null;
		File tmpFile = new File(path);
		if (tmpFile.exists()) {
			FileInputStream fis = null;
			try {
				long fileSize = tmpFile.length();
				data = new byte[(int) fileSize];
				fis = new FileInputStream(tmpFile);
				fis.read(data);
				fis.close();

			} catch (IOException e) {
				return null;
			}
		}
		return data;
	}
}
