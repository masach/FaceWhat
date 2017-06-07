package com.chat.ui.adapter;

import java.util.List;

import com.chat.IM;
import com.chat.R;
import com.chat.service.aidl.FileInfo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PictureAdapter extends BaseAdapter{
	private Context context;
	private List<FileInfo> fileList;
	private LayoutInflater mLayoutInflater = null;

	public PictureAdapter(Context context,List<FileInfo> fileList){
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
			convertView = mLayoutInflater.inflate(R.layout.tt_item_picture,
					null);
			holder = new HolderView(convertView);
			convertView.setTag(holder);
		}else{
			holder = (HolderView) convertView.getTag();
		}

		FileInfo fileInfo = getItem(pos);
		Bitmap bm = BitmapFactory.decodeFile(fileInfo.getFilePath());
		if( bm != null){
			holder.ivFileIcon.setImageDrawable(IM.Bitmap2Drawable(IM.zoomImg(bm,120,120)));
		}

		holder.tvFilePath.setText(fileInfo.getFilePath());

		return convertView;
	}

	class HolderView{
		ImageView ivFileIcon;
		TextView tvFilePath;

		public HolderView(View v){
			ivFileIcon = (ImageView)v.findViewById(R.id.itemImage);
			tvFilePath = (TextView)v.findViewById(R.id.itemName);
		}
	}

}

