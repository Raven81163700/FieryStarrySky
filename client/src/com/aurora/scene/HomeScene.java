package com.aurora.scene;

import com.aurora.character.AvatarDraft;
import com.aurora.character.AvatarRenderer;
import com.aurora.character.RandomNameGenerator;
import com.aurora.ui.DrawUtils;

import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

public final class HomeScene extends Canvas implements Scene, CommandListener {

    private static final int MODE_SELECT = 0;
    private static final int MODE_CREATE = 1;

    private static final int FOCUS_NAME = 0;
    private static final int FOCUS_GENDER = 1;
    private static final int FOCUS_BODY = 2;
    private static final int FOCUS_SKIN_PRESET = 3;
    private static final int FOCUS_SKIN_R = 4;
    private static final int FOCUS_SKIN_G = 5;
    private static final int FOCUS_SKIN_B = 6;
    private static final int FOCUS_EYE = 7;
    private static final int FOCUS_NOSE = 8;
    private static final int FOCUS_MOUTH = 9;
    private static final int FOCUS_ACCESSORY = 10;
    private static final int FOCUS_FACE_EDIT = 11;
    private static final int FOCUS_BODY_EDIT = 12;
    private static final int FOCUS_SAVE = 13;

    private static final int MAX_CHARACTERS = 4;

    private final SceneManager sceneManager;
    private final String loginUsername;
    private final int accountId;

    private final RandomNameGenerator nameGenerator = new RandomNameGenerator();
    private final Vector characters = new Vector();
    private final AvatarDraft draft = new AvatarDraft();

    private int mode = MODE_SELECT;
    private int focus = 0;
    private int selectedIndex = 0;

    private boolean faceEditMode = false;
    private boolean bodyEditMode = false;
    private int cursorX = AvatarDraft.FACE_W / 2;
    private int cursorY = AvatarDraft.FACE_H / 2;

    private TextBox editor;
    private final Command cmdEditOk = new Command("确定", Command.OK, 1);
    private final Command cmdEditCancel = new Command("取消", Command.CANCEL, 2);

    private final Command cmdToggleMode = new Command("切换模式", Command.SCREEN, 1);
    private final Command cmdRandomName = new Command("随机昵称", Command.SCREEN, 2);
    private final Command cmdEditName = new Command("改昵称", Command.SCREEN, 3);
    private final Command cmdSave = new Command("保存角色", Command.OK, 1);
    private final Command cmdDelete = new Command("删除角色", Command.SCREEN, 4);
    private final Command cmdLogout = new Command("返回登录", Command.BACK, 1);
    private final Command cmdExit = new Command("退出", Command.EXIT, 9);

    public HomeScene(SceneManager sceneManager, String username, int accountId) {
        this.sceneManager = sceneManager;
        this.loginUsername = username;
        this.accountId = accountId;

        draft.roleName = nameGenerator.nextName();
        AvatarRenderer.applySkinPreset(draft);

        addCommand(cmdToggleMode);
        addCommand(cmdRandomName);
        addCommand(cmdEditName);
        addCommand(cmdSave);
        addCommand(cmdDelete);
        addCommand(cmdLogout);
        addCommand(cmdExit);
        setCommandListener(this);
    }

    protected void paint(Graphics g) {
        int w = getWidth();
        DrawUtils.fillBackground(g, w, getHeight());
        DrawUtils.drawMainTitle(g, w, "FieryStarrySky");
        DrawUtils.drawSubtitle(g, w, 30, mode == MODE_SELECT ? "角色选择" : "角色创建");

        g.setColor(0xD6E4FF);
        g.drawString("账号: " + loginUsername + "  #" + accountId, 10, 48, Graphics.TOP | Graphics.LEFT);

        if (mode == MODE_SELECT) {
            paintSelectMode(g);
        } else {
            paintCreateMode(g);
        }
    }

    private void paintSelectMode(Graphics g) {
        int y = 68;
        int i;
        g.setColor(0x9FB4D8);
        g.drawString("已保存角色", 10, y, Graphics.TOP | Graphics.LEFT);
        y += 16;

        if (characters.size() == 0) {
            g.setColor(0xFFFFFF);
            g.drawString("暂无角色，切换到创建模式", 10, y, Graphics.TOP | Graphics.LEFT);
            return;
        }

        for (i = 0; i < characters.size(); i++) {
            CharacterSlot slot = (CharacterSlot) characters.elementAt(i);
            boolean selected = i == selectedIndex;
            g.setColor(selected ? 0x2E7DFF : 0x4D5D7A);
            g.fillRect(8, y - 2, getWidth() - 16, 18);
            g.setColor(0xFFFFFF);
            g.drawRect(8, y - 2, getWidth() - 16, 18);
            g.drawString(slot.name + " [" + slot.gender + "]", 12, y + 1, Graphics.TOP | Graphics.LEFT);
            y += 22;
        }

        CharacterSlot selected = (CharacterSlot) characters.elementAt(selectedIndex);
        g.setColor(0xC9D8F2);
        g.drawString("按中键: 选择角色", 10, getHeight() - 36, Graphics.TOP | Graphics.LEFT);
        AvatarRenderer.drawAvatar(g, selected.avatar, getWidth() - 74, 64, 2);
    }

    private void paintCreateMode(Graphics g) {
        int y = 66;
        drawCreateLine(g, FOCUS_NAME, y, "昵称", draft.roleName);
        y += 14;
        drawCreateLine(g, FOCUS_GENDER, y, "性别", draft.genderLabel());
        y += 14;
        drawCreateLine(g, FOCUS_BODY, y, "体态", "预设 " + (draft.bodyPreset + 1));
        y += 14;
        drawCreateLine(g, FOCUS_SKIN_PRESET, y, "肤色预设", "#" + (draft.skinPreset + 1));
        y += 14;
        drawCreateLine(g, FOCUS_SKIN_R, y, "肤色R", String.valueOf(draft.skinR));
        y += 14;
        drawCreateLine(g, FOCUS_SKIN_G, y, "肤色G", String.valueOf(draft.skinG));
        y += 14;
        drawCreateLine(g, FOCUS_SKIN_B, y, "肤色B", String.valueOf(draft.skinB));
        y += 14;
        drawCreateLine(g, FOCUS_EYE, y, "眼睛", "#" + (draft.eyePreset + 1));
        y += 14;
        drawCreateLine(g, FOCUS_NOSE, y, "鼻子", "#" + (draft.nosePreset + 1));
        y += 14;
        drawCreateLine(g, FOCUS_MOUTH, y, "嘴巴", "#" + (draft.mouthPreset + 1));
        y += 14;
        drawCreateLine(g, FOCUS_ACCESSORY, y, "装饰", accessoryLabel(draft.accessoryPreset));
        y += 14;
        drawCreateLine(g, FOCUS_FACE_EDIT, y, "面部涂改", faceEditMode ? "编辑中" : "进入编辑");
        y += 14;
        drawCreateLine(g, FOCUS_BODY_EDIT, y, "全身涂改", bodyEditMode ? "编辑中" : "进入编辑");
        y += 14;
        drawCreateLine(g, FOCUS_SAVE, y, "保存", "保存到角色槽");

        AvatarRenderer.drawAvatar(g, draft, getWidth() - 74, 64, 2);

        if (faceEditMode || bodyEditMode) {
            drawEditCursor(g);
            g.setColor(0xFFDD57);
            g.drawString("*#切色 0清除 9退出 中键绘制", 8, getHeight() - 30, Graphics.TOP | Graphics.LEFT);
        }

        g.setColor(0xC9D8F2);
        g.drawString("色:" + toHex(draft.selectedPaintColor), getWidth() - 72, getHeight() - 30,
                Graphics.TOP | Graphics.LEFT);
    }

    private void drawCreateLine(Graphics g, int index, int y, String label, String value) {
        boolean selected = focus == index;
        g.setColor(selected ? 0x1F6FEB : 0x42516B);
        g.fillRect(8, y - 1, 98, 13);
        g.setColor(0xFFFFFF);
        g.drawRect(8, y - 1, 98, 13);
        g.drawString(label + ": " + value, 10, y + 1, Graphics.TOP | Graphics.LEFT);
    }

    private void drawEditCursor(Graphics g) {
        int px;
        int py;
        if (faceEditMode) {
            px = AvatarDraft.FACE_X + cursorX;
            py = AvatarDraft.FACE_Y + cursorY;
        } else {
            px = cursorX;
            py = cursorY;
        }
        int drawX = getWidth() - 74 + px * 2;
        int drawY = 64 + py * 2;
        g.setColor(0xFFEB3B);
        g.drawRect(drawX, drawY, 2, 2);
    }

    protected void keyPressed(int keyCode) {
        if (faceEditMode || bodyEditMode) {
            handlePaintKey(keyCode);
            return;
        }

        int action = getGameAction(keyCode);
        if (mode == MODE_SELECT) {
            handleSelectKey(action);
            return;
        }
        handleCreateKey(action);
    }

    private void handleSelectKey(int action) {
        if (characters.size() == 0) {
            return;
        }
        if (action == UP) {
            selectedIndex = (selectedIndex - 1 + characters.size()) % characters.size();
            repaint();
            return;
        }
        if (action == DOWN) {
            selectedIndex = (selectedIndex + 1) % characters.size();
            repaint();
            return;
        }
        if (action == FIRE) {
            CharacterSlot slot = (CharacterSlot) characters.elementAt(selectedIndex);
            showInfo("已选择", "角色 " + slot.name + "，后续将进入游戏场景");
        }
    }

    private void handleCreateKey(int action) {
        int maxFocus = FOCUS_SAVE;
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
        if (action == LEFT) {
            adjustFocused(-1);
            repaint();
            return;
        }
        if (action == RIGHT) {
            adjustFocused(1);
            repaint();
            return;
        }
        if (action == FIRE) {
            activateCreateFocus();
        }
    }

    private void handlePaintKey(int keyCode) {
        if (keyCode == KEY_NUM9) {
            faceEditMode = false;
            bodyEditMode = false;
            repaint();
            return;
        }
        if (keyCode == KEY_STAR) {
            cyclePaintColor(-1);
            repaint();
            return;
        }
        if (keyCode == KEY_POUND) {
            cyclePaintColor(1);
            repaint();
            return;
        }
        if (keyCode == KEY_NUM0) {
            paintPixel(-1);
            repaint();
            return;
        }

        int action = getGameAction(keyCode);
        int maxX = faceEditMode ? (AvatarDraft.FACE_W - 1) : (AvatarDraft.WIDTH - 1);
        int maxY = faceEditMode ? (AvatarDraft.FACE_H - 1) : (AvatarDraft.HEIGHT - 1);

        if (action == LEFT) {
            cursorX = clamp(cursorX - 1, 0, maxX);
            repaint();
            return;
        }
        if (action == RIGHT) {
            cursorX = clamp(cursorX + 1, 0, maxX);
            repaint();
            return;
        }
        if (action == UP) {
            cursorY = clamp(cursorY - 1, 0, maxY);
            repaint();
            return;
        }
        if (action == DOWN) {
            cursorY = clamp(cursorY + 1, 0, maxY);
            repaint();
            return;
        }
        if (action == FIRE) {
            paintPixel(draft.selectedPaintColor);
            repaint();
        }
    }

    private void cyclePaintColor(int delta) {
        int[] palette = AvatarRenderer.getPalette();
        int i;
        int current = 0;
        for (i = 0; i < palette.length; i++) {
            if (palette[i] == draft.selectedPaintColor) {
                current = i;
                break;
            }
        }
        current = (current + delta + palette.length) % palette.length;
        draft.selectedPaintColor = palette[current];
    }

    private void paintPixel(int color) {
        if (faceEditMode) {
            draft.setFacePaint(cursorX, cursorY, color);
        } else if (bodyEditMode) {
            draft.setBodyPaint(cursorX, cursorY, color);
        }
    }

    private void adjustFocused(int delta) {
        if (focus == FOCUS_GENDER) {
            draft.gender = cycle(draft.gender, delta, 3);
            return;
        }
        if (focus == FOCUS_BODY) {
            draft.bodyPreset = cycle(draft.bodyPreset, delta, AvatarDraft.BODY_PRESET_COUNT);
            return;
        }
        if (focus == FOCUS_SKIN_PRESET) {
            draft.skinPreset = cycle(draft.skinPreset, delta, AvatarDraft.SKIN_PRESET_COUNT);
            AvatarRenderer.applySkinPreset(draft);
            return;
        }
        if (focus == FOCUS_SKIN_R) {
            draft.skinR = clamp(draft.skinR + delta * 5, 0, 255);
            return;
        }
        if (focus == FOCUS_SKIN_G) {
            draft.skinG = clamp(draft.skinG + delta * 5, 0, 255);
            return;
        }
        if (focus == FOCUS_SKIN_B) {
            draft.skinB = clamp(draft.skinB + delta * 5, 0, 255);
            return;
        }
        if (focus == FOCUS_EYE) {
            draft.eyePreset = cycle(draft.eyePreset, delta, AvatarDraft.EYE_PRESET_COUNT);
            return;
        }
        if (focus == FOCUS_NOSE) {
            draft.nosePreset = cycle(draft.nosePreset, delta, AvatarDraft.NOSE_PRESET_COUNT);
            return;
        }
        if (focus == FOCUS_MOUTH) {
            draft.mouthPreset = cycle(draft.mouthPreset, delta, AvatarDraft.MOUTH_PRESET_COUNT);
            return;
        }
        if (focus == FOCUS_ACCESSORY) {
            draft.accessoryPreset = cycle(draft.accessoryPreset, delta, AvatarDraft.ACCESSORY_PRESET_COUNT);
        }
    }

    private void activateCreateFocus() {
        if (focus == FOCUS_NAME) {
            openNameEditor();
            return;
        }
        if (focus == FOCUS_FACE_EDIT) {
            faceEditMode = true;
            bodyEditMode = false;
            cursorX = AvatarDraft.FACE_W / 2;
            cursorY = AvatarDraft.FACE_H / 2;
            repaint();
            return;
        }
        if (focus == FOCUS_BODY_EDIT) {
            bodyEditMode = true;
            faceEditMode = false;
            cursorX = AvatarDraft.WIDTH / 2;
            cursorY = AvatarDraft.HEIGHT / 2;
            repaint();
            return;
        }
        if (focus == FOCUS_SAVE) {
            saveCharacter();
        }
    }

    private void openNameEditor() {
        editor = new TextBox("角色昵称", draft.roleName, 24, TextField.ANY);
        editor.addCommand(cmdEditOk);
        editor.addCommand(cmdEditCancel);
        editor.setCommandListener(this);
        sceneManager.getDisplay().setCurrent(editor);
    }

    private void saveCharacter() {
        if (draft.roleName == null || draft.roleName.trim().length() < 3) {
            showInfo("保存失败", "昵称至少3位");
            return;
        }

        if (characters.size() >= MAX_CHARACTERS) {
            showInfo("保存失败", "角色槽已满(最多" + MAX_CHARACTERS + ")");
            return;
        }

        CharacterSlot slot = new CharacterSlot();
        slot.name = draft.roleName;
        slot.gender = draft.genderLabel();
        slot.avatar = draft.copy();
        characters.addElement(slot);
        selectedIndex = characters.size() - 1;
        mode = MODE_SELECT;
        showInfo("保存成功", "角色已加入选择列表");
    }

    public Displayable asDisplayable() {
        return this;
    }

    public void onEnter() {
    }

    public void onExit() {
        faceEditMode = false;
        bodyEditMode = false;
    }

    public void commandAction(Command c, Displayable d) {
        if (d == editor) {
            if (c == cmdEditOk) {
                draft.roleName = editor.getString();
                if (draft.roleName == null || draft.roleName.trim().length() == 0) {
                    draft.roleName = nameGenerator.nextName();
                }
            }
            editor = null;
            sceneManager.getDisplay().setCurrent(this);
            repaint();
            return;
        }

        if (c == cmdToggleMode) {
            mode = (mode == MODE_SELECT) ? MODE_CREATE : MODE_SELECT;
            repaint();
            return;
        }
        if (c == cmdRandomName) {
            draft.roleName = nameGenerator.nextName();
            repaint();
            return;
        }
        if (c == cmdEditName) {
            openNameEditor();
            return;
        }
        if (c == cmdSave) {
            saveCharacter();
            return;
        }
        if (c == cmdDelete) {
            deleteSelected();
            repaint();
            return;
        }
        if (c == cmdLogout) {
            sceneManager.showLoginScene();
            return;
        }
        if (c == cmdExit) {
            sceneManager.exitApp();
            return;
        }
    }

    private void deleteSelected() {
        if (mode != MODE_SELECT || characters.size() == 0) {
            return;
        }
        characters.removeElementAt(selectedIndex);
        if (selectedIndex >= characters.size()) {
            selectedIndex = characters.size() - 1;
        }
        if (selectedIndex < 0) {
            selectedIndex = 0;
        }
    }

    private void showInfo(String title, String msg) {
        Alert alert = new Alert(title, msg, null, AlertType.INFO);
        alert.setTimeout(1200);
        sceneManager.getDisplay().setCurrent(alert, this);
    }

    private String accessoryLabel(int idx) {
        if (idx == 1) {
            return "眼罩";
        }
        if (idx == 2) {
            return "创可贴";
        }
        if (idx == 3) {
            return "头带";
        }
        return "无";
    }

    private static int cycle(int value, int delta, int count) {
        return (value + delta + count) % count;
    }

    private static int clamp(int v, int min, int max) {
        if (v < min) {
            return min;
        }
        if (v > max) {
            return max;
        }
        return v;
    }

    private static String toHex(int color) {
        String hex = Integer.toHexString(color & 0xFFFFFF).toUpperCase();
        while (hex.length() < 6) {
            hex = "0" + hex;
        }
        return "#" + hex;
    }

    private static final class CharacterSlot {
        String name;
        String gender;
        AvatarDraft avatar;
    }
}
