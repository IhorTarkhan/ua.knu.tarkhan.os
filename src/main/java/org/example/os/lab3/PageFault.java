package org.example.os.lab3;

import org.example.os.lab3.domain.Page;

import java.util.List;

public class PageFault {
    public static WSClock wsclock = new WSClock();

    public static void replacePage(List<Page> mem, int replacePageNum, Kernel kernel, int tau, IOSystem io) {
        System.nanoTime();

        int removingPage = wsclock.getReplaceable(mem, tau, replacePageNum, io);

        Page page = mem.get(removingPage);
        Page nextpage = mem.get(replacePageNum);

        kernel.removePhysicalPage(removingPage);

        nextpage.physical = page.physical;

        kernel.addPhysicalPage(nextpage.physical, replacePageNum);

        page.inMemTime = 0;
        page.lastTouchTime = 0;
        page.R = 0;
        page.M = 0;
        page.physical = -1;
    }

}
