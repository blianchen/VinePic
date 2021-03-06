/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package top.yxgu.littlepic;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.disk.NoOpDiskTrimmableRegistry;
import com.facebook.common.util.ByteConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.ImagePipelineConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import top.yxgu.pic.Global;
import top.yxgu.pic.ImagePipeline.SmbAndHttpPipelineConfigFactory;
import top.yxgu.pic.ServerListFile;

/*
 * MainActivity class that loads {@link MainFragment}.
 */
public class MainActivity extends AppCompatActivity {
    private static final String IMAGE_PIPELINE_CACHE_DIR = "PipelineCache";
    //默认图极低磁盘空间缓存的最大值
    private static final int MAX_DISK_CACHE_VERYLOW_SIZE = 32 * ByteConstants.MB;
    //默认图低磁盘空间缓存的最大值
    private static final int MAX_DISK_CACHE_LOW_SIZE = 128 * ByteConstants.MB;
    //默认图磁盘缓存的最大值
    private static final int MAX_DISK_CACHE_SIZE = 512 * ByteConstants.MB;

    private GridView mContentView;

    private Button mAddButton;
    private List<Map<String, Object>> dataList;
    private SimpleAdapter simpleAdapter;

    private String albumPath = null;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ServerListFile.init(this);

        albumPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
        if (!albumPath.startsWith("file://")) {
            albumPath = "file://" + albumPath;
        }

        initFresco();

        dataList = getItemList();

        simpleAdapter = new SimpleAdapter(this, dataList, R.layout.activity_listitem,
                new String[]{"ItemImage", "ItemText"}, new int[]{R.id.ItemImage, R.id.ItemText});

        mContentView = findViewById(R.id.GridViewContent);
        mContentView.setAdapter(simpleAdapter);
        mContentView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> itemMap = dataList.get(position);
                String url = (String) itemMap.get("ItemText");

                if (ServerListFile.NAME_LOCAL_STORAGE.equals(url)) {
                    Intent intent = new Intent("top.yxgu.pic.FilelistActivity");
                    intent.putExtra("top.yxgu.pic.root", albumPath);
                    startActivity(intent);
                } else if (ServerListFile.NAME_LOCAL_NETWORK.equals(url)) {
                    Intent intent = new Intent("top.yxgu.pic.NetWorkActivity");
                    startActivity(intent);
                } else {
                    Intent intent = new Intent("top.yxgu.pic.FilelistActivity");
                    intent.putExtra("top.yxgu.pic.root", url);
                    startActivity(intent);
                }
            }
        });
        this.registerForContextMenu(mContentView);

        mAddButton = findViewById(R.id.AddButton);
        mAddButton.setOnTouchListener(new Button.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    //点击按钮未松开时按钮放大
                    blow_up(mAddButton);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    //点击按钮松开后按钮缩小
                    narrow(mAddButton);
                }
                return false;
            }
        });
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("top.yxgu.pic.AddServerActivity");
                startActivityForResult(intent, 1);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == 1) {
            dataList.clear();
            ArrayList<Map<String, Object>> list = getItemList();
            dataList.addAll(list);
            simpleAdapter.notifyDataSetChanged();
        }
    }

    private ArrayList<Map<String, Object>> getItemList() {
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map;

        List<String> srvList = ServerListFile.get();
        for (String str : srvList) {
            map = new HashMap<>();
            if (ServerListFile.NAME_LOCAL_STORAGE.equals(str)) {
                map.put("ItemImage", R.drawable.my_computer);
            } else if (ServerListFile.NAME_LOCAL_NETWORK.equals(str)) {
                map.put("ItemImage", R.drawable.smb_server);
            } else {
                map.put("ItemImage", R.drawable.http_server);
            }
            map.put("ItemText", str);
            list.add(map);
        }
        return list;
    }

    //放大按钮动画
    private void blow_up(View v) {
        float[] vaules = new float[]{1.0f, 1.1f, 1.2f, 1.3f, 1.4f, 1.5f};
        AnimatorSet set = new AnimatorSet();
        set.playTogether(ObjectAnimator.ofFloat(v, "scaleX", vaules), ObjectAnimator.ofFloat(v, "scaleY", vaules));
        set.setDuration(150);
        set.start();
    }
    //缩小按钮动画
    private void narrow(View v) {
        float[] vaules = new float[]{1.5f, 1.4f, 1.3f, 1.2f, 1.1f, 1.0f};
        AnimatorSet set = new AnimatorSet();
        set.playTogether(ObjectAnimator.ofFloat(v, "scaleX", vaules), ObjectAnimator.ofFloat(v, "scaleY", vaules));
        set.setDuration(150);
        set.start();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0, 1, Menu.NONE, " 删 除 ");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int pos = (int)mContentView.getAdapter().getItemId(menuInfo.position);
        switch (item.getItemId()) {
            case 1: {
                if (dataList.remove(pos) != null) {
                    ServerListFile.remove(pos);
                    simpleAdapter.notifyDataSetChanged();
                }
            }
            default:
                super.onContextItemSelected(item);
        }
        return true;
    }

    private void initFresco() {
        //小图片的磁盘配置,用来储存用户头像之类的小图
//        DiskCacheConfig diskSmallCacheConfig = DiskCacheConfig.newBuilder(this)
//                .setBaseDirectoryPath(this.getCacheDir())//缓存图片基路径
//                .setBaseDirectoryName(getString(R.string.app_name))//文件夹名
//                .setMaxCacheSize(32 * ByteConstants.MB)//默认缓存的最大大小。
//                .setMaxCacheSizeOnLowDiskSpace(16 * ByteConstants.MB)//缓存的最大大小,使用设备时低磁盘空间。
//                .setMaxCacheSizeOnVeryLowDiskSpace(4 * ByteConstants.MB)//缓存的最大大小,当设备极低磁盘空间
//                .build();

        DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder(this)
                .setBaseDirectoryPath(getApplicationContext().getCacheDir())//缓存图片基路径
                .setBaseDirectoryName(IMAGE_PIPELINE_CACHE_DIR)//文件夹名
                .setMaxCacheSize(MAX_DISK_CACHE_SIZE)//默认缓存的最大大小。
                .setMaxCacheSizeOnLowDiskSpace(MAX_DISK_CACHE_LOW_SIZE)//缓存的最大大小,使用设备时低磁盘空间。
                .setMaxCacheSizeOnVeryLowDiskSpace(MAX_DISK_CACHE_VERYLOW_SIZE)//缓存的最大大小,当设备极低磁盘空间
                .setDiskTrimmableRegistry(NoOpDiskTrimmableRegistry.getInstance())
                .build();

        ImagePipelineConfig imagePipelineConfig = SmbAndHttpPipelineConfigFactory.newBuilder(this, Global.getOkHttpClient())
                .setDownsampleEnabled(true)
                .setResizeAndRotateEnabledForNetwork(true) // 对网络图片进行resize处理，减少内存消耗
                .setMainDiskCacheConfig(diskCacheConfig)
//                .setSmallImageDiskCacheConfig(diskSmallCacheConfig)
//                .setBitmapsConfig(Bitmap.Config.RGB_565)
                .build();

        Fresco.initialize(this, imagePipelineConfig);
    }
}
