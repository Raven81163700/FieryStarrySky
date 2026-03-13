/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fierystarrysky.game.ui.menu.space;

import fierystarrysky.game.canvas.SpaceCanvas;
import fierystarrysky.game.ui.menu.BaseMenu;
import fierystarrysky.game.object.SpaceObject;
import fierystarrysky.ui.InputManager;
import javax.microedition.lcdui.TextField;

/**
 *
 * @author Raven
 */
public class ObjectMenu extends BaseMenu {

    private SpaceObject object;

    public ObjectMenu(int x, int y, int width, int itemHeight, SpaceObject object) {
        super(x, y, width, itemHeight);
        addItem(new MenuItem("环绕", 0));
        addItem(new MenuItem("接近", 1));
        addItem(new MenuItem("锁定", 3));
        addItem(new MenuItem("返回", 2));
        totalHeight = itemHeight * menuItems.size();

        this.object = object;
    }

    public void select(final SpaceCanvas canvas) {
        //当前选中的菜单项
        int menuCode = ((BaseMenu.MenuItem)menuItems.elementAt(selectedIndex)).getCode();
        switch (menuCode) {
            case 0:
                //环绕
                InputManager inputOrbitManager = new InputManager("环绕距离（千米）", "0.5", 10, TextField.DECIMAL, canvas) {
                    protected void onOk(String text) {
                        if (text != null) {
                            try {
                                float value = Float.parseFloat(text);
                                canvas.getShipStatus().setTargetObject(object);
                                canvas.getShipStatus().setLockedObject(object);
                                canvas.getPlayerMovement().startAutoOrbit(object.getX(), object.getY(), value);
                                canvas.setStatus("正在环绕 " + object.getDisplayName(), -1, 0);
                            } catch (NumberFormatException e) {
                            }
                        }
                        canvas.clearMenu();
                        super.backToCanvas();
                    }

                    protected void onCancel() {
                        canvas.clearMenu();
                        super.backToCanvas();
                    }
                };
                inputOrbitManager.showInput();
                break;
            case 1:
                //接近
                //调用输入管理器
                InputManager inputApprorachManager = new InputManager("保持距离（千米）", "0.5", 10, TextField.DECIMAL, canvas) {
                    protected void onOk(String text) {
                        if (text != null) {
                            try {
                                float value = Float.parseFloat(text);
                                canvas.getShipStatus().setTargetObject(object);
                                canvas.getShipStatus().setLockedObject(object);
                                canvas.getPlayerMovement().startAutoApproach(object.getX(), object.getY(), value);
                                canvas.setStatus("正在接近 " + object.getDisplayName(), -1, 0);
                            } catch (NumberFormatException e) {
                            }
                        }
                        canvas.clearMenu();
                        super.backToCanvas();
                    }

                    protected void onCancel() {
                        canvas.clearMenu();
                        super.backToCanvas();
                    }
                };
                inputApprorachManager.showInput();
                break;
            case 2:
                //停止显示菜单
                canvas.clearMenu();
                break;
            case 3:
                //菜单操作为“锁定”目标，和点击选中逻辑分离
                canvas.getShipStatus().setLockedObject(object);
                canvas.setStatus("已锁定目标 " + object.getDisplayName(), -1, 1500);
                canvas.clearMenu();
                break;
        }
    }
}
