/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fierystarrysky.game.ui.menu.space;

import fierystarrysky.game.canvas.SpaceCanvas;
import fierystarrysky.game.ui.menu.BaseMenu;
import fierystarrysky.ui.FormManager;

/**
 *
 * @author chenb
 */
public class ActionMenu extends BaseMenu {

    public ActionMenu(int x, int y, int width, int itemHeight) {
        super(x, y, width, itemHeight);
        addItem(new MenuItem("舰船信息", 0));
        totalHeight = itemHeight * menuItems.size();
        //这个菜单从底部向上
        this.y -= totalHeight;
    }

    public void select(final SpaceCanvas canvas) {
        int menuCode = ((BaseMenu.MenuItem) menuItems.elementAt(selectedIndex)).getCode();
        switch (menuCode) {
            case 0:
                FormManager formManager = new FormManager("信息", canvas) {
                    protected void onOk() {
                        canvas.clearMenu();
                        super.backToCanvas();
                    }

                    protected void onCancel() {
                        canvas.clearMenu();
                        super.backToCanvas();
                    }
                };
                formManager.addText("名称: " + canvas.getShipStatus().getName());
                formManager.addText("描述: " + canvas.getShipStatus().getDescription());

                formManager.addText("最大速度: " + canvas.getShipStatus().getMaxSpeed());
                formManager.addText("加速度: " + canvas.getShipStatus().getAcceleration());
                formManager.addText("减速倍率: " + canvas.getShipStatus().getBrakingRatio());
                formManager.addText("转向速度: " + canvas.getShipStatus().getTurnRate());

                formManager.addText("最大护盾: " + canvas.getShipStatus().getMaxShield());
                formManager.addText("最大装甲: " + canvas.getShipStatus().getMaxArmor());
                formManager.addText("最大结构: " + canvas.getShipStatus().getMaxStructure());
                formManager.addText("最大能量: " + canvas.getShipStatus().getMaxPower());

                formManager.addText("护盾回复时间: " + canvas.getShipStatus().getShieldRechargeTime());
                formManager.addText("能量回复时间: " + canvas.getShipStatus().getPowerRechargeTime());

                formManager.addText("护盾-热能抗性: " + canvas.getShipStatus().getShieldResistance().getThermalResistance());
                formManager.addText("护盾-爆炸抗性: " + canvas.getShipStatus().getShieldResistance().getExplosionResistance());
                formManager.addText("护盾-电能抗性: " + canvas.getShipStatus().getShieldResistance().getElectricResistance());
                formManager.addText("护盾-动能抗性: " + canvas.getShipStatus().getShieldResistance().getKineticResistance());

                formManager.addText("装甲-热能抗性: " + canvas.getShipStatus().getArmorResistance().getThermalResistance());
                formManager.addText("装甲-爆炸抗性: " + canvas.getShipStatus().getArmorResistance().getExplosionResistance());
                formManager.addText("装甲-电能抗性: " + canvas.getShipStatus().getArmorResistance().getElectricResistance());
                formManager.addText("装甲-动能抗性: " + canvas.getShipStatus().getArmorResistance().getKineticResistance());

                formManager.addText("结构-热能抗性: " + canvas.getShipStatus().getStructureResistance().getThermalResistance());
                formManager.addText("结构-爆炸抗性: " + canvas.getShipStatus().getStructureResistance().getExplosionResistance());
                formManager.addText("结构-电能抗性: " + canvas.getShipStatus().getStructureResistance().getElectricResistance());
                formManager.addText("结构-动能抗性: " + canvas.getShipStatus().getStructureResistance().getKineticResistance());

                formManager.addText("挂载槽: " + canvas.getShipStatus().getMountingSlot());
                formManager.addText("功能槽: " + canvas.getShipStatus().getFunctionSlot());
                formManager.addText("模块槽: " + canvas.getShipStatus().getModuleSlot());
                formManager.addText("附加槽: " + canvas.getShipStatus().getAdditionalSlot());

                formManager.showForm();
                break;
        }
    }
}
