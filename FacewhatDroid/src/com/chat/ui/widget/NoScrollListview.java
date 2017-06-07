package com.chat.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class NoScrollListview extends ListView{  
	
    public NoScrollListview(Context context, AttributeSet attrs) {  
            super(context, attrs);  
    }  
      
    /** 
     * ÉèÖÃ²»¹ö¶¯ 
     */  
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)  
    {  
            int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,  
                            MeasureSpec.AT_MOST);  
            super.onMeasure(widthMeasureSpec, expandSpec);  

    }  

}  