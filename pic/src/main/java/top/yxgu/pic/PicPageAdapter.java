package top.yxgu.pic;

import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.AutoRotateDrawable;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.util.LinkedList;
import java.util.List;

import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;

public class PicPageAdapter extends PagerAdapter {

    private List<ItemInfo> mDatas;//数据源
    private LinkedList<SimpleDraweeView> mViewCache;//缓存view
    private LinkedList<JzvdStd> mVideoCache;
    private Context mContext;
    private int mChildCount;

//    private boolean isTv = false;

    private int width;
    private int height;

    public PicPageAdapter(Context context, List<ItemInfo> list, int width, int height) {
        this.mContext = context;
        this.mDatas = list;
        mViewCache = new LinkedList<>();
        mVideoCache = new LinkedList<>();
        this.width = width;
        this.height = height;

//        UiModeManager uiModeManager = (UiModeManager)mContext.getSystemService(Context.UI_MODE_SERVICE);
//        if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
//            isTv = true;
//        }
    }

    @Override
    public void notifyDataSetChanged() {
        mChildCount = getCount();
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.mDatas.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }


    @Override
    public int getItemPosition(Object object) {
        if (mChildCount > 0) {
            mChildCount--;
            return POSITION_NONE;
        }
        return super.getItemPosition(object);
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.setPrimaryItem(container, position, object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ItemInfo item = mDatas.get(position);

        View pageView;
        if (item.type == ItemInfo.TYPE_MOVIE) {
//            VideoView view = new VideoView(mContext);
//            view.setVideoPath(item.url);

            JzvdStd view;
            if (mVideoCache.size() == 0) {
                view = new JzvdStd(mContext);
            } else {
                view = mVideoCache.removeFirst();
            }
            view.setUp(item.url, item.name, Jzvd.SCREEN_WINDOW_NORMAL);
            pageView = view;
        } else {
            SimpleDraweeView view;
            if (mViewCache.size() == 0) {
                view = new SimpleDraweeView(mContext);
                view.setBackgroundColor(0xff000000);
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(width, height);
                view.setLayoutParams(layoutParams);

                view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        ((PicPageActivity)mContext).getHandler().sendEmptyMessage(PicPageActivity.AutoHandler.MSG_AUTO_PLAY);
                        return false;
                    }
                });

                GenericDraweeHierarchy hierarchy = view.getHierarchy();
                hierarchy.setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER);
                hierarchy.setPlaceholderImage(R.drawable.icon_pic, ScalingUtils.ScaleType.CENTER_INSIDE);
                hierarchy.setFailureImage(R.drawable.icon_err, ScalingUtils.ScaleType.CENTER_INSIDE);
                hierarchy.setProgressBarImage(new AutoRotateDrawable(mContext.getResources().getDrawable(R.drawable.icon_progress_bar, null), 2000), ScalingUtils.ScaleType.CENTER_INSIDE);
            } else {
                view = mViewCache.removeFirst();
            }

            ImageRequest request = ImageRequestBuilder
                    .newBuilderWithSource(Uri.parse(item.url))
                    .setResizeOptions(new ResizeOptions(width, height))
                    .build();
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request)
                    .setOldController(view.getController())
                    .build();
            view.setController(controller);
            pageView = view;
        }

        //这里我是用Facebook的Fresco加载的图片，你可以在这里换成你使用的图片加载方式
//        final SimpleDraweeView view = view;
//        PipelineDraweeControllerBuilder controller = Fresco.newDraweeControllerBuilder();
//        controller.setUri(Uri.parse(Constants.PICTURE_HOST + mDatas.get(position)));
//        controller.setOldController(view.getController());
//        controller.setControllerListener(new BaseControllerListener<ImageInfo>() {
//            @Override
//            public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
//                super.onFinalImageSet(id, imageInfo, animatable);
//                if (imageInfo == null) {
//                    return;
//                }
//                view.update(imageInfo.getWidth(), imageInfo.getHeight());
//            }
//        });
//        view.setController(controller.build());
//        view.setImageURI(mDatas.get(position).url);
        pageView.setTag(position);
        container.addView(pageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return pageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (object instanceof JzvdStd) {
            JzvdStd contentView = (JzvdStd) object;
            contentView.release();
            container.removeView(contentView);
            this.mVideoCache.add(contentView);
        } else {
            SimpleDraweeView contentView = (SimpleDraweeView) object;
            container.removeView(contentView);
            this.mViewCache.add(contentView);
        }
    }
}
