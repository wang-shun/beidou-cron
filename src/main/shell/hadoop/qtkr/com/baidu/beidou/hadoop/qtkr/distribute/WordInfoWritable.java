package hadoop.qtkr.com.baidu.beidou.hadoop.qtkr.distribute;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public class WordInfoWritable implements Writable{	
	private Long cookies_30days = null;	
	private Long maxpv_30days = null;
	private Long adview = null;

	public Long getCookies_30days() {
		return cookies_30days;
	}

	public void setCookies_30days(Long cookies_30days) {
		this.cookies_30days = cookies_30days;
	}

	public Long getMaxpv_30days() {
		return maxpv_30days;
	}

	public void setMaxpv_30days(Long maxpv_30days) {
		this.maxpv_30days = maxpv_30days;
	}

	public Long getAdview() {
		return adview;
	}

	public void setAdview(Long adview) {
		this.adview = adview;
	}

	public void readFields(DataInput in) throws IOException {
		cookies_30days=in.readLong();
		maxpv_30days=in.readLong();
		adview=in.readLong();
	}

	public void write(DataOutput out) throws IOException {
		out.writeLong(cookies_30days);
		out.writeLong(maxpv_30days);
		out.writeLong(adview);
	}

	public String toString() {
		String str = Long.toString(cookies_30days)
		    +"\t"+Long.toString(maxpv_30days)
			+"\t"+Long.toString(adview);		
	    return str;
	}
}
