package com.chat.ui.widget;

import com.chat.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

public class TopTabButton extends FrameLayout{
	private Context context;
	private Button tabALLBtn;
	private Button tabDepartmentBtn;

	public Button getTabDepartmentBtn(){
		return tabDepartmentBtn;
	}
	
	public Button getTabALLBtn(){
		return tabALLBtn;
	}

	public TopTabButton(Context context){
		this(context,null);
	}

	public TopTabButton(Context context, AttributeSet attrs){
		this(context,attrs,0);
	}

	public TopTabButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		initView();
	}

	private void initView(){
		LayoutInflater inflater = (LayoutInflater)context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.tt_top_tab_button, this);
		tabALLBtn = (Button)findViewById(R.id.all_btn);
		tabDepartmentBtn = (Button)findViewById(R.id.department_btn);

		tabDepartmentBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				///ÄÚÈÝÎ´Ð´
				setSelTextColor(1);
				tabDepartmentBtn.setBackgroundResource(R.drawable.tt_contact_top_right_sel);
				tabALLBtn.setBackgroundResource(R.drawable.tt_contact_top_left_nor);
			}
		});

		tabALLBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				///ÄÚÈÝÎ´Ð´
				
				setSelTextColor(0);
                tabALLBtn.setBackgroundResource(R.drawable.tt_contact_top_left_sel);
                tabDepartmentBtn.setBackgroundResource(R.drawable.tt_contact_top_right_nor);
			}
		});
	}

	public void setSelTextColor(int index){
		if(0 == index){
			tabALLBtn.setTextColor(getResources().getColor(android.R.color.white));
			tabDepartmentBtn.setTextColor(getResources().getColor(R.color.default_blue_color));
		}else{
			tabDepartmentBtn.setTextColor(getResources().getColor(android.R.color.white));
			tabALLBtn.setTextColor(getResources().getColor(R.color.default_blue_color));
		}
	}
	
	public void setTabAllBtn(String title){
		tabALLBtn.setText(title);
	}
	
	public void setTabDeptBtn(String title){
		tabDepartmentBtn.setText(title);
	}
}
