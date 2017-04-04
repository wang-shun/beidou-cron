package com.baidu.beidou.cprounit.mcdriver.mcparser;
/**
 * JFIFFile.java
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


import java.io.IOException;

/**
 * @author @author@ (@author-email@)
 * 
 * @version @version@, $Date: 2011-6-30$
 * 
 */
public class JFIFFile {
    private final JFIFReader reader;
    private String app1, app2;

    public JFIFFile(JFIFReader reader) throws IOException {
        super();
        this.reader = reader;
        init();
    }
    
    public JFIFFile(byte[] data) throws IOException {
        this(new JFIFReader(data, 0));
    }

    private void init() throws IOException {
        int head = reader.readShort();
        if (head != 0xffd8) {
            throw new IOException("It's not JFIF format.");
        }
        int tag, length;
        tag = reader.readShort();
        length = reader.readShort();
        if (tag == 0xffe0) {
            reader.movePos(length - 2);
            tag = reader.readShort();
            length = reader.readShort();
        }
        if (tag == 0xffe1) {
            app1 = reader.readString(length - 2);
            if (!app1.contains("Admaker@baidu.com_")) {
                throw new IOException("It's not Admaker format.");
            }
            tag = reader.readShort();
            length = reader.readShort();
        }
    }

    public String getApp1() {
        return app1;
    }

    public String getApp2() {
        return app2;
    }
    
}
