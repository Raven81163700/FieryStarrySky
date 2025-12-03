/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fierystarrysky.game.ui.dialog;

import fierystarrysky.game.canvas.SpaceCanvas;
import fierystarrysky.util.ColorUtils;
import fierystarrysky.util.FontUtils;
import javax.microedition.lcdui.Graphics;

/**
 * 游戏内对话框（支持自动消失和手动点击两种模式）
 *
 * @author Raven
 */
public class BaseDialog {

    public static final int TYPE_INFO = 0;   // 仅提醒（自动消失）
    public static final int TYPE_CONFIRM = 1; // 玩家操作（手动关闭）
    private String message;       // 显示的内容
    private String title;         // 标题
    private int type;             // 对话框类型
    // 界面尺寸与位置
    private int x, y, width, height;
    // 自动消失相关
    private long createTime;
    private long duration;        // 毫秒
    // 交互相关
    private String[] buttons;     // 按钮文本
    private int selectedIndex;    // 当前选中按钮
    private boolean closed;       // 是否已关闭
    private boolean isShow = false; //是否已经展示

    public BaseDialog(String title, String message, int x, int y, int width, int height, int type) {
        this.title = title;
        this.message = message;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
        this.selectedIndex = 0;
        this.closed = false;

        if (type == TYPE_INFO) {
            this.createTime = System.currentTimeMillis();
            this.duration = 3000; // 默认3秒
        } else {
            this.buttons = new String[]{"确定", "取消"};
        }
    }

    /**
     * 设置自动消失时长（仅 TYPE_INFO 有效）
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * 设置交互按钮（仅 TYPE_CONFIRM 有效）
     */
    public void setButtons(String[] buttons) {
        this.buttons = buttons;
    }

    /**
     * 是否已经过期/关闭
     */
    public boolean isClosed() {
        if (type == TYPE_INFO) {
            return (System.currentTimeMillis() - createTime) > duration;
        }
        return closed;
    }

    /**
     * 绘制对话框
     */
    public void draw(Graphics g, int screenW, int screenH) {
        // 背景
        g.setColor(ColorUtils.grayBlue);
        g.fillRect(x, y, width, height);

        // 边框
        g.setColor(ColorUtils.white);
        g.drawRect(x, y, width, height);

        // 标题
        if (title != null) {
            g.setFont(FontUtils.getSmall());
            g.drawString(title, x + width / 2, y + 4, Graphics.TOP | Graphics.HCENTER);
        }

        // 内容
        g.setFont(FontUtils.getSmall());
        int drawY = y + height / 2 - FontUtils.getSmallHeight() / 2;
        g.drawString(message, x + width / 2, drawY, Graphics.HCENTER | Graphics.TOP);

        // 如果是交互模式，绘制按钮
        if (type == TYPE_CONFIRM && buttons != null) {
            int btnY = y + height - 20;
            for (int i = 0; i < buttons.length; i++) {
                int btnX = x + (i * (width / buttons.length));
                int btnW = width / buttons.length;

                if (i == selectedIndex) {
                    g.setColor(ColorUtils.yellow);
                } else {
                    g.setColor(ColorUtils.white);
                }
                g.drawRect(btnX, btnY, btnW - 2, 18);
                int btnDrawY = btnY + 9 - FontUtils.getSmallHeight() / 2;
                g.drawString(buttons[i], btnX + btnW / 2, btnDrawY, Graphics.HCENTER | Graphics.TOP);
            }
        }
    }

    /**
     * 按钮选择控制
     */
    public void selectLeft() {
        if (type == TYPE_CONFIRM && buttons != null) {
            selectedIndex--;
            if (selectedIndex < 0) {
                selectedIndex = buttons.length - 1;
            }
        }
    }

    public void selectRight() {
        if (type == TYPE_CONFIRM && buttons != null) {
            selectedIndex++;
            if (selectedIndex >= buttons.length) {
                selectedIndex = 0;
            }
        }
    }

    /**
     * 确认按钮
     */
    public void confirm() {
        if (type == TYPE_CONFIRM) {
            // 这里可以回调或直接关闭
            closed = true;
            System.out.println("Dialog选择了: " + buttons[selectedIndex]);
        }
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean bool) {
        isShow = bool;
    }

    public int getType() {
        return type;
    }

    public void setCreateTime(long time) {
        createTime = time;
    }
}