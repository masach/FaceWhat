package com.chat.ui;

import java.io.File;

import org.jivesoftware.smack.util.StringUtils;

import com.chat.IM;
import com.chat.IMService;
import com.chat.R;
import com.chat.db.provider.DeptProvider;
import com.chat.db.provider.SMSProvider;
import com.chat.db.provider.SMSProvider.SMSColumns;
import com.chat.service.LoginAsyncTask;
import com.chat.service.aidl.Contact;
import com.chat.service.aidl.IMXmppBinder;
import com.chat.ui.adapter.ChatAdapter;
import com.chat.ui.base.TTBaseActivity;
import com.chat.ui.helper.Emoparser;
import com.chat.ui.widget.EmoGridView;
import com.chat.ui.widget.MyToast;
import com.chat.ui.widget.EmoGridView.OnEmoGridViewItemClick;
import com.chat.ui.widget.YayaEmoGridView;
import com.chat.utils.FileUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class ChatActivity  extends TTBaseActivity implements
OnRefreshListener2<ListView>,
View.OnClickListener,
OnTouchListener,
TextWatcher,
SensorEventListener{

	private Contact contact;
	private static boolean isDeptChat = false;
	private Context context;

	private Cursor cursor;
	private ContentValues values;
	private ContentObserver co;

	private InputMethodManager inputManager = null;//输入法管理

	private ListView lvPTR = null;
	private TextView textView_new_msg_tip = null;//新消息

	//底部控件
	private TextView sendBtn = null;//发送消息
	private ImageView showVoice = null;//切换语音输入
	private ImageView showEmo = null;//切换表情输入
	private ImageView showPhoto = null;//切换图片输入
	private ImageView showKeyboard = null;//切换文本输入，与语音切换
	private EditText textMsgEdt = null;//文本输入
	private Button recordAudioBtn  = null;//语音输入,按住说话
	private LinearLayout emoLayout = null;//表情界面
	private EmoGridView emoGridView = null;//默认表情
	private YayaEmoGridView yayaEmoGridView = null;//默认表情
	private RadioGroup emoRadioGroup = null;//牙牙表情和默认表情切换
	private View addOthersPanelView = null;//其它容器
	private View pictureBtn = null; //获取照片
	private View fileBtn = null; //获取文件
	private View recentlyBtn = null; //最近使用
	//getRotationMatrix用来计算旋转矩阵，过getOrientation求得设备的方向（航向角、俯仰角、横滚角）


	boolean isShowEmo = false;//用来标记是否要显示表情界面，false不显示，true要显示
	int rootBottom = Integer.MIN_VALUE, keyboardHeight = 0;//键盘布局相关参数

	private ChatAdapter adapter = null;

	//处理拍照与照片选择
	private File tempFile;//拍照文件

	private static int CROLL_FIRST_POS;//记录list上一次滑动的位置
	private final static int SEND_MESSAGE = 1000;
	private final static int SEND_YAYA = 1001;
	private final static int SEND_FILE = 1002;
	private final static int UPDATE_UNREAD_SESSION = 1003;
	private final static int FIND_SESSION = 1003;
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			String chatType = isDeptChat?"groupchat":"chat";
			switch(msg.what){
			case SEND_MESSAGE:
				Log.e("发送文本消息",""+chatType);
				try {
					binder.createConnection().sendMessage(contact.getAccount(),contact.getName(), msg.obj.toString(),chatType);
					textMsgEdt.setText("");
					
				} catch (Exception e) {
					e.printStackTrace();
					MyToast.showToastLong(context, "请检查网络，发送失败");
				}
				break;
			case SEND_YAYA:
				Log.e("发送牙牙表情",""+chatType);
				try {
					binder.createConnection().sendMessage(contact.getAccount(),contact.getName(), msg.obj.toString(),chatType);
				} catch (Exception e) {
					e.printStackTrace();
					MyToast.showToastLong(ChatActivity.this, "请检查网络，发送失败");
				}
				break;
			case SEND_FILE:
				break;
			case UPDATE_UNREAD_SESSION:
				//				ListView lv = lvPTR.getRefreshableView();
				//				if(lv != null && (adapter.getCount() - lv.getSelectedItemPosition())<10){
				//将未读信息改成已读
				values=new ContentValues();
				values.put(SMSColumns.UNREAD, "read");
				getContentResolver().update(
						SMSProvider.SMS_URI, 
						values, 
						SMSColumns.SESSION_ID + "=? and "+SMSColumns.UNREAD + "=? ",
						new String[]{contact.getAccount(),"unread"});
				//				}
				break;
			}
		}
	};
	private IMXmppBinder binder;
	private LoginAsyncTask loginTask = new LoginTask();
	private ServiceConnection serviceConnect = new XmppServiceConnect();
	// XMPP连接服务 
	private class XmppServiceConnect implements ServiceConnection {
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			binder = IMXmppBinder.Stub.asInterface(iBinder);
			loginTask.execute(binder);
		}
		public void onServiceDisconnected(ComponentName componentName) {
			binder = null;
		}
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;

		Bundle bundle = getIntent().getExtras(); 
		contact = bundle.getParcelable("contact"); 
		Log.e("ChatActivity获取传过 的jid为：", contact.getAccount()==null?"null":contact.getAccount());

		//查询当前账户的信息
		cursor = getContentResolver().query(DeptProvider.DEPT_URI,
				null, DeptProvider.DeptColumns.GROUP_JID +"=?",
				new String[]{contact.getAccount()}, null);
		//判断群聊还是个人聊天
		if(cursor != null && cursor.getCount() > 0 ){
			isDeptChat = true;
		}else{
			isDeptChat = false;
		}
		Log.e("是否是部门？",""+isDeptChat);



		initEmo();//初始化表情
		initParent();//初始化父组件
		initView();//初始化界面
		initData();//初始化数据

	}

	private void initParent(){
		// 绑定布局资源(注意放所有资源初始化之前)
		LayoutInflater.from(this).inflate(R.layout.tt_activity_message, topContentView);
		//判断是否是部门
		if(isDeptChat){
			//查询当前账户的信息
			cursor = getContentResolver().query(DeptProvider.DEPT_URI,
					null, DeptProvider.DeptColumns.GROUP_JID +"=?",
					new String[]{contact.getAccount()}, null);
			
			if(cursor != null && cursor.moveToFirst()){
				cursor.moveToPosition(0);
				contact.setName(cursor.getString(cursor.getColumnIndex(DeptProvider.DeptColumns.DISPLAY_NAME)));
				setTitle(contact.getName());
			}
			setRightButton(R.drawable.tt_top_right_group_manager);
		}else{
			setTitle(contact.getName()==null?StringUtils.parseName(contact.getAccount()):contact.getName());
			setRightButton(R.drawable.tt_top_right_contact_manager);
		}

		//初始父换件
		setLeftButton(R.drawable.tt_top_back);
		setLeftText("返回");
		
		topLeftBtn.setOnClickListener(this);
		letTitleTxt.setOnClickListener(this);
		topRightBtn.setOnClickListener(this);
	}

	private void initData(){
		//内容观察者监听
		co = new ContentObserver(new Handler()){
			@Override
			public void onChange(boolean selfChange){
				mHandler.sendEmptyMessage(UPDATE_UNREAD_SESSION);
				cursor = getContentResolver()
						.query(
								SMSProvider.SMS_URI, 
								null,
								SMSColumns.SESSION_ID + "=?",
								new String[]{contact.getAccount()},
								null);
				adapter.changeCursor(cursor);
			}
		};
		//注册内容观察者
		getContentResolver().registerContentObserver(SMSProvider.SMS_URI,true, co);
		co.onChange(true);
	}

	//初始化界面控件
	private void initView() {
		//初始化键盘管理
		inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		textView_new_msg_tip = (TextView) findViewById(R.id.tt_new_msg_tip);
		
		//消息列表
		lvPTR = (ListView) this.findViewById(R.id.message_list);
		adapter = new ChatAdapter(cursor,this);
		lvPTR.setAdapter(adapter);
		
		textView_new_msg_tip.setOnClickListener(this);

		// 界面底部输入框布局
		showVoice = (ImageView)findViewById(R.id.show_voice_btn);//切换语音输入
		showVoice.setOnClickListener(this);
		showEmo = (ImageView)findViewById(R.id.show_emo_btn);//显示表情
		showEmo.setOnClickListener(this);
		showPhoto = (ImageView)findViewById(R.id.show_photo_btn);//显示图片
		showPhoto.setOnClickListener(this);
		showKeyboard = (ImageView)findViewById(R.id.show_keyboard_btn);//显示键盘输入
		showKeyboard.setOnClickListener(this);
		textMsgEdt = (EditText)findViewById(R.id.message_text);//文本框输入
		textMsgEdt.setOnFocusChangeListener(msgEditOnFocusChangeListener);
		textMsgEdt.setOnClickListener(this);
		textMsgEdt.addTextChangedListener(this);
		inputManager.hideSoftInputFromWindow(textMsgEdt.getWindowToken(), 0);//隐藏键盘
		sendBtn = (TextView)findViewById(R.id.send_message_btn);//发送信息
		sendBtn.setOnClickListener(this);
		recordAudioBtn  = (Button)findViewById(R.id.record_voice_btn);//语音输入,按住说话

		//切换的几个面板
		emoLayout = (LinearLayout)findViewById(R.id.emo_layout);//表情界面
		emoGridView = (EmoGridView) findViewById(R.id.emo_gridview);
		emoGridView.setOnEmoGridViewItemClick(onEmoGridViewItemClick);
		emoGridView.setAdapter();
		yayaEmoGridView = (YayaEmoGridView) findViewById(R.id.yaya_emo_gridview);
		yayaEmoGridView.setOnEmoGridViewItemClick(yayaOnEmoGridViewItemClick);
		yayaEmoGridView.setAdapter();
		addOthersPanelView = (LinearLayout)findViewById(R.id.add_others_panel);//其它面板
		emoRadioGroup = (RadioGroup)findViewById(R.id.emo_tab_group);//进行默认表情和牙牙表情的切换
		emoRadioGroup.setOnCheckedChangeListener(emoOnCheckedChangeListener);

		pictureBtn = (View)findViewById(R.id.picture_btn); 
		pictureBtn.setOnClickListener(this);
		fileBtn = (View)findViewById(R.id.file_btn); 
		fileBtn.setOnClickListener(this);
		recentlyBtn = (View)findViewById(R.id.recently_btn); 
		recentlyBtn.setOnClickListener(this);
	}

	protected void onStart() {
		super.onStart();
		bindService(new Intent(this, IMService.class), serviceConnect, BIND_AUTO_CREATE);
	}

	protected void onDestroy() {
		super.onDestroy();
		getContentResolver().unregisterContentObserver(co);
		unbindService(serviceConnect);
	}

	public void onClick(View v) {
		final int id = v.getId();
		switch(id){
		case R.id.left_btn:
		case R.id.left_txt:
			ChatActivity.this.finish();//结束
			break;
		case R.id.right_btn:
			Intent intent = new Intent();
			intent.setClass(ChatActivity.this, UserInfoActivity.class);
			intent.putExtra("contact", contact);
			if(isDeptChat){
				intent.putExtra("who_send", "dept");
			}else{
				intent.putExtra("who_send", "person");
			}
			ChatActivity.this.startActivity(intent);
			break;
		case R.id.show_photo_btn:
			if(isDeptChat){
				return;
			}
			//进行文件选择
			inputManager.hideSoftInputFromWindow(textMsgEdt.getWindowToken(), 0);
			emoLayout.setVisibility(View.GONE);
			addOthersPanelView.setVisibility(View.VISIBLE);
			break;
		case R.id.show_keyboard_btn:
			//显示键盘输入                                                  
			showVoice.setVisibility(View.VISIBLE);
			showKeyboard.setVisibility(View.GONE);
			showEmo.setVisibility(View.VISIBLE);
			recordAudioBtn.setVisibility(View.GONE);
			textMsgEdt.setVisibility(View.VISIBLE);
			textMsgEdt.requestFocus();
			break;
		case R.id.show_voice_btn:
			if(isDeptChat){
				return;
			}
			//显示语音输入
			showVoice.setVisibility(View.GONE);
			showKeyboard.setVisibility(View.VISIBLE);
			showEmo.setVisibility(View.GONE);
			recordAudioBtn.setVisibility(View.VISIBLE);
			textMsgEdt.setVisibility(View.GONE);
			addOthersPanelView.setVisibility(View.GONE);
			emoLayout.setVisibility(View.GONE);
			inputManager.hideSoftInputFromWindow(textMsgEdt.getWindowToken(), 0);
			break;
		case R.id.show_emo_btn:
			if(emoLayout.getVisibility() == View.GONE){
				//显示表情界面
				inputManager.hideSoftInputFromWindow(textMsgEdt.getWindowToken(), 0);
				addOthersPanelView.setVisibility(View.GONE);
				emoLayout.setVisibility(View.VISIBLE);
				emoGridView.setVisibility(View.VISIBLE);
				yayaEmoGridView.setVisibility(View.GONE);
			}else{
				inputManager.hideSoftInputFromWindow(textMsgEdt.getWindowToken(), 0);
				addOthersPanelView.setVisibility(View.GONE);
				emoLayout.setVisibility(View.GONE);
				emoGridView.setVisibility(View.GONE);
				yayaEmoGridView.setVisibility(View.GONE);
			}
			break;
		case R.id.message_text:
			//点击输入文本
			emoLayout.setVisibility(View.GONE);
			addOthersPanelView.setVisibility(View.GONE);
			break;
		case R.id.tt_new_msg_tip:
			//滑到上一次记录的位置底部
//			ListView lv = lvPTR.getRefreshableView();
//			if (lv != null) {
//				lv.setSelection(adapter.getCount() + 1);
//			}
			textView_new_msg_tip.setVisibility(View.GONE);
			break;

			//选择照片
		case R.id.picture_btn: 
			Intent intentSelect = new Intent(Intent.ACTION_PICK, null);
			intentSelect.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,  
					"image/*");
			startActivityForResult(intentSelect, IM.selectCode);
			break;

			//发送文件
		case R.id.file_btn:
			Intent file = new Intent(this, FileActivity.class);
			file.putExtra("chat_type",  IM.FILE_TYPE[4]);  
			startActivityForResult(file,IM.fileCode);
			break;
			
			//最近使用过的
		case R.id.recently_btn:
			Intent recently = new Intent(this, FileActivity.class);
			recently.putExtra("chat_type", "rec");  
			startActivityForResult(recently,IM.fileCode);
			break;
			
		case R.id.send_message_btn:
			//发送信息
			String bodyStr = textMsgEdt.getText().toString();
			if(bodyStr.equals("")){
				return;
			}
			Message msg = mHandler.obtainMessage();
			msg.what = SEND_MESSAGE;
			msg.obj = bodyStr;
			mHandler.sendMessage(msg);
			break;
		}
	}    

	//通过点击表情，将表情对应的名称输入到文本框
	private OnEmoGridViewItemClick onEmoGridViewItemClick = new OnEmoGridViewItemClick() {
		public void onItemClick(int facesPos, int viewIndex)  {
			int deleteId = (++viewIndex) * (IM.pageSize - 1);
			if (deleteId > Emoparser.getInstance(ChatActivity.this).getResIdList().length) {
				deleteId = Emoparser.getInstance(ChatActivity.this).getResIdList().length;
			}

			if (deleteId == facesPos) {
				String msgContent = textMsgEdt.getText().toString();
				if (msgContent.isEmpty())
					return;
				if (msgContent.contains("["))
					msgContent = msgContent.substring(0, msgContent.lastIndexOf("["));
				textMsgEdt.setText(msgContent);
			} else {
				int resId = Emoparser.getInstance(ChatActivity.this).getResIdList()[facesPos];
				String pharse = Emoparser.getInstance(ChatActivity.this).getIdPhraseMap()
						.get(resId);
				int startIndex = textMsgEdt.getSelectionStart();
				//Editable对字体进行编辑
				Editable edit = textMsgEdt.getEditableText();
				if (startIndex < 0 || startIndex >= edit.length()) {
					if (null != pharse) {
						edit.append(pharse);
					}
				} else {
					if (null != pharse) {
						edit.insert(startIndex, pharse);
					}
				}
			}
			Editable edtable = textMsgEdt.getText();
			int position = edtable.length();
			Selection.setSelection(edtable, position);
		}
	};

	//直接发送牙牙表情
	private YayaEmoGridView.OnEmoGridViewItemClick yayaOnEmoGridViewItemClick = new YayaEmoGridView.OnEmoGridViewItemClick() {
		public void onItemClick(int facesPos, int viewIndex) {
			int resId = Emoparser.getInstance(ChatActivity.this).getYayaResIdList()[facesPos];

			String content = Emoparser.getInstance(ChatActivity.this).getYayaIdPhraseMap()
					.get(resId);
			if (content.equals("")) {
				Toast.makeText(ChatActivity.this,
						getResources().getString(R.string.message_null), Toast.LENGTH_LONG).show();
				return;
			}
			Message msg = mHandler.obtainMessage();
			msg.what = SEND_YAYA;
			msg.obj = content;
			mHandler.sendMessage(msg);
		}
	};

	public void onAccuracyChanged(Sensor arg0, int arg1) {}
	public void onSensorChanged(SensorEvent arg0) {}
	public void afterTextChanged(Editable arg0) {}
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,int arg3) {}
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (s.length() > 0) {
			sendBtn.setVisibility(View.VISIBLE);
			RelativeLayout.LayoutParams param = (LayoutParams) textMsgEdt
					.getLayoutParams();
			param.addRule(RelativeLayout.LEFT_OF, R.id.show_emo_btn);
			showPhoto.setVisibility(View.GONE);
		} else {
			showPhoto.setVisibility(View.VISIBLE);
			RelativeLayout.LayoutParams param = (LayoutParams) textMsgEdt
					.getLayoutParams();
			param.addRule(RelativeLayout.LEFT_OF, R.id.show_emo_btn);
			sendBtn.setVisibility(View.GONE);
		}
	}

	public boolean onTouch(View arg0, MotionEvent arg1) {return false;}
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {}
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {}


	//初始化表情
	private void initEmo() {
		Emoparser.getInstance(ChatActivity.this);
	}

	private OnTouchListener lvPTROnTouchListener = new View.OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				textMsgEdt.clearFocus();
				if (emoLayout.getVisibility() == View.VISIBLE) {
					emoLayout.setVisibility(View.GONE);
				}

				if (addOthersPanelView.getVisibility() == View.VISIBLE) {
					addOthersPanelView.setVisibility(View.GONE);
				}
				inputManager.hideSoftInputFromWindow(textMsgEdt.getWindowToken(), 0);
			}
			return false;
		}
	};

	private RadioGroup.OnCheckedChangeListener emoOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
		public void onCheckedChanged(RadioGroup radioGroup, int id) {
			switch (id) {
			case R.id.tab1:
				if (emoGridView.getVisibility() != View.VISIBLE) {
					yayaEmoGridView.setVisibility(View.GONE);
					emoGridView.setVisibility(View.VISIBLE);
				}
				break;
			case R.id.tab2:
				if (yayaEmoGridView.getVisibility() != View.VISIBLE) {
					emoGridView.setVisibility(View.GONE);
					yayaEmoGridView.setVisibility(View.VISIBLE);
				}
				break;
			}
		}
	};

	//输入框获取焦点监听
	private View.OnFocusChangeListener msgEditOnFocusChangeListener = new android.view.View.OnFocusChangeListener() {
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				addOthersPanelView.setVisibility(View.GONE);
				emoLayout.setVisibility(View.GONE);
				inputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}
	};

	private void sendFile(String filePath) {
		if(filePath.isEmpty()){
			return;
		}
		boolean bool = true;
		try {
			bool = binder.createConnection().isOnlineByJID(contact.getAccount());
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
		if(!bool){
			MyToast.showToastLong(context, "对方不在线，不能发送离线文件");
			return;
		}

		Log.e("ChatActivity:sendFile", "filePath = " + filePath);
		String[] pathStrings = filePath.split("/"); // 文件名
		String fileName = null ;
		if (pathStrings!=null && pathStrings.length>0) {
			fileName = pathStrings[pathStrings.length-1];
		}
		try {
			binder.createConnection().sendFile(contact.getAccount(), filePath, fileName);
		} catch (RemoteException e) {
			e.printStackTrace();
			Log.e("ChatActivity:sendPicture",""+e.getMessage().toString());
		}
	}

	public void onBackPressed(){
		ChatActivity.this.finish();//结束
	}

	//图片选择之后的回调
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode != Activity.RESULT_OK){
			return;
		}
		switch(requestCode){
		case IM.fileCode:
			String filePath = data.getExtras().getString("file_path");
			Log.e("ChatActivity:onActivityResult","sendfile");
			sendFile(filePath);
			break;
		case IM.selectCode:
			
			//获得图片的uri
			Uri uri = data.getData();
			//获取图片的路径：
			String[] proj = {MediaStore.Images.Media.DATA};
			//filePath =
			//好像是android多媒体数据库的封装接口，具体的看Android文档
            Cursor cursor = managedQuery(uri, proj, null, null, null); 
            //按我个人理解 这个是获得用户选择的图片的索引值
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            //将光标移至开头 ，这个很重要，不小心很容易引起越界
            cursor.moveToFirst();
            //最后根据索引值获取图片路径
            String path = cursor.getString(column_index);
            
			Log.e("ChatActivity:onActivityResult","图片路径= "+path);
			sendFile(path);
			break;
		}
	}

	class LoginTask extends LoginAsyncTask{

		protected void onPostExecute(Integer result) {
			switch(result){
			case IM.LOGIN_OK:
				break;

			case IM.LOGIN_NET_ERROR:
				MyToast.showToastLong(context, "网络断开");
				break;

			case IM.LOGIN_SERVER_ERROR:
				MyToast.showToastLong(context, "服务器未连接");
				break;

			case IM.LOGIN_PASSWORD_ERROR:
				break;
			}
		}
	}
}
