package com.skyeyes.storemonitor.activity;

import com.skyeyes.base.view.TopTitleView;
import com.skyeyes.base.view.TopTitleView.OnClickListenerCallback;
import com.skyeyes.storemonitor.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
/** 设备状态*/
public class DevicesStatusActivity extends Activity {
	private TopTitleView topTitleView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.devices_status_view);
		topTitleView = (TopTitleView) findViewById(R.id.ds_topView);
		topTitleView
		.setOnMenuButtonClickListener(new OnClickListenerCallback() {

			@Override
			public void onClick() {
				// TODO Auto-generated method stub
				HomeActivity.getInstance().toggleMenu();

			}
		});
	}
}
