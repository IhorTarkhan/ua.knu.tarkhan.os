package org.example.os.lab2;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Scheduling {
    private static final List<Process> processVector = new ArrayList<>();
    private static int processNum = 5;
    private static int meanDev = 1000;
    private static int standardDev = 100;
    private static int runtime = 1000;

    private static void Init(String file) {
        File f = new File(file);
        String line;
        int cpuTime = 0;
        int ioBlocking = 0;

        try (DataInputStream in = new DataInputStream(new FileInputStream(f))) {
            while ((line = in.readLine()) != null) {
                if (line.startsWith("numprocess")) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    processNum = Integer.parseInt(st.nextToken().trim());
                }
                if (line.startsWith("meandev")) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    meanDev = Integer.parseInt(st.nextToken().trim());
                }
                if (line.startsWith("standdev")) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    standardDev = Integer.parseInt(st.nextToken().trim());
                }
                if (line.startsWith("process")) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    ioBlocking = Integer.parseInt(st.nextToken().trim());
                    double X;
                    do {
                        X = Common.R1();
                    } while (X == -1.0);
                    cpuTime = (int) X * standardDev + meanDev;
                    processVector.add(new Process(cpuTime, ioBlocking));
                }
                if (line.startsWith("runtime")) {
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    runtime = Integer.parseInt(st.nextToken().trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        if (args.length != 1) {
            System.err.println("Usage: 'java Scheduling <INIT FILE>'");
            System.exit(-1);
        }
        File f = new File(args[0]);
        if (!(f.exists())) {
            System.err.println("Scheduling: error, file '" + f.getName() + "' does not exist.");
            System.exit(-1);
        }
        if (!(f.canRead())) {
            System.err.println("Scheduling: error, read of " + f.getName() + " failed.");
            System.exit(-1);
        }
        System.out.println("Working...");
        Init(args[0]);
        if (processVector.size() < processNum) {
            for (int i = 0; processVector.size() < processNum; i++) {
                double X;
                do {
                    X = Common.R1();
                } while (X == -1.0);
                int cpuTime = (int) X * standardDev + meanDev;
                processVector.add(new Process(cpuTime, i * 100));

            }
        }
        Results result = GuaranteedSchedulingAlgorithm.run(runtime, processVector);
        try (PrintStream out = new PrintStream(new FileOutputStream("Summary-Results"))) {
            out.println("Scheduling Type: " + result.schedulingType);
            out.println("Scheduling Name: " + result.schedulingName);
            out.println("Simulation Run Time: " + result.compuTime);
            out.println("Mean: " + meanDev);
            out.println("Standard Deviation: " + standardDev);
            out.println("Process #\tCPU Time\tIO Blocking\tCPU Completed\tCPU Blocked");
            for (int i = 0; i < processVector.size(); i++) {
                Process process = processVector.get(i);
                out.print(i);
                if (i < 100) {
                    out.print("\t\t");
                } else {
                    out.print("\t");
                }
                out.print(process.totalSize);
                if (process.totalSize < 100) {
                    out.print(" (ms)\t\t");
                } else {
                    out.print(" (ms)\t");
                }
                out.print(process.batchSize);
                if (process.batchSize < 100) {
                    out.print(" (ms)\t\t");
                } else {
                    out.print(" (ms)\t");
                }
                out.print(process.totalProgress);
                if (process.totalProgress < 100) {
                    out.print(" (ms)\t\t");
                } else {
                    out.print(" (ms)\t");
                }
                out.println(process.blockersCount + " times");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Completed.");
    }
}

