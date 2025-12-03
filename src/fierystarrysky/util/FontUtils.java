/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fierystarrysky.util;

import javax.microedition.lcdui.Font;

/**
 *
 * @author Raven
 */
public class FontUtils {
    static private Font fontLarge = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_LARGE);
    static private Font fontMedium = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
    static private Font fontSmall = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    
    static public Font getLarge(){
        return fontLarge;
    }
    
    static public Font getMedium(){
        return fontMedium;
    }
    
    static public Font getSmall(){
        return fontSmall;
    }
    
    static public int getLargeHeight(){
        return fontLarge.getHeight();
    }
    
    static public int getMediumHeight(){
        return fontMedium.getHeight();
    }
    
    static public int getSmallHeight(){
        return fontSmall.getHeight();
    }
    static public int getLargeWidth(){
        return fontLarge.charWidth("啊".charAt(0));
    }
    static public int getMediumWidth(){
        return fontMedium.charWidth("啊".charAt(0));
    }
    static public int getSmallWidth(){
        return fontSmall.charWidth("啊".charAt(0));
    }
}
