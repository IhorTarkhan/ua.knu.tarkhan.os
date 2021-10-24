package org.example.os;

import java.io.IOException;
import java.nio.channels.Pipe;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Pipe pipeF = Pipe.open();
        Pipe pipeG = Pipe.open();

        PipeUtil.fillPipe(pipeF, "Test Data to file F");
        PipeUtil.fillPipe(pipeG, "Test Data to file G");

        FileF fileF = new FileF(pipeF);
        FileG fileG = new FileG(pipeG);

        fileF.start();
        fileG.start();

        fileF.join();
        fileG.join();
    }
}
