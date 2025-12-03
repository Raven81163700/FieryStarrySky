/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package res.image.loader;

import javax.microedition.lcdui.Image;

/**
 *
 * @author Raven
 */
public class SpaceDashBoardImageLoader {

    private Image powerIcon = null;
    private Image shieldIcon = null;
    private Image armorIcon = null;
    private Image structureIcon = null;

    private void loadPowerIcon() {
        try {
            powerIcon = Image.createImage("/res/image/power.png");
        } catch (Exception e) {
        }
    }

    private void loadShieldIcon() {
        try {
            shieldIcon = Image.createImage("/res/image/shield.png");
        } catch (Exception e) {
        }
    }

    private void loadArmorIcon() {
        try {
            armorIcon = Image.createImage("/res/image/armor.png");
        } catch (Exception e) {
        }
    }

    private void loadStructureIcon() {
        try {
            structureIcon = Image.createImage("/res/image/structure.png");
        } catch (Exception e) {
        }
    }

    public Image getPowerIcon() {
        if (powerIcon == null) {
            loadPowerIcon();
        }
        return powerIcon;
    }

    public Image getShieldIcon() {
        if (shieldIcon == null) {
            loadShieldIcon();
        }
        return shieldIcon;
    }

    public Image getArmorIcon() {
        if (armorIcon == null) {
            loadArmorIcon();
        }
        return armorIcon;
    }

    public Image getStructureIcon() {
        if (structureIcon == null) {
            loadStructureIcon();
        }
        return structureIcon;
    }
}
