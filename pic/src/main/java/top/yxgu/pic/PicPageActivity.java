package top.yxgu.pic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;

import java.util.List;

public class PicPageActivity extends Activity {

    private ViewPager viewPager;

    private int initPos = 0;
    private List<ItemInfo> dataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_page);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        dataList = (List<ItemInfo>)bundle.getSerializable("list");
        initPos = bundle.getInt("pos");

        viewPager = findViewById(R.id.PicPage);

        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;// 屏幕宽度（像素）
        int height= dm.heightPixels; // 屏幕高度（像素）

        PicPageAdapter adapter = new PicPageAdapter(this, dataList, width, height);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(initPos);
    }

}
