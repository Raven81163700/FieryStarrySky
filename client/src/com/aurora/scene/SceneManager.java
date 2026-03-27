package com.aurora.scene;

import com.aurora.auth.AuthService;
import com.aurora.net.PingService;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

public final class SceneManager {

    private final MIDlet midlet;
    private final Display display;
    private final AuthService authService;
    private final PingService pingService;
    private final String serverHost;
    private final int serverPort;
    private final com.aurora.net.CharacterService characterService;
    private final com.aurora.net.StarMapService starMapService;

    private Scene current;

    public SceneManager(MIDlet midlet, Display display, AuthService authService, PingService pingService, String serverHost, int serverPort) {
        this.midlet = midlet;
        this.display = display;
        this.authService = authService;
        this.pingService = pingService;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.characterService = new com.aurora.net.CharacterService(serverHost, serverPort);
        this.starMapService = new com.aurora.net.StarMapService(serverHost, serverPort);
    }

    public void showLoginScene() {
        switchTo(new LoginScene(this, authService, pingService));
    }

    public void showHomeScene(String username, int accountId) {
        switchTo(new HomeScene(this, username, accountId, characterService));
    }

    public void showStarMapScene(String username, int accountId, int characterId) {
        switchTo(new StarMapScene(this, username, accountId, characterId, starMapService));
    }

    public com.aurora.net.CharacterService getCharacterService() {
        return characterService;
    }

    public com.aurora.net.StarMapService getStarMapService() {
        return starMapService;
    }

    public void resume() {
        if (current == null) {
            showLoginScene();
            return;
        }
        display.setCurrent(current.asDisplayable());
        current.onEnter();
    }

    public void pause() {
        if (current != null) {
            current.onExit();
        }
    }

    public void switchTo(Scene next) {
        if (next == null) {
            return;
        }
        if (current != null) {
            current.onExit();
        }
        current = next;
        display.setCurrent(next.asDisplayable());
        current.onEnter();
    }

    public void exitApp() {
        if (current != null) {
            current.onExit();
        }
        midlet.notifyDestroyed();
    }

    public Display getDisplay() {
        return display;
    }
}
