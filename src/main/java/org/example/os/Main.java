package org.example.os;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Pipe pipeF = getPipe("Test Data to file F");
        Pipe pipeG = getPipe("Test Data to file G");

        FileF fileF = new FileF(pipeF);
        FileG fileG = new FileG(pipeG);

        fileF.start();
        fileG.start();

        fileF.join();
        fileG.join();
    }

    private static Pipe getPipe(String data) {
        try {
            Pipe pipe = Pipe.open();

            Pipe.SinkChannel pipeSinkChannel = pipe.sink();
            ByteBuffer bufferWrite = ByteBuffer.allocate(512);
            bufferWrite.clear();
            bufferWrite.put(data.getBytes());
            bufferWrite.flip();
            while (bufferWrite.hasRemaining()) {
                pipeSinkChannel.write(bufferWrite);
            }
            return pipe;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
