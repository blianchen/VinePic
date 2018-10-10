package top.yxgu.pic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.List;

public class PicPageActivity extends Activity {

    private ViewPager viewPager;

    private int currPos = 0;
    private List<ItemInfo> dataList;

    private Handler handler = new AutoHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_page);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        dataList = (List<ItemInfo>)bundle.getSerializable("list");
        currPos = bundle.getInt("pos");

        viewPager = findViewById(R.id.PicPage);

        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;// 屏幕宽度（像素）
        int height= dm.heightPixels; // 屏幕高度（像素）

        PicPageAdapter adapter = new PicPageAdapter(this, dataList, width, height);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currPos);
        viewPager.setPageTransformer(true, new DepthPageTransformer());
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                handler.removeMessages(AutoHandler.MSG_AUTO_PLAY);
                handler.sendEmptyMessage(AutoHandler.MSG_START_AUTO_PLAY);
                return false;
            }
        });

        handler.sendEmptyMessage(AutoHandler.MSG_START_AUTO_PLAY);
    }

    class AutoHandler extends Handler {
        public static final int MSG_AUTO_PLAY = 1;
        public static final int MSG_START_AUTO_PLAY = 2;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d("AutoHandler", "receive message " + msg.what);
            switch (msg.what) {
                case MSG_AUTO_PLAY:
                    viewPager.setCurrentItem(++currPos);
                    handler.sendEmptyMessageDelayed(MSG_AUTO_PLAY, 3000);
                    break;
                case MSG_START_AUTO_PLAY:
                    handler.sendEmptyMessageDelayed(MSG_AUTO_PLAY, 60000 * 5);
                    break;
            }
        }
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0, 1, Menu.NONE, " 自动播放 ");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        handler.sendEmptyMessage(AutoHandler.MSG_AUTO_PLAY);
        super.onContextItemSelected(item);
        return true;
    }
}
