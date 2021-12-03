package org.example.os.lab3.domain;

public class Page {
    public final int id;
    public int physical;
    public byte R;
    public byte M;
    public int inMemTime;
    public int lastTouchTime;
    public final long high;
    public final long low;

    public Page(int id, long high, long low) {
        this.id = id;
        this.physical = -1;
        this.R = 0;
        this.M = 0;
        this.inMemTime = 0;
        this.lastTouchTime = 0;
        this.high = high;
        this.low = low;

    }

    public Page(int id, int physical, byte R, byte M, int inMemTime, int lastTouchTime, long high, long low) {
        validate(R, M, inMemTime, lastTouchTime);
        this.id = id;
        this.physical = physical;
        this.R = R;
        this.M = M;
        this.inMemTime = inMemTime;
        this.lastTouchTime = lastTouchTime;
        this.high = high;
        this.low = low;
    }

    private void validate(byte R, byte M, int inMemTime, int lastTouchTime) {
        if (R < 0 || R > 1) {
            throw new RuntimeException("MemoryManagement: Invalid R value in config file");
        }
        if (M < 0 || M > 1) {
            throw new RuntimeException("MemoryManagement: Invalid M value in config file");
        }
        if (inMemTime < 0) {
            throw new RuntimeException("MemoryManagement: Invalid inMemTime in config file");
        }
        if (lastTouchTime < 0) {
            throw new RuntimeException("MemoryManagement: Invalid lastTouchTime in config file");
        }
    }
}
