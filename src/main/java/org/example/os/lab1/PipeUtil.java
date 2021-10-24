package org.example.os.lab1;

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

    public static String getData(Pipe pipe) {
        try {
            Pipe.SourceChannel pipeSourceChannel = pipe.source();
            ByteBuffer bufferRead = ByteBuffer.allocate(512);

            pipeSourceChannel.read(bufferRead);

            bufferRead.flip();
            StringBuilder resultBuilder = new StringBuilder();
            while (bufferRead.hasRemaining()) {
                char ch = (char) bufferRead.get();
                resultBuilder.append(ch);
            }
            bufferRead.clear();
            return resultBuilder.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
