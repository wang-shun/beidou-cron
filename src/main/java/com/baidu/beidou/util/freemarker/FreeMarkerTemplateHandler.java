package com.baidu.beidou.util.freemarker;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.util.LogUtils;

import freemarker.template.Template;
import freemarker.template.Configuration;

public class FreeMarkerTemplateHandler implements TemplateHandler {
	
	private static final Log log = LogFactory.getLog(FreeMarkerTemplateHandler.class);

	private static Configuration cfg;
	
	private static String charset = "UTF-8";
	
	
	public FreeMarkerTemplateHandler(){
		cfg = new Configuration();
		cfg.setClassForTemplateLoading(FreeMarkerTemplateHandler.class, "/");
		cfg.setOutputEncoding(charset);
		cfg.setDefaultEncoding(charset);
		cfg.setNumberFormat("#");//指定数据格式为最简单
	}
	
	public FreeMarkerTemplateHandler(String diycharset){
		charset = diycharset;
		cfg = new Configuration();
		cfg.setClassForTemplateLoading(FreeMarkerTemplateHandler.class, "/");
		cfg.setOutputEncoding(charset);
		cfg.setDefaultEncoding(charset);
		cfg.setNumberFormat("#");
	}
	
	

	
	public String applyTemplate(String templatePath, Map args) throws Exception {

		Template template = null;
		try {
			template = cfg.getTemplate(templatePath,charset);
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
		
		StringWriter writer = new StringWriter();

		template.process(args, writer);

		return writer.toString();
	}
	
	public void writeToFile(String templatePath, Map args, String filePath) {		
		
		FileOutputStream fout = null;		
		try {
			String content = applyTemplate(templatePath, args);
			
			fout = new FileOutputStream(filePath);
			fout.write(content.getBytes(charset));			
		}
		catch(Exception e)
		{
			String errorMessage="exception happened when write to file in function VelocityTemplateHandler:writeToFile";
        	LogUtils.fatal(log, errorMessage);
			return;
		}
		finally
		{
			try {
				if(fout != null){
					fout.close();
				}								
			} catch (IOException e) {						
				e.printStackTrace();
			}
		}
	}
}
