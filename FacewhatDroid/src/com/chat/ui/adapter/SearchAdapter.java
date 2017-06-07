package com.chat.ui.adapter;

import java.util.List;
import java.util.Map;

import com.chat.IM;
import com.chat.R;
import com.chat.service.aidl.Contact;
import com.chat.ui.widget.ImageViewCircle;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SearchAdapter  extends BaseAdapter{
	private Context context;
	
	//显示的数据 
	private List<Contact> myData;
	
	//构造函数一
	public SearchAdapter(Context context){
		this.context = context;
	}
	
	public void putData(List<Contact> myData){
		this.myData = myData;
		notifyDataSetChanged();
	}
	
	public int getCount() {
		return myData.size();
	}
	
	public Object getItem(int pos) {
		return myData.get(pos);
	}
	
	public long getItemId(int pos) {
		return 0;
	}

	@Override
	public View getView(int pos, View currentView, ViewGroup arg2) {
		HolderView holder = null;
		if(currentView == null){
			holder = new HolderView();
			currentView = LayoutInflater.from(context).inflate(R.layout.tt_item_search, null);
			holder.icon = (ImageViewCircle)currentView.findViewById(R.id.search_item_icon);
			holder.tvName = (TextView)currentView.findViewById(R.id.search_item_name);
			holder.tvAccount = (TextView)currentView.findViewById(R.id.search_item_account);
			
			currentView.setTag(holder);
		}else{
			holder = (HolderView)currentView.getTag();
		}
		Contact contact = (Contact)getItem(pos);
		if(!contact.getAccount().isEmpty()){
			holder.icon.setImageDrawable(IM.getAvatar(contact.getAccount()));
			holder.tvAccount.setText(contact.getAccount());
		}
		if(!contact.getName().isEmpty()){
			holder.tvName.setText(contact.getName());
		}
		
		return currentView;
	}

	class HolderView{
		ImageViewCircle icon;
		TextView tvName;
		TextView tvAccount;
	}
	
	public void clearList(){
		if(this.myData != null){
			if(this.myData.size() > 0){
				this.myData.removeAll(myData);
				notifyDataSetChanged();
			}
		}
	}
}
