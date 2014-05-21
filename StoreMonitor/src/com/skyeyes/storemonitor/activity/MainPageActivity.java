package com.skyeyes.storemonitor.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.skyeyes.base.BaseSocketHandler;
import com.skyeyes.base.activity.BaseActivity;
import com.skyeyes.base.cmd.CommandControl.REQUST;
import com.skyeyes.base.cmd.bean.ReceiveCmdBean;
import com.skyeyes.base.cmd.bean.impl.ReceivLogin;
import com.skyeyes.base.cmd.bean.impl.ReceiveChannelPic;
import com.skyeyes.base.cmd.bean.impl.ReceiveDeviceChannelListStatus;
import com.skyeyes.base.cmd.bean.impl.ReceiveReadDeviceList;
import com.skyeyes.base.cmd.bean.impl.ReceiveRealVideo;
import com.skyeyes.base.cmd.bean.impl.ReceiveVideoData;
import com.skyeyes.base.cmd.bean.impl.SendObjectParams;
import com.skyeyes.base.exception.CommandParseException;
import com.skyeyes.base.exception.NetworkException;
import com.skyeyes.base.network.impl.SkyeyeSocketClient;
import com.skyeyes.base.util.PreferenceUtil;
import com.skyeyes.base.util.StringUtil;
import com.skyeyes.storemonitor.R;
import com.skyeyes.storemonitor.process.DeviceProcessInterface.DeviceReceiveCmdProcess;
import com.skyeyes.storemonitor.service.DevicesService;

public class MainPageActivity extends BaseActivity{
	String TAG = "MainPageActivity";
	ImageView app_left_menu_iv = null;
	TextView store_login_id_tv = null;
	LinearLayout layout_root;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		this.setContentView(R.layout.app_video_page);
		layout_root = (LinearLayout)findViewById(R.id.layout_root);
		app_left_menu_iv = (ImageView)findViewById(R.id.app_left_menu_iv);
		store_login_id_tv = (TextView)findViewById(R.id.store_login_id_tv);
		
		app_left_menu_iv.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				HomeActivity.getInstance().toggleMenu();
			}
			
		});
		

	}
    
    
    public void onResume(){
    	super.onResume();
    	if(DevicesService.getInstance() == null){
    		Log.i(TAG,"onResume()");
    		String userName = PreferenceUtil.getConfigString(PreferenceUtil.ACCOUNT_IFNO, PreferenceUtil.account_login_name);
    		
    		String userPsd = PreferenceUtil.getConfigString(PreferenceUtil.ACCOUNT_IFNO, PreferenceUtil.account_login_psd);
    		
    		String ip = PreferenceUtil.getConfigString(PreferenceUtil.SYSCONFIG, PreferenceUtil.sysconfig_server_ip);
    		
    		String port = PreferenceUtil.getConfigString(PreferenceUtil.ACCOUNT_IFNO, PreferenceUtil.account_login_psd);
    		
    		if(StringUtil.isNull(userName)||StringUtil.isNull(userPsd)||StringUtil.isNull(ip)||StringUtil.isNull(port)){
    			store_login_id_tv.setText("用户数据不完整，请前往设置用户数据...............");
    		}else{
        		SkyeyeSocketClient skyeyeSocketClient = null;
        		try {
        			skyeyeSocketClient = new SkyeyeSocketClient(new SocketHandlerImpl(), true);
        		} catch (NetworkException e1) {
        			// TODO Auto-generated catch block
        			e1.printStackTrace();
        		}
        		queryEquitListNoLogin(skyeyeSocketClient);//查询设备
        		
        		store_login_id_tv.setText("正在登陆...............");
    		}
    	}
    }
    
    public void onDestroy(){
    	super.onDestroy();
    }
    
	// 查询设备列表
	public void queryEquitListNoLogin(
			SkyeyeSocketClient skyeyeSocketClient) {
		SendObjectParams sendObjectParams = new SendObjectParams();
		
		String userName = PreferenceUtil.getConfigString(PreferenceUtil.ACCOUNT_IFNO, PreferenceUtil.account_login_name);
		
		String userPsd = PreferenceUtil.getConfigString(PreferenceUtil.ACCOUNT_IFNO, PreferenceUtil.account_login_psd);

		Object[] params = new Object[] { userName, userPsd };
		//params = new Object[]{};
		try {

			sendObjectParams.setParams(REQUST.cmdUserEquitListNOLogin, params);

			Log.i(TAG,"testEquitListNoLogin入参数："+ sendObjectParams.toString());
		} catch (CommandParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			skyeyeSocketClient.sendCmd(sendObjectParams);
		} catch (NetworkException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
    
    
	private class SocketHandlerImpl extends BaseSocketHandler {
		
		public SocketHandlerImpl(){
			super();
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onReceiveCmdEx(final ReceiveCmdBean receiveCmdBean) {
			// TODO Auto-generated method stub
			Log.i("MainPageActivity","解析报文成功:" + (receiveCmdBean!=null?receiveCmdBean.toString():"receiveCmdBean is null"));
			if (receiveCmdBean instanceof ReceiveReadDeviceList) {
				if(((ReceiveReadDeviceList) receiveCmdBean).getCommandHeader().cmdCode == 0){
					final ReceiveReadDeviceList receiveReadDeviceList = ((ReceiveReadDeviceList) receiveCmdBean);
					PreferenceUtil.setSingleConfigInfo(PreferenceUtil.DEVICE_INFO,
							PreferenceUtil.device_count, receiveReadDeviceList.deviceCodeList.size());
					PreferenceUtil.setSingleConfigInfo(PreferenceUtil.DEVICE_INFO,
							PreferenceUtil.device_code_list,  receiveReadDeviceList.deviceListString);
					store_login_id_tv.setText(receiveReadDeviceList.deviceListString);	
					
					MainPageActivity.this.startService(new Intent(MainPageActivity.this,DevicesService.class));
					

					new Thread(){
						public void run(){
							while(true){
								if(DevicesService.getInstance() != null){
									if(DevicesService.getInstance().getCurrentDeviceCode() == null){
										DevicesService.getInstance().selectDevice(receiveReadDeviceList.deviceCodeList.get(0));
										DevicesService.getInstance().registerCmdProcess(ReceivLogin.class.getSimpleName(), new LoginReceive());
										DevicesService.getInstance().registerCmdProcess(ReceiveDeviceChannelListStatus.class.getSimpleName(),
												new DeviceChannelListStatusReceive());
									}
									
									if(DevicesService.getInstance().getDeviceDeviceProcesss().containsKey(
											receiveReadDeviceList.deviceCodeList.get(0))){
										runOnUiThread(new Runnable(){
											public void run(){
												store_login_id_tv.setText("登陆成功...............");
											}
										});
										break;
									}
								}
								try {
									sleep(10);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}.start();
					
					Log.i("MainPageActivity", "DevicesService.getInstance()==null:"+(DevicesService.getInstance()==null));
					

//					for(final String deviceCode:((ReceiveReadDeviceList) receiveCmdBean).deviceCodeList){
//						Button button = new Button(MainPageActivity.this);
//						button.setText(deviceCode);
//						LinearLayout.LayoutParams lp = 
//								new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
//										LinearLayout.LayoutParams.WRAP_CONTENT); 
//						((LinearLayout)MainPageActivity.this.findViewById(R.id.layout_root)).addView(button,lp);
//						button.setOnClickListener(new OnClickListener(){
//							@Override
//							public void onClick(View arg0) {
//								// TODO Auto-generated method stub
//								DevicesService.getInstance().selectDevice(deviceCode);
//								Intent intent = new Intent(MainPageActivity.this,RealTimeVideoActivity.class);
//								MainPageActivity.this.startActivity(intent);
//							}
//						});
//					}
					
				}
			}else{
				Toast.makeText(MainPageActivity.this, "查询失败："+((ReceiveReadDeviceList) receiveCmdBean).getCommandHeader().errorInfo, Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onCmdExceptionEx(CommandParseException ex) {
			// TODO Auto-generated method stub
			Toast.makeText(MainPageActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onSocketExceptionEx(NetworkException ex) {
			// TODO Auto-generated method stub
			Toast.makeText(MainPageActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();

		}

		@Override
		public void onSocketClosedEx() {
			// TODO Auto-generated method stub
			//System.out.println("测试连接失败:onFailure");
			//Toast.makeText(MainPageActivity.this, "测试连接失败:onFailure", Toast.LENGTH_SHORT).show();

		}

	};
	
	private class LoginReceive extends DeviceReceiveCmdProcess<ReceivLogin>{

		@Override
		public void onProcess(ReceivLogin receiveCmdBean) {
			// TODO Auto-generated method stub
			if(receiveCmdBean.getCommandHeader().resultCode != 0){
				store_login_id_tv.setText(receiveCmdBean.getCommandHeader().errorInfo);

			}else{
				store_login_id_tv.setText("登陆成功11111...............");
				VideoDataReceive video = new VideoDataReceive();
				DevicesService.getInstance().registerCmdProcess(ReceiveVideoData.class.getSimpleName(), video);
				
				
				SendObjectParams sendObjectParams = new SendObjectParams();
				Object[] params = new Object[] { 0x00 };
				try {
					sendObjectParams.setParams(REQUST.cmdReqRealVideo, params);
					System.out.println("cmdReqRealVideo入参数：" + sendObjectParams.toString());
					
					DevicesService.sendCmd(sendObjectParams, new RealVideoReceive());
				} catch (CommandParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
				
		}

		@Override
		public void onFailure(String errinfo) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class DeviceChannelListStatusReceive extends DeviceReceiveCmdProcess<ReceiveDeviceChannelListStatus>{

		@Override
		public void onProcess(ReceiveDeviceChannelListStatus receiveCmdBean) {
			// TODO Auto-generated method stub
			store_login_id_tv.setText("通道状态："+receiveCmdBean.toString());
			//查询通道图片
			SendObjectParams sendObjectParams = new SendObjectParams();
			Object[] params = new Object[] {(byte)0x00};
			try {
				sendObjectParams.setParams(REQUST.cmdReqVideoChannelPic, params);
				System.out.println("getChannelPic入参数：" + sendObjectParams.toString());
				
				DevicesService.sendCmd(sendObjectParams, new ChannelPicReceive());
			} catch (CommandParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void onFailure(String errinfo) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class ChannelPicReceive extends DeviceReceiveCmdProcess<ReceiveChannelPic>{

		@Override
		public void onProcess(ReceiveChannelPic receiveCmdBean) {
			// TODO Auto-generated method stub
			Log.i("MainPageActivity", "ChannelPicReceive================");
			store_login_id_tv.setText("");
			//获得通道图片
			Bitmap pic = BitmapFactory.decodeByteArray(receiveCmdBean.pic, 0, receiveCmdBean.pic.length);
			ImageView iv = new ImageView(MainPageActivity.this);
			iv.setImageBitmap(pic);
			
			LinearLayout.LayoutParams ly = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
			
			layout_root.addView(iv,ly);
			
			iv.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					Log.i("MainPageActivity", "iv.setOnClickListener(new OnClickListener()================");
					VideoDataReceive video = new VideoDataReceive();
					DevicesService.getInstance().registerCmdProcess(ReceiveVideoData.class.getSimpleName(), video);
					
					
					SendObjectParams sendObjectParams = new SendObjectParams();
					Object[] params = new Object[] { 0x00 };
					try {
						sendObjectParams.setParams(REQUST.cmdReqRealVideo, params);
						System.out.println("cmdReqRealVideo入参数：" + sendObjectParams.toString());
						
						DevicesService.sendCmd(sendObjectParams, new RealVideoReceive());
					} catch (CommandParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			});
		}

		@Override
		public void onFailure(String errinfo) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	
	private class RealVideoReceive extends DeviceReceiveCmdProcess<ReceiveRealVideo>{

		@Override
		public void onProcess(ReceiveRealVideo receiveCmdBean) {
			// TODO Auto-generated method stub
			//打开视频播放界面
			Intent it = new Intent(MainPageActivity.this,VideoPlayActivity.class);
			MainPageActivity.this.startActivity(it);
			Log.i("MainPageActivity", "MainPageActivity.this.startActivity(it)================");
		}

		@Override
		public void onFailure(String errinfo) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class VideoDataReceive extends DeviceReceiveCmdProcess<ReceiveVideoData>{

		@Override
		public void onProcess(ReceiveVideoData receiveCmdBean) {
			Log.e("MainPageActivity", "VideoDataReceive================");
			// TODO Auto-generated method stub
			if(VideoPlayActivity.getInstance()!=null)
				VideoPlayActivity.getInstance().sendStream(receiveCmdBean.data);
		}

		@Override
		public void onFailure(String errinfo) {
			// TODO Auto-generated method stub
			
		}
		
	}

}