package org.example.os.lab3;

import org.example.os.lab3.domain.Page;

import java.util.List;
import java.util.Vector;

public class WSClock {
    private int pointer = 0;
    private Vector indexes = new Vector();

    public void init(List mem) {
        if (indexes.size() == 0) {
            for (int i = 0; i < mem.size(); i++) {
                Page page = (Page) mem.get(i);

                if (page.physical == -1)
                    continue;

                indexes.addElement(i);
            }
        }
    }

    public int getReplacable(List mem, int tau, int addingPage, IOSystem io) {

        int removingPage = -1;
        int lastUnmodifiedPage = -1;
        int index = -1;
        int moves = 0;
        boolean writeScheduled = false;

        while (moves < indexes.size() || writeScheduled) {
            moves++;
            if (pointer == indexes.size())
                pointer = 0;

            if (writeScheduled) io.tick();

            index = (int) indexes.elementAt(pointer);
            Page page = (Page) mem.get(index);

            if (page.R == 1) {
                page.R = 0;
                pointer++;
                continue;
            }

            if (page.M == 0)
                lastUnmodifiedPage = index;


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

        if (removingPage == -1)
            removingPage = lastUnmodifiedPage;

        if (removingPage == -1)
            removingPage = index;

        System.out.println(removingPage);

        indexes.setElementAt(addingPage, indexes.indexOf(removingPage));

        return removingPage;
    }
}
