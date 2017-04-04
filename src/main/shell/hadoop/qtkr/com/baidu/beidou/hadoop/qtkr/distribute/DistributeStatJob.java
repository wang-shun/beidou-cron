package hadoop.qtkr.com.baidu.beidou.hadoop.qtkr.distribute;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapred.lib.IdentityReducer;
import org.apache.hadoop.mapred.lib.LongSumReducer;
import org.apache.hadoop.util.ToolRunner;

import hadoop.qtkr.com.baidu.beidou.hadoop.util.AbstractJob;

public class DistributeStatJob extends AbstractJob{
	public final static String STAT_TYPE = "krstat.stat.type";
	public final static int STAT_TYPE_MAXPV = 0;
	public final static int STAT_TYPE_COOKIES = 1;
	public final static int STAT_TYPE_ADVIEW = 2;
	
	public int run(String[] arg) throws Exception {
		if (arg.length < 2) {
	        System.out.println("qtkr-distribute <inputDir> <outputDir>");
	        ToolRunner.printGenericCommandUsage(System.out);
	        return -1;
	    }
	    
		Path inputPath = new Path(arg[0]);
		Path outputDir = new Path(arg[1]);
		Path tempPath = new Path(outputDir,"temp");
		Path filterOutputPath = new Path(tempPath,"filter");
		Path adviewOutputPath = new Path(outputDir,"adview");
		Path cookieOutputPath = new Path(outputDir,"cookie");
		Path maxpvOutputPath = new Path(outputDir,"maxpv");
	
		JobConf confFilter = prepareJob(
				inputPath, filterOutputPath, TextInputFormat.class,
				WordMapper.class, LongWritable.class, WordInfoWritable.class,
				IdentityReducer.class, LongWritable.class, WordInfoWritable.class,
				SequenceFileOutputFormat.class);
		JobClient.runJob(confFilter);
		
		JobConf confAdview = prepareJob(
				filterOutputPath, adviewOutputPath, SequenceFileInputFormat.class,
				DistributeStatMapper.class, LongWritable.class, LongWritable.class,
				LongSumReducer.class, LongWritable.class, LongWritable.class,
				TextOutputFormat.class);
		confAdview.setInt(DistributeStatJob.STAT_TYPE, DistributeStatJob.STAT_TYPE_ADVIEW);
		confAdview.setCombinerClass(LongSumReducer.class);
		confAdview.setJarByClass(DistributeStatJob.class);
		JobClient.runJob(confAdview);
		
		JobConf confCookie = prepareJob(
				filterOutputPath, cookieOutputPath, SequenceFileInputFormat.class,
				DistributeStatMapper.class, LongWritable.class, LongWritable.class,
				LongSumReducer.class, LongWritable.class, LongWritable.class,
				TextOutputFormat.class);
		confCookie.setInt(DistributeStatJob.STAT_TYPE, DistributeStatJob.STAT_TYPE_COOKIES);
		confCookie.setCombinerClass(LongSumReducer.class);
		confCookie.setJarByClass(DistributeStatJob.class);
		JobClient.runJob(confCookie);
		
		JobConf confMaxpv = prepareJob(
				filterOutputPath, maxpvOutputPath, SequenceFileInputFormat.class,
				DistributeStatMapper.class, LongWritable.class, LongWritable.class,
				LongSumReducer.class, LongWritable.class, LongWritable.class,
				TextOutputFormat.class);
		confMaxpv.setInt(DistributeStatJob.STAT_TYPE, DistributeStatJob.STAT_TYPE_MAXPV);
		confMaxpv.setCombinerClass(LongSumReducer.class);
		confMaxpv.setJarByClass(DistributeStatJob.class);
		JobClient.runJob(confMaxpv);
		
		return 0;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new DistributeStatJob(), args);
		System.exit(res);
	}
}
