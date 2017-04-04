package hadoop.qtkr.com.baidu.beidou.hadoop.qtkr;

import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;


public class WordStatMapper extends MapReduceBase
	implements Mapper<LongWritable,Text, WordKey, WordStatOrAdviewWritable> {

	private final static String COUNTER_GROUP_MAP = "my_map_record_count";

	public void map(LongWritable key,
	                     Text value,
	                     OutputCollector<WordKey, WordStatOrAdviewWritable> output, Reporter reporter) throws IOException {
		reporter.getCounter(COUNTER_GROUP_MAP,"total").increment(1);
		Pattern ptn = Pattern.compile("\t");
		String[] tokens = ptn.split(value.toString());
		if(tokens.length==11){
			reporter.getCounter(COUNTER_GROUP_MAP,"valid_all").increment(1);
			WordStatOrAdviewWritable ws = new WordStatOrAdviewWritable();
			ws.setTag(WordStatOrAdviewWritable.TAG_WORDSTAT);
			ws.setCookies_3days(Long.parseLong(tokens[3]));
			ws.setCookies_7days(Long.parseLong(tokens[4]));
			ws.setCookies_15days(Long.parseLong(tokens[5]));
			ws.setCookies_30days(Long.parseLong(tokens[6]));
			ws.setMaxpv_3days(Long.parseLong(tokens[7]));
			ws.setMaxpv_7days(Long.parseLong(tokens[8]));
			ws.setMaxpv_15days(Long.parseLong(tokens[9]));
			ws.setMaxpv_30days(Long.parseLong(tokens[10]));

			long wordid = Long.parseLong(tokens[0]);
			int allRegion = -1;
			int firstRegion = Integer.parseInt(tokens[2]);
			int secondRegion = Integer.parseInt(tokens[1]);	
			WordKey allRegionKey = new WordKey(wordid,allRegion);
	  		output.collect(allRegionKey, ws);
	  		if(firstRegion!=0){
	  			WordKey firstRegionKey = new WordKey(wordid,firstRegion);
		  		output.collect(firstRegionKey, ws);
	  		}else{
	  			reporter.getCounter(COUNTER_GROUP_MAP,"invalid_first").increment(1);	
	  		}
	  		if(secondRegion!=0){
	  			WordKey secondRegionKey = new WordKey(wordid,secondRegion);
		  		output.collect(secondRegionKey, ws);
	  		}else{
	  			reporter.getCounter(COUNTER_GROUP_MAP,"invalid_second").increment(1);
	  		}
		}else{
			reporter.getCounter(COUNTER_GROUP_MAP,"invalid_all").increment(1);
		}
	}
}
