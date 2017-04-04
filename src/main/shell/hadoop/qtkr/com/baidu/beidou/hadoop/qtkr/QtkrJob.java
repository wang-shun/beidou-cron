package hadoop.qtkr.com.baidu.beidou.hadoop.qtkr;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapred.lib.IdentityMapper;
import org.apache.hadoop.util.ToolRunner;

import hadoop.qtkr.com.baidu.beidou.hadoop.util.AbstractJob;

public class QtkrJob extends AbstractJob{

	public final static String STAT_TYPE = "krstat.stat.type";
	public final static int STAT_TYPE_ALL_REGION = 0;
	public final static int STAT_TYPE_FIRST_REGION = 1;
	public final static int STAT_TYPE_SECOND_REGION = 2;
	
	public int run(String[] arg) throws Exception {
	    if (arg.length < 3) {
	        System.out.println("qtkr <ufsInputDir> <adviewInputDir> <outputDir>");
	        ToolRunner.printGenericCommandUsage(System.out);
	        return -1;
	    }
	    
	    String strUfsPath = arg[0];
	    String strAdviewPath = arg[1];
		Path ufsInputPath = null;
		Path adviewInputPath = null;
		Path outputDir = new Path(arg[2]);
		Path tempPath = new Path(outputDir,"temp");
		Path ufsOutputPath = new Path(tempPath,"ufs");
		Path adviewOutputPath = new Path(tempPath,"adview");
		Path joinOutputPath = new Path(tempPath,"join");
		Path outputPath = new Path(outputDir,"output");
	
		JobConf confUfs = prepareJob(
				ufsInputPath, ufsOutputPath, TextInputFormat.class,
				WordStatMapper.class, WordKey.class, WordStatOrAdviewWritable.class,
				WordStatReducer.class, WordKey.class, WordStatOrAdviewWritable.class,
				SequenceFileOutputFormat.class);
		FileSystem ufsFs = FileSystem.get(tempPath.toUri(), confUfs);
		String[] arrUfsPath = strUfsPath.split(",");
		for(String strPath: arrUfsPath){
			Path p = new Path(strPath);
			p = p.makeQualified(ufsFs);
			FileInputFormat.addInputPath(confUfs, p);
		}
		JobClient.runJob(confUfs);
		
		JobConf confAdview = prepareJob(
				adviewInputPath, adviewOutputPath, TextInputFormat.class,
				WordAdviewMapper.class, WordKey.class, WordStatOrAdviewWritable.class,
				WordAdviewReducer.class, WordKey.class, WordStatOrAdviewWritable.class,
				SequenceFileOutputFormat.class);
		FileSystem adviewFs = FileSystem.get(tempPath.toUri(), confAdview);
		String[] arrAdviewPath = strAdviewPath.split(",");
		for(String strPath: arrAdviewPath){
			Path p = new Path(strPath);
			p = p.makeQualified(adviewFs);
			FileInputFormat.addInputPath(confAdview, p);
		}
		JobClient.runJob(confAdview);
		
		JobConf confJoin = prepareJob(
				new Path(ufsOutputPath+","+adviewOutputPath), joinOutputPath, SequenceFileInputFormat.class,
				IdentityMapper.class, WordKey.class, WordStatOrAdviewWritable.class,
				WordStatJoinAdviewReducer.class, WordKey.class, WordStatAndAdviewWritable.class,
				SequenceFileOutputFormat.class);		
		FileSystem joinFs = FileSystem.get(tempPath.toUri(), confJoin);
    	ufsOutputPath = ufsOutputPath.makeQualified(joinFs);
    	adviewOutputPath = adviewOutputPath.makeQualified(joinFs);  	
    	FileInputFormat.setInputPaths(confJoin, ufsOutputPath, adviewOutputPath);    	
		JobClient.runJob(confJoin);
		
		JobConf confFilter = prepareJob(
				joinOutputPath, outputPath, SequenceFileInputFormat.class,
				IdentityMapper.class, WordKey.class, WordStatAndAdviewWritable.class,
				FilterJoinReducer.class, LongWritable.class, WordStatAndAdviewWritable.class,
				TextOutputFormat.class);
    	confFilter.setPartitionerClass(WordKey.WordKeyPartitioner.class);
        confFilter.setOutputValueGroupingComparator(WordKey.WordKeyGroupComparator.class);
		JobClient.runJob(confFilter);
		
		return 0;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new QtkrJob(), args);
		System.exit(res);
	}
}
