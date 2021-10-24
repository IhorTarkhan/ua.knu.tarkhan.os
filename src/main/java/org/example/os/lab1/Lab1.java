package org.example.os.lab1;

import java.io.IOException;
import java.nio.channels.Pipe;

public class Lab1 {
    public static void main(String[] args) throws IOException, InterruptedException {
        Pipe pipeF = Pipe.open();
        Pipe pipeG = Pipe.open();

        PipeUtil.fillPipe(pipeF, "Test Data to file F");
        PipeUtil.fillPipe(pipeG, "Test Data to file G");

        Thread fileThreadF = new FileF(pipeF);
        Thread fileThreadG = new FileG(pipeG);

        fileThreadF.start();
        fileThreadG.start();

        fileThreadF.join();
        fileThreadG.join();

        String responseFromF = PipeUtil.getData(pipeF);
        String responseFromG = PipeUtil.getData(pipeG);

        System.out.println(responseFromF);
        System.out.println(responseFromG);
    }
}
