package org.example.os.lab2;
// Run() is called from Scheduling.main() and is where
// the scheduling algorithm written by the user resides.
// User modification should occur within the Run() function.

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class SchedulingAlgorithm {

    public static Results run(int maxRunTime, List<Process> processVector, Results result) {
        result.schedulingType = "Batch (Nonpreemptive)";
        result.schedulingName = "First-Come First-Served";

        int currentProcessIndex = 0;
        int completed = 0;

        try (PrintStream out = new PrintStream(new FileOutputStream("Summary-Processes"))) {
            Process currentProcess = processVector.get(currentProcessIndex);
            printRegistered(out, currentProcessIndex, currentProcess);
            for (int currentRunTime = 0; currentRunTime < maxRunTime; currentRunTime++) {
                if (currentProcess.cpu_done == currentProcess.cpu_time) { // if process finish
                    completed++;
                    printlnCompleted(out, currentProcessIndex, currentProcess);
                    if (completed == processVector.size()) { // if all finish
                        result.compuTime = currentRunTime;
                        return result;
                    }
                    for (int i = processVector.size() - 1; i >= 0; i--) {
                        currentProcess = processVector.get(i);
                        if (currentProcess.cpu_done < currentProcess.cpu_time) {
                            currentProcessIndex = i;
                        }
                    }
                    currentProcess = processVector.get(currentProcessIndex);
                    printRegistered(out, currentProcessIndex, currentProcess);
                }
                if (currentProcess.io_blocking == currentProcess.io_next) { // if batch done
                    printlnBlocked(out, currentProcessIndex, currentProcess);
                    currentProcess.num_blocked++;
                    currentProcess.io_next = 0;
                    int previousProcess = currentProcessIndex;
                    for (int i = processVector.size() - 1; i >= 0; i--) {
                        currentProcess = processVector.get(i);
                        if (currentProcess.cpu_done < currentProcess.cpu_time && previousProcess != i) {
                            currentProcessIndex = i;
                        }
                    }
                    currentProcess = processVector.get(currentProcessIndex);
                    printRegistered(out, currentProcessIndex, currentProcess);
                }
                currentProcess.cpu_done++;
                if (currentProcess.io_blocking > 0) {
                    currentProcess.io_next++;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static void printRegistered(PrintStream out, int processIndex, Process process) {
        out.println("Process: " + processIndex + " registered... (" + process.cpu_time + " " + process.io_blocking + " " + process.cpu_done + " " + process.cpu_done + ")");
    }

    private static void printlnCompleted(PrintStream out, int processIndex, Process process) {
        out.println("Process: " + processIndex + " completed... (" + process.cpu_time + " " + process.io_blocking + " " + process.cpu_done + " " + process.cpu_done + ")");
    }

    private static void printlnBlocked(PrintStream out, int processIndex, Process process) {
        out.println("Process: " + processIndex + " I/O blocked... (" + process.cpu_time + " " + process.io_blocking + " " + process.cpu_done + " " + process.cpu_done + ")");
    }
}
