package com.aurora.game;

import com.aurora.auth.AuthService;
import com.aurora.net.PingService;
import com.aurora.scene.SceneManager;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * Aurora Online Game - 主MIDlet入口
 *
 * JavaME MIDP 2.1 / CLDC 1.1
 *
 * 生命周期:
 *   startApp()   - MIDlet启动/从暂停恢复
 *   pauseApp()   - MIDlet被系统暂停
 *   destroyApp() - MIDlet销毁
 *
 * 当前实现: 自绘登录/注册界面，输入使用 JavaME TextBox 组件
 */
public class AuroraGameMIDlet extends MIDlet {

    private static final String DEFAULT_SERVER_HOST = "127.0.0.1";
    private static final int DEFAULT_SERVER_PORT = 9000;
    private static final String PROP_SERVER_HOST = "Aurora-Server-Host";
    private static final String PROP_SERVER_PORT = "Aurora-Server-Port";

    private Display display;
    private SceneManager sceneManager;

    protected void startApp() throws MIDletStateChangeException {
        if (display == null) {
            display = Display.getDisplay(this);
            String serverHost = readAppProperty(PROP_SERVER_HOST, DEFAULT_SERVER_HOST);
            int serverPort = readAppPropertyInt(PROP_SERVER_PORT, DEFAULT_SERVER_PORT);
            AuthService authService = new AuthService(serverHost, serverPort);
            PingService pingService = new PingService(serverHost, serverPort);
            sceneManager = new SceneManager(this, display, authService, pingService);
        }
        sceneManager.resume();
    }

    private String readAppProperty(String key, String defaultValue) {
        String value = getAppProperty(key);
        if (value == null) {
            return defaultValue;
        }
        value = value.trim();
        if (value.length() == 0) {
            return defaultValue;
        }
        return value;
    }

    private int readAppPropertyInt(String key, int defaultValue) {
        String value = getAppProperty(key);
        if (value == null) {
            return defaultValue;
        }
        value = value.trim();
        if (value.length() == 0) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    protected void pauseApp() {
        if (sceneManager != null) {
            sceneManager.pause();
        }
    }

    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        if (sceneManager != null) {
            sceneManager.pause();
        }
    }
}
