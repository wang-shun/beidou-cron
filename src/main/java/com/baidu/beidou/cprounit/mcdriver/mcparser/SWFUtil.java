package com.baidu.beidou.cprounit.mcdriver.mcparser;
/**
 * SWFUtil.java
 *
 * Copyright 2010 @company@, Inc.
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

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//import com.baidu.common.flash.util.CoreUtil;

/**
 * @author @author@ (@author-email@)
 * 
 * @version @version@, $Date: 2010-8-24$
 * 
 */
public class SWFUtil {
    //private static final Log log = LogFactory.getLog(SWFUtil.class);

    public static final int calcHeaderLength(byte[] swf) {
        // Stage dimensions are stored in a rect
        int nbits = ((swf[8] & 0xff) >> 3);

        int maxBits = nbits * 4 + 5;
        int maxBytes = maxBits / 8 + (maxBits % 8 == 0 ? 0 : 1);

        return maxBytes + 12;
    }

    public static final int readInt(byte[] data, int curByte) {
        return (data[curByte] & 0xff) + ((data[curByte + 1] & 0xff) << 8) + ((data[curByte + 2] & 0xff) << 16)
                + ((data[curByte + 3] & 0xff) << 24);
    }

    public static final int readShort(byte[] data, int curByte) {
        return (data[curByte] & 0xff) + ((data[curByte + 1] & 0xff) << 8);
    }

    public static byte[] repack(byte[] data) {
        //log.info(CoreUtil.getLogId() + " In SWFRepacker Original data size=" + data.length);
        byte[] de = null;
        if (isCompressed(data[0])) {
            de = decompress(data);
            //log.info(CoreUtil.getLogId() + " Decompressd data size=" + (de.length + 8));
        }
        data = compress(de);
        //log.info(CoreUtil.getLogId() + " Out SWFRepacker Final data size=" + data.length);
        return data;
    }

    public static byte[] decompress(final byte[] data) {
        Inflater decompresser = new Inflater();
        // feed the Inflater the bytes
        decompresser.setInput(data, 8, data.length - 8);
        byte[] result = new byte[data.length];
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length * 2);
        bos.write(data, 0, 8);
        int resultLength = 0;
        try {
            while ((resultLength = decompresser.inflate(result)) > 0) {
                bos.write(result, 0, resultLength);
            }
        } catch (DataFormatException e) {
            throw new IllegalArgumentException("Swf file has been distorted - " + e.getMessage());
        }
        decompresser.end();
        byte[] input = bos.toByteArray();
        // the first byte of the swf indicates the swf is uncompressed
        input[0] = 70;
        return input;
    }

    public static byte[] compress(byte[] data) {
        Deflater compresser = new Deflater(Deflater.BEST_COMPRESSION);
        compresser.setInput(data, 8, data.length - 8);
        compresser.finish();
        byte[] result = new byte[data.length];
        int clen = compresser.deflate(result);
        if (clen == result.length) {
            // compressed too large, use uncompressed data instead
            return data;
        } else {
            byte[] tmp = new byte[clen + 8];
            // copy the first 8 bytes which are uncompressed into the swf array
            System.arraycopy(data, 0, tmp, 0, 8);
            // copy the uncompressed data into the swf array
            System.arraycopy(result, 0, tmp, 8, clen);
            // the first byte of the swf indicates the swf is compressed
            tmp[0] = 67;
            return tmp;
        }
    }

    public static boolean isCompressed(int firstByte) {
        if (firstByte == 67) {
            return true;
        } else {
            return false;
        }
    }
}
