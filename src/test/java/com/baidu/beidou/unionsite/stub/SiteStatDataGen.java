/**
 * 2009-4-24 上午10:50:20
 */
package com.baidu.beidou.unionsite.stub;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class SiteStatDataGen {

	/**
	 * @author zengyunfeng
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String fileName = "data/beidousitestat.20090423";
		String fileSuffix = "-baidu.com";
		int count = 40000;
		int subCount = 3;
		String cntnSuffix = "-baidu";
		Random random = new Random(System.currentTimeMillis());
		FileWriter writer = new FileWriter(fileName);
		for(int i=0; i<count; i++){
			int key = random.nextInt();
			String domain = key+fileSuffix;
			
			int subDomain = random.nextInt(subCount);
			String cntn = key+cntnSuffix;
			writer.append(cntn).append('\t');	//计费名
			writer.append(domain).append('\t');	//域名
			writer.append(String.valueOf(10000)).append('\t');	//检索量
			writer.append(String.valueOf(100000)).append('\t');	//日展现量
			writer.append(String.valueOf(2)).append('\t');	//文字/图片标识位
			writer.append(String.valueOf(1000)).append('\t'); //独立ip数
			writer.append(String.valueOf(2000)).append('\t'); //独立cookie数
			writer.append(String.valueOf(1500)).append('\t'); //日点击数
			writer.append(String.valueOf(1600)).append('\t'); //日点击消费
			writer.append("728*90|760*60|360*300").append('\t');	//尺寸
			writer.append("720|760|300").append('\n');	//尺寸流量
			for(int j=0; j<subDomain; j++){
				String subDomainStr = subDomain+"."+domain; 
				writer.append(cntn).append('\t');	//计费名
				writer.append(subDomainStr).append('\t');	//域名
				writer.append(String.valueOf(10000)).append('\t');	//检索量
				writer.append(String.valueOf(100000)).append('\t');	//日展现量
				writer.append(String.valueOf(2)).append('\t');	//文字/图片标识位
				writer.append(String.valueOf(1000)).append('\t'); //独立ip数
				writer.append(String.valueOf(2000)).append('\t'); //独立cookie数
				writer.append(String.valueOf(1500)).append('\t'); //日点击数
				writer.append(String.valueOf(1600)).append('\t'); //日点击消费
				writer.append("728*90|760*60|360*300").append('\t');	//尺寸
				writer.append("720|760|300").append('\n');	//尺寸流量
			}
			
		}
		writer.close();
	}

}
