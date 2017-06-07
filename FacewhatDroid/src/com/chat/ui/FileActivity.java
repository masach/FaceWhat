package com.chat.ui;

import java.util.List;

import com.chat.IM;
import com.chat.R;
import com.chat.service.aidl.FileInfo;
import com.chat.ui.adapter.FileAdapter;
import com.chat.ui.adapter.PictureAdapter;
import com.chat.ui.base.TTBaseActivity;
import com.chat.utils.FileUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class FileActivity extends TTBaseActivity implements 
	OnClickListener,
	OnItemClickListener,
    OnItemLongClickListener{
	private Context context;

//	private LinearLayout btnPicture,btnMusic,btnVideo,btnFile,btnZip,btnApplication,btnRecently;
//	private TextView tvPicture,tvMusic,tvVideo,tvFile,tvZip,tvApplication,tvRecently;

	private List<FileInfo> fileInfoList;
	private static String[] fileType = new String[]{"图片","音乐","视频","文档","压缩包","应用","最近使用"};

	private FileAdapter fileAdapter;
	private ListView listView;
	private GridView gridView;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initParent();
		init();
		setAdapter(getIntent().getSerializableExtra("chat_type").toString());
	}

	private void initParent(){
		// 绑定布局资源(注意放所有资源初始化之前)
		LayoutInflater.from(this).inflate(R.layout.tt_activity_file, topContentView);

		//初始父换件
		setLeftButton(R.drawable.tt_top_back);
		setLeftText(getResources().getString(R.string.top_left_back));
		setTitle("文件");
		topLeftBtn.setOnClickListener(this);
		letTitleTxt.setOnClickListener(this);
	}

	public void init(){
		context = this;
		listView = (ListView)findViewById(R.id.fileList);
		gridView = (GridView)findViewById(R.id.pictureList);
		listView.setOnItemClickListener(this);
	}

	private void setAdapter(String type){
		if(IM.FILE_TYPE[4].equals(type)){//文件
			gridView.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
			fileInfoList = FileUtil.findFileInfo(FileUtil.concat(IM.FILE_SUFFIX,IM.ZIP_SUFFIX,
					IM.APPLICATION_SUFFIX),IM.ALL_FILE_PATH);
			listView.setAdapter(new FileAdapter(context,fileInfoList));
		}else if("rec".equals(type)){//最近使用
			gridView.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
			fileInfoList = FileUtil.findFileInfo(FileUtil.concat(IM.PICTURE_SUFFIX,IM.MUSIC_SUFFIX,
					IM.VIDEO_SUFFIX,IM.FILE_SUFFIX,IM.ZIP_SUFFIX,IM.APPLICATION_SUFFIX),IM.ALL_FILE_PATH);
			listView.setAdapter(new FileAdapter(context,fileInfoList));
		}
	}

	public void onClick(View v) {
		switch(v.getId()){
		case R.id.left_btn://返回 上一级
		case R.id.left_txt:this.finish();break;
		}
	}

	public boolean onItemLongClick(AdapterView<?> viewParent, View view, int pos,long id) {
		return false;
	}

	public void onItemClick(AdapterView<?> viewParent, View view, int pos,long id) {
		FileInfo fileInfo = (FileInfo)viewParent.getAdapter().getItem(pos);
		Intent data = new Intent();
		data.putExtra("file_path", fileInfo.getFilePath());
		setResult(Activity.RESULT_OK, data);
		finish();
	}
}
