package com.chat.ui;


import com.chat.R;
import com.chat.utils.IMStackManager;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class SearchActivity extends FragmentActivity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//»Î’ª
//		IMStackManager.getStackManager().pushActivity(this);
		setContentView(R.layout.tt_activity_search);
	}

	protected void onStart() {
		super.onStart();
	}


	protected void onDestroy() {
		//≥ˆ’ª
//		IMStackManager.getStackManager().popActivity(this);
		super.onDestroy();
	}

}
