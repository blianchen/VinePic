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

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilelistActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "FilelistActivity";

    private String url;
    private List<Map<String,Object>> dataList;
    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filelist);

        Intent intent = getIntent();
        url = intent.getStringExtra("top.yxgu.pic.url");

        setTitle(url);

        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;// 屏幕宽度（像素）
        float density = dm.density;//屏幕密度（0.75 / 1.0 / 1.5）
        int screenWidth = (int) (width/density);//屏幕宽度(dp)  == 698
        int col = screenWidth / 196;
        int wdp = (int)(screenWidth / col * density);

        dataList = new ArrayList<>();

        Uri uri = Uri.parse("http://share.routerlogin.net/shares/U/");
        gridView = findViewById(R.id.GridViewFile);
        gridView.setColumnWidth(wdp);

//        Map<String, Object> map = new HashMap<>();
//        map.put("ItemImage", "http://share.routerlogin.net/shares/U/Documents/IMG_20171226_124736.jpg");
//        map.put("ItemText", "IMG_20171226_124736.jpg");
//        dataList.add(map);
//        map = new HashMap<>();
//        map.put("ItemImage", "http://share.routerlogin.net/shares/U/Documents/IMG_20180307_212454.jpg");
//        map.put("ItemText", "IMG_20180307_212454.jpg");
//        dataList.add(map);
//        map = new HashMap<>();
//        map.put("ItemImage", "http://share.routerlogin.net/shares/U/Documents/IMG_20180317_154847.jpg");
//        map.put("ItemText", "IMG_20180317_154847.jpg");
//        dataList.add(map);

        List<ItemInfo> list = new ArrayList<>();
        ItemInfo item = new ItemInfo("smb://share/storage/Documents/IMG_20171226_124736.jpg", "IMG_20171226_124736.jpg");
        list.add(item);
        item = new ItemInfo("smb://share/storage/Documents/IMG_20180307_212454.jpg", "IMG_20180307_212454.jpg");
        list.add(item);
        item = new ItemInfo("http://share.routerlogin.net/shares/U/Documents/IMG_20180317_154847.jpg", "IMG_20180317_154847.jpg");
        list.add(item);
        item = new ItemInfo("res:///"+R.drawable.icon_folder_pic, "wwwwww");
        list.add(item);

//        SimpleAdapter sad = new SimpleAdapter(this, dataList, R.layout.activity_fileitem,
////                new String[]{"ItemImage","ItemText"}, new int[]{R.id.ItemImage, R.id.ItemText});
        PicListAdapter sad = new PicListAdapter(this, list, R.layout.activity_fileitem);
        gridView.setAdapter(sad);
        gridView.setOnItemClickListener(this);

        ListAdapter listAdapter = gridView.getAdapter();
        View view = listAdapter.getView(0, null, gridView);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
//        layoutParams.width = 50;
        view.setLayoutParams(layoutParams);

//        ViewGroup.LayoutParams layoutParams = draweeView.getLayoutParams();
//        layoutParams.width = lineLayout.getWidth();
//        layoutParams.height = height;
//        draweeView.setImageURI(uri);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        HashMap<String,String> map=(HashMap<String,String>)parent.getItemAtPosition(position);
////        final String text=map.get("ItemImage");
        ItemInfo itemInfo = (ItemInfo) parent.getItemAtPosition(position);

        Intent intent = new Intent("top.yxgu.pic.PicActivity");
        intent.putExtra("top.yxgu.pic.url", itemInfo.url);
        startActivity(intent);
    }
}
