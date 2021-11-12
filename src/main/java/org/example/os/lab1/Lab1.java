package org.example.os.lab1;

import java.io.IOException;
import java.nio.channels.Pipe;
import java.util.Scanner;

public class Lab1 {
    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter X: ");
        int x = scanner.nextInt();
        System.out.print("Enter MAX time to wait calculation in millis: ");
        int maxTimeToSleep = scanner.nextInt();

        Pipe pipeF = Pipe.open();
        Pipe pipeG = Pipe.open();

        PipeUtil.fillPipe(pipeF, Integer.toString(x));
        PipeUtil.fillPipe(pipeG, Integer.toString(x));

        Thread fileThreadF = new FileF(pipeF);
        Thread fileThreadG = new FileG(pipeG);

        fileThreadF.start();
        fileThreadG.start();

        for (int i = 0; i < maxTimeToSleep / 100; i++) {
            if (!fileThreadF.isAlive() && !fileThreadG.isAlive()) {
                break;
            } else {
                Thread.sleep(100);
            }
        }

        fileThreadF.interrupt();
        fileThreadG.interrupt();

        String responseFromF = PipeUtil.getData(pipeF);
        String responseFromG = PipeUtil.getData(pipeG);

        try {
            int fResult = Integer.parseInt(responseFromF);
            int gResult = Integer.parseInt(responseFromG);
            System.out.println("Result = " + (fResult + gResult));
        } catch (Exception e) {
            System.err.println("Failed");
            System.err.println("F result: " + responseFromF);
            System.err.println("G result: " + responseFromG);
        }
    }
}
