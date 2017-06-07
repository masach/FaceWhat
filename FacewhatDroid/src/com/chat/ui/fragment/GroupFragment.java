package com.chat.ui.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.util.StringUtils;

import com.chat.IM;
import com.chat.IMService;
import com.chat.R;
import com.chat.db.provider.DeptProvider;
import com.chat.service.aidl.Contact;
import com.chat.service.aidl.GroupItem;
import com.chat.service.aidl.IMXmppBinder;
import com.chat.service.aidl.UserItem;
import com.chat.ui.ChatActivity;
import com.chat.ui.UserInfoActivity;
import com.chat.ui.adapter.GroupAdapter;
import com.chat.ui.adapter.ContactAdapter.ContactHolder;
import com.chat.ui.base.TTBaseFragment;
import com.chat.ui.widget.MyToast;
import com.chat.ui.widget.NoScrollExpandableListview;
import com.chat.ui.widget.NoScrollListview;
import com.chat.utils.IMUIHelper;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class GroupFragment extends TTBaseFragment 
implements 
OnClickListener,
OnItemClickListener,
OnGroupClickListener,
OnChildClickListener{
	private View curView = null;
	private NoScrollExpandableListview listCompany;
	private NoScrollListview listMeet,listDept;
	private GroupAdapter groupAdapter;
	private SimpleAdapter myDeptAdapter,myMeetAdapter;
	private TextView tvCompany;
	private TextView tvMyDept;
	private TextView tvMeet;

	//用来显示ContactAdapter里面的CheckBox
	private LinearLayout isShowCheck;
	private Button btnCreateMul,btnCancel,btnAllSelect,btnReverseSelect;

	private final static int GET_GROUP = 1000;
	private final static int GET_MY_DEPT = 1001;
	private final static int GET_USER_VCARD = 1002;
	private final static int GET_MY_MEET = 1003;
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case GET_MY_MEET:
				if(listMeet.getVisibility() == View.GONE){
					listMeet.setVisibility(View.VISIBLE);
				}else{
					listMeet.setVisibility(View.GONE);
				}

				break;
			case GET_MY_DEPT://只获取自己所在的部门
				if(listDept.getVisibility() == View.GONE){
					listDept.setVisibility(View.VISIBLE);
				}else{
					listDept.setVisibility(View.GONE);
				}

				Cursor myDept = getActivity().getContentResolver().query(
						DeptProvider.DEPT_URI, null, 
						DeptProvider.DeptColumns.USER_JID + "=? or "+
								DeptProvider.DeptColumns.GROUP_FATHER_NAME + "=?",
								new String[]{StringUtils.parseBareAddress(IM.getString(IM.ACCOUNT_JID)),"0"},
								null);
				List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
				for(int i = 0;i < myDept.getCount();i++){
					myDept.moveToPosition(i);
					String groupJid = myDept.getString(myDept.getColumnIndex(DeptProvider.DeptColumns.GROUP_JID));
					String displayName = myDept.getString(myDept.getColumnIndex(DeptProvider.DeptColumns.DISPLAY_NAME));

					Map<String, Object> item = new HashMap<String, Object>(); 
					item.put("name", displayName);
					item.put("jid", groupJid);

					list.add(item);
				}
				myDeptAdapter = new SimpleAdapter(getActivity(),
						list, R.layout.tt_item_group_parent, new String[]{"jid","name"},
						new int[]{R.id.tt_item_group_parent_id,R.id.tt_item_group_parent_name});
				listDept.setAdapter(myDeptAdapter);
				break;

			case GET_GROUP://获取所有的通信录
				if(listCompany.getVisibility() == View.GONE){
					listCompany.setVisibility(View.VISIBLE);
				}else{
					listCompany.setVisibility(View.GONE);
				}

				Cursor myCompany = getActivity().getContentResolver().query(
						DeptProvider.DEPT_URI, null, null, null,null);
				List<GroupItem> groupList = dataChange(myCompany);
				for(int i = 0;i < groupList.size();i++){
					GroupItem groupItem =  groupList.get(i);
					if(!groupItem.isOrgnization()){
						groupList.remove(i);
					}
					if(groupItem.isOrgnization() && groupItem.getGroupFatherName().equals("0")){
						if(i > 0 ){
							groupList.set(i, groupList.get(0));
							groupList.set(0, groupItem);
						}
					}
				}
				groupAdapter = new GroupAdapter(getActivity(),groupList);
				listCompany.setAdapter(groupAdapter);
				break;

			case GET_USER_VCARD:
				try {
					GroupItem groupItem = groupAdapter.getGroup(Integer.parseInt(msg.obj.toString()));
					List<UserItem> userList = groupItem.getUserItem();
					for(UserItem userItem : userList){
						binder.createConnection().getVCard(userItem.getUserJid());
					}
				} catch (Exception e) {
					e.printStackTrace();
					MyToast.showToastLong(getActivity(), "获取失败，请检查网络");
				}
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

	public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
		if (null != curView) {
			((ViewGroup) curView.getParent()).removeView(curView);
			return curView;
		}
		curView = inflater.inflate(R.layout.tt_fragment_group,
				topContentView);

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

	private void initParent() {
		// 设置标题
		setTopTitleBold(getActivity().getString(R.string.chat_title));
		//右上角的查找、添加。。。
		onSearchDataReady();
		//搜索功能
		setTopSearchButton(R.drawable.search);
		//右上角功能事件
		topRightBtn.setOnClickListener(this);
	}

	private void init(){
		tvCompany = (TextView)curView.findViewById(R.id.tt_fragment_group_company);
		tvCompany.setOnClickListener(this);
		tvCompany.setText(" "+getActivity().getResources().getString(R.string.app_name)+"企业通讯录 ");

		tvMyDept = (TextView)curView.findViewById(R.id.tt_fragment_group_my_dept);
		tvMyDept.setOnClickListener(this);
		tvMyDept.setText("我的部门");

		tvMeet = (TextView)curView.findViewById(R.id.tt_fragment_group_my_meet);
		tvMeet.setOnClickListener(this);
		tvMeet.setText("我的会议 ");

		listCompany = (NoScrollExpandableListview)curView.findViewById(R.id.tt_fragment_group_list_my_company);
		listCompany.setOnGroupClickListener(this);
		listCompany.setOnChildClickListener(this);

		listMeet = (NoScrollListview)curView.findViewById(R.id.tt_fragment_group_list_my_meet);
		listMeet.setOnItemClickListener(this);

		listDept = (NoScrollListview)curView.findViewById(R.id.tt_fragment_group_list_my_dept);
		listDept.setOnItemClickListener(this);

		isShowCheck = (LinearLayout)curView.findViewById(R.id.tt_fragment_group_layout_show_check);
		btnCreateMul = (Button)curView.findViewById(R.id.tt_fragment_group_btn_create_mul);
		btnCancel = (Button)curView.findViewById(R.id.tt_fragment_group_btn_cancel);
		btnAllSelect = (Button)curView.findViewById(R.id.tt_fragment_group_btn_all_select);
		btnReverseSelect = (Button)curView.findViewById(R.id.tt_fragment_group_btn_reverse_select);

		btnCreateMul.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
		btnAllSelect.setOnClickListener(this);
		btnReverseSelect.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.tt_fragment_group_my_dept:
			mHandler.sendEmptyMessage(GET_MY_DEPT);
			break;

		case R.id.tt_fragment_group_company:
			mHandler.sendEmptyMessage(GET_GROUP);
			break;

		case R.id.right_btn:
			PopupMenu popup = new PopupMenu(getActivity(),v);
			getActivity().getMenuInflater().inflate(R.menu.tt_popumenu_group,popup.getMenu());
			popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

				@Override
				public boolean onMenuItemClick(MenuItem item) {
					switch(item.getItemId()){
					//开会
					case R.id.menu_group_chat:

						break;

//						//查找好友
//					case R.id.menu_find:
//						IMUIHelper.instance().openSearchActivit(getActivity());
//						break;
					}
					return true;
				}
			});
			popup.show();
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int pos, long arg3) {
		//判断是否在进行选人开会
//		if(isShowCheck.getVisibility() == View.VISIBLE){
//			// 取得ViewHolder对象，这样就省去了通过层层的findViewById去实例化我们需要的cb实例的步骤
//			ContactHolder holder = (ContactHolder) view.getTag();
//			// 改变CheckBox的状态
//			holder.cbMeet.toggle();
//			// 将CheckBox的选中状况记录下来
//			groupAdapter.getIsSelected().put(pos, holder.cbMeet.isChecked());
//		}else{
			TextView tvJid = (TextView)view.findViewById(R.id.tt_item_group_parent_id);
			TextView tvName = (TextView)view.findViewById(R.id.tt_item_group_parent_name);
			Log.e("GroupFragment:list item", tvJid.getText().toString());

			Contact contact = new Contact();
			contact.setAccount(tvJid.getText().toString());
			contact.setName(tvName.getText().toString());
			Intent intent = new Intent();
			intent.setClass(getActivity(), ChatActivity.class);
			intent.putExtra("contact", contact);
			getActivity().startActivity(intent);
//		}

	}

	@Override
	public boolean onGroupClick(ExpandableListView arg0, View v, int pos,
			long id) {
		Message msg = mHandler.obtainMessage();
		msg.what = GET_USER_VCARD;
		msg.obj = pos;
		mHandler.sendMessage(msg);
		return false;
	}

	@Override
	public boolean onChildClick(ExpandableListView arg0, View v, int posGroup,
			int posUser, long arg4) {
		UserItem userItem = groupAdapter.getChild(posGroup, posUser);

		Contact contact = new Contact();
		contact.setAccount(userItem.getUserJid());
		contact.setName(userItem.getUserNickName());
		Intent intent = new Intent();
		intent.setClass(getActivity(), ChatActivity.class);
		intent.putExtra("contact", contact);
		getActivity().startActivity(intent);
		return false;
	}

	//将cursor转换成List<GroupItem>
	private List<GroupItem> dataChange(Cursor cursor){
		List<GroupItem> groupList = new ArrayList<GroupItem>();
		List<UserItem> userList = null;
		GroupItem groupItem = null;
		String flag = "";
		int first = 0;
		for(int i = 0;i < cursor.getCount();i++){
			cursor.moveToPosition(i);
			String displayName = cursor.getString(cursor.getColumnIndex(DeptProvider.DeptColumns.DISPLAY_NAME));
			String groupFatherName = cursor.getString(cursor.getColumnIndex(DeptProvider.DeptColumns.GROUP_FATHER_NAME));
			String groupJid = cursor.getString(cursor.getColumnIndex(DeptProvider.DeptColumns.GROUP_JID));
			String groupName = cursor.getString(cursor.getColumnIndex(DeptProvider.DeptColumns.GROUP_NAME));
			boolean isOrgnization = cursor.getString(cursor.getColumnIndex(DeptProvider.DeptColumns.IS_ORGNIZATION)).equals("0")?false:true;
			String fullPinYin = cursor.getString(cursor.getColumnIndex(DeptProvider.DeptColumns.FULL_PIN_YIN));
			String shortPinYin = cursor.getString(cursor.getColumnIndex(DeptProvider.DeptColumns.SHORT_PIN_YIN));
			String userJid = cursor.getString(cursor.getColumnIndex(DeptProvider.DeptColumns.USER_JID));
			String userName = cursor.getString(cursor.getColumnIndex(DeptProvider.DeptColumns.USER_NAME));
			String userNickName = cursor.getString(cursor.getColumnIndex(DeptProvider.DeptColumns.USER_NICK_NAME));

			//相等代表是同一组
			if(flag.equals(groupJid)){
				UserItem userItem = new UserItem();
				userItem.setFullPinYin(fullPinYin);
				userItem.setShortPinYin(shortPinYin);
				userItem.setUserJid(userJid);
				userItem.setUserName(userName);
				userItem.setUserNickName(userNickName);
				userList.add(userItem);
			}else{//不等代表是另外一组
				flag = groupJid;//
				if(first == 0){
					first ++ ;

					groupItem = new GroupItem();
					groupItem.setDisplayName(displayName);
					groupItem.setGroupFatherName(groupFatherName);
					groupItem.setGroupJid(groupJid);
					groupItem.setGroupName(groupName);
					groupItem.setOrgnization(isOrgnization);

					userList = new ArrayList<UserItem>();
					UserItem userItem = new UserItem();
					userItem.setFullPinYin(fullPinYin);
					userItem.setShortPinYin(shortPinYin);
					userItem.setUserJid(userJid);
					userItem.setUserName(userName);
					userItem.setUserNickName(userNickName);

					userList.add(userItem);

				}else{
					groupItem.setUserItem(userList);
					groupList.add(groupItem);

					groupItem = new GroupItem();
					groupItem.setDisplayName(displayName);
					groupItem.setGroupFatherName(groupFatherName);
					groupItem.setGroupJid(groupJid);
					groupItem.setGroupName(groupName);
					groupItem.setOrgnization(isOrgnization);

					userList = new ArrayList<UserItem>();
					UserItem userItem = new UserItem();
					userItem.setFullPinYin(fullPinYin);
					userItem.setShortPinYin(shortPinYin);
					userItem.setUserJid(userJid);
					userItem.setUserName(userName);
					userItem.setUserNickName(userNickName);

					userList.add(userItem);
				}
			}
			if(cursor.isLast()){
				groupItem.setUserItem(userList);
				groupList.add(groupItem);
			}
		}
		return groupList;
	}


}
