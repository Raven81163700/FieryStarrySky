/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fierystarrysky.model.item;

/**
 *
 * @author chenb
 */
public class BaseItemModel {
    public static final int SHIP_EQUIPMENT_ITEM = 0;
    public static final int AMMO_ITEM = 1;
    
    private String itemId;
    private String name;
    private String description;
    private int itemType;
    private String icon;

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getItemType() {
        return itemType;
    }

    public String getItemId() {
        return itemId;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
    
}
