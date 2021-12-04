package org.example.os.lab3;

import java.io.File;

public class MemoryManagement {
    public static void main(String[] args) {
        if (args.length != 1 && args.length != 2) {
            throw new RuntimeException("Usage: 'java MemoryManagement <COMMAND FILE> <PROPERTIES FILE>'");
        }

        validateFile(args[0]);
        if (args.length == 2) {
            validateFile(args[1]);
            new ControlPanel("Memory Management", args[0], args[1]);
        } else {
            new ControlPanel("Memory Management", args[0], null);
        }
    }

    private static void validateFile(String fileSrc) {
        File file = new File(fileSrc);
        if (!(file.exists())) {
            throw new RuntimeException("MemoryM: error, file '" + file.getName() + "' does not exist.");
        }
        if (!(file.canRead())) {
            throw new RuntimeException("MemoryM: error, read of " + file.getName() + " failed.");
        }
    }
}
