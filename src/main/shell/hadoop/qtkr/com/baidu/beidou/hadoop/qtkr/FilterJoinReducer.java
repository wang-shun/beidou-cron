package hadoop.qtkr.com.baidu.beidou.hadoop.qtkr;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

public class FilterJoinReducer extends MapReduceBase
	implements Reducer<WordKey, WordStatAndAdviewWritable, LongWritable, WordStatAndAdviewWritable> {

	private int maxpv_threshold = 0;	
	@Override
	public void configure(JobConf jobConf) {		
		maxpv_threshold = jobConf.getInt("krstat.maxpv.threshold", 5);
	}
	
	public void reduce(WordKey key, Iterator<WordStatAndAdviewWritable> values,
			OutputCollector<LongWritable, WordStatAndAdviewWritable> output,
			Reporter arg3) throws IOException {		
		WordStatAndAdviewWritable v = values.next();
		// ����һ������ȫ����ͳ�ƣ���ȫ����ͳ�Ƶ�maxpv_30daysС����ֵ������˵�
		if(v.getRegion()!=-1 || v.getMaxpv_30days()<maxpv_threshold){
			return;
		}
		LongWritable wordid = new LongWritable(key.getWordid()); 
		output.collect(wordid, v);
		while(values.hasNext()){
			v = values.next();
			output.collect(wordid, v);
		}
	}

}
