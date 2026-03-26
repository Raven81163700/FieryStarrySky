package com.aurora.character;

public final class AvatarDraft {

    public static final int GENDER_MALE = 0;
    public static final int GENDER_FEMALE = 1;
    public static final int GENDER_AGENDER = 2;

    public static final int WIDTH = 32;
    public static final int HEIGHT = 64;

    public static final int FACE_X = 8;
    public static final int FACE_Y = 8;
    public static final int FACE_W = 16;
    public static final int FACE_H = 16;

    public static final int BODY_PRESET_COUNT = 3;
    public static final int SKIN_PRESET_COUNT = 6;
    public static final int EYE_PRESET_COUNT = 4;
    public static final int NOSE_PRESET_COUNT = 4;
    public static final int MOUTH_PRESET_COUNT = 4;
    public static final int ACCESSORY_PRESET_COUNT = 4;

    public String roleName = "";
    public int gender = GENDER_MALE;
    public int bodyPreset = 0;

    public int skinPreset = 2;
    public int skinR = 224;
    public int skinG = 177;
    public int skinB = 132;

    public int eyePreset = 0;
    public int nosePreset = 0;
    public int mouthPreset = 0;

    public int accessoryPreset = 0;

    public int selectedPaintColor = 0x000000;

    // -1 表示不覆盖
    private final int[] facePaint = new int[FACE_W * FACE_H];
    private final int[] bodyPaint = new int[WIDTH * HEIGHT];

    public AvatarDraft() {
        clearPaint();
    }

    public void clearPaint() {
        int i;
        for (i = 0; i < facePaint.length; i++) {
            facePaint[i] = -1;
        }
        for (i = 0; i < bodyPaint.length; i++) {
            bodyPaint[i] = -1;
        }
    }

    public int getFacePaint(int x, int y) {
        return facePaint[y * FACE_W + x];
    }

    public void setFacePaint(int x, int y, int color) {
        facePaint[y * FACE_W + x] = color;
    }

    public int getBodyPaint(int x, int y) {
        return bodyPaint[y * WIDTH + x];
    }

    public void setBodyPaint(int x, int y, int color) {
        bodyPaint[y * WIDTH + x] = color;
    }

    public String genderLabel() {
        if (gender == GENDER_FEMALE) {
            return "女";
        }
        if (gender == GENDER_AGENDER) {
            return "无性";
        }
        return "男";
    }

    public AvatarDraft copy() {
        AvatarDraft d = new AvatarDraft();
        d.roleName = roleName;
        d.gender = gender;
        d.bodyPreset = bodyPreset;
        d.skinPreset = skinPreset;
        d.skinR = skinR;
        d.skinG = skinG;
        d.skinB = skinB;
        d.eyePreset = eyePreset;
        d.nosePreset = nosePreset;
        d.mouthPreset = mouthPreset;
        d.accessoryPreset = accessoryPreset;
        d.selectedPaintColor = selectedPaintColor;

        int i;
        for (i = 0; i < facePaint.length; i++) {
            d.facePaint[i] = facePaint[i];
        }
        for (i = 0; i < bodyPaint.length; i++) {
            d.bodyPaint[i] = bodyPaint[i];
        }
        return d;
    }
}
