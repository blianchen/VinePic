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

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.ImagePipelineConfig;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/*
 * MainActivity class that loads {@link MainFragment}.
 */
public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initFresco();
    }

    private void initFresco() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor( new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
//                        Log.d("ImageLoader", "request-url: " + chain.request().url().toString());
                        return chain.proceed(chain.request());
                    }
                }).build();

        ImagePipelineConfig imagePipelineConfig = OkHttpImagePipelineConfigFactory.newBuilder(this, client)
                .setDownsampleEnabled(true)
                .setResizeAndRotateEnabledForNetwork(true) // 对网络图片进行resize处理，减少内存消耗
//                .setSmallImageDiskCacheConfig(diskSmallCacheConfig)
//                .setBitmapsConfig(Bitmap.Config.RGB_565)
                .build();

        Fresco.initialize(this, imagePipelineConfig);
    }
}
