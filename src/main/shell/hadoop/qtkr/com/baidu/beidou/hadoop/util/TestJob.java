package hadoop.qtkr.com.baidu.beidou.hadoop.util;

import java.util.Iterator;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.lib.IdentityMapper;
import org.apache.hadoop.mapred.lib.LongSumReducer;
import org.apache.hadoop.util.ToolRunner;

public class TestJob extends AbstractJob{

	public int run(String[] arg) throws Exception {
		JobConf jobConf = new JobConf(getConf());
		System.out.println("jar="+jobConf.getJar());
		System.out.println("job name="+jobConf.getJobName());
		System.out.println("map="+jobConf.getNumMapTasks());
		System.out.println("reduce="+jobConf.getNumReduceTasks());
		for(String str:arg){
			System.out.println(str);
		}
		if (arg.length < 2) {
	        System.out.println("test <inputDir> <outputDir>");
	        ToolRunner.printGenericCommandUsage(System.out);
	        return -1;
	    }
		JobConf confFilter = prepareJob(
				new Path(arg[0]), new Path(arg[1]), TextInputFormat.class,
				IdentityMapper.class, LongWritable.class, Text.class,
				LongSumReducer.class, LongWritable.class, Text.class,
				SequenceFileOutputFormat.class);
		System.out.println("jar="+confFilter.getJar());
		System.out.println("job name="+confFilter.getJobName());
		System.out.println("map="+confFilter.getNumMapTasks());
		System.out.println("reduce="+confFilter.getNumReduceTasks());		
		
		Iterator<Map.Entry<String, String>> iter= jobConf.iterator();
		while(iter.hasNext()){
			Map.Entry<String, String> entry = iter.next();
			System.out.println(entry.getKey()+"="+entry.getValue());
		}
		
		return 0;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new TestJob(), args);
		System.exit(res);
	}
}
