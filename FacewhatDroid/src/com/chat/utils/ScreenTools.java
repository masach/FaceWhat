package com.chat.utils;

import java.lang.reflect.Field;

import android.content.Context;

//获取屏幕,分辨率相关
public class ScreenTools {
	private Context mCtx;
	private static ScreenTools mScreenTools;
	
	private ScreenTools(Context ctx){
		mCtx = ctx.getApplicationContext();
	}
	
	public static ScreenTools instance(Context ctx){
		if(null == mScreenTools){
			mScreenTools = new ScreenTools(ctx);
		}
		return mScreenTools;
	}
	
//	this.getResources().getDisplayMetrics().widthPixels*2/3 偏移量
//	this.getResources() 获取资源
//	this.getResources().getDisplayMetrics() 获取屏幕信息
//	this.getResources().getDisplayMetrics().widthPixels 获取屏幕宽度
//	this.getResources().getDisplayMetrics().widthPixels*2/3 偏移量是屏幕宽度的2/3
	public int getScreenWidth(){
		return mCtx.getResources().getDisplayMetrics().widthPixels;
	}
	
	public int dip2px(int dip){
		float density = getDensity(mCtx);
		return (int)(dip * density + 0.5);
	}
	
	public int px2dip(int px){
		float density = getDensity(mCtx);
		return (int)((px - 0.5) / density);
	}
	
	private  float getDensity(Context ctx){
		//后面的density就是屏幕的密度，类似分辨率,但与分辨率是两个不同的概念
//		density值表示每英寸有多少个显示点
		return ctx.getResources().getDisplayMetrics().density;
	}
	
	/**
	 * ５40 的分辨率上是85 （
	 * @param width　当前屏幕宽度
	 * @return
	 */
	public int getScal(){
		 return (int)(getScreenWidth() * 100 / 480);
	}
	
	 /**
	  * 宽全屏, 根据当前分辨率　动态获取高度 
	  * height 在８００*４８０情况下　的高度
	  * @return
	  */
	public int get480Height(int height480){
		 int width = getScreenWidth();
		 return (height480 * width / 480); 
	 }
	
	/**
	 * 获取状态栏高度
	 * @return
	 */
	public int getStatusBarHeight(){
		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0, sbar = 0;
		try {
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			sbar = mCtx.getResources().getDimensionPixelSize(x);
		} catch (Exception e1) {
			e1.printStackTrace();
		} 
		return sbar;
	}
	
	public int getScreenHeight(){
		return mCtx.getResources().getDisplayMetrics().heightPixels;
	}
}
