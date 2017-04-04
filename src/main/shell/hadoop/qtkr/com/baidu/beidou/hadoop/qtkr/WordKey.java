package hadoop.qtkr.com.baidu.beidou.hadoop.qtkr;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Partitioner;

public class WordKey implements WritableComparable<WordKey>{
	private long wordid;
	private int region;
	
	public WordKey(){}
	
	public WordKey(long wordid,int region){
		this.wordid=wordid;
		this.region=region;
	}
	
	public long getWordid() {
		return wordid;
	}
	public void setWordid(long wordid) {
		this.wordid = wordid;
	}
	public int getRegion() {
		return region;
	}
	public void setRegion(int region) {
		this.region = region;
	}
	
	public void set(long wordid,int region){
		this.wordid=wordid;
		this.region=region;
	}
	
	public void readFields(DataInput in) throws IOException {
		wordid = in.readLong();
		region = in.readInt();
	}

	public void write(DataOutput out) throws IOException {
		out.writeLong(wordid);
		out.writeInt(region);
	}

	public int compareTo(WordKey o) {
		int aCompare = wordid < o.getWordid() ? -1 : wordid > o.getWordid() ? 1 : 0;
		if(aCompare!=0){
			return aCompare;
		}
		int bCompare = region < o.getRegion() ? -1 : region > o.getRegion() ? 1 : 0;
		return bCompare;
	}
	
	@Override
	public int hashCode() {
		return new Long(wordid).hashCode() + 31 * region;
	}
	  
	@Override
	public boolean equals(Object o) {
		if (o instanceof WordKey) {
			WordKey w = (WordKey) o;
			return wordid==w.getWordid() && region == w.getRegion();
		}
		return false;
	}
	  
	@Override
	public String toString() {
		return wordid + "\t" + region;
	}
	
	public static class WordKeyPartitioner implements Partitioner<WordKey,Writable> {
		public int getPartition(WordKey key, Writable value, int numPartitions) {
			return new Long(key.getWordid()).hashCode() % numPartitions;
		}

		public void configure(JobConf arg0) {}
	}

	public static class WordKeyGroupComparator extends WritableComparator implements Serializable {
		public WordKeyGroupComparator() {
			super(WordKey.class, true);
		}

		@Override
		public int compare(WritableComparable a, WritableComparable b) {
			WordKey aw = (WordKey)a;
			WordKey bw = (WordKey)b;
			int aCompare = aw.getWordid() < bw.getWordid() ? -1 : aw.getWordid() > bw.getWordid() ? 1 : 0;
			return aCompare;
		}
	}	
}
