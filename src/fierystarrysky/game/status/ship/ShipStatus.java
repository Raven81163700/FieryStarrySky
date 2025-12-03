/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fierystarrysky.game.status.ship;

import fierystarrysky.game.object.SpaceObject;
import fierystarrysky.model.data.ResistanceModel;
import fierystarrysky.model.ship.ShipModel;

/**
 *
 * @author Raven
 */
public class ShipStatus {
    //初始化信息（TODO:其他Ship类应当从这里取，因为将在这里加载舰船配置文件）
    //固定属性

    private float maxSpeed;
    private float acceleration;
    private float brakingRatio;
    private float turnRate;
    private float maxShield;
    private float maxArmor;
    private float maxStructure;
    private float maxPower;
    private float shieldRechargeTime;
    private float powerRechargeTime;
    //抗性
    private ResistanceModel shieldResistance;
    private ResistanceModel armorResistance;
    private ResistanceModel structureResistance;
    //实时信息
    private int throttle;
    private float currentAngle; //当前朝向
    private float currentX, currentY; //当前所在世界坐标
    private float currentSpeed; //当前速度
    //玩家设定信息
    private SpaceObject targetObject; //正在自动导航的目标物件
    private SpaceObject selectedObject; //当前选中的目标
    //槽位上限设置
    private int mountingSlot; //挂载槽
    private int functionSlot; //功能槽
    private int moduleSlot; //模块槽
    private int additionalSlot; //附加槽
    private ShipSlot shipSlot;
    
    private String name;
    private String description;

    public ShipStatus(ShipModel shipModel) {
        this.maxSpeed = shipModel.getMaxSpeed();
        this.acceleration = shipModel.getAcceleration();
        this.brakingRatio = shipModel.getBrakingRatio();
        this.turnRate = shipModel.getTurnRate();

        this.maxShield = shipModel.getMaxShield();
        this.maxArmor = shipModel.getMaxArmor();
        this.maxStructure = shipModel.getMaxStructure();
        this.maxPower = shipModel.getMaxPower();

        this.shieldRechargeTime = shipModel.getShieldRechargeTime();
        this.powerRechargeTime = shipModel.getPowerRechargeTime();

        this.shieldResistance = shipModel.getShieldResistance();
        this.armorResistance = shipModel.getArmorResistance();
        this.structureResistance = shipModel.getStructureResistance();

        this.mountingSlot = shipModel.getMountingSlot();
        this.functionSlot = shipModel.getFunctionSlot();
        this.moduleSlot = shipModel.getModuleSlot();
        this.additionalSlot = shipModel.getAdditionalSlot();
        
        this.name = shipModel.getName();
        this.description = shipModel.getDescirption();

        shipSlot = new ShipSlot();
    }

    //getter/setter
    public int getThrottle() {
        return throttle;
    }

    public void setThrottle(int throttle) {
        this.throttle = throttle;
    }

    public float getCurrentAngle() {
        return currentAngle;
    }

    public void setCurrentAngle(float currentAngle) {
        this.currentAngle = currentAngle;
    }

    public float getCurrentX() {
        return currentX;
    }

    public void setCurrentX(float currentX) {
        this.currentX = currentX;
    }

    public float getCurrentY() {
        return currentY;
    }

    public void setCurrentY(float currentY) {
        this.currentY = currentY;
    }

    public float getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(float currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public SpaceObject getTargetObject() {
        return targetObject;
    }

    public void setTargetObject(SpaceObject targetObject) {
        this.targetObject = targetObject;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public float getAcceleration() {
        return acceleration;
    }

    public float getBrakingRatio() {
        return brakingRatio;
    }

    public float getTurnRate() {
        return turnRate;
    }

    public ShipSlot getShipSlot() {
        return shipSlot;
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

    public void setArmorResistance(ResistanceModel armorResistance) {
        this.armorResistance = armorResistance;
    }

    public ResistanceModel getStructureResistance() {
        return structureResistance;
    }

    public void setStructureResistance(ResistanceModel structureResistance) {
        this.structureResistance = structureResistance;
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
    
    public String getName(){
        return name;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public String getDescription(){
        return description;
    }
    
    public void setDescription(String description){
        this.description = description;
    }

    public SpaceObject getSelectedObject(){
        return this.selectedObject;
    }

    public void setSelectedObject(SpaceObject object){
        this.selectedObject = object;
    }
}
