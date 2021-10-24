package org.example.os;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;

public class FileG extends Thread {
    private final Pipe pipe;

    public FileG(Pipe pipe) {
        this.pipe = pipe;
    }

    @Override
    public void run() {
        try {
            Pipe.SourceChannel pipeSourceChannel = pipe.source();
            ByteBuffer bufferRead = ByteBuffer.allocate(512);

            pipeSourceChannel.read(bufferRead);

            bufferRead.flip();
            while (bufferRead.hasRemaining()) {
                char ch = (char) bufferRead.get();
                System.out.print(ch);
            }
            System.out.println();
            bufferRead.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
