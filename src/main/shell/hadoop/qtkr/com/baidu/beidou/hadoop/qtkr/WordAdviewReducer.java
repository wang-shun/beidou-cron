package hadoop.qtkr.com.baidu.beidou.hadoop.qtkr;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

public class WordAdviewReducer extends MapReduceBase
	implements Reducer<WordKey, WordStatOrAdviewWritable, WordKey, WordStatOrAdviewWritable> {
	
	public void reduce(WordKey key, Iterator<WordStatOrAdviewWritable> value,
			OutputCollector<WordKey, WordStatOrAdviewWritable> output, Reporter reporter)
			throws IOException {		
		long adviews=0;
		while(value.hasNext()){
			WordStatOrAdviewWritable ws = value.next();
			adviews += ws.getAdview();
		}
		
		WordStatOrAdviewWritable avg = new WordStatOrAdviewWritable();
		avg.setTag(WordStatOrAdviewWritable.TAG_ADVIEW);
		avg.setAdview(getWeekAvg(adviews));
		
		output.collect(key, avg);		
	}
	
	private long getWeekAvg(long sum){
		if(sum==0){
			return 0;
		}
		return sum/7;
	}

}
