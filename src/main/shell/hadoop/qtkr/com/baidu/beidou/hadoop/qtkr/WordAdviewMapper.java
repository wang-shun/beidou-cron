package hadoop.qtkr.com.baidu.beidou.hadoop.qtkr;

import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

public class WordAdviewMapper  extends MapReduceBase
	implements Mapper<LongWritable,Text, WordKey, WordStatOrAdviewWritable> {
	
	public void map(LongWritable key,
	                     Text value,
	                     OutputCollector<WordKey, WordStatOrAdviewWritable> output, Reporter reporter) throws IOException {
		Pattern ptn = Pattern.compile("\t");
		String[] tokens = ptn.split(value.toString());
		if(tokens.length==4){
			WordStatOrAdviewWritable ws = new WordStatOrAdviewWritable();
			ws.setTag(WordStatOrAdviewWritable.TAG_ADVIEW);
			ws.setAdview(Long.parseLong(tokens[3]));
			
			long wordid = Long.parseLong(tokens[0]);
			int allRegion = -1;
			int firstRegion = Integer.parseInt(tokens[2]);
			int secondRegion = Integer.parseInt(tokens[1]);	
			WordKey allRegionKey = new WordKey(wordid,allRegion);
	  		output.collect(allRegionKey, ws);
	  		if(firstRegion!=0){
	  			WordKey firstRegionKey = new WordKey(wordid,firstRegion);
		  		output.collect(firstRegionKey, ws);
	  		}
	  		if(secondRegion!=0){
	  			WordKey secondRegionKey = new WordKey(wordid,secondRegion);
		  		output.collect(secondRegionKey, ws);
	  		}
		}
	}
}