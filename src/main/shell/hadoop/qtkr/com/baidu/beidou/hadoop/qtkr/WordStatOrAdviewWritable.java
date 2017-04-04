package hadoop.qtkr.com.baidu.beidou.hadoop.qtkr;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public class WordStatOrAdviewWritable implements Writable{
	public final static int TAG_WORDSTAT = 1;
	public final static int TAG_ADVIEW = 2;
	private int tag;
	private Long cookies_3days = null;
	private Long cookies_7days = null;
	private Long cookies_15days = null;
	private Long cookies_30days = null;
	private Long maxpv_3days = null;
	private Long maxpv_7days = null;
	private Long maxpv_15days = null;
	private Long maxpv_30days = null;
	private Long adview = null;

	public int getTag() {
		return tag;
	}

	public void setTag(int tag) {
		this.tag = tag;
	}

	public Long getCookies_3days() {
		return cookies_3days;
	}

	public void setCookies_3days(Long cookies_3days) {
		this.cookies_3days = cookies_3days;
	}

	public Long getCookies_7days() {
		return cookies_7days;
	}

	public void setCookies_7days(Long cookies_7days) {
		this.cookies_7days = cookies_7days;
	}

	public Long getCookies_15days() {
		return cookies_15days;
	}

	public void setCookies_15days(Long cookies_15days) {
		this.cookies_15days = cookies_15days;
	}

	public Long getCookies_30days() {
		return cookies_30days;
	}

	public void setCookies_30days(Long cookies_30days) {
		this.cookies_30days = cookies_30days;
	}

	public Long getMaxpv_3days() {
		return maxpv_3days;
	}

	public void setMaxpv_3days(Long maxpv_3days) {
		this.maxpv_3days = maxpv_3days;
	}

	public Long getMaxpv_7days() {
		return maxpv_7days;
	}

	public void setMaxpv_7days(Long maxpv_7days) {
		this.maxpv_7days = maxpv_7days;
	}

	public Long getMaxpv_15days() {
		return maxpv_15days;
	}

	public void setMaxpv_15days(Long maxpv_15days) {
		this.maxpv_15days = maxpv_15days;
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
		tag=in.readInt();
		if(tag==TAG_WORDSTAT){
			cookies_3days=in.readLong();
			cookies_7days=in.readLong();
			cookies_15days=in.readLong();
			cookies_30days=in.readLong();
			maxpv_3days=in.readLong();
			maxpv_7days=in.readLong();
			maxpv_15days=in.readLong();
			maxpv_30days=in.readLong();
		}else{
			adview=in.readLong();
		}
	}


	public void write(DataOutput out) throws IOException {
		out.writeInt(tag);
		if(tag==TAG_WORDSTAT){
			out.writeLong(cookies_3days);
			out.writeLong(cookies_7days);
			out.writeLong(cookies_15days);
			out.writeLong(cookies_30days);
			out.writeLong(maxpv_3days);
			out.writeLong(maxpv_7days);
			out.writeLong(maxpv_15days);
			out.writeLong(maxpv_30days);
		}else{
			out.writeLong(adview);
		}
	}

	public String toString() {
		String str = "";
		if(tag==TAG_WORDSTAT){
			str = Long.toString(cookies_3days)
		    +"\t"+Long.toString(cookies_7days)
		    +"\t"+Long.toString(cookies_15days)
		    +"\t"+Long.toString(cookies_30days)
		    +"\t"+Long.toString(maxpv_3days)
		    +"\t"+Long.toString(maxpv_7days)
		    +"\t"+Long.toString(maxpv_15days)
		    +"\t"+Long.toString(maxpv_30days);
		}else{
			str = Long.toString(adview);
		}
	    return str;
	}
}
