package org.example.os.lab2;

public class Process {
    public final int totalSize;
    public final int batchSize;
    public int totalProgress;
    public int batchProgress;
    public int blockersCount;

    public Process(int totalSize, int batchSize, int totalProgress, int batchProgress, int blockersCount) {
        this.totalSize = totalSize;
        this.batchSize = batchSize;
        this.totalProgress = totalProgress;
        this.batchProgress = batchProgress;
        this.blockersCount = blockersCount;
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
