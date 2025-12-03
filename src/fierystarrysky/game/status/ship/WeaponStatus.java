/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fierystarrysky.game.status.ship;

import fierystarrysky.model.item.WeaponModel;

/**
 *
 * @author chenb
 */
public class WeaponStatus extends MountingSlotEquipmentStatus {
    //炮塔专有

    private float finalTrackingRate; //追踪速度
    //导弹专有
    private float finalTrackingSpeed; //飞行速度
    private float finalExplosionRadius; //爆炸半径
    //通用属性
    private float finalEffectiveRange; //有效射程

    public WeaponStatus(WeaponModel model) {
        super(model);
        int weaponType = model.getWeaponType();
        if (weaponType == WeaponModel.TYPE_WEAPON_GUN){
            this.finalTrackingRate = model.getTrackingRate();
        }else if(weaponType == WeaponModel.TYPE_WEAPON_MISSILE){
            this.finalExplosionRadius = model.getExplosionRadius();
            this.finalTrackingSpeed = model.getTrackingSpeed();
        }
        finalEffectiveRange = model.getEffectiveRange();
    }
}
