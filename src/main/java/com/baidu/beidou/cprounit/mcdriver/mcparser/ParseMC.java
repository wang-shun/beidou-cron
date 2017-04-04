package com.baidu.beidou.cprounit.mcdriver.mcparser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * #func 从给定url中解析物料（swf, jpg格式）在admaker中的创意id。
 * 
 * @author huangxiaowei
 * 
 */
public class ParseMC {
	
	private static final Log logger = LogFactory.getLog(ParseMC.class);

	public static int getTpIdForSwf(byte[] data) {
		if (data.length == 0) {
			logger.info("This file is an empty file.");
			return 0;
		}
		
		return getTemplateIdFromSwf(data);
	}
	
	/**
	 * #func 获取创意的id。
	 */
	public static int getTemplateId(String url) {
		int templateId = 0;

		// 获取文件类型。
		String type = getType(url);

		// 检查物料的类型，只处理swf和jpg两种物料
		if ((!type.equals("swf")) && (!type.equals("jpg"))) {
			logger.info("The type of the target file is not swf or jpg.");
			return 0;
		}

		// 从给定url中获取文件二进制数据流。
		byte[] content = getBytes(url);
		if (content.length == 0) {
			logger.info("This file is an empty file.");
			return 0;
		}

		if (type.equals("swf")) {
			templateId = getTemplateIdFromSwf(content);
		} else if (type.equals("jpg")) {
			templateId = getTemplateIdFromJpg(content);
		}

		return templateId;
	}

	/**
	 * #func 读取文件的类型。 
	 */
	private static String getType(String url) {
		String arrTmp = url.substring(url.lastIndexOf(".") + 1);
		if (StringUtils.isEmpty(url) || StringUtils.isEmpty(arrTmp)) {
			return null;
		}
		return arrTmp.toLowerCase();
	}

	/**
	 * #func 读取文件的二进制流。 #desc
	 */
	private static byte[] getBytes(String url) {
		URL file = null;
		URLConnection ucConnection = null;
		InputStream in = null;
		InputStream raw = null;
		int contentLength = 0;
		byte[] creativeBytes = null;

		try {
			file = new URL(url);
			ucConnection = file.openConnection();
			contentLength = ucConnection.getContentLength();
			raw = ucConnection.getInputStream();
			in = new BufferedInputStream(raw);
			creativeBytes = new byte[contentLength];
			int bytesRead = 0;
			int offset = 0;
			while (offset < contentLength) {
				bytesRead = in.read(creativeBytes, offset, creativeBytes.length
						- offset);
				if (bytesRead == -1)
					break;
				offset += bytesRead;
				}
			in.close();
			in = null;

			if (offset != contentLength) {
				throw new IOException("Only read " + offset
						+ " bytes; Expected " + contentLength + " bytes");
				}
			} catch (MalformedURLException e) {
			    logger.error(e.getMessage(), e);
			} catch (IOException e) {
			    logger.error(e.getMessage(), e);
			} finally {
				try {
					if (in != null) {
					in.close();
					in = null;
					    }
					if (raw != null) {
					raw.close();
					raw = null;
				        }
					} catch (IOException e) {
				    e.printStackTrace();
				    }
			    }
		return creativeBytes;
	}

	/**
	 * #func 从SWF文件中读取模板的id。 #desc
	 */
	private static int getTemplateIdFromSwf(byte[] swfContent) {
		if (swfContent == null || swfContent.length == 0) {
			System.out.println("The swf content is empty!");
			return 0;
		}
		try {
			swfContent = SWFUtil.decompress(swfContent);
		} catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		try {
			Pattern p = Pattern.compile("Admaker@baidu.com_(\\d+)_(\\d+)");
			String swfContentStr = new String(swfContent, "UTF-8");
			Matcher m = p.matcher(swfContentStr);
			if (m.find()) {
				return Integer.parseInt(m.group(1));
			}
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}
		
		return 0;
	}

	/**
	 * #func 从Jpg文件中读取模板的id。 #desc
	 */
	private static int getTemplateIdFromJpg(byte[] content) {
		try {
			String fingerPrint = new JFIFFile(content).getApp1();
			if (fingerPrint != null && fingerPrint.length() != 0) {
				Pattern p = Pattern.compile("Admaker@baidu.com_(\\d+)_(\\d+)");
				Matcher m = p.matcher(fingerPrint);
				if (m.find()) {
					int templateId = Integer.parseInt(m.group(1));
					return templateId;
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return 0;
	}
	
	/**
	 * 是1.0版本，返回解析后的XML；非1.0版本、解析失败，返回null
	 * @param bytes
	 * @return
	 */
	public static String extrateXml(byte[] bytes) {
		Pattern p = Pattern.compile("<root[^>]*?>.*?<\\/root>");

		try {
			bytes = SWFUtil.decompress(bytes);
		} catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		try {
			String swfContentStr = new String(bytes, "UTF-8");
			Matcher m = p.matcher(swfContentStr);
			if (m.find()) {
				return m.group(0);
			}
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}
		
		return null;
	}

	public static String parseDrmcFromSwf(byte[] swfContent) {
		if (swfContent == null || swfContent.length == 0) {
			System.out.println("The swf content is empty!");
			return null;
		}
		try {
			swfContent = SWFUtil.decompress(swfContent);
		} catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		try {
			Pattern p = Pattern.compile("drmcmm.baidu.com");
			String swfContentStr = new String(swfContent, "UTF-8");
			Matcher m = p.matcher(swfContentStr);
			if (m.find()) {
				return m.group(0);
			}
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}
		
		return null;
	}
}
