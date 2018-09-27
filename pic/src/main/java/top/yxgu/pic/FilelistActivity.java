package top.yxgu.pic;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import top.yxgu.pic.net.SmbTools;

public class FilelistActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "FilelistActivity";

    private ArrayList<ItemInfo> dataList;
    private GridView gridView;
    private String rootPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filelist);


        Intent intent = getIntent();
//        rootPath = intent.getStringExtra("path");
        rootPath = intent.getStringExtra("top.yxgu.pic.root");
        setTitle(rootPath);
        if (rootPath.charAt(rootPath.length()-1) != '/') {
            rootPath += "/";
        }

        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;// 屏幕宽度（像素）
        float density = dm.density;//屏幕密度（0.75 / 1.0 / 1.5）
        int screenWidth = (int) (width/density);//屏幕宽度(dp)  == 698
        int col = screenWidth / 196;
        int wdp = (int)(screenWidth / col * density);


        gridView = findViewById(R.id.GridViewFile);
        gridView.setColumnWidth(wdp);

        fillContext();
    }

    private void fillContext() {
        Uri uri = Uri.parse(this.rootPath);
        if ("smb".equals(uri.getScheme())) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        dataList = SmbTools.getFileList(rootPath);
                        if (dataList != null && dataList.size() > 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setViewItem();
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(FilelistActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            }.start();
        } else if ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) {
            Request request = new Request.Builder().url(uri.toString()).get().build();
            OkHttpClient okHttpClient = Global.getOkHttpClient();
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "http onFailure: "+e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String body = response.body().string();
                    dataList = getFileListByHtml(body);

                    if (dataList != null && dataList.size() > 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setViewItem();
                            }
                        });
                    }
                }
            });
        }
    }

    private ArrayList<ItemInfo> getFileListByHtml(String html) {
        ArrayList<ItemInfo> list = new ArrayList<>();
        ItemInfo itemInfo;

        Pattern p = Pattern.compile("<a .*href=.+</a>", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(html);
        while(m.find()) {
            String href = m.group(); //找到超链接地址并截取字符串
            String hrefLow = href.toLowerCase(Locale.ROOT);
            //有无引号
            href = href.substring(hrefLow.indexOf("href="));
            if (href.charAt(5) == '\"') {
                href = href.substring(6);
            } else {
                href = href.substring(5);
            }
            //截取到引号或者空格或者到">"结束
            int idx = href.indexOf("\"");
            if (idx > 0) {
                href = href.substring(0, idx);
            } else {
                idx = href.indexOf(" ");
                if (idx > 0) {
                    href = href.substring(0, idx);
                } else {
                    href = href.substring(0, href.indexOf(">"));
                }
            }

            String url;
            String name;
            int type;
            Log.d(TAG, "fillContext: "+href);
            if (href.startsWith("http://") || href.startsWith("https://")) {
                url = href;
                name = href.substring(href.lastIndexOf("/")+1);
            } else {
                name = href;
                url = rootPath + name;
            }
            if (name.lastIndexOf(".") < 0) {
                type = ItemInfo.TYPE_FOLDER;
            } else {
                type = ItemInfo.getItemType(name);
                if (type == ItemInfo.TYPE_UNKNOWN) {
                    continue;
                }
            }
            itemInfo = new ItemInfo(url, name, type);
            list.add(itemInfo);
        }
        return list;
    }

    private void setViewItem() {
        PicListAdapter sad = new PicListAdapter(this, dataList, R.layout.activity_fileitem);
        gridView.setAdapter(sad);
        gridView.setOnItemClickListener(this);

        ListAdapter listAdapter = gridView.getAdapter();
        View view = listAdapter.getView(0, null, gridView);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
//        layoutParams.width = 50;
        view.setLayoutParams(layoutParams);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ItemInfo itemInfo = (ItemInfo) parent.getItemAtPosition(position);

        if (itemInfo.type == ItemInfo.TYPE_FOLDER) {
            Intent intent = new Intent("top.yxgu.pic.FilelistActivity");
            intent.putExtra("top.yxgu.pic.root", itemInfo.url);
            startActivity(intent);
        } else if (itemInfo.type == ItemInfo.TYPE_IMAGE) {
//            Intent intent = new Intent("top.yxgu.pic.PicActivity");
//            intent.putExtra("top.yxgu.pic.root", rootPath);
//            intent.putExtra("top.yxgu.pic.url", itemInfo.url);
//            startActivity(intent);
            Intent intent = new Intent("top.yxgu.pic.PicPageActivity");
            Bundle bundle = new Bundle();
            bundle.putSerializable("list", this.dataList);
            bundle.putInt("pos", position);
            intent.putExtras(bundle);
            startActivity(intent);
        } else if (itemInfo.type == ItemInfo.TYPE_MOVIE) {
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            String path = Environment.getExternalStorageDirectory().getPath()+ "/1.mp4";//该路径可以自定义
//            File file = new File(path);
//            Uri uri = Uri.fromFile(file);
//            intent.setDataAndType(uri, "video/*");
//            startActivity(intent);
        }
    }
}
