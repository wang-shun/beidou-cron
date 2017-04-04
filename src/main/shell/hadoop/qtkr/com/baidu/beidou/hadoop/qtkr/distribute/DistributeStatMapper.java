package hadoop.qtkr.com.baidu.beidou.hadoop.qtkr.distribute;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

public class DistributeStatMapper extends MapReduceBase
	implements Mapper<LongWritable, WordInfoWritable, LongWritable, LongWritable> {
	
	private final static LongWritable one = new LongWritable(1);	
	private int stattype = 0;
	@Override
	public void configure(JobConf jobConf) {		
		stattype = jobConf.getInt(DistributeStatJob.STAT_TYPE, DistributeStatJob.STAT_TYPE_ADVIEW);
	}
	
	public void map(LongWritable key, WordInfoWritable value,
			OutputCollector<LongWritable, LongWritable> output, Reporter arg3)
			throws IOException {
		if(stattype==DistributeStatJob.STAT_TYPE_ADVIEW){
			output.collect(new LongWritable(value.getAdview()), one);
		}else if(stattype==DistributeStatJob.STAT_TYPE_COOKIES){
			output.collect(new LongWritable(value.getCookies_30days()), one);
		}else if(stattype==DistributeStatJob.STAT_TYPE_MAXPV){
			output.collect(new LongWritable(value.getMaxpv_30days()), one);
		}
	}

}
