/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fierystarrysky.game.status.ship;

import fierystarrysky.model.item.MountingSlotEquipmentModel;

/**
 *
 * @author chenb
 */
public class MountingSlotEquipmentStatus {

    private MountingSlotEquipmentModel model;
    private long finalCooldown;
    private long cooldownTimer;
    private float finalPowerCost;
    private int ammoNum = 0;

    public MountingSlotEquipmentStatus(MountingSlotEquipmentModel model) {
        this.model = model;
        //暂时不计算
        this.finalCooldown = model.getCoolDown();
        this.finalPowerCost = model.getPowerCost();
    }

    // 每帧调用
    public void update(long deltaTime) {
        if (cooldownTimer > 0) {
            cooldownTimer -= deltaTime;
            if (cooldownTimer < 0) {
                cooldownTimer = 0;
            }
        }
    }

    // 开火时调用
    public boolean tryFire() {
        if (cooldownTimer == 0) {
            cooldownTimer = finalCooldown;
            return true;
        }
        return false;
    }

    // getter
    public float getCooldownProgress() {
        return 1f - (cooldownTimer / finalCooldown); // 0~1 进度条
    }

    public int getAmmoNum() {
        return ammoNum;
    }

    public void setAmmoNum(int ammoNum) {
        this.ammoNum = ammoNum;
    }
}
