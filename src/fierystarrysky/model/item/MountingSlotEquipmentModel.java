/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fierystarrysky.model.item;

/**
 *
 * @author chenb
 */
public class MountingSlotEquipmentModel extends ShipEquipmentModel {

    public static final int TYPE_WEAPON = 0;
    public static final int TYPE_LOGISTICS = 1;
    public static final int TYPE_ECM = 2;
    
    /*public static final int TYPE_LOGISTICS_POWER = 2;
    public static final int TYPE_LOGISTICS_SHIELD = 3;
    public static final int TYPE_LOGISTICS_ARMOR = 4;
    public static final int TYPE_ECM_SENSOR = 5;*/
    
    public static final int SIZE_AIRCRAFT = 0;
    public static final int SIZE_SMALL = 1;
    public static final int SIZE_MEDIUM = 2;
    public static final int SIZE_LARGE = 3;
    public static final int SIZE_EXTRA_LARGE = 4;
    public static final int SIZE_FLAGSHIP = 5;
    //基本信息
    private int equipmentType;
    private int slotSize;
    //弹药
    private boolean needAmmo;
    private int ammoTypeId;
    private int ammoSizeId;
    private int ammoCapacity;
    //属性
    private long coolDown;
    private float powerCost;

    public int getEquipmentType() {
        return equipmentType;
    }

    public void setEquipmentType(int equipmentType) {
        this.equipmentType = equipmentType;
    }

    public int getSlotSize() {
        return slotSize;
    }

    public void setSlotSize(int slotSize) {
        this.slotSize = slotSize;
    }

    public boolean isNeedAmmo() {
        return needAmmo;
    }

    public void setNeedAmmo(boolean needAmmo) {
        this.needAmmo = needAmmo;
    }

    public int getAmmoCapacity() {
        return ammoCapacity;
    }

    public void setAmmoCapacity(int ammoCapacity) {
        this.ammoCapacity = ammoCapacity;
    }

    public long getCoolDown() {
        return coolDown;
    }

    public void setCoolDown(long coolDown) {
        this.coolDown = coolDown;
    }

    public float getPowerCost() {
        return powerCost;
    }

    public void setPowerCost(float powerCost) {
        this.powerCost = powerCost;
    }

    public int getAmmoTypeId() {
        return ammoTypeId;
    }

    public void setAmmoTypeId(int ammoTypeId) {
        this.ammoTypeId = ammoTypeId;
    }

    public int getAmmoSizeId() {
        return ammoSizeId;
    }

    public void setAmmoSizeId(int ammoSizeId) {
        this.ammoSizeId = ammoSizeId;
    }
}
