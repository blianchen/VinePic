/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package top.yxgu.pic.ImagePipeline;

import android.net.Uri;
import android.os.Looper;
import android.os.SystemClock;

import com.facebook.imagepipeline.common.BytesRange;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.producers.BaseNetworkFetcher;
import com.facebook.imagepipeline.producers.BaseProducerContextCallbacks;
import com.facebook.imagepipeline.producers.Consumer;
import com.facebook.imagepipeline.producers.FetchState;
import com.facebook.imagepipeline.producers.NetworkFetcher;
import com.facebook.imagepipeline.producers.ProducerContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Network fetcher that uses OkHttp 3 as a backend.
 */
public class SmbAndHttpFetcher extends BaseNetworkFetcher<SmbAndHttpFetcher.SmbAndHttpFetchState> {

    public static class SmbAndHttpFetchState extends FetchState {

        public long submitTime;
        public long responseTime;
        public long fetchCompleteTime;

        public SmbAndHttpFetchState(Consumer<EncodedImage> consumer, ProducerContext producerContext) {
            super(consumer, producerContext);
        }
    }

    private static final String TAG = "SmbFetchProducer";
    private static final String QUEUE_TIME = "queue_time";
    private static final String FETCH_TIME = "fetch_time";
    private static final String TOTAL_TIME = "total_time";
    private static final String IMAGE_SIZE = "image_size";

    private final ExecutorService mExecutorService;
    private final Call.Factory mCallFactory;

    private Executor mCancellationExecutor;

    private static final int NUM_NETWORK_THREADS = 2;


    public SmbAndHttpFetcher(OkHttpClient okHttpClient) {
        this(okHttpClient, okHttpClient.dispatcher().executorService());
    }

    /**
     */
    public SmbAndHttpFetcher(Call.Factory callFactory, Executor cancellationExecutor) {
        mExecutorService = Executors.newFixedThreadPool(NUM_NETWORK_THREADS);
        mCallFactory = callFactory;
        mCancellationExecutor = cancellationExecutor;
    }

    @Override
    public SmbAndHttpFetchState createFetchState(Consumer<EncodedImage> consumer, ProducerContext context) {
        return new SmbAndHttpFetchState(consumer, context);
    }

    @Override
    public void fetch(final SmbAndHttpFetchState fetchState, final Callback callback) {
        fetchState.submitTime = SystemClock.elapsedRealtime();
        final Uri uri = fetchState.getUri();
        String scheme = uri.getScheme();

        if ("smb".equals(scheme)) {
            final Future<?> future = mExecutorService.submit(
                    new Runnable() {
                        @Override
                        public void run() {
                            fetchSync(fetchState, callback);
                        }
                    });
            fetchState.getContext().addCallbacks(
                    new BaseProducerContextCallbacks() {
                        @Override
                        public void onCancellationRequested() {
                            if (future.cancel(false)) {
                                callback.onCancellation();
                            }
                        }
                    });
        } else {
            try {
                final Request.Builder requestBuilder = new Request.Builder()
                        .cacheControl(new CacheControl.Builder().noStore().build())
                        .url(uri.toString())
                        .get();

                final BytesRange bytesRange = fetchState.getContext().getImageRequest().getBytesRange();
                if (bytesRange != null) {
                    requestBuilder.addHeader("Range", bytesRange.toHttpRangeHeaderValue());
                }

                fetchWithRequest(fetchState, callback, requestBuilder.build());
            } catch (Exception e) {
                // handle error while creating the request
                callback.onFailure(e);
            }
        }
    }

    void fetchSync(FetchState fetchState, Callback callback) {
        String url = fetchState.getUri().toString();
        try (SmbFile smbFile = new SmbFile(url);
            InputStream is = smbFile.getInputStream()) {
            callback.onResponse(is, -1);
        } catch (SmbException e) {
            callback.onFailure(e);
        } catch (IOException e) {
            callback.onFailure(e);
        }
    }

    protected void fetchWithRequest(
            final SmbAndHttpFetcher.SmbAndHttpFetchState fetchState,
            final NetworkFetcher.Callback callback,
            final Request request) {
        final Call call = mCallFactory.newCall(request);

        fetchState.getContext()
                .addCallbacks(
                        new BaseProducerContextCallbacks() {
                            @Override
                            public void onCancellationRequested() {
                                if (Looper.myLooper() != Looper.getMainLooper()) {
                                    call.cancel();
                                } else {
                                    mCancellationExecutor.execute(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    call.cancel();
                                                }
                                            });
                                }
                            }
                        });

        call.enqueue(
                new okhttp3.Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        fetchState.responseTime = SystemClock.elapsedRealtime();
                        final ResponseBody body = response.body();
                        try {
                            if (!response.isSuccessful()) {
                                handleException(
                                        call, new IOException("Unexpected HTTP code " + response), callback);
                                return;
                            }

                            BytesRange responseRange =
                                    BytesRange.fromContentRangeHeader(response.header("Content-Range"));
                            if (responseRange != null
                                    && !(responseRange.from == 0
                                    && responseRange.to == BytesRange.TO_END_OF_CONTENT)) {
                                // Only treat as a partial image if the range is not all of the content
                                fetchState.setResponseBytesRange(responseRange);
                                fetchState.setOnNewResultStatusFlags(Consumer.IS_PARTIAL_RESULT);
                            }

                            long contentLength = body.contentLength();
                            if (contentLength < 0) {
                                contentLength = 0;
                            }
                            callback.onResponse(body.byteStream(), (int) contentLength);
                        } catch (Exception e) {
                            handleException(call, e, callback);
                        } finally {
                            body.close();
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        handleException(call, e, callback);
                    }
                });
    }

    @Override
    public void onFetchCompletion(SmbAndHttpFetchState fetchState, int byteSize) {
        fetchState.fetchCompleteTime = SystemClock.elapsedRealtime();
    }

    @Override
    public Map<String, String> getExtraMap(SmbAndHttpFetchState fetchState, int byteSize) {
        Map<String, String> extraMap = new HashMap<>(4);
        extraMap.put(QUEUE_TIME, Long.toString(fetchState.responseTime - fetchState.submitTime));
        extraMap.put(FETCH_TIME, Long.toString(fetchState.fetchCompleteTime - fetchState.responseTime));
        extraMap.put(TOTAL_TIME, Long.toString(fetchState.fetchCompleteTime - fetchState.submitTime));
        extraMap.put(IMAGE_SIZE, Integer.toString(byteSize));
        return extraMap;
    }

    private void handleException(final Call call, final Exception e, final Callback callback) {
        if (call.isCanceled()) {
            callback.onCancellation();
        } else {
            callback.onFailure(e);
        }
    }

}
