package org.example.os;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;

public class FileF extends Thread {
    private final Pipe pipe;

    public FileF(Pipe pipe) {
        this.pipe = pipe;
    }

    @Override
    public void run() {
        String result = getInput(this.pipe);
        System.out.println(result);
    }

    private String getInput(Pipe pipe) {
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
