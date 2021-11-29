package org.example.os.lab3.domain;

public class Page {
    public final int id;
    public int physical;
    public byte R;
    public byte M;
    public int inMemTime;
    public int lastTouchTime;
    public long high;
    public long low;

    public Page(int id) {
        this.id = id;
        this.physical = -1;
        this.R = 0;
        this.M = 0;
        this.inMemTime = 0;
        this.lastTouchTime = 0;
        this.high = (Config.block * (id + 1)) - 1;
        this.low = Config.block * id;
    }

    public Page(int id, int physical, byte R, byte M, int inMemTime, int lastTouchTime) {
        validate(id, physical, R, M, inMemTime, lastTouchTime);
        this.id = id;
        this.physical = physical;
        this.R = R;
        this.M = M;
        this.inMemTime = inMemTime;
        this.lastTouchTime = lastTouchTime;
        this.high = (Config.block * (id + 1)) - 1;
        this.low = Config.block * id;
    }

    private void validate(int id, int physical, byte R, byte M, int inMemTime, int lastTouchTime) {
        if ((0 > id || id > Config.virtPageNum) || (-1 > physical || physical > ((Config.virtPageNum - 1) / 2))) {
            throw new RuntimeException("MemoryManagement: Invalid page value in config file");
        }
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
