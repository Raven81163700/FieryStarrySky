/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fierystarrysky.game.ui.menu;

import fierystarrysky.game.canvas.SpaceCanvas;
import fierystarrysky.util.ColorUtils;
import fierystarrysky.util.FontUtils;
import java.util.Vector;
import javax.microedition.lcdui.Graphics;

/**
 *
 * @author Raven
 */
public abstract class BaseMenu {

    protected Vector menuItems;   // 存储菜单项（字符串）
    protected int selectedIndex;  // 当前选中项
    protected int x, y;           // 菜单左上角偏移
    protected int itemHeight;     // 每项高度
    protected int width;          // 菜单宽度
    protected int colorNormal = ColorUtils.white;
    protected int colorSelected = ColorUtils.yellow;
    protected int colorBackground = ColorUtils.grayBlue;
    protected int totalHeight;
    //可选项
    protected boolean drawHeader = false;
    protected int headerW, headerH;
    protected String title; //标题

    public BaseMenu(int x, int y, int width, int itemHeight) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.itemHeight = itemHeight;
        this.menuItems = new Vector();
        this.selectedIndex = 0;
    }

    // 添加菜单项
    public void addItem(MenuItem item) {
        menuItems.addElement(item);
    }

    // 绘制菜单
    public void draw(Graphics g) {
        int startY = y;
        if (drawHeader) {
            g.setColor(colorBackground);
            g.fillRect(x, y, headerW, headerH);
            g.setColor(colorNormal);
            g.setFont(FontUtils.getSmall());
            int drawY = y + headerH / 2 - FontUtils.getSmallHeight() / 2;
            g.drawString(title, x + headerW / 2, drawY, Graphics.HCENTER | Graphics.TOP);

            startY += headerH; //头部占用空间偏移
        }
        for (int i = 0; i < menuItems.size(); i++) {
            MenuItem item = (MenuItem) menuItems.elementAt(i);
            int drawY = startY + i * itemHeight;
            g.setColor(colorBackground);
            g.fillRect(x, drawY, width, itemHeight);
            if (i == selectedIndex) {
                g.setColor(colorSelected);
            } else {
                g.setColor(colorNormal);
            }
            g.setFont(FontUtils.getSmall());
            int itemDrawY = drawY + itemHeight / 2 - FontUtils.getSmallHeight() / 2;
            g.drawString(item.getText(), x + width / 2, itemDrawY, Graphics.HCENTER | Graphics.TOP);
        }
    }

    // 向上选择
    public void selectUp() {
        selectedIndex--;
        if (selectedIndex < 0) {
            selectedIndex = menuItems.size() - 1;
        }
    }

    // 向下选择
    public void selectDown() {
        selectedIndex++;
        if (selectedIndex >= menuItems.size()) {
            selectedIndex = 0;
        }
    }

    public int getItemAt(int clickX, int clickY) {
        int startY = y;
        if (drawHeader){
            startY += headerH;
        }
        for (int i = 0; i < menuItems.size(); i++) {
            int itemX = x;
            int itemY = startY + i * itemHeight;
            int itemW = width;
            int itemH = itemHeight;

            if (clickX >= itemX && clickX < itemX + itemW
                    && clickY >= itemY && clickY < itemY + itemH) {
                selectedIndex = i;  // 更新选中项
                return i;
            }
        }
        return -1; // 没有点击到任何菜单项
    }

    // 执行选中项动作（由子类实现）
    public abstract void select(SpaceCanvas canva);

    public boolean isDrawHeader() {
        return drawHeader;
    }

    public void setDrawHeader(boolean drawHeader) {
        this.drawHeader = drawHeader;
    }

    public int getHeaderW() {
        return headerW;
    }

    public void setHeaderW(int headerW) {
        this.headerW = headerW;
    }

    public int getHeaderH() {
        return headerH;
    }

    public void setHeaderH(int headerH) {
        this.headerH = headerH;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public class MenuItem{
        private String menuText;
        private int menuCode;
        public MenuItem(String menuText, int menuCode){
            this.menuCode = menuCode;
            this.menuText = menuText;
        }
        public String getText(){
            return menuText;
        }
        public int getCode(){
            return menuCode;
        }
    }
}
