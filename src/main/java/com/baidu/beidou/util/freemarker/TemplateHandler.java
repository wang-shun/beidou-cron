package com.baidu.beidou.util.freemarker;

/*
 * Created on 2007-10-16
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
import java.util.Map;

/**
 * @author wuhao
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface TemplateHandler {
	
	public String applyTemplate(String templatePath, Map args) throws Exception;
	
	public void writeToFile(String templatePath, Map args, String filePath);
}
