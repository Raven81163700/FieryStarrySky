package com.aurora.ui;

public final class CanvasScrollHelper {

    private int viewportTop;
    private int viewportBottom;
    private int contentHeight;
    private int scrollY;

    public void setViewport(int top, int bottom) {
        viewportTop = top;
        viewportBottom = bottom;
        clamp();
    }

    public void setContentHeight(int height) {
        contentHeight = height;
        clamp();
    }

    public int getScrollY() {
        return scrollY;
    }

    public void setScrollY(int y) {
        scrollY = y;
        clamp();
    }

    public void ensureVisible(int itemTop, int itemBottom) {
        if (itemTop < viewportTop) {
            scrollY -= (viewportTop - itemTop);
        } else if (itemBottom > viewportBottom) {
            scrollY += (itemBottom - viewportBottom);
        }
        clamp();
    }

    public boolean scrollBy(int delta) {
        int old = scrollY;
        scrollY += delta;
        clamp();
        return old != scrollY;
    }

    public int getMaxScroll() {
        int viewportHeight = viewportBottom - viewportTop;
        int max = contentHeight - viewportHeight;
        return max > 0 ? max : 0;
    }

    private void clamp() {
        int max = getMaxScroll();
        if (scrollY < 0) {
            scrollY = 0;
        }
        if (scrollY > max) {
            scrollY = max;
        }
    }
}
