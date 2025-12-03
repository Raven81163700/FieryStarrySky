/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fierystarrysky.game.object.space;

import fierystarrysky.game.canvas.SpaceCanvas;
import fierystarrysky.game.ui.menu.space.ObjectMenu;
import fierystarrysky.game.object.SpaceObject;
import fierystarrysky.util.FontUtils;
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author Raven
 */
public class TestTarget extends SpaceObject {

    private String name;

    public TestTarget(float x, float y, int size, String name) {
        super(x, y, size, 0x00FFFF);
        this.name = name;
    }

    public void draw(Graphics g, float playerX, float playerY, int screenW, int screenH) {
        int screenX = toScreenX(playerX, screenW);
        int screenY = toScreenY(playerY, screenH);

        if (isVisible(screenX, screenY, screenW, screenH)) {
            g.setColor(color);
            g.fillRect(screenX - size / 2, screenY - size / 2, size, size);
            g.setColor(0xffffff);
            g.setFont(FontUtils.getSmall());
            g.drawString(name, screenX, screenY, Graphics.HCENTER | Graphics.BASELINE);

            super.drawClickCircle(g, playerX, playerY, screenW, screenH);
        }
    }

    public void onClick(SpaceCanvas canvas, int screenW, int screenH) {
        ObjectMenu objectMenu = new ObjectMenu(0, 0, FontUtils.getSmallWidth() * 2 + 16, FontUtils.getMediumHeight() + 8, this);
        objectMenu.setDrawHeader(true);
        objectMenu.setTitle(name);
        objectMenu.setHeaderH(FontUtils.getSmallHeight() + 8);
        objectMenu.setHeaderW(FontUtils.getSmallWidth() * name.length() + 8);
        canvas.setCurrentMenu(objectMenu);
    }
}
