/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fierystarrysky.model.ship;

import fierystarrysky.model.data.ResistanceModel;

/**
 *
 * @author Raven
 */
public class ShipModel {
    private String id;
    private String name;
    private String description;
    private float maxSpeed; //最大速度
    private float acceleration; //加速度
    private float brakingRatio; //减速倍率
    private float turnRate; //转向速度
    private float maxShield;
    private float maxArmor;
    private float maxStructure;
    private float maxPower;
    private float shieldRechargeTime;
    private float powerRechargeTime;
    private ResistanceModel shieldResistance;
    private ResistanceModel armorResistance;
    private ResistanceModel structureResistance;
    private int mountingSlot; //挂载槽
    private int functionSlot; //功能槽
    private int moduleSlot; //模块槽
    private int additionalSlot; //附加槽

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public float getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(float acceleration) {
        this.acceleration = acceleration;
    }

    public float getBrakingRatio() {
        return brakingRatio;
    }

    public void setBrakingRatio(float brakingRatio) {
        this.brakingRatio = brakingRatio;
    }

    public float getTurnRate() {
        return turnRate;
    }

    public void setTurnRate(float turnRate) {
        this.turnRate = turnRate;
    }

    public int getMountingSlot() {
        return mountingSlot;
    }

    public void setMountingSlot(int mountingSlot) {
        this.mountingSlot = mountingSlot;
    }

    public int getFunctionSlot() {
        return functionSlot;
    }

    public void setFunctionSlot(int functionSlot) {
        this.functionSlot = functionSlot;
    }

    public int getModuleSlot() {
        return moduleSlot;
    }

    public void setModuleSlot(int moduleSlot) {
        this.moduleSlot = moduleSlot;
    }

    public int getAdditionalSlot() {
        return additionalSlot;
    }

    public void setAdditionalSlot(int additionalSlot) {
        this.additionalSlot = additionalSlot;
    }

    public float getMaxShield() {
        return maxShield;
    }

    public void setMaxShield(float maxShield) {
        this.maxShield = maxShield;
    }

    public float getMaxArmor() {
        return maxArmor;
    }

    public void setMaxArmor(float maxArmor) {
        this.maxArmor = maxArmor;
    }

    public float getMaxStructure() {
        return maxStructure;
    }

    public void setMaxStructure(float maxStructure) {
        this.maxStructure = maxStructure;
    }

    public float getMaxPower() {
        return maxPower;
    }

    public void setMaxPower(float maxPower) {
        this.maxPower = maxPower;
    }

    public ResistanceModel getShieldResistance() {
        return shieldResistance;
    }

    public void setShieldResistance(ResistanceModel shieldResistance) {
        this.shieldResistance = shieldResistance;
    }

    public ResistanceModel getArmorResistance() {
        return armorResistance;
    }

    public void setArmorResistance(ResistanceModel armroResistance) {
        this.armorResistance = armroResistance;
    }

    public ResistanceModel getStructureResistance() {
        return structureResistance;
    }

    public void setStructureResistance(ResistanceModel structureResistance) {
        this.structureResistance = structureResistance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescirption() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getShieldRechargeTime() {
        return shieldRechargeTime;
    }

    public void setShieldRechargeTime(float shieldRechargeTime) {
        this.shieldRechargeTime = shieldRechargeTime;
    }

    public float getPowerRechargeTime() {
        return powerRechargeTime;
    }

    public void setPowerRechargeTime(float powerRechargeTime) {
        this.powerRechargeTime = powerRechargeTime;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
