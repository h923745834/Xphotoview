package com.piclib.loader;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

/**
 * Created by hsd on 2019/8/5.
 *  对长图截取（0到屏幕高度）的区域图片
 */
public class FirstScreenTransformation extends BitmapTransformation {

    private final Context context;
    private final int heightPixels;
    private final int widthPixels;
    private static final String ID = "com.glide.expand.FirstScreenTransformation";
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

    /**
     *
     * @param context
     */
    public FirstScreenTransformation(Context context) {
        this.context = context;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            final Display display = windowManager.getDefaultDisplay();
            Point outPoint = new Point();
            //获取屏幕物理尺寸，用于计算图片转换时，截图大小。
            if (Build.VERSION.SDK_INT >= 19) {
                // 可能有虚拟按键的情况
                display.getRealSize(outPoint);
                int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
                int statusBarHeight = 0;
                if (resourceId > 0) {
                    //根据资源ID获取响应的尺寸值
                    statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
                }
                //根据dialog大小，决定是否需要减去状态状态栏高度(没有沉浸式)
                this.heightPixels = outPoint.y-statusBarHeight;
                this.widthPixels = outPoint.x;
            } else {
                // 不可能有虚拟按键
                display.getSize(outPoint);
                this.heightPixels = outPoint.y;
                this.widthPixels = outPoint.x;
            }
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        final Bitmap result;
        int height = toTransform.getHeight();
        int width = toTransform.getWidth();
        float targetH = widthPixels*1f*height/width;//适配屏幕宽后，原图的高如果大于屏幕的高（竖直长图）
        if(targetH>heightPixels){
            float scale = width*1f/widthPixels;
            int clipH = (int)(heightPixels*scale);
            final Bitmap toReuse = pool.get(width, clipH, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(toReuse);
            canvas.drawBitmap(toTransform, new Rect(0, 0, width, clipH),new Rect(0,0,width,clipH),new Paint());
            result =toReuse;
            canvas.setBitmap(null);
        }else{
            result = toTransform;
        }

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof  FirstScreenTransformation;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
       messageDigest.update(ID_BYTES);
    }
}
