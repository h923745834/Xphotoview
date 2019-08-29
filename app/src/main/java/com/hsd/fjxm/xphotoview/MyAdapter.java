package com.hsd.fjxm.xphotoview;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.piclib.loader.GlideImageLoader;

import java.util.List;

/**
 * Created by hsd on 2019/8/14.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private Context context;
    private List<String> urlList;

    public MyAdapter(Context context, List<String> urlList) {
        this.context = context;
        this.urlList = urlList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.recycleview_item, null);
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int postion) {
        ImageView imageView = viewHolder.imageView;
        viewHolder.setOnClick(imageView,postion);
        String url = urlList.get(postion);
        GlideImageLoader.with(context).load(url).intoPre(imageView);
    }

    @Override
    public int getItemCount() {
        return urlList!=null?urlList.size():0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageview);


        }
        public void setOnClick(View view,final int position){
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (context instanceof OnItemClickListener) {
                        ((OnItemClickListener) context).onItemClick(v, position);
                    }
                }
            });
        }
    }
    interface OnItemClickListener{
        abstract void onItemClick(View v,int position);
        }

}
