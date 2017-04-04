package hadoop.qtkr.com.baidu.beidou.hadoop.qtkr.test;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.util.ToolRunner;

import hadoop.qtkr.com.baidu.beidou.hadoop.qtkr.WordKey;
import hadoop.qtkr.com.baidu.beidou.hadoop.util.AbstractJob;

public class QtkrTestJob extends AbstractJob{

	public final static String STAT_TYPE = "krstat.stat.type";
	public final static int STAT_TYPE_ALL_REGION = 0;
	public final static int STAT_TYPE_FIRST_REGION = 1;
	public final static int STAT_TYPE_SECOND_REGION = 2;
	
	public int run(String[] arg) throws Exception {
	    if (arg.length < 2) {
	        System.out.println("qtkr-test <inputDir> <outputDir>");
	        ToolRunner.printGenericCommandUsage(System.out);
	        return -1;
	    }
	    
	    Path inputPath = new Path(arg[0]);
		Path outputPath = new Path(arg[1]);
	
		JobConf confUfs = prepareJob(
				inputPath, outputPath, TextInputFormat.class,
				WordStatMapper.class, WordKey.class, LongWritable.class,
				WordStatReducer.class, WordKey.class, LongWritable.class,
				TextOutputFormat.class);
		JobClient.runJob(confUfs);
		
		return 0;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new QtkrTestJob(), args);
		System.exit(res);
	}
}
