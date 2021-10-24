package org.example.os;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;

public class PipeUtil {
    public static void fillPipe(Pipe pipe, String data) {
        try {
            Pipe.SinkChannel pipeSinkChannel = pipe.sink();
            ByteBuffer bufferWrite = ByteBuffer.allocate(512);
            bufferWrite.clear();
            bufferWrite.put(data.getBytes());
            bufferWrite.flip();
            while (bufferWrite.hasRemaining()) {
                pipeSinkChannel.write(bufferWrite);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
