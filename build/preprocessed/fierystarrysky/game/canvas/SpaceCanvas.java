/*
 * Reconstructed SpaceCanvas with HUD integration and performance/threading improvements.
 */
package fierystarrysky.game.canvas;

import fierystarrysky.Midlet;
import fierystarrysky.game.status.ship.ShipMovement;
import fierystarrysky.game.ui.menu.BaseMenu;
import fierystarrysky.game.ui.menu.space.CommonMenu;
import fierystarrysky.model.item.WeaponModel;
import fierystarrysky.game.object.SpaceObject;
import fierystarrysky.game.object.space.SpaceStation;
import fierystarrysky.game.object.space.TestTarget;
import fierystarrysky.game.status.ship.ShipStatus;
import fierystarrysky.game.status.ship.WeaponStatus;
import fierystarrysky.game.status.ship.MountingSlotEquipmentStatus;
import fierystarrysky.game.ui.dialog.BaseDialog;
import fierystarrysky.game.ui.menu.space.ActionMenu;
import fierystarrysky.util.FontUtils;
import fierystarrysky.util.RMSUtils;
import fierystarrysky.util.WorldUtils;
import java.util.Vector;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.GameCanvas;
import res.config.loader.ShipInfoLoader;
import res.image.loader.SpaceDashBoardImageLoader;
import res.image.loader.ItemCollectionImageLoader;
import fierystarrysky.game.ui.HUD;

public class SpaceCanvas extends GameCanvas implements Runnable {

    private int screenW, screenH;
    private ShipMovement shipMovement;
    private ShipStatus shipStatus;
    private Vector objects = new Vector();
    private long lastTimestamp = 0L;
    private Midlet midlet;
    private BaseMenu currentMenu;
    private boolean menuAction;
    private long lastKeyTime = 0;
    private final int KEY_DELAY = 200;
    private int leftSoftKey = -999;
    private int rightSoftKey = -999;
    public static float zoomScale = 10;
    private int commonMenuX;
    private int commonMenuY;
    private int COMMON_MENU_WIDTH;
    private int cachedSmallWidth;
    private int cachedSmallHeight;
    private Font cachedSmallFont;
    // reuse menu instances
    private CommonMenu commonMenuInstance;
    private ActionMenu actionMenuInstance;
    private int[] starX1, starY1;
    private int[] starX2, starY2;
    private int[] starSize1;
    private int[] starBrightness1;
    private int starCount1 = 50;
    private int starCount2 = 15;
    private Vector dialogs = new Vector();
    private SpaceDashBoardImageLoader dashboardRes;
    private HUD hud;
    private ItemCollectionImageLoader itemImgLoader;
    // collapsible active-module panel (right-bottom, above softkey menu)
    private boolean modulePanelExpanded = false;
    private int modulePanelArrowW = 12;
    private int modulePanelArrowH = 20;
    private int modulePanelItemH;
    private int modulePanelW;

    public SpaceCanvas(Midlet midlet) {
        super(true);
        this.midlet = midlet;

        dashboardRes = new SpaceDashBoardImageLoader();
        itemImgLoader = new ItemCollectionImageLoader();

        String leftKey = RMSUtils.loadRMS("leftSoftKey");
        String rightKey = RMSUtils.loadRMS("rightSoftKey");
        if (leftKey != null) {
            this.leftSoftKey = Integer.parseInt(leftKey);
        }
        if (rightKey != null) {
            this.rightSoftKey = Integer.parseInt(rightKey);
        }

        screenW = getWidth();
        screenH = getHeight();

        // cache font metrics
        cachedSmallFont = FontUtils.getSmall();
        cachedSmallWidth = FontUtils.getSmallWidth();
        cachedSmallHeight = FontUtils.getSmallHeight();
        COMMON_MENU_WIDTH = cachedSmallWidth * 6 - 4;
        modulePanelItemH = cachedSmallHeight + 8;
        modulePanelW = cachedSmallWidth * 9 + 24;

        commonMenuX = screenW - cachedSmallWidth * 4 - 16;
        commonMenuY = screenH - cachedSmallHeight - 4;

        initStars();
        ShipInfoLoader shipInfoLoader = new ShipInfoLoader("b096ea75-4ecf-4c17-9c9b-35660a8e7435");
        shipStatus = new ShipStatus(shipInfoLoader.getInfo());
        shipMovement = new ShipMovement(0, 0, this);
        shipMovement.loadShip(shipStatus.getAcceleration(), shipStatus.getBrakingRatio(), shipStatus.getTurnRate(), shipStatus.getMaxSpeed());

        WeaponModel testRocket = (WeaponModel) new WeaponModel.MissileWeaponLoader("6930471d-52b8-4101-9aa8-18f00f5feaa4").getInfo();
        WeaponStatus testRocketStatus = new WeaponStatus(testRocket);
        shipStatus.getShipSlot().addMountingSlot(testRocketStatus);
        objects.addElement(new SpaceStation(80, 80, 50, "维拉希首星-4 联合工业矿物精炼站"));
        objects.addElement(new TestTarget(-2, 2, 10, "测试标靶"));

        hud = new HUD(shipStatus, shipMovement, dashboardRes, cachedSmallFont, cachedSmallWidth, cachedSmallHeight);
        HUD.SpaceCanvasCompat.zoomScale = zoomScale;
    }

    public void clearMenu() {
        currentMenu = null;
        menuAction = false;
    }

    protected void pointerPressed(int x, int y) {
        if (handleModulePanelClick(x, y)) {
            return;
        }

        if (menuAction) {
            if (currentMenu != null) {
                int itemIndex = currentMenu.getItemAt(x, y);
                if (itemIndex == -1) {
                    clearMenu();
                } else {
                    currentMenu.select(this);
                }
                return;
            }
            clearMenu();
        }
        int actionX = commonMenuX + COMMON_MENU_WIDTH / 2;
        int actionY = commonMenuY;

        int endX = actionX;
        int endY = screenH;

        int actionEndX = screenW;
        int actionEndY = screenH;
        if (x > commonMenuX && x < endX && y > commonMenuY && y < endY) {
            onLeftSoftKey();
        }
        if (x > actionX && x < actionEndX && y > actionY && y < actionEndY) {
            onRightSoftKey();
        }
        boolean clickedAny = false;
        for (int i = 0; i < objects.size(); i++) {
            SpaceObject obj = (SpaceObject) objects.elementAt(i);
            if (obj.isClicked(x, y, shipMovement.getX(), shipMovement.getY(), screenW, screenH)) {
                // mark selected object in ship status
                shipStatus.setSelectedObject(obj);
                obj.onClick(this, screenW, screenH);
                clickedAny = true;
                break;
            }
        }
        if (!clickedAny) {
            shipStatus.setSelectedObject(null);
        }
    }

    public void run() {
        final int targetFrameMs = 20;
        Graphics g = null;
        while (midlet.isRunning()) {
            long frameStart = System.currentTimeMillis();
            try {
                if (g == null) {
                    g = getGraphics();
                }
                input();
                update();
                try {
                    render(g);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                flushGraphics();

                long elapsed = System.currentTimeMillis() - frameStart;
                long sleepMs = targetFrameMs - elapsed;
                if (sleepMs > 0) {
                    try {
                        Thread.sleep(sleepMs);
                    } catch (InterruptedException ie) {
                        break;
                    }
                } else {
                    Thread.yield();
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }

    protected void keyPressed(int keyCode) {
        if (keyCode == leftSoftKey) {
            onLeftSoftKey();
        } else if (keyCode == rightSoftKey) {
            onRightSoftKey();
        }
    }

    private void onLeftSoftKey() {
        if (menuAction && currentMenu != null) {
            clearMenu();
        } else {
            menuAction = true;
            if (commonMenuInstance == null) {
                commonMenuInstance = new CommonMenu(commonMenuX, commonMenuY, COMMON_MENU_WIDTH, cachedSmallHeight + 8);
            }
            currentMenu = commonMenuInstance;
        }
    }

    private void onRightSoftKey() {
        if (menuAction && currentMenu != null) {
            clearMenu();
        } else {
            menuAction = true;
            if (actionMenuInstance == null) {
                actionMenuInstance = new ActionMenu(commonMenuX, commonMenuY, COMMON_MENU_WIDTH, cachedSmallHeight + 8);
            }
            currentMenu = actionMenuInstance;
        }
    }

    private void input() {
        int key = getKeyStates();
        long now = System.currentTimeMillis();

        if ((key & LEFT_PRESSED) != 0) {
            shipMovement.turnLeft();
        }
        if ((key & RIGHT_PRESSED) != 0) {
            shipMovement.turnRight();
        }
        if ((key & UP_PRESSED) != 0) {
            if (menuAction) {
                if (now - lastKeyTime > KEY_DELAY) {
                    menuPrevious();
                    lastKeyTime = now;
                }
                return;
            }
            shipMovement.throttleUp();
        }
        if ((key & DOWN_PRESSED) != 0) {
            if (menuAction) {
                if (now - lastKeyTime > KEY_DELAY) {
                    menuNext();
                    lastKeyTime = now;
                }
                return;
            }
            shipMovement.throttleDown();
        }

        if ((key & FIRE_PRESSED) != 0) {
            if (menuAction) {
                if (now - lastKeyTime > KEY_DELAY) {
                    menuConfirm();
                    lastKeyTime = now;
                }
                return;
            }
        }
    }

    private void menuPrevious() {
        if (currentMenu != null) {
            currentMenu.selectUp();
            return;
        }
        menuAction = false;
    }

    private void menuNext() {
        if (currentMenu != null) {
            currentMenu.selectDown();
            return;
        }
        menuAction = false;
    }

    private void menuConfirm() {
        if (currentMenu != null) {
            currentMenu.select(this);
            return;
        }
        menuAction = false;
    }

    private void update() {
        shipStatus.setThrottle(shipMovement.getThrottle());
        shipStatus.setCurrentAngle(shipMovement.getCurrentAngle());
        shipStatus.setCurrentX(shipMovement.getX());
        shipStatus.setCurrentY(shipMovement.getY());
        shipStatus.setCurrentSpeed(shipMovement.getCurrentSpeed());

        long nowTime = System.currentTimeMillis();
        long deltaMs = (lastTimestamp == 0L ? 0 : (nowTime - lastTimestamp));
        float deltaTime = deltaMs / 1000f;
        lastTimestamp = nowTime;

        shipMovement.update(deltaTime);
        updateActiveModules(deltaMs);
        hud.update(deltaTime);
    }

    private void render(Graphics g) {
        drawBackground(g);
        drawObject(g);
        drawPlayer(g);
        hud.render(g, screenW, screenH);
        drawMenu(g);
        drawModulePanel(g);
    }

    private void initStars() {
        java.util.Random rand = new java.util.Random();

        starX1 = new int[starCount1];
        starY1 = new int[starCount1];
        starSize1 = new int[starCount1];
        starBrightness1 = new int[starCount1];
        for (int i = 0; i < starCount1; i++) {
            starX1[i] = rand.nextInt(screenW);
            starY1[i] = rand.nextInt(screenH);
        }

        starX2 = new int[starCount2];
        starY2 = new int[starCount2];
        for (int i = 0; i < starCount2; i++) {
            starX2[i] = rand.nextInt(screenW);
            starY2[i] = rand.nextInt(screenH);
            starSize1[i] = 1 + rand.nextInt(2);
            starBrightness1[i] = 0x88 + rand.nextInt(0x77);
        }
    }

    private void drawBackground(Graphics g) {
        g.setColor(0x000000);
        g.fillRect(0, 0, screenW, screenH);

        for (int i = 0; i < starCount1; i++) {
            int brightness = starBrightness1[i];
            int color = (brightness << 16) | (brightness << 8) | brightness;
            g.setColor(color);
            g.fillRect(starX1[i], starY1[i], starSize1[i], starSize1[i]);
        }

        g.setColor(0xFFFFFF);
        float parallax = 0.5f * zoomScale;
        float shipX = shipMovement.getX();
        float shipYscreen = WorldUtils.worldToScreen(shipMovement.getY());

        for (int i = 0; i < starCount2; i++) {
            int x = (int) (starX2[i] - shipX * parallax) % screenW;
            int y = (int) ((starY2[i] - shipYscreen * parallax) % screenH);

            if (x < 0) x += screenW;
            if (y < 0) y += screenH;

            g.fillRect(x, y, 3, 3);
        }
    }

    private void drawPlayer(Graphics g) {
        int cx = screenW / 2;
        int cy = screenH / 2;

        g.setColor(0xFF0000);
        g.fillRect(cx - 3, cy - 3, 6, 6);

        float angle = shipMovement.getCurrentAngle();
        double rad = angle * Math.PI / 180.0;
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        int hx = cx + (int) (cos * screenH / 20.0);
        int hy = cy + (int) (WorldUtils.worldToScreen((float) (sin * screenH / 20.0)));
        g.setColor(0xaaaaaa);
        g.drawLine(cx, cy, hx, hy);

        float targetAngle = shipMovement.getTargetAngle();
        double targetRad = targetAngle * Math.PI / 180.0;
        double targetCos = Math.cos(targetRad);
        double targetSin = Math.sin(targetRad);
        int targetHx = cx + (int) (targetCos * screenH / 15);
        int targetHy = cy + (int) (WorldUtils.worldToScreen((float) (targetSin * screenH / 15)));
        g.setColor(0xFFFFFF);
        g.drawLine(cx, cy, targetHx, targetHy);
    }

    private void drawObject(Graphics g) {
        for (int i = 0; i < objects.size(); i++) {
            SpaceObject obj = (SpaceObject) objects.elementAt(i);
            obj.setScale(zoomScale);
            obj.draw(g, shipMovement.getX(), shipMovement.getY(), screenW, screenH);
            int sx = obj.toScreenX(shipMovement.getX(), screenW);
            int sy = obj.toScreenY(shipMovement.getY(), screenH);
            int cr = obj.getClickRadius();

            // yellow: click-selected object
            if (obj == shipStatus.getSelectedObject()) {
                g.setColor(0xFFFF00);
                g.drawArc(sx - cr - 2, sy - cr - 2, (cr + 2) * 2, (cr + 2) * 2, 0, 360);
                g.drawArc(sx - cr - 4, sy - cr - 4, (cr + 4) * 2, (cr + 4) * 2, 0, 360);

                g.setFont(FontUtils.getSmall());
                String name = obj.getDisplayName();
                if (name == null || name.length() == 0) {
                    name = "目标";
                }
                g.drawString(name, sx, sy - cr - 8, Graphics.HCENTER | Graphics.BOTTOM);
            }

            // red: menu-locked object (can coexist with yellow selection)
            if (obj == shipStatus.getLockedObject()) {
                g.setColor(0xFF3333);
                g.drawArc(sx - cr - 7, sy - cr - 7, (cr + 7) * 2, (cr + 7) * 2, 0, 360);
                g.drawArc(sx - cr - 9, sy - cr - 9, (cr + 9) * 2, (cr + 9) * 2, 0, 360);
            }
        }
    }

    private void drawMenu(Graphics g) {
        g.setFont(cachedSmallFont);
        String menuText = "菜单";
        String actionText = "操作";
        int menuHeight = cachedSmallHeight;
        int menuWidth = cachedSmallWidth * 4;
        g.setColor(0xAAAAAA);
        int startX = screenW - menuWidth - 16;
        int startY = screenH - menuHeight - 4;
        g.fillRect(startX, startY, menuWidth + 16, menuHeight + 4);
        g.setColor(0xffffff);
        g.drawString(menuText, startX + 4, startY + 2, Graphics.LEFT | Graphics.TOP);
        g.drawString(actionText, startX + 12 + menuWidth / 2, startY + 2, Graphics.LEFT | Graphics.TOP);
        if (currentMenu != null) {
            currentMenu.draw(g);
            menuAction = true;
        }
    }

    private void updateActiveModules(long deltaMs) {
        if (deltaMs <= 0) {
            return;
        }
        Vector slots = shipStatus.getShipSlot().getMoutingSlots();
        for (int i = 0; i < slots.size(); i++) {
            MountingSlotEquipmentStatus st = (MountingSlotEquipmentStatus) slots.elementAt(i);
            if (st != null) {
                st.update(deltaMs);
            }
        }
    }

    private void drawModulePanel(Graphics g) {
        int menuHeight = cachedSmallHeight + 4;
        int menuWidth = cachedSmallWidth * 4 + 16;
        int menuStartX = screenW - menuWidth - 16;
        int menuStartY = screenH - menuHeight - 4;

        int anchorY = menuStartY - 8;
        // arrow sits flush to right screen edge (use same calc for drawing and hit-testing)
        int arrowX = screenW - modulePanelArrowW - 2;
        int arrowY = anchorY - modulePanelArrowH;

        Vector active = collectActiveModules();

        // if panel is not expanded, only draw the arrow and return
        if (!modulePanelExpanded) {
            g.setColor(0x666666);
            g.fillRoundRect(arrowX, arrowY, modulePanelArrowW, modulePanelArrowH, 4, 4);
            g.setColor(0xFFFFFF);
            g.drawString(modulePanelExpanded ? "<" : ">", arrowX + modulePanelArrowW / 2, arrowY + 2, Graphics.HCENTER | Graphics.TOP);
            return;
        }

        // if expanded but no active modules, still draw arrow and an empty panel background
        if (active.size() == 0) {
            int panelH = 4;
            int panelX = screenW - modulePanelW - 4;
            int panelY = anchorY - panelH;
            g.setColor(0x222222);
            g.fillRoundRect(panelX, panelY, modulePanelW, panelH, 6, 6);
            g.setColor(0xFFFFFF);
            g.drawRoundRect(panelX, panelY, modulePanelW, panelH, 6, 6);

            g.setColor(0x666666);
            g.fillRoundRect(arrowX, arrowY, modulePanelArrowW, modulePanelArrowH, 4, 4);
            g.setColor(0xFFFFFF);
            g.drawString(modulePanelExpanded ? "<" : ">", arrowX + modulePanelArrowW / 2, arrowY + 2, Graphics.HCENTER | Graphics.TOP);
            return;
        }

        int panelH = active.size() * modulePanelItemH + 4;
        int panelX = screenW - modulePanelW - 4;
        int panelY = anchorY - panelH;

        g.setColor(0x222222);
        g.fillRoundRect(panelX, panelY, modulePanelW, panelH, 6, 6);
        g.setColor(0xFFFFFF);
        g.drawRoundRect(panelX, panelY, modulePanelW, panelH, 6, 6);

        g.setFont(cachedSmallFont);
        for (int i = 0; i < active.size(); i++) {
            MountingSlotEquipmentStatus st = (MountingSlotEquipmentStatus) active.elementAt(i);
            int rowY = panelY + 2 + i * modulePanelItemH;
            g.setColor(0x333333);
            g.fillRect(panelX + 2, rowY, modulePanelW - 4, modulePanelItemH - 1);

            // draw icon if available
            int iconX = panelX + 6;
            int iconY = rowY + 2;
            int iconSize = modulePanelItemH - 4;
            if (st.getModel() != null && st.getModel().getItemId() != null) {
                try {
                    javax.microedition.lcdui.Image img = itemImgLoader.getItemImage(st.getModel().getItemId());
                    if (img != null) {
                        // draw image (may be larger; draw at iconX,iconY)
                        g.drawImage(img, iconX, rowY + (modulePanelItemH - img.getHeight()) / 2, Graphics.LEFT | Graphics.TOP);
                    }
                } catch (Throwable t) {
                }
            }

            String name = st.getModel() == null ? "模块" : st.getModel().getName();
            if (name == null || name.length() == 0) {
                name = "模块";
            }
            if (name.length() > 12) {
                name = name.substring(0, 12);
            }
            g.setColor(0xFFFFFF);
            int textX = panelX + 6 + iconSize + 6;
            g.drawString((i + 1) + "." + name, textX, rowY + 2, Graphics.LEFT | Graphics.TOP);

            int cx = panelX + modulePanelW - 14;
            int cy = rowY + modulePanelItemH / 2;
            int r = 6;
            g.setColor(0x777777);
            g.drawArc(cx - r, cy - r, r * 2, r * 2, 0, 360);

            if (!st.isReady()) {
                // compute remaining fraction and progress consistently
                float cooldownFrac = 0f;
                long finalCd = st.getFinalCooldown();
                if (finalCd > 0) {
                    cooldownFrac = (float) st.getCooldownTimer() / (float) finalCd; // 1 -> just fired; 0 -> ready
                }
                int remainAngle = (int) (360f * cooldownFrac);
                g.setColor(0xFF8800);
                // draw remaining arc from top (90) clockwise by remainAngle
                g.drawArc(cx - r, cy - r, r * 2, r * 2, 90, -remainAngle);

                // pointer at the edge of remaining arc (end angle = 90 - remainAngle)
                int pointerAngle = 90 - remainAngle;
                double rad = Math.toRadians(pointerAngle);
                int px = cx + (int) (Math.cos(rad) * r);
                int py = cy + (int) (Math.sin(rad) * r);
                g.drawLine(cx, cy, px, py);
            } else {
                g.setColor(0x66FF66);
                g.fillArc(cx - 2, cy - 2, 4, 4, 0, 360);
            }
        }
        // draw arrow on top so it's always visible and clickable
        g.setColor(0x666666);
        g.fillRoundRect(arrowX, arrowY, modulePanelArrowW, modulePanelArrowH, 4, 4);
        g.setColor(0xFFFFFF);
        g.drawString(modulePanelExpanded ? "<" : ">", arrowX + modulePanelArrowW / 2, arrowY + 2, Graphics.HCENTER | Graphics.TOP);
    }

    private boolean handleModulePanelClick(int x, int y) {
        int menuHeight = cachedSmallHeight + 4;
        int menuWidth = cachedSmallWidth * 4 + 16;
        int menuStartX = screenW - menuWidth - 16;
        int menuStartY = screenH - menuHeight - 4;

        int anchorY = menuStartY - 8;
        int arrowX = screenW - modulePanelArrowW - 2;
        int arrowY = anchorY - modulePanelArrowH;

        if (x >= arrowX && x <= arrowX + modulePanelArrowW && y >= arrowY && y <= arrowY + modulePanelArrowH) {
            modulePanelExpanded = !modulePanelExpanded;
            return true;
        }

        if (!modulePanelExpanded) {
            return false;
        }

        Vector active = collectActiveModules();
        if (active.size() == 0) {
            return false;
        }

        int panelH = active.size() * modulePanelItemH + 4;
        int panelX = screenW - modulePanelW - 4;
        int panelY = anchorY - panelH;

        if (!(x >= panelX && x <= panelX + modulePanelW && y >= panelY && y <= panelY + panelH)) {
            return false;
        }

        int relY = y - (panelY + 2);
        if (relY < 0) {
            return true;
        }
        int index = relY / modulePanelItemH;
        if (index < 0 || index >= active.size()) {
            return true;
        }

        MountingSlotEquipmentStatus st = (MountingSlotEquipmentStatus) active.elementAt(index);
        if (st.tryFire()) {
            String name = st.getModel() == null ? "模块" : st.getModel().getName();
            if (name == null || name.length() == 0) {
                name = "模块";
            }
            float distanceMeters = -1f;
            SpaceObject target = shipStatus.getLockedObject();
            if (target == null) {
                target = shipStatus.getSelectedObject();
            }
            if (target != null) {
                float dx = target.getX() - shipMovement.getX();
                float dy = target.getY() - shipMovement.getY();
                distanceMeters = (float) Math.sqrt(dx * dx + dy * dy) * 1000f;
            }
            setStatus("开火 " + name, distanceMeters, 1000);
        } else {
            setStatus("冷却中", -1, 800);
        }

        return true;
    }

    private Vector collectActiveModules() {
        Vector active = new Vector();
        Vector slots = shipStatus.getShipSlot().getMoutingSlots();
        for (int i = 0; i < slots.size(); i++) {
            MountingSlotEquipmentStatus st = (MountingSlotEquipmentStatus) slots.elementAt(i);
            if (st != null) {
                // prepare image resource if model provides icon URI
                try {
                    if (st.getModel() != null && st.getModel().getIcon() != null) {
                        itemImgLoader.prepareResources(st.getModel().getItemId(), st.getModel().getIcon());
                    }
                } catch (Throwable t) {}
                active.addElement(st);
            }
        }
        return active;
    }

    // getters/setters
    public BaseMenu getCurrentMenu() { return currentMenu; }
    public void setCurrentMenu(BaseMenu menu) { currentMenu = menu; }
    public boolean getMenuAction() { return menuAction; }
    public void setMenuAction(boolean bool) { menuAction = bool; }
    public ShipMovement getPlayerMovement() { return shipMovement; }
    public ShipStatus getShipStatus() { return shipStatus; }
    public void addDialog(BaseDialog dialog) { dialogs.addElement(dialog); }

    // HUD status controls
    public void setStatus(String message, float distance, int durationMs) {
        if (hud != null) hud.setStatus(message, distance, durationMs);
    }

    public void clearStatus() {
        if (hud != null) hud.clearStatus();
    }

}
