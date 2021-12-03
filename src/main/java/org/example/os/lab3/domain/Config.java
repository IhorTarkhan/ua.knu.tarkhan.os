package org.example.os.lab3.domain;

import lombok.Getter;

@Getter
public class Config {
    private final int virtualPageNum;
    private final long block;
    private final String output;
    private final byte addressRadix;
    private final boolean doStdoutLog;
    private final boolean doFileLog;

    public Config(int virtualPageNum, long block, String output, byte addressRadix, boolean doStdoutLog, boolean doFileLog) {
        validate(virtualPageNum, block, addressRadix);
        this.virtualPageNum = virtualPageNum;
        this.block = block;
        this.output = output;
        this.addressRadix = addressRadix;
        this.doStdoutLog = doStdoutLog;
        this.doFileLog = doFileLog;
    }

    public Config() {
        this.virtualPageNum = 63;
        this.block = (int) Math.pow(2, 12);
        this.output = null;
        this.addressRadix = 10;
        this.doStdoutLog = false;
        this.doFileLog = false;
    }

    private void validate(int virtualPageNum, long block, byte addressRadix) {
        if (block < 64 || block > Math.pow(2, 26)) {
            throw new RuntimeException("MemoryManagement: pagesize is out of bounds");
        }

        if (virtualPageNum < 2 || virtualPageNum > 63) {
            throw new RuntimeException("MemoryManagement: numpages out of bounds.");
        }

        if (addressRadix < 0 || addressRadix > 20) {
            throw new RuntimeException("MemoryManagement: addressRadix out of bounds.");
        }
    }

    public  long getAddressLimit() {
        return block * virtualPageNum;
    }
}
