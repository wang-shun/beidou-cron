
package com.baidu.beidou.unionsite.bo;

import java.io.Serializable;


public class IntegerEntry implements Serializable {

    private int key;
    private long value;
    
    public IntegerEntry(int key, long value) {
        this.key = key;
        this.value =value;
    }
    public int getKey() {
        return key;
    }
    public void setKey(int key) {
        this.key = key;
    }
    public long getValue() {
        return value;
    }
    public void setValue(long value) {
        this.value = value;
    }
    public String toString() {
        return key + "," + value;
    }
}

