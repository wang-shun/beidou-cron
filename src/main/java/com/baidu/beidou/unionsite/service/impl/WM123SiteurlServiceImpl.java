package com.baidu.beidou.unionsite.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.unionsite.bo.WM123Siteurl;
import com.baidu.beidou.unionsite.constant.SiteConstant;
import com.baidu.beidou.unionsite.service.WM123SiteurlService;
import com.baidu.beidou.util.UrlParser;

/**
 * InterfaceName: WM123SiteurlService <br>
 * Function: 根据siteurl list文件，生成一级域名包含www.开头的另一份文件，供司南系统使用生成访客特征数据
 *
 * @author   <a href="mailto:zhangxu04@baidu.com">张旭</a>
 */
public class WM123SiteurlServiceImpl implements WM123SiteurlService{
	
	private static final Log LOG = LogFactory.getLog(WM123SiteurlServiceImpl.class);
    
    /** 两个Index数据文件所用的索引 */
    private String fileEncoding = "GBK";

	/**
	 * 根据siteurl list文件，生成一级域名包含www.开头的另一份文件，供司南系统使用生成访客特征数据 <br>
	 * 
	 * input的siteurl文件格式如下：<br>
	 * ifeng.com <br>
	 * youku.com <br>
	 * sina.com.cn <br>
	 * 
	 * output的siteurl文件格式如下：<br>
	 * www.ifeng.com <br>
	 * www.youku.com <br>
	 * www.sina.com.cn <br>
	 * 
	 * @param file
	 * @return 
	 */
	public void getSiteurl4SN(String src_file, String dest_file) throws IOException{
		if (src_file == null || src_file.equals("")) {
            LOG.error("请输入要格式化的siteurl文件名");
            throw new IOException("请输入要格式化的siteurl文件名");
        }
		if (dest_file == null || dest_file.equals("")) {
            LOG.error("请输入格式化后保存文件的文件名");
            throw new IOException("请输入格式化后保存文件的文件名");
        }
        
        File srcFile = new File(src_file);
        File destFile = new File(dest_file);
        
        if (!srcFile.exists()) {

            LOG.error("找不到要格式化的siteurl文件：" + src_file);
            throw new FileNotFoundException("找不到要格式化的siteurl文件：" + src_file);
        }

        
        BufferedReader br = new BufferedReader(new InputStreamReader( new FileInputStream(src_file), fileEncoding));
        List<WM123Siteurl> wm123siteurlList = genSiteurlList(br);
        br.close();
        
        BufferedWriter bw = new BufferedWriter(new FileWriter(dest_file));
        outputDestFile(wm123siteurlList,bw);
        bw.flush();
        bw.close();
       
	}
    
	
	/**
     * genSiteurlList: 根据input的文件生成WM123Siteurl的list对象
     *
     * @param br 输入流
     * @return  WM123Siteurl的List
     * @throws IOException 
    */
    private List<WM123Siteurl> genSiteurlList(BufferedReader br) throws IOException {
        List<WM123Siteurl> list = new ArrayList<WM123Siteurl>(10000);
        
        String line;
        WM123Siteurl vo;
        
        int ignoreCount = 0;//略过个数
        int validCount = 0;//有效个数
        int failedConvertMainDomainCount = 0;//siteurl获取maindomain失败

        while ( (line = br.readLine()) != null ) {
            if (org.apache.commons.lang.StringUtils.isEmpty(line)) {
                continue;
            }
            String siteurl = line.toLowerCase().trim();
            String temp = null;
            try {
            	if(org.apache.commons.lang.StringUtils.isEmpty(siteurl)){
                	LOG.info("[" + line + "] siteurl is null");
                    ignoreCount ++;
                    continue;
            	}
                temp = UrlParser.fetchMainDomain(siteurl);
            	if(org.apache.commons.lang.StringUtils.isEmpty(temp)){
                	LOG.info("[" + line + "] fetch main domain failed");
                	failedConvertMainDomainCount ++;
                    continue;
            	}
                vo = new WM123Siteurl();
                vo.setOri_url(siteurl);
                if(temp.equalsIgnoreCase(siteurl)){
                	vo.setWww_url(SiteConstant.WWW_PREFIX + temp);
                }
                else
                {
                	vo.setWww_url(siteurl);
                }
            } catch (Exception e) {
                LOG.info("[" + line + "] is ignored due to following error: " + e.getMessage());
                ignoreCount ++;
                continue;
            }
            validCount++;
            list.add(vo);
        }
        LOG.info("*************wm123 siteurl file dealing result:ignore=" + ignoreCount + ",valid=" + validCount + ", failedConvertMainDomainCount=" + failedConvertMainDomainCount);
        return list;
    }
    
    /**
     * outputDestFile: 写入最终的文件
     *
     * @param wm123siteurlList
     * @param bw
     * @return 
     * @throws IOException 
    */
    private void outputDestFile(List<WM123Siteurl> wm123siteurlList, BufferedWriter bw) throws IOException {
    	for(WM123Siteurl wM123Siteurl : wm123siteurlList){
    		bw.write(wM123Siteurl.getWww_url());
    		bw.newLine();
    	}
    }
    
}
