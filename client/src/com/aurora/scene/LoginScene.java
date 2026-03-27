package com.aurora.scene;

import com.aurora.auth.AuthResult;
import com.aurora.auth.AuthService;
import com.aurora.net.PingService;
import com.aurora.ui.DrawUtils;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

public final class LoginScene extends Canvas implements Scene, CommandListener {

    private static final int MODE_LOGIN = 0;
    private static final int MODE_REGISTER = 1;

    private static final int FIELD_USER = 0;
    private static final int FIELD_PASS = 1;
    private static final int FIELD_PASS2 = 2;

    private static final int PING_INTERVAL_MS = 3000;

    private final SceneManager sceneManager;
    private final Display display;
    private final AuthService authService;
    private final PingService pingService;

    private String username = "";
    private String password = "";
    private String password2 = "";

    private int mode = MODE_LOGIN;
    private int focus = 0;

    private TextBox editor;
    private int editingField = -1;

    private boolean busy = false;
    private int latencyMs = -1;
    private boolean pingRunning = false;

    private final Command cmdOk = new Command("确定", Command.OK, 1);
    private final Command cmdCancel = new Command("取消", Command.CANCEL, 2);
    private final Command cmdSubmit = new Command("选择", Command.SCREEN, 1);
    private final Command cmdExit = new Command("退出", Command.EXIT, 9);

    public LoginScene(SceneManager sceneManager, AuthService authService, PingService pingService) {
        this.sceneManager = sceneManager;
        this.display = sceneManager.getDisplay();
        this.authService = authService;
        this.pingService = pingService;
        addCommand(cmdSubmit);
        addCommand(cmdExit);
        setCommandListener(this);
    }

    protected void showNotify() {
        onEnter();
    }

    protected void hideNotify() {
        onExit();
    }

    protected void paint(Graphics g) {
        int w = getWidth();
        int boxW = w - 20;

        int yUser = 58;
        int yPass = 94;
        int yPass2 = 130;
        int yBtn = (mode == MODE_LOGIN) ? 136 : 172;

        DrawUtils.fillBackground(g, w, getHeight());
        DrawUtils.drawMainTitle(g, w, "FieryStarrySky");
        DrawUtils.drawSubtitle(g, w, 30, mode == MODE_LOGIN ? "登录" : "注册");
        DrawUtils.drawLatency(g, w, latencyMs);

        DrawUtils.drawField(g, 10, yUser, boxW, 24, "账号", username, focus == 0);
        DrawUtils.drawField(g, 10, yPass, boxW, 24, "密码", DrawUtils.mask(password), focus == 1);

        if (mode == MODE_REGISTER) {
            DrawUtils.drawField(g, 10, yPass2, boxW, 24, "确认密码", DrawUtils.mask(password2), focus == 2);
        }

        DrawUtils.drawButton(g, 10, yBtn, boxW, 24,
                mode == MODE_LOGIN ? "登录" : "注册",
                focus == ((mode == MODE_LOGIN) ? 2 : 3));

        DrawUtils.drawButton(g, 10, yBtn + 30, boxW, 20,
                mode == MODE_LOGIN ? "切换到注册" : "切换到登录",
                focus == ((mode == MODE_LOGIN) ? 3 : 4));

        if (busy) {
            g.setColor(0xFFCE45);
            g.drawString("正在与服务器通信...", w / 2, yBtn + 56, Graphics.TOP | Graphics.HCENTER);
        }
    }

    protected void keyPressed(int keyCode) {
        if (busy) {
            return;
        }

        int action = resolveAction(keyCode);
        int maxFocus = (mode == MODE_LOGIN) ? 3 : 4;

        if (action == UP) {
            focus = (focus - 1 + maxFocus + 1) % (maxFocus + 1);
            repaint();
            return;
        }
        if (action == DOWN) {
            focus = (focus + 1) % (maxFocus + 1);
            repaint();
            return;
        }
        if (action == FIRE) {
            activateFocus();
        }
    }

    private int resolveAction(int keyCode) {
        int action = 0;
        try {
            action = getGameAction(keyCode);
        } catch (IllegalArgumentException e) {
            action = 0;
        }

        if (action != 0) {
            return action;
        }

        if (keyCode == KEY_NUM2) {
            return UP;
        }
        if (keyCode == KEY_NUM8) {
            return DOWN;
        }
        if (keyCode == KEY_NUM5) {
            return FIRE;
        }
        return 0;
    }

    protected void pointerPressed(int x, int y) {
        if (busy) {
            return;
        }

        int yUser = 58;
        int yPass = 94;
        int yPass2 = 130;
        int yBtn = (mode == MODE_LOGIN) ? 136 : 172;

        if (hit(y, yUser, 24)) {
            focus = 0;
            activateFocus();
            return;
        }
        if (hit(y, yPass, 24)) {
            focus = 1;
            activateFocus();
            return;
        }
        if (mode == MODE_REGISTER && hit(y, yPass2, 24)) {
            focus = 2;
            activateFocus();
            return;
        }
        if (hit(y, yBtn, 24)) {
            focus = (mode == MODE_LOGIN) ? 2 : 3;
            activateFocus();
            return;
        }
        if (hit(y, yBtn + 30, 20)) {
            focus = (mode == MODE_LOGIN) ? 3 : 4;
            activateFocus();
        }
    }

    private boolean hit(int py, int y, int h) {
        return py >= y && py <= (y + h);
    }

    private void activateFocus() {
        if (busy) {
            return;
        }

        if (focus == 0) {
            openEditor(FIELD_USER, "输入账号", username, 16, TextField.ANY);
            return;
        }
        if (focus == 1) {
            openEditor(FIELD_PASS, "输入密码", password, 32, TextField.PASSWORD);
            return;
        }
        if (mode == MODE_REGISTER && focus == 2) {
            openEditor(FIELD_PASS2, "确认密码", password2, 32, TextField.PASSWORD);
            return;
        }

        int submitIndex = (mode == MODE_LOGIN) ? 2 : 3;
        int switchIndex = (mode == MODE_LOGIN) ? 3 : 4;

        if (focus == submitIndex) {
            submit();
            return;
        }
        if (focus == switchIndex) {
            mode = (mode == MODE_LOGIN) ? MODE_REGISTER : MODE_LOGIN;
            focus = 0;
            repaint();
        }
    }

    private void openEditor(int field, String title, String current, int maxSize, int constraints) {
        editingField = field;
        editor = new TextBox(title, current, maxSize, constraints);
        editor.addCommand(cmdOk);
        editor.addCommand(cmdCancel);
        editor.setCommandListener(this);
        display.setCurrent(editor);
    }

    private void submit() {
        if (username.length() < 3) {
            showInfo("提示", "账号至少3位");
            return;
        }
        if (password.length() < 6) {
            showInfo("提示", "密码至少6位");
            return;
        }
        if (mode == MODE_REGISTER && !password.equals(password2)) {
            showInfo("提示", "两次密码不一致");
            return;
        }

        final int actionMode = mode;
        final String u = username;
        final String p = password;
        busy = true;
        repaint();

        Thread worker = new Thread(new Runnable() {
            public void run() {
                final AuthResult result;
                if (actionMode == MODE_LOGIN) {
                    result = authService.login(u, p);
                } else {
                    result = authService.register(u, p);
                }

                display.callSerially(new Runnable() {
                    public void run() {
                        busy = false;
                        onAuthFinished(actionMode, result);
                        repaint();
                    }
                });
            }
        });
        worker.start();
    }

    private void onAuthFinished(int actionMode, AuthResult result) {
        if (!result.isSuccess()) {
            showInfo("失败", result.getMessage());
            return;
        }

        if (actionMode == MODE_LOGIN) {
            sceneManager.showHomeScene(result.getUsername(), result.getAccountId());
            return;
        }

        mode = MODE_LOGIN;
        password2 = "";
        focus = 0;
        showInfo("注册成功", result.getMessage() + "，请继续登录");
    }

    private void showInfo(String title, String msg) {
        Alert alert = new Alert(title, msg, null, AlertType.INFO);
        alert.setTimeout(1400);
        display.setCurrent(alert, this);
    }

    private void startPingLoop() {
        if (pingRunning) {
            return;
        }
        pingRunning = true;

        Thread pingThread = new Thread(new Runnable() {
            public void run() {
                while (pingRunning) {
                    final int measured = pingService.measureLatencyMillis();
                    display.callSerially(new Runnable() {
                        public void run() {
                            latencyMs = measured;
                            repaint();
                        }
                    });

                    try {
                        Thread.sleep(PING_INTERVAL_MS);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
        pingThread.start();
    }

    private void stopPingLoop() {
        pingRunning = false;
    }

    public Displayable asDisplayable() {
        return this;
    }

    public void onEnter() {
        startPingLoop();
    }

    public void onExit() {
        stopPingLoop();
    }

    public void commandAction(Command c, Displayable d) {
        if (d == this && c == cmdSubmit) {
            activateFocus();
            return;
        }

        if (d == editor) {
            if (c == cmdOk) {
                String value = editor.getString();
                if (editingField == FIELD_USER) {
                    username = value;
                } else if (editingField == FIELD_PASS) {
                    password = value;
                } else if (editingField == FIELD_PASS2) {
                    password2 = value;
                }
            }
            editingField = -1;
            editor = null;
            display.setCurrent(this);
            repaint();
            return;
        }

        if (c.getCommandType() == Command.EXIT) {
            sceneManager.exitApp();
        }
    }
}
