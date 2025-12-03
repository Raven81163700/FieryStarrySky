package fierystarrysky.model.data;

public class DamageModel {
    private float thermalDamage; // 热能
    private float explosionDamage; // 爆炸
    private float electricDamage; // 电能
    private float kineticDamage; // 动能

    public float getThermalDamage() {
        return thermalDamage;
    }

    public void setThermalDamage(float thermalDamage) {
        this.thermalDamage = thermalDamage;
    }

    public float getExplosionDamage() {
        return explosionDamage;
    }

    public void setExplosionDamage(float explosionDamage) {
        this.explosionDamage = explosionDamage;
    }

    public float getElectricDamage() {
        return electricDamage;
    }

    public void setElectricDamage(float electricDamage) {
        this.electricDamage = electricDamage;
    }

    public float getKineticDamage() {
        return kineticDamage;
    }

    public void setKineticDamage(float kineticDamage) {
        this.kineticDamage = kineticDamage;
    }
}
