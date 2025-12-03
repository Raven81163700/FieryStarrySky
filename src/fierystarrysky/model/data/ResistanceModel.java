/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fierystarrysky.model.data;

/**
 *
 * @author chenb
 */
public class ResistanceModel {

    private float thermalResistance; //热能
    private float explosionResistance; //爆炸
    private float electricResistance; //电能
    private float kineticResistance; //动能

    public float getThermalResistance() {
        return thermalResistance;
    }

    public void setThermalResistance(float thermalResistance) {
        this.thermalResistance = thermalResistance;
    }

    public float getExplosionResistance() {
        return explosionResistance;
    }

    public void setExplosionResistance(float explosionResistance) {
        this.explosionResistance = explosionResistance;
    }

    public float getElectricResistance() {
        return electricResistance;
    }

    public void setElectricResistance(float electricResistance) {
        this.electricResistance = electricResistance;
    }

    public float getKineticResistance() {
        return kineticResistance;
    }

    public void setKineticResistance(float kineticResistance) {
        this.kineticResistance = kineticResistance;
    }
}
