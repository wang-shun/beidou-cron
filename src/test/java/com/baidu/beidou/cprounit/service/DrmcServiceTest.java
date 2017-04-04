package com.baidu.beidou.cprounit.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.base.BaseMultiDataSourceTest;
import com.baidu.beidou.cprounit.service.bo.BeidouMaterialBase;
import com.baidu.beidou.cprounit.service.bo.request.RequestImageMaterial2;

@TransactionConfiguration(transactionManager = "addbTransactionManager")
public class DrmcServiceTest extends BaseMultiDataSourceTest {
	
	@Autowired
	DrmcService drmcService;

	@Test
	public void testGetElements() {
		// 创建文字物料
		
		List<BeidouMaterialBase> result = drmcService.getElements(new long[]{2001649607,2001649605,2001649297}, false);
		Assert.assertEquals(1, result.size());
		
	}
	
	@Test
	public void testInsertImageUnit() {
		String title = "1111111111111111";
		String showUrl = "baidu.com";
		String targetUrl = "http://baidu.com";
		String wirelessShowUrl = "";
		String wirelessTargetUrl = "";
		Integer width = 200;
		Integer height = 200;
		String drmcUrl = "http://db-testing-ecom43.db01.baidu.com:8810/media/id=nW0snjDvPW03rf&gp=403&time=nHnLPjTYnWm1rf.swf";
		
		List<BeidouMaterialBase> ads = new ArrayList<BeidouMaterialBase>();
		RequestImageMaterial2 flash = new RequestImageMaterial2(title, showUrl, targetUrl,
				drmcUrl, width, height, wirelessShowUrl, wirelessTargetUrl);
		ads.add(flash);
		List<BeidouMaterialBase> result = drmcService.tmpInsertBatch(ads, false);
		
		Assert.assertEquals(1, result.size());
	}
	
	private byte[] getImageData(String fileName) {
		byte[] data=null;
		fileName = getImageFilePath(fileName);
		FileInputStream input = null;
		File tmpFile = new File(fileName);
		try {
			long fileSize = tmpFile.length();
			data = new byte[(int) fileSize];
			input = new FileInputStream(tmpFile);
			input.read(data);
			input.close();
		} catch (IOException e) {
			System.out.println("image data not found");
			data = null;
		}
		
		return data;
	}
	
	private String getImageFilePath(String fileName) {
		return new File("").getAbsolutePath() + "\\src\\test\\java\\com\\baidu\\beidou\\cprounit\\images\\" + fileName;
	}
}
