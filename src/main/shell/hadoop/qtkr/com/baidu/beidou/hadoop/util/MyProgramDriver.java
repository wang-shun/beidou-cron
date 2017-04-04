package hadoop.qtkr.com.baidu.beidou.hadoop.util;

import org.apache.hadoop.util.ProgramDriver;

import hadoop.qtkr.com.baidu.beidou.hadoop.qtkr.QtkrJob;
import hadoop.qtkr.com.baidu.beidou.hadoop.qtkr.distribute.DistributeStatJob;
import hadoop.qtkr.com.baidu.beidou.hadoop.qtkr.test.QtkrTestJob;


public class MyProgramDriver {
	public static void main(String[] args) {
	    int exitCode = -1;
	    ProgramDriver pgd = new ProgramDriver();
	    try {
	      pgd.addClass("test", TestJob.class, "A Job for test Tool arguments");
	      pgd.addClass("qtkr", QtkrJob.class, "A Job stats the word pv info for QT-KR");
	      pgd.addClass("qtkr-distribute", DistributeStatJob.class, "stats the data Distribute info for QT-KR");
	      pgd.addClass("qtkr-test", QtkrTestJob.class, "A Job validate the input data format for QT-KR");
	      
	      pgd.driver(args);
	      // Success
	      exitCode = 0;
	    }catch(Throwable e){
	      e.printStackTrace();
	    }
	    
	    System.exit(exitCode);
	}
}
