package com.baidu.beidou.cprounit.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.baidu.beidou.base.BaseMultiDataSourceTest;
import com.baidu.beidou.cprounit.mcdriver.bean.response.GrantResult;
import com.baidu.beidou.cprounit.mcdriver.mcparser.ParseMC;
import com.baidu.beidou.cprounit.service.syncubmc.vo.UnitMaterCheckView;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestGroup;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestIconMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestIconUnitWithData;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestIconUnitWithMediaId;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestImageUnitWithData;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestImageUnitWithMediaId;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestLite;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestMaterialForTest;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestTextUnit;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseGroup;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseIconMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseIconUnit;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseImageUnit;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseLite;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseMaterialForTest;
import com.baidu.beidou.cprounit.ubmcdriver.material.response.ResponseTextUnit;
import com.baidu.beidou.util.MD5;
import com.baidu.chuangyi.flash.decode.DecodeResult;
import com.baidu.chuangyi.flash.decode.FlashDecoder;

@TransactionConfiguration(transactionManager = "addbTransactionManager")
public class UbmcServiceTest extends BaseMultiDataSourceTest {
	
	@Autowired
	UbmcService ubmcService;
	@Autowired
	RecompileCreativeService recompileCreativeService;

//	@Test
	public void testInsertTextUnit() {
		// 创建文字物料
		List<RequestBaseMaterial> units = new LinkedList<RequestBaseMaterial>();
		RequestTextUnit request = new RequestTextUnit(null, null, "title: 你好:", "desc1: 123", 
				"desc2: 123", "www.baidu.com", "http://www.baidu.com", "www.baidu.com", "http://www.baidu.com");
		units.add(request);
		
		List<ResponseBaseMaterial> result = ubmcService.insert(units);
		Assert.assertEquals(1, result.size());
		
		ResponseTextUnit response = (ResponseTextUnit)result.get(0);
		Assert.assertEquals(request.getTitle(), response.getTitle());
	}
	
//	@Test
	public void testInsertIconMaterial() {
		// 获取图片二进制byte数组
		byte[] data = getImageData("icon.jpg");
		String fileSrcMd5 = MD5.getMd5(data);
		
		List<RequestBaseMaterial> units = new LinkedList<RequestBaseMaterial>();
		RequestIconMaterial request = new RequestIconMaterial(null, null, 60, 60, data, fileSrcMd5);
		units.add(request);
		
		List<ResponseBaseMaterial> result = ubmcService.insert(units);
		Assert.assertEquals(1, result.size());
		
		ResponseIconMaterial response = (ResponseIconMaterial)result.get(0);
		Assert.assertEquals(request.getHeight(), response.getHeight());
	}
	
//	@Test
	public void testInsertIconUnitWithData() {
		// 获取图片二进制byte数组
		byte[] data = getImageData("icon.jpg");
		String fileSrcMd5 = MD5.getMd5(data);
		
		List<RequestBaseMaterial> units = new LinkedList<RequestBaseMaterial>();
		RequestIconUnitWithData request = new RequestIconUnitWithData(null, null, "title: 123", "desc1: 123", 
				"desc2: 123", "www.baidu.com", "http://www.baidu.com", "www.baidu.com", 
				"http://www.baidu.com", 60, 60, data, fileSrcMd5);
		units.add(request);
		
		List<ResponseBaseMaterial> result = ubmcService.insert(units);
		Assert.assertEquals(1, result.size());
		
		ResponseIconUnit response = (ResponseIconUnit)result.get(0);
		Assert.assertEquals(request.getTitle(), response.getTitle());
		String fileSrc = response.getFileSrc();
		System.out.println("fileSrc:" + fileSrc + ", ascii(1) index:" + fileSrc.indexOf("\1"));
	}
	
	@Test
	public void testInsertIconForChuchuang() {
		File directory = new File("");// 设定为当前文件夹
		try {
			System.out.println(directory.getCanonicalPath());// 获取标准的路径
			System.out.println(directory.getAbsolutePath());// 获取绝对路径
		} catch (IOException e) {
		}
		
		String inputPath = ".\\image";
		directory = new File(inputPath);// 设定为当前文件夹
		
		String outputFileName = "output.txt";
		PrintWriter output = null;
		try {
			output = new PrintWriter(new File(outputFileName), "GBK");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		File[] list = directory.listFiles();
		for (int i = 0; i < list.length; i++) {
			System.out.println(list[i].toString());
			
			// 获取图片二进制byte数组
			byte[] data = null;
			String fileName = list[i].toString();
			
			String imageFileName = list[i].getName();
			
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
			}
			String fileSrcMd5 = MD5.getMd5(data);
			
			List<RequestBaseMaterial> units = new LinkedList<RequestBaseMaterial>();
			RequestIconMaterial request = new RequestIconMaterial(null, null, 60, 60, data, fileSrcMd5);
			units.add(request);
			
			List<ResponseBaseMaterial> result = ubmcService.insert(units);
			if (CollectionUtils.isEmpty(result) || result.get(0) == null) {
				System.out.print("[FAIL]insert failed" + "\t" + fileName);
			} else {
				ResponseIconMaterial response = (ResponseIconMaterial)result.get(0);
				String fileSrc = response.getFileSrc();
				Long mediaId = ubmcService.getMediaIdFromFileSrc(fileSrc);
				output.println(imageFileName + "\t" + mediaId);
				output.flush();
			}
		}
		
		try {
			output.flush();
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}

    @Test
    public void testInsertImageForWebp() {
        String inputPath = ".\\images";
        File directory = new File(inputPath);// 设定为当前文件夹
        
        String outputFileName = "output.txt";
        PrintWriter output = null;
        try {
            output = new PrintWriter(new File(outputFileName), "GBK");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        File[] list = directory.listFiles();
        for (int i = 0; i < list.length; i++) {
            System.out.println(list[i].toString());
            
            // 获取图片二进制byte数组
            byte[] data = null;
            String fileName = list[i].toString();
            
            String imageFileName = list[i].getName();
            if (imageFileName.contains("gif")) {
                continue;
            }
            
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
            }
            
            List<RequestBaseMaterial> units = new LinkedList<RequestBaseMaterial>();
            RequestMaterialForTest request = new RequestMaterialForTest(null, null, "webp", data);
            units.add(request);
            
            List<ResponseBaseMaterial> result = ubmcService.insert(units);
            if (CollectionUtils.isEmpty(result) || result.get(0) == null) {
                System.out.print("[FAIL]insert failed" + "\t" + fileName);
            } else {
                ResponseMaterialForTest response = (ResponseMaterialForTest)result.get(0);
                String fileSrc = response.getFileSrc();
                Long mediaId = ubmcService.getMediaIdFromFileSrc(fileSrc);
                output.println(imageFileName
                        + "\t" + response.getMcId()
                        + "\t" + response.getVersionId()
                        + "\t" + mediaId);
                output.flush();
            }
        }
        
        try {
            output.flush();
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    @Test
    public void testInsertImageForOther() {
        String inputPath = ".\\images";
        File directory = new File(inputPath);// 设定为当前文件夹
        
        String outputFileName = "output.txt";
        PrintWriter output = null;
        try {
            output = new PrintWriter(new File(outputFileName), "GBK");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        File[] list = directory.listFiles();
        for (int i = 0; i < list.length; i++) {
            System.out.println(list[i].toString());
            
            // 获取图片二进制byte数组
            byte[] data = null;
            String fileName = list[i].toString();
            
            String imageFileName = list[i].getName();
            
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
            }
            
            List<RequestBaseMaterial> units = new LinkedList<RequestBaseMaterial>();
            RequestMaterialForTest request = new RequestMaterialForTest(null, null, "jpg", data);
            units.add(request);
            
            List<ResponseBaseMaterial> result = ubmcService.insert(units);
            if (CollectionUtils.isEmpty(result) || result.get(0) == null) {
                System.out.print("[FAIL]insert failed" + "\t" + fileName);
            } else {
                ResponseMaterialForTest response = (ResponseMaterialForTest)result.get(0);
                String fileSrc = response.getFileSrc();
                Long mediaId = ubmcService.getMediaIdFromFileSrc(fileSrc);
                output.println(imageFileName + "\t" + response.getMcId() 
                        + "\t" + response.getVersionId() + "\t" + mediaId);
                output.flush();
            }
        }
        
        try {
            output.flush();
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    @Test
    public void testInsertFlv() {
        String inputFile = "suning418v2.flv";
        
        // 获取图片二进制byte数组
        byte[] data = getImageData(inputFile);
        if (data == null) {
            return ;
        }
        
        List<RequestBaseMaterial> units = new LinkedList<RequestBaseMaterial>();
        RequestMaterialForTest request = new RequestMaterialForTest(null, null, "flv", data);
        units.add(request);
        
        List<ResponseBaseMaterial> result = ubmcService.insert(units);
        if (CollectionUtils.isEmpty(result) || result.get(0) == null) {
            System.out.print("[FAIL]insert failed" + "\t" + inputFile);
        } else {
            ResponseMaterialForTest response = (ResponseMaterialForTest)result.get(0);
            String fileSrc = response.getFileSrc();
            Long mediaId = ubmcService.getMediaIdFromFileSrc(fileSrc);
            System.out.println(inputFile + "\t" + response.getMcId() + "\t" + mediaId);
        }
        
    }

    @Test
    public void testGetUrlForFile() {
        String inputFileName = "unit_top500.log";
        String outputFileName = "output.txt";
        BufferedReader reader = null;
        PrintWriter output = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFileName), "UTF-8"));
            output = new PrintWriter(new File(outputFileName), "GBK");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        String line = null;
        try {
            while ((line = reader.readLine()) != null && (!StringUtils.isEmpty(line))) {
                String[] items = line.split("\t");
                Long mcId = Long.valueOf(items[1]);
                Integer versionId = Integer.valueOf(items[2]);
                
                String url = ubmcService.getTmpUrl(mcId, versionId);
                if (StringUtils.isEmpty(url)) {
                    System.out.println("generate tmp url from ubmc failed for [" + line + "]");
                    continue;
                }
                
                output.println(line + "\t" + url);
                output.flush();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        try {
            output.flush();
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    @Test
    public void testFixAttachId() {
        String inputFileName = "todo_group_attach.txt";
        String outputFileName = "output.txt";
        BufferedReader reader = null;
        PrintWriter output = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFileName), "UTF-8"));
            output = new PrintWriter(new File(outputFileName), "GBK");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        String line = null;
        try {
            while ((line = reader.readLine()) != null && (!StringUtils.isEmpty(line))) {
                String[] items = line.split("\t");
                
                Integer groupId = Integer.valueOf(items[0]);
                Long mcId = Long.valueOf(items[1]);
                Integer versionId = Integer.valueOf(items[2]);
                
//                String url = ubmcService.getTmpUrl(mcId, versionId);
//                if (StringUtils.isEmpty(url)) {
//                    System.out.println("generate tmp url from ubmc failed for [" + line + "]");
//                    continue;
//                }
                List<RequestBaseMaterial> preRequests = new LinkedList<RequestBaseMaterial>();
                RequestLite preReq = new RequestLite(mcId, versionId);
                preRequests.add(preReq);
                List<ResponseBaseMaterial> preResponses = ubmcService.get(preRequests, false);
                if (CollectionUtils.isEmpty(preResponses) || preResponses.get(0) == null) {
                    System.out.println("data is null, get attach info from ubmc failed for [" + line + "]");
                }
                
                ResponseGroup preRes = (ResponseGroup) preResponses.get(0);
                
                if (preRes.getPhoneId() < 0 || preRes.getMsgPhoneId() < 0) {
                    Long phoneId = preRes.getPhoneId();
                    if (phoneId < 0) {
                        phoneId = transNegative(phoneId);
                    }
                    
                    Long msgPhoneId = preRes.getMsgPhoneId();
                    if (msgPhoneId < 0) {
                        msgPhoneId = transNegative(msgPhoneId);
                    }
                
                    List<RequestBaseMaterial> requests = new LinkedList<RequestBaseMaterial>();
                    List<ResponseBaseMaterial> result = null;
                    RequestGroup request = null;
                    // 根据附加信息类型，修改相应字段
                    request = new RequestGroup(preRes.getMcId(), preRes.getVersionId(), groupId, phoneId, 
                            preRes.getPhone(), msgPhoneId, preRes.getMsgPhone(), preRes.getMsgContent(), 
                            preRes.getSubUrlParam(), preRes.getSubUrlTitle(), 
                            preRes.getSubUrlLink(), preRes.getSubUrlWirelessLink());
                    requests.add(request);
                    result = ubmcService.update(requests);
                    if (CollectionUtils.isEmpty(result) || result.get(0) == null) {
                        System.out.println("data is null, update attach info into ubmc failed for [" + line + "]");
                    } else {
                        output.println(line + "\t" + preRes.getPhoneId() + "\t" + phoneId
                                + "\t" + preRes.getMsgPhoneId() + "\t" + msgPhoneId);
                        output.flush();
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        try {
            output.flush();
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private static long transNegative(long x) {
        return x - (Integer.MIN_VALUE * 2L);
    }
	
//	@Test
	public void testInsertIconUnitWithEmptyData() {
		// 获取图片二进制byte数组
		byte[] data = new byte[0];
		String fileSrcMd5 = MD5.getMd5(data);
		
		List<RequestBaseMaterial> units = new LinkedList<RequestBaseMaterial>();
		RequestIconUnitWithData request = new RequestIconUnitWithData(null, null, "title: 123", "desc1: 123", 
				"desc2: 123", "www.baidu.com", "http://www.baidu.com", "www.baidu.com", 
				"http://www.baidu.com", 60, 60, data, fileSrcMd5);
		units.add(request);
		
		List<ResponseBaseMaterial> result = ubmcService.insert(units);
		Assert.assertEquals(1, result.size());
		
		ResponseIconUnit response = (ResponseIconUnit)result.get(0);
		Assert.assertEquals(request.getTitle(), response.getTitle());
		String fileSrc = response.getFileSrc();
		System.out.println("fileSrc:" + fileSrc + ", ascii(1) index:" + fileSrc.indexOf("\1"));
	}
	
//	@Test
	public void testInsertIconUnitWithMediaId() {
		List<RequestBaseMaterial> units = new LinkedList<RequestBaseMaterial>();
		RequestIconUnitWithMediaId request = new RequestIconUnitWithMediaId(0L, 1, "title: sdfasfsdfas", "desc1: sfdsfdsfsda", 
				"desc2: 123", "wenhua.sh.cn", "http://wenhua.sh.cn", "", 
				"", 60, 60, "fileSrc:%%BEGIN_MEDIA%%mediaid=27785591\1type=jpg%%END_MEDIA%%", "b0348036bc09c42148b74a083ad5f7cf");
		units.add(request);
		
		List<ResponseBaseMaterial> result = ubmcService.insert(units);
		Assert.assertEquals(1, result.size());
		
		ResponseIconUnit response = (ResponseIconUnit)result.get(0);
		Assert.assertEquals(request.getTitle(), response.getTitle());
	}
	
//	@Test
	public void testInsertImageUnitWiteData() {
		// 获取图片二进制byte数组
		byte[] data = getImageData("image_300x250.jpg");
		String fileSrcMd5 = MD5.getMd5(data);
		
		List<RequestBaseMaterial> units = new LinkedList<RequestBaseMaterial>();
		RequestImageUnitWithData request = new RequestImageUnitWithData(null, null, 2, "title: 123", "www.baidu.com", 
				"http://www.baidu.com", "www.baidu.com", "http://www.baidu.com", 360, 300, data, "", "", "", "", null);
		units.add(request);
		
		List<ResponseBaseMaterial> result = ubmcService.insert(units);
		Assert.assertEquals(1, result.size());
		
		ResponseImageUnit response = (ResponseImageUnit)result.get(0);
		Assert.assertEquals(request.getTitle(), response.getTitle());
	}
	
//	@Test
	public void testInsertImageUnitWiteMediaId() {
		List<RequestBaseMaterial> units = new LinkedList<RequestBaseMaterial>();
		RequestImageUnitWithMediaId request = new RequestImageUnitWithMediaId(null, null, 2, "title: 123", "www.baidu.com", 
				"http://www.baidu.com", "www.baidu.com", "http://www.baidu.com", 60, 60, 
				"fileSrc:%%BEGIN_MEDIA%%mediaid=25460945\1type=jpg%%END_MEDIA%%", "md5", "", "", "", null);
		units.add(request);
		
		List<ResponseBaseMaterial> result = ubmcService.insert(units);
		Assert.assertEquals(1, result.size());
		
		ResponseImageUnit response = (ResponseImageUnit)result.get(0);
		Assert.assertEquals(request.getTitle(), response.getTitle());
	}
	
//	@Test
	public void testInsertWithNullResponse() {
		List<RequestBaseMaterial> units = new LinkedList<RequestBaseMaterial>();
		RequestTextUnit request = new RequestTextUnit(null, null, "title: 123", "desc1: 123", 
				"desc2: 123", "www.baidu.com", "http://www.baidu.com", "www.baidu.com", "http://www.baidu.com");
		units.add(request);
		units.add(null);
		
		List<ResponseBaseMaterial> result = ubmcService.insert(units);
		Assert.assertEquals(0, result.size());
	}
	
	/**
	 * Function: testInsertRequestGroup
	 * 
	 * @author genglei01 
	 */

//  @Test
    public void testInsertRequestGroup() {
        List<RequestBaseMaterial> units = new LinkedList<RequestBaseMaterial>();
        RequestGroup request = new RequestGroup(0L, 1, 12001661, 0L, "", 0L, "", "",
                "", "体育,新闻,天气,汽车", "",
                "http://sports.sina.com.cn/,http://news.sina.com.cn/,"
                        + "http://weather.sina.com.cn/,http://auto.sina.com.cn/");
        units.add(request);
        
        List<ResponseBaseMaterial> result = ubmcService.insert(units);
        Assert.assertEquals(0, result.size());
    }
	
//	@Test
	public void testUpdate() {
		// 更新某个图文创意
		List<RequestBaseMaterial> units = new LinkedList<RequestBaseMaterial>();
		RequestIconUnitWithMediaId request = new RequestIconUnitWithMediaId(2097632L, 1, "title: 123-update", "desc1: 123", 
				"desc2: 123", "www.baidu.com", "http://www.baidu.com", "www.baidu.com", 
				"http://www.baidu.com", 60, 60, "fileSrc:%%BEGIN_MEDIA%%mediaid=25460945\1type=jpg%%END_MEDIA%%", "md5");
		units.add(request);
		
		List<ResponseBaseMaterial> result = ubmcService.update(units);
		Assert.assertEquals(1, result.size());
		
		ResponseIconUnit response = (ResponseIconUnit)result.get(0);
		Assert.assertEquals(request.getTitle(), response.getTitle());
	}
	
	
	@Test
	public void testUpdateFix() {
		// 获取某个图标信息，不生成预览URL，返回fileSrc的mediaId
		List<RequestBaseMaterial> units = new LinkedList<RequestBaseMaterial>();
		RequestLite request = new RequestLite(300798343L, 1);
		units.add(request);
//		request = new RequestLite(917904563L, 1);
//		units.add(request);
		List<ResponseBaseMaterial> result = ubmcService.get(units, false);
		
		// 更新某个图文创意
		List<RequestBaseMaterial> unitUpdates = new LinkedList<RequestBaseMaterial>();
		ResponseImageUnit response = (ResponseImageUnit)result.get(0);
		
		
		String url = "http://www.meilele.com/?se=bc!X18p!X10d!X!QA8AE5B1BBE7BABAE4BEA4E7B8B0E58EBFE8A!H-60!X301!QAAFE7B687E69D97E5A!Hflash";
		System.out.println(UnitBeanUtils.compareString(url, response.getTargetUrl()));
		System.out.println(response.getTargetUrl());
		
		RequestImageUnitWithMediaId requestUpdate = new RequestImageUnitWithMediaId(300798343L, 1, response.getWuliaoType(),
				response.getTitle(), response.getShowUrl(), url, response.getWirelessShowUrl(), 
				response.getWirelessTargetUrl(), response.getWidth(), response.getHeight(), response.getFileSrc(), 
				response.getFileSrcMd5(), response.getAttribute(), response.getRefMcId(), response.getDescInfo(), null);
		unitUpdates.add(requestUpdate);
		
		List<ResponseBaseMaterial> resultUpdates = ubmcService.update(unitUpdates);
		Assert.assertEquals(1, result.size());
		
		result = ubmcService.get(units, false);
		System.out.println(((ResponseImageUnit)result.get(0)).getTargetUrl());
	}
	
//	@Test
	public void testRemove() {
		// 删除某个创意，返回结果仅包含mcId和versionId
		List<RequestBaseMaterial> units = new LinkedList<RequestBaseMaterial>();
		RequestLite request = new RequestLite(50564549L, 1);
		units.add(request);
		
		List<ResponseBaseMaterial> result = ubmcService.remove(units);
		Assert.assertEquals(1, result.size());
		
		ResponseLite response = (ResponseLite)result.get(0);
		Assert.assertEquals(request.getMcId(), response.getMcId());
	}
	
//	@Test
	public void testCopy() {
		List<RequestBaseMaterial> units = new LinkedList<RequestBaseMaterial>();
		// 拷贝图文创意
		RequestLite request1 = new RequestLite(50564552L, 1);
		units.add(request1);
		// 拷贝图片创意
		RequestLite request2 = new RequestLite(50564555L, 1);
		units.add(request2);
		
		List<ResponseBaseMaterial> result = ubmcService.copy(units);
		Assert.assertEquals(2, result.size());
		
		ResponseIconUnit response1 = (ResponseIconUnit)result.get(0);
		Assert.assertNotSame(request1.getMcId(), response1.getMcId());
		ResponseImageUnit response2 = (ResponseImageUnit)result.get(1);
		Assert.assertNotSame(request2.getMcId(), response2.getMcId());
	}
	
	@Test
	public void testGetPreview() {
		// 获取某个图标信息，生成预览URL
		List<RequestBaseMaterial> units = new LinkedList<RequestBaseMaterial>();
		RequestLite request = new RequestLite(498352317L, 1);
		units.add(request);
		
		List<ResponseBaseMaterial> result = ubmcService.get(units, true);
		Assert.assertEquals(1, result.size());
		
		ResponseBaseMaterial response = result.get(0);
		Assert.assertEquals(request.getMcId(), response.getMcId());
//		System.out.println(response.getFileSrc());
	}
	
	@Test
	public void testGetMediaId() {
		// 获取某个图标信息，不生成预览URL，返回fileSrc的mediaId
		List<RequestBaseMaterial> units = new LinkedList<RequestBaseMaterial>();
		RequestLite request = new RequestLite(300850597L, 1);
		units.add(request);
		
		List<ResponseBaseMaterial> result = ubmcService.get(units, false);
		Assert.assertEquals(1, result.size());
		
		ResponseImageUnit response = (ResponseImageUnit)result.get(0);
		
		UnitMaterCheckView unit = new UnitMaterCheckView();
		unit.setWuliaoType(2);
		unit.setTitle("120-600");
		unit.setShowUrl("www.meilele.com");
		unit.setTargetUrl("http://www.meilele.com/?se=bc!X18p!X10d!X!QA8AE5B1BBE7BABAE4BEA4E7B8B0E58EBFE8A!H-60!X301!QAAFE7B687E69D97E5A!Hflash");
		unit.setWirelessShowUrl(null);
		unit.setWirelessTargetUrl(null);
		unit.setHeight(600);
		unit.setWidth(120);
		System.out.println(UnitBeanUtils.compareString(unit.getTargetUrl(), response.getTargetUrl()));
		int compareCode = UnitBeanUtils.compareMaterialFromDbToUbmc(unit, result.get(0));
		
//		ResponseAdmakerMaterial response = (ResponseAdmakerMaterial)result.get(0);
//		Assert.assertEquals(request.getMcId(), response.getMcId());
//		System.out.println(response.getFileSrc());
//		
//		System.out.println(response.getDescInfo());
//		System.out.println(UnitBeanUtils.filterSpecialChar(response.getDescInfo()));
	}
	
	@Test
	public void testGetTmpUrl() {
		String result1 = ubmcService.getTmpUrl(280354060L, 1);
		System.out.println(result1);
		
//		String result2 = ubmcService.getTmpUrl(2104133L, 1);
//		System.out.println(result2);
	}
	
	@Test
	public void testGetMediaIdAndData() {
		// 获取某个图标信息，不生成预览URL，返回fileSrc的mediaId
		List<RequestBaseMaterial> units = new LinkedList<RequestBaseMaterial>();
		Long mcId = 1249395611L;
		RequestLite request = new RequestLite(mcId, 1);
		units.add(request);
		
		List<ResponseBaseMaterial> result = ubmcService.get(units, false);
		Assert.assertEquals(1, result.size());
		
		ResponseImageUnit response = (ResponseImageUnit)result.get(0);
		Assert.assertEquals(request.getMcId(), response.getMcId());
		String fileSrc = response.getFileSrc();
		System.out.println(fileSrc);
		
		Long mediaId = ubmcService.getMediaIdFromFileSrc(fileSrc);
		
		byte[] data = ubmcService.getMediaData(mediaId);

//		try {
//			String a = new String(data, "UTF-8");
//			System.out.println(a);
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		String md5= MD5.getMd5(data);
//		System.out.println(md5);
		
		if (response.getWuliaoType() == 3) {
			System.out.println(data.length);
			String xmlStr = ParseMC.extrateXml(data);
			if (StringUtils.isNotEmpty(xmlStr)) {
				System.out.println(xmlStr);
			}
			long tpId = ParseMC.getTpIdForSwf(data);
			System.out.println(tpId);
			System.out.println(ParseMC.parseDrmcFromSwf(data));
			
			FlashDecoder decoder = new FlashDecoder();
			DecodeResult decodeResult = decoder.decodeSwfDescJson(data);
			System.out.println("tpId: " + tpId);
			System.out.println("descJson: " + decodeResult.getMessage());
		}
		
		String result1 = ubmcService.getTmpUrl(mcId, 1);
		System.out.println(result1);
	}
	
	@Test
	public void testRecompileMaterial() {
		// 获取某个图标信息，不生成预览URL，返回fileSrc的mediaId
		List<RequestBaseMaterial> units = new LinkedList<RequestBaseMaterial>();
		Long mcId = 293265980L;
		RequestLite request = new RequestLite(mcId, 1);
		units.add(request);
		
		List<ResponseBaseMaterial> result = ubmcService.get(units, false);
		Assert.assertEquals(1, result.size());
		
		ResponseImageUnit response = (ResponseImageUnit)result.get(0);
		Assert.assertEquals(request.getMcId(), response.getMcId());
		String fileSrc = response.getFileSrc();
		System.out.println(fileSrc);
		
		String result1 = ubmcService.getTmpUrl(mcId, 1);
		System.out.println(result1);
		
		Long mediaId = ubmcService.getMediaIdFromFileSrc(fileSrc);
		
		byte[] data = ubmcService.getMediaData(mediaId);
		
		long tpId = ParseMC.getTpIdForSwf(data);
		
		String xmlMeta = ParseMC.extrateXml(data);
		GrantResult grantResult = recompileCreativeService.grantAuthorityForXmlMeta(xmlMeta, tpId);
		
		result1 = ubmcService.getTmpUrl(grantResult.getMcId(), 1);
		System.out.println(result1);
	}
	
	@Test
	public void testAddVersion() {
		List<RequestBaseMaterial> units = new LinkedList<RequestBaseMaterial>();
		// 拷贝图文创意，mcId不变，而是增加版本
		RequestLite request1 = new RequestLite(18884L, 1);
		units.add(request1);
		// 拷贝图片创意，mcId不变，而是增加版本
//		RequestLite request2 = new RequestLite(18884L, 1);
//		units.add(request2);
		
		List<ResponseBaseMaterial> result = ubmcService.addVersion(units);
		Assert.assertEquals(2, result.size());
		
		ResponseIconUnit response1 = (ResponseIconUnit)result.get(0);
		Assert.assertEquals(request1.getMcId(), response1.getMcId());
		Assert.assertNotSame(request1.getVersionId(), response1.getVersionId());
		ResponseImageUnit response2 = (ResponseImageUnit)result.get(1);
//		Assert.assertEquals(request2.getMcId(), response2.getMcId());
//		Assert.assertNotSame(request2.getVersionId(), response2.getVersionId());
	}
	
	@Test
	public void testGetMediaData() {
		Long mediaId = 1413971L;
		byte[] data = ubmcService.getMediaData(mediaId);
		Assert.assertNotNull(data);
		Assert.assertTrue(data.length > 0);
	}
	
	@Test
	public void testJudgeAdmakerFlash() {
		byte[] data = getImageData("error.swf");
		long tpId = ParseMC.getTpIdForSwf(data);
		System.out.println(tpId);
	}

    /**
     * Function: testOfflineMedia
     * 
     * @author genglei01 
     */
    @Test
    public void testOfflineMedia() {
        Long mediaId = 2140860524L;
        boolean flag = ubmcService.offlineMedia(mediaId);
        Assert.assertTrue(flag);
    }
    
    /**
     * Function: testGetRelatedText
     * 
     * @author genglei01 
     */
    @Test
    public void testGetRelatedText() {
        Long mediaId = 2140860491L;
        List<ResponseLite> result = ubmcService.getRelatedText(mediaId);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.size() > 0);
    }

	
	@Test
	public void testGetDescJsonAdmakerFlash() {
		byte[] data = getImageData("admaker_old_drmc.swf");
		long tpId = ParseMC.getTpIdForSwf(data);
		if (tpId > 0) {
			FlashDecoder decoder = new FlashDecoder();
			DecodeResult decodeResult = decoder.decodeSwfDescJson(data);
			System.out.println("tpId: " + tpId);
			System.out.println("descJson: " + decodeResult.getMessage());
		}
		
		FlashDecoder decoder = new FlashDecoder();
		DecodeResult decodeResult = decoder.decodeSwfDescJson(data);
		System.out.println("tpId: " + tpId);
		System.out.println("descJson: " + decodeResult.getMessage());
	}
	
	@Test
	public void testJudgeAdmakerFlashForDrmc() {
		byte[] data = getImageData("admaker_old_drmc.swf");
		System.out.println(ParseMC.parseDrmcFromSwf(data));
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
		return new File("").getAbsolutePath() + "\\" + fileName;
	}
	
}
