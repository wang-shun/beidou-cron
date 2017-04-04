package hadoop.qtkr.com.baidu.beidou.hadoop.qtkr.test;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

import hadoop.qtkr.com.baidu.beidou.hadoop.qtkr.WordKey;


public class WordStatReducer extends MapReduceBase
	implements Reducer<WordKey, LongWritable, WordKey, LongWritable> {

	private final static String COUNTER_GROUP_REDUCE = "my_reduce_record_count";

	public void reduce(WordKey key, Iterator<LongWritable> value,
			OutputCollector<WordKey, LongWritable> output, Reporter reporter)
			throws IOException {		
		reporter.getCounter(COUNTER_GROUP_REDUCE,"total").increment(1);
		long i=0;
		while(value.hasNext()){
			i+=value.next().get();
			reporter.getCounter(COUNTER_GROUP_REDUCE,"valid_all").increment(1);			
		}
		if(i>1){
			output.collect(key, new LongWritable(i));
		}
	}
	
}
