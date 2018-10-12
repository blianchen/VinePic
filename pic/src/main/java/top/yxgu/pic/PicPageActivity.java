package top.yxgu.pic;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.List;

import cn.jzvd.JzvdStd;

public class PicPageActivity extends Activity {

    private ViewPager viewPager;

    private int currPos = 0;
    private List<ItemInfo> dataList;

    private Handler handler = new AutoHandler();
    private boolean isAutoPlay = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_page);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        boolean isTv = false;
        UiModeManager uiModeManager = (UiModeManager)getSystemService(Context.UI_MODE_SERVICE);
        if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            isTv = true;
        }
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

//        viewPager.setPageTransformer(true, new DepthPageTransformer());
        if (isTv) {
            viewPager.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    handler.removeMessages(AutoHandler.MSG_START_AUTO_PLAY);
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                            if ((event.getFlags() & KeyEvent.FLAG_LONG_PRESS) != 0) {
                                Log.i("OnKeyListener", "long press: ");
                                handler.sendEmptyMessage(AutoHandler.MSG_AUTO_PLAY);
                            }
                            break;
                        default:
                            handler.removeMessages(AutoHandler.MSG_AUTO_PLAY);
                            isAutoPlay = false;
                            handler.sendEmptyMessage(AutoHandler.MSG_START_AUTO_PLAY);
                            break;
                    }
                    return false;
                }
            });
        } else {
            viewPager.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    handler.removeMessages(AutoHandler.MSG_AUTO_PLAY);
                    handler.removeMessages(AutoHandler.MSG_START_AUTO_PLAY);
                    isAutoPlay = false;
                    handler.sendEmptyMessage(AutoHandler.MSG_START_AUTO_PLAY);
                    return false;
                }
            });
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
//                Log.i("OnPageChangeListener", "onPageSelected: "+position);
                currPos = position;
                JzvdStd.releaseAllVideos();
                View view = viewPager.findViewWithTag(position);
                Log.i("OnPageChangeListener", "onPageSelected: "+ position + ", view:"+view);
                if (view!=null) {
                    if (view instanceof JzvdStd) {
                        ((JzvdStd) view).startVideo();
                    } else if (view instanceof VideoView) {
                        ((VideoView)view).start();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.i("OnPageChangeListener", "onPageScrollStateChanged: "+state);
            }
        });

        viewPager.setCurrentItem(currPos);

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
                    isAutoPlay = true;
                    viewPager.setCurrentItem(++currPos);
                    handler.sendEmptyMessageDelayed(MSG_AUTO_PLAY, 5000);
                    Toast.makeText(PicPageActivity.this, "开始幻灯片播放", Toast.LENGTH_SHORT);
                    break;
                case MSG_START_AUTO_PLAY:
                    handler.sendEmptyMessageDelayed(MSG_AUTO_PLAY, 60000 * 5);
                    break;
            }
        }
    }

    public Handler getHandler() {
        return handler;
    }

    @Override
    public void onBackPressed() {
        if (JzvdStd.backPress()) {
            return;
        }
        super.onBackPressed();
        handler.removeMessages(AutoHandler.MSG_AUTO_PLAY);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        JzvdStd.releaseAllVideos();
        handler.removeMessages(AutoHandler.MSG_AUTO_PLAY);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        JzvdStd.releaseAllVideos();
        handler.removeMessages(AutoHandler.MSG_AUTO_PLAY);
    }
}
