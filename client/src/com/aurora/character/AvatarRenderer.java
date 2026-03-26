package com.aurora.character;

import javax.microedition.lcdui.Graphics;

public final class AvatarRenderer {

    private static final int[][] SKIN_PRESETS = {
            {255, 224, 189},
            {241, 194, 125},
            {224, 177, 132},
            {198, 134, 66},
            {141, 85, 36},
            {92, 60, 33}
    };

    // 每个体态预设中的皮肤矩形元数据: x,y,w,h
    private static final int[][][] SKIN_BLOCKS = {
            {
                    {11, 8, 10, 10}, {9, 18, 14, 6}, {6, 28, 4, 14}, {22, 28, 4, 14}
            },
            {
                    {10, 8, 12, 10}, {9, 18, 14, 6}, {5, 28, 4, 14}, {23, 28, 4, 14}
            },
            {
                    {11, 8, 10, 10}, {9, 18, 14, 6}, {4, 28, 5, 14}, {23, 28, 5, 14}
            }
    };

    private static final int[] PALETTE = {
            0x000000, 0xFFFFFF, 0xD35400, 0x3498DB, 0x2ECC71, 0xE91E63, 0xF1C40F, 0x9B59B6
    };

    private AvatarRenderer() {
    }

    public static int[] getPalette() {
        return PALETTE;
    }

    public static void applySkinPreset(AvatarDraft draft) {
        int idx = clamp(draft.skinPreset, 0, SKIN_PRESETS.length - 1);
        draft.skinR = SKIN_PRESETS[idx][0];
        draft.skinG = SKIN_PRESETS[idx][1];
        draft.skinB = SKIN_PRESETS[idx][2];
    }

    public static void drawAvatar(Graphics g, AvatarDraft draft, int x, int y, int scale) {
        drawBodyBase(g, draft, x, y, scale);
        drawSkinBlocks(g, draft, x, y, scale);
        drawFaceFeatures(g, draft, x, y, scale);
        drawFacePaint(g, draft, x, y, scale);
        drawAccessory(g, draft, x, y, scale);
        drawBodyPaint(g, draft, x, y, scale);
        g.setColor(0xFFFFFF);
        g.drawRect(x - 1, y - 1, AvatarDraft.WIDTH * scale + 1, AvatarDraft.HEIGHT * scale + 1);
    }

    private static void drawBodyBase(Graphics g, AvatarDraft draft, int x, int y, int scale) {
        int clothA;
        int clothB;
        if (draft.bodyPreset == 1) {
            clothA = 0x3F51B5;
            clothB = 0x283593;
        } else if (draft.bodyPreset == 2) {
            clothA = 0x00897B;
            clothB = 0x00695C;
        } else {
            clothA = 0x8E44AD;
            clothB = 0x6C3483;
        }

        fill(g, x, y, 9, 24, 14, 22, scale, clothA);
        fill(g, x, y, 10, 46, 5, 14, scale, clothB);
        fill(g, x, y, 17, 46, 5, 14, scale, clothB);

        if (draft.gender == AvatarDraft.GENDER_FEMALE) {
            fill(g, x, y, 8, 36, 16, 12, scale, clothA);
        } else if (draft.gender == AvatarDraft.GENDER_AGENDER) {
            fill(g, x, y, 8, 36, 16, 10, scale, clothA);
            fill(g, x, y, 9, 46, 14, 3, scale, 0x455A64);
        }

        // 发型基底
        fill(g, x, y, 9, 6, 14, 8, scale, 0x4E342E);
        if (draft.bodyPreset == 1) {
            fill(g, x, y, 8, 10, 2, 8, scale, 0x4E342E);
            fill(g, x, y, 22, 10, 2, 8, scale, 0x4E342E);
        }
        if (draft.bodyPreset == 2) {
            fill(g, x, y, 8, 6, 16, 3, scale, 0x263238);
        }
    }

    private static void drawSkinBlocks(Graphics g, AvatarDraft draft, int x, int y, int scale) {
        int[][] blocks = SKIN_BLOCKS[clamp(draft.bodyPreset, 0, SKIN_BLOCKS.length - 1)];
        int skinColor = rgb(draft.skinR, draft.skinG, draft.skinB);
        int i;
        for (i = 0; i < blocks.length; i++) {
            fill(g, x, y, blocks[i][0], blocks[i][1], blocks[i][2], blocks[i][3], scale, skinColor);
        }
    }

    private static void drawFaceFeatures(Graphics g, AvatarDraft draft, int x, int y, int scale) {
        int fx = AvatarDraft.FACE_X;
        int fy = AvatarDraft.FACE_Y;

        // 眼睛
        int eyeY = fy + 6 + (draft.eyePreset % 2);
        if (draft.eyePreset == 0) {
            fill(g, x, y, fx + 4, eyeY, 2, 1, scale, 0x111111);
            fill(g, x, y, fx + 10, eyeY, 2, 1, scale, 0x111111);
        } else if (draft.eyePreset == 1) {
            fill(g, x, y, fx + 4, eyeY, 2, 2, scale, 0x111111);
            fill(g, x, y, fx + 10, eyeY, 2, 2, scale, 0x111111);
        } else if (draft.eyePreset == 2) {
            fill(g, x, y, fx + 3, eyeY, 3, 1, scale, 0x111111);
            fill(g, x, y, fx + 10, eyeY, 3, 1, scale, 0x111111);
        } else {
            fill(g, x, y, fx + 4, eyeY, 1, 1, scale, 0x111111);
            fill(g, x, y, fx + 11, eyeY, 1, 1, scale, 0x111111);
        }

        // 鼻子
        if (draft.nosePreset == 0) {
            fill(g, x, y, fx + 8, fy + 9, 1, 2, scale, 0x8D6E63);
        } else if (draft.nosePreset == 1) {
            fill(g, x, y, fx + 7, fy + 10, 2, 1, scale, 0x8D6E63);
        } else if (draft.nosePreset == 2) {
            fill(g, x, y, fx + 8, fy + 10, 1, 1, scale, 0x8D6E63);
            fill(g, x, y, fx + 9, fy + 11, 1, 1, scale, 0x8D6E63);
        } else {
            fill(g, x, y, fx + 8, fy + 10, 2, 1, scale, 0x8D6E63);
        }

        // 嘴巴
        if (draft.mouthPreset == 0) {
            fill(g, x, y, fx + 6, fy + 13, 4, 1, scale, 0x5D4037);
        } else if (draft.mouthPreset == 1) {
            fill(g, x, y, fx + 6, fy + 13, 5, 1, scale, 0xAD1457);
        } else if (draft.mouthPreset == 2) {
            fill(g, x, y, fx + 6, fy + 13, 2, 1, scale, 0x5D4037);
            fill(g, x, y, fx + 9, fy + 13, 2, 1, scale, 0x5D4037);
        } else {
            fill(g, x, y, fx + 6, fy + 14, 4, 1, scale, 0x5D4037);
        }
    }

    private static void drawFacePaint(Graphics g, AvatarDraft draft, int x, int y, int scale) {
        int px;
        int py;
        for (py = 0; py < AvatarDraft.FACE_H; py++) {
            for (px = 0; px < AvatarDraft.FACE_W; px++) {
                int c = draft.getFacePaint(px, py);
                if (c != -1) {
                    fill(g, x, y, AvatarDraft.FACE_X + px, AvatarDraft.FACE_Y + py, 1, 1, scale, c);
                }
            }
        }
    }

    private static void drawAccessory(Graphics g, AvatarDraft draft, int x, int y, int scale) {
        int fx = AvatarDraft.FACE_X;
        int fy = AvatarDraft.FACE_Y;
        if (draft.accessoryPreset == 1) {
            fill(g, x, y, fx + 2, fy + 6, 12, 2, scale, 0x2C3E50); // 眼罩
        } else if (draft.accessoryPreset == 2) {
            fill(g, x, y, fx + 10, fy + 11, 3, 2, scale, 0xF5CBA7); // 创可贴
            fill(g, x, y, fx + 11, fy + 11, 1, 2, scale, 0xE74C3C);
        } else if (draft.accessoryPreset == 3) {
            fill(g, x, y, fx + 4, fy + 2, 8, 2, scale, 0x616161); // 头带
        }
    }

    private static void drawBodyPaint(Graphics g, AvatarDraft draft, int x, int y, int scale) {
        int px;
        int py;
        for (py = 0; py < AvatarDraft.HEIGHT; py++) {
            for (px = 0; px < AvatarDraft.WIDTH; px++) {
                int c = draft.getBodyPaint(px, py);
                if (c != -1) {
                    fill(g, x, y, px, py, 1, 1, scale, c);
                }
            }
        }
    }

    private static void fill(Graphics g, int baseX, int baseY, int x, int y, int w, int h, int scale, int color) {
        g.setColor(color);
        g.fillRect(baseX + x * scale, baseY + y * scale, w * scale, h * scale);
    }

    private static int rgb(int r, int g, int b) {
        return (clamp(r, 0, 255) << 16) | (clamp(g, 0, 255) << 8) | clamp(b, 0, 255);
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
}
