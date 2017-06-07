package com.chat.utils;

import com.chat.IM;
import com.chat.service.aidl.Contact;
import com.chat.ui.ChatActivity;
import com.chat.ui.MySetActivity;
import com.chat.ui.SearchActivity;
import com.chat.ui.SettingActivity;
import com.chat.ui.UserInfoActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.ContextThemeWrapper;

public class IMUIHelper {
	private static IMUIHelper imUIHelper;
	
	private IMUIHelper(){}
	
	public static synchronized IMUIHelper instance(){
		if(imUIHelper == null)
			imUIHelper = new IMUIHelper();
		return imUIHelper;
	}

	//长按弹出对话框
	public void handleContactItemLongClick(Context ctx,final Contact contact){
		if(contact == null || ctx == null){
			return;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(
				new ContextThemeWrapper(ctx,android.R.style.Theme_Holo_Light_Dialog));
		builder.setTitle("信息提示");
		String[] items = new String[]{"设置备注及标签","删除好友"};
		builder.setItems(items,new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int which) {
				switch(which){
					case 0:
						
						break;
					case 1:
						
						break;
				}
			}
		});
		AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
	}
	
	//跳转到用户信息页面
    public void openUserInfoActivity(Context ctx, String jid) {
        Intent intent = new Intent();
        intent.setClass(ctx, UserInfoActivity.class);
        intent.putExtra(IM.KEY_CONTACT_JID, jid);
        ctx.startActivity(intent);
    }
    
    // 跳转到聊天页面
    public void openChatActivity(Context ctx, String jid) {
    	Intent intent = new Intent();
        intent.setClass(ctx, ChatActivity.class);
        intent.putExtra(IM.KEY_CONTACT_JID, jid);
        ctx.startActivity(intent);
    }
    
    //转到搜索界面
    public void openSearchActivit(Context ctx){
    	Intent intent = new Intent();
        intent.setClass(ctx, SearchActivity.class);
    	ctx.startActivity(intent);
    }
    
    //跑到消息提醒界面
    public void openSettingActivity(Context ctx){
    	Intent intent = new Intent();
        intent.setClass(ctx, SettingActivity.class);
    	ctx.startActivity(intent);
    }
    
  //前往个人信息
    public void openMyInfoSetActivity(Context ctx){
    	Intent intent = new Intent();
        intent.setClass(ctx, MySetActivity.class);
    	ctx.startActivity(intent);
    }
    
}
