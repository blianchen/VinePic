package top.yxgu.pic;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ServerListFile {

    private static String fileName = "srv.conf";
    private static String charset = "UTF-8";

    public static String NAME_LOCAL_STORAGE = "本地";
    public static String NAME_LOCAL_NETWORK = "局域网";

    private static Context context;
    private static List<String> list = new ArrayList<>();

    public static void init(Context cxt) {
        context = cxt;
        try (FileInputStream fi = context.openFileInput(fileName)) {
            int byteNum = fi.available();
            byte[] bytes = new byte[byteNum];
            int num = fi.read(bytes);
            if (num > 0) {
                String str = new String(bytes, charset);
                String[] arr = str.split("\n");
                for (String v : arr) {
                    list.add(v);
                }
            }
        } catch (FileNotFoundException e) {
            list.add(NAME_LOCAL_STORAGE);
            list.add(NAME_LOCAL_NETWORK);
            list.add("http://share.routerlogin.net/shares/U/Documents/");
            saveToFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void add(String s) {
        list.add(s);
        saveToFile();
    }

    public static List<String> get() {
        return list;
    }

    public static void remove(int pos) {
        list.remove(pos);
        saveToFile();
    }

    private static void saveToFile() {
        try (FileOutputStream fo = context.openFileOutput(fileName, Context.MODE_PRIVATE)) {
            for (String v: list) {
                fo.write(v.getBytes(charset));
                fo.write('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
