package com.skyeyes.storemonitor.activity.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skyeyes.base.bean.OpenCloseDoorInfoBean;
import com.skyeyes.base.util.DateUtil;
import com.skyeyes.storemonitor.R;
import com.skyeyes.storemonitor.activity.VideoPlayActivity;

public class DoorRecordViewAdapter extends BaseAdapter {
	ArrayList<OpenCloseDoorInfoBean> list;
    LayoutInflater inflater;
    Context mContext;
    public DoorRecordViewAdapter(Context context,ArrayList<OpenCloseDoorInfoBean> list) {
    	mContext = context;
        this.list=list;
        this.inflater=LayoutInflater.from(context);
    }

    
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        CacheView cacheView;
        if(convertView==null){
        	cacheView=new CacheView();
            convertView=inflater.inflate(R.layout.door_record_item_page, null);
            cacheView.open_ll=(LinearLayout) convertView.findViewById(R.id.door_record_open);
            cacheView.close_ll=(LinearLayout) convertView.findViewById(R.id.door_record_close);
            cacheView.open_des=(TextView) convertView.findViewById(R.id.door_record_open_time);
            cacheView.close_des=(TextView) convertView.findViewById(R.id.door_record_close_time);            
            convertView.setTag(cacheView);
        }else{
            cacheView=(CacheView) convertView.getTag();
        }
        
        LinearLayout ll = null;
        if(list.get(position).type==1){
            cacheView.open_ll.setVisibility(View.VISIBLE);
            cacheView.close_ll.setVisibility(View.GONE);
            cacheView.open_des.setText(DateUtil.getTimeStringFormat(list.get(position).time, DateUtil.TIME_FORMAT_YMDHMS));
            ll = cacheView.open_ll;
        }else{
            cacheView.open_ll.setVisibility(View.GONE);
            cacheView.close_ll.setVisibility(View.VISIBLE);
            cacheView.close_des.setText(DateUtil.getTimeStringFormat(list.get(position).time, DateUtil.TIME_FORMAT_YMDHMS));
            ll = cacheView.close_ll;
        }
        
        ll.setOnClickListener(new OnClickListener(){
        	
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.i("MainPageActivity", "iv.setOnClickListener(new OnClickListener()================");
				Intent intent = new Intent(mContext, VideoPlayActivity.class);
				intent.putExtra("alarmId",list.get(position).eventCode);
				intent.putExtra("chennalId",list.get(position).chennalId);
				intent.putExtra("videoType",2);
				mContext.startActivity(intent);
			}
		});
        
        return convertView;
    }

    private static class CacheView{
        TextView open_des;
        TextView close_des;
        LinearLayout open_ll;
        LinearLayout close_ll;
    }
}
