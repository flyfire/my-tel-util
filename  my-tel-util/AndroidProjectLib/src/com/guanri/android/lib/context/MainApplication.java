package com.guanri.android.lib.context;

import android.app.Activity;
import android.app.Application;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.guanri.android.lib.bluetooth.BluetoothPool;
import com.guanri.android.lib.network.NetWorkTools;

/**
 * 
 * @author 杨雪平
 *
 */
public class MainApplication extends Application {
	private static MainApplication instance;
	public int screenHeight = 0;
	public int screenWidth = 0;
	
	private boolean isLogin = false;//是否已经登陆
	private Object userInfo = null;//用户信息对象
	private NetWorkTools netWorkTools = null;//网络连接状态监听工具


	public static MainApplication getInstance() {
		return instance;
	}
	/**
	 * 设置屏幕的高度和宽度
	 */
	public void setScreenW2H(Activity activity){
		WindowManager manage = activity.getWindowManager();
		Display display = manage.getDefaultDisplay();
		screenHeight = display.getHeight();
		screenWidth = display.getWidth();
	}
	/**
	 * 获取屏幕高度
	 * @return
	 */
	public int getScreenHeight(){
		return screenHeight;
	}
	/**
	 * 获取屏幕宽度
	 * @return
	 */
	public int getScreenWidth(){
		return screenWidth;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		//System.out.println("Aplication 初始化");
		instance = this;
	}
	
	
	
	public void setUserInfo(Object userInfo){
		this.isLogin = true;
		this.userInfo = userInfo;
	}
	
	public void logout(){
		this.isLogin = false;
		this.userInfo = null;
		BluetoothPool.getInstance().releasBluetooth();
	}
	
	public boolean isLogin() {
		return isLogin;
	}
	
	public Object getUserInfo() {
		return userInfo;
	}
	
	/**
	 * 开始监听网络状态
	 * 
	 * 可通过handler获取状态变更情况
	 * 也可以通过监听 NetWorkTools.ACTION_CONNECTION_STATUS_CHANGE广播获取网络变更情况
	 * 
	 * @param handler
	 */
	public boolean startNetWorkListen(Handler handler){
		if(netWorkTools!=null){
			try{
				netWorkTools.cancelMmonitor();
			}catch(Exception e){
				
			}
		}
		netWorkTools = new NetWorkTools(this,handler);
		return true;
	}
	/**
	 * 停止监听网络状态
	 * @param handler
	 */
	public boolean stopNetWorkListen(){
		if(netWorkTools!=null){
			netWorkTools.cancelMmonitor();
			return true;
		}else{
			return false;
		}
	}	
	
	/**
	 * 打开网络连接
	 * @param handler
	 */
	public boolean createConnection(){
		if(netWorkTools!=null){
			netWorkTools.autoConnect();
			return true;
		}else{
			return false;
		}
	}
	
}
