/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fierystarrysky;

import fierystarrysky.game.canvas.SettingCanvas;
import fierystarrysky.game.canvas.SpaceCanvas;
import fierystarrysky.util.RMSUtils;
import javax.microedition.lcdui.Display;
import javax.microedition.midlet.*;

/**
 * @author Raven
 */
public class Midlet extends MIDlet {

    private SpaceCanvas spaceCanvas;
    private SettingCanvas settingCanvas;
    private Thread gameThread;
    private boolean running;
    private static Midlet instance;

    public void startApp() {
        instance = this;

        String leftSoftKey = RMSUtils.loadRMS("leftSoftKey");
        String rightSoftKey = RMSUtils.loadRMS("rightSoftKey");
        if (leftSoftKey != null && rightSoftKey != null) {
            if (spaceCanvas == null) {
                showSpaceCanvas();
            }
        } else {
            settingCanvas = new SettingCanvas();
            Display.getDisplay(this).setCurrent(settingCanvas);
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public static Midlet getInstance() {
        return instance;
    }

    public void showSpaceCanvas() {
        if (spaceCanvas == null) {
            spaceCanvas = new SpaceCanvas(this);
            running = true;
            gameThread = new Thread(spaceCanvas);
            gameThread.start();
        }
        Display.getDisplay(this).setCurrent(spaceCanvas);
    }
}
