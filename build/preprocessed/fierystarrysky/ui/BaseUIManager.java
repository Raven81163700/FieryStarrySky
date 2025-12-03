/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fierystarrysky.ui;

import fierystarrysky.Midlet;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

/**
 *
 * @author chenb
 */
public abstract class BaseUIManager {

    protected final Canvas returnCanvas;

    public BaseUIManager(Canvas canvas) {
        this.returnCanvas = canvas;
    }

    /**
     * 显示某个 Displayable（由子类提供）
     */
    protected void show(Displayable d) {
        Display.getDisplay(Midlet.getInstance()).setCurrent(d);
    }

    /**
     * 返回到原 Canvas
     */
    protected void backToCanvas() {
        if (returnCanvas != null) {
            Display.getDisplay(Midlet.getInstance()).setCurrent(returnCanvas);
        }
    }
}
