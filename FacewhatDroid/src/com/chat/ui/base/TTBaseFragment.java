package com.chat.ui.base;


import com.chat.R;
import com.chat.ui.SearchActivity;
import com.chat.ui.widget.SearchEditText;
import com.chat.ui.widget.TopTabButton;
import com.chat.utils.ScreenTools;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public abstract class TTBaseFragment extends Fragment{

	protected ImageView topLeftBtn;
	protected ImageView topRightBtn;
	
	protected ImageView topSearchBtn;
	
	protected TextView topTitleTxt;
	protected TextView topLetTitleTxt;
	protected TextView topRightTitleTxt;

	protected ViewGroup topBar;
	protected TopTabButton topContactTitle;
	protected SearchEditText topSearchEdt;
	protected ViewGroup topContentView;
	protected RelativeLayout topLeftContainerLayout;
	protected FrameLayout searchFrameLayout;
	protected FrameLayout topContactFrame;
	
	protected float x1, y1, x2, y2 = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		topContentView = (ViewGroup) LayoutInflater
				.from(getActivity()).inflate(R.layout.tt_fragment_base, null);

		topBar = (ViewGroup) topContentView.findViewById(R.id.tt_fragment_base_topbar);
		topTitleTxt = (TextView) topContentView.findViewById(R.id.base_fragment_title);
		topLetTitleTxt = (TextView) topContentView.findViewById(R.id.left_txt);
		topRightTitleTxt = (TextView) topContentView.findViewById(R.id.right_txt);
		topLeftBtn = (ImageView) topContentView.findViewById(R.id.left_btn);
		topRightBtn = (ImageView) topContentView.findViewById(R.id.right_btn);
		topSearchBtn = (ImageView) topContentView.findViewById(R.id.search_btn);
		topContactTitle = (TopTabButton) topContentView.findViewById(R.id.contact_tile);
		topSearchEdt = (SearchEditText) topContentView.findViewById(R.id.chat_title_search);
		topLeftContainerLayout=(RelativeLayout)topContentView.findViewById(R.id.top_left_container);
		searchFrameLayout = (FrameLayout)topContentView.findViewById(R.id.searchbar);
		topContactFrame = (FrameLayout)topContentView.findViewById(R.id.contactTopBar);
		
		topTitleTxt.setVisibility(View.GONE);
		topRightBtn.setVisibility(View.GONE);
		topLeftBtn.setVisibility(View.GONE);
		topLetTitleTxt.setVisibility(View.GONE);
		topRightTitleTxt.setVisibility(View.GONE);
		topContactTitle.setVisibility(View.GONE);
		topSearchEdt.setVisibility(View.GONE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup vg,
			Bundle bundle) {
		if (null != topContentView) {
			((ViewGroup) topContentView.getParent()).removeView(topContentView);
			return topContentView;
		}
		return topContentView;
	}

	protected void setTopTitleBold(String title) {
		if (title == null) {
			return;
		}
		if (title.length() > 12) {
			title = title.substring(0, 11) + "...";
		}
		// 设置字体为加粗
		TextPaint paint =  topTitleTxt.getPaint();  
		paint.setFakeBoldText(true); 
		
		topTitleTxt.setText(title);
		topTitleTxt.setVisibility(View.VISIBLE);
				
	}
	
	protected void setTopTitle(String title) {
		if (title == null) {
			return;
		}
		if (title.length() > 12) {
			title = title.substring(0, 11) + "...";
		}
		topTitleTxt.setText(title);
		topTitleTxt.setVisibility(View.VISIBLE);
	}

	protected void hideTopTitle() {
		topTitleTxt.setVisibility(View.GONE);
	}

	protected void showContactTopBar() {
		topContactFrame.setVisibility(View.VISIBLE);
		topContactTitle.setVisibility(View.VISIBLE);
	}

	protected void setTopLeftButton(int resID) {
		if (resID <= 0) {
			return;
		}

		topLeftBtn.setImageResource(resID);
		topLeftBtn.setVisibility(View.VISIBLE);
	}

    protected void setTopLeftButtonPadding(int l,int t,int r,int b) {
        topLeftBtn.setPadding(l,t,r,b);
    }

	protected void hideTopLeftButton() {
		topLeftBtn.setVisibility(View.GONE);
	}

	protected void setTopLeftText(String text) {
		if (null == text) {
			return;
		}
		topLetTitleTxt.setText(text);
		topLetTitleTxt.setVisibility(View.VISIBLE);
	}

	protected void setTopRightText(String text) {
		if (null == text) {
			return;
		}
		topRightTitleTxt.setText(text);
		topRightTitleTxt.setVisibility(View.VISIBLE);
	}

	protected void setTopRightButton(int resID) {
		if (resID <= 0) {
			return;
		}

		topRightBtn.setImageResource(resID);
		topRightBtn.setVisibility(View.VISIBLE);
	}
	
	protected void setTopSearchButton(int resID) {
		if (resID <= 0) {
			return;
		}

		topSearchBtn.setImageResource(resID);
		topSearchBtn.setVisibility(View.VISIBLE);
		topSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showSearchView();
            }
        });
	}
	
	protected void hideTopRightButton() {
		topRightBtn.setVisibility(View.GONE);
	}
	
	protected void hideTopSearchButton() {
		topSearchBtn.setVisibility(View.GONE);
	}
	
	protected void setTopBar(int resID) {
		if (resID <= 0) {
			return;
		}
		topBar.setBackgroundResource(resID);
	}

    protected void hideTopBar()
    {
        topBar.setVisibility(View.GONE);
        LinearLayout.LayoutParams linearParams =(LinearLayout.LayoutParams) topContactFrame.getLayoutParams();
        linearParams.height = ScreenTools.instance(getActivity()).dip2px(45);
        topContactFrame.setLayoutParams(linearParams);
        topContactFrame.setPadding(0,ScreenTools.instance(getActivity()).dip2px(10),0,0);
    }

	protected void showTopSearchBar() {
		topSearchEdt.setVisibility(View.VISIBLE);
	}

	protected void hideTopSearchBar() {
		topSearchEdt.setVisibility(View.GONE);
	}
	
	protected void showSearchFrameLayout(){
		searchFrameLayout.setVisibility(View.VISIBLE);
        /**还是以前的页面，没有看psd是否改过*/
        searchFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showSearchView();
            }
        });
	}

	@Override
	public void onActivityCreated(Bundle bundle) {
		super.onActivityCreated(bundle);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	protected void initSearch() {
		setTopRightButton(R.drawable.right_btn_add);
	}

	public void showSearchView() {
		startActivity(new Intent(getActivity(), SearchActivity.class));
	}
	
	protected void onSearchDataReady() {
		initSearch();
	}
}