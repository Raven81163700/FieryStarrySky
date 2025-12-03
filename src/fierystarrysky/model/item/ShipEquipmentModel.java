/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fierystarrysky.model.item;

/**
 *
 * @author chenb
 */
public class ShipEquipmentModel extends BaseItemModel{
    public final static int MOUNTING_SLOT = 0;
    public final static int FUNCTION_SLOT = 1;
    public final static int MODULE_SLOT = 2;
    public final static int ADDITIONAL_SLOT = 3;
    
    private int slotType;
    
    public int getSlotType(){
        return slotType;
    }

    public void setSlotType(int slotType) {
        this.slotType = slotType;
    }
    
}
