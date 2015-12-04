package me.geed.widget.animationbanner;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * AnimationBanner
 * <p/>
 * Created by Andy on 2015/11/30.
 * Author Email:yourswee@gmail.com
 * <p/>
 * 由于动画部分使用了ViewPropertyAnimator,因此需要API level 12，可以对动画部分的写法做些修改，使其支持API level 11
 */
public class AnimationBanner extends FrameLayout {
    /**
     * 上下文对象
     */
    private Context mContext;

    /**
     * Banner横幅数量，必须大于等于1
     */
    private int bannerNum;

    /**
     * 默认滑动动画时间，默认500
     */
    private int mDuration = 500;

    /**
     * 当前显示Banner条幅的索引
     */
    private int mCurrentBannerIndex = 0;

    /**
     * Banner点击事件监听器
     */
    private OnBannerClickListener mListener;

    /**
     * 存储所有的Banner横幅
     */
    private List<ImageView> mImageViews;

    /**
     * 存储所有的Indicator
     */
    private List<PointIndicator> mIndicators;

    /**
     * Indicator小圆点指示器位置，默认在右下方
     */
    private int mIndicatorPosition = IndicatorPosition.RIGHT_BOTTOM;

    /**
     * 指示器小圆点半径，默认为10
     */
    private int mRadius = 10;

    /**
     * 小圆点处于当前位置时的颜色
     */
    private int mSelectedColor = Color.WHITE;

    /**
     * 小圆点非当前位置时的颜色
     */
    private int mUnSelectedColor = Color.GRAY;

    /**
     * 是否只有一张广告横幅
     */
    private boolean isOnlyOne = false;

    /**
     * 动画是否正在进行状态
     */
    private boolean isAnimationing = false;

    /**
     * 是否自动循环播放，默认false
     */
    private boolean isAutoSwitch = false;

    /**
     * 用于Touch监听的初始坐标
     */
    private float firstX = 0;
    private float firstY = 0;

    /**
     * 默认距离阈值，大于此值则认为是滑动
     */
    private static final int DISTANCE_THRESHOLD = 30;

    public AnimationBanner(Context context) {
        this(context, null);
    }

    public AnimationBanner(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public void initView() {
        if (bannerNum <= 0) {
            return;
        }

        if (bannerNum == 1) {
            isOnlyOne = true;
        }
        /**
         * 添加Banner布局
         */
        LinearLayout mBannerContainer = getBannerContainer();
        if (mBannerContainer != null) {
            addView(mBannerContainer);
        }

        /**
         * 添加指示器小圆点布局
         */
        LinearLayout mIndicatorContainer = getIndicatorContainer();
        if (mIndicatorContainer != null) {
            addView(mIndicatorContainer);
        }

        /**
         * 或者在xml布局文件中设置，否则无法响应ACTION_MOVE和ACTION_UP
         */
        setClickable(true);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /**
         * 在此处理左右滑动响应
         */
        int action = event.getAction();
        float currentX, currentY;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                firstX = event.getX();
                firstY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                currentX = event.getX();
                currentY = event.getY();
                if (Math.abs(currentX - firstX) > Math.abs(currentY - firstY)) {
                    if (!isOnlyOne) {
                        if (currentX - firstX > 0) {
                            /**
                             }
                             * 向右滑动,如果处于动画状态，则不响应本次滑动
                             */
                            if (!isAnimationing) {
                                doRightAnimation();
                            }
                        } else {
                            /**
                             * 向左滑动,如果处于动画状态，则不响应本次滑动
                             */
                            if (!isAnimationing) {
                                doLeftAnimation();
                            }
                        }
                    }
                } else if (Math.abs(currentX - firstX) < DISTANCE_THRESHOLD && Math.abs(currentY - firstY) < DISTANCE_THRESHOLD) {
                    /**
                     * 响应点击，如正在滑动状态则不响应
                     */
                    if (mListener != null) {
                        if (!isAnimationing) {
                            mListener.onClick(mCurrentBannerIndex);
                        }
                    }
                }
                break;
            default:
                break;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        /**
         * 拦截事件，在onTouchEvent中做处理
         */
        return true;
    }

    /**
     * 动态生成Banner视图
     *
     * @return
     */
    private LinearLayout getBannerContainer() {
        mImageViews = new ArrayList<>();
        LinearLayout mBannerContainer = new LinearLayout(mContext);
        mBannerContainer.setOrientation(LinearLayout.HORIZONTAL);
        mBannerContainer.setLayoutParams(new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mBannerContainer.removeAllViews();

        for (int i = 0; i < bannerNum; i++) {
            ImageView imageView = new ImageView(mContext);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(getScreenWidth(), LinearLayout.LayoutParams.WRAP_CONTENT));
            mBannerContainer.addView(imageView);
            mImageViews.add(imageView);
        }

        return mBannerContainer;
    }

    /**
     * 动态生成Indicator小圆点指示器，当Banner条幅数量为1时，不显示Indicator<br>
     * 还可扩展，如在小圆点前面添加文本描述、添加透明背景等，可自由实现
     *
     * @return
     */
    private LinearLayout getIndicatorContainer() {
        if (isOnlyOne) {
            return null;
        }

        mIndicators = new ArrayList<>();
        LinearLayout mIndicatorContainer = new LinearLayout(mContext);
        mIndicatorContainer.setOrientation(LinearLayout.HORIZONTAL);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        switch (mIndicatorPosition) {
            case IndicatorPosition.CENTER_BOTTOM:
                params.gravity = Gravity.CENTER | Gravity.BOTTOM;
                break;
            case IndicatorPosition.LEFT_BOTTOM:
                params.gravity = Gravity.LEFT | Gravity.BOTTOM;
                break;
            case IndicatorPosition.RIGHT_BOTTOM:
                params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
                break;
            default:
                break;
        }
        mIndicatorContainer.setLayoutParams(params);
        mIndicatorContainer.setPadding(50, 30, 50, 30);
        mIndicatorContainer.removeAllViews();

        for (int i = 0; i < bannerNum; i++) {
            PointIndicator indicator;
            if (i == 0) {
                indicator = new PointIndicator(mContext, mRadius, mSelectedColor);
                indicator.setLayoutParams(new LinearLayout.LayoutParams(mRadius * 3, mRadius * 2));
            } else {
                indicator = new PointIndicator(mContext, mRadius, mUnSelectedColor);
                indicator.setLayoutParams(new LinearLayout.LayoutParams(mRadius * 3, mRadius * 2));
            }
            mIndicatorContainer.addView(indicator);
            mIndicators.add(indicator);
        }

        return mIndicatorContainer;
    }

    /**
     * 设置动画持续时间，即Banner滑动的快慢
     *
     * @param millisecond
     */
    public void setAnimDuration(int millisecond) {
        mDuration = millisecond;
    }

    /**
     * 设置Banner条幅的数量，需要与实际提供的图片url数量一致
     *
     * @param num
     */
    public void setBannerNum(int num) {
        bannerNum = num;
    }

    /**
     * 设置指示器小圆点的显示位置<br>
     * 有3种选择：IndicatorPosition.CENTER_BOTTOM、IndicatorPosition.RIGHT_BOTTOM、IndicatorPosition.LEFT_BOTTOM<br>
     * 默认为RIGHT_BOTTOM
     *
     * @param position 位置常量
     */
    public void setIndicatorPosition(int position) {
        mIndicatorPosition = position;
    }

    /**
     * 设置指示器小圆点的半径
     *
     * @param radius
     */
    public void setIndicatorRaidus(int radius) {
        mRadius = radius;
    }

    /**
     * 设置小圆点处于当前位置的颜色
     *
     * @param selectedColor
     */
    public void setSelectedColor(int selectedColor) {
        mSelectedColor = selectedColor;
    }

    /**
     * 设置小圆点非当前位置的颜色
     *
     * @param unSelectedColor
     */
    public void setUnSelectedColor(int unSelectedColor) {
        mUnSelectedColor = unSelectedColor;
    }

    /**
     * 设置Banner的大小，并按比例适配图片，需在initView之后，下载图片之前调用，否则无效
     *
     * @param ratio       实际图片的宽与高之比
     * @param bannerWidth 最终需要显示的Banner宽度
     */
    public void setBannerSize(double ratio, int bannerWidth) {
        if (mImageViews == null || mImageViews.size() <= 0) {
            return;
        }

        if (ratio > 0 && bannerWidth > 0) {
            int bannerHeight = (int) (bannerWidth / ratio);
            for (ImageView imageView : mImageViews) {
                imageView.getLayoutParams().width = bannerWidth;
                imageView.getLayoutParams().height = bannerHeight;
            }
            getLayoutParams().width = bannerWidth;
        }
    }

    /**
     * 设置Banner的大小,并按比例适配图片，宽度默认为屏幕宽度，需在initView之后，下载图片之前调用，否则无效
     *
     * @param ratio 实际图片的宽与高之比
     */
    public void setBannerSize(double ratio) {
        int defaultBannerWidth = getScreenWidth();
        setBannerSize(ratio, defaultBannerWidth);
    }

    /**
     * 设置点击监听接口
     *
     * @param listener
     */
    public void setOnBannerClickListener(OnBannerClickListener listener) {
        this.mListener = listener;
    }

    /**
     * 提供获取ImageView的接口，供图片库下载图片使用
     *
     * @return
     */

    public List<ImageView> getBannerImageViews() {
        if (mImageViews != null) {
            return mImageViews;
        }

        return null;
    }

    /**
     * 获取设备屏幕宽度
     *
     * @return
     */
    private int getScreenWidth() {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(metrics);

        return metrics.widthPixels;
    }

    /**
     * 处理左滑
     * <p/>
     * 需要单独对每个ImageView做动画，如果对ImageView的容器做动画，则循环时会有突兀的感觉
     */
    private void doLeftAnimation() {
        if (mCurrentBannerIndex == bannerNum - 1) {
            mCurrentBannerIndex = 0;
        } else {
            mCurrentBannerIndex += 1;
        }

        for (int i = 0; i < bannerNum; i++) {
            doLeftAnimation(mImageViews.get(i), i);
        }
    }

    /**
     * 左滑动画，左滑之后，将滑到左边看不到的广告位移动到右边末尾
     * Calls requires API level 12
     *
     * @param v
     */
    private void doLeftAnimation(final View v, final int index) {
        v.animate().x(v.getX() - v.getWidth()).setDuration(mDuration).setInterpolator(new LinearInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                isAnimationing = true;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                isAnimationing = false;
                if (index == bannerNum - 1) {
                    /**
                     * 修改指示器小圆点状态
                     */
                    mIndicators.get(mCurrentBannerIndex).changeStatus(true);
                    if (mCurrentBannerIndex != 0) {
                        mIndicators.get(mCurrentBannerIndex - 1).changeStatus(false);
                    } else {
                        mIndicators.get(bannerNum - 1).changeStatus(false);
                    }
                }

                /**
                 * 将滑到屏幕左侧的视图移到右侧尾部
                 */
                if (v.getX() < 0) {
                    v.setX((bannerNum - 1) * v.getWidth());
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    /**
     * 处理右滑
     * <p/>
     * 由于没有采用常规的设置一个很大数，然后取余实现循环的原理；
     * 本例默认N个Banner广告均在手机屏幕可见区域及右侧（屏幕可见区域仅显示当前广告条）
     * 因此向右滑动之前，需要预先将最右侧的广告条移到手机屏幕左侧不可见区域
     */
    private void doRightAnimation() {
        if (mCurrentBannerIndex == 0) {
            mCurrentBannerIndex = bannerNum - 1;
        } else {
            mCurrentBannerIndex -= 1;
        }

        /**
         * 将屏幕右侧尾部的视图移到左侧
         */
        for (int i = 0; i < bannerNum; i++) {
            if (mImageViews.get(i).getX() == mImageViews.get(i).getWidth() * (bannerNum - 1)) {
                mImageViews.get(i).setX(-mImageViews.get(i).getWidth());
                break;
            }
        }

        for (int i = 0; i < bannerNum; i++) {
            doRightAnimation(mImageViews.get(i), i);
        }
    }

    /**
     * 右滑动画
     * Calls requires API level 12
     */
    private void doRightAnimation(final View v, final int index) {
        v.animate().x(v.getX() + v.getWidth()).setDuration(mDuration).setInterpolator(new LinearInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                isAnimationing = true;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                isAnimationing = false;
                if (index == bannerNum - 1) {
                    /**
                     * 修改指示器小圆点状态
                     */
                    mIndicators.get(mCurrentBannerIndex).changeStatus(true);
                    if (mCurrentBannerIndex != bannerNum - 1) {
                        mIndicators.get(mCurrentBannerIndex + 1).changeStatus(false);
                    } else {
                        mIndicators.get(0).changeStatus(false);
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    /**
     * 自动播放广告条，播放速度可由动画时间控制
     * 目前自动播放效果不好，且可能与点击造成混乱，暂时关闭，待后续优化
     */
    public void startAutoPlay() {
        if (isAutoSwitch && !isOnlyOne) {
            for (int i = 0; i < bannerNum; i++) {
                doAutoAnimation(mImageViews.get(i));
            }
        }
    }

    /**
     * 自动播放循环动画
     * Calls requires API level 12
     *
     * @param v
     */
    public void doAutoAnimation(final View v) {
        v.animate().x(v.getX() - v.getWidth()).setDuration(mDuration * 3).setInterpolator(new LinearInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (v.getX() < 0) {
                    v.setX((bannerNum - 1) * v.getWidth());
                }

                doAutoAnimation(v);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    /**
     * Indicator位置常量
     */
    public final class IndicatorPosition {
        public static final int RIGHT_BOTTOM = 0;
        public static final int LEFT_BOTTOM = 1;
        public static final int CENTER_BOTTOM = 2;
    }

    /**
     * Banner点击监听接口
     */
    public interface OnBannerClickListener {
        void onClick(int bannerIndex);
    }

    /**
     * Indicator指示器实现类
     * <p/>
     * 目前方案：先绘制小圆点，再根据切换改变颜色，需要重绘
     * 其他方案：
     * 1.使用属性动画移动圆点，会造成视觉移动，体验不好
     * 2.每个位置绘制一个灰色和白色圆点，叠在一起，根据切换显示与隐藏
     * 3.第一个位置绘制白色和灰色圆点，叠加放置，其他都是灰色圆点，对第一个白色圆点做属性动画
     */
    public class PointIndicator extends View {
        private Paint mPaint;
        private int mRadius;
        private int mColor;

        public PointIndicator(Context context, int radius, int color) {
            super(context);
            this.mRadius = radius;
            this.mColor = color;
            initPaint();
        }

        public void initPaint() {
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mColor);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawCircle(mRadius + mRadius / 2, mRadius, mRadius, mPaint);
        }

        /**
         * 更改小圆点颜色
         *
         * @param isCurrent 是否当前需要高亮的小圆点
         */
        public void changeStatus(boolean isCurrent) {
            if (isCurrent) {
                mColor = mSelectedColor;
                initPaint();
                invalidate();
            } else {
                mColor = mUnSelectedColor;
                initPaint();
                invalidate();
            }
        }
    }

}
