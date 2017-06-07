package com.chat.utils.pinyin;

import java.util.Comparator;  

import com.chat.service.aidl.Contact;

/**  
 *   
 * @author xiaanming  
 *  
 */ 
public class PinyinContactComparator implements Comparator<Contact> {  
 
    public int compare(Contact o1, Contact o2) {  
        //这里主要是用来对ListView里面的数据根据ABCDEFG...来排序  
        if (o2.getSort().equals("#")) {  
            return -1;  
        } else if (o1.getSort().equals("#")) {  
            return 1;  
        } else {  
            return o1.getSort().compareTo(o2.getSort());  
        }  
    }  
} 
