package com.baidu.beidou.cprounit.mcdriver.mcparser;
/**
 * JFIFReader.java
 *
 * Copyright 2011 @company@, Inc.
 *
 * @company@ licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */


import java.io.UnsupportedEncodingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author @author@ (@author-email@)
 * 
 * @version @version@, $Date: 2011-6-30$
 * 
 */
public class JFIFReader {
    private static final Log log = LogFactory.getLog(JFIFReader.class);

    private final byte[] data;
    private int dataPos;

    public JFIFReader(byte[] data, int dataPos) {
        super();
        this.data = data;
        this.dataPos = dataPos;
    }
    
    public final void movePos(int pos) {
        dataPos += pos;
    }

    public final int readShort() {
        return ((data[dataPos++] & 0xff) << 8) + (data[dataPos++] & 0xff);
    }
    
    public final int readInt() {
        return ((data[dataPos++] & 0xff) << 24) + ((data[dataPos++] & 0xff) << 16) + ((data[dataPos++] & 0xff) << 8)
                + (data[dataPos++] & 0xff);
    }

    public final String readString(int size) {
        String s = null;
        try {
            s = new String(data, dataPos, size, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.warn("Error occured when create string: " + e.getMessage(), e);
        }
        dataPos += size;
        return s;
    }
    
}
