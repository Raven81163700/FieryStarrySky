package com.aurora.scene;

import javax.microedition.lcdui.Displayable;

public interface Scene {
    Displayable asDisplayable();
    void onEnter();
    void onExit();
}
