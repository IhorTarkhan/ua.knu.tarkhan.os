package org.example.os.lab3;

import java.util.Random;

public class Common {
    static public int s2i(String s) {
        return Integer.parseInt(s.trim());
    }

    static public byte s2b(String s) {
        return Byte.parseByte(s.trim());
    }

    public static long randomLong(long maxValue) {
        return new Random(System.currentTimeMillis()).nextLong(0, maxValue);
    }
}

