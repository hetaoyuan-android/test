package com.example.test.chart;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;

import com.example.test.R;

import java.util.ArrayList;
import java.util.List;

import static android.os.Build.VERSION_CODES.R;


public class PieChart extends View {
    private boolean isShowCenterCircle;
    private boolean isShowCenterText;
    private boolean isShowPercentage;
    private boolean isOpenCheckStatus;
    @ColorInt
    @ColorRes
    private int mCenterTextColor;
    @ColorInt
    @ColorRes
    private int mCenterBgColor;
    private int mCenterTextSize;
    private int mCenterRadius;
    private String mCenterText;
    private int mOuterRadius;
    private int mOuterAddRadius;
    private int mPercentageLong;
    private int mPercentageWidth;
    private int mPercentageTvSize;
    private Paint mCenterPaint;
    private Paint mCenterTvPaint;
    private float mCenterX;
    private float mCenterY;
    private List<PieChartBean> mDatas;
    private List<Region> mRegions;
    private float sum;
    private float percent = 0f;
    private boolean isOpenAnimation;


    public PieChart(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PieChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mDatas = new ArrayList<>();
        mRegions = new ArrayList<>();
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, com.example.test.R.styleable.PieChart, defStyleAttr, 0);
        isShowCenterCircle = typedArray.getBoolean(com.example.test.R.styleable.PieChart_isShowCenterCircle, true);
        isShowCenterText = typedArray.getBoolean(com.example.test.R.styleable.PieChart_isShowCenterText, true);
        isShowPercentage = typedArray.getBoolean(com.example.test.R.styleable.PieChart_isShowPercentage, true);
        isOpenCheckStatus = typedArray.getBoolean(com.example.test.R.styleable.PieChart_isOpenCheckStatus, true);
        mCenterTextColor = typedArray.getResourceId(com.example.test.R.styleable.PieChart_CenterTextColor, Color.BLACK);
        mCenterText = typedArray.getString(com.example.test.R.styleable.PieChart_CenterText);
        mCenterBgColor = typedArray.getResourceId(com.example.test.R.styleable.PieChart_CenterBgColor, Color.WHITE);
        mCenterTextSize = sp2px(context, typedArray.getInt(com.example.test.R.styleable.PieChart_CenterTextSize, 15));
        mCenterRadius = dip2px(context, typedArray.getInt(com.example.test.R.styleable.PieChart_CenterRadius, 30));
        mOuterRadius = dip2px(context, typedArray.getInt(com.example.test.R.styleable.PieChart_OuterRadius, 60));
        mOuterAddRadius = dip2px(context, typedArray.getInt(com.example.test.R.styleable.PieChart_OuterAddRadius, 10));
        mPercentageLong = dip2px(context, typedArray.getInt(com.example.test.R.styleable.PieChart_PercentageLong, 10));
        mPercentageWidth = dip2px(context, typedArray.getInt(com.example.test.R.styleable.PieChart_PercentageWidth, 1));
        mPercentageTvSize = sp2px(context, typedArray.getInt(com.example.test.R.styleable.PieChart_PercentageTvSize, 12));
        typedArray.recycle();
        mCenterPaint = new Paint();
        mCenterPaint.setAntiAlias(true);
        mCenterPaint.setColor(mCenterBgColor);
        mCenterTvPaint = new Paint();
        mCenterTvPaint.setAntiAlias(true);
        mCenterTvPaint.setTextSize(mCenterTextSize);
        mCenterTvPaint.setColor(mCenterTextColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (0 == mDatas.size()) return;
        drawArcPath(canvas);
        if (isShowCenterCircle) {
            drawCenterCircle(canvas);
        }
    }

    /**
     * ?????? ?????? ?????? ?????????
     *
     * @param canvas
     */
    private void drawArcPath(Canvas canvas) {
        mRegions.clear();
        float mStartAngle = 0f;
        for (int i = 0; i < mDatas.size(); i++) {
            float mOuterRadius = this.mOuterRadius;
            mOuterRadius = isOpenCheckStatus && mDatas.get(i).isCheck() ? mOuterRadius + mOuterAddRadius : mOuterRadius;
            RectF mRect = new RectF(mCenterX - mOuterRadius, mCenterY - mOuterRadius, mCenterX + mOuterRadius, mCenterY + mOuterRadius);
            Paint mPaint = new Paint();
            Path mPath = new Path();
            //???????????????
            float mSweepAngle = 0f;
            mPaint.setAntiAlias(true);
            mPaint.setColor(mDatas.get(i).getColor());
            //??????????????????
            mPath.moveTo(mCenterX, mCenterY);
            //????????????????????????
            mSweepAngle = (mDatas.get(i).getVaule() / sum) * (360f - mDatas.size());
            if (isOpenAnimation) {
                mSweepAngle = mSweepAngle * percent;
            }
            //??????????????????
            mPath.arcTo(mRect, mStartAngle, mSweepAngle);
            RectF rectF = new RectF();
            //????????????
            mPath.computeBounds(rectF, true);
            //????????????
            mPath.close();
            //??????????????????
            Region mRegion = new Region();
            mRegion.set((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom);
            mRegion.setPath(mPath, mRegion);
            mRegions.add(mRegion);
            if (isShowPercentage) {
                //?????????x,y??????  Math.toRadians??????
                float startX = (float) ((mOuterRadius) * Math.cos(Math.toRadians(mStartAngle + mSweepAngle / 2)));
                float startY = (float) ((mOuterRadius) * Math.sin(Math.toRadians(mStartAngle + mSweepAngle / 2)));
                float stopX = (float) ((mOuterRadius + mPercentageLong) * Math.cos(Math.toRadians(mStartAngle + mSweepAngle / 2)));
                float stopY = (float) ((mOuterRadius + mPercentageLong) * Math.sin(Math.toRadians(mStartAngle + mSweepAngle / 2)));
                //???????????????????????????
                mPaint.setStrokeWidth(mPercentageWidth);
                canvas.drawLine(mCenterX + startX, mCenterY + startY, mCenterX + stopX, mCenterY + stopY, mPaint);
                try {
                    //??????????????????????????????
                    String mPercentageTv = BigDecimalUtil.mul(BigDecimalUtil.round(mDatas.get(i).getVaule() / sum, 4), 100) + "%";
                    mPaint.setTextSize(mPercentageTvSize);
                    if (stopX > 0) {
                        //??? 1   2??????
                        canvas.drawLine(mCenterX + stopX, mCenterY + stopY, mCenterX + stopX + mPercentageLong, mCenterY + stopY, mPaint);
                        canvas.drawText(mPercentageTv, mCenterX + stopX + mPercentageLong, mCenterY + stopY, mPaint);
                    } else {
                        //???  3  4??????
                        canvas.drawLine(mCenterX + stopX, mCenterY + stopY, mCenterX + stopX - mPercentageLong, mCenterY + stopY, mPaint);
                        canvas.drawText(mPercentageTv, mCenterX + stopX - mPercentageLong - mPaint.measureText(mPercentageTv), mCenterY + stopY, mPaint);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            //????????????????????????
            mStartAngle += mSweepAngle + 1f;
            //????????????
            canvas.drawPath(mPath, mPaint);
        }

    }

    private TimeInterpolator pointInterpolator = new DecelerateInterpolator();

    public void startAnimation(int duration) {
        isOpenAnimation = true;
        ValueAnimator mAnimator = ValueAnimator.ofFloat(0, 1);
        mAnimator.setDuration(duration);
        mAnimator.setInterpolator(pointInterpolator);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                percent = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        mAnimator.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getDefaultSize(50, widthMeasureSpec), getDefaultSize(50, heightMeasureSpec));
    }

    /**
     * ?????? ????????? ????????????
     *
     * @param canvas
     */
    private void drawCenterCircle(Canvas canvas) {
        canvas.drawCircle(mCenterX, mCenterY, mCenterRadius, mCenterPaint);
        if (isShowCenterText) {
            //??????????????????
            float tvWidth = mCenterTvPaint.measureText(mCenterText);
            //??????????????????
            float tvHeight = mCenterTvPaint.getFontMetrics().descent - mCenterTvPaint.getFontMetrics().ascent;//????????????????????????
            //????????????
            canvas.drawText(mCenterText, mCenterX - tvWidth / 2, mCenterY + tvHeight / 2, mCenterTvPaint);
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);
        mCenterX = width / 2;
        mCenterY = height / 2;
    }

    //dp???px
    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    //sp???px
    private static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public void setData(List<PieChartBean> data) {
        mDatas.clear();
        mDatas.addAll(data);
        sum = 0f;
        for (int i = 0; i < data.size(); i++) {
            sum += data.get(i).getVaule();
        }
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && isOpenCheckStatus) {
            float x = event.getX();
            float y = event.getY();
            for (int i = 0; i < mRegions.size(); i++) {
                if (mRegions.get(i).contains((int) x, (int) y)) {
                    mDatas.get(i).setCheck(!mDatas.get(i).isCheck());
                    invalidate();
                } else {
                    mDatas.get(i).setCheck(false);
                }
            }
        }
        return super.onTouchEvent(event);
    }
}
