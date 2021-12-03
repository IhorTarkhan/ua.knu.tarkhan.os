package org.example.os.lab3;

import lombok.SneakyThrows;
import org.example.os.lab3.domain.Config;
import org.example.os.lab3.domain.Instruction;
import org.example.os.lab3.domain.Page;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

public class Kernel extends Thread {
    private final List<Page> memVector = new ArrayList<>();
    private final List<Instruction> instructions = new ArrayList<>();
    private final ControlPanel controlPanel;
    private int step;

    public Kernel(ControlPanel controlPanel, String config, String commands) {
        this.controlPanel = controlPanel;
        if (config != null) {
            configInit(config);
        }
        commandsInit(commands);
        step = 0;
        int map_count = 0;
        for (int i = 0; i < Config.virtPageNum; i++) {
            Page page = memVector.get(i);
            if (page.physical != -1) {
                map_count++;
            }
            int physical_count = 0;
            for (int j = 0; j < Config.virtPageNum; j++) {
                Page tmp_page = memVector.get(j);
                if (tmp_page.physical == page.physical && page.physical >= 0) {
                    physical_count++;
                }
            }
            if (physical_count > 1) {
                throw new RuntimeException("MemoryManagement: Duplicate physical page's in " + config);
            }
        }
        if (map_count < (Config.virtPageNum + 1) / 2) {
            for (int i = 0; i < Config.virtPageNum; i++) {
                Page page = memVector.get(i);
                if (page.physical == -1 && map_count < (Config.virtPageNum + 1) / 2) {
                    page.physical = i;
                    map_count++;
                }
            }
        }
        for (int i = 0; i < Config.virtPageNum; i++) {
            Page page = memVector.get(i);
            if (page.physical == -1) {
                controlPanel.removePhysicalPage(i);
            } else {
                controlPanel.addPhysicalPage(i, page.physical);
            }
        }
        for (Instruction instruction : instructions) {
            if (instruction.address() < 0 || instruction.address() > Config.getAddressLimit()) {
                throw new RuntimeException("MemoryManagement: Instruction (" + instruction.inst() + " " + instruction.address() + ") out of bounds.");
            }
        }
    }

    @SneakyThrows
    private void configInit(String config) {
        List<String> lines = Files.lines(Paths.get(config)).toList();

        long block = getLinesFrom("pagesize", lines)
                .findFirst()
                .map(value -> {
                    if (value[1].startsWith("power")) {
                        int power = Integer.parseInt(value[2]);
                        return (long) Math.pow(2, power);
                    } else {
                        return Long.parseLong(value[1], 10);
                    }

                }).orElse((long) Math.pow(2, 12));
        int virtPageNum = getLinesFrom("numpages", lines)
                .findFirst()
                .map(value -> Integer.parseInt(value[1]) - 1)
                .orElse(63);
        Optional<String[]> enableLogging = getLinesFrom("enable_logging", lines)
                .findFirst();
        boolean doStdoutLog = enableLogging.isPresent() && enableLogging.get()[1].startsWith("true");
        Optional<String[]> logFile = getLinesFrom("log_file", lines).findFirst();

        boolean doFileLog;
        String output;
        if (logFile.isPresent()) {
            if (logFile.get()[1].startsWith("log_file")) {
                doFileLog = false;
                output = "tracefile";
            } else {
                doFileLog = true;
                doStdoutLog = false;
                output = logFile.get()[1];
            }
        } else {
            doFileLog = false;
            doStdoutLog = false;
            output = null;
        }
        Optional<Byte> addressradixOptional = getLinesFrom("addressradix", lines)
                .findFirst()
                .map(value -> Byte.parseByte(value[1]));
        byte addressradix;
        if (addressradixOptional.isPresent()) {
            addressradix = addressradixOptional.get();
        } else {
            addressradix = 10;
        }
        Config.setBlock(block);
        Config.setVirtPageNum(virtPageNum);
        Config.doStdoutLog = doStdoutLog;
        Config.doFileLog = doFileLog;
        Config.output = output;
        Config.setAddressradix(addressradix);

        for (int i = 0; i <= Config.virtPageNum; i++) {
            memVector.add(new Page(i));
        }
        getLinesFrom("memset", lines)
                .forEach(values -> {
                    int id = Integer.parseInt(values[1]);
                    int physical = values[2].startsWith("x") ? -1 : Integer.parseInt(values[2].trim());
                    byte R = Byte.parseByte(values[3]);
                    byte M = Byte.parseByte(values[4]);
                    int inMemTime = Integer.parseInt(values[5]);
                    int lastTouchTime = Integer.parseInt(values[6]);
                    memVector.set(id, new Page(id, physical, R, M, inMemTime, lastTouchTime));
                });
    }

    @SneakyThrows
    private void commandsInit(String commands) {
        Files.lines(Paths.get(commands))
                .filter(line -> line.startsWith("READ") || line.startsWith("WRITE"))
                .map(line -> line.split("[ \t\n\r\f]"))
                .forEach(value -> {
                    String command = value[0].startsWith("READ") ? "READ" : "WRITE";
                    if (value[1].startsWith("random")) {
                        instructions.add(new Instruction(command, new Random(System.currentTimeMillis()).nextLong(0, Config.getAddressLimit())));
                    } else {
                        long address;
                        if (value[1].startsWith("bin")) {
                            address = Long.parseLong(value[2], 2);
                        } else if (value[1].startsWith("oct")) {
                            address = Long.parseLong(value[2], 8);
                        } else if (value[1].startsWith("hex")) {
                            address = Long.parseLong(value[2], 16);
                        } else {
                            address = Long.parseLong(value[1]);
                        }
                        if (0 > address || address > Config.getAddressLimit()) {
                            throw new RuntimeException("MemoryManagement: " + address + ", Address out of range in " + commands);
                        }
                        instructions.add(new Instruction(command, address));
                    }
                });
        if (instructions.size() < 1) {
            throw new RuntimeException("MemoryManagement: no instructions present for execution.");
        }
    }

    private Stream<String[]> getLinesFrom(String key, List<String> lines) {
        return lines.stream()
                .filter(line -> line.startsWith(key))
                .map(line -> line.split("[ \t\n\r\f]"));
    }

    public Page getPage(int index) {
        return memVector.get(index);
    }

    public boolean isRunFinished() {
        return step == instructions.size();
    }

    @SneakyThrows
    private void printLogFile(String message) {
        if (!new File(Config.output).exists()) {
            new File(Config.output).createNewFile();
        }
        Files.write(Paths.get(Config.output), (message + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
    }

    @SneakyThrows
    public void run() {
        do {
            Thread.sleep(20);
            step();
        } while (step != instructions.size());
    }

    public void step() {
        Instruction instruct = instructions.get(step);
        controlPanel.instructionValueLabel.setText(instruct.inst());
        controlPanel.addressValueLabel.setText(Long.toString(instruct.address(), Config.addressradix));
        controlPanel.paintPage(memVector.get(Virtual2Physical.pageNum(instruct.address(), Config.virtPageNum, Config.block)));
        if ("YES".equals(controlPanel.pageFaultValueLabel.getText())) {
            controlPanel.pageFaultValueLabel.setText("NO");
        }
        if (instruct.inst().startsWith("READ")) {
            Page page = memVector.get(Virtual2Physical.pageNum(instruct.address(), Config.virtPageNum, Config.block));
            if (page.physical == -1) {
                if (Config.doFileLog) {
                    printLogFile("READ " + Long.toString(instruct.address(), Config.addressradix) + " ... page fault");
                }
                if (Config.doStdoutLog) {
                    System.out.println("READ " + Long.toString(instruct.address(), Config.addressradix) + " ... page fault");
                }
                PageFault.replacePage(memVector, Config.virtPageNum, Virtual2Physical.pageNum(instruct.address(), Config.virtPageNum, Config.block), controlPanel);
                controlPanel.pageFaultValueLabel.setText("YES");
            } else {
                page.R = 1;
                page.lastTouchTime = 0;
                if (Config.doFileLog) {
                    printLogFile("READ " + Long.toString(instruct.address(), Config.addressradix) + " ... okay");
                }
                if (Config.doStdoutLog) {
                    System.out.println("READ " + Long.toString(instruct.address(), Config.addressradix) + " ... okay");
                }
            }
        }
        if (instruct.inst().startsWith("WRITE")) {
            Page page = memVector.get(Virtual2Physical.pageNum(instruct.address(), Config.virtPageNum, Config.block));
            if (page.physical == -1) {
                if (Config.doFileLog) {
                    printLogFile("WRITE " + Long.toString(instruct.address(), Config.addressradix) + " ... page fault");
                }
                if (Config.doStdoutLog) {
                    System.out.println("WRITE " + Long.toString(instruct.address(), Config.addressradix) + " ... page fault");
                }
                PageFault.replacePage(memVector, Config.virtPageNum, Virtual2Physical.pageNum(instruct.address(), Config.virtPageNum, Config.block), controlPanel);
                controlPanel.pageFaultValueLabel.setText("YES");
            } else {
                page.M = 1;
                page.lastTouchTime = 0;
                if (Config.doFileLog) {
                    printLogFile("WRITE " + Long.toString(instruct.address(), Config.addressradix) + " ... okay");
                }
                if (Config.doStdoutLog) {
                    System.out.println("WRITE " + Long.toString(instruct.address(), Config.addressradix) + " ... okay");
                }
            }
        }
        for (int i = 0; i < Config.virtPageNum; i++) {
            Page page = memVector.get(i);
            if (page.R == 1 && page.lastTouchTime == 10) {
                page.R = 0;
            }
            if (page.physical != -1) {
                page.inMemTime = page.inMemTime + 10;
                page.lastTouchTime = page.lastTouchTime + 10;
            }
        }
        step++;
        controlPanel.timeValueLabel.setText(step * 10 + " (ns)");
    }
}
