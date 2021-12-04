package org.example.os.lab3;

import org.example.os.lab3.domain.Page;

import java.util.List;

import static java.util.Comparator.comparingInt;

public class PageFault {

    /**
     * The page replacement algorithm for the memory management sumulator.
     * This method gets called whenever a page needs to be replaced.
     * <p>
     * The page replacement algorithm included with the simulator is
     * FIFO (first-in first-out).  A while or for loop should be used
     * to search through the current memory contents for a canidate
     * replacement page.  In the case of FIFO the while loop is used
     * to find the proper page while making sure that virtPageNum is
     * not exceeded.
     * <pre>
     *   Page page = ( Page ) mem.elementAt( oldestPage )
     * </pre>
     * This line brings the contents of the Page at oldestPage (a
     * specified integer) from the mem vector into the page object.
     * Next recall the contents of the target page, replacePageNum.
     * Set the physical memory address of the page to be added equal
     * to the page to be removed.
     * <pre>
     *   controlPanel.removePhysicalPage( oldestPage )
     * </pre>
     * Once a page is removed from memory it must also be reflected
     * graphically.  This line does so by removing the physical page
     * at the oldestPage value.  The page which will be added into
     * memory must also be displayed through the addPhysicalPage
     * function call.  One must also remember to reset the values of
     * the page which has just been removed from memory.
     *
     * @param mem            is the vector which contains the contents of the pages
     *                       in memory being simulated.  mem should be searched to find the
     *                       proper page to remove, and modified to reflect any changes.
     * @param virtPageNum    is the number of virtual pages in the
     *                       simulator (set in Kernel.java).
     * @param replacePageNum is the requested page which caused the
     *                       page fault.
     * @param kernel         allows one to modify the current display.
     */
    public static void replacePage(List<Page> mem, int virtPageNum, int replacePageNum, Kernel kernel) {
        Page oldest = mem.stream()
                .limit(virtPageNum)
                .filter(page -> page.physical != -1)
                .max(comparingInt(p -> p.inMemTime))
                .orElse(mem.get(0));
        int oldestPage = mem.indexOf(oldest);

        Page nextpage = mem.get(replacePageNum);
        kernel.removePhysicalPage(oldestPage);
        nextpage.physical = oldest.physical;
        kernel.addPhysicalPage(nextpage.physical, replacePageNum);
        oldest.inMemTime = 0;
        oldest.lastTouchTime = 0;
        oldest.R = 0;
        oldest.M = 0;
        oldest.physical = -1;
    }

}
