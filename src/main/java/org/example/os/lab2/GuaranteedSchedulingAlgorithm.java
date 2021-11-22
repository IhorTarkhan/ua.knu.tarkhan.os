package org.example.os.lab2;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class GuaranteedSchedulingAlgorithm {

    public static Results run(int maxRunTime, List<Process> processes) {
        int currentProcessIndex = 0;
        int currentRunTime = 0;

        try (PrintStream out = new PrintStream(new FileOutputStream("Summary-Processes"))) {
            Process currentProcess = processes.get(currentProcessIndex);
            printRegistered(out, currentProcessIndex, currentProcess);
            while (processes.stream().anyMatch(Process::isActive)) {
                if (currentProcess.isFinish()) {
                    printlnCompleted(out, currentProcessIndex, currentProcess);

                    currentProcessIndex = getNextProcessIndex(currentProcessIndex, processes);
                    currentProcess = processes.get(currentProcessIndex);

                    printRegistered(out, currentProcessIndex, currentProcess);
                }
                if (currentProcess.isBatchFinish()) {
                    printlnBlocked(out, currentProcessIndex, currentProcess);

                    currentProcess.blockersCount++;
                    currentProcess.batchProgress = 0;
                    currentProcessIndex = getNextProcessIndex(currentProcessIndex, processes);
                    currentProcess = processes.get(currentProcessIndex);

                    printRegistered(out, currentProcessIndex, currentProcess);
                }
                currentProcess.totalProgress++;
                currentProcess.batchProgress++;
                currentRunTime++;
            }
            printlnCompleted(out, currentProcessIndex, currentProcess);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Results("Batch (Preemptive)", "Guaranteed", currentRunTime);
    }

    private static int getNextProcessIndex(int defaultValue, List<Process> processes) {
        int result = defaultValue;
        for (int i = 0; i < processes.size(); i++) {
            if (processes.get(result).isFinish()) {
                result = i;
            }
            if (processes.get(i).isActive() &&
                    processes.get(i).totalProgress < processes.get(result).totalProgress) {
                result = i;
            }
        }
        return result;
    }

    private static void printRegistered(PrintStream out, int processIndex, Process process) {
        out.println("Process: " + processIndex + " registered... (" + process.totalSize + " " + process.batchSize + " " + process.totalProgress + " " + process.totalProgress + ")");
    }

    private static void printlnCompleted(PrintStream out, int processIndex, Process process) {
        out.println("Process: " + processIndex + " completed... (" + process.totalSize + " " + process.batchSize + " " + process.totalProgress + " " + process.totalProgress + ")");
    }

    private static void printlnBlocked(PrintStream out, int processIndex, Process process) {
        out.println("Process: " + processIndex + " I/O blocked... (" + process.totalSize + " " + process.batchSize + " " + process.totalProgress + " " + process.totalProgress + ")");
    }
}
