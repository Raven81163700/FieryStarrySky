/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package res.image.loader;

import java.util.Hashtable;
import javax.microedition.lcdui.Image;

/**
 *
 * @author chenb
 */
public class ItemCollectionImageLoader {

    private Hashtable resources;
    private Hashtable images;

    public ItemCollectionImageLoader() {
        this.resources = new Hashtable();
        this.images = new Hashtable();
    }

    private void loadImage(String itemId) {
        try {
            Image image = Image.createImage((String) resources.get(itemId));
            images.put(itemId, image);
        } catch (Exception e) {
        }
    }

    public void prepareResources(String itemId, String uri) {
        if (resources.containsKey(itemId)) {
            return;
        }
        this.resources.put(itemId, uri);
    }

    public Image getItemImage(String itemId) {
        if (images.containsKey(itemId)) {
            return (Image) images.get(itemId);
        } else {
            if (resources.containsKey(itemId)) {
                this.loadImage(itemId);
                return (Image) images.get(itemId);
            } else {
                return null;
            }
        }
    }
}
