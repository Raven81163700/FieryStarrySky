/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fierystarrysky.game.canvas;

import fierystarrysky.Midlet;
import fierystarrysky.util.RMSUtils;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author Raven
 */
public class SettingCanvas extends Canvas {

    private int step = 0;
    private int leftSoftKeyCode;
    private int rightSoftKeyCode;

    protected void paint(Graphics g) {
        g.setColor(0);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(0xFFFFFF);
        g.drawString("触摸屏幕任意位置跳过设置", getWidth() / 2, getHeight() - 1, Graphics.BASELINE | Graphics.HCENTER);
        if (step == 0) {
            g.drawString("请按下左软键", getWidth() / 2, getHeight() / 2, Graphics.HCENTER | Graphics.BASELINE);
        } else if (step == 1) {
            g.drawString("请按下右软键", getWidth() / 2, getHeight() / 2, Graphics.HCENTER | Graphics.BASELINE);
        } else {
            g.drawString("设置完成！", getWidth() / 2, getHeight() / 2, Graphics.HCENTER | Graphics.BASELINE);
        }
    }
    protected void pointerPressed(int x, int y) {
        skipSetting();
    }

    protected void keyPressed(int keyCode) {
        if (step == 0) {
            leftSoftKeyCode = keyCode;
            step = 1;
        } else if (step == 1) {
            rightSoftKeyCode = keyCode;
            step = 2;
            // 这里可以保存到RMS
            saveSoftKeys(leftSoftKeyCode, rightSoftKeyCode);
            Midlet.getInstance().showSpaceCanvas();
        }
        repaint();
    }

    private void skipSetting() {
        step = 2;
        saveSoftKeys(-999, -999);
        Midlet.getInstance().showSpaceCanvas();
    }

    private void saveSoftKeys(int left, int right) {
        RMSUtils.saveRMS("leftSoftKey", String.valueOf(left));
        RMSUtils.saveRMS("rightSoftKey", String.valueOf(right));
    }
}
