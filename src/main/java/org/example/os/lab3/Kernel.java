package org.example.os.lab3;

import lombok.SneakyThrows;
import org.example.os.lab3.domain.Instruction;
import org.example.os.lab3.domain.Page;

import java.io.*;
import java.util.*;

public class Kernel extends Thread {
    public static byte addressradix = 10;
    private static int virtPageNum = 63;
    private final List<Page> memVector = new ArrayList<>();
    private final List<Instruction> instructVector = new ArrayList<>();
    public int runs;
    public int runcycles;
    public long block = (int) Math.pow(2, 12);
    private String output = null;
    private String command_file;
    private String config_file;
    private ControlPanel controlPanel;
    private boolean doStdoutLog = false;
    private boolean doFileLog = false;

    public void init(String commands, String config) {
        command_file = commands;
        config_file = config;
        String tmp = null;
        int physical;
        int physical_count = 0;
        long address_limit = (block * virtPageNum + 1) - 1;

        if (config != null) {
            File f = new File(config);
            try {
                DataInputStream in = new DataInputStream(new FileInputStream(f));
                String line;

                while ((line = in.readLine()) != null) {
                    if (line.startsWith("numpages")) {
                        StringTokenizer st = new StringTokenizer(line);
                        while (st.hasMoreTokens()) {
                            tmp = st.nextToken();
                            virtPageNum = Integer.parseInt(st.nextToken().trim()) - 1;
                            if (virtPageNum < 2 || virtPageNum > 63) {
                                throw new RuntimeException("MemoryManagement: numpages out of bounds.");
                            }
                            address_limit = (block * virtPageNum + 1) - 1;
                        }
                    }
                }
                in.close();
            } catch (IOException e) { /* Handle exceptions */ }
            for (int i = 0; i <= virtPageNum; i++) {
                long high = (block * (i + 1)) - 1;
                long low = block * i;
                memVector.add(new Page(i, -1, (byte) 0, (byte) 0, 0, 0, high, low));
            }
            try {
                DataInputStream in = new DataInputStream(new FileInputStream(f));
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("memset")) {
                        StringTokenizer st = new StringTokenizer(line);
                        st.nextToken();
                        while (st.hasMoreTokens()) {
                            int id = Integer.parseInt(st.nextToken().trim());
                            tmp = st.nextToken();
                            if (tmp.startsWith("x")) {
                                physical = -1;
                            } else {
                                physical = Integer.parseInt(tmp.trim());
                            }
                            if ((0 > id || id > virtPageNum) || (-1 > physical || physical > ((virtPageNum - 1) / 2))) {
                                throw new RuntimeException("MemoryManagement: Invalid page value in " + config);
                            }
                            byte R = Byte.parseByte(st.nextToken().trim());
                            if (R < 0 || R > 1) {
                                throw new RuntimeException("MemoryManagement: Invalid R value in " + config);
                            }
                            byte M = Byte.parseByte(st.nextToken().trim());
                            if (M < 0 || M > 1) {
                                throw new RuntimeException("MemoryManagement: Invalid M value in " + config);
                            }
                            int inMemTime = Integer.parseInt(st.nextToken().trim());
                            if (inMemTime < 0) {
                                throw new RuntimeException("MemoryManagement: Invalid inMemTime in " + config);
                            }
                            int lastTouchTime = Integer.parseInt(st.nextToken().trim());
                            if (lastTouchTime < 0) {
                                throw new RuntimeException("MemoryManagement: Invalid lastTouchTime in " + config);
                            }
                            Page page = memVector.get(id);
                            page.physical = physical;
                            page.R = R;
                            page.M = M;
                            page.inMemTime = inMemTime;
                            page.lastTouchTime = lastTouchTime;
                        }
                    }
                    if (line.startsWith("enable_logging")) {
                        StringTokenizer st = new StringTokenizer(line);
                        while (st.hasMoreTokens()) {
                            if (st.nextToken().startsWith("true")) {
                                doStdoutLog = true;
                            }
                        }
                    }
                    if (line.startsWith("log_file")) {
                        StringTokenizer st = new StringTokenizer(line);
                        while (st.hasMoreTokens()) {
                            tmp = st.nextToken();
                        }
                        if (Objects.requireNonNull(tmp).startsWith("log_file")) {
                            doFileLog = false;
                            output = "tracefile";
                        } else {
                            doFileLog = true;
                            doStdoutLog = false;
                            output = tmp;
                        }
                    }
                    if (line.startsWith("pagesize")) {
                        StringTokenizer st = new StringTokenizer(line);
                        while (st.hasMoreTokens()) {
                            st.nextToken();
                            tmp = st.nextToken();
                            if (tmp.startsWith("power")) {
                                int power = Integer.parseInt(st.nextToken());
                                block = (int) Math.pow(2, power);
                            } else {
                                block = Long.parseLong(tmp, 10);
                            }
                            address_limit = (block * virtPageNum + 1) - 1;
                        }
                        if (block < 64 || block > Math.pow(2, 26)) {
                            throw new RuntimeException("MemoryManagement: pagesize is out of bounds");
                        }
                        for (int i = 0; i <= virtPageNum; i++) {
                            Page page = memVector.get(i);
                            page.high = (block * (i + 1)) - 1;
                            page.low = block * i;
                        }
                    }
                    if (line.startsWith("addressradix")) {
                        StringTokenizer st = new StringTokenizer(line);
                        while (st.hasMoreTokens()) {
                            st.nextToken();
                            tmp = st.nextToken();
                            addressradix = Byte.parseByte(tmp);
                            if (addressradix < 0 || addressradix > 20) {
                                throw new RuntimeException("MemoryManagement: addressradix out of bounds.");
                            }
                        }
                    }
                }
                in.close();
            } catch (IOException e) { /* Handle exceptions */ }
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
                    tmp = st.nextToken();
                    if (tmp.startsWith("random")) {
                        instructVector.add(new Instruction(command, new Random(System.currentTimeMillis()).nextLong(0, address_limit)));
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
                        if (0 > addr || addr > address_limit) {
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
        if (doFileLog) {
            File trace = new File(output);
            trace.delete();
        }
        runs = 0;
        int map_count = 0;
        for (int i = 0; i < virtPageNum; i++) {
            Page page = memVector.get(i);
            if (page.physical != -1) {
                map_count++;
            }
            for (int j = 0; j < virtPageNum; j++) {
                Page tmp_page = memVector.get(j);
                if (tmp_page.physical == page.physical && page.physical >= 0) {
                    physical_count++;
                }
            }
            if (physical_count > 1) {
                throw new RuntimeException("MemoryManagement: Duplicate physical page's in " + config);
            }
            physical_count = 0;
        }
        if (map_count < (virtPageNum + 1) / 2) {
            for (int i = 0; i < virtPageNum; i++) {
                Page page = memVector.get(i);
                if (page.physical == -1 && map_count < (virtPageNum + 1) / 2) {
                    page.physical = i;
                    map_count++;
                }
            }
        }
        for (int i = 0; i < virtPageNum; i++) {
            Page page = memVector.get(i);
            if (page.physical == -1) {
                controlPanel.removePhysicalPage(i);
            } else {
                controlPanel.addPhysicalPage(i, page.physical);
            }
        }
        for (Instruction instruction : instructVector) {
            long high = block * virtPageNum;
            if (instruction.addr < 0 || instruction.addr > high) {
                throw new RuntimeException("MemoryManagement: Instruction (" + instruction.inst + " " + instruction.addr + ") out of bounds.");
            }
        }
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

        File trace = new File(output);
        if (trace.exists()) {
            try {
                DataInputStream in = new DataInputStream(new FileInputStream(output));
                while ((line = in.readLine()) != null) {
                    temp.append(line).append(System.lineSeparator());
                }
                in.close();
            } catch (IOException e) {
                /* Do nothing */
            }
        }
        try {
            PrintStream out = new PrintStream(new FileOutputStream(output));
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
        controlPanel.addressValueLabel.setText(Long.toString(instruct.addr, addressradix));
        getPage(Virtual2Physical.pageNum(instruct.addr, virtPageNum, block));
        if ("YES".equals(controlPanel.pageFaultValueLabel.getText())) {
            controlPanel.pageFaultValueLabel.setText("NO");
        }
        if (instruct.inst.startsWith("READ")) {
            Page page = memVector.get(Virtual2Physical.pageNum(instruct.addr, virtPageNum, block));
            if (page.physical == -1) {
                if (doFileLog) {
                    printLogFile("READ " + Long.toString(instruct.addr, addressradix) + " ... page fault");
                }
                if (doStdoutLog) {
                    System.out.println("READ " + Long.toString(instruct.addr, addressradix) + " ... page fault");
                }
                PageFault.replacePage(memVector, virtPageNum, Virtual2Physical.pageNum(instruct.addr, virtPageNum, block), controlPanel);
                controlPanel.pageFaultValueLabel.setText("YES");
            } else {
                page.R = 1;
                page.lastTouchTime = 0;
                if (doFileLog) {
                    printLogFile("READ " + Long.toString(instruct.addr, addressradix) + " ... okay");
                }
                if (doStdoutLog) {
                    System.out.println("READ " + Long.toString(instruct.addr, addressradix) + " ... okay");
                }
            }
        }
        if (instruct.inst.startsWith("WRITE")) {
            Page page = memVector.get(Virtual2Physical.pageNum(instruct.addr, virtPageNum, block));
            if (page.physical == -1) {
                if (doFileLog) {
                    printLogFile("WRITE " + Long.toString(instruct.addr, addressradix) + " ... page fault");
                }
                if (doStdoutLog) {
                    System.out.println("WRITE " + Long.toString(instruct.addr, addressradix) + " ... page fault");
                }
                PageFault.replacePage(memVector, virtPageNum, Virtual2Physical.pageNum(instruct.addr, virtPageNum, block), controlPanel);
                controlPanel.pageFaultValueLabel.setText("YES");
            } else {
                page.M = 1;
                page.lastTouchTime = 0;
                if (doFileLog) {
                    printLogFile("WRITE " + Long.toString(instruct.addr, addressradix) + " ... okay");
                }
                if (doStdoutLog) {
                    System.out.println("WRITE " + Long.toString(instruct.addr, addressradix) + " ... okay");
                }
            }
        }
        for (int i = 0; i < virtPageNum; i++) {
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
