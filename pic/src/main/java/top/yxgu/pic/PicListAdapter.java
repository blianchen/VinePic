package top.yxgu.pic;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.util.List;

public class PicListAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private List<ItemInfo> mData;
    private int mResource;
    private Context mContext;

    public PicListAdapter(Context context, List<ItemInfo> data,
                           @LayoutRes int resource) {
        mData = data;
        mResource = resource;
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ItemInfo item = mData.get(position);
        if (item == null) {
            return null;
        }

        View view;
        if (convertView == null) {
            view = mInflater.inflate(mResource, parent, false);
        } else {
            view = convertView;
        }

        final SimpleDraweeView v = view.findViewById(R.id.ItemImage);

        GenericDraweeHierarchy hierarchy = v.getHierarchy();
        if (item.type == ItemInfo.TYPE_FOLDER) {
//            hierarchy.setPlaceholderImage(R.drawable.icon_folder);
            v.setImageResource(R.drawable.icon_folder);
        } else if (item.type == ItemInfo.TYPE_IMAGE) {
            hierarchy.setPlaceholderImage(R.drawable.icon_pic);
            int width = 256, height = 256;
            ImageRequest request =ImageRequestBuilder
                    .newBuilderWithSource(Uri.parse(item.url))
                    .setResizeOptions(new ResizeOptions(width, height))
                    .build();
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request)
                    .setOldController(v.getController())
                    .build();
            v.setController(controller);
        } else if (item.type == ItemInfo.TYPE_MOVIE) {
            hierarchy.setPlaceholderImage(R.drawable.icon_movie);
            hierarchy.setOverlayImage(this.mContext.getResources().getDrawable(R.drawable.icon_movie, null));
//            v.setImageResource(R.drawable.icon_movie);
            int width = 256, height = 256;
            ImageRequest request =ImageRequestBuilder
                    .newBuilderWithSource(Uri.parse(item.url))
                    .setResizeOptions(new ResizeOptions(width, height))
                    .build();
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request)
//                    .setControllerListener(new BaseControllerListener<ImageInfo>(){
//                        @Override
//                        public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
//                        }
//                    })
                    .setOldController(v.getController())
                    .build();
            v.setController(controller);
        } else {
            v.setImageResource(R.drawable.icon_unknown);
        }

        final TextView v1 = view.findViewById(R.id.ItemText);
        v1.setText(item.name);
        return view;
    }
}
