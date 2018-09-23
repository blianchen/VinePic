package top.yxgu.pic;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import jcifs.CIFSContext;
import jcifs.CIFSException;
import jcifs.CloseableIterator;
import jcifs.SmbResource;
import jcifs.config.PropertyConfiguration;
import jcifs.context.BaseContext;
import jcifs.smb.SmbFile;
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
        }
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
