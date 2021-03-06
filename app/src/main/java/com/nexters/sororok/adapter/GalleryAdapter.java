package com.nexters.sororok.adapter;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.nexters.sororok.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.util.ArrayList;

public class GalleryAdapter extends BaseAdapter{

    private Context context;
    private ArrayList<String> imageUrls;
    private SparseBooleanArray mSparseBooleanArray;//Variable to store selected Images
    private DisplayImageOptions options;

    public GalleryAdapter(Context context, ArrayList<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
        mSparseBooleanArray = new SparseBooleanArray();
        /*options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .resetViewBeforeLoading(true).cacheOnDisk(true)
                .considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565)
                .build();*/
    }

    //Method to return selected Images
    public ArrayList<String> getCheckedItems() {
        ArrayList<String> mTempArry = new ArrayList<>();

        for (int i = 0; i < imageUrls.size(); i++) {
            if (mSparseBooleanArray.get(i)) {
                mTempArry.add(imageUrls.get(i));
            }
        }
        return mTempArry;
    }

    @Override
    public int getCount() {
        return imageUrls.size();
    }

    @Override
    public Object getItem(int position) {
        return imageUrls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (view == null)
            view = inflater.inflate(R.layout.gridview_item, viewGroup, false);//Inflate layout

        final ImageView imageView = view.findViewById(R.id.img_user_gallery);
       // ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this.context)); //추가

        if(position==0)
            Glide.with(context).load(R.drawable.default_camera).into(imageView);
            //ImageLoader.getInstance().displayImage("drawable://"+R.drawable.camera, imageView, options);
        else
            Glide.with(context).load("file://" + imageUrls.get(position)).into(imageView);
           // ImageLoader.getInstance().displayImage("file://" + imageUrls.get(position), imageView, options);

        return view;
    }



}
