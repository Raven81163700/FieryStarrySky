/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fierystarrysky.util;

import java.io.UnsupportedEncodingException;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

/**
 *
 * @author Raven
 */
public class RMSUtils {

    /**
     * 从 RMS 中读取配置文件内容
     *
     * @param storeName RMS 存储名
     * @return 字符串内容，如果不存在返回 null
     */
    public static String loadRMS(String storeName) {
        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(storeName, false); // false = 不创建
            if (rs.getNumRecords() == 0) {
                return null;
            }

            byte[] data = rs.getRecord(1); // RMS 记录ID从1开始
            return new String(data, "UTF-8");
        } catch (RecordStoreException e) {
            return null;
        } catch (UnsupportedEncodingException e) {
            return null;
        } finally {
            if (rs != null) {
                try {
                    rs.closeRecordStore();
                } catch (RecordStoreException e) {
                }
            }
        }
    }

    /**
     * 保存配置到 RMS
     *
     * @param storeName RMS 存储名
     * @param content 需要保存的字符串
     */
    public static void saveRMS(String storeName, String content) {
        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(storeName, true); // true = 如果不存在则创建
            byte[] data = content.getBytes("UTF-8");

            if (rs.getNumRecords() == 0) {
                rs.addRecord(data, 0, data.length);
            } else {
                rs.setRecord(1, data, 0, data.length);
            }
        } catch (RecordStoreException e) {
        } catch (UnsupportedEncodingException e) {
        } finally {
            if (rs != null) {
                try {
                    rs.closeRecordStore();
                } catch (RecordStoreException e) {
                }
            }
        }
    }

    /**
     * 删除 RMS
     */
    public static void deleteRMS(String storeName) {
        try {
            RecordStore.deleteRecordStore(storeName);
        } catch (RecordStoreException e) {
        }
    }
}
