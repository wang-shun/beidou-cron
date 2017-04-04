package hadoop.qtkr.com.baidu.beidou.hadoop.qtkr.test;

import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

import hadoop.qtkr.com.baidu.beidou.hadoop.qtkr.WordKey;


public class WordStatMapper extends MapReduceBase
	implements Mapper<LongWritable,Text, WordKey, LongWritable> {

	private final static String COUNTER_GROUP_MAP = "my_map_record_count";
	private final static LongWritable one = new LongWritable(1);

	public void map(LongWritable key,
	                     Text value,
	                     OutputCollector<WordKey, LongWritable> output, Reporter reporter) throws IOException {
		
		reporter.getCounter(COUNTER_GROUP_MAP,"total").increment(1);
		
		Pattern ptn = Pattern.compile("\t");
		String[] tokens = ptn.split(value.toString());
		if(tokens.length==11){
			reporter.getCounter(COUNTER_GROUP_MAP,"valid_all").increment(1);

			long wordid = Long.parseLong(tokens[0]);			
			int secondRegion = Integer.parseInt(tokens[1]);	
			
	  		if(secondRegion!=0){
	  			WordKey secondRegionKey = new WordKey(wordid,secondRegion);
		  		output.collect(secondRegionKey, one);
	  		}else{
	  			reporter.getCounter(COUNTER_GROUP_MAP,"invalid_second").increment(1);
	  		}
		}else{
			reporter.getCounter(COUNTER_GROUP_MAP,"invalid_all").increment(1);
		}
	}
}
