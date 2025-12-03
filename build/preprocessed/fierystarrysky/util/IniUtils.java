/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fierystarrysky.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Hashtable;

/**
 *
 * @author Raven
 */
public class IniUtils {

    private Hashtable sections = new Hashtable();
    private String currentSection = ""; // ← 用成员变量保存当前 section

    public String get(String section, String key) {
        Hashtable kv = (Hashtable) sections.get(section);
        if (kv == null) {
            return null;
        }
        return (String) kv.get(key);
    }

    public void set(String section, String key, String value) {
        Hashtable kv = (Hashtable) sections.get(section);
        if (kv == null) {
            kv = new Hashtable();
            sections.put(section, kv);
        }
        kv.put(key, value);
    }

    public void load(InputStream is) throws IOException {
        InputStreamReader reader = new InputStreamReader(is, "UTF-8");
        StringBuffer sb = new StringBuffer();
        int ch;

        while ((ch = reader.read()) != -1) {
            if (ch == '\r') {
                continue;
            }
            if (ch == '\n') {
                parseLine(sb.toString());
                sb.setLength(0);
            } else {
                sb.append((char) ch);
            }
        }
        if (sb.length() > 0) {
            parseLine(sb.toString());
        }
        reader.close();
    }

    private void parseLine(String line) {
        line = line.trim();
        if (line.length() == 0 || line.startsWith(";") || line.startsWith("#")) {
            return;
        }
        if (line.startsWith("[") && line.endsWith("]")) {
            currentSection = line.substring(1, line.length() - 1).trim();
            if (!sections.containsKey(currentSection)) {
                sections.put(currentSection, new Hashtable());
            }
        } else {
            int idx = line.indexOf('=');
            if (idx > 0) {
                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                set(currentSection, key, value);
            }
        }
    }

    public void save(OutputStream os) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
        for (java.util.Enumeration e = sections.keys(); e.hasMoreElements();) {
            String section = (String) e.nextElement();
            writer.write("[" + section + "]\n");
            Hashtable kv = (Hashtable) sections.get(section);
            for (java.util.Enumeration ek = kv.keys(); ek.hasMoreElements();) {
                String key = (String) ek.nextElement();
                String value = (String) kv.get(key);
                writer.write(key + "=" + value + "\n");
            }
            writer.write("\n");
        }
        writer.flush();
        writer.close();
    }
}
