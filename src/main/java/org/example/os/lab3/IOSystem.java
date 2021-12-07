package org.example.os.lab3;

import org.example.os.lab3.domain.Page;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;

class IOSystem {
    private final List<Page> writingPages = new ArrayList<>();
    private final int maxSchedules;
    private final int clockTicks;
    private int currSchedules = 0;
    private int currClockTicks = 0;

    public IOSystem(int schedules, int clockTicks) {
        this.maxSchedules = schedules;
        this.clockTicks = clockTicks;
    }

    boolean scheduleWrite(Page page) {
        if (currSchedules == maxSchedules) {
            return false;
        }
        page.scheduled = 1;
        writingPages.add(page);
        currSchedules++;
        return true;
    }

    void tick() {
        currClockTicks++;
        if (currClockTicks == clockTicks) {
            currClockTicks = 0;
            oneWrite();
        }
    }

    void oneWrite() {
        Random random = new Random();
        int index = random.nextInt(currSchedules);

        Page page = writingPages.get(index);
        page.scheduled = 0;
        page.M = 0;

        writingPages.remove(page);
        currSchedules--;
    }

    void writeAll() {
        currClockTicks = 0;
        for (int i = 0; i < currSchedules; i++) {
            oneWrite();
        }
    }
}

