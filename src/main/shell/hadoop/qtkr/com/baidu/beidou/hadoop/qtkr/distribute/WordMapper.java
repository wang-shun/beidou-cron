package hadoop.qtkr.com.baidu.beidou.hadoop.qtkr.distribute;

import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

public class WordMapper extends MapReduceBase
	implements Mapper<LongWritable,Text, LongWritable, WordInfoWritable> {

	public void map(LongWritable key, Text value,
			OutputCollector<LongWritable, WordInfoWritable> output, Reporter arg3)
			throws IOException {
		Pattern ptn = Pattern.compile("\t");
		String[] tokens = ptn.split(value.toString());
		if(tokens.length==11){
			int region = Integer.parseInt(tokens[1]);
			// ֻȡ��ͳ�ƽ���е�ȫ�������ͳ�����ڷ���
			if(region!=-1){
				return;
			}
			long wordid = Long.parseLong(tokens[0]);			
			WordInfoWritable ws = new WordInfoWritable();
			ws.setCookies_30days(Long.parseLong(tokens[5]));
			ws.setMaxpv_30days(Long.parseLong(tokens[9]));
			ws.setAdview(Long.parseLong(tokens[10]));

			output.collect(new LongWritable(wordid), ws);
		}
	}

}
