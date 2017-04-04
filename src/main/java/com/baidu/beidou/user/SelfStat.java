package com.baidu.beidou.user;

import java.io.BufferedWriter;
import java.io.FileWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.user.service.UserInfoMgr;

public class SelfStat {

	private static final Log log = LogFactory.getLog(SelfStat.class);

	/**
	 * @param args
	 * 下午1:33:46 created by qianlei
	 * 自由流量数据提供：
	 * 导出vip&heavy userid
	 * 并单独导出heavy userid
	 * 共两份文件
	 * args[0]：vip&heavy userid导出的文件路径
	 * args[1]：heavy userid导出的文件路径
	 */
	public static void main(String[] args) {

		// 进行输入参数的检验
		if (args == null || args.length < 2 || args[0] == null || args[1] == null) {
			log.error("the param is error. vip and heavy userid outputFile is  need.");
			System.exit(1);
		}

		//vip&heavy userid导出的文件路径
		String vipAndHeavyUidsFile = args[0];
		
		//heavy userid导出的文件路径
		String heavyUidsFile = args[1];

		String[] paths = new String[] { "applicationContext.xml", "classpath:/com/baidu/beidou/user/applicationContext.xml" };

		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(paths);

		UserInfoMgr userInfoMgr = (UserInfoMgr) ctx.getBean("userInfoMgr");

		//获取vip&heavy userid
		int[] vipAndHeavyUids = userInfoMgr.getAllHeavyCustomers();
		if (vipAndHeavyUids == null || vipAndHeavyUids.length == 0) {
			log.error("vip and heavy userid empty.");
			System.exit(1);
		}

		//获取heavy userid
		int[] heavyUids = userInfoMgr.getAllClientCustomersFromUc();

		if (heavyUids == null || heavyUids.length == 0) {
			log.error("heavy userid empty.");
			System.exit(1);
		}

		//将数据写入文件
		BufferedWriter bwVipAndHeavy = null;
		BufferedWriter bwHeavy = null;
		try {
			//写vip&heavy userid到文件
			bwVipAndHeavy = new BufferedWriter(new FileWriter(vipAndHeavyUidsFile));
			for (int i = 0; i < vipAndHeavyUids.length - 1; i++) {
				bwVipAndHeavy.write("" + vipAndHeavyUids[i]);
				bwVipAndHeavy.newLine();
			}
			bwVipAndHeavy.write("" + vipAndHeavyUids[vipAndHeavyUids.length - 1]);

			//写heavy userid到文件
			bwHeavy = new BufferedWriter(new FileWriter(heavyUidsFile));
			for (int i = 0; i < heavyUids.length - 1; i++) {
				bwHeavy.write("" + heavyUids[i]);
				bwHeavy.newLine();
			}
			bwHeavy.write("" + heavyUids[heavyUids.length - 1]);

		} catch (Exception e) {
			log.error(e);
		} finally {
			try {
				if (bwVipAndHeavy != null) {
					bwVipAndHeavy.close();
				}
				if (bwHeavy != null) {
					bwHeavy.close();
				}
			} catch (Exception e) {
				log.error(e);
			}
		}
	}
}
