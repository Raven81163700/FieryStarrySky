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
    private int mountingCapacity = 0;
    
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
        // try to put into first empty slot if capacity initialized
        if (mountingCapacity > 0) {
            for (int i = 0; i < moutingSlots.size() && i < mountingCapacity; i++) {
                Object o = moutingSlots.elementAt(i);
                if (o == null) {
                    moutingSlots.setElementAt(slot, i);
                    return;
                }
            }
            // no empty slot found, if vector shorter than capacity, add at end
            if (moutingSlots.size() < mountingCapacity) {
                moutingSlots.addElement(slot);
                return;
            }
            // otherwise ignore (no capacity)
        } else {
            moutingSlots.addElement(slot);
        }
    }

    public void initMountingSlots(int capacity) {
        this.mountingCapacity = capacity;
        moutingSlots.removeAllElements();
        for (int i = 0; i < capacity; i++) {
            moutingSlots.addElement(null);
        }
    }

    public int getMountingSlotCount() {
        return mountingCapacity > 0 ? mountingCapacity : moutingSlots.size();
    }

    public MountingSlotEquipmentStatus getMountingSlotAt(int index) {
        if (index < 0 || index >= moutingSlots.size()) return null;
        return (MountingSlotEquipmentStatus) moutingSlots.elementAt(index);
    }

    public boolean installMountingSlotAt(int index, MountingSlotEquipmentStatus slot) {
        if (index < 0) return false;
        if (mountingCapacity > 0 && index >= mountingCapacity) return false;
        // expand if needed
        while (moutingSlots.size() <= index) {
            moutingSlots.addElement(null);
        }
        moutingSlots.setElementAt(slot, index);
        return true;
    }

    public boolean removeMountingSlotAt(int index) {
        if (index < 0 || index >= moutingSlots.size()) return false;
        moutingSlots.setElementAt(null, index);
        return true;
    }
}
