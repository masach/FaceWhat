package com.chat.utils;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.chat.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;


public class ExpressionUtil {
	//对spanableString进行正则判断，如果符合要求，则以表情图片代替
    public static void dealExpression(Context context,SpannableString spannableString, Pattern patten, int start) throws SecurityException, NoSuchFieldException, NumberFormatException, IllegalArgumentException, IllegalAccessException {
    	Matcher matcher = patten.matcher(spannableString);
        while (matcher.find()) {
            String key = matcher.group();
            if (matcher.start() < start) {
                continue;
            }
            //通过上面匹配得到的字符串来生成图片资源id
            Field field = R.drawable.class.getDeclaredField(key);
			int resId = Integer.parseInt(field.get(null).toString());		
            if (resId != 0) {
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);
                //通过图片资源id来得到bitmap，用一个ImageSpan来包装
                ImageSpan imageSpan = new ImageSpan(bitmap);
                //计算该图片名字的长度，也就是要替换的字符串的长度
                int end = matcher.start() + key.length();			
                //将该图片替换字符串中规定的位置中
                spannableString.setSpan(imageSpan, matcher.start(), end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);	
                //如果整个字符串还未验证完，则继续。。
                if (end < spannableString.length()) {						
                    dealExpression(context,spannableString,  patten, end);
                }
                break;
            }
        }
    }
    
    //得到一个SpanableString对象，通过传入的字符串,并进行正则判断
    public static SpannableString getExpressionString(Context context,String str,String zhengze){
    	SpannableString spannableString = new SpannableString(str);
    	//通过传入的正则表达式来生成一个pattern
        Pattern sinaPatten = Pattern.compile(zhengze, Pattern.CASE_INSENSITIVE);		
        try {
            dealExpression(context,spannableString, sinaPatten, 0);
        } catch (Exception e) {
            Log.e("dealExpression", e.getMessage());
        }
        return spannableString;
    }
	

}