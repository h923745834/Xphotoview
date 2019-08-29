package com.hsd.fjxm.xphotoview;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.piclib.loader.GlideImageLoader;
import com.piclib.style.index.CircleIndexIndicator;
import com.piclib.style.progress.ProgressBarIndicator;
import com.piclib.transfer.TransferConfig;
import com.piclib.transfer.Transferee;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,MyAdapter.OnItemClickListener {

    private List<String> urlList = new ArrayList<>();
    private List<ImageView> viewList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private int Recycle = 1;
    private int ViewList = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initdata();
        initview();
    }

    private void initdata(){
//        try {
//            urlList.add("http://watermark.image.7723.cn/t_style_mod/attachments/icons/13/mJjrsDkBbW4SpsJMM6ZHxpBiQTiPJcGX.png");
//            String[] list = getAssets().list("");
//            for(String name:list){
//                if(name.endsWith("jpg")||name.endsWith("gif"))
//                urlList.add(;
//            }
//            urlList.add("file:///android_asset/pikacu.gif");
            urlList.add("file:///android_asset/long2.jpg");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
    private void initview() {
        mRecyclerView = findViewById(R.id.recyclerview);
        ImageView mImage1 = findViewById(R.id.image1);
        ImageView mImage2 = findViewById(R.id.image2);
        ImageView mImage3 = findViewById(R.id.image3);
        mImage1.setOnClickListener(this);
        mImage2.setOnClickListener(this);
        mImage3.setOnClickListener(this);
        GridLayoutManager manager = new GridLayoutManager(this,4);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(new MyAdapter(this,urlList));
        GlideImageLoader.with(this).load(urlList.get(0)).intoPre(mImage1);
//        GlideImageLoader.with(this).load(urlList.get(1)).intoPre(mImage2);
//        GlideImageLoader.with(this).load(urlList.get(2)).intoPre(mImage3);
        viewList.add(mImage1);
//        viewList.add(mImage2);
//        viewList.add(mImage3);
    }

    @Override
    public void onItemClick(View v, int position) {
        showTransfer(Recycle,position);
    }

    private void showTransfer(int viewFrom,int position) {
        TransferConfig config = null;
        TransferConfig.Builder builder = TransferConfig.build()
                .setSourceImageList(urlList)
                .setThumbnailImageList(urlList)
                .setMissPlaceHolder(R.mipmap.ic_launcher)
                .setErrorPlaceHolder(R.mipmap.ic_launcher)
                .setProgressIndicator(new ProgressBarIndicator())
                .setIndexIndicator(new CircleIndexIndicator())
                .setJustLoadHitImage(true)
                .setNowThumbnailIndex(position);
        if(viewFrom==Recycle) {
           config= builder .bindRecyclerView(mRecyclerView, R.id.imageview);
        }else if(viewFrom==ViewList){
          config =  builder.bindImageviewList(viewList);
        }
        Transferee.getDefault(this).apply(config).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.image1:
                showTransfer(ViewList,0);
                break;
            case R.id.image2:
                showTransfer(ViewList,1);
                break;
            case R.id.image3:
                showTransfer(ViewList,2);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new Thread(){
            @Override
            public void run() {
                Glide.get(MainActivity.this).clearDiskCache();
                super.run();
            }
        }.start();
    }


}
