package com.aurora.scene;

import com.aurora.ui.DrawUtils;
import com.aurora.world.StarMapCatalog;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;

public final class StarMapScene extends Canvas implements Scene {

    private static final int MAP_X = 6;
    private static final int MAP_Y_BASE = 26;
    private static final int MAP_BOTTOM_RESERVED = 20;
    private static final int MAP_EDGE_MARGIN = 24;
    private static final int CURSOR_STEP = 14;
    private static final int SCROLL_STEP = 14;
    private static final int TOUCH_BAR_THICK = 8;
    private static final int TOUCH_BAR_MIN_LEN = 36;
    private static final int TOUCH_PAN_STEP = 32;
    private static final int WORLD_PADDING = 140;
    private static final int BOUNDARY_BUFFER = 28;
    private static final int BLINK_INTERVAL_MS = 380;
    private static final int BOUNDARY_PULSE_MS = 900;
    private static final int SNAP_DISTANCE = 7;
    private static final int ZOOM_MIN = 20;
    private static final int ZOOM_MAX = 180;
    private static final int ZOOM_STEP = 20;
    private static final int ZOOM_BTN_SIZE = 14;
    private static final int BACK_BTN_W = 24;
    private static final int BACK_BTN_H = 12;

    private final SceneManager sceneManager;
    private final String username;
    private final int accountId;
    private final int characterId;
    private final com.aurora.net.StarMapService starMapService;

    private com.aurora.net.StarMapService.StarSystem[] systems = new com.aurora.net.StarMapService.StarSystem[0];
    private com.aurora.net.StarMapService.SystemLink[] links = new com.aurora.net.StarMapService.SystemLink[0];
    private com.aurora.net.StarMapService.Body[] bodies = new com.aurora.net.StarMapService.Body[0];

    private int selectedSystemIndex = 0;
    private int worldLeft = 0;
    private int worldTop = 0;
    private int worldRight = 0;
    private int worldBottom = 0;

    private int cursorWorldX = 0;
    private int cursorWorldY = 0;
    private int viewX = 0;
    private int viewY = 0;
    private int zoomPercent = 100;
    private int snappedSystemIndex = -1;
    private int minSystemX = 0;
    private int maxSystemX = 0;
    private int minSystemY = 0;
    private int maxSystemY = 0;
    private int boundaryLeft = 0;
    private int boundaryRight = 0;
    private int boundaryTop = 0;
    private int boundaryBottom = 0;

    private volatile boolean blinkRunning = false;
    private Thread blinkThread;
    private long boundaryPulseUntilMs = 0L;

    private String statusText = "加载中...";

    public StarMapScene(SceneManager sceneManager,
                        String username,
                        int accountId,
                        int characterId,
                        com.aurora.net.StarMapService starMapService) {
        this.sceneManager = sceneManager;
        this.username = username;
        this.accountId = accountId;
        this.characterId = characterId;
        this.starMapService = starMapService;
    }

    public Displayable asDisplayable() {
        return this;
    }

    public void onEnter() {
        startBlinkLoop();
        loadStarMap();
    }

    public void onExit() {
        stopBlinkLoop();
    }

    protected void paint(Graphics g) {
        int w = getWidth();
        int h = getHeight();

        DrawUtils.fillBackground(g, w, h);
        DrawUtils.drawMainTitle(g, w, "星图");

        int mapW = getMapWidth(w);
        int mapH = getMapHeight(h);
        int mapY = getMapY();

        g.setColor(0x2C3E5C);
        g.fillRect(MAP_X, mapY, mapW, mapH);
        g.setColor(0x7FA2D8);
        g.drawRect(MAP_X, mapY, mapW, mapH);

        int clipX = g.getClipX();
        int clipY = g.getClipY();
        int clipW = g.getClipWidth();
        int clipH = g.getClipHeight();
        applyMapAndBoundaryClip(g, mapW, mapH, mapY);
        drawBoundary(g);
        drawLinks(g);
        drawSystems(g);
        drawCursor(g);
        g.setClip(clipX, clipY, clipW, clipH);
        drawTouchPad(g, w, h);

        drawFooter(g, w, h);
    }

    private void drawBoundary(Graphics g) {
        boolean pulse = System.currentTimeMillis() < boundaryPulseUntilMs;
        if (!pulse && ((System.currentTimeMillis() / BLINK_INTERVAL_MS) & 1L) != 0L) {
            return;
        }
        int x1 = worldToScreenX(boundaryLeft);
        int y1 = worldToScreenY(boundaryTop);
        int x2 = worldToScreenX(boundaryRight);
        int y2 = worldToScreenY(boundaryBottom);
        int left = x1 < x2 ? x1 : x2;
        int top = y1 < y2 ? y1 : y2;
        int width = x1 < x2 ? x2 - x1 : x1 - x2;
        int height = y1 < y2 ? y2 - y1 : y1 - y2;

        g.setColor(pulse ? 0xFFD86B : 0xFF9D4D);
        g.drawRect(left, top, width, height);
        if (pulse && width > 2 && height > 2) {
            g.drawRect(left + 1, top + 1, width - 2, height - 2);
        }
    }

    private void drawLinks(Graphics g) {
        int i;
        g.setColor(0x5B82C2);
        for (i = 0; i < links.length; i++) {
            com.aurora.net.StarMapService.SystemLink l = links[i];
            com.aurora.net.StarMapService.StarSystem a = findSystem(l.fromId);
            com.aurora.net.StarMapService.StarSystem b = findSystem(l.toId);
            if (a == null || b == null) {
                continue;
            }
            int ax = worldToScreenX(a.x);
            int ay = worldToScreenY(a.y);
            int bx = worldToScreenX(b.x);
            int by = worldToScreenY(b.y);
            g.drawLine(ax, ay, bx, by);
        }
    }

    private void drawSystems(Graphics g) {
        int i;
        int mapW = getMapWidth(getWidth());
        int mapH = getMapHeight(getHeight());
        for (i = 0; i < systems.length; i++) {
            com.aurora.net.StarMapService.StarSystem s = systems[i];
            int sx = worldToScreenX(s.x);
            int sy = worldToScreenY(s.y);
            if (!isPointNearMap(sx, sy, mapW, mapH)) {
                continue;
            }

            boolean selected = i == snappedSystemIndex;
            g.setColor(selected ? 0xFFD166 : 0xFFFFFF);
            g.fillRect(sx - 2, sy - 2, 5, 5);
            if (selected) {
                g.drawString(s.name, sx + 4, sy - 6, Graphics.TOP | Graphics.LEFT);
            }
        }
    }

    private void drawCursor(Graphics g) {
        int mapW = getMapWidth(getWidth());
        int mapH = getMapHeight(getHeight());
        int cx = worldToScreenX(cursorWorldX);
        int cy = worldToScreenY(cursorWorldY);
        if (!isPointNearMap(cx, cy, mapW, mapH)) {
            return;
        }
        g.setColor(0x70E3FF);
        g.drawRect(cx - 4, cy - 4, 8, 8);
        g.drawLine(cx - 7, cy, cx - 5, cy);
        g.drawLine(cx + 5, cy, cx + 7, cy);
        g.drawLine(cx, cy - 7, cx, cy - 5);
        g.drawLine(cx, cy + 5, cx, cy + 7);
    }

    private void drawTouchPad(Graphics g, int w, int h) {
        int mapW = getMapWidth(w);
        int mapH = getMapHeight(h);
        int mapY = getMapY();
        int topLen = mapW / 3;
        if (topLen < TOUCH_BAR_MIN_LEN) topLen = TOUCH_BAR_MIN_LEN;
        int sideLen = mapH / 3;
        if (sideLen < TOUCH_BAR_MIN_LEN) sideLen = TOUCH_BAR_MIN_LEN;

        int topX = MAP_X + (mapW - topLen) / 2;
        int topY = mapY + 2;
        int bottomX = topX;
        int bottomY = mapY + mapH - TOUCH_BAR_THICK - 2;
        int leftX = MAP_X + 2;
        int leftY = mapY + (mapH - sideLen) / 2;
        int rightX = MAP_X + mapW - TOUCH_BAR_THICK - 2;
        int rightY = leftY;

        drawTouchButton(g, leftX, leftY, TOUCH_BAR_THICK, sideLen, "<");
        drawTouchButton(g, rightX, rightY, TOUCH_BAR_THICK, sideLen, ">");
        drawTouchButton(g, topX, topY, topLen, TOUCH_BAR_THICK, "^");
        drawTouchButton(g, bottomX, bottomY, topLen, TOUCH_BAR_THICK, "v");

        int[] zoomRect = getZoomRect(w, h);
        int plusX = zoomRect[0];
        int plusY = zoomRect[1];
        int minusX = zoomRect[2];
        int minusY = zoomRect[3];
        drawTouchButton(g, plusX, plusY, ZOOM_BTN_SIZE, ZOOM_BTN_SIZE, "+");
        drawTouchButton(g, minusX, minusY, ZOOM_BTN_SIZE, ZOOM_BTN_SIZE, "-");

        int[] backRect = getBackRect();
        drawTouchButton(g, backRect[0], backRect[1], BACK_BTN_W, BACK_BTN_H, "返回");
    }

    private void drawTouchButton(Graphics g, int x, int y, int w, int h, String text) {
        g.setColor(0x1E3554);
        g.fillRect(x, y, w, h);
        g.setColor(0x95B8EA);
        g.drawRect(x, y, w, h);
        g.setColor(0xE4F0FF);
        g.drawString(text, x + w / 2, y + (h / 2) - 4, Graphics.TOP | Graphics.HCENTER);
    }

    private void drawFooter(Graphics g, int w, int h) {
        g.setColor(0xC9D8F2);
        int y = h - 14;

        com.aurora.net.StarMapService.StarSystem s = getSnappedSystem();
        if (s != null) {
            g.drawString("系统:" + s.name + "  安全:" + s.security, 6, y, Graphics.TOP | Graphics.LEFT);
        } else {
            g.drawString("系统:--  安全:--", 6, y, Graphics.TOP | Graphics.LEFT);
        }
    }

    protected void keyPressed(int keyCode) {
        handleKeyControl(keyCode);
    }

    protected void keyRepeated(int keyCode) {
        handleKeyControl(keyCode);
    }

    private void handleKeyControl(int keyCode) {
        if (keyCode == KEY_NUM1 || keyCode == KEY_STAR) {
            applyZoom(-ZOOM_STEP);
            return;
        }
        if (keyCode == KEY_NUM3 || keyCode == KEY_POUND) {
            applyZoom(ZOOM_STEP);
            return;
        }

        int action = getGameAction(keyCode);
        if (action == LEFT) {
            moveCursor(-CURSOR_STEP, 0);
            return;
        }
        if (action == RIGHT) {
            moveCursor(CURSOR_STEP, 0);
            return;
        }
        if (action == UP) {
            moveCursor(0, -CURSOR_STEP);
            return;
        }
        if (action == DOWN) {
            moveCursor(0, CURSOR_STEP);
            return;
        }
    }

    protected void pointerPressed(int x, int y) {
        if (touchBackAt(x, y)) {
            sceneManager.showHomeScene(username, accountId);
            return;
        }

        int zoomAction = touchZoomAt(x, y);
        if (zoomAction != 0) {
            applyZoom(zoomAction > 0 ? ZOOM_STEP : -ZOOM_STEP);
            return;
        }

        int touchAction = touchDirectionAt(x, y);
        if (touchAction != 0) {
            if (touchAction == LEFT) panView(-TOUCH_PAN_STEP, 0);
            else if (touchAction == RIGHT) panView(TOUCH_PAN_STEP, 0);
            else if (touchAction == UP) panView(0, -TOUCH_PAN_STEP);
            else if (touchAction == DOWN) panView(0, TOUCH_PAN_STEP);
            return;
        }

        int mapW = getMapWidth(getWidth());
        int mapH = getMapHeight(getHeight());
        int mapY = getMapY();
        if (x < MAP_X || x > MAP_X + mapW || y < mapY || y > mapY + mapH) {
            return;
        }

        int wx = screenToWorldX(x);
        int wy = screenToWorldY(y);
        cursorWorldX = clamp(wx, worldLeft, worldRight);
        cursorWorldY = clamp(wy, worldTop, worldBottom);
        updateSnap();
        repaint();
    }

    private void moveCursor(int dx, int dy) {
        int oldX = cursorWorldX;
        int oldY = cursorWorldY;
        cursorWorldX = clamp(cursorWorldX + dx, minSystemX, maxSystemX);
        cursorWorldY = clamp(cursorWorldY + dy, minSystemY, maxSystemY);
        if (oldX == cursorWorldX && oldY == cursorWorldY && (dx != 0 || dy != 0)) {
            boundaryPulseUntilMs = System.currentTimeMillis() + BOUNDARY_PULSE_MS;
        }
        updateSnap();
        autoScrollByCursor();
        repaint();
    }

    private void updateSnap() {
        int i;
        int bestIdx = -1;
        int bestD2 = 0x7FFFFFFF;
        for (i = 0; i < systems.length; i++) {
            if (!isSystemVisible(systems[i])) {
                continue;
            }
            int dx = systems[i].x - cursorWorldX;
            int dy = systems[i].y - cursorWorldY;
            int d2 = dx * dx + dy * dy;
            if (d2 < bestD2) {
                bestD2 = d2;
                bestIdx = i;
            }
        }
        if (bestIdx >= 0 && bestD2 <= SNAP_DISTANCE * SNAP_DISTANCE) {
            snappedSystemIndex = bestIdx;
            selectedSystemIndex = bestIdx;
            cursorWorldX = systems[bestIdx].x;
            cursorWorldY = systems[bestIdx].y;
        } else {
            snappedSystemIndex = -1;
        }
    }

    private void autoScrollByCursor() {
        int mapW = getWidth() - 12;
        int mapH = getMapHeight(getHeight());
        int mapY = getMapY();
        int sx = worldToScreenX(cursorWorldX);
        int sy = worldToScreenY(cursorWorldY);

        if (sx < MAP_X + MAP_EDGE_MARGIN) {
            viewX -= SCROLL_STEP;
        } else if (sx > MAP_X + mapW - MAP_EDGE_MARGIN) {
            viewX += SCROLL_STEP;
        }
        if (sy < mapY + MAP_EDGE_MARGIN) {
            viewY -= SCROLL_STEP;
        } else if (sy > mapY + mapH - MAP_EDGE_MARGIN) {
            viewY += SCROLL_STEP;
        }
        clampView();
    }

    private void panView(int dx, int dy) {
        viewX += dx;
        viewY += dy;
        clampView();
        updateSnap();
        repaint();
    }

    private void loadStarMap() {
        statusText = "加载星图...";
        Thread t = new Thread(new Runnable() {
            public void run() {
                final com.aurora.net.StarMapService.Result res = starMapService.getStarMap();
                sceneManager.getDisplay().callSerially(new Runnable() {
                    public void run() {
                        if (!res.isSuccess()) {
                            applyCatalogFallback("网络星图失败，已用本地配置");
                            return;
                        }
                        com.aurora.net.StarMapService.StarMapData data = (com.aurora.net.StarMapService.StarMapData) res.getData();
                        systems = data.systems == null ? new com.aurora.net.StarMapService.StarSystem[0] : data.systems;
                        links = data.links == null ? new com.aurora.net.StarMapService.SystemLink[0] : data.links;
                        prepareWorldBoundsAndCursor();
                        statusText = "星图加载完成";
                        repaint();
                    }
                });
            }
        });
        t.start();
    }

    private void applyCatalogFallback(String status) {
        systems = mapSystemsFromCatalog();
        links = mapLinksFromCatalog();
        prepareWorldBoundsAndCursor();
        statusText = status;
        repaint();
    }

    private void prepareWorldBoundsAndCursor() {
        if (systems.length == 0) {
            worldLeft = 0;
            worldTop = 0;
            worldRight = 100;
            worldBottom = 100;
            viewX = 0;
            viewY = 0;
            cursorWorldX = 0;
            cursorWorldY = 0;
            snappedSystemIndex = -1;
            return;
        }

        int i;
        int minX = systems[0].x;
        int maxX = systems[0].x;
        int minY = systems[0].y;
        int maxY = systems[0].y;
        for (i = 1; i < systems.length; i++) {
            if (systems[i].x < minX) minX = systems[i].x;
            if (systems[i].x > maxX) maxX = systems[i].x;
            if (systems[i].y < minY) minY = systems[i].y;
            if (systems[i].y > maxY) maxY = systems[i].y;
        }

        minSystemX = minX;
        maxSystemX = maxX;
        minSystemY = minY;
        maxSystemY = maxY;
        boundaryLeft = minX - BOUNDARY_BUFFER;
        boundaryRight = maxX + BOUNDARY_BUFFER;
        boundaryTop = minY - BOUNDARY_BUFFER;
        boundaryBottom = maxY + BOUNDARY_BUFFER;

        worldLeft = minX - WORLD_PADDING;
        worldTop = minY - WORLD_PADDING;
        worldRight = maxX + WORLD_PADDING;
        worldBottom = maxY + WORLD_PADDING;

        if (selectedSystemIndex < 0 || selectedSystemIndex >= systems.length) {
            selectedSystemIndex = 0;
        }

        cursorWorldX = systems[selectedSystemIndex].x;
        cursorWorldY = systems[selectedSystemIndex].y;
        centerViewOn(cursorWorldX, cursorWorldY);
        updateSnap();
    }

    private com.aurora.net.StarMapService.StarSystem[] mapSystemsFromCatalog() {
        StarMapCatalog.StarSystem[] src = StarMapCatalog.systems();
        com.aurora.net.StarMapService.StarSystem[] out = new com.aurora.net.StarMapService.StarSystem[src.length];
        int i;
        for (i = 0; i < src.length; i++) {
            com.aurora.net.StarMapService.StarSystem s = new com.aurora.net.StarMapService.StarSystem();
            s.id = src[i].id;
            s.name = src[i].name;
            s.x = src[i].x;
            s.y = src[i].y;
            s.security = src[i].security;
            out[i] = s;
        }
        return out;
    }

    private com.aurora.net.StarMapService.SystemLink[] mapLinksFromCatalog() {
        StarMapCatalog.SystemLink[] src = StarMapCatalog.links();
        com.aurora.net.StarMapService.SystemLink[] out = new com.aurora.net.StarMapService.SystemLink[src.length];
        int i;
        for (i = 0; i < src.length; i++) {
            com.aurora.net.StarMapService.SystemLink l = new com.aurora.net.StarMapService.SystemLink();
            l.fromId = src[i].fromId;
            l.toId = src[i].toId;
            l.linkType = src[i].linkType;
            l.cost = src[i].cost;
            out[i] = l;
        }
        return out;
    }

    private com.aurora.net.StarMapService.StarSystem findSystem(int id) {
        int i;
        for (i = 0; i < systems.length; i++) {
            if (systems[i].id == id) {
                return systems[i];
            }
        }
        return null;
    }

    private com.aurora.net.StarMapService.StarSystem getSnappedSystem() {
        if (systems.length == 0 || snappedSystemIndex < 0 || snappedSystemIndex >= systems.length) {
            return null;
        }
        return systems[snappedSystemIndex];
    }

    private int worldToScreenX(int wx) {
        return MAP_X + ((wx - worldLeft - viewX) * zoomPercent) / 100;
    }

    private int worldToScreenY(int wy) {
        return getMapY() + ((wy - worldTop - viewY) * zoomPercent) / 100;
    }

    private int screenToWorldX(int sx) {
        return worldLeft + viewX + ((sx - MAP_X) * 100) / zoomPercent;
    }

    private int screenToWorldY(int sy) {
        return worldTop + viewY + ((sy - getMapY()) * 100) / zoomPercent;
    }

    private void centerViewOn(int wx, int wy) {
        int visibleW = getVisibleWorldWidth();
        int visibleH = getVisibleWorldHeight();
        viewX = (wx - worldLeft) - visibleW / 2;
        viewY = (wy - worldTop) - visibleH / 2;
        clampView();
    }

    private void clampView() {
        int visibleW = getVisibleWorldWidth();
        int visibleH = getVisibleWorldHeight();
        int maxX = (worldRight - worldLeft) - visibleW;
        int maxY = (worldBottom - worldTop) - visibleH;
        if (maxX < 0) maxX = 0;
        if (maxY < 0) maxY = 0;

        if (viewX < 0) viewX = 0;
        if (viewY < 0) viewY = 0;
        if (viewX > maxX) viewX = maxX;
        if (viewY > maxY) viewY = maxY;
    }

    private boolean isPointNearMap(int x, int y, int mapW, int mapH) {
        int mapY = getMapY();
        return x >= MAP_X - 8 && x <= MAP_X + mapW + 8 && y >= mapY - 8 && y <= mapY + mapH + 8;
    }

    private void applyMapAndBoundaryClip(Graphics g, int mapW, int mapH, int mapY) {
        int x1 = worldToScreenX(boundaryLeft);
        int y1 = worldToScreenY(boundaryTop);
        int x2 = worldToScreenX(boundaryRight);
        int y2 = worldToScreenY(boundaryBottom);
        int left = x1 < x2 ? x1 : x2;
        int top = y1 < y2 ? y1 : y2;
        int width = x1 < x2 ? x2 - x1 : x1 - x2;
        int height = y1 < y2 ? y2 - y1 : y1 - y2;

        g.setClip(MAP_X + 1, mapY + 1, mapW - 1, mapH - 1);
        g.clipRect(left, top, width + 1, height + 1);
    }

    private boolean isSystemVisible(com.aurora.net.StarMapService.StarSystem s) {
        int mapW = getMapWidth(getWidth());
        int mapH = getMapHeight(getHeight());
        int mapY = getMapY();
        int sx = worldToScreenX(s.x);
        int sy = worldToScreenY(s.y);
        return sx >= MAP_X && sx <= MAP_X + mapW && sy >= mapY && sy <= mapY + mapH;
    }

    private int clamp(int v, int min, int max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }

    private int getMapY() {
        return MAP_Y_BASE;
    }

    private int getMapWidth(int screenW) {
        int w = screenW - (MAP_X * 2);
        if (w < 80) w = 80;
        return w;
    }

    private int getMapHeight(int screenH) {
        int available = screenH - getMapY() - MAP_BOTTOM_RESERVED;
        int minH = 82;
        if (available < minH) {
            return minH;
        }
        return available;
    }

    private int getVisibleWorldWidth() {
        return (getMapWidth(getWidth()) * 100) / zoomPercent;
    }

    private int getVisibleWorldHeight() {
        return (getMapHeight(getHeight()) * 100) / zoomPercent;
    }

    private void applyZoom(int delta) {
        int oldZoom = zoomPercent;
        int next = zoomPercent + delta;
        if (next < ZOOM_MIN) next = ZOOM_MIN;
        if (next > ZOOM_MAX) next = ZOOM_MAX;
        if (next == oldZoom) {
            return;
        }
        zoomPercent = next;
        centerViewOn(cursorWorldX, cursorWorldY);
        updateSnap();
        statusText = "缩放: " + zoomPercent + "%";
        repaint();
    }

    private int touchDirectionAt(int x, int y) {
        int w = getWidth();
        int h = getHeight();
        int mapW = getMapWidth(w);
        int mapH = getMapHeight(h);
        int mapY = getMapY();
        int topLen = mapW / 3;
        if (topLen < TOUCH_BAR_MIN_LEN) topLen = TOUCH_BAR_MIN_LEN;
        int sideLen = mapH / 3;
        if (sideLen < TOUCH_BAR_MIN_LEN) sideLen = TOUCH_BAR_MIN_LEN;

        int topX = MAP_X + (mapW - topLen) / 2;
        int topY = mapY + 2;
        int bottomX = topX;
        int bottomY = mapY + mapH - TOUCH_BAR_THICK - 2;
        int leftX = MAP_X + 2;
        int leftY = mapY + (mapH - sideLen) / 2;
        int rightX = MAP_X + mapW - TOUCH_BAR_THICK - 2;
        int rightY = leftY;

        if (insideRect(x, y, leftX, leftY, TOUCH_BAR_THICK, sideLen)) {
            return LEFT;
        }
        if (insideRect(x, y, rightX, rightY, TOUCH_BAR_THICK, sideLen)) {
            return RIGHT;
        }
        if (insideRect(x, y, topX, topY, topLen, TOUCH_BAR_THICK)) {
            return UP;
        }
        if (insideRect(x, y, bottomX, bottomY, topLen, TOUCH_BAR_THICK)) {
            return DOWN;
        }
        return 0;
    }

    private int touchZoomAt(int x, int y) {
        int[] rect = getZoomRect(getWidth(), getHeight());
        if (insideRect(x, y, rect[0], rect[1], ZOOM_BTN_SIZE, ZOOM_BTN_SIZE)) {
            return 1;
        }
        if (insideRect(x, y, rect[2], rect[3], ZOOM_BTN_SIZE, ZOOM_BTN_SIZE)) {
            return -1;
        }
        return 0;
    }

    private int[] getZoomRect(int w, int h) {
        int mapW = getMapWidth(w);
        int mapY = getMapY();
        int plusX = MAP_X + mapW - ZOOM_BTN_SIZE - 2;
        int plusY = mapY + 2;
        int minusX = plusX;
        int minusY = plusY + ZOOM_BTN_SIZE + 2;
        return new int[]{plusX, plusY, minusX, minusY};
    }

    private int[] getBackRect() {
        int x = MAP_X + 2;
        int y = getMapY() + 2;
        return new int[]{x, y};
    }

    private boolean touchBackAt(int x, int y) {
        int[] rect = getBackRect();
        return insideRect(x, y, rect[0], rect[1], BACK_BTN_W, BACK_BTN_H);
    }

    private void startBlinkLoop() {
        stopBlinkLoop();
        blinkRunning = true;
        blinkThread = new Thread(new Runnable() {
            public void run() {
                while (blinkRunning) {
                    try {
                        Thread.sleep(BLINK_INTERVAL_MS);
                    } catch (InterruptedException e) {
                        // no-op
                    }
                    if (!blinkRunning) {
                        break;
                    }
                    repaint();
                }
            }
        });
        blinkThread.start();
    }

    private void stopBlinkLoop() {
        blinkRunning = false;
        if (blinkThread != null) {
            blinkThread.interrupt();
            blinkThread = null;
        }
    }

    private boolean insideRect(int px, int py, int x, int y, int w, int h) {
        return px >= x && px <= x + w && py >= y && py <= y + h;
    }
}
