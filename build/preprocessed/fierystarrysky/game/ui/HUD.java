package fierystarrysky.game.ui;

import fierystarrysky.game.status.ship.ShipMovement;
import fierystarrysky.game.status.ship.ShipStatus;
import res.image.loader.SpaceDashBoardImageLoader;
import fierystarrysky.util.ColorUtils;
import fierystarrysky.util.FontUtils;
import javax.microedition.lcdui.*;

public class HUD {

    private ShipStatus shipStatus;
    private ShipMovement shipMovement;
    private SpaceDashBoardImageLoader imgs;

    // status box
    private String statusMessage = null;
    private float statusDistance = -1f;
    private long statusExpireTime = 0;

    private Font cachedSmallFont;
    private int cachedSmallWidth;
    private int cachedSmallHeight;

    public HUD(ShipStatus shipStatus, ShipMovement shipMovement, SpaceDashBoardImageLoader imgs, Font smallFont, int smallWidth, int smallHeight) {
        this.shipStatus = shipStatus;
        this.shipMovement = shipMovement;
        this.imgs = imgs;
        this.cachedSmallFont = smallFont;
        this.cachedSmallWidth = smallWidth;
        this.cachedSmallHeight = smallHeight;
    }

    public void update(float delta) {
        // future: HUD animations, timeouts etc.
    }

    public void render(Graphics g, int screenW, int screenH) {
        drawShipInterface(g, screenW, screenH);
        drawStatusBox(g, screenW, screenH);
    }

    private void drawShipInterface(Graphics g, int screenW, int screenH) {
        if (shipMovement.getMode() == ShipMovement.AUTO_ORBIT) {
            g.setColor(ColorUtils.grayBlue);
            int radius = (int) (shipMovement.getTargetDistance() * SpaceCanvasCompat.zoomScale);
            g.drawArc(shipStatus.getTargetObject().toScreenX(shipMovement.getX(), screenW) - radius, shipStatus.getTargetObject().toScreenY(shipMovement.getY(), screenH) - radius, radius * 2, radius * 2, 0, 360);
            radius -= 1;
            g.drawArc(shipStatus.getTargetObject().toScreenX(shipMovement.getX(), screenW) - radius, shipStatus.getTargetObject().toScreenY(shipMovement.getY(), screenH) - radius, radius * 2, radius * 2, 0, 360);
        }

        int drawX, drawY;
        int fontHeight = cachedSmallHeight + 4;
        int dashboardSize = (int) (screenW * 0.25f);
        drawX = 0 - dashboardSize;
        drawY = screenH - dashboardSize - fontHeight;

        g.setColor(ColorUtils.sliverGray);
        g.fillArc(drawX, drawY, dashboardSize * 2, dashboardSize * 2, 0, 90);
        g.setColor(ColorUtils.ironGray);
        g.fillArc(drawX + 1, drawY + 1, dashboardSize * 2 - 2, dashboardSize * 2 - 2, 0, 90);
        g.setColor(ColorUtils.shieldBlue);
        g.fillArc(drawX + 1, drawY + 1, dashboardSize * 2 - 2, dashboardSize * 2 - 2, 45, 45);
        g.setColor(ColorUtils.armorGray);
        g.fillArc(drawX + 1, drawY + 1, dashboardSize * 2 - 2, dashboardSize * 2 - 2, 0, 45);

        drawX += 8;
        drawY += 8;
        dashboardSize -= 8;
        g.setColor(ColorUtils.sliverGray);
        g.fillArc(drawX, drawY, dashboardSize * 2, dashboardSize * 2, 0, 90);
        g.setColor(ColorUtils.ironGray);
        g.fillArc(drawX + 1, drawY + 1, dashboardSize * 2 - 2, dashboardSize * 2 - 2, 0, 90);
        g.setColor(ColorUtils.deepRed);
        g.fillArc(drawX + 1, drawY + 1, dashboardSize * 2 - 2, dashboardSize * 2 - 2, 45, 45);
        g.setColor(ColorUtils.powerYellow);
        g.fillArc(drawX + 1, drawY + 1, dashboardSize * 2 - 2, dashboardSize * 2 - 2, 0, 45);

        g.setColor(ColorUtils.sliverGray);
        drawX += 7;
        drawY += 7;
        dashboardSize -= 7;
        g.fillArc(drawX, drawY, dashboardSize * 2, dashboardSize * 2, 0, 90);
        g.setColor(ColorUtils.grayBlue);
        g.fillArc(drawX + 1, drawY + 1, dashboardSize * 2 - 2, dashboardSize * 2 - 2, 0, 90);
        float throttle = shipStatus.getThrottle() / 100f;

        g.drawImage(imgs.getPowerIcon(), dashboardSize - 16, screenH - fontHeight, Graphics.BOTTOM | Graphics.LEFT);
        g.drawImage(imgs.getArmorIcon(), dashboardSize + 16, screenH - fontHeight, Graphics.BOTTOM | Graphics.LEFT);
        g.drawImage(imgs.getShieldIcon(), 0, screenH - fontHeight - dashboardSize - 15, Graphics.BOTTOM | Graphics.LEFT);
        g.drawImage(imgs.getStructureIcon(), 0, screenH - fontHeight - dashboardSize + 4, Graphics.TOP | Graphics.LEFT);

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

        String speedString = String.valueOf((int) (currentSpeed * 1000));
        String speedText = speedString + "m/s";
        int speedWidth = cachedSmallWidth * 5;
        g.setColor(ColorUtils.white);
        if (speedWidth < dashboardSize + 16) {
            speedWidth = dashboardSize + 16;
        }
        g.fillRect(0, screenH - fontHeight, speedWidth, fontHeight);
        g.setColor(ColorUtils.black);
        g.fillRect(1, screenH - fontHeight + 1, speedWidth - 2, fontHeight - 2);
        g.setColor(ColorUtils.white);
        int pointerYtext = screenH - fontHeight / 2 - cachedSmallHeight / 2;
        g.drawString(speedText, speedWidth / 2, pointerYtext, Graphics.HCENTER | Graphics.TOP);
    }

    private void drawStatusBox(Graphics g, int screenW, int screenH) {
        if (statusMessage == null) return;
        if (statusExpireTime > 0 && System.currentTimeMillis() > statusExpireTime) {
            statusMessage = null;
            statusDistance = -1f;
            statusExpireTime = 0;
            return;
        }

        String text = statusMessage;
        if (statusDistance >= 0f) {
            text = text + " " + formatDistance(statusDistance);
        }

        Font f = (cachedSmallFont != null) ? cachedSmallFont : FontUtils.getSmall();
        int fh = (cachedSmallHeight > 0) ? cachedSmallHeight : FontUtils.getSmallHeight();
        g.setFont(f);

        int paddingX = 6;
        int paddingY = 4;
        int textWidth = cachedSmallWidth * text.length();
        int boxW = textWidth + paddingX * 2;
        int boxH = fh + paddingY * 2;

        int centerX = screenW / 2;
        int x = centerX - boxW / 2;
        int y = screenH - boxH - 6;

        g.setColor(0x333333);
        g.fillRoundRect(x, y, boxW, boxH, 6, 6);
        g.setColor(0xFFFFFF);
        g.drawRoundRect(x, y, boxW, boxH, 6, 6);
        g.setColor(0xFFFFFF);
        int tx = x + paddingX;
        int ty = y + paddingY;
        g.drawString(text, tx, ty, Graphics.LEFT | Graphics.TOP);
    }

    private String formatDistance(float meters) {
        if (meters < 1000f) {
            return ((int) meters) + "m";
        } else {
            float km = meters / 1000f;
            int whole = (int) km;
            int dec = (int) ((km - whole) * 10);
            return whole + "." + dec + "km";
        }
    }

    public void setStatus(String message, float distance, int durationMs) {
        this.statusMessage = message;
        this.statusDistance = distance;
        if (durationMs > 0) {
            this.statusExpireTime = System.currentTimeMillis() + durationMs;
        } else {
            this.statusExpireTime = 0;
        }
    }

    public void clearStatus() {
        this.statusMessage = null;
        this.statusDistance = -1f;
        this.statusExpireTime = 0;
    }

    // compatibility holder for static zoomScale from SpaceCanvas
    public static class SpaceCanvasCompat {
        public static float zoomScale = 10f;
    }
}
