package com.chat.ui.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.jivesoftware.smack.util.StringUtils;

import com.chat.IM;
import com.chat.R;
import com.chat.db.provider.ContactProvider;
import com.chat.service.aidl.Contact;
import com.chat.utils.pinyin.PinyinContactComparator;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

/**
 * 获取好友列表
 * @author Administrator
 *
 */
public class ContactAdapter extends CursorAdapter{
	//获取联系人
	private Cursor cursor;
	//记录是否显示
	private boolean isShowChecked;
	//记录是否选中
	private HashMap<Integer, Boolean> isSelected;
	
	public ContactAdapter(Cursor cursor) {
		super(IM.im,cursor,FLAG_REGISTER_CONTENT_OBSERVER);
		//默认隐藏
		this.cursor = cursor;
		isShowChecked = false;
		isSelected = new HashMap<Integer, Boolean>(); 
		// 初始化数据
		initDate();
	}

	// 初始化isSelected的数据
	private void initDate() {
		if(cursor == null){
			return;
		}
		for (int i = 0; i < cursor.getCount(); i++) {
			getIsSelected().put(i, false);
		}
	}
	
	//根据分类列的索引号获得该序列的首个位置
	public int getPositionForSection(int section) {
		int index = 0;
		for(int i=0;i<cursor.getCount();i++){
			cursor.moveToPosition(i);
			String sort = cursor.getString(cursor.getColumnIndex(ContactProvider.ContactColumns.SORT));
			int firstCharacter = sort.charAt(0);
			if(firstCharacter == section){
				return index;
			}
			index ++;
		}
		return -1;
	}
	
	@Override
	public View getView(int pos, View view, ViewGroup parent) {
		ContactHolder contactHolder = null;

		if(view == null){
			contactHolder = new ContactHolder();
			view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tt_item_contact, parent, false);

			contactHolder.name = (TextView) view.findViewById(R.id.tt_fragment_contact_name);
			contactHolder.sort = (TextView) view.findViewById(R.id.tt_fragment_contact_sort);
			contactHolder.avatar = (ImageView)view.findViewById(R.id.tt_fragment_contact_portrait);
			contactHolder.divider = view.findViewById(R.id.tt_fragment_contact_divider);
			contactHolder.cbMeet = (CheckBox)view.findViewById(R.id.tt_fragment_contact_checkBox);
			contactHolder.cbMeet.setTag(pos);
			view.setTag(contactHolder);
		}else{
			contactHolder = (ContactHolder)view.getTag();
		}

		Cursor c = (Cursor)getItem(pos);
		//账户
		String account = cursor.getString(cursor.getColumnIndex(ContactProvider.ContactColumns.ACCOUNT));
		//拼音首字母
		String sort = c.getString(c.getColumnIndex(ContactProvider.ContactColumns.SORT));
		//备注
		String name = c.getString(c.getColumnIndex(ContactProvider.ContactColumns.NAME));
		//登录状态
		String status = c.getString(c.getColumnIndex(ContactProvider.ContactColumns.STATUS));

		//根据position获取分类的首字母的char ascii值  
		int section = sort.charAt(0);  
		
		//如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现  
		if(pos == getPositionForSection(section)){ 
			contactHolder.sort.setVisibility(View.VISIBLE);  
			contactHolder.sort.setText(sort);  
		}else{  
			contactHolder.sort.setVisibility(View.GONE);  
		}  
		
		//设置名称
		contactHolder.name.setText(name);
		
		//根据状态来对头像是否灰处理
		if(status == null || status.equals("离线")){
			Log.e("ContactAdapter:",status+" 处理 " + IM.getAvatar(account));
			Bitmap bitmap = IM.drawableToBitmap(IM.getAvatar(StringUtils.parseBareAddress(account)));
			contactHolder.avatar.setImageBitmap(IM.grey(bitmap));
		}else{
			contactHolder.avatar.setImageDrawable(IM.getAvatar(account));  
		}
		
		//设置CheckBox是否显示
		if(isShowCheck()){
			contactHolder.cbMeet.setVisibility(View.VISIBLE);
			contactHolder.cbMeet.setChecked(getIsSelected().get(pos));
		}else{
			contactHolder.cbMeet.setVisibility(View.GONE);
			contactHolder.cbMeet.setChecked(false);
		}
		
		return view;
	}

	@Override
	public void bindView(View arg0, Context arg1, Cursor arg2) {
	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		return null;
	}

	public static class ContactHolder{
		View divider;
		TextView sort;
		TextView account;
		TextView name;
		ImageView avatar;
		public CheckBox cbMeet;
	}

	//设置是否显示checkbox
	public void setShowCheck(boolean isShowChecked){
		Log.e("contactAdapter","进入显示checkbox");
		this.isShowChecked = isShowChecked;
		notifyDataSetChanged();
	}

	//获取是否显示checkbox的值 
	public boolean isShowCheck(){
		return this.isShowChecked;
	}

	//获取checkbox的值
	public HashMap<Integer, Boolean> getIsSelected() {
		return isSelected;
	}
	
	//设置checkbox的值
	public void setIsSelected(HashMap<Integer, Boolean> isSelected) {
		this.isSelected = isSelected;
	}

}
