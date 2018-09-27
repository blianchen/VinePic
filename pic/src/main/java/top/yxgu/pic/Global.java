package top.yxgu.pic;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class Global {
    private static OkHttpClient okHttpClient = null;
    public static OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient.Builder()
                    .addNetworkInterceptor( new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
//                        Log.d("ImageLoader", "request-url: " + chain.request().url().toString());
                            return chain.proceed(chain.request());
                        }
                    }).build();
        }
        return okHttpClient;
    }
}
