package com.piclib.view.Xphoto;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by zhenghui on 2017/5/19.
 */

public class XPhotoView extends android.support.v7.widget.AppCompatImageView implements IXphotoView {
    private static final String TAG = "XPhotoView";

    private IViewAttacher mPhotoAttacher;

    private GestureManager mGestureManager;

    private OnTabListener mSingleTabListener;

    private DoubleTabScale mDefaultDoubleTabScale;

    private Movie mMovie;

    private boolean sScaleEnable = true;

    private boolean sGif = false;

    private long movieStart;
    private OnClickListener mClickListener;

    public XPhotoView(Context context) {
        this(context, null, 0);
    }

    public XPhotoView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XPhotoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs);
        mPhotoAttacher = new PhotoViewAttacher(this);
        mGestureManager = new GestureManager(this.getContext(), this, mPhotoAttacher);
    }

    /**
     * 获取默认配置属性，如 ScaleType 等*/
    private void initialize(Context context, AttributeSet attrs) {
        mDefaultDoubleTabScale = DoubleTabScale.CENTER_CROP;

        super.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
    }

    public void setScaleEnable(boolean flag) {
        sScaleEnable = flag;
    }

    public void setSingleTabListener(OnTabListener listener) {
        mSingleTabListener = listener;
    }

    public void setImageResource(@DrawableRes int resId) {
        Drawable drawable = this.getContext().getResources().getDrawable(resId);
        if(drawable == null) {
            return;
        }
        this.setImageDrawable(drawable);

    }

    /** 设置图片的主入口 */
    public void setImage(Bitmap image) {
        if(sGif) {
            return;
        }
        super.setImageBitmap(image);
        mPhotoAttacher.setBitmap(image, false);
        onSetImageFinished(null, true, null);
    }

    public void setImage(String path) {
        setImage(new File(path));
    }

    public void setImage(File file) {

        try {
            if(file == null || !file.exists()) {
                return;
            }
            FileInputStream fis = new FileInputStream(file);
            mMovie = Movie.decodeStream(fis);
            fis.close();
            if(mMovie == null) {
                sGif = false;
                mPhotoAttacher.setInputStream(file, Bitmap.Config.RGB_565);
            } else {
                sGif = true;
                sScaleEnable = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                }
            }
            onSetImageFinished(null, true, null);
        } catch (FileNotFoundException exp) {

        } catch (IOException e) {

        }
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        onSetImageFinished(null, true, null);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (sScaleEnable) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mPhotoAttacher != null && !mPhotoAttacher.isNotAvailable()) {
                        interceptParentTouchEvent(true);
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    break;

                case MotionEvent.ACTION_UP:
                    interceptParentTouchEvent(false);
                    break;
            }
        }

        return mGestureManager.onTouchEvent(event);
    }

    @Override
    public void onSingleTab() {
        if(mSingleTabListener != null) {
            mSingleTabListener.onSingleTab();
        }
        //调用点击事件
        performClick();
    }

    @Override
    public void onLongTab() {
        if(mSingleTabListener != null) {
            mSingleTabListener.onLongTab();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
        if(sGif && mMovie != null) {
            onGifDraw(canvas);
        } else if(!sGif){
            mPhotoAttacher.draw(canvas, getWidth(), getHeight());
        }
    }

    private void onGifDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        int vH = getHeight();
        int vW = getWidth();
        int mH = mMovie.height();
        int mW = mMovie.width();
        float scaleX = (float) getWidth() * 1f / mMovie.width();
        float scaleY = (float) getHeight() * 1f / mMovie.height();
        float scale = Math.min(scaleX, scaleY);
        canvas.scale(scale, scale);

        //make sure picture shown in center
        int startY = Math.round((vH * 1f / scale - mH) / 2);
        int startX = Math.round((vW * 1f / scale - mW) / 2);

        long now = android.os.SystemClock.uptimeMillis();

        if (movieStart == 0) {
            movieStart = (int) now;
        }

        int duration;
        if (mMovie != null) {
            duration = mMovie.duration() == 0 ? 500:mMovie.duration();
            int relTime = (int) ((now - movieStart) % duration);
            mMovie.setTime(relTime);
            mMovie.draw(canvas, startX, startY);
            this.invalidate();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mPhotoAttacher.onViewSizeChanged(w, h);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(sGif && mMovie != null) {
            mMovie = null;
        }
        mPhotoAttacher.destroy();
    }

    @Override
    public void recycleAll() {
        this.onDetachedFromWindow();
    }

    @Override
    public DoubleTabScale getDoubleTabScale() {
        return mDefaultDoubleTabScale;
    }

    @Override
    public String getCachedDir() {
        return null;
    }

    @Override
    public void onImageSetFinished(boolean finished) {

    }

    @Override
    public void callPostInvalidate() {
        postInvalidate();
    }

    @Override
    public void onSetImageFinished(IViewAttacher bm, boolean success, Rect image) {

    }

    @Override
    public void interceptParentTouchEvent(boolean intercept) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(intercept);
        }
    }

    public void setGif(boolean sGif) {
        this.sGif = sGif;
    }
}