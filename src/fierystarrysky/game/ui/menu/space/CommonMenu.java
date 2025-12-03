/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fierystarrysky.game.ui.menu.space;

import fierystarrysky.game.canvas.SpaceCanvas;
import fierystarrysky.game.ui.menu.BaseMenu;
import fierystarrysky.ui.SingleChoiceManager;

/**
 *
 * @author Raven
 */
public class CommonMenu extends BaseMenu {

    public CommonMenu(int x, int y, int width, int itemHeight) {
        super(x, y, width, itemHeight);
        addItem(new MenuItem("视角", 0));
        addItem(new MenuItem("开发者菜单", 1));
        totalHeight = itemHeight * menuItems.size();
        //这个菜单从底部向上
        this.y -= totalHeight;
    }

    public void select(final SpaceCanvas canvas) {
        int menuCode = ((BaseMenu.MenuItem)menuItems.elementAt(selectedIndex)).getCode();
        switch (menuCode) {
            case 0:
                String[] options = new String[]{"10km", "1km", "500m", "100m", "10m"};
                final float[] values = new float[]{0.1f, 1, 2, 10, 100};
                SingleChoiceManager singleChoiceManager = new SingleChoiceManager("像素比例尺", options, 3, canvas) {
                    protected void onOk(int selectedIndex) {
                        canvas.zoomScale = values[selectedIndex];
                        canvas.clearMenu();
                        super.backToCanvas();
                    }

                    protected void onCancel() {
                        canvas.clearMenu();
                        super.backToCanvas();
                    }
                };
                singleChoiceManager.showMenu();
                break;
            case 1:
                break;
        }
    }
}
