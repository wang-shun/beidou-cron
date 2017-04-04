package hadoop.qtkr.com.baidu.beidou.hadoop.util;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MultiFileInputFormat;
import org.apache.hadoop.mapred.MultiFileSplit;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;

public class MultiFileUtils {

	public static class WordOffset implements WritableComparable<WordOffset> {

	    private long offset;
	    private String fileName;

	    public long getOffset() {
			return offset;
		}

		public void setOffset(long offset) {
			this.offset = offset;
		}

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public void readFields(DataInput in) throws IOException {
			this.offset = in.readLong();
			this.fileName = Text.readString(in);
	    }

	    public void write(DataOutput out) throws IOException {
	    	out.writeLong(offset);
	    	Text.writeString(out, fileName);
	    }

	    public int compareTo(WordOffset that) {
	    	int f = this.fileName.compareTo(that.fileName);
	    	if(f == 0) {
	    		return (int)Math.signum((double)(this.offset - that.offset));
	    	}
	    	return f;
	    }
	    @Override
	    public boolean equals(Object obj) {
	    	if(obj instanceof WordOffset)
	    		return this.compareTo((WordOffset)obj) == 0;
	    	return false;
	    }
	    @Override
	    public int hashCode() {	    	
	      	return new Long(offset).hashCode()+fileName.hashCode();
	    }
	}

	  /**
	   * To use {@link MultiFileInputFormat}, one should extend it, to return a 
	   * (custom) {@link RecordReader}. MultiFileInputFormat uses 
	   * {@link MultiFileSplit}s. 
	   */
	public static class MyMultiFileInputFormat 
	    extends MultiFileInputFormat<WordOffset, Text>  {

	    @Override
	    public RecordReader<WordOffset,Text> getRecordReader(InputSplit split
	        , JobConf job, Reporter reporter) throws IOException {
	      return new MultiFileLineRecordReader(job, (MultiFileSplit)split);
	    }
	}

	  /**
	   * RecordReader is responsible from extracting records from the InputSplit. 
	   * This record reader accepts a {@link MultiFileSplit}, which encapsulates several 
	   * files, and no file is divided.
	   */
	public static class MultiFileLineRecordReader 
	    implements RecordReader<WordOffset, Text> {

	    private MultiFileSplit split;
	    private long offset; //total offset read so far;
	    private long totLength;
	    private FileSystem fs;
	    private int count = 0;
	    private Path[] paths;
	    
	    private FSDataInputStream currentStream;
	    private BufferedReader currentReader;
	    
	    public MultiFileLineRecordReader(Configuration conf, MultiFileSplit split)
	      throws IOException {
	      
	      this.split = split;
	      fs = FileSystem.get(conf);
	      this.paths = split.getPaths();
	      this.totLength = split.getLength();
	      this.offset = 0;
	      
	      //open the first file
	      Path file = paths[count];
	      currentStream = fs.open(file);
	      currentReader = new BufferedReader(new InputStreamReader(currentStream));
	    }

	    public void close() throws IOException { }

	    public long getPos() throws IOException {
	      long currentOffset = currentStream == null ? 0 : currentStream.getPos();
	      return offset + currentOffset;
	    }

	    public float getProgress() throws IOException {
	      return ((float)getPos()) / totLength;
	    }

	    public boolean next(WordOffset key, Text value) throws IOException {
	      if(count >= split.getNumPaths())
	        return false;

	      /* Read from file, fill in key and value, if we reach the end of file,
	       * then open the next file and continue from there until all files are
	       * consumed.  
	       */
	      String line;
	      do {
	        line = currentReader.readLine();
	        if(line == null) {
	          //close the file
	          currentReader.close();
	          offset += split.getLength(count);
	          
	          if(++count >= split.getNumPaths()) //if we are done
	            return false;
	          
	          //open a new file
	          Path file = paths[count];
	          currentStream = fs.open(file);
	          currentReader=new BufferedReader(new InputStreamReader(currentStream));
	          key.fileName = file.getName();
	        }
	      } while(line == null);
	      //update the key and value
	      key.offset = currentStream.getPos();
	      value.set(line);
	      
	      return true;
	    }

	    public WordOffset createKey() {
	      WordOffset wo = new WordOffset();
	      wo.fileName = paths[0].toString(); //set as the first file
	      return wo;
	    }

	    public Text createValue() {
	      return new Text();
	    }
	}

}
