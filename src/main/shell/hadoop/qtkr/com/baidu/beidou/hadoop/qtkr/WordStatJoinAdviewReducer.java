package hadoop.qtkr.com.baidu.beidou.hadoop.qtkr;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

public class WordStatJoinAdviewReducer extends MapReduceBase
	implements Reducer<WordKey, WordStatOrAdviewWritable, WordKey, WordStatAndAdviewWritable> {

	public void reduce(WordKey key, Iterator<WordStatOrAdviewWritable> values,
			OutputCollector<WordKey, WordStatAndAdviewWritable> output,
			Reporter arg3) throws IOException {
		WordStatAndAdviewWritable ws = new WordStatAndAdviewWritable();
		ws.setRegion(key.getRegion());
		boolean hasLeftJoin = false;
		// ������ѭ������һ����������ֻ��������ݣ�ƴ�������Ҳ����
		while(values.hasNext()){
			WordStatOrAdviewWritable v = values.next();
			if(v.getTag()==WordStatOrAdviewWritable.TAG_ADVIEW){
				ws.setAdview(v.getAdview());
			}else if(v.getTag()==WordStatOrAdviewWritable.TAG_WORDSTAT){
				hasLeftJoin = true;
				ws.setCookies_3days(v.getCookies_3days());
				ws.setCookies_7days(v.getCookies_7days());
				ws.setCookies_15days(v.getCookies_15days());
				ws.setCookies_30days(v.getCookies_30days());
				ws.setMaxpv_3days(v.getMaxpv_3days());
				ws.setMaxpv_7days(v.getMaxpv_7days());
				ws.setMaxpv_15days(v.getMaxpv_15days());
				ws.setMaxpv_30days(v.getMaxpv_30days());
			}
		}
		// ����UFSͳ������Ǳ���ģ�����������ֱ�ӷ���
		if(!hasLeftJoin){
			return;
		}
		// �Ҳ��adview��ݲ��Ǳ���ģ��������������Ĭ��ֵ0����ռλ
		if(ws.getAdview()==null){
			ws.setAdview(new Long(0));
		}
		output.collect(key, ws);
	}

}
