package org.example.os.lab2;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class GuaranteedSchedulingAlgorithm {

    public static Results run(int maxRunTime, List<Process> processes, Results result) {
        result.schedulingType = "Batch (Preemptive)";
        result.schedulingName = "Guaranteed";

        int currentProcessIndex = 0;
        int currentRunTime = 0;

        try (PrintStream out = new PrintStream(new FileOutputStream("Summary-Processes"))) {
            Process currentProcess = processes.get(currentProcessIndex);
            printRegistered(out, currentProcessIndex, currentProcess);
            while (currentRunTime < maxRunTime && processes.stream().anyMatch(Process::isActive)) {
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
                    currentProcessIndex = getNextProcessIndex(currentProcessIndex, processes, currentProcessIndex);
                    currentProcess = processes.get(currentProcessIndex);

                    printRegistered(out, currentProcessIndex, currentProcess);
                }
                currentProcess.totalProgress++;
                currentProcess.batchProgress++;
                currentRunTime++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        result.compuTime = currentRunTime;
        return result;
    }

    private static int getNextProcessIndex(int defaultValue, List<Process> processes) {
        return getNextProcessIndex(defaultValue, processes, -1);
    }

    private static int getNextProcessIndex(int defaultValue, List<Process> processes, int skipIndex) {
        int index = defaultValue;
        Process currentProcess;
        for (int i = processes.size() - 1; i >= 0; i--) {
            currentProcess = processes.get(i);
            if (currentProcess.isActive() && i != skipIndex) {
                index = i;
            }
        }
        return index;
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
