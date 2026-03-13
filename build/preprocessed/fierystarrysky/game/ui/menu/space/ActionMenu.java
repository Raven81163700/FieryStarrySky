/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fierystarrysky.game.ui.menu.space;

import fierystarrysky.game.canvas.SpaceCanvas;
import fierystarrysky.game.ui.menu.BaseMenu;
import fierystarrysky.ui.FormManager;
import fierystarrysky.ui.SingleChoiceManager;
import fierystarrysky.model.item.WeaponModel;
import fierystarrysky.game.status.ship.WeaponStatus;
import fierystarrysky.model.item.WeaponModel.MissileWeaponLoader;
import fierystarrysky.game.status.ship.MountingSlotEquipmentStatus;

/**
 *
 * @author chenb
 */
public class ActionMenu extends BaseMenu {

    public ActionMenu(int x, int y, int width, int itemHeight) {
        super(x, y, width, itemHeight);
        addItem(new MenuItem("舰船信息", 0));
        addItem(new MenuItem("挂载管理", 1));
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
            case 1:
                // Mount management
                int slotCount = canvas.getShipStatus().getShipSlot().getMountingSlotCount();
                String[] options = new String[slotCount];
                for (int i = 0; i < slotCount; i++) {
                    MountingSlotEquipmentStatus status = canvas.getShipStatus().getShipSlot().getMountingSlotAt(i);
                    if (status == null) {
                        options[i] = "槽 " + (i + 1) + ": 空";
                    } else {
                        options[i] = "槽 " + (i + 1) + ": 已装";
                    }
                }
                SingleChoiceManager scm = new SingleChoiceManager("挂载管理", options, 0, canvas) {
                    protected void onOk(int selectedIndex) {
                        final int slotIndex = selectedIndex;
                        MountingSlotEquipmentStatus s = canvas.getShipStatus().getShipSlot().getMountingSlotAt(slotIndex);
                        if (s == null) {
                            // offer to install a test missile (demo)
                            String[] installOpts = new String[]{"安装 测试导弹"};
                            SingleChoiceManager installMenu = new SingleChoiceManager("安装", installOpts, 0, canvas) {
                                protected void onOk(int selectedIndex) {
                                    // create and install test missile
                                    WeaponModel wm = (WeaponModel) new MissileWeaponLoader("6930471d-52b8-4101-9aa8-18f00f5feaa4").getInfo();
                                    WeaponStatus ws = new WeaponStatus(wm);
                                    canvas.getShipStatus().getShipSlot().installMountingSlotAt(slotIndex, ws);
                                    canvas.setStatus("已安装导弹", -1, 2000);
                                    canvas.clearMenu();
                                    super.backToCanvas();
                                }

                                protected void onCancel() {
                                    canvas.clearMenu();
                                    super.backToCanvas();
                                }
                            };
                            installMenu.showMenu();
                        } else {
                            // offer to remove
                            String[] removeOpts = new String[]{"卸下装备"};
                            SingleChoiceManager removeMenu = new SingleChoiceManager("卸下", removeOpts, 0, canvas) {
                                protected void onOk(int selectedIndex) {
                                    canvas.getShipStatus().getShipSlot().removeMountingSlotAt(slotIndex);
                                    canvas.setStatus("已卸下", -1, 2000);
                                    canvas.clearMenu();
                                    super.backToCanvas();
                                }

                                protected void onCancel() {
                                    canvas.clearMenu();
                                    super.backToCanvas();
                                }
                            };
                            removeMenu.showMenu();
                        }
                    }

                    protected void onCancel() {
                        canvas.clearMenu();
                        super.backToCanvas();
                    }
                };
                scm.showMenu();
                break;
        }
    }
}
