package com.chat.ui.widget;

import com.chat.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;

/**
 * 源于http://blog.csdn.net/xiaanming/article/details/11066685
 * @author Administrator
 *
 */
public class SearchEditText extends EditText implements OnFocusChangeListener,TextWatcher{
//	 删除按钮的引用
	private Drawable clearWordsImage;
//	控件是否有焦点 
	private boolean hasFoucs;

	public SearchEditText(Context context) {
		this(context,null);
	}

	//	使用AttributeSet来完成控件类的构造函数,并在构造函数中将自定义控件类中变量与attrs.xml中的属性连接起来.
	public SearchEditText(Context context,AttributeSet attrs){
		//这里构造方法也很重要，不加这个很多属性不能再XML里面定义  
		this(context,attrs,android.R.attr.editTextStyle);
	}

	public SearchEditText(Context context,AttributeSet attrs,int defStyle){
		super(context,attrs,defStyle);
	}

	private void init(){
		//extView.setCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom)
		//这个看你设定的是在哪个方向了，就把drawable放在哪个对应的位置。
		//如果放在文本框的右面，这样就可以了 getCompoundDrawables()[2]
		//如果放在其他的位置按着这样写就可以了
		
		 //获取EditText的DrawableRight,假如没有设置我们就使用默认的图片 
		clearWordsImage = getCompoundDrawables()[2];
		if(clearWordsImage == null){
			clearWordsImage = getResources().getDrawable(R.drawable.tt_delete_bar);
		}
		clearWordsImage.setBounds(0, 0, clearWordsImage.getIntrinsicWidth(), clearWordsImage.getIntrinsicHeight());
		 //默认设置隐藏图标 
		setClearIconVisible(false);
		//设置焦点改变的监听  
		setOnFocusChangeListener(this);
		//设置输入框里面内容发生改变的监听  
	
	}
	
	/** 
     * 因为我们不能直接给EditText设置点击事件，所以我们用记住我们按下的位置来模拟点击事件 
     * 当我们按下的位置 在  EditText的宽度 - 图标到控件右边的间距 - 图标的宽度  和 
     * EditText的宽度 - 图标到控件右边的间距之间我们就算点击了图标，竖直方向就没有考虑 
     */  
	@Override 
	public boolean onTouchEvent(MotionEvent event) { 
		if(event.getAction() == MotionEvent.ACTION_UP){
			if(getCompoundDrawables()[2] != null){
				boolean touchable = event.getX() > (getWidth()-getTotalPaddingRight())
						&& (event.getX() < ((getWidth()-getPaddingRight())));
				if(touchable){
					this.setText("");
				}
			}
		}
		return super.onTouchEvent(event);
	}

	/** 
     * 当ClearEditText焦点发生变化的时候，判断里面字符串长度设置清除图标的显示与隐藏 
     */  
	@Override 
	public void onFocusChange(View v, boolean hasFocus) { 
		this.hasFoucs = hasFocus;
		if(hasFocus){
			setClearIconVisible(getText().length()>0);
		}else{
			setClearIconVisible(false);
		}
	}

	/** 
     * 当输入框里面内容发生变化的时候回调的方法 
     */  
	@Override 
	public void onTextChanged(CharSequence s, int start, int count, 
			int after) { 
		if (hasFoucs) {
			setClearIconVisible(s.length() > 0);
		}
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {

	}

	@Override
	public void afterTextChanged(Editable arg0) {

	}

	 /**
     * 设置清除图标的显示与隐藏，调用setCompoundDrawables为EditText绘制上去
     * @param visible
     */
	protected void setClearIconVisible(boolean visible) {
		Drawable right = visible?clearWordsImage:null;
		setCompoundDrawables(getCompoundDrawables()[0],
				getCompoundDrawables()[1],
				right,
				getCompoundDrawables()[3]);
	}
	
	 /**
     * 设置晃动动画
     */
	public void setShakeAnimation(){
		this.setAnimation(shakeAnimation(5));
	}
	
	/**
     * 晃动动画
     * @param counts 1秒钟晃动多少下
     * @return
     */
	public static Animation shakeAnimation(int counts){
		Animation translateAnimation = new TranslateAnimation(0, 10,0,0);
		translateAnimation.setInterpolator(new CycleInterpolator(counts));
		translateAnimation.setDuration(1000);
		return translateAnimation;
	}
}
