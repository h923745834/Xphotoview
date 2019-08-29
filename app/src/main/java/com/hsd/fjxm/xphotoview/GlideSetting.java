package com.hsd.fjxm.xphotoview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.DiskLruCacheWrapper;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.piclib.loader.OkHttpUrlLoader;
import com.piclib.loader.ProgressInterceptor;

import java.io.File;
import java.io.InputStream;

import okhttp3.OkHttpClient;

/**
 * Created by hsd on 2019/8/15.
 */
@GlideModule
public class GlideSetting extends AppGlideModule {

    @Override
    public void applyOptions(@NonNull final Context context, @NonNull GlideBuilder builder) {
        super.applyOptions(context, builder);
        builder.setDiskCache(new DiskCache.Factory() {
            @Nullable
            @Override
            public DiskCache build() {
                File cacheDir = new File(context.getExternalCacheDir(),"glidecache");
                return DiskLruCacheWrapper.create(cacheDir,30*1024*1024);
            }
        });

    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new ProgressInterceptor())
                .build();
        registry.replace(GlideUrl.class,InputStream.class,new OkHttpUrlLoader.Factory(okHttpClient));
//        super.registerComponents(context, glide, registry);
    }
}
