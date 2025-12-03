package fierystarrysky.model.item;

import fierystarrysky.model.data.DamageModel;
import res.config.loader.ItemInfoLoader;

public class AmmoItemModel extends BaseItemModel {
    public static final int AMMO_TYPE_MISSILE = 0;
    public static final int AMMO_TYPE_SHELL = 1;

    public static final int AMMO_SIZE_20MM_SHELL = 0;
    public static final int AMMO_SIZE_TINY_ROCKET = 1;

    private int typeId, sizeId;
    private DamageModel damageModel;

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int getSizeId() {
        return sizeId;
    }

    public void setSizeId(int sizeId) {
        this.sizeId = sizeId;
    }

    public DamageModel getDamageModel() {
        return damageModel;
    }

    public void setDamageModel(DamageModel damageModel) {
        this.damageModel = damageModel;
    }

    public class AmmoItemLoader extends ItemInfoLoader{
        public AmmoItemLoader(String id){
            super(id, new AmmoItemModel());
            loadExtend();
        }

        protected void loadExtend() {
            String section = "";
            String key = "";
        }
    }
}
