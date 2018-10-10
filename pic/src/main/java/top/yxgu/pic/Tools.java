package top.yxgu.pic;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

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

public class Tools {
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


    public static String getRealPath(Context context, Uri uri) {
        int sdkVer = Build.VERSION.SDK_INT;
        if (sdkVer >= 19) {
            return getRealPath19(context, uri);
        } else {
            return getDataColumn(context, uri, null, null);
        }
    }

    public static String getRealPath19(Context context, Uri uri) {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ( "com.android.providers.media.documents".equals(uri.getAuthority()) ) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = {selection};
                return getDataColumn(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
            } else if ( "com.android.providers.downloads.documents".equals(uri.getAuthority()) ) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                return getDataColumn(context, contentUri, null, null);
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.toString();
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        String path = null;

        String[] projection = new String[]{MediaStore.Images.Media.DATA};
        try (Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
                path = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            Log.e("Tools", "getDataColumn: ", e);
        }
        return path;
    }
}
