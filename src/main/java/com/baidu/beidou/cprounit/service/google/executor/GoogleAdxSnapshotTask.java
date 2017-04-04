/**
 * beidou-cron-640#com.baidu.beidou.cprounit.service.google.GoogleAdxSnapshotTask.java
 * 下午12:06:36 created by kanghongwei
 */
package com.baidu.beidou.cprounit.service.google.executor;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.baidu.beidou.cprounit.bo.UnitAdxSnapshotVo;
import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.cprounit.service.UbmcService;
import com.baidu.beidou.cprounit.service.UnitAdxMgr;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestImageUnitWithMediaId;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestLite;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseImageUnit;
import com.baidu.beidou.util.ImageCompressUtil;
import com.baidu.beidou.util.ImageCutUtil;
import com.baidu.beidou.util.page.DataPage;

/**
 * 
 * @author kanghongwei
 * @fileName GoogleAdxSnapshotTask.java
 * @dateTime 2013-10-17 下午12:06:36
 */

public class GoogleAdxSnapshotTask implements Callable<Boolean> {

	private static final Log log = LogFactory.getLog(GoogleAdxSnapshotTask.class);

	public static final String IMAGE_FORMAT_NAME = "png";

	private int userid;

	private List<UnitAdxSnapshotVo> snapshotList;

	private UbmcService ubmcService;

	private UnitAdxMgr unitAdxMgr;

	// 截图服务重试次数
	private int snapshotRetryTime;

	private String snapshotServiceUrls;

	// flash渲染时间[ms]
	private long flashLoadTime;

	// update db间隔时间[ms]
	private long updateDBSleepTime;

	// 调用截图服务间隔时间[ms]
	private long snapshotCallInterval;

	// 截图+flash最大限制[KB]
	private int materialMaxSize;

	// 单次批量更新最大限制
	private int upadteMaxNum;

	// 浏览器-截图x轴偏移量[随浏览器变化]
	private int offsetX;

	// 浏览器-截图x轴偏移量[随浏览器变化]
	private int offsetY;

	// 截图失败-全黑色图片大小的阈值[KB]
	private int snapshotFailedThreshold;

	// 图片压缩质量[满分10分]
	private int pictureCompressQuality;

	// 广告请求内容映射
	private Map<Long, UnitAdxSnapshotVo> adRequestMap;

	// 广告截图url映射
	private Map<Long, String> adUbmcUrlMap;

	// 广告截图内容映射
	private Map<Long, byte[]> adSnapshotMap;

	// 广告截图压缩映射
	private Map<Long, byte[]> adCompressMap;

	// 广告大小过滤成功列表
	private List<Long> filterSizeSuccessList;

	// 广告截图成功列表
	private List<Long> snapshotSuccessList;

	// 广告截图失败列表
	private List<Long> snapshotFailedList;

	// 广告截图保存路径
	private String snapshotPath;

	// 广告截图文件保存与否(0:不保存，1:保存)
	private int snapshotKeepFile;

	public GoogleAdxSnapshotTask() {
		super();
	}

	public GoogleAdxSnapshotTask(int userid, List<UnitAdxSnapshotVo> snapshotList) {
		super();
		this.userid = userid;
		this.snapshotList = snapshotList;
	}

	public Boolean call() throws Exception {
		long startTime = System.currentTimeMillis();
		log.info("GoogleAdxSnapshotTask start, userid: " + userid + " snapshotList size: " + snapshotList.size());

		// 批量获取ubmc tmp url
		getTmpUbmcUrlInBatch();

		// 根据物料url进行截图
		snapshot4Url();

		// 压缩图片
		compressPicture();

		// 过滤大小超限物料
		filterUnitOutOfSize();

		// 更新ubmc物料存储
		updateUbmc4Snapshot();

		// 更新成功截图db
		updateDB4Snapshot(snapshotSuccessList, CproUnitConstant.GOOGLE_SNAPSHOT_STATE_SUCCESS);

		// 更新失败截图db
		updateDB4Snapshot(snapshotFailedList, CproUnitConstant.GOOGLE_SNAPSHOT_STATE_FAILED);

		log.info("GoogleAdxSnapshotTask end, userid: " + userid + ", expend " + (System.currentTimeMillis() - startTime) / 1000 + "s");

		return true;
	}

	public void getTmpUbmcUrlInBatch() {
		if (CollectionUtils.isEmpty(snapshotList)) {
			adUbmcUrlMap = new HashMap<Long, String>(0);
			adRequestMap = new HashMap<Long, UnitAdxSnapshotVo>(0);
			return;
		}
		List<RequestBaseMaterial> requestList = new ArrayList<RequestBaseMaterial>(snapshotList.size());

		Map<Long, Long> mcIdAdIdMap = new HashMap<Long, Long>(snapshotList.size());

		adUbmcUrlMap = new HashMap<Long, String>(snapshotList.size());
		adRequestMap = new HashMap<Long, UnitAdxSnapshotVo>(snapshotList.size());

		for (UnitAdxSnapshotVo vo : snapshotList) {
			RequestLite request = new RequestLite(vo.getMcId(), vo.getMcVersionId());
			requestList.add(request);

			adRequestMap.put(vo.getAdid(), vo);
			mcIdAdIdMap.put(vo.getMcId(), vo.getAdid());
		}

		Map<RequestBaseMaterial, String> materialUrlMap = ubmcService.generateMaterUrl(requestList);
		if (MapUtils.isEmpty(materialUrlMap)) {
			return;
		}

		for (RequestBaseMaterial material : materialUrlMap.keySet()) {
			long mcId = material.getMcId();
			long adid = mcIdAdIdMap.get(mcId);
			String ubmcUrl = materialUrlMap.get(material);

			if (StringUtils.isNotEmpty(ubmcUrl)) {
				adUbmcUrlMap.put(adid, ubmcUrl);
			}
		}

		log.info("GoogleAdxSnapshotTask:(1) getTmpUbmcUrlInBatch, userid: " + userid + " adUbmcUrlMap size: " + adUbmcUrlMap.size());

	}

	public void snapshot4Url() {
		if (MapUtils.isEmpty(adUbmcUrlMap)) {
			adSnapshotMap = new HashMap<Long, byte[]>(0);
			return;
		}

		adSnapshotMap = new HashMap<Long, byte[]>(adUbmcUrlMap.size());
		for (long adid : adUbmcUrlMap.keySet()) {
			snapshotWithRetry(adid, adUbmcUrlMap.get(adid));
		}
		log.info("GoogleAdxSnapshotTask:(2) snapshot4Url, userid: " + userid + " adSnapshotMap size: " + adSnapshotMap.size());
	}

	private void snapshotWithRetry(long adid, String flashUrl) {

		boolean snapshotSuccess = false;

		String[] serverArray = snapshotServiceUrls.split(",");

		int serverLength = serverArray.length;

		int serverIndex = 0;

		String serverUrl = null;

		for (int i = 0; (i < snapshotRetryTime && snapshotSuccess == false); i++) {

			// 随机选中“截图服务器”
			serverIndex = (int) (Math.random() * serverLength);
			serverUrl = serverArray[serverIndex];

			WebDriver driver = null;
			try {
				driver = new RemoteWebDriver(new URL(serverUrl), DesiredCapabilities.chrome());
				driver = new Augmenter().augment(driver);
				driver.get(flashUrl);

				UnitAdxSnapshotVo vo = adRequestMap.get(adid);
				int width = vo.getWidth();
				int height = vo.getHeight();

				int snapshotWidth = width + offsetX;
				int snapshotHeight = height + offsetY;

				Dimension dimension = new Dimension(snapshotWidth, snapshotHeight);
				driver.manage().window().setSize(dimension);

				// flash渲染等待
				try {
					TimeUnit.MILLISECONDS.sleep(flashLoadTime);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}

				TakesScreenshot screenShot = (TakesScreenshot) driver;
				byte[] bytes = screenShot.getScreenshotAs(OutputType.BYTES);

				// 过滤“全黑色的失败截图”
				if ((bytes == null) || (bytes.length / 1000 < snapshotFailedThreshold)) {
					snapshotSuccess = false;
					continue;
				}

				// 针对“特殊几个尺寸”的截图做“去除白边处理”
				if (CproUnitConstant.GOOGLE_SNAPSHOT_DEAL_BLANK_SIZES.isBlankSizeValid(width, height)) {
					// 从截图的字节流中获取截图的“宽”和“高”
					ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
					BufferedImage bufferedImage = ImageIO.read(inputStream);
					int snapshotedWidth = bufferedImage.getWidth();
					int snapshotedHeight = bufferedImage.getHeight();

					int xPoint = (snapshotedWidth - width) / 2;
					int yPoint = (snapshotedHeight - height) / 2;
					bytes = ImageCutUtil.cutPicture(IMAGE_FORMAT_NAME, bytes, xPoint, yPoint, width, height);
					if (bytes == null) {
						snapshotSuccess = false;
						continue;
					}
				}

				snapshotSuccess = true;
				adSnapshotMap.put(adid, bytes);

			} catch (Exception e) {
				log.error("GoogleAdxSnapshotTask: snapshot failed, e: " + e.getMessage());
				snapshotSuccess = false;
			} finally {
				if (driver != null) {
					driver.close();
					driver.quit();
				}
				if ((!snapshotSuccess) && (i < (snapshotRetryTime - 1))) {
					try {
						TimeUnit.MILLISECONDS.sleep(snapshotCallInterval);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}

		}

		if (!snapshotSuccess) {
			log.error("GoogleAdxSnapshotTask: snapshot failed, adid: " + adid + ",  url: " + flashUrl);
		}
	}

	public void compressPicture() {
		if (MapUtils.isEmpty(adSnapshotMap)) {
			adCompressMap = new HashMap<Long, byte[]>();
			return;
		}

		adCompressMap = new HashMap<Long, byte[]>(adSnapshotMap.size());
		for (long adid : adSnapshotMap.keySet()) {
			byte[] compressByte = ImageCompressUtil.compressImage(adSnapshotMap.get(adid), pictureCompressQuality * 0.1f);
			if (compressByte != null) {
				adCompressMap.put(adid, compressByte);
			}
		}
		log.info("GoogleAdxSnapshotTask:(3) compressPicture, userid: " + userid + " adCompressMap size: " + adCompressMap.size());
	}

	public void filterUnitOutOfSize() {
		if (MapUtils.isEmpty(adCompressMap)) {
			filterSizeSuccessList = new ArrayList<Long>(0);
			return;
		}
		filterSizeSuccessList = new ArrayList<Long>(adCompressMap.size());

		for (long adid : adCompressMap.keySet()) {
			UnitAdxSnapshotVo vo = adRequestMap.get(adid);
			int flashSize = getFlashSize(vo.getMcId(), vo.getMcVersionId());
			int compressPictureSize = adCompressMap.get(adid).length;

			if ((flashSize != 0) && (compressPictureSize != 0) && ((flashSize + compressPictureSize) / 1000 < materialMaxSize)) {
				filterSizeSuccessList.add(adid);
			}
		}
		log.info("GoogleAdxSnapshotTask:(4) filterUnitOutOfSize, userid: " + userid + " filterSizeSuccessList size: " + filterSizeSuccessList.size());
	}

	private int getFlashSize(long mcId, int mcVersionId) {

		int flashSize = 0;

		List<RequestBaseMaterial> units = new LinkedList<RequestBaseMaterial>();
		RequestLite request = new RequestLite(mcId, mcVersionId);
		units.add(request);

		List<ResponseBaseMaterial> result = ubmcService.get(units, false);

		if (CollectionUtils.isEmpty(result)) {
			return flashSize;
		}

		ResponseImageUnit response = (ResponseImageUnit) result.get(0);

		String fileSrc = response.getFileSrc();
		String[] attributes = response.getAttribute().split(",");

		List<Long> mediaIdList = new ArrayList<Long>();
		mediaIdList.add(ubmcService.getMediaIdFromFileSrc(fileSrc));

		if (!ArrayUtils.isEmpty(attributes)) {
			for (int i = 0; i < attributes.length; i++) {
				mediaIdList.add(ubmcService.getMediaIdFromFileSrc(attributes[i]));
			}
		}

		List<byte[]> byteList = ubmcService.getMediaData(mediaIdList);
		if (CollectionUtils.isNotEmpty(byteList)) {
			for (byte[] innerByte : byteList) {
				if (innerByte != null) {
					flashSize += innerByte.length;
				}
			}
		}

		return flashSize;
	}

	public void updateUbmc4Snapshot() {
		snapshotFailedList = new ArrayList<Long>(adRequestMap.size());
		if (CollectionUtils.isEmpty(filterSizeSuccessList)) {
			snapshotSuccessList = new ArrayList<Long>(0);
			for (long adid : adRequestMap.keySet()) {
				snapshotFailedList.add(adid);
			}
			return;
		}

		snapshotSuccessList = new ArrayList<Long>(filterSizeSuccessList.size());

		for (long adid : filterSizeSuccessList) {
			// 逐个更新flash的ubmc的截图
			UnitAdxSnapshotVo vo = adRequestMap.get(adid);
			boolean isUpdateUbmcSuccess = updateUbmc(vo.getMcId(), vo.getMcVersionId(), adCompressMap.get(adid));
			if (isUpdateUbmcSuccess) {
				snapshotSuccessList.add(adid);

				// 保留“截图文件”
				if (snapshotKeepFile == 1) {
					String fileName = snapshotPath + adid + "." + IMAGE_FORMAT_NAME;
					archiveSnapshotFile(adCompressMap.get(adid), fileName);
				}
			}
		}

		for (long adid : adRequestMap.keySet()) {
			if (!snapshotSuccessList.contains(adid)) {
				snapshotFailedList.add(adid);
			}
		}
		log.info("GoogleAdxSnapshotTask:(5) updateUbmc4Snapshot, userid: " + userid + " snapshotSuccessList size: " + snapshotSuccessList.size());
		log.info("GoogleAdxSnapshotTask:(5) updateUbmc4Snapshot, userid: " + userid + " snapshotFailedList size: " + snapshotFailedList.size());
	}

	private void archiveSnapshotFile(byte bytes[], String fileName) {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(fileName, true);
			out.write(bytes);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException iex) {
			}
		}
	}

	private boolean updateUbmc(long mcId, int mcVersionId, byte[] compressSnapshot) {
		List<RequestBaseMaterial> units = new LinkedList<RequestBaseMaterial>();
		RequestLite request = new RequestLite(mcId, mcVersionId);
		units.add(request);

		List<ResponseBaseMaterial> resultGet = ubmcService.get(units, false);
		if (CollectionUtils.isEmpty(resultGet)) {
			return false;
		}

		ResponseImageUnit responseUnit = (ResponseImageUnit) resultGet.get(0);
		List<RequestBaseMaterial> requestUpdates = new LinkedList<RequestBaseMaterial>();
		RequestBaseMaterial requestUpdate = new RequestImageUnitWithMediaId(responseUnit.getMcId(), responseUnit.getVersionId(), responseUnit.getWuliaoType(), responseUnit.getTitle(), responseUnit.getShowUrl(), responseUnit.getTargetUrl(), responseUnit.getWirelessShowUrl(), responseUnit
				.getWirelessTargetUrl(), responseUnit.getWidth(), responseUnit.getHeight(), responseUnit.getFileSrc(), responseUnit.getFileSrcMd5(), responseUnit.getAttribute(), responseUnit.getRefMcId(), responseUnit.getDescInfo(), compressSnapshot);
		requestUpdates.add(requestUpdate);

		List<ResponseBaseMaterial> resultUpdates = ubmcService.update(requestUpdates);
		if (CollectionUtils.isNotEmpty(resultUpdates)) {
			ResponseImageUnit updateUnit = (ResponseImageUnit) resultUpdates.get(0);
			if (StringUtils.isNotEmpty(updateUnit.getSnapshot())) {
				return true;
			}
		}
		return false;
	}

	public void updateDB4Snapshot(List<Long> adIdList, int snapshotState) {
		if (CollectionUtils.isEmpty(adIdList)) {
			return;
		}
		// 分批更新数据库
//		List<List<Long>> adIdPageList = PageUtil.pageAds(adIdList, upadteMaxNum);
//		for (List<Long> adIdListInPage : adIdPageList) {
		int pageNo = 1;
		boolean next = false;
		do {
			DataPage<Long> adIdListInPage = DataPage.getByList(adIdList, upadteMaxNum, pageNo);
			unitAdxMgr.updateGoogleAdxSnapshotState(userid, adIdListInPage.getRecord(), snapshotState);
			next = adIdListInPage.hasNextPage();
			pageNo++;

			try {
				TimeUnit.MILLISECONDS.sleep(updateDBSleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		} while (next);
		
		log.info("GoogleAdxSnapshotTask:(6) updateDB4Snapshot, userid: " + userid + " adIdList size: " + adIdList.size() + ", snapshotState" + snapshotState);
	}

	public void setUbmcService(UbmcService ubmcService) {
		this.ubmcService = ubmcService;
	}

	public void setUserid(int userid) {
		this.userid = userid;
	}

	public void setSnapshotList(List<UnitAdxSnapshotVo> snapshotList) {
		this.snapshotList = snapshotList;
	}

	public void setUnitAdxMgr(UnitAdxMgr unitAdxMgr) {
		this.unitAdxMgr = unitAdxMgr;
	}

	public void setSnapshotRetryTime(int snapshotRetryTime) {
		this.snapshotRetryTime = snapshotRetryTime;
	}

	public void setMaterialMaxSize(int materialMaxSize) {
		this.materialMaxSize = materialMaxSize;
	}

	public void setUpadteMaxNum(int upadteMaxNum) {
		this.upadteMaxNum = upadteMaxNum;
	}

	public void setOffsetX(int offsetX) {
		this.offsetX = offsetX;
	}

	public void setOffsetY(int offsetY) {
		this.offsetY = offsetY;
	}

	public void setFlashLoadTime(long flashLoadTime) {
		this.flashLoadTime = flashLoadTime;
	}

	public void setSnapshotFailedThreshold(int snapshotFailedThreshold) {
		this.snapshotFailedThreshold = snapshotFailedThreshold;
	}

	public void setUpdateDBSleepTime(long updateDBSleepTime) {
		this.updateDBSleepTime = updateDBSleepTime;
	}

	public void setSnapshotCallInterval(long snapshotCallInterval) {
		this.snapshotCallInterval = snapshotCallInterval;
	}

	public void setPictureCompressQuality(int pictureCompressQuality) {
		this.pictureCompressQuality = pictureCompressQuality;
	}

	public void setSnapshotServiceUrls(String snapshotServiceUrls) {
		this.snapshotServiceUrls = snapshotServiceUrls;
	}

	public void setSnapshotPath(String snapshotPath) {
		this.snapshotPath = snapshotPath;
	}

	public void setSnapshotKeepFile(int snapshotKeepFile) {
		this.snapshotKeepFile = snapshotKeepFile;
	}

}
