package org.example.os.lab3.domain;

public class Config {
    public static int virtPageNum = 63;
    public static long block = (int) Math.pow(2, 12);
    public static String output = null;
    public static byte addressradix = 10;
    public static boolean doStdoutLog = false;
    public static boolean doFileLog = false;


    public static long getAddressLimit() {
        return block * virtPageNum;
    }

    public static void setBlock(long block) {
        if (block < 64 || block > Math.pow(2, 26)) {
            throw new RuntimeException("MemoryManagement: pagesize is out of bounds");
        }
        Config.block = block;
    }

    public static void setVirtPageNum(int virtPageNum) {
        if (virtPageNum < 2 || virtPageNum > 63) {
            throw new RuntimeException("MemoryManagement: numpages out of bounds.");
        }
        Config.virtPageNum = virtPageNum;
    }

    public static void setAddressradix(byte addressradix) {
        if (addressradix < 0 || addressradix > 20) {
            throw new RuntimeException("MemoryManagement: addressradix out of bounds.");
        }
        Config.addressradix = addressradix;
    }
}
