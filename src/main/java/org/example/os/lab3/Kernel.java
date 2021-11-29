package org.example.os.lab3;

import lombok.SneakyThrows;
import org.example.os.lab3.domain.Config;
import org.example.os.lab3.domain.Instruction;
import org.example.os.lab3.domain.Page;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Kernel extends Thread {
    public static final String UNIVERSAL_DELIMITERS = "[ \t\n\r\f]";
    ///
    private final List<Page> memVector = new ArrayList<>();
    private final List<Instruction> instructVector = new ArrayList<>();
    public int runs;
    public int runcycles;
    ///
    private String command_file;
    private String config_file;
    ///
    private ControlPanel controlPanel;

    @SneakyThrows
    public void init(String commands, String config) {
        command_file = commands;
        config_file = config;

        if (config != null) {
            List<String> lines = Files.lines(Paths.get(config)).collect(Collectors.toList());

            getLinesFrom("pagesize", lines)
                    .findFirst()
                    .ifPresent(value -> {
                        String tmp = value[1];
                        if (tmp.startsWith("power")) {
                            int power = Integer.parseInt(value[2]);
                            Config.setBlock((int) Math.pow(2, power));
                        } else {
                            Config.setBlock(Long.parseLong(tmp, 10));
                        }

                    });

            getLinesFrom("numpages", lines)
                    .findFirst()
                    .ifPresent(value -> Config.setVirtPageNum(Integer.parseInt(value[1]) - 1));

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
            getLinesFrom("enable_logging", lines)
                    .findFirst()
                    .ifPresent(value -> {
                        if (value[1].startsWith("true")) {
                            Config.doStdoutLog = true;
                        }
                    });
            getLinesFrom("log_file", lines)
                    .findFirst()
                    .ifPresent(value -> {
                        if (value[1].startsWith("log_file")) {
                            Config.doFileLog = false;
                            Config.output = "tracefile";
                        } else {
                            Config.doFileLog = true;
                            Config.doStdoutLog = false;
                            Config.output = value[1];
                        }
                    });
            getLinesFrom("addressradix", lines)
                    .findFirst()
                    .ifPresent(value -> Config.setAddressradix(Byte.parseByte(value[1])));
        }
        try {
            File f = new File(commands);
            DataInputStream in = new DataInputStream(new FileInputStream(f));
            String line;

            while ((line = in.readLine()) != null) {
                if (line.startsWith("READ") || line.startsWith("WRITE")) {
                    String command = "";
                    if (line.startsWith("READ")) {
                        command = "READ";
                    }
                    if (line.startsWith("WRITE")) {
                        command = "WRITE";
                    }
                    StringTokenizer st = new StringTokenizer(line);
                    st.nextToken();
                    String tmp = st.nextToken();
                    if (tmp.startsWith("random")) {
                        instructVector.add(new Instruction(command, new Random(System.currentTimeMillis()).nextLong(0, Config.getAddressLimit())));
                    } else {
                        long addr;
                        if (tmp.startsWith("bin")) {
                            addr = Long.parseLong(st.nextToken(), 2);
                        } else if (tmp.startsWith("oct")) {
                            addr = Long.parseLong(st.nextToken(), 8);
                        } else if (tmp.startsWith("hex")) {
                            addr = Long.parseLong(st.nextToken(), 16);
                        } else {
                            addr = Long.parseLong(tmp);
                        }
                        if (0 > addr || addr > Config.getAddressLimit()) {
                            throw new RuntimeException("MemoryManagement: " + addr + ", Address out of range in " + commands);
                        }
                        instructVector.add(new Instruction(command, addr));
                    }
                }
            }
            in.close();
        } catch (IOException e) { /* Handle exceptions */ }
        runcycles = instructVector.size();
        if (runcycles < 1) {
            throw new RuntimeException("MemoryManagement: no instructions present for execution.");
        }
        if (Config.doFileLog) {
            File trace = new File(Config.output);
            trace.delete();
        }
        runs = 0;
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
        for (Instruction instruction : instructVector) {
            if (instruction.addr < 0 || instruction.addr > Config.getAddressLimit()) {
                throw new RuntimeException("MemoryManagement: Instruction (" + instruction.inst + " " + instruction.addr + ") out of bounds.");
            }
        }
    }

    private Stream<String[]> getLinesFrom(String key, List<String> lines) {
        return lines.stream()
                .filter(line -> line.startsWith(key))
                .map(line -> line.split(UNIVERSAL_DELIMITERS));
    }

    public void setControlPanel(ControlPanel newControlPanel) {
        controlPanel = newControlPanel;
    }

    public void getPage(int pageNum) {
        Page page = memVector.get(pageNum);
        controlPanel.paintPage(page);
    }

    private void printLogFile(String message) {
        String line;
        StringBuilder temp = new StringBuilder();

        File trace = new File(Config.output);
        if (trace.exists()) {
            try {
                DataInputStream in = new DataInputStream(new FileInputStream(Config.output));
                while ((line = in.readLine()) != null) {
                    temp.append(line).append(System.lineSeparator());
                }
                in.close();
            } catch (IOException e) {
                /* Do nothing */
            }
        }
        try {
            PrintStream out = new PrintStream(new FileOutputStream(Config.output));
            out.print(temp);
            out.print(message);
            out.close();
        } catch (IOException e) {
            /* Do nothing */
        }
    }

    @SneakyThrows
    public void run() {
        do {
            Thread.sleep(20);
            step();
        } while (runs != runcycles);
    }

    public void step() {
        Instruction instruct = instructVector.get(runs);
        controlPanel.instructionValueLabel.setText(instruct.inst);
        controlPanel.addressValueLabel.setText(Long.toString(instruct.addr, Config.addressradix));
        getPage(Virtual2Physical.pageNum(instruct.addr, Config.virtPageNum, Config.block));
        if ("YES".equals(controlPanel.pageFaultValueLabel.getText())) {
            controlPanel.pageFaultValueLabel.setText("NO");
        }
        if (instruct.inst.startsWith("READ")) {
            Page page = memVector.get(Virtual2Physical.pageNum(instruct.addr, Config.virtPageNum, Config.block));
            if (page.physical == -1) {
                if (Config.doFileLog) {
                    printLogFile("READ " + Long.toString(instruct.addr, Config.addressradix) + " ... page fault");
                }
                if (Config.doStdoutLog) {
                    System.out.println("READ " + Long.toString(instruct.addr, Config.addressradix) + " ... page fault");
                }
                PageFault.replacePage(memVector, Config.virtPageNum, Virtual2Physical.pageNum(instruct.addr, Config.virtPageNum, Config.block), controlPanel);
                controlPanel.pageFaultValueLabel.setText("YES");
            } else {
                page.R = 1;
                page.lastTouchTime = 0;
                if (Config.doFileLog) {
                    printLogFile("READ " + Long.toString(instruct.addr, Config.addressradix) + " ... okay");
                }
                if (Config.doStdoutLog) {
                    System.out.println("READ " + Long.toString(instruct.addr, Config.addressradix) + " ... okay");
                }
            }
        }
        if (instruct.inst.startsWith("WRITE")) {
            Page page = memVector.get(Virtual2Physical.pageNum(instruct.addr, Config.virtPageNum, Config.block));
            if (page.physical == -1) {
                if (Config.doFileLog) {
                    printLogFile("WRITE " + Long.toString(instruct.addr, Config.addressradix) + " ... page fault");
                }
                if (Config.doStdoutLog) {
                    System.out.println("WRITE " + Long.toString(instruct.addr, Config.addressradix) + " ... page fault");
                }
                PageFault.replacePage(memVector, Config.virtPageNum, Virtual2Physical.pageNum(instruct.addr, Config.virtPageNum, Config.block), controlPanel);
                controlPanel.pageFaultValueLabel.setText("YES");
            } else {
                page.M = 1;
                page.lastTouchTime = 0;
                if (Config.doFileLog) {
                    printLogFile("WRITE " + Long.toString(instruct.addr, Config.addressradix) + " ... okay");
                }
                if (Config.doStdoutLog) {
                    System.out.println("WRITE " + Long.toString(instruct.addr, Config.addressradix) + " ... okay");
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
        runs++;
        controlPanel.timeValueLabel.setText(runs * 10 + " (ns)");
    }

    public void reset() {
        memVector.clear();
        instructVector.clear();
        controlPanel.statusValueLabel.setText("STOP");
        controlPanel.timeValueLabel.setText("0");
        controlPanel.instructionValueLabel.setText("NONE");
        controlPanel.addressValueLabel.setText("NULL");
        controlPanel.pageFaultValueLabel.setText("NO");
        controlPanel.virtualPageValueLabel.setText("x");
        controlPanel.physicalPageValueLabel.setText("0");
        controlPanel.RValueLabel.setText("0");
        controlPanel.MValueLabel.setText("0");
        controlPanel.inMemTimeValueLabel.setText("0");
        controlPanel.lastTouchTimeValueLabel.setText("0");
        controlPanel.lowValueLabel.setText("0");
        controlPanel.highValueLabel.setText("0");
        init(command_file, config_file);
    }
}
