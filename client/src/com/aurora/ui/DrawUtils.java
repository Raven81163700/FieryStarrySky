package com.aurora.ui;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public final class DrawUtils {

    private DrawUtils() {
    }

    public static void fillBackground(Graphics g, int width, int height) {
        g.setColor(0x142033);
        g.fillRect(0, 0, width, height);
    }

    public static void drawMainTitle(Graphics g, int width, String title) {
        g.setColor(0xFFFFFF);
        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM));
        g.drawString(title, width / 2, 8, Graphics.TOP | Graphics.HCENTER);
    }

    public static void drawSubtitle(Graphics g, int width, int y, String subtitle) {
        g.setColor(0xFFFFFF);
        g.setFont(Font.getDefaultFont());
        g.drawString(subtitle, width / 2, y, Graphics.TOP | Graphics.HCENTER);
    }

    public static void drawField(Graphics g, int x, int y, int w, int h,
                                 String label, String value, boolean focused) {
        g.setColor(0xB8C6DB);
        g.setFont(Font.getDefaultFont());
        g.drawString(label, x, y - 16, Graphics.TOP | Graphics.LEFT);

        g.setColor(focused ? 0x66B8FF : 0x5C6A84);
        g.drawRect(x, y, w, h);

        g.setColor(0xFFFFFF);
        if (value.length() == 0) {
            g.drawString("点击输入", x + 4, y + 5, Graphics.TOP | Graphics.LEFT);
        } else {
            g.drawString(value, x + 4, y + 5, Graphics.TOP | Graphics.LEFT);
        }
    }

    public static void drawButton(Graphics g, int x, int y, int w, int h, String text, boolean focused) {
        g.setColor(focused ? 0x2E7DFF : 0x22344C);
        g.fillRect(x, y, w, h);
        g.setColor(0xFFFFFF);
        g.drawRect(x, y, w, h);
        g.drawString(text, x + w / 2, y + 4, Graphics.TOP | Graphics.HCENTER);
    }

    public static void drawLatency(Graphics g, int width, int latencyMs) {
        String text = latencyMs >= 0 ? ("延迟 " + latencyMs + "ms") : "延迟 --ms";
        g.setColor(0x98A7C4);
        g.setFont(Font.getDefaultFont());
        g.drawString(text, width - 4, 4, Graphics.TOP | Graphics.RIGHT);
    }

    public static String mask(String s) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            sb.append('*');
        }
        return sb.toString();
    }
}
