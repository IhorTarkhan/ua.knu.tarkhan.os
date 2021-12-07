package org.example.os.lab3;

import lombok.SneakyThrows;
import org.example.os.lab3.domain.Config;
import org.example.os.lab3.domain.Instruction;
import org.example.os.lab3.domain.Page;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Stream;

public class Kernel extends Thread {
    private final List<Page> memVector = new ArrayList<>();
    private final List<Instruction> instructions = new ArrayList<>();
    private final ControlPanel controlPanel;
    private final Config config;
    private final int tau;
    private final int schedules;
    private final IOSystem io;
    private int step = 0;

    @SneakyThrows
    public Kernel(ControlPanel controlPanel, String configFile, String commands) {
        this.controlPanel = controlPanel;
        this.controlPanel.reset();
        this.config = configInit(configFile);
        commandsInit(commands);

        List<String> lines = Files.lines(Paths.get(configFile)).toList();
        tau = getLinesFrom("tau", lines)
                .findFirst()
                .map(value -> {
                    int result = Integer.parseInt(value[1]);
                    if (result < 10 || result > 10000) {
                        throw new RuntimeException("MemoryManagement: tau out of bounds.");
                    }
                    return result;
                }).orElse(100);
        schedules = getLinesFrom("schedules", lines)
                .findFirst()
                .map(value -> {
                    int result = Integer.parseInt(value[1]);
                    if (result < 2 || result > 16) {
                        throw new RuntimeException("MemoryManagement: schedules out of bounds.");
                    }
                    return result;
                }).orElse(3);
        io = getLinesFrom("clockticks", lines)
                .findFirst()
                .map(value -> {
                    int result = Integer.parseInt(value[1]);
                    if (result < 2 || result > 100) {
                        throw new RuntimeException("MemoryManagement: clockticks out of bounds.");
                    }
                    return new IOSystem(schedules, result);
                }).orElse(null);
        int map_count = 0;
        for (int i = 0; i < config.getVirtualPageNum(); i++) {
            Page page = memVector.get(i);
            if (page.physical != -1) {
                map_count++;
            }
            int physical_count = 0;
            for (int j = 0; j < config.getVirtualPageNum(); j++) {
                Page tmp_page = memVector.get(j);
                if (tmp_page.physical == page.physical && page.physical >= 0) {
                    physical_count++;
                }
            }
            if (physical_count > 1) {
                throw new RuntimeException("MemoryManagement: Duplicate physical page's in " + config);
            }
        }
        if (map_count < (config.getVirtualPageNum() + 1) / 2) {
            for (int i = 0; i < config.getVirtualPageNum(); i++) {
                Page page = memVector.get(i);
                if (page.physical == -1 && map_count < (config.getVirtualPageNum() + 1) / 2) {
                    page.physical = i;
                    map_count++;
                }
            }
        }
        for (int i = 0; i < config.getVirtualPageNum(); i++) {
            Page page = memVector.get(i);
            if (page.physical == -1) {
                removePhysicalPage(i);
            } else {
                addPhysicalPage(i, page.physical);
            }
        }
        for (Instruction instruction : instructions) {
            if (instruction.address() < 0 || instruction.address() > config.getAddressLimit()) {
                throw new RuntimeException("MemoryManagement: Instruction (" + instruction.inst() + " " + instruction.address() + ") out of bounds.");
            }
        }

        PageFault.wsclock = new WSClock();
        PageFault.wsclock.init(memVector);
    }

    @SneakyThrows
    private Config configInit(String configFile) {
        if (configFile == null) {
            return new Config();
        }

        List<String> lines = Files.lines(Paths.get(configFile)).toList();
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
        int virtualPageNum = getLinesFrom("numpages", lines)
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
        byte addressRadix;
        if (addressradixOptional.isPresent()) {
            addressRadix = addressradixOptional.get();
        } else {
            addressRadix = 10;
        }
        for (int i = 0; i <= virtualPageNum; i++) {
            long high = (block * (i + 1)) - 1;
            long low = block * i;
            memVector.add(new Page(i, high, low));
        }
        getLinesFrom("memset", lines)
                .forEach(values -> {
                    int id = Integer.parseInt(values[1]);
                    int physical = values[2].startsWith("x") ? -1 : Integer.parseInt(values[2].trim());
                    byte R = Byte.parseByte(values[3]);
                    byte M = Byte.parseByte(values[4]);
                    int inMemTime = Integer.parseInt(values[5]);
                    int lastTouchTime = Integer.parseInt(values[6]);
                    long high = (block * (id + 1)) - 1;
                    long low = block * id;
                    memVector.set(id, new Page(id, physical, R, M, inMemTime, lastTouchTime, high, low));
                });
        return new Config(virtualPageNum, block, output, addressRadix, doStdoutLog, doFileLog);
    }

    @SneakyThrows
    private void commandsInit(String commands) {
        Files.lines(Paths.get(commands))
                .filter(line -> line.startsWith("READ") || line.startsWith("WRITE"))
                .map(line -> line.split("[ \t\n\r\f]"))
                .forEach(value -> {
                    String command = value[0].startsWith("READ") ? "READ" : "WRITE";
                    if (value[1].startsWith("random")) {
                        instructions.add(new Instruction(command, new Random(System.currentTimeMillis()).nextLong(0, config.getAddressLimit())));
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
                        if (0 > address || address > config.getAddressLimit()) {
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

    public byte getAddressRadix() {
        return config.getAddressRadix();
    }

    public boolean isRunFinished() {
        return step == instructions.size();
    }

    public void paintPage(Page page) {
        controlPanel.virtualPageValueLabel.setText(Integer.toString(page.id));
        controlPanel.physicalPageValueLabel.setText(Integer.toString(page.physical));
        controlPanel.RValueLabel.setText(Integer.toString(page.R));
        controlPanel.MValueLabel.setText(Integer.toString(page.M));
        controlPanel.inMemTimeValueLabel.setText(Integer.toString(page.inMemTime));
        controlPanel.lastTouchTimeValueLabel.setText(Integer.toString(page.lastTouchTime));
        controlPanel.lowValueLabel.setText(Long.toString(page.low, getAddressRadix()));
        controlPanel.highValueLabel.setText(Long.toString(page.high, getAddressRadix()));
    }

    public void setStatus(String status) {
        controlPanel.statusValueLabel.setText(status);
    }

    public void addPhysicalPage(int pageNum, int physicalPage) {
        controlPanel.labels.get(physicalPage).setText("page " + pageNum);
    }

    public void removePhysicalPage(int physicalPage) {
        controlPanel.labels.get(physicalPage).setText(null);
    }


    @SneakyThrows
    private void printLogFile(String message) {
        if (!new File(config.getOutput()).exists()) {
            boolean newFile = new File(config.getOutput()).createNewFile();
            System.out.println(newFile ? "Created new log file " + config.getOutput() : "can not create new log file " + config.getOutput());
        }
        Files.write(Paths.get(config.getOutput()), (message + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
    }

    public void run() {
        setStatus("RUN");
        controlPanel.runButton.setEnabled(false);
        controlPanel.stepButton.setEnabled(false);
        controlPanel.resetButton.setEnabled(false);
        do {
            step(true);
        } while (step != instructions.size());
        setStatus("STOP");
        controlPanel.resetButton.setEnabled(true);
    }

    @SneakyThrows
    public void step(boolean isRun) {
        setStatus(isRun ? "RUN" : "STEP");
        Thread.sleep(100);
        Instruction instruct = instructions.get(step);
        controlPanel.instructionValueLabel.setText(instruct.inst());
        controlPanel.addressValueLabel.setText(Long.toString(instruct.address(), config.getAddressRadix()));
        paintPage(memVector.get(Virtual2Physical.pageNum(instruct.address(), config.getVirtualPageNum(), config.getBlock())));
        if ("YES".equals(controlPanel.pageFaultValueLabel.getText())) {
            controlPanel.pageFaultValueLabel.setText("NO");
        }
        if (instruct.inst().startsWith("READ")) {
            Page page = memVector.get(Virtual2Physical.pageNum(instruct.address(), config.getVirtualPageNum(), config.getBlock()));
            if (page.physical == -1) {
                if (config.isDoFileLog()) {
                    printLogFile("READ " + Long.toString(instruct.address(), config.getAddressRadix()) + " ... page fault");
                }
                if (config.isDoStdoutLog()) {
                    System.out.println("READ " + Long.toString(instruct.address(), config.getAddressRadix()) + " ... page fault");
                }
                PageFault.replacePage(memVector, config.getVirtualPageNum(), Virtual2Physical.pageNum(instruct.address(), config.getVirtualPageNum(), config.getBlock()), this, tau, io);
                controlPanel.pageFaultValueLabel.setText("YES");
            } else {
                page.R = 1;
                page.lastTouchTime = 0;
                if (config.isDoFileLog()) {
                    printLogFile("READ " + Long.toString(instruct.address(), config.getAddressRadix()) + " ... okay");
                }
                if (config.isDoStdoutLog()) {
                    System.out.println("READ " + Long.toString(instruct.address(), config.getAddressRadix()) + " ... okay");
                }
            }
        }
        if (instruct.inst().startsWith("WRITE")) {
            Page page = memVector.get(Virtual2Physical.pageNum(instruct.address(), config.getVirtualPageNum(), config.getBlock()));
            if (page.physical == -1) {
                if (config.isDoFileLog()) {
                    printLogFile("WRITE " + Long.toString(instruct.address(), config.getAddressRadix()) + " ... page fault");
                }
                if (config.isDoStdoutLog()) {
                    System.out.println("WRITE " + Long.toString(instruct.address(), config.getAddressRadix()) + " ... page fault");
                }
                PageFault.replacePage(memVector, config.getVirtualPageNum(), Virtual2Physical.pageNum(instruct.address(), config.getVirtualPageNum(), config.getBlock()), this, tau, io);
                controlPanel.pageFaultValueLabel.setText("YES");
            } else {
                page.M = 1;
                page.lastTouchTime = 0;
                if (config.isDoFileLog()) {
                    printLogFile("WRITE " + Long.toString(instruct.address(), config.getAddressRadix()) + " ... okay");
                }
                if (config.isDoStdoutLog()) {
                    System.out.println("WRITE " + Long.toString(instruct.address(), config.getAddressRadix()) + " ... okay");
                }
            }
        }
        for (int i = 0; i < config.getVirtualPageNum(); i++) {
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
        io.writeAll();


        if (isRunFinished()) {
            controlPanel.stepButton.setEnabled(false);
            controlPanel.runButton.setEnabled(false);
        }
        setStatus("STOP");
    }
}
