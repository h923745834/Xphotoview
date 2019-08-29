package com.piclib.transfer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.hsd.fjxm.xphotoview.R;
import com.piclib.style.IIndexIndicator;
import java.util.HashSet;
import java.util.Set;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Transferee 中 Dialog 显示的内容
 * <p>
 * 所有过渡动画的展示，图片的加载都是在这个 FrameLayout 中实现
 * <p>
 * Created by Hitomis on 2017/4/23 0023.
 * <p>
 * email: 196425254@qq.com
 */
class TransferLayout extends FrameLayout {

    private Context context;
    private TransferImage transImage;
    private ViewPager transViewPager;
    private TransferAdapter transAdapter;
    private TransferConfig transConfig;
    private OnLayoutResetListener layoutResetListener;
    private Set<Integer> loadedIndexSet;

    /**
     * ViewPager 页面切换监听器 => 当页面切换时，根据相邻优先加载的规则去加载图片
     */
    private ViewPager.OnPageChangeListener transChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            transConfig.setNowThumbnailIndex(position);

            if (transConfig.isJustLoadHitImage()) {
                loadSourceImageOffset(position, 0);
            } else {
                for (int i = 1; i <= transConfig.getOffscreenPageLimit(); i++) {
                    loadSourceImageOffset(position, i);
                }
            }
        }
    };
    /**
     * TransferAdapter 中对应页面创建完成监听器
     */
    private TransferAdapter.OnInstantiateItemListener instantListener = new TransferAdapter.OnInstantiateItemListener() {
        @Override
        public void onComplete() {
            transViewPager.addOnPageChangeListener(transChangeListener);

            int position = transConfig.getNowThumbnailIndex();
            if (transConfig.isJustLoadHitImage()) {
                loadSourceImageOffset(position, 0);
            } else {
                loadSourceImageOffset(position, 1);
            }

        }

    };

    /**
     * TransferImage 伸/缩动画执行完成监听器
     */
    private TransferImage.OnTransferListener transListener = new TransferImage.OnTransferListener() {
        @Override
        public void onTransferStart(int state, int cate, int stage) {
            if (state == TransferImage.STATE_TRANS_OUT){
                setBackgroundColor(0);
                transViewPager.setVisibility(INVISIBLE);
            }

        }

        @Override
        public void onTransferComplete(int state, int cate, int stage) {
            if (cate == TransferImage.CATE_ANIMA_TOGETHER) {
                switch (state) {
                    case TransferImage.STATE_TRANS_IN: // 伸展动画执行完毕
                        addIndexIndicator();
                        transViewPager.setVisibility(View.VISIBLE);
                        removeFromParent(transImage);
                        setBackgroundColor(transConfig.getBackgroundColor());
                        break;
                    case TransferImage.STATE_TRANS_OUT: // 缩小动画执行完毕
                        resetTransfer();
                        setBackgroundColor(0);
                        break;
                }
            } else { // 如果动画是分离的
                switch (state) {
                    case TransferImage.STATE_TRANS_IN:
                        if (stage == TransferImage.STAGE_TRANSLATE) {
                            // 第一阶段位移动画执行完毕
                            addIndexIndicator();
                            transViewPager.setVisibility(View.VISIBLE);
                            removeFromParent(transImage);
                            setBackgroundColor(transConfig.getBackgroundColor());
                        }
                        break;
                    case TransferImage.STATE_TRANS_OUT:
                        if (stage == TransferImage.STAGE_TRANSLATE) {
                            // 位移动画执行完毕
                            resetTransfer();
                            setBackgroundColor(0);
                        }
                        break;
                }
            }
        }
    };

    /**
     * 构造方法
     *
     * @param context 上下文环境
     */
    TransferLayout(Context context) {
        super(context);

        this.context = context;
        this.loadedIndexSet = new HashSet<>();
    }

    /**
     * 加载 [position - offset] 到 [position + offset] 范围内有效索引位置的图片
     *
     * @param position 当前显示图片的索引
     * @param offset   postion 左右偏移量
     */
    private void loadSourceImageOffset(int position, int offset) {
        int left = position - offset;
        int right = position + offset;

        if (!loadedIndexSet.contains(position)) {
            loadSourceImage(position);
            loadedIndexSet.add(position);
        }
        if (left >= 0 && !loadedIndexSet.contains(left)) {
            loadSourceImage(left);
            loadedIndexSet.add(left);
        }
        if (right < transConfig.getSourceImageList().size() && !loadedIndexSet.contains(right)) {
            loadSourceImage(right);
            loadedIndexSet.add(right);
        }
    }

    public void removeLoadedIndexSet(int position){
        loadedIndexSet.remove(position);
    }

    private int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 加载索引位置为 position 处的图片
     *
     * @param position 当前有效的索引
     */
    private void loadSourceImage(int position) {
        getTransferState(position).transferLoad(position);
    }

    /**
     * 重置 TransferLayout 布局中的内容
     */
    private void resetTransfer() {
        loadedIndexSet.clear();
        removeIndexIndicator();
        removeAllViews();
        layoutResetListener.onReset();
    }

    /**
     * 创建 ViewPager 并添加到 TransferLayout 中
     */
    private void createTransferViewPager() {

        TextView textView = new TextView(context);
        textView.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
        textView.setBackgroundColor(0x50000000);
        textView.setTextSize(15);
        textView.setGravity(Gravity.CENTER);
        textView.setText("保存");
        LayoutParams textLp = new LayoutParams(dip2px(50),dip2px(25));
        textLp.gravity = Gravity.TOP|Gravity.RIGHT;
        textLp.setMargins(0,dip2px(25),dip2px(15),0);

        transAdapter = new TransferAdapter(this,
                transConfig.getSourceImageList().size(),
                transConfig.getNowThumbnailIndex());
        transAdapter.setOnInstantListener(instantListener);

        transViewPager = new ViewPager(context);
        // 先隐藏，待 ViewPager 下标为 config.getCurrOriginIndex() 的页面创建完毕再显示
        transViewPager.setVisibility(View.INVISIBLE);
        transViewPager.setOffscreenPageLimit(1);
        transViewPager.setAdapter(transAdapter);
        transViewPager.setCurrentItem(transConfig.getNowThumbnailIndex());

        addView(transViewPager, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
        addView(textView,textLp);
       //点击保存
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = transConfig.getNowThumbnailIndex();
                String imgUrl = transConfig.getSourceImageList().get(index);
                Toast.makeText(context,"此处可写保存逻辑！",Toast.LENGTH_LONG).show();

            }
        });
    }

    /**
     * 将 view 从 view 的父布局中移除
     *
     * @param view 待移除的 view
     */
    private void removeFromParent(View view) {
        ViewGroup vg = (ViewGroup) view.getParent();
        if (vg != null)
            vg.removeView(view);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // unregister PageChangeListener
        if(transAdapter!=null)
            transAdapter.clear();
        transViewPager.removeOnPageChangeListener(transChangeListener);
    }

    public TransferAdapter getTransAdapter() {
        return transAdapter;
    }

    public TransferConfig getTransConfig() {
        return transConfig;
    }

    public TransferImage.OnTransferListener getTransListener() {
        return transListener;
    }

    /**
     * 初始化 TransferLayout 中的各个组件，并执行图片从缩略图到 Transferee 进入动画
     */
    public void show() {

        createTransferViewPager();
        int nowThumbnailIndex = transConfig.getNowThumbnailIndex();
        TransferState transferState = getTransferState(nowThumbnailIndex);
        transImage = transferState.createTransferIn(nowThumbnailIndex);
    }

    /**
     * 依据当前有效索引 position 创建并返回一个 {@link TransferState}
     *
     * @param position 前有效索引
     * @return {@link TransferState}
     */
    TransferState getTransferState(int position) {
        TransferState transferState;

//        if (!transConfig.isThumbnailEmpty()) { // 客户端指定了缩略图路径集合
            transferState = new RemoteThumState(this);
//        } else {
//            String url = transConfig.getSourceImageList().get(position);
//            if(transConfig.getImageLoader().isLoaded(url)){
//                transferState = new LocalThumState(this);
//            }else{
//                transferState = new EmptyThumState(this);
//            }
//        }
        return transferState;
    }

    /**
     * 为加载完的成图片ImageView 绑定点 Transferee 操作事件
     *
     * @param imageView 加载完成的 ImageView
     * @param pos       关闭 Transferee 时图片所在的索引
     */
    public void bindOnOperationListener(final ImageView imageView, final int pos) {
        // bind click dismiss listener
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss(pos);
            }
        });

        // bind long click listener
        if (transConfig.getLongClickListener() != null)
            imageView.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    transConfig.getLongClickListener().onLongClick(imageView, pos);
                    return false;
                }
            });
    }

    /**
     * 开启 Transferee 关闭动画，并隐藏 transferLayout 中的各个组件
     *
     * @param pos 关闭 Transferee 时图片所在的索引
     */
    public void dismiss(int pos) {
        if (transImage != null && transImage.getState()
                == TransferImage.STATE_TRANS_OUT) // 防止双击
            return;

        transImage = getTransferState(pos).transferOut(pos);

        if (transImage == null){
            diffusionTransfer(pos);
        }
        hideIndexIndicator();

    }

    /**
     * 扩散消失动画
     *
     * @param pos 动画作用于 pos 索引位置的图片
     */
    private void diffusionTransfer(int pos) {
        transImage = transAdapter.getImageItem(pos);
        transImage.setState(TransferImage.STATE_TRANS_OUT);
        transImage.setScaleEnable(false);

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setDuration(transConfig.getDuration());
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        PropertyValuesHolder alphaHolder = PropertyValuesHolder.ofFloat("alpha", 1, 0);
        PropertyValuesHolder scaleXHolder = PropertyValuesHolder.ofFloat("scaleX", 1, 1.2f);
        valueAnimator.setValues(alphaHolder, scaleXHolder);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (Float) animation.getAnimatedValue("alpha");
                float scale = (Float) animation.getAnimatedValue("scaleX");

                transImage.setAlpha(alpha);
                transImage.setScaleX(scale);
                transImage.setScaleY(scale);
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                setBackgroundColor(0);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                resetTransfer();
            }
        });

        valueAnimator.start();
    }

    /**
     * 配置参数
     *
     * @param config 参数对象
     */
    public void apply(TransferConfig config) {
        transConfig = config;
    }

    /**
     * 绑定 TransferLayout 内容重置时回调监听器
     *
     * @param listener 重置回调监听器
     */
    public void setOnLayoutResetListener(OnLayoutResetListener listener) {
        layoutResetListener = listener;
    }

    /**
     * 在 TransferImage 面板中添加下标指示器 UI 组件
     */
    private void addIndexIndicator() {
        IIndexIndicator indexIndicator = transConfig.getIndexIndicator();
        if (indexIndicator != null && transConfig.getSourceImageList().size() >= 2) {
            indexIndicator.attach(this);
            indexIndicator.onShow(transViewPager);
        }
    }

    /**
     * 隐藏下标指示器 UI 组件
     */
    private void hideIndexIndicator() {
        IIndexIndicator indexIndicator = transConfig.getIndexIndicator();
        if (indexIndicator != null && transConfig.getSourceImageList().size() >= 2) {
            indexIndicator.onHide();
        }
    }

    /**
     * 从 TransferImage 面板中移除下标指示器 UI 组件
     */
    private void removeIndexIndicator() {
        IIndexIndicator indexIndicator = transConfig.getIndexIndicator();
        if (indexIndicator != null && transConfig.getSourceImageList().size() >= 2) {
            indexIndicator.onRemove();
        }
    }

    /**
     * TransferLayout 中内容重置时监听器
     */
    interface OnLayoutResetListener {
        /**
         * 调用于：当关闭动画执行完毕，TransferLayout 中所有内容已经重置（清空）时
         */
        void onReset();
    }

}
