/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fierystarrysky.model.item;

import res.config.loader.ItemInfoLoader;

/**
 *
 * @author chenb
 */
public class WeaponModel extends MountingSlotEquipmentModel {

    public static final int TYPE_WEAPON_GUN = 0;
    public static final int TYPE_WEAPON_MISSILE = 1;
    private int weaponType;
    //炮塔专有
    private float trackingRate; //追踪速度
    //导弹专有
    private float trackingSpeed; //飞行速度
    private float explosionRadius; //爆炸半径
    //通用属性
    private float effectiveRange; //有效射程

    /*
    public WeaponModel(String itemId, String name, String description, int slotSize,
    boolean needAmmo, long coolDown, float trackingRate, float effectiveRange, float damage, int powerCost) {
    super(itemId, name, description, TYPE_WEAPON, slotSize, needAmmo, coolDown, powerCost);
    this.weaponType = TYPE_WEAPON_GUN;
    this.trackingRate = trackingRate;
    this.effectiveRange = effectiveRange;
    this.damage = damage;
    }
    public WeaponModel(String itemId, String name, String description, int slotSize,
    boolean needAmmo, long coolDown, float trackingSpeed, float explosionRadius, float effectiveRange, float damage, int powerCost) {
    super(itemId, name, description, TYPE_WEAPON, slotSize, needAmmo, coolDown, powerCost);
    this.weaponType = TYPE_WEAPON_MISSILE;
    this.trackingSpeed = trackingSpeed;
    this.explosionRadius = explosionRadius;
    this.effectiveRange = effectiveRange;
    this.damage = damage;
    }
     */
    public int getWeaponType() {
        return weaponType;
    }

    public void setWeaponType(int weaponType) {
        this.weaponType = weaponType;
    }

    public float getTrackingRate() {
        return trackingRate;
    }

    public void setTrackingRate(float trackingRate) {
        this.trackingRate = trackingRate;
    }

    public float getTrackingSpeed() {
        return trackingSpeed;
    }
    public void setTrackingSpeed(float trackingSpeed) {
        this.trackingSpeed = trackingSpeed;
    }

    public float getExplosionRadius() {
        return explosionRadius;
    }

    public void setExplosionRadius(float explosionRadius) {
        this.explosionRadius = explosionRadius;
    }

    public float getEffectiveRange() {
        return effectiveRange;
    }

    public void setEffectiveRange(float effectiveRange) {
        this.effectiveRange = effectiveRange;
    }
    
    //专有的loader
    public static class MissileWeaponLoader extends ItemInfoLoader{

        public MissileWeaponLoader(String id){
            super(id, new WeaponModel());
            loadExtend();
        }
        protected void loadExtend() {
            WeaponModel weaponModel = (WeaponModel) baseItemModel;
            //加载ShipEquipmentModel层
            String section = "slot";
            String key = "slotType";
            weaponModel.setSlotType(Integer.parseInt(iniUtils.get(section, key)));
            key = "equipmentType";
            weaponModel.setEquipmentType(Integer.parseInt(iniUtils.get(section, key)));
            key = "slotSize";
            weaponModel.setSlotSize(Integer.parseInt(iniUtils.get(section, key)));
            section = "ammo";
            key = "needAmmo";
            weaponModel.setNeedAmmo(iniUtils.get(section, key) == "true");
            key = "ammoTypeId";
            weaponModel.setAmmoTypeId(Integer.parseInt(iniUtils.get(section, key)));
            key = "ammoSizeId";
            weaponModel.setAmmoSizeId(Integer.parseInt(iniUtils.get(section, key)));
            key = "ammoCapacity";
            weaponModel.setAmmoCapacity(Integer.parseInt(iniUtils.get(section, key)));
            section = "attribute";
            key = "weaponType";
            weaponModel.setWeaponType(Integer.parseInt(iniUtils.get(section, key)));
            key = "coolDown";
            weaponModel.setCoolDown(Long.parseLong(iniUtils.get(section, key)));
            key = "powerCost";
            weaponModel.setPowerCost(Float.parseFloat(iniUtils.get(section, key)));
            key = "trackingSpeed";
            weaponModel.setTrackingSpeed(Float.parseFloat(iniUtils.get(section, key)));
            key = "explosionRadius";
            weaponModel.setExplosionRadius(Float.parseFloat(iniUtils.get(section, key)));
            key = "effectiveRange";
            weaponModel.setEffectiveRange(Float.parseFloat(iniUtils.get(section, key)));
        }
    }
}
