package org.example.os;

import java.nio.channels.Pipe;

public class FileG extends Thread {
    private final Pipe pipe;

    public FileG(Pipe pipe) {
        this.pipe = pipe;
    }

    @Override
    public void run() {
        String input = PipeUtil.getData(this.pipe);
        System.out.println("File G receive " + input);
        PipeUtil.fillPipe(this.pipe, "File G send response");
    }
}
