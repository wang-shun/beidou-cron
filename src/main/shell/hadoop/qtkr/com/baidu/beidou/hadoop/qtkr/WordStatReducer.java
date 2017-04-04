package hadoop.qtkr.com.baidu.beidou.hadoop.qtkr;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;


public class WordStatReducer extends MapReduceBase
	implements Reducer<WordKey, WordStatOrAdviewWritable, WordKey, WordStatOrAdviewWritable> {

	private final static String COUNTER_GROUP_REDUCE = "my_reduce_record_count";

	public void reduce(WordKey key, Iterator<WordStatOrAdviewWritable> value,
			OutputCollector<WordKey, WordStatOrAdviewWritable> output, Reporter reporter)
			throws IOException {
		reporter.getCounter(COUNTER_GROUP_REDUCE,"total").increment(1);
		long cookies_3days=0,cookies_7days=0,cookies_15days=0,cookies_30days=0;
		long maxpv_3days=0,maxpv_7days=0,maxpv_15days=0,maxpv_30days=0;
		while(value.hasNext()){
			reporter.getCounter(COUNTER_GROUP_REDUCE,"valid_all").increment(1);
			WordStatOrAdviewWritable ws = value.next();
			cookies_3days += ws.getCookies_3days();
			cookies_7days += ws.getCookies_7days();
			cookies_15days += ws.getCookies_15days();
			cookies_30days += ws.getCookies_30days();
			maxpv_3days += ws.getMaxpv_3days();
			maxpv_7days += ws.getMaxpv_7days();
			maxpv_15days += ws.getMaxpv_15days();
			maxpv_30days += ws.getMaxpv_30days();
		}
		
		WordStatOrAdviewWritable avg = new WordStatOrAdviewWritable();
		avg.setTag(WordStatOrAdviewWritable.TAG_WORDSTAT);
		avg.setCookies_3days(getWeekAvg(cookies_3days));
		avg.setCookies_7days(getWeekAvg(cookies_7days));
		avg.setCookies_15days(getWeekAvg(cookies_15days));
		avg.setCookies_30days(getWeekAvg(cookies_30days));
		avg.setMaxpv_3days(getWeekAvg(maxpv_3days));
		avg.setMaxpv_7days(getWeekAvg(maxpv_7days));
		avg.setMaxpv_15days(getWeekAvg(maxpv_15days));
		avg.setMaxpv_30days(getWeekAvg(maxpv_30days));
		
		output.collect(key, avg);
	}
	
	private long getWeekAvg(long sum){
		if(sum==0){
			return 0;
		}
		return sum/7;
	}

}
