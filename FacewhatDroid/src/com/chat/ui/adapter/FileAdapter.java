package com.chat.ui.adapter;

import java.util.List;

import com.chat.IM;
import com.chat.R;
import com.chat.service.aidl.FileInfo;
import com.chat.utils.FileUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileAdapter extends BaseAdapter{
	private Context context;
	private List<FileInfo> fileList;
	private LayoutInflater mLayoutInflater = null;

	public FileAdapter(Context context,List<FileInfo> fileList){
		this.context = context;
		this.fileList = fileList;
		mLayoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return fileList.size();
	}

	public FileInfo getItem(int pos) {
		return fileList.get(pos);
	}

	public long getItemId(int pos) {
		return 0;
	}

	public View getView(int pos, View convertView, ViewGroup viewGroup) {
		HolderView holder = null;
		if(convertView == null || convertView.getTag() == null){
			convertView = mLayoutInflater.inflate(R.layout.tt_item_file,
					null);
			holder = new HolderView(convertView);
			convertView.setTag(holder);
		}else{
			holder = (HolderView) convertView.getTag();
		}

		FileInfo fileInfo = (FileInfo)getItem(pos);
//		IM.Bitmap2Drawable(IM.zoomImg(IM.getBitmap(R.drawable.videoicon),80,80))
		if(FileUtil.compareFile(fileInfo.getFileType(), IM.PICTURE_SUFFIX)){//图片处理
			Bitmap bm = BitmapFactory.decodeFile(fileInfo.getFilePath());
			if( bm != null){
				holder.ivFileIcon.setImageDrawable(IM.Bitmap2Drawable(IM.zoomImg(bm,100,100)));
			}
		}else if(FileUtil.compareFile(fileInfo.getFileType(), IM.MUSIC_SUFFIX)){//语音
//			holder.ivFileIcon.setImageDrawable(IM.Bitmap2Drawable(
//					IM.zoomImg(IM.getBitmap(R.drawable.videoicon),80,80)));
		}else if(FileUtil.compareFile(fileInfo.getFileType(), IM.VIDEO_SUFFIX)){//视频
			holder.ivFileIcon.setImageDrawable(IM.Bitmap2Drawable(
					IM.zoomImg(IM.getBitmap(R.drawable.videoicon),80,80)));
		}else if(FileUtil.compareFile(fileInfo.getFileType(), IM.FILE_SUFFIX)){//文件
			holder.ivFileIcon.setImageDrawable(IM.Bitmap2Drawable(
					IM.zoomImg(IM.getBitmap(R.drawable.fileicon),80,80)));
		}else if(FileUtil.compareFile(fileInfo.getFileType(), IM.ZIP_SUFFIX)){//压缩包
			holder.ivFileIcon.setImageDrawable(IM.Bitmap2Drawable(
					IM.zoomImg(IM.getBitmap(R.drawable.zipicon),80,80)));
		}else if(FileUtil.compareFile(fileInfo.getFileType(), IM.APPLICATION_SUFFIX)){//应用程序
			holder.ivFileIcon.setImageDrawable(IM.Bitmap2Drawable(
					IM.zoomImg(IM.getBitmap(R.drawable.applicationicon),80,80)));
		}

		holder.tvFileName.setText(fileInfo.getFileName());
		holder.tvFilePath.setText(fileInfo.getFilePath());

		return convertView;
	}

	class HolderView{
		ImageView ivFileIcon;
		TextView tvFileName;
		TextView tvFilePath;

		public HolderView(View v){
			ivFileIcon = (ImageView)v.findViewById(R.id.imgFileIcon);
			tvFileName = (TextView)v.findViewById(R.id.tvFileName);
			tvFilePath = (TextView)v.findViewById(R.id.tvFilePath);
		}
	}

}
