/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fierystarrysky.game.status.ship;

import java.util.Vector;

/**
 *
 * @author chenb
 */
public class ShipSlot {
    private Vector moutingSlots;
    private Vector functionSlots;
    private Vector moduleSlots;
    private Vector additionalSlots;
    
    public ShipSlot(){
        moutingSlots = new Vector();
        functionSlots = new Vector();
        moduleSlots = new Vector();
        additionalSlots = new Vector();
    }
    
    public Vector getMoutingSlots(){
        return moutingSlots;
    }
    
    public Vector getFunctionSlots(){
        return functionSlots;
    }
    
    public Vector getModuleSlots(){
        return moduleSlots;
    }
    
    public Vector getAdditionalSlots(){
        return additionalSlots;
    }
    
    public void addMountingSlot(MountingSlotEquipmentStatus slot){
        this.moutingSlots.addElement(slot);
    }
}
