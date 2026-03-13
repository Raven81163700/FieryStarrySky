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
public class SpaceStation extends SpaceObject {

    private String name;

    public SpaceStation(float x, float y, int size, String name) {
        super(x, y, size, 0x00FF00);
        this.name = name;
    }

    public void draw(Graphics g, float playerX, float playerY, int screenW, int screenH) {
        int screenX = toScreenX(playerX, screenW);
        int screenY = toScreenY(playerY, screenH);

        if (isVisible(screenX, screenY, screenW, screenH)) {
            g.setColor(color);
            int scaledSize = (int) (size * zoomScale);
            g.fillRect(screenX - scaledSize / 2, screenY - scaledSize / 2, scaledSize, scaledSize);
            g.setColor(0xffffff);
            g.setFont(FontUtils.getSmall());
            g.drawString(name, screenX, screenY, Graphics.HCENTER | Graphics.BASELINE);
            
            super.drawClickCircle(g, playerX, playerY, screenW, screenH);
        }
    }
    
    public void onClick(SpaceCanvas canvas, int screenW, int screenH){
        // Keep selection in sync even if menu is opened from other entry points.
        canvas.getShipStatus().setSelectedObject(this);
        ObjectMenu objectMenu = new ObjectMenu(0, 0, FontUtils.getSmallWidth() * 2 + 16, FontUtils.getMediumHeight() + 8, this);
        objectMenu.setDrawHeader(true);
        objectMenu.setTitle(name);
        objectMenu.setHeaderH(FontUtils.getSmallHeight() + 8);
        objectMenu.setHeaderW(FontUtils.getSmallWidth() * name.length() + 8);
        canvas.setCurrentMenu(objectMenu);
    }

    public String getDisplayName() {
        return name;
    }
}
