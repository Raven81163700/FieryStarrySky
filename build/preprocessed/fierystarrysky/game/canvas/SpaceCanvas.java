/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import fierystarrysky.game.ui.dialog.BaseDialog;
import fierystarrysky.game.ui.menu.space.ActionMenu;
import fierystarrysky.util.ColorUtils;
import fierystarrysky.util.FontUtils;
import fierystarrysky.util.RMSUtils;
import fierystarrysky.util.WorldUtils;
import java.util.Vector;
import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.GameCanvas;
import res.config.loader.ShipInfoLoader;
import res.image.loader.SpaceDashBoardImageLoader;

/**
 *
 * @author Raven
 */
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
    // reuse menu instances to avoid allocations on key press
    private CommonMenu commonMenuInstance;
    private ActionMenu actionMenuInstance;
    private int[] starX1, starY1; // 底层星点
    private int[] starX2, starY2; // 前景星点
    private int[] starSize1;        // 星星大小（1~2像素）
    private int[] starBrightness1;  // 星星亮度（0x88~0xFF）
    private int starCount1 = 50;  // 底层星点数量
    private int starCount2 = 15;  // 前景星点数量
    //dialog列表
    private Vector dialogs = new Vector();
    //图像加载器
    private SpaceDashBoardImageLoader dashboardRes;

    public SpaceCanvas(Midlet midlet) {
        super(true);
        this.midlet = midlet;

        //初始化仪表盘图像引用
        dashboardRes = new SpaceDashBoardImageLoader();

        //初始化左右软键
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

        // cache font metrics to avoid repeated calls each frame
        cachedSmallFont = FontUtils.getSmall();
        cachedSmallWidth = FontUtils.getSmallWidth();
        cachedSmallHeight = FontUtils.getSmallHeight();
        COMMON_MENU_WIDTH = cachedSmallWidth * 6 - 4;

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
    }

    public void clearMenu() {
        currentMenu = null;
        menuAction = false;
    }

    protected void pointerPressed(int x, int y) {
        if (menuAction) {
            if (currentMenu != null) {
                int itemIndex = currentMenu.getItemAt(x, y);
                if (itemIndex == -1) {
                    clearMenu();
                } else {
                    //执行点击事件
                    currentMenu.select(this);
                }
                return;
            }
            clearMenu();
        }
        //右下角菜单点击范围
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
        for (int i = 0; i < objects.size(); i++) {
            SpaceObject obj = (SpaceObject) objects.elementAt(i);
            if (obj.isClicked(x, y, shipMovement.getX(), shipMovement.getY(), screenW, screenH)) {
                obj.onClick(this, screenW, screenH);
            }
        }
    }

    public void run() {
        final int targetFrameMs = 20; // target ~50fps (keep original timing)
        Graphics g = null;
        while (midlet.isRunning()) {
            long frameStart = System.currentTimeMillis();
            try {
                if (g == null) {
                    g = getGraphics();
                }
                input();   // 处理输入
                update();  // 更新逻辑
                try {
                    render(g); // 绘制
                } catch (Throwable t) {
                    // prevent render exceptions from killing the thread
                    t.printStackTrace();
                }
                flushGraphics();

                long elapsed = System.currentTimeMillis() - frameStart;
                long sleepMs = targetFrameMs - elapsed;
                if (sleepMs > 0) {
                    try {
                        Thread.sleep(sleepMs);
                    } catch (InterruptedException ie) {
                        // stop the loop on interrupt
                        break;
                    }
                } else {
                    // too slow, yield to allow other threads to run
                    Thread.yield();
                }
            } catch (Throwable ex) {
                // top-level catch to avoid silent thread death
                ex.printStackTrace();
            }
        }
    }

    protected void keyPressed(int keyCode) {
        if (keyCode == leftSoftKey) {
            // 左软键被按下
            onLeftSoftKey();
        } else if (keyCode == rightSoftKey) {
            // 右软键被按下
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
    //菜单的硬键盘操作

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
        //将移动信息同步到ShipStatus
        shipStatus.setThrottle(shipMovement.getThrottle());
        shipStatus.setCurrentAngle(shipMovement.getCurrentAngle());
        shipStatus.setCurrentX(shipMovement.getX());
        shipStatus.setCurrentY(shipMovement.getY());
        shipStatus.setCurrentSpeed(shipMovement.getCurrentSpeed());

        long nowTime = System.currentTimeMillis();
        float deltaTime = (lastTimestamp == 0L ? 0 : (nowTime - lastTimestamp) / 1000f);
        lastTimestamp = nowTime;

        shipMovement.update(deltaTime);

    }

    private void render(Graphics g) {
        drawBackground(g);
        drawObject(g);
        drawPlayer(g);
        drawShipInterface(g);
        drawMenu(g);
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
            // 星星大小：1 或 2 像素
            starSize1[i] = 1 + rand.nextInt(2);

            // 星星亮度：0x88~0xFF，越亮越接近白色
            starBrightness1[i] = 0x88 + rand.nextInt(0x77);
        }
    }

    private void drawBackground(Graphics g) {
        // 填充黑色背景
        g.setColor(0x000000);
        g.fillRect(0, 0, screenW, screenH);

        // ---------------------
        // 底层背景板（远处，不动）
        // ---------------------
        for (int i = 0; i < starCount1; i++) {
            int brightness = starBrightness1[i];
            int color = (brightness << 16) | (brightness << 8) | brightness;
            g.setColor(color);
            g.fillRect(starX1[i], starY1[i], starSize1[i], starSize1[i]);
        }

        // ---------------------
        // 前景星层（随移动，视差效果）
        // ---------------------
        g.setColor(0xFFFFFF);
        float parallax = 0.5f * zoomScale;
        // cache movement values to avoid repeated method calls
        float shipX = shipMovement.getX();
        float shipYscreen = WorldUtils.worldToScreen(shipMovement.getY());

        for (int i = 0; i < starCount2; i++) {
            int x = (int) (starX2[i] - shipX * parallax) % screenW;
            int y = (int) ((starY2[i] - shipYscreen * parallax) % screenH);

            if (x < 0) {
                x += screenW;
            }
            if (y < 0) {
                y += screenH;
            }

            g.fillRect(x, y, 3, 3);
        }
    }

    // 玩家始终在屏幕中心
    private void drawPlayer(Graphics g) {
        // 飞船中心点
        int cx = screenW / 2;
        int cy = screenH / 2;

        // 绘制船体（红色方块）
        g.setColor(0xFF0000);
        g.fillRect(cx - 3, cy - 3, 6, 6);

        // 船头方向（灰色短线）
        float angle = shipMovement.getCurrentAngle(); // float
        double rad = angle * Math.PI / 180.0;
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        int hx = cx + (int) (cos * screenH / 20.0);
        int hy = cy + (int) (WorldUtils.worldToScreen((float) (sin * screenH / 20.0)));
        g.setColor(0xaaaaaa);
        g.drawLine(cx, cy, hx, hy);
        // 船头目标方向（白色短线）
        float targetAngle = shipMovement.getTargetAngle();
        double targetRad = targetAngle * Math.PI / 180.0;
        double targetCos = Math.cos(targetRad);
        double targetSin = Math.sin(targetRad);
        int targetHx = cx + (int) (targetCos * screenH / 15);
        int targetHy = cy + (int) (WorldUtils.worldToScreen((float) (targetSin * screenH / 15)));
        g.setColor(0xFFFFFF);
        g.drawLine(cx, cy, targetHx, targetHy);
    }
    //绘制物品

    private void drawObject(Graphics g) {
        for (int i = 0; i < objects.size(); i++) {
            SpaceObject obj = (SpaceObject) objects.elementAt(i);
            obj.setScale(zoomScale);
            obj.draw(g, shipMovement.getX(), shipMovement.getY(), screenW, screenH);
        }
    }

    private void drawMenu(Graphics g) {
        //绘制默认菜单
        g.setFont(cachedSmallFont);
        String menuText = "菜单";
        String actionText = "操作";
        int menuHeight = cachedSmallHeight;
        int menuWidth = cachedSmallWidth * 4;
        //从右下角开始
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
    //绘制舰船仪表盘

    private void drawShipInterface(Graphics g) {
        //如果在环绕，绘制环绕航路
        if (shipMovement.getMode() == ShipMovement.AUTO_ORBIT) {
            g.setColor(ColorUtils.grayBlue);
            int radius = (int) (shipMovement.getTargetDistance() * zoomScale);
            g.drawArc(shipStatus.getTargetObject().toScreenX(shipMovement.getX(), screenW) - radius, shipStatus.getTargetObject().toScreenY(shipMovement.getY(), screenH) - radius, radius * 2, radius * 2, 0, 360);
            radius -= 1;
            g.drawArc(shipStatus.getTargetObject().toScreenX(shipMovement.getX(), screenW) - radius, shipStatus.getTargetObject().toScreenY(shipMovement.getY(), screenH) - radius, radius * 2, radius * 2, 0, 360);
        }


        //求出绘图点
        int drawX, drawY;
        //速度表盘边框绘图点
        int fontHeight = cachedSmallHeight + 4;
        //屏幕自适应大小
        int dashboardSize = (int) (screenW * 0.25f);
        drawX = 0 - dashboardSize;
        drawY = screenH - dashboardSize - fontHeight;
        //护盾和装甲 
        //边框
        g.setColor(ColorUtils.sliverGray);
        g.fillArc(drawX, drawY, dashboardSize * 2, dashboardSize * 2, 0, 90);
        //状态条背景色
        g.setColor(ColorUtils.ironGray);
        g.fillArc(drawX + 1, drawY + 1, dashboardSize * 2 - 2, dashboardSize * 2 - 2, 0, 90);
        //TODO:计算装甲值和护盾值的弧度
        g.setColor(ColorUtils.shieldBlue);
        g.fillArc(drawX + 1, drawY + 1, dashboardSize * 2 - 2, dashboardSize * 2 - 2, 45, 45);
        g.setColor(ColorUtils.armorGray);
        g.fillArc(drawX + 1, drawY + 1, dashboardSize * 2 - 2, dashboardSize * 2 - 2, 0, 45);

        //偏移，绘制电力
        drawX += 8;
        drawY += 8;
        dashboardSize -= 8;
        //电力边框
        g.setColor(ColorUtils.sliverGray);
        g.fillArc(drawX, drawY, dashboardSize * 2, dashboardSize * 2, 0, 90);
        //状态条背景
        g.setColor(ColorUtils.ironGray);
        g.fillArc(drawX + 1, drawY + 1, dashboardSize * 2 - 2, dashboardSize * 2 - 2, 0, 90);
        //结构
        g.setColor(ColorUtils.deepRed);
        g.fillArc(drawX + 1, drawY + 1, dashboardSize * 2 - 2, dashboardSize * 2 - 2, 45, 45);
        //电力
        g.setColor(ColorUtils.powerYellow);
        //TODO 根据百分比求0-45角度
        g.fillArc(drawX + 1, drawY + 1, dashboardSize * 2 - 2, dashboardSize * 2 - 2, 0, 45);
        //绘制速度仪表盘
        g.setColor(ColorUtils.sliverGray);
        drawX += 7;
        drawY += 7;
        dashboardSize -= 7;
        g.fillArc(drawX, drawY, dashboardSize * 2, dashboardSize * 2, 0, 90);
        g.setColor(ColorUtils.grayBlue);
        g.fillArc(drawX + 1, drawY + 1, dashboardSize * 2 - 2, dashboardSize * 2 - 2, 0, 90);
        float throttle = shipStatus.getThrottle() / 100f;
        //电力图标绘制于指针之下
        g.drawImage(dashboardRes.getPowerIcon(), dashboardSize - 16, screenH - fontHeight, Graphics.BOTTOM | Graphics.LEFT);
        g.drawImage(dashboardRes.getArmorIcon(), dashboardSize + 16, screenH - fontHeight, Graphics.BOTTOM | Graphics.LEFT);
        g.drawImage(dashboardRes.getShieldIcon(), 0, screenH - fontHeight - dashboardSize - 15, Graphics.BOTTOM | Graphics.LEFT);
        g.drawImage(dashboardRes.getStructureIcon(), 0, screenH - fontHeight - dashboardSize + 4, Graphics.TOP | Graphics.LEFT);
        //根据百分比在仪表盘定指针
        int pointerX = 0 + (int) (dashboardSize * 0.8 * Math.sin(Math.toRadians(85 * throttle)));
        int pointerY = screenH - fontHeight - (int) (dashboardSize * 0.8 * Math.cos(Math.toRadians(85 * throttle)));
        g.setColor(ColorUtils.fluorescentGreen);
        g.drawLine(0, screenH - fontHeight, pointerX, pointerY);
        float currentSpeed = shipStatus.getCurrentSpeed();
        float speedPercent = currentSpeed / shipStatus.getMaxSpeed();
        pointerX = 0 + (int) (dashboardSize * 0.6 * Math.sin(Math.toRadians(85 * speedPercent)));
        pointerY = screenH - fontHeight - (int) (dashboardSize * 0.6 * Math.cos(Math.toRadians(85 * speedPercent)));
        g.setColor(ColorUtils.orangeRed);
        g.drawLine(0, screenH - fontHeight, pointerX, pointerY);
        g.drawLine(0 - 1, screenH - fontHeight, pointerX - 1, pointerY);
        g.drawLine(0 + 1, screenH - fontHeight, pointerX + 1, pointerY);
        //速度条
        String speedString = String.valueOf((int) (currentSpeed * 1000));
        String speedText = speedString + "m/s";
        int speedWidth = cachedSmallWidth * 5;
        g.setColor(ColorUtils.white);
        if (speedWidth < dashboardSize + 16){
            speedWidth = dashboardSize + 16;
        }
        g.fillRect(0, screenH - fontHeight, speedWidth, fontHeight);
        g.setColor(ColorUtils.black);
        g.fillRect(1, screenH - fontHeight + 1, speedWidth - 2, fontHeight - 2);
        g.setColor(ColorUtils.white);
        pointerY = screenH - fontHeight / 2 - cachedSmallHeight / 2;
        g.drawString(speedText, speedWidth / 2, pointerY, Graphics.HCENTER | Graphics.TOP);

        if (dialogs.isEmpty()) {
            return;
        }
        //绘制弹窗
        BaseDialog dialog = (BaseDialog) dialogs.elementAt(0);
        if (!dialog.isShow() && dialog.getType() == BaseDialog.TYPE_INFO) {
            dialog.setCreateTime(System.currentTimeMillis());
            dialog.setShow(true);
        }
        if (dialog.isClosed()) {
            dialogs.removeElementAt(0);
        }
        dialog.draw(g, screenW, screenH);
    }

    //getter/setter
    public BaseMenu getCurrentMenu() {
        return currentMenu;
    }

    public void setCurrentMenu(BaseMenu menu) {
        currentMenu = menu;
    }

    public boolean getMenuAction() {
        return menuAction;
    }

    public void setMenuAction(boolean bool) {
        menuAction = bool;
    }

    public ShipMovement getPlayerMovement() {
        return shipMovement;
    }

    public ShipStatus getShipStatus() {
        return shipStatus;
    }

    public void addDialog(BaseDialog dialog) {
        dialogs.addElement(dialog);
    }
}
