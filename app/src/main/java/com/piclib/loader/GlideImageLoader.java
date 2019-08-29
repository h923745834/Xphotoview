package com.piclib.loader;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.hsd.fjxm.xphotoview.R;
import com.piclib.XLog;
import com.piclib.view.Xphoto.XPhotoView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用 <a href="https://github.com/nostra13/Android-Universal-Image-Loader">
 * Android-Universal-Image-Loader</a>作为 Transferee 的图片加载器
 * <p>
 * Created by hitomi on 2017/5/3.
 * <p>
 * email: 196425254@qq.com
 */
public class GlideImageLoader implements TransferImageLoader {

    private Context context;
    private String mUrl;
    private Map<String,Boolean> failMap = new HashMap<>();
    private final String TAG = "GlideImageLoader";


    private final RequestOptions ResourceOptions = new RequestOptions()
                     .diskCacheStrategy(DiskCacheStrategy.RESOURCE)//缓存转换后的图片
                     .placeholder(R.mipmap.ic_launcher)
                     .error(R.mipmap.ic_launcher);
    private final RequestOptions DataOptions = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.DATA);//缓存原图

    private GlideImageLoader(Context context) {
        this.context = context;

    }

    public static GlideImageLoader with(Context context) {
        return new GlideImageLoader(context);
    }

    private void displayImage(final String url, final ImageView target, RequestOptions options, RequestListener listener ) {

        CustomTarget<File> simpleTarget = new CustomTarget<File>() {

            @Override
            public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                if (resource != null) {
                    if(target instanceof XPhotoView)
                        ((XPhotoView) target).setImage(resource);
                }
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                //取消下载时被调用
                ProgressInterceptor.removeListener(url);
            }
        };
        if (TextUtils.isEmpty(url)) {
            return;
        }
//        if (url.contains(".gif")) {
//            Glide.with(context).asGif().load(url).apply(options).listener(listener).into(target);
//        } else {
            Glide.with(context).asFile().load(url).apply(options).listener(listener).into(simpleTarget);
//        }
    }

    @Override
    public void showImage(final String imageUrl, ImageView imageView, Drawable placeholder, final SourceCallback sourceCallback) {

        DataOptions.placeholder(placeholder);
        DataOptions.error(placeholder);
        displayImage(imageUrl, imageView, DataOptions, new RequestListener() {

            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                if (sourceCallback != null)
                    sourceCallback.onDelivered(STATUS_DISPLAY_FAILED);
                ProgressInterceptor.removeListener(imageUrl);
                return false;
            }

            @Override
            public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                if (sourceCallback != null) {
                    sourceCallback.onFinish();
                    sourceCallback.onDelivered(STATUS_DISPLAY_SUCCESS);
                }
                ProgressInterceptor.removeListener(imageUrl);
                return false;
            }

        });
        ProgressInterceptor.addListener(imageUrl, new ProgressListener() {
            @Override
            public void onProgress(int progress) {
                XLog.d(TAG,"loading percent："+progress);
                if (sourceCallback != null) {
                    sourceCallback.onProgress(progress);
                }
            }
        });
    }

    @Override
    public void loadImageAsync(String imageUrl, final ThumbnailCallback callback) {

        Glide.with(context).asFile().load(imageUrl).apply(DataOptions).into(new SimpleTarget<File>() {
            @Override
            public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                if (callback != null) {
                    callback.onFinish(resource);
                }
            }
        });
    }

    @Override
    public boolean isLoaded(String url) {
        Boolean aBoolean = failMap.get(url);
        if(aBoolean==null){
            return true;
        }
        return aBoolean;
    }

    @Override
    public void clearCache() {

    }

    public GlideImageLoader load(String url){
        mUrl = url;
        return this;
    }

    public void intoPre(final ImageView target){

        ResourceOptions.transform(new FirstScreenTransformation(context));
        Glide.with(context).load(mUrl).apply(ResourceOptions).listener(new RequestListener() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                failMap.put((String)model,false);
                return false;
            }

            @Override
            public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
                failMap.remove(model);
                return false;
            }
        }).into(target);
    }
}
