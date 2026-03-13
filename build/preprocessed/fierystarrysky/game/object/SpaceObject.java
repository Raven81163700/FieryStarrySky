/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fierystarrysky.game.object;

import fierystarrysky.game.canvas.SpaceCanvas;
import fierystarrysky.util.WorldUtils;
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author Raven
 */
public abstract class SpaceObject {

    protected float x;     // 世界坐标 X
    protected float y;     // 世界坐标 Y
    protected int size;    // 尺寸
    protected int color;   // 绘制颜色
    protected int clickRadius = 30;
    protected float zoomScale = 1f; //比例  多少像素用于显示1km

    public SpaceObject(float x, float y, int size, int color) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.color = color;
    }

    // 判断是否在屏幕范围内
    public boolean isVisible(int screenX, int screenY, int screenW, int screenH) {
        int scaledSize = (int) (size * zoomScale);
        return screenX + scaledSize > 0
                && screenX < screenW
                && screenY + scaledSize > 0
                && screenY < screenH;
    }

    public void setScale(float scale) {
        this.zoomScale = scale;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    // 世界坐标 → 屏幕坐标
    public int toScreenX(float playerX, int screenW) {
        return (int) ((x - playerX) * zoomScale + screenW / 2);
    }

    public int toScreenY(float playerY, int screenH) {
        return (int)((int) WorldUtils.worldToScreen((y - playerY) * zoomScale) + screenH / 2);
    }

    public boolean isClicked(int clickX, int clickY, float playerX, float playerY, int screenW, int screenH) {
        int screenX = toScreenX(playerX, screenW);
        int screenY = toScreenY(playerY, screenH);
        return screenX - clickRadius < clickX && screenX + clickRadius > clickX && screenY - clickRadius < clickY && screenY + clickRadius > clickY;
    }

    protected void drawClickCircle(Graphics g, float playerX, float playerY, int screenW, int screenH) {
        int screenX = toScreenX(playerX, screenW);
        int screenY = toScreenY(playerY, screenH);
        float dx = x - playerX;
        float dy = y - playerY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        String distanceText;
        if (distance >= 10) {
            distanceText = (int) distance + "km"; // 1 km 或以上直接显示 km
        } else {
            distanceText = (int) (distance * 1000) + "m"; // 小于1 km 转换成 m
        }
        g.setColor(0xFFFFFF);
        g.drawArc(screenX - clickRadius, screenY - clickRadius, clickRadius * 2, clickRadius * 2, 0, 360);
        g.drawString(distanceText, screenX, screenY, Graphics.HCENTER | Graphics.TOP);
    }

    // 子类必须实现的绘制逻辑
    public abstract void draw(Graphics g, float playerX, float playerY, int screenW, int screenH);

    public abstract void onClick(SpaceCanvas canvas, int screenW, int screenH);

    /**
     * 返回用于界面显示的名称，子类可覆盖
     */
    public String getDisplayName() {
        return "";
    }

    public int getClickRadius() {
        return clickRadius;
    }
}
