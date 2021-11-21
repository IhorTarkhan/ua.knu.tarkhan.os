package org.example.os.lab2;

public class Process {
    public final int totalSize;
    public final int batchSize;
    public int totalProgress;
    public int batchProgress;
    public int blockersCount;

    public Process(int totalSize, int batchSize) {
        this.totalSize = totalSize;
        this.batchSize = batchSize;
        this.totalProgress = 0;
        this.batchProgress = 0;
        this.blockersCount = 0;
    }

    public boolean isFinish() {
        return totalProgress >= totalSize;
    }

    public boolean isActive() {
        return !isFinish();
    }

    public boolean isBatchFinish() {
        return batchSize == batchProgress;
    }
}
