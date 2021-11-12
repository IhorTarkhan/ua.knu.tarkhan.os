package org.example.os.lab1;

import os.lab1.compfuncs.basic.IntOps;

import java.nio.channels.Pipe;
import java.util.Optional;

public class FileF extends Thread {
    private final Pipe pipe;

    public FileF(Pipe pipe) {
        this.pipe = pipe;
    }

    @Override
    public void run() {
        try {
            String input = PipeUtil.getData(this.pipe);
            Optional<Integer> result = IntOps.trialF(Integer.parseInt(input));
            if (result.isPresent()) {
                PipeUtil.fillPipe(this.pipe, result.get().toString());
            } else {
                PipeUtil.fillPipe(this.pipe, "NaN");
            }
        } catch (Exception e) {
            PipeUtil.fillPipe(this.pipe, "NaN");
        }
    }
}
