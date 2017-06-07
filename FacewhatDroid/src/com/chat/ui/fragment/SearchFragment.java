package com.chat.ui.fragment;

import java.util.List;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chat.IM;
import com.chat.IMService;
import com.chat.R;
import com.chat.service.aidl.Contact;
import com.chat.service.aidl.IMXmppBinder;
import com.chat.ui.UserInfoActivity;
import com.chat.ui.adapter.SearchAdapter;
import com.chat.ui.base.TTBaseFragment;

public class SearchFragment extends TTBaseFragment 
	implements 
	OnClickListener,
	OnItemClickListener,
	TextWatcher{

	private View curView = null;
	private ListView listView;
	private TextView noSearchResultView;
	private SearchAdapter searchAdapter;
	private ProgressBar showBar;
	
	private final static int BEGIN_BAR = 99;
	private final static int BEGIN_SERACH = 100;
	private final static int END_BAR = 101;
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case BEGIN_BAR:
				showBar.setVisibility(View.VISIBLE);
				break;
			case BEGIN_SERACH:
				mHandler.sendEmptyMessage(BEGIN_BAR);
				try {
					List<Contact> list = binder.createConnection().searchUser(topSearchEdt.getText().toString());
					if(list.size()<=0){
						noSearchResultView.setText(R.string.no_search_result);
					}else{
						noSearchResultView.setText("搜出 "+ list.size() + " 记录");
						searchAdapter.putData(list);
						listView.setAdapter(searchAdapter);
					}
				} catch (RemoteException e) {
					e.printStackTrace();
					System.out.println("SearchFragmet " + e.getMessage().toString());
				}
				mHandler.sendEmptyMessage(END_BAR);
				break;
			case END_BAR:
				showBar.setVisibility(View.GONE);
				break;
			}
		}
	};
	
	private IMXmppBinder binder;
	private ServiceConnection serviceConnect = new XmppServiceConnect();
	// XMPP连接服务 
	private class XmppServiceConnect implements ServiceConnection {
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			binder = IMXmppBinder.Stub.asInterface(iBinder);
		}
		public void onServiceDisconnected(ComponentName componentName) {
			binder = null;
		}
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		System.out.println("SearchFragment:onCreateView");
		if (null != curView) {
			((ViewGroup) curView.getParent()).removeView(curView);
			return curView;
		}
		curView = inflater.inflate(R.layout.tt_fragment_search, topContentView);
		initParent();
		init();
		return curView;
	}

	@Override
	public void onStart() {
		super.onStart();
		getActivity().bindService(new Intent(getActivity(), IMService.class), serviceConnect, getActivity().BIND_AUTO_CREATE);
	}
	
	public void onDestroy(){
		super.onDestroy();
		getActivity().unbindService(serviceConnect);
	}
	
	private void initParent(){
		setTopBar(R.drawable.tt_top_default_bk);
		showTopSearchBar();
		setTopLeftButton(R.drawable.tt_top_back);
		setTopLeftText(getResources().getString(R.string.top_left_back));
		
		//返回上一级
		topLeftBtn.setPadding(0, 0, 0, 0);
		topLeftBtn.setOnClickListener(this);
		topLetTitleTxt.setOnClickListener(this);
		
		//清除搜索框
		setTopRightText(getResources().getString(R.string.cancel));
		topRightTitleTxt.setBackgroundResource(R.drawable.tt_default_btn_bk);
		topRightTitleTxt.setOnClickListener(this);
		
		//搜索监听
		topSearchEdt.addTextChangedListener(this);
	}
	
	private void init(){
		noSearchResultView = (TextView)curView.findViewById(R.id.tt_fragment_search_no_result);
		searchAdapter = new SearchAdapter(getActivity());
		listView = (ListView) curView.findViewById(R.id.tt_fragment_search_list);
		listView.setOnItemClickListener(this);
		showBar = (ProgressBar)curView.findViewById(R.id.tt_fragment_search_pro);
	}
	
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.left_btn:
		case R.id.left_txt:
			getActivity().finish();
			break;
		case R.id.right_txt:
			if(TextUtils.isEmpty(topSearchEdt.getText().toString())){
				getActivity().finish();
			}
			topSearchEdt.setText(null);
			break;
		}
	}

	public void beforeTextChanged(CharSequence s, int start, int count,int after) {
		if(s.toString().length()>0){
			searchAdapter.clearList();
		}
	}
	public void onTextChanged(CharSequence s, int start, int before,int count) {
		if(s.toString().length()>0){
			mHandler.sendEmptyMessage(BEGIN_SERACH);
		}
	}
	public void afterTextChanged(Editable s) {}

	public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
		Contact contact = (Contact)searchAdapter.getItem(pos);
		Intent intent = new Intent();
		intent.setClass(getActivity(), UserInfoActivity.class);
		intent.putExtra("contact", contact);
		intent.putExtra("who_send", "person");
		getActivity().startActivity(intent);
		
	}
	
}
