/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package res.config.loader;

import fierystarrysky.model.data.ResistanceModel;
import fierystarrysky.model.ship.ShipModel;
import fierystarrysky.util.IniUtils;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author chenb
 */
public class ShipInfoLoader {

    private IniUtils iniUtils;
    private ShipModel shipModel;

    public ShipInfoLoader(String id) {
        shipModel = new ShipModel();
        shipModel.setId(id);
        iniUtils = new IniUtils();
        InputStream inputStream = getClass().getResourceAsStream("/res/config/ship/" + id + ".ini");
        try {
            iniUtils.load(inputStream);
        } catch (Exception e) {
        }
        load();
    }

    private void load() {
        String section = "base";
        String key = "name";
        String pointer = iniUtils.get(section, key);
        shipModel.setName(pointer);
        key = "description";
        pointer = iniUtils.get(section, key);
        shipModel.setDescription(pointer);

        section = "movement";
        key = "maxSpeed";
        pointer = iniUtils.get(section, key);
        shipModel.setMaxSpeed(Float.parseFloat(pointer));
        key = "acceleration";
        pointer = iniUtils.get(section, key);
        shipModel.setAcceleration(Float.parseFloat(pointer));
        key = "brakingRatio";
        pointer = iniUtils.get(section, key);
        shipModel.setBrakingRatio(Float.parseFloat(pointer));
        key = "turnRate";
        pointer = iniUtils.get(section, key);
        shipModel.setTurnRate(Float.parseFloat(pointer));

        section = "attribute";
        key = "maxShield";
        pointer = iniUtils.get(section, key);
        shipModel.setMaxShield(Float.parseFloat(pointer));
        key = "maxArmor";
        pointer = iniUtils.get(section, key);
        shipModel.setMaxArmor(Float.parseFloat(pointer));
        key = "maxStructure";
        pointer = iniUtils.get(section, key);
        shipModel.setMaxStructure(Float.parseFloat(pointer));
        key = "maxPower";
        pointer = iniUtils.get(section, key);
        shipModel.setMaxPower(Float.parseFloat(pointer));
        key = "shieldRechargeTime";
        pointer = iniUtils.get(section, key);
        shipModel.setShieldRechargeTime(Float.parseFloat(pointer));
        key = "powerRechargeTime";
        pointer = iniUtils.get(section, key);
        shipModel.setPowerRechargeTime(Float.parseFloat(pointer));

        ResistanceModel shieldResistance = new ResistanceModel();
        key = "shield.thermalResistance";
        pointer = iniUtils.get(section, key);
        shieldResistance.setThermalResistance(Float.parseFloat(pointer));
        key = "shield.explosionResistance";
        pointer = iniUtils.get(section, key);
        shieldResistance.setExplosionResistance(Float.parseFloat(pointer));
        key = "shield.electricResistance";
        pointer = iniUtils.get(section, key);
        shieldResistance.setElectricResistance(Float.parseFloat(pointer));
        key = "shield.kineticResistance";
        pointer = iniUtils.get(section, key);
        shieldResistance.setKineticResistance(Float.parseFloat(pointer));
        shipModel.setShieldResistance(shieldResistance);

        ResistanceModel armorResistance = new ResistanceModel();
        key = "armor.thermalResistance";
        pointer = iniUtils.get(section, key);
        armorResistance.setThermalResistance(Float.parseFloat(pointer));
        key = "armor.explosionResistance";
        pointer = iniUtils.get(section, key);
        armorResistance.setExplosionResistance(Float.parseFloat(pointer));
        key = "armor.electricResistance";
        pointer = iniUtils.get(section, key);
        armorResistance.setElectricResistance(Float.parseFloat(pointer));
        key = "armor.kineticResistance";
        pointer = iniUtils.get(section, key);
        armorResistance.setKineticResistance(Float.parseFloat(pointer));
        shipModel.setArmorResistance(armorResistance);

        ResistanceModel structureResistance = new ResistanceModel();
        key = "structure.thermalResistance";
        pointer = iniUtils.get(section, key);
        structureResistance.setThermalResistance(Float.parseFloat(pointer));
        key = "structure.explosionResistance";
        pointer = iniUtils.get(section, key);
        structureResistance.setExplosionResistance(Float.parseFloat(pointer));
        key = "structure.electricResistance";
        pointer = iniUtils.get(section, key);
        structureResistance.setElectricResistance(Float.parseFloat(pointer));
        key = "structure.kineticResistance";
        pointer = iniUtils.get(section, key);
        structureResistance.setKineticResistance(Float.parseFloat(pointer));
        shipModel.setStructureResistance(structureResistance);

        section = "slot";
        key = "mountingSlot";
        pointer = iniUtils.get(section, key);
        shipModel.setMountingSlot(Integer.parseInt(pointer));
        key = "functionSlot";
        pointer = iniUtils.get(section, key);
        shipModel.setFunctionSlot(Integer.parseInt(pointer));
        key = "moduleSlot";
        pointer = iniUtils.get(section, key);
        shipModel.setModuleSlot(Integer.parseInt(pointer));
        key = "additionalSlot";
        pointer = iniUtils.get(section, key);
        shipModel.setAdditionalSlot(Integer.parseInt(pointer));
    }

    public ShipModel getInfo() {
        return shipModel;
    }
}
