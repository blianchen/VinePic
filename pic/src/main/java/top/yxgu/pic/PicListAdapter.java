package top.yxgu.pic;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.net.URI;
import java.util.List;

public class PicListAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private List<ItemInfo> mData;
    private int mResource;

    public PicListAdapter(Context context, List<ItemInfo> data,
                           @LayoutRes int resource) {
        mData = data;
        mResource = resource;
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


        int width = 128, height = 128;
        ImageRequest request =ImageRequestBuilder
                .newBuilderWithSource(Uri.parse(item.url))
                .setResizeOptions(new ResizeOptions(width, height))
                .build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setOldController(v.getController())
                .setImageRequest(request)
                .build();
        v.setController(controller);

//        v.setImageURI(item.url);
        final TextView v1 = view.findViewById(R.id.ItemText);
        v1.setText(item.name);
        return view;
    }
}
