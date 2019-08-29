package com.piclib.transfer;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.piclib.loader.GlideImageLoader;
import com.piclib.loader.TransferImageLoader;
import com.piclib.style.IProgressIndicator;

import java.io.File;
import java.util.List;

/**
 * 用户指定了缩略图路径，使用该路径加载缩略图，
 * 并使用 {@link TransferImage#CATE_ANIMA_TOGETHER} 动画类型展示图片
 * <p>
 * Created by hitomi on 2017/5/4.
 * <p>
 * email: 196425254@qq.com
 */
class RemoteThumState extends TransferState {

    RemoteThumState(TransferLayout transfer) {
        super(transfer);
    }

    @Override
    public void prepareTransfer(final TransferImage transImage, final int position) {

        final TransferConfig config = transfer.getTransConfig();
        if(position<config.getOriginImageList().size()){
            //直接使用Imageview的drawable
            Drawable drawable = config.getOriginImageList().get(position).getDrawable();
            if(drawable==null){
                transImage.setImageDrawable(config.getMissDrawable(transfer.getContext()));//使用占位图
            }else{
                transImage.setImageDrawable(drawable);
            }
        }else{
            //加载预览图
            String imgUrl = config.getThumbnailImageList().get(position);
            GlideImageLoader.with(transfer.getContext()).load(imgUrl).intoPre(transImage);
        }

    }

    @Override
    public TransferImage createTransferIn(final int position) {
        TransferConfig config = transfer.getTransConfig();
        TransferImage transImage = createTransferImage(
                config.getOriginImageList().get(position));
        transformThumbnail(position, transImage, true);
        transfer.addView(transImage, 1);

        return transImage;
    }

    @Override
    public void transferLoad(final int position) {

        TransferAdapter adapter = transfer.getTransAdapter();
        final TransferConfig config = transfer.getTransConfig();
        final TransferImage targetImage = transfer.getTransAdapter().getImageItem(position);
        final TransferImageLoader imageLoader = config.getImageLoader();
        final IProgressIndicator progressIndicator = config.getProgressIndicator();
        progressIndicator.attach(position, adapter.getParentItem(position));

        if (config.isJustLoadHitImage()) {
            // 如果用户设置了 JustLoadHitImage 属性，说明在 prepareTransfer 中已经
            // 对 TransferImage 裁剪且设置了占位图， 所以这里直接加载原图即可
            loadSourceImage(targetImage.getDrawable(), position, targetImage, progressIndicator);
        } else {
           final String thumUrl = config.getThumbnailImageList().get(position);
            if (imageLoader.isLoaded(thumUrl)) {
                imageLoader.loadImageAsync(thumUrl, new TransferImageLoader.ThumbnailCallback() {

                    @Override
                    public void onFinish(File file) {
                        if (file != null){
                            targetImage.setImage(file);
                        }
                    }

                });
            } else {
                loadSourceImage(config.getMissDrawable(transfer.getContext()),
                        position, targetImage, progressIndicator);
            }
        }
    }

    private void loadSourceImage(Drawable drawable, final int position, final TransferImage targetImage, final IProgressIndicator progressIndicator) {
        final TransferConfig config = transfer.getTransConfig();

        config.getImageLoader().showImage(config.getSourceImageList().get(position),
                targetImage, drawable, new TransferImageLoader.SourceCallback() {

                    @Override
                    public void onStart() {
                        progressIndicator.onStart(position);
                    }

                    @Override
                    public void onProgress(int progress) {
                        progressIndicator.onProgress(position, progress);
                    }

                    @Override
                    public void onFinish() {
                    }

                    @Override
                    public void onDelivered(int status) {
                        switch (status) {
                            case TransferImageLoader.STATUS_DISPLAY_SUCCESS:
                                progressIndicator.onFinish(position); // onFinish 只是说明下载完毕，并没更新图像
                                // 启用 TransferImage 的手势缩放功能
                                targetImage.setScaleEnable(true);
                                // 绑定点击关闭 Transferee
                                transfer.bindOnOperationListener(targetImage, position);
                                break;
                            case TransferImageLoader.STATUS_DISPLAY_FAILED:  // 加载失败，显示加载错误的占位图
                                targetImage.setImageDrawable(config.getErrorDrawable(transfer.getContext()));
                                progressIndicator.onFinish(position);
                                transfer.bindOnOperationListener(targetImage, position);
                                break;
                        }
                    }
                });
    }

    @Override
    public TransferImage transferOut(final int position) {
        TransferImage transImage = null;

        TransferConfig config = transfer.getTransConfig();
        List<ImageView> originImageList = config.getOriginImageList();

        if (position <= originImageList.size() - 1 && originImageList.get(position) != null) {
            transImage = createTransferImage(originImageList.get(position));
            transformThumbnail(position, transImage, false);
            transfer.addView(transImage, 1);
        }

        return transImage;
    }
}
