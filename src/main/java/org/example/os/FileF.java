package org.example.os;

import java.nio.channels.Pipe;

public class FileF extends Thread {
    private final Pipe pipe;

    public FileF(Pipe pipe) {
        this.pipe = pipe;
    }

    @Override
    public void run() {
        String input = PipeUtil.getData(this.pipe);
        System.out.println("File F receive " + input);
        PipeUtil.fillPipe(this.pipe, "File F send response");
    }
}
