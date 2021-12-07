package org.example.os.lab3;

import org.example.os.lab3.domain.Page;

import java.util.ArrayList;
import java.util.List;

public class WSClock {
    private final List<Integer> indexes = new ArrayList<>();
    private int pointer = 0;

    public WSClock() {
    }

    public WSClock(List<Page> mem) {
        for (int i = 0; i < mem.size(); i++) {
            if (mem.get(i).physical == -1) {
                continue;
            }
            indexes.add(i);
        }
    }

    public int getReplaceable(List<Page> mem, int tau, int addingPage, IOSystem io) {
        int removingPage = -1;
        int lastUnmodifiedPage = -1;
        int index = -1;
        boolean writeScheduled = false;

        for (int moves = 1; moves < indexes.size() || writeScheduled; moves++) {
            if (pointer == indexes.size()) {
                pointer = 0;
            }

            if (writeScheduled) {
                io.tick();
            }

            index = indexes.get(pointer);
            Page page = mem.get(index);

            if (page.R == 1) {
                page.R = 0;
                pointer++;
                continue;
            }

            if (page.M == 0) {
                lastUnmodifiedPage = index;
            }

            if (page.lastTouchTime < tau) {
                pointer++;
                continue;
            }

            if (page.M == 1 && page.scheduled != 1) {
                writeScheduled = io.scheduleWrite(page);
                pointer++;
                continue;
            }

            removingPage = index;
            break;
        }

        if (removingPage == -1) {
            removingPage = lastUnmodifiedPage;
        }

        if (removingPage == -1) {
            removingPage = index;
        }
        indexes.set(indexes.indexOf(removingPage), addingPage);
        return removingPage;
    }
}
