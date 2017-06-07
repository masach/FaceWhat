package com.chat.ui.widget;

import com.chat.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;

public class ImageViewCircle extends ImageView
{
    /**
     * 图片的类型，圆形or圆角
     */
    private int type;
    private static final int TYPE_CIRCLE=0;
    private static final int TYPE_ROUND = 1;
    /**
     * 圆角大小的默认值
     */
    private static final int BODER_RADIUS_DEFAULT = 10;
    /**
     * 圆角的大小
     */
    private int mBorderRadius;
    /**
     * 绘图的paint
     */
    private Paint mBitmapPaint;
    /**
     * 圆角 的半径
     */
    private int mRadius;
    /**
     * 3*3矩阵，用于缩放大小
     */
    private Matrix mMatrix;
    /**
     * 渲染图像，使用图像为绘制图形着色
     */
    private BitmapShader mBitmapShader;
    /**
     * view 的宽度
     */
    private int mWidth;
    private RectF mRoundRect;

    public ImageViewCircle(Context context,AttributeSet attrs)
    {
        super(context, attrs);
        mMatrix = new Matrix();
        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(true);
        //进行配置
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundImageView);

        mBorderRadius = a.getDimensionPixelSize(
                R.styleable.RoundImageView_borderRadius,(int)TypedValue.applyDimension(TypedValue
                                .COMPLEX_UNIT_DIP,BODER_RADIUS_DEFAULT,getResources().getDisplayMetrics()));
        type = a.getInt(R.styleable.RoundImageView_type, TYPE_CIRCLE);//默认为circle
        a.recycle();//为了保证以后使该属性一致
    }
	
    /**
     * 在onMeasure中计算childView的测量值以及模式，以及设置自己的宽和高：
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        /**
         * 如果类型是圆形，则强制改变view的高宽一致
         */
        if(type == TYPE_CIRCLE)
        {
            mWidth = Math.min(getMeasuredWidth(),getMeasuredHeight());
            mRadius = mWidth/2;
            setMeasuredDimension(mWidth,mWidth);
        }
    }
    /**
     * 初始化BitmapShader
     */
    private void setUpShader()
    {
        Drawable drawable = getDrawable();
        if(drawable == null){return;}

        Bitmap bmp = drawableToBitamp(drawable);
        /** 将bmp作为着色器，就是在指定区域内绘制bmp
         * BitmapShader(Bitmap bitmap,Shader.TileMode tileX,Shader.TileMode tileY)
         * tileX 在位图上X方向花砖模式  tileY 在位图上Y方向花砖模式
         * TileMode:CLAMP 如果渲染器超出原始边界范围，会复制范围内边缘染色。
         *          REPEAT ：横向和纵向的重复渲染器图片，平铺。
         *          MIRROR ：横向和纵向的重复渲染器图片，这个和REPEAT 重复方式不一样，他是以镜像方式平铺。
         */
        mBitmapShader = new BitmapShader(bmp, Shader.TileMode.CLAMP,Shader.TileMode.CLAMP);
        float scale = 1.0f;
        if(type == TYPE_CIRCLE)
        {
            // 拿到bitmap宽或高的小值
            int bSize = Math.min(bmp.getWidth(),bmp.getHeight());
            scale = mWidth*1.0f/bSize;
        }else if(type==TYPE_ROUND)
        {
            /**
             *如果图片的宽或者高与view的宽高不匹配，计算出需要缩放的比例；
             * 缩放后的图片的宽高，一定要大于我们view的宽高；所以我们这里取大值；
              */
           scale = Math.max(getWidth()*1.0f/bmp.getWidth(),getHeight())*0.1f/bmp.getHeight();
        }
        /**
         * shader 的变换矩阵，我们这里用于放大或缩小
         */
        mMatrix.setScale(scale,scale);
        //设置变换矩阵
        mBitmapShader.setLocalMatrix(mMatrix);
        //设置shader
        mBitmapPaint.setShader(mBitmapShader);
    }
    /**
     * 将drawable转bitmap
     */
    private Bitmap drawableToBitamp(Drawable drawable)
    {
        if(drawable instanceof BitmapDrawable)
        {
            BitmapDrawable bd = (BitmapDrawable)drawable;
            return bd.getBitmap();
        }
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        //Canvas画布
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        if (getDrawable() == null)
        {
            return;
        }
        setUpShader();

        if (type == TYPE_ROUND)
        {
            canvas.drawRoundRect(mRoundRect, mBorderRadius, mBorderRadius,
                    mBitmapPaint);
        } else
        {
            canvas.drawCircle(mRadius, mRadius, mRadius, mBitmapPaint);
            // drawSomeThing(canvas);
        }
    }
    @Override
    protected void onSizeChanged(int w,int h,int oldw,int oldh)
    {
        super.onSizeChanged(w,h,oldw,oldh);
        //圆角图片的范围
        if(type == TYPE_ROUND)
        {
            mRoundRect = new RectF(0,0,getWidth(),getHeight());
        }
    }
}
