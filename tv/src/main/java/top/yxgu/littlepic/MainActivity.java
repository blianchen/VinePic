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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.disk.NoOpDiskTrimmableRegistry;
import com.facebook.common.util.ByteConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.ImagePipelineConfig;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import top.yxgu.pic.ImagePipeline.SmbAndHttpPipelineConfigFactory;

/*
 * MainActivity class that loads {@link MainFragment}.
 */
public class MainActivity extends Activity {
    private static final String IMAGE_PIPELINE_CACHE_DIR = "PipelineCache";
    //默认图极低磁盘空间缓存的最大值
    private static final int MAX_DISK_CACHE_VERYLOW_SIZE = 32 * ByteConstants.MB;
    //默认图低磁盘空间缓存的最大值
    private static final int MAX_DISK_CACHE_LOW_SIZE = 128 * ByteConstants.MB;
    //默认图磁盘缓存的最大值
    private static final int MAX_DISK_CACHE_SIZE = 512 * ByteConstants.MB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initFresco();
    }

    private void initFresco() {
        DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder(this)
                .setBaseDirectoryPath(getApplicationContext().getCacheDir())//缓存图片基路径
                .setBaseDirectoryName(IMAGE_PIPELINE_CACHE_DIR)//文件夹名
                .setMaxCacheSize(MAX_DISK_CACHE_SIZE)//默认缓存的最大大小。
                .setMaxCacheSizeOnLowDiskSpace(MAX_DISK_CACHE_LOW_SIZE)//缓存的最大大小,使用设备时低磁盘空间。
                .setMaxCacheSizeOnVeryLowDiskSpace(MAX_DISK_CACHE_VERYLOW_SIZE)//缓存的最大大小,当设备极低磁盘空间
                .setDiskTrimmableRegistry(NoOpDiskTrimmableRegistry.getInstance())
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor( new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
//                        Log.d("ImageLoader", "request-url: " + chain.request().url().toString());
                        return chain.proceed(chain.request());
                    }
                }).build();

        ImagePipelineConfig imagePipelineConfig = SmbAndHttpPipelineConfigFactory.newBuilder(this, client)
                .setDownsampleEnabled(true)
                .setResizeAndRotateEnabledForNetwork(true) // 对网络图片进行resize处理，减少内存消耗
                .setMainDiskCacheConfig(diskCacheConfig)
//                .setSmallImageDiskCacheConfig(diskSmallCacheConfig)
//                .setBitmapsConfig(Bitmap.Config.RGB_565)
                .build();

        Fresco.initialize(this, imagePipelineConfig);
    }
}
