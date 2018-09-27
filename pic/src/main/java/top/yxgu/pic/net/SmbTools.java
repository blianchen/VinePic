package top.yxgu.pic.net;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Properties;

import jcifs.CIFSContext;
import jcifs.CIFSException;
import jcifs.CloseableIterator;
import jcifs.SmbResource;
import jcifs.config.PropertyConfiguration;
import jcifs.context.BaseContext;
import jcifs.smb.SmbFile;
import top.yxgu.pic.ItemInfo;

public class SmbTools {
    public static ArrayList<ItemInfo> getFileList(String path) throws CIFSException, MalformedURLException {
        ArrayList<ItemInfo> list = new ArrayList<>();
        ItemInfo itemInfo;

        CIFSContext ctx = withAnonymousCredentials();

        try (SmbFile smbFile = new SmbFile(path, ctx);
             CloseableIterator<SmbResource> it = smbFile.children()) {
            while (it.hasNext()) {
                try (SmbResource item = it.next()) {
                    if ("IPC$/".equalsIgnoreCase(item.getName())) continue;
                    int type;
                    if (item.isDirectory()) {
                        type = ItemInfo.TYPE_FOLDER;
                    } else {
                        type = ItemInfo.getItemType(item.getName());
                        if (type == ItemInfo.TYPE_UNKNOWN) continue;
                    }
                    itemInfo = new ItemInfo(path + item.getName(), item.getName(), type);
                    list.add(itemInfo);
                }
            }
        }
        return list;
    }

    private static CIFSContext withAnonymousCredentials () throws CIFSException {
        Properties cfg = new Properties();
//        cfg.put("jcifs.smb.client.maxVersion", "SMB1");
//        cfg.put("jcifs.smb.client.useUnicode", "false");
//        cfg.put("jcifs.smb.client.forceUnicode", "false");
//        cfg.put("jcifs.smb.client.useNtStatus", "false");
//        cfg.put("jcifs.smb.client.useNTSmbs", "false");
        BaseContext baseContext = new BaseContext(new PropertyConfiguration(cfg));
        return baseContext.withAnonymousCredentials();
    }
}
