package org.example.os.lab3;

import org.example.os.lab3.domain.Page;

import java.util.Random;
import java.util.Vector;

class IOSystem {
    int max_schedules;
    int curr_schedules = 0;
    int clockticks;
    int curr_clockticks;
    Vector writingPages = new Vector();

    public IOSystem(int schedules, int clockticks) {
        this.max_schedules = schedules;
        this.clockticks = clockticks;
    }

    boolean scheduleWrite(Page page) {
        if (curr_schedules == max_schedules)
            return false;
        page.scheduled = 1;
        writingPages.addElement(page);
        curr_schedules++;
        return true;
    }

    void tick() {
        curr_clockticks++;
        if (curr_clockticks == clockticks) {
            curr_clockticks = 0;
            oneWrite();
        }
    }

    void oneWrite() {
        Random random = new Random();
        int index = random.nextInt(curr_schedules);

        Page page = (Page) writingPages.elementAt(index);
        page.scheduled = 0;
        page.M = 0;

        writingPages.removeElement(page);
        curr_schedules--;
    }

    void writeAll() {
        curr_clockticks = 0;
        int scheduled_amount = curr_schedules;
        for (int i = 0; i < scheduled_amount; i++) {
            oneWrite();
        }
    }
}

