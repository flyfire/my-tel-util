package com.custom.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Context;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.GestureDetector.OnGestureListener;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.custom.bean.PageNumBean;
import com.custom.bean.ResourceBean;
import com.custom.utils.Logger;

public class SecondViewGroup extends ViewGroup {

	private static final Logger logger = Logger.getLogger(SecondViewGroup.class);

	private Scroller scroller;

	private int currentScreenIndex;

	private GestureDetector gestureDetector;
	
	private Context context = null;
	

	// 设置一个标志位，防止底层的onTouch事件重复处理UP事件
	private boolean fling;


	public Scroller getScroller() {
		return scroller;
	}


	public SecondViewGroup(Context context,ArrayList<Entry<String,ResourceBean>> resourceInfo,PageNumView pageNumView) {
		super(context);
		this.resourceInfo = resourceInfo;
		this.pageNumView = pageNumView;
		this.pageNumBean = pageNumView.getPageNumBean();
		this.context = context;
		
		WindowManager manage = ((Activity)context).getWindowManager();
		Display display = manage.getDefaultDisplay();
		screenHeight = display.getHeight();
		screenWidth = display.getWidth();
		
		initView(context);
	}

	private void initView(final Context context) {
		
		
		this.scroller = new Scroller(context);

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
				
				if(distanceX > 0 && currentScreenIndex == getChildCount() - 1){//移动过最后一页
					if(pageNumBean.nextPageView()){
						SecondViewGroup.this.createIndexButton();
						pageNumView.initPageNumView();
						logger.error( "on scroll>>>>>>>>>>>>>>>>>向后移动<<<<<<<<<<<<<<>>>");
					}
					
				}else if(distanceX < 0 && getScrollX() < 0){//向第一页之前移动
					if(pageNumBean.prePageView()){
						SecondViewGroup.this.createIndexButton();
						pageNumView.initPageNumView();
						logger.error( "on scroll>>>>>>>>>>>>>>>>>向前移动<<<<<<<<<<<<<<>>>");
					}
					
				}else if ((distanceX > 0 && currentScreenIndex < getChildCount() - 1)// 防止移动过最后一页
						|| (distanceX < 0 && getScrollX() > 0)) {// 防止向第一页之前移动
					scrollBy((int) distanceX, 0);
					logger.error( "on scroll>>>>>>>>>>>>>>>>>防止向第一页之前移动<<<<<<<<<<<<<<>>>");
				}
				
				return true;
			}

			@Override
			public void onLongPress(MotionEvent e) {
			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2,
					float velocityX, float velocityY) {
				logger.error( "min velocity >>>"
						+ ViewConfiguration.get(context)
								.getScaledMinimumFlingVelocity()
						+ " current velocity>>" + velocityX);
				if (Math.abs(velocityX) > ViewConfiguration.get(context)
						.getScaledMinimumFlingVelocity()) {// 判断是否达到最小轻松速度，取绝对值的
					if (velocityX > 0 && currentScreenIndex > 0) {
						logger.error( ">>>>fling to left");
						fling = true;
						scrollToScreen(currentScreenIndex - 1);
					} else if (velocityX < 0
							&& currentScreenIndex < getChildCount() - 1) {
						logger.error( ">>>>fling to right");
						fling = true;
						scrollToScreen(currentScreenIndex + 1);
					}
				}

				return true;
			}

			@Override
			public boolean onDown(MotionEvent e) {
				return false;
			}
		});
		
		createIndexButton();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		logger.error( ">>>>>>>>>>>>>>>>>>>>left: " + left + " top: " + top + " right: " + right
				+ " bottom:" + bottom);

		/**
		 * 设置布局，将子视图顺序横屏排列
		 */
		
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			child.setVisibility(View.VISIBLE);
			child.measure(right - left, bottom - top);
			child.layout(0 + i * getWidth(), 0, getWidth() + i * getWidth(),
					getHeight());
		}
	}

	@Override
	public void computeScroll() {
		if (scroller.computeScrollOffset()) {
			//logger.error( ">>>>>>>>>>computeScroll>>>>>"+scroller.getCurrX());

			scrollTo(scroller.getCurrX(), 0);
			postInvalidate();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		gestureDetector.onTouchEvent(event);

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
			//logger.error( ">>ACTION_UP:>>>>>>>> MotionEvent.ACTION_UP>>>>>");
			if (!fling) {
				snapToDestination();
			}
			fling = false;
			break;
		default:
			break;
		}
		return true;
	}

	/**
	 * 切换到指定屏
	 * 
	 * @param whichScreen
	 */
	public void scrollToScreen(int whichScreen) {
		if (getFocusedChild() != null && whichScreen != currentScreenIndex
				&& getFocusedChild() == getChildAt(currentScreenIndex)) {
			getFocusedChild().clearFocus();
		}

		final int delta = whichScreen * getWidth() - getScrollX();
		scroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta) * 2);
		invalidate();

		if(currentScreenIndex>whichScreen&&pageNumBean.prePageNum()){
			pageNumView.initPageNumView();
		}else if(currentScreenIndex<whichScreen&&pageNumBean.nextPageNum()){
			pageNumView.initPageNumView();
		}
		
		
		currentScreenIndex = whichScreen;
		

	}

	/**
	 * 根据当前x坐标位置确定切换到第几屏
	 */
	private void snapToDestination() {
		scrollToScreen((getScrollX() + (getWidth() / 2)) / getWidth());
	}
	
	ArrayList<Entry<String,ResourceBean>> resourceInfo = null;
	private ArrayList<View> pageNumViews = new ArrayList<View>();
	PageNumBean pageNumBean=null;
	PageNumView pageNumView = null;
	int screenHeight = 0;
	int screenWidth = 0;
	protected void createIndexButton() {
		//this.removeAllViews();
		while(pageNumViews.size()>0){
			try{
				//this.removeAllViews();
			    this.removeViewInLayout(pageNumViews.remove(0));
			}catch(Exception e){
				
			}
			
		}
		//this.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT) );
		for(int pageNum=pageNumBean.getStartPageNum();pageNum<=pageNumBean.getEndPageNum();pageNum++){
			LinearLayout.LayoutParams pageLayoutParams = new LinearLayout.LayoutParams(
					screenWidth, screenHeight);
			AbsoluteLayout pageLayout = new AbsoluteLayout(context);
			pageLayout.setLayoutParams(pageLayoutParams);
			this.addView(pageLayout);
			int[] index = pageNumBean.getButtonIndexbyPageNum(pageNum);
			logger.error("index:"+Arrays.toString(index)+":"+pageNumBean.getButtonCount());
			
			for(int i=index[0];i<=index[1];i++){
				ResourceBean resourceBean = resourceInfo.get(i).getValue();
				IndexImageButtonImp imageView = null;
				setXY(resourceBean,i);
				imageView = new IndexImagePicButton(context,null,resourceBean);
				pageLayout.addView(imageView);
			}
			//pageNumViews.add(pageLayout);
		}

	}
	protected void setXY(ResourceBean resourceBean,int buttonIndex) {
		//设置图标的位置
		// TODO Auto-generated method stub
		//int[] indexs = MondifyIndexImageIndex.getImageIndexs(resourceBean.getBtnKey());
		resourceBean.setX(buttonIndex%4*200+50);
		resourceBean.setY(buttonIndex/4%2*200+150);
	}

}
