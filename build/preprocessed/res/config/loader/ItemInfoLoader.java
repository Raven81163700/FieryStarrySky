/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package res.config.loader;

import fierystarrysky.model.item.BaseItemModel;
import fierystarrysky.util.IniUtils;
import java.io.InputStream;

/**
 *
 * @author chenb
 */
public abstract class ItemInfoLoader {

    protected IniUtils iniUtils;
    protected BaseItemModel baseItemModel;

    public ItemInfoLoader(String id, BaseItemModel itemModel) {
        iniUtils = new IniUtils();
        baseItemModel = itemModel;
        baseItemModel.setItemId(id);
        InputStream inputStream = getClass().getResourceAsStream("/res/config/item/" + id + ".ini");
        try {
            iniUtils.load(inputStream);
        } catch (Exception e) {
        }
        load();
    }
    
    private void load(){
        String section = "base";
        String key = "name";
        baseItemModel.setName(iniUtils.get(section, key));
        key = "description";
        baseItemModel.setDescription(iniUtils.get(section, key));
        key = "itemType";
        baseItemModel.setItemType(Integer.parseInt(iniUtils.get(section, key)));
        key = "icon";
        baseItemModel.setIcon(iniUtils.get(section, key));
    }
    
    public BaseItemModel getInfo(){
        return baseItemModel;
    }
    
    protected abstract void loadExtend();
}
