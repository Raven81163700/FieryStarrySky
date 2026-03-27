package com.aurora.scene;

import com.aurora.character.RandomNameGenerator;
import com.aurora.ui.CanvasScrollHelper;
import com.aurora.ui.DrawUtils;
import com.aurora.ui.ImageTransformUtils;
import com.aurora.util.Base64;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

public final class HomeScene extends Canvas implements Scene, CommandListener {

    private static final int MODE_SELECT = 0;
    private static final int MODE_CREATE = 1;

    private static final int FOCUS_NAME = 0;
    private static final int FOCUS_GENDER = 1;
    private static final int FOCUS_BACKGROUND = 2;
    private static final int FOCUS_CHARACTER = 3;
    private static final int FOCUS_SAVE = 4;

    private static final int CREATE_FORM_X = 8;
    private static final int CREATE_FORM_WIDTH = 122;
    private static final int CREATE_FORM_START_Y = 66;
    private static final int CREATE_FORM_ROW_HEIGHT = 18;
    private static final int CREATE_FORM_BOTTOM_MARGIN = 42;

    private static final int GENDER_MALE = 0;
    private static final int GENDER_FEMALE = 1;
    private static final int GENDER_AGENDER = 2;

    private static final int MAX_CHARACTERS = 2;

    private static final String[] BACKGROUND_PATHS = {
        "/game_res/background/background_1.png",
        "/game_res/background/background_2.png",
        "/game_res/background/background_3.png",
        "/game_res/background/background_4.png",
        "/game_res/background/background_5.png",
        "/game_res/background/background_6.png"
    };

    private static final String[] MALE_CHARACTER_PATHS = {
        "/game_res/character/male/male1.png",
        "/game_res/character/male/male2.png",
        "/game_res/character/male/male3.png"
    };

    private static final String[] FEMALE_CHARACTER_PATHS = {
        "/game_res/character/female/female1.png",
        "/game_res/character/female/female2.png",
        "/game_res/character/female/female3.png",
        "/game_res/character/female/female4.png"
    };

    private static final String[] NO_SEX_CHARACTER_PATHS = {
        "/game_res/character/no_sex/normal1.png",
        "/game_res/character/no_sex/normal2.png"
    };

    private final SceneManager sceneManager;
    private final String loginUsername;
    private final int accountId;
    private final com.aurora.net.CharacterService characterService;

    private final RandomNameGenerator nameGenerator = new RandomNameGenerator();
    private final Vector characters = new Vector();

    private String roleName;
    private int gender = GENDER_MALE;

    private String[] selectableCharacterPaths;
    private int selectedBackgroundIndex = 0;
    private int selectedCharacterIndex = 0;

    private Image previewImage32;
    private Image previewImage64;

    private int mode = MODE_SELECT;
    private int focus = 0;
    private int selectedIndex = 0;
    private final CanvasScrollHelper createScroller = new CanvasScrollHelper();
    private boolean createDragActive = false;
    private int createLastDragY = 0;

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

    public HomeScene(SceneManager sceneManager, String username, int accountId, com.aurora.net.CharacterService characterService) {
        this.sceneManager = sceneManager;
        this.loginUsername = username;
        this.accountId = accountId;
        this.characterService = characterService;

        roleName = nameGenerator.nextName();
        reloadCharacterOptions();
        refreshPreview();

        updateCommandsForMode();
        setCommandListener(this);
    }

    private void updateCommandsForMode() {
        try { removeCommand(cmdToggleMode); } catch (Exception e) { }
        try { removeCommand(cmdRandomName); } catch (Exception e) { }
        try { removeCommand(cmdEditName); } catch (Exception e) { }
        try { removeCommand(cmdSave); } catch (Exception e) { }
        try { removeCommand(cmdDelete); } catch (Exception e) { }
        try { removeCommand(cmdLogout); } catch (Exception e) { }
        try { removeCommand(cmdExit); } catch (Exception e) { }

        // always offer mode toggle and session commands
        addCommand(cmdToggleMode);
        addCommand(cmdLogout);
        addCommand(cmdExit);

        if (mode == MODE_CREATE) {
            addCommand(cmdRandomName);
            addCommand(cmdEditName);
            addCommand(cmdSave);
        } else {
            if (characters.size() > 0) {
                addCommand(cmdDelete);
            }
        }
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
            updateCreateScrollerConfig();
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
        if (selected.preview64 != null) {
            g.drawImage(selected.preview64, getWidth() - 74, 64, Graphics.TOP | Graphics.LEFT);
        }
    }

    private void paintCreateMode(Graphics g) {
        int y0 = CREATE_FORM_START_Y - createScroller.getScrollY();
        drawCreateLine(g, FOCUS_NAME, y0 + CREATE_FORM_ROW_HEIGHT * FOCUS_NAME, "昵称", roleName);
        drawCreateLine(g, FOCUS_GENDER, y0 + CREATE_FORM_ROW_HEIGHT * FOCUS_GENDER, "性别", genderLabel(gender));
        drawCreateLine(g, FOCUS_BACKGROUND, y0 + CREATE_FORM_ROW_HEIGHT * FOCUS_BACKGROUND, "背景", "#" + (selectedBackgroundIndex + 1));
        drawCreateLine(g, FOCUS_CHARACTER, y0 + CREATE_FORM_ROW_HEIGHT * FOCUS_CHARACTER, "人物", "#" + (selectedCharacterIndex + 1));
        drawCreateLine(g, FOCUS_SAVE, y0 + CREATE_FORM_ROW_HEIGHT * FOCUS_SAVE, "保存", "");

        if (previewImage64 != null) {
            g.drawImage(previewImage64, getWidth() - 74, 64, Graphics.TOP | Graphics.LEFT);
        } else {
            g.setColor(0xD32F2F);
            g.drawString("资源未找到", getWidth() - 74, 64, Graphics.TOP | Graphics.LEFT);
        }
    }

    private void drawCreateLine(Graphics g, int index, int y, String label, String value) {
        int lineHeight = CREATE_FORM_ROW_HEIGHT - 2;
        if (y + lineHeight < CREATE_FORM_START_Y - 2 || y > getHeight() - CREATE_FORM_BOTTOM_MARGIN) {
            return;
        }

        boolean selected = focus == index;
        g.setColor(selected ? 0x1F6FEB : 0x42516B);
        g.fillRect(CREATE_FORM_X, y - 1, CREATE_FORM_WIDTH, lineHeight);
        g.setColor(0xFFFFFF);
        g.drawRect(CREATE_FORM_X, y - 1, CREATE_FORM_WIDTH, lineHeight);
        g.drawString(label + ": " + value, CREATE_FORM_X + 2, y + 1, Graphics.TOP | Graphics.LEFT);
    }

    protected void keyPressed(int keyCode) {
        int action = getGameAction(keyCode);
        if (mode == MODE_SELECT) {
            if (characters.size() == 0 && (action == FIRE || action == RIGHT)) {
                mode = MODE_CREATE;
                focus = FOCUS_NAME;
                updateCommandsForMode();
                repaint();
                return;
            }
            handleSelectKey(action);
            return;
        }
        handleCreateKey(action);
    }

    protected void pointerPressed(int x, int y) {
        if (mode == MODE_SELECT) {
            handleSelectTouch(x, y);
            return;
        }

        updateCreateScrollerConfig();
        if (isInCreateViewport(y)) {
            createDragActive = true;
            createLastDragY = y;
        }
        handleCreateTouch(x, y);
    }

    protected void pointerDragged(int x, int y) {
        if (mode != MODE_CREATE || !createDragActive) {
            return;
        }
        updateCreateScrollerConfig();
        int delta = createLastDragY - y;
        createLastDragY = y;
        if (createScroller.scrollBy(delta)) {
            repaint();
        }
    }

    protected void pointerReleased(int x, int y) {
        createDragActive = false;
    }

    private void handleSelectTouch(int x, int y) {
        int listStartY = 84;
        int rowStep = 22;
        int rowTopOffset = -2;
        int rowHeight = 18;

        if (characters.size() == 0) {
            // Tap the empty-list hint area to enter create mode.
            if (x >= 8 && x <= getWidth() - 8 && y >= 80 && y <= 120) {
                mode = MODE_CREATE;
                focus = FOCUS_NAME;
                updateCommandsForMode();
                repaint();
            }
            return;
        }

        int i;
        for (i = 0; i < characters.size(); i++) {
            int rowBaseY = listStartY + i * rowStep;
            int top = rowBaseY + rowTopOffset;
            int bottom = top + rowHeight;
            if (x >= 8 && x <= getWidth() - 8 && y >= top && y <= bottom) {
                if (selectedIndex == i) {
                    enterStarMapForSelected();
                } else {
                    selectedIndex = i;
                    repaint();
                }
                return;
            }
        }

        // Tap bottom hint area to select current role.
        if (y >= getHeight() - 44) {
            enterStarMapForSelected();
        }
    }

    private void handleCreateTouch(int x, int y) {
        updateCreateScrollerConfig();
        int lineStartY = CREATE_FORM_START_Y;
        int lineStep = CREATE_FORM_ROW_HEIGHT;
        int maxFocus = FOCUS_SAVE;

        int i;
        for (i = 0; i <= maxFocus; i++) {
            int top = lineStartY + i * lineStep - createScroller.getScrollY() - 1;
            int bottom = top + (CREATE_FORM_ROW_HEIGHT - 2);
            if (x >= CREATE_FORM_X && x <= (CREATE_FORM_X + CREATE_FORM_WIDTH)
                    && y >= top && y <= bottom) {
                focus = i;
                ensureCreateFocusVisible();
                if (i == FOCUS_NAME) {
                    openNameEditor();
                    return;
                }
                if (i == FOCUS_SAVE) {
                    saveCharacter();
                    return;
                }
                adjustFocused(1);
                repaint();
                return;
            }
        }

        // Tap preview area to cycle character image quickly.
        if (x >= getWidth() - 74 && x <= getWidth() - 10 && y >= 64 && y <= 192) {
            focus = FOCUS_CHARACTER;
            adjustFocused(1);
            repaint();
        }
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
            enterStarMapForSelected();
        }
    }

    private void enterStarMapForSelected() {
        if (characters.size() == 0) {
            return;
        }
        CharacterSlot slot = (CharacterSlot) characters.elementAt(selectedIndex);
        sceneManager.showStarMapScene(loginUsername, accountId, slot.id);
    }

    private void handleCreateKey(int action) {
        int maxFocus = FOCUS_SAVE;
        if (action == UP) {
            focus = (focus - 1 + maxFocus + 1) % (maxFocus + 1);
            ensureCreateFocusVisible();
            repaint();
            return;
        }
        if (action == DOWN) {
            focus = (focus + 1) % (maxFocus + 1);
            ensureCreateFocusVisible();
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

    private void adjustFocused(int delta) {
        if (focus == FOCUS_GENDER) {
            gender = cycle(gender, delta, 3);
            reloadCharacterOptions();
            refreshPreview();
            return;
        }
        if (focus == FOCUS_BACKGROUND) {
            selectedBackgroundIndex = cycle(selectedBackgroundIndex, delta, BACKGROUND_PATHS.length);
            refreshPreview();
            return;
        }
        if (focus == FOCUS_CHARACTER) {
            selectedCharacterIndex = cycle(selectedCharacterIndex, delta, selectableCharacterPaths.length);
            refreshPreview();
        }
    }

    private void activateCreateFocus() {
        if (focus == FOCUS_NAME) {
            openNameEditor();
            return;
        }
        if (focus == FOCUS_SAVE) {
            saveCharacter();
        }
    }

    private void openNameEditor() {
        editor = new TextBox("角色昵称", roleName, 24, TextField.ANY);
        editor.addCommand(cmdEditOk);
        editor.addCommand(cmdEditCancel);
        editor.setCommandListener(this);
        sceneManager.getDisplay().setCurrent(editor);
    }

    private void saveCharacter() {
        if (roleName == null || roleName.trim().length() < 3) {
            showInfo("保存失败", "昵称至少3位");
            return;
        }

        if (previewImage32 == null || previewImage64 == null) {
            showInfo("保存失败", "预览图合成失败，请检查资源");
            return;
        }

        if (characters.size() >= MAX_CHARACTERS) {
            showInfo("保存失败", "角色槽已满(最多" + MAX_CHARACTERS + ")");
            return;
        }

        // send create request to server asynchronously
        final String name = roleName;
        final int g = gender;
        final String bgPath = BACKGROUND_PATHS[selectedBackgroundIndex];
        final String chPath = selectableCharacterPaths[selectedCharacterIndex];
        final int backgroundId = resolveBackgroundId(bgPath);
        final int characterId = resolveCharacterId(chPath);
        final int aid = this.accountId;
        final HomeScene self = this;

        Thread t = new Thread(new Runnable() {
            public void run() {
                final com.aurora.net.CharacterService.Result res = characterService.createCharacter(aid, name, g, backgroundId, characterId);
                sceneManager.getDisplay().callSerially(new Runnable() {
                    public void run() {
                        if (!res.isSuccess()) {
                            showInfo("保存失败", res.getMessage());
                            return;
                        }
                        CharacterSlot slot = new CharacterSlot();
                        slot.id = ((Integer) res.getData()).intValue();
                        slot.name = name;
                        slot.gender = genderLabel(g);
                        slot.backgroundPath = bgPath;
                        slot.characterPath = chPath;
                        slot.preview32 = previewImage32;
                        slot.preview64 = previewImage64;
                        characters.addElement(slot);
                        selectedIndex = characters.size() - 1;
                        mode = MODE_SELECT;
                        updateCommandsForMode();
                        showInfo("保存成功", "角色已经创建");
                        repaint();
                    }
                });
            }
        });
        t.start();
    }

    private void ensureCreateFocusVisible() {
        updateCreateScrollerConfig();
        int focusTop = CREATE_FORM_START_Y + focus * CREATE_FORM_ROW_HEIGHT - createScroller.getScrollY();
        int focusBottom = focusTop + (CREATE_FORM_ROW_HEIGHT - 2);
        createScroller.ensureVisible(focusTop, focusBottom);
    }

    public Displayable asDisplayable() {
        return this;
    }

    public void onEnter() {
        updateCommandsForMode();
        // fetch characters from server
        final HomeScene self = this;
        final int aid = this.accountId;
        Thread t = new Thread(new Runnable() {
            public void run() {
                        try {
                            final com.aurora.net.CharacterService.Result res = characterService.listCharacters(aid);
                            sceneManager.getDisplay().callSerially(new Runnable() {
                                public void run() {
                                    if (!res.isSuccess()) {
                                        // ignore error, keep local state
                                        return;
                                    }
                                    java.util.Vector list = (java.util.Vector) res.getData();
                                    characters.removeAllElements();
                                    for (int i = 0; i < list.size(); i++) {
                                        com.aurora.net.CharacterService.CharInfo ci = (com.aurora.net.CharacterService.CharInfo) list.elementAt(i);
                                        CharacterSlot slot = new CharacterSlot();
                                        slot.id = ci.id;
                                        slot.name = ci.name;
                                        slot.gender = (ci.gender == 1) ? "女" : (ci.gender == 2 ? "无性" : "男");
                                        slot.backgroundPath = resolveBackgroundPathById(ci.backgroundId, ci.backgroundPath);
                                        slot.characterPath = resolveCharacterPathById(ci.characterId, ci.characterPath);
                                        slot.preview32 = null;
                                        slot.preview64 = null;
                                        if (!applyPreviewFromBase64(slot, ci.previewBase64)) {
                                            applyPreviewFromPaths(slot, slot.backgroundPath, slot.characterPath);
                                        }
                                        characters.addElement(slot);
                                    }
                                    if (characters.size() > 0) selectedIndex = 0;
                                    updateCommandsForMode();
                                    repaint();
                                }
                            });
                        } catch (Exception e) {
                            // ignore
                        }
            }
        });
        t.start();
    }

    public void onExit() {
        // no-op
    }

    public void commandAction(Command c, Displayable d) {
        if (d == editor) {
            if (c == cmdEditOk) {
                roleName = editor.getString();
                if (roleName == null || roleName.trim().length() == 0) {
                    roleName = nameGenerator.nextName();
                }
            }
            editor = null;
            sceneManager.getDisplay().setCurrent(this);
            repaint();
            return;
        }

        if (c == cmdToggleMode) {
            mode = (mode == MODE_SELECT) ? MODE_CREATE : MODE_SELECT;
            if (mode == MODE_CREATE) {
                createScroller.setScrollY(0);
                ensureCreateFocusVisible();
            }
            updateCommandsForMode();
            repaint();
            return;
        }
        if (c == cmdRandomName) {
            roleName = nameGenerator.nextName();
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
        final int idx = selectedIndex;
        final CharacterSlot slot = (CharacterSlot) characters.elementAt(idx);
        final int aid = this.accountId;

        if (slot.id > 0) {
            Thread t = new Thread(new Runnable() {
                public void run() {
                    final com.aurora.net.CharacterService.Result res = characterService.deleteCharacter(aid, slot.id);
                    sceneManager.getDisplay().callSerially(new Runnable() {
                        public void run() {
                            if (!res.isSuccess()) {
                                showInfo("删除失败", res.getMessage());
                                return;
                            }
                            characters.removeElementAt(idx);
                            if (selectedIndex >= characters.size()) {
                                selectedIndex = characters.size() - 1;
                            }
                            if (selectedIndex < 0) selectedIndex = 0;
                            updateCommandsForMode();
                            repaint();
                        }
                    });
                }
            });
            t.start();
            return;
        }

        // local-only slot
        characters.removeElementAt(selectedIndex);
        if (selectedIndex >= characters.size()) {
            selectedIndex = characters.size() - 1;
        }
        if (selectedIndex < 0) {
            selectedIndex = 0;
        }
        updateCommandsForMode();
    }

    private void showInfo(String title, String msg) {
        Alert alert = new Alert(title, msg, null, AlertType.INFO);
        alert.setTimeout(1200);
        sceneManager.getDisplay().setCurrent(alert, this);
    }

    private static int cycle(int value, int delta, int count) {
        return (value + delta + count) % count;
    }

    private static String genderLabel(int g) {
        if (g == GENDER_FEMALE) {
            return "女";
        }
        if (g == GENDER_AGENDER) {
            return "无性";
        }
        return "男";
    }

    private void updateCreateScrollerConfig() {
        createScroller.setViewport(CREATE_FORM_START_Y, getHeight() - CREATE_FORM_BOTTOM_MARGIN);
        createScroller.setContentHeight((FOCUS_SAVE + 1) * CREATE_FORM_ROW_HEIGHT);
    }

    private boolean isInCreateViewport(int y) {
        return y >= CREATE_FORM_START_Y && y <= (getHeight() - CREATE_FORM_BOTTOM_MARGIN);
    }

    private void reloadCharacterOptions() {
        if (gender == GENDER_FEMALE) {
            selectableCharacterPaths = mergePaths(FEMALE_CHARACTER_PATHS, NO_SEX_CHARACTER_PATHS);
        } else if (gender == GENDER_AGENDER) {
            selectableCharacterPaths = mergePaths(NO_SEX_CHARACTER_PATHS, MALE_CHARACTER_PATHS, FEMALE_CHARACTER_PATHS);
        } else {
            selectableCharacterPaths = mergePaths(MALE_CHARACTER_PATHS, NO_SEX_CHARACTER_PATHS);
        }
        if (selectedCharacterIndex >= selectableCharacterPaths.length) {
            selectedCharacterIndex = 0;
        }
    }

    private void refreshPreview() {
        try {
            Image bg = ImageTransformUtils.loadResource(BACKGROUND_PATHS[selectedBackgroundIndex]);
            Image role = ImageTransformUtils.loadResource(selectableCharacterPaths[selectedCharacterIndex]);
            previewImage32 = ImageTransformUtils.composeLayers(bg, role, 32, 64);
            previewImage64 = ImageTransformUtils.scale(previewImage32, 64, 128);
        } catch (IOException e) {
            previewImage32 = null;
            previewImage64 = null;
        }
    }

    private boolean applyPreviewFromBase64(CharacterSlot slot, String previewBase64) {
        if (previewBase64 == null || previewBase64.length() == 0) {
            return false;
        }
        try {
            byte[] data = Base64.decode(previewBase64);
            if (data == null || data.length == 0) {
                return false;
            }
            Image img = Image.createImage(data, 0, data.length);
            slot.preview64 = ImageTransformUtils.scale(img, 64, 128);
            slot.preview32 = ImageTransformUtils.scale(img, 32, 64);
            return true;
        } catch (Throwable t) {
            slot.preview32 = null;
            slot.preview64 = null;
            return false;
        }
    }

    private boolean applyPreviewFromPaths(CharacterSlot slot, String backgroundPath, String characterPath) {
        try {
            if (backgroundPath == null || characterPath == null) {
                return false;
            }
            Image bg = ImageTransformUtils.loadResource(backgroundPath);
            Image role = ImageTransformUtils.loadResource(characterPath);
            slot.preview32 = ImageTransformUtils.composeLayers(bg, role, 32, 64);
            slot.preview64 = ImageTransformUtils.scale(slot.preview32, 64, 128);
            return true;
        } catch (Throwable t) {
            slot.preview32 = null;
            slot.preview64 = null;
            return false;
        }
    }

    private int resolveBackgroundId(String backgroundPath) {
        int i;
        for (i = 0; i < BACKGROUND_PATHS.length; i++) {
            if (BACKGROUND_PATHS[i].equals(backgroundPath)) {
                return i + 1;
            }
        }
        return 1;
    }

    private int resolveCharacterId(String characterPath) {
        int i;
        for (i = 0; i < MALE_CHARACTER_PATHS.length; i++) {
            if (MALE_CHARACTER_PATHS[i].equals(characterPath)) {
                return i + 1;
            }
        }
        for (i = 0; i < FEMALE_CHARACTER_PATHS.length; i++) {
            if (FEMALE_CHARACTER_PATHS[i].equals(characterPath)) {
                return 101 + i;
            }
        }
        for (i = 0; i < NO_SEX_CHARACTER_PATHS.length; i++) {
            if (NO_SEX_CHARACTER_PATHS[i].equals(characterPath)) {
                return 201 + i;
            }
        }
        return 1;
    }

    private String resolveBackgroundPathById(int backgroundId, String legacyPath) {
        if (backgroundId >= 1 && backgroundId <= BACKGROUND_PATHS.length) {
            return BACKGROUND_PATHS[backgroundId - 1];
        }
        return legacyPath;
    }

    private String resolveCharacterPathById(int characterId, String legacyPath) {
        if (characterId >= 1 && characterId <= 3) {
            return MALE_CHARACTER_PATHS[characterId - 1];
        }
        if (characterId >= 101 && characterId <= 104) {
            return FEMALE_CHARACTER_PATHS[characterId - 101];
        }
        if (characterId >= 201 && characterId <= 202) {
            return NO_SEX_CHARACTER_PATHS[characterId - 201];
        }
        return legacyPath;
    }

    private static String[] mergePaths(String[] a, String[] b) {
        return mergePaths(a, b, null);
    }

    private static String[] mergePaths(String[] a, String[] b, String[] c) {
        int n = a.length + b.length + (c == null ? 0 : c.length);
        String[] out = new String[n];
        int i = 0;
        int p;
        for (p = 0; p < a.length; p++) {
            out[i++] = a[p];
        }
        for (p = 0; p < b.length; p++) {
            out[i++] = b[p];
        }
        if (c != null) {
            for (p = 0; p < c.length; p++) {
                out[i++] = c[p];
            }
        }
        return out;
    }

    private static final class CharacterSlot {
        int id;
        String name;
        String gender;
        String backgroundPath;
        String characterPath;
        Image preview32;
        Image preview64;
    }
}
