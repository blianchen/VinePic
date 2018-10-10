package top.yxgu.pic;


import java.io.Serializable;

public class ItemInfo implements Serializable {
    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_FOLDER = 1;
    public static final int TYPE_IMAGE = 2;
    public static final int TYPE_MOVIE = 3;

    public String url;
    public String name;
    public int type;

    public ItemInfo() {

    }

    public ItemInfo(String url, String name, int type) {
        this.url = url;
        this.name = name;
        this.type = type;
    }

    public static int getItemType(String url) {
        if (url == null || url.isEmpty()) return TYPE_UNKNOWN;
        int idx = url.lastIndexOf(".");
        if (idx < 0) {
            return TYPE_UNKNOWN;
        }
        String ext = url.substring(idx+1);
        if ("jpg".equalsIgnoreCase(ext)
                || "jpeg".equalsIgnoreCase(ext)
                || "png".equalsIgnoreCase(ext)) {
            return TYPE_IMAGE;
        } else if ("mp4".equalsIgnoreCase(ext)
                || "mkv".equalsIgnoreCase(ext)
                || "rmvb".equalsIgnoreCase(ext)) {
            return TYPE_MOVIE;
        } else {
            return TYPE_UNKNOWN;
        }
    }
}
