package com.baidu.beidou.bes.user.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.bes.user.po.QualificationInfo;
import com.baidu.beidou.bes.user.request.TencentRequestType;
import com.baidu.beidou.util.JsonUtils;
import com.baidu.beidou.util.string.StringUtil;
/**
 * http协议 post方法 json格式请求工具，spring配置线程池，可并发注入使用
 * 
 * @author caichao
 */
public class HttpClientUtil {

	private Log logger = LogFactory.getLog(this.getClass());
	private HttpClient httpClient;
	
	private Integer connectTimeOut = 10000;
	private Integer readTimeOut = 10000;

	
	public HttpClientUtil() {
	}

	/**
	 * 调用 API
	 * @param param
	 * @return
	 * @throws IOException 
	 */
	public String post(Map<String,String> params,String url) throws IOException {
		String result = "";
		if (params == null || url == null) {
			logger.error("request param is incorrect");
			return result;
		}
		PostMethod method = new PostMethod(url);
		BufferedReader bufferReader = null;
		InputStream input = null;
		try {
			//建立一个NameValuePair数组，用于存储欲传送的参数  
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			for (Map.Entry<String, String> param : params.entrySet()) {
				logger.info(param.getKey() + " : " + param.getValue());
				NameValuePair pair = new NameValuePair(param.getKey(),param.getValue());
				pairs.add(pair);
			}
			
			//添加参数  
			method.addParameters(pairs.toArray(new NameValuePair[]{}));
			method.getParams().setContentCharset("utf-8");
			Long startTime = System.currentTimeMillis();
			

			//执行post请求
			int response = httpClient.executeMethod(method);
			
			Long endTime = System.currentTimeMillis();
			
			if (response != HttpStatus.SC_OK) {
				logger.error("method failed: status code is " + response);
				return result;
			}

			input = method.getResponseBodyAsStream();
			
			bufferReader = new BufferedReader(new InputStreamReader(input));
			
			String tmp = null;
			StringBuffer inputString = new StringBuffer();
			while((tmp=bufferReader.readLine())!=null){
				inputString.append(tmp);
			}
			 
			result = StringUtil.unicodeToString(inputString.toString());
			
			logger.info("api result : " + result);
			logger.info("api cost time " + (endTime - startTime) + "ms");
		}catch (IOException e) {
			logger.error("ioexception occurred ",e);
		}catch(Exception e){
			logger.info("exception occurred", e);
		} finally {
			method.releaseConnection();
			if (bufferReader != null) {
				bufferReader.close();
			}
			if (input != null){
				input.close();
			}
		}
	return result;
}

	 
	
	public static void main(String[] args){
		String[] paths = new String[] { "applicationContext.xml", "classpath:/com/baidu/beidou/bes/user/applicationContext.xml", "classpath:/com/baidu/beidou/cprounit/applicationContext.xml" };
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(paths);
		
    	HttpClientUtil util = (HttpClientUtil)context.getBean("httpClientUtil");
		//System.out.println(util);
		//System.out.println(util.httpClient);
		List<QualificationInfo> list = new ArrayList<QualificationInfo>();
		QualificationInfo info = new QualificationInfo();
		info.setFile_name("aaa");
		info.setFile_url("http://www.baidu.com");
		list.add(info);
		
		List<TencentRequestType> test = new ArrayList<TencentRequestType>();
		for (int i=0;i<50 ;i++) {
		TencentRequestType request = new TencentRequestType();
		request.setName(String.valueOf(i));
		request.setUrl("http://www.baidu.com");
//		request.setOverwrite_qualification(true);
//		request.setQualification_files(list);
		request.setMemo("空");
		
		
		test.add(request);
		}
		
		List<String> test1 = new  ArrayList<String>();
		test1.add("彩超");
		test1.add("百度");
		test1.add("中文569948");
		test1.add("中文569978");
		test1.add("中文145990");
		
		//List<Map<String,TencentRequestType>> test1 = new ArrayList<Map<String,TencentRequestType>>();
		Map<String,String> testMap = new HashMap<String,String>();
		testMap.put("dsp_id", "162");
		testMap.put("token", "fb6c8886122001803fe3d2aece6f2346");
		testMap.put("names",JsonUtils.toJson(test1));
	
		
		try {
		String body = util.post(testMap,"http://opentest.adx.qq.com/client/info");
		System.out.println(body);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	public HttpClient getHttpClient() {
		return httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public Integer getConnectTimeOut() {
		return connectTimeOut;
	}

	public void setConnectTimeOut(Integer connectTimeOut) {
		this.connectTimeOut = connectTimeOut;
	}

	public Integer getReadTimeOut() {
		return readTimeOut;
	}

	public void setReadTimeOut(Integer readTimeOut) {
		this.readTimeOut = readTimeOut;
	}

	
}
