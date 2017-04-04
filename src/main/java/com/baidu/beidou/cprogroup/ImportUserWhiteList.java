package com.baidu.beidou.cprogroup;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.cprogroup.facade.CproGroupFacade;

/**
 * @author zhuqian
 * 
 */
public class ImportUserWhiteList {

	private static final Log log = LogFactory.getLog(ImportUserWhiteList.class);

	public static void main(String[] args) throws Exception {

		if (args.length < 1) {
			log.error("Usage: ReloadUserWhiteList [user_white_list_file]");
			throw new Exception("Usage: ReloadUserWhiteList [user_white_list_file]");
		}

		String filepath = args[0];
		String[] paths = new String[] { "applicationContext.xml", "classpath:/com/baidu/beidou/user/applicationContext.xml" };

		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(paths);
		CproGroupFacade cproGroupFacade = (CproGroupFacade) ctx.getBean("cproGroupFacade");

		cproGroupFacade.updateUserWhiteList(getUserListFromFile(filepath));
	}

	public static List<Integer> getUserListFromFile(String filepath) throws Exception {
		List<Integer> result = new ArrayList<Integer>();
		try {

			DataInputStream in = new DataInputStream(new FileInputStream(filepath));
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {

				if (StringUtils.isEmpty(strLine)) {
					continue;
				}

				try {
					Integer userid = Integer.parseInt(strLine.trim());
					result.add(userid);
				} catch (NumberFormatException e) {
					log.error("wrong input line='" + strLine + "'");
					continue;
				}
			}
			// Close the input stream
			in.close();

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			throw e;
		}
		return result;
	}
}
