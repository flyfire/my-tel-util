package com.custom.view;


import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.custom.bean.ResourceBean;

public class IndexImagePicButton extends IndexImageButtonImp{
	private static final String TAG = "IndexImageView";
	
	private GestureDetector gestureDetector=null;
	private BackgroundLinearLayout scrollView = null;
	public IndexImagePicButton(Context context,BackgroundLinearLayout scrollView,ResourceBean resourceBean) {
		super(context,resourceBean);
		this.scrollView = scrollView;
		initView();
	}
	
	int touchState = 0;
	int startTouchX = 0;
	int startTouchY = 0;
	int endTouchX = 0;
	int endTouchY = 0;
	long startTime = 0;
	long endTime = 0;
	boolean notClick = false;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getPointerCount()>1){
			return true;
		}
		if(gestureDetector!=null){
			gestureDetector.onTouchEvent(event);
			return true;
		}else{
			int action = event.getAction();
			int distance = 0;
			switch(action){
			case MotionEvent.ACTION_DOWN:
				startTouchX = (int)event.getX();
				startTouchY = (int)event.getY();
				startTime = System.currentTimeMillis();
				onBackGroundTouchEvent(event);
				notClick = false;
				return true;
			case MotionEvent.ACTION_MOVE:
				endTouchX = (int)event.getX();
				endTouchY = (int)event.getY();
				endTime = System.currentTimeMillis();
				distance = (int)Math.sqrt(Math.pow(endTouchX-startTouchX,2)+Math.pow(endTouchY-startTouchY,2));
				//Log.e(TAG,"endTime-startTime:"+(endTime-startTime)+":distance:"+distance+":notClick:"+notClick);
				onBackGroundTouchEvent(event);
				if((endTime-startTime<500&&distance>50)){
					return true;
				}else{
					return false;
				}
			case MotionEvent.ACTION_UP:
				onBackGroundTouchEvent(event);
				endTouchX = (int)event.getX();
				endTouchY = (int)event.getY();
				endTime = System.currentTimeMillis();
				distance = (int)Math.sqrt(Math.exp(endTouchX-startTouchX)+Math.exp(endTouchY-startTouchY));
				//Log.e(TAG,"endTime-startTime22:"+(endTime-startTime)+":distance:"+distance);
				if(endTime-startTime<500&&endTime-startTime>50&&distance<50){
					Toast.makeText(context, "单击事件", Toast.LENGTH_SHORT).show();
				}		
			default:
				break;
			}
			
			return false;
		}
	}

	public void onBackGroundTouchEvent(MotionEvent event){
		if(scrollView!=null){
			scrollView.onTouchEvent(event);
		}
		
	}
	
	@Override
	protected void initView() {
		super.initView();

		//如果可以移动
		if(imageCanMove){
			this.gestureDetector = new GestureDetector(new OnGestureListener() {
				@Override
				public boolean onSingleTapUp(MotionEvent e) {
					return false;
				}
				@Override
				public void onShowPress(MotionEvent e) {
				}
				@Override
				public boolean onScroll(MotionEvent e1, MotionEvent e2,
						float distanceX, float distanceY) {
					moveImage(distanceX, distanceY);
					return true;
				}
				@Override
				public void onLongPress(MotionEvent e) {
				}
				@Override
				public boolean onFling(MotionEvent e1, MotionEvent e2,
						float velocityX, float velocityY) {
					return true;
				}
				@Override
				public boolean onDown(MotionEvent e) {
					return false;
				}
			});
		
		}
	}
	
	private void moveImage(float distanceX, float distanceY){
		//Log.e(TAG,"distanceX:"+distanceX+":distanceY:"+distanceY+":scrollView.getWidth():"+scrollView.getWidth()+":scrollView.getHeight():"+scrollView.getHeight());
		AbsoluteLayout.LayoutParams alayout = (AbsoluteLayout.LayoutParams)this.getLayoutParams();
		//Log.e(TAG,"alayout.width:"+alayout.width+":alayout.height:"+alayout.height);
		alayout.x=(int)(alayout.x-distanceX);
		alayout.y=(int)(alayout.y-distanceY);
		if(alayout.x<0){
			alayout.x=0;
		}else if(alayout.x>scrollView.child.getWidth()-alayout.width){
			alayout.x=scrollView.child.getWidth()-alayout.width;
		}
		
		if(alayout.y<0){
			alayout.y=0;
		}else if(alayout.y>scrollView.child.getHeight()-alayout.height){
			alayout.y=scrollView.child.getHeight()-alayout.height;
		}
		
		//Log.e(TAG,"alayout.y:"+alayout.y+":alayout.x:"+alayout.x);
		setLayoutParams(alayout);
		MondifyIndexImageIndex.modifyImageIndexs(context, resourceBean.getBtnKey(), new int[]{alayout.x,alayout.y});
	}
}
