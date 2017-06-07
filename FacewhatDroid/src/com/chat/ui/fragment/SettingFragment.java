package com.chat.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.chat.R;
import com.chat.ui.base.TTBaseFragment;

public class SettingFragment extends TTBaseFragment {
	private View curView = null;
	private CheckBox notificationNoDisturbCheckBox;
	private CheckBox notificationGotSoundCheckBox;
	private CheckBox notificationGotVibrationCheckBox;
//	CheckboxConfigHelper checkBoxConfiger = new CheckboxConfigHelper();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (null != curView) {
			((ViewGroup) curView.getParent()).removeView(curView);
			return curView;
		}
		curView = inflater.inflate(R.layout.tt_fragment_setting, topContentView);
		initRes();
		return curView;
	}
	
	private void initRes() {
		// …Ë÷√±ÍÃ‚¿∏
//		setTopTitle(getActivity().getString(R.string.setting_page_name));
		setTopLeftButton(R.drawable.tt_top_back);
		topLeftContainerLayout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				getActivity().finish();
			}
		});
		setTopLeftText(getResources().getString(R.string.top_left_back));
	}
	
	
	private void initOptions() {
		notificationNoDisturbCheckBox = (CheckBox) curView.findViewById(R.id.NotificationNoDisturbCheckbox);
		notificationGotSoundCheckBox = (CheckBox) curView.findViewById(R.id.notifyGotSoundCheckBox);
		notificationGotVibrationCheckBox = (CheckBox) curView.findViewById(R.id.notifyGotVibrationCheckBox);
//		saveTrafficModeCheckBox = (CheckBox) curView.findViewById(R.id.saveTrafficCheckBox);
		
//		checkBoxConfiger.initCheckBox(notificationNoDisturbCheckBox, SysConstant.SETTING_GLOBAL, ConfigurationSp.CfgDimension.NOTIFICATION );
//		checkBoxConfiger.initCheckBox(notificationGotSoundCheckBox, SysConstant.SETTING_GLOBAL , ConfigurationSp.CfgDimension.SOUND);
//		checkBoxConfiger.initCheckBox(notificationGotVibrationCheckBox, SysConstant.SETTING_GLOBAL,ConfigurationSp.CfgDimension.VIBRATION );
//		checkBoxConfiger.initCheckBox(saveTrafficModeCheckBox, ConfigDefs.SETTING_GLOBAL, ConfigDefs.KEY_SAVE_TRAFFIC_MODE, ConfigDefs.DEF_VALUE_SAVE_TRAFFIC_MODE);
	}

}
