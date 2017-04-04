/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hadoop.qtkr.com.baidu.beidou.hadoop.util;

import java.io.IOException;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputFormat;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.lib.IdentityReducer;
import org.apache.hadoop.util.Tool;

public abstract class AbstractJob extends Configured implements Tool {

  protected JobConf prepareJob(Path inputPath,
                           Path outputPath,
                           Class<? extends InputFormat> inputFormat,
                           Class<? extends Mapper> mapper,
                           Class<? extends Writable> mapperKey,
                           Class<? extends Writable> mapperValue,
                           Class<? extends Reducer> reducer,
                           Class<? extends Writable> reducerKey,
                           Class<? extends Writable> reducerValue,
                           Class<? extends OutputFormat> outputFormat) throws IOException {

	JobConf jobConf = new JobConf(getConf());

    if (reducer.equals(Reducer.class)||reducer.equals(IdentityReducer.class)) {
        if (mapper.equals(Mapper.class)) {
          throw new IllegalStateException("Can't figure out the user class jar file from mapper/reducer");
        }
        jobConf.setJarByClass(mapper);
    } else {
    	jobConf.setJarByClass(reducer);
    }
  
    jobConf.setInputFormat(inputFormat);
    if(inputPath!=null){
    	FileInputFormat.setInputPaths(jobConf, inputPath);
    }
    jobConf.setMapperClass(mapper);
    jobConf.setMapOutputKeyClass(mapperKey);
    jobConf.setMapOutputValueClass(mapperValue);

    jobConf.setBoolean("mapred.compress.map.output", true);

    jobConf.setReducerClass(reducer);
    jobConf.setOutputKeyClass(reducerKey);
    jobConf.setOutputValueClass(reducerValue);

    jobConf.setJobName(getCustomJobName(jobConf, mapper, reducer));

    jobConf.setOutputFormat(outputFormat);
    FileOutputFormat.setOutputPath(jobConf, outputPath);

    return jobConf;
  }

  private String getCustomJobName(JobConf jobConf,
                                  Class<? extends Mapper> mapper,
                                  Class<? extends Reducer> reducer) {
    StringBuilder name = new StringBuilder(100);
    String customJobName = jobConf.getJobName();
    if (customJobName == null || customJobName.trim().length() == 0) {
      name.append(getClass().getSimpleName());
    } else {
      name.append(customJobName);
    }
    name.append('-').append(mapper.getSimpleName());
    name.append('-').append(reducer.getSimpleName());
    return name.toString();
  }

}
