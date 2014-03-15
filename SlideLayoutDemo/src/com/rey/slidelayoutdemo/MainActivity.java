package com.rey.slidelayoutdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.rey.slidelayout.SlideLayout;

public class MainActivity extends Activity implements SlideLayout.OnStateChangedListener, AdapterView.OnItemClickListener {

	SlideLayout sl_top;
	SlideLayout sl_bottom;
	SlideLayout sl_activity;
	
	CheckBox cb_top;
	CheckBox cb_anim;	
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		sl_top = (SlideLayout)findViewById(R.id.main_sl_top);
		sl_bottom = (SlideLayout)findViewById(R.id.main_sl_bottom);
		cb_top = (CheckBox)findViewById(R.id.main_cb_top);
		cb_anim = (CheckBox)findViewById(R.id.main_cb_anim);
		
		cb_top.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				sl_top.setVisibility(isChecked ? View.VISIBLE : View.GONE);
				sl_bottom.setVisibility(isChecked ? View.GONE : View.VISIBLE);
			}
			
		});
		
		initTopSlideLayout();		
		initBottomSlideLayout();		
		initActivitySlideLayout();
	}
	
	private void initActivitySlideLayout(){
		sl_activity = new SlideLayout(this, null, R.style.SlideLayoutStyle2Demo);
		
		TextView v = (TextView)LayoutInflater.from(this).inflate(R.layout.layout_text, null);
		v.setBackgroundColor(0xFFFF0000);
		v.setText("Left Menu");
		sl_activity.addView(v);
		
		v = (TextView)LayoutInflater.from(this).inflate(R.layout.layout_text, null);
		v.setBackgroundColor(0xFF00FF00);
		v.setText("Right Menu");
		sl_activity.addView(v);
		
		v = (TextView)LayoutInflater.from(this).inflate(R.layout.layout_text, null);
		v.setBackgroundColor(0xFF0000FF);
		v.setText("Bottom Menu");
		sl_activity.addView(v);
		
		sl_activity.attachToActivity(this, true);
	}
	
	private void initTopSlideLayout(){
		ListView lv_left = (ListView)sl_top.findViewById(R.id.top_left);
		ListView lv_right = (ListView)sl_top.findViewById(R.id.top_right);
		ListView lv_top = (ListView)sl_top.findViewById(R.id.top_top);
		ListView lv_bottom = (ListView)sl_top.findViewById(R.id.top_bottom);
		
		lv_left.setAdapter(new ButtonAdapter(10, "Close left menu"));
		lv_right.setAdapter(new ButtonAdapter(10, "Close right menu"));
		lv_top.setAdapter(new ButtonAdapter(10, "Close top menu"));
		lv_bottom.setAdapter(new ButtonAdapter(10, "Close bottom menu"));
		
		lv_left.setOnItemClickListener(this);
		lv_right.setOnItemClickListener(this);
		lv_top.setOnItemClickListener(this);
		lv_bottom.setOnItemClickListener(this);
		
		Button bt_left = (Button)sl_top.findViewById(R.id.content_bt_left);
		Button bt_right = (Button)sl_top.findViewById(R.id.content_bt_right);
		Button bt_top = (Button)sl_top.findViewById(R.id.content_bt_top);
		Button bt_bottom = (Button)sl_top.findViewById(R.id.content_bt_bottom);
		
		View.OnClickListener listener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
					case R.id.content_bt_left:
						sl_top.openLeftMenu(cb_anim.isChecked());
						break;
					case R.id.content_bt_right:
						sl_top.openRightMenu(cb_anim.isChecked());
						break;
					case R.id.content_bt_top:
						sl_top.openTopMenu(cb_anim.isChecked());
						break;
					case R.id.content_bt_bottom:
						sl_top.openBottomMenu(cb_anim.isChecked());
						break;
				}
			}
		};
		
		bt_left.setOnClickListener(listener);
		bt_right.setOnClickListener(listener);
		bt_top.setOnClickListener(listener);
		bt_bottom.setOnClickListener(listener);		
		
		TextView tv = (TextView)sl_top.findViewById(R.id.content_tv);
		tv.setText("Content Above Menu");
	}
	
	private void initBottomSlideLayout(){
		ListView lv_left = (ListView)sl_bottom.findViewById(R.id.bottom_left);
		ListView lv_right = (ListView)sl_bottom.findViewById(R.id.bottom_right);
		ListView lv_top = (ListView)sl_bottom.findViewById(R.id.bottom_top);
		ListView lv_bottom = (ListView)sl_bottom.findViewById(R.id.bottom_bottom);
		
		lv_left.setAdapter(new ButtonAdapter(10, "Close left menu"));
		lv_right.setAdapter(new ButtonAdapter(10, "Close right menu"));
		lv_top.setAdapter(new ButtonAdapter(10, "Close top menu"));
		lv_bottom.setAdapter(new ButtonAdapter(10, "Close bottom menu"));
		
		lv_left.setOnItemClickListener(this);
		lv_right.setOnItemClickListener(this);
		lv_top.setOnItemClickListener(this);
		lv_bottom.setOnItemClickListener(this);
		
		Button bt_left = (Button)sl_bottom.findViewById(R.id.content_bt_left);
		Button bt_right = (Button)sl_bottom.findViewById(R.id.content_bt_right);
		Button bt_top = (Button)sl_bottom.findViewById(R.id.content_bt_top);
		Button bt_bottom = (Button)sl_bottom.findViewById(R.id.content_bt_bottom);
		
		View.OnClickListener listener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
					case R.id.content_bt_left:
						sl_bottom.openLeftMenu(cb_anim.isChecked());
						break;
					case R.id.content_bt_right:
						sl_bottom.openRightMenu(cb_anim.isChecked());
						break;
					case R.id.content_bt_top:
						sl_bottom.openTopMenu(cb_anim.isChecked());
						break;
					case R.id.content_bt_bottom:
						sl_bottom.openBottomMenu(cb_anim.isChecked());
						break;
				}
			}
		};
		
		bt_left.setOnClickListener(listener);
		bt_right.setOnClickListener(listener);
		bt_top.setOnClickListener(listener);
		bt_bottom.setOnClickListener(listener);		
		
		TextView tv = (TextView)sl_bottom.findViewById(R.id.content_tv);
		tv.setText("Content Below Menu");
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		switch (parent.getId()) {
			case R.id.top_left:
				sl_top.closeLeftMenu(cb_anim.isChecked());
				break;
			case R.id.top_right:
				sl_top.closeRightMenu(cb_anim.isChecked());
				break;
			case R.id.top_top:
				sl_top.closeTopMenu(cb_anim.isChecked());
				break;
			case R.id.top_bottom:
				sl_top.closeBottomMenu(cb_anim.isChecked());
				break;
			case R.id.bottom_left:
				sl_bottom.closeLeftMenu(cb_anim.isChecked());
				break;
			case R.id.bottom_right:
				sl_bottom.closeRightMenu(cb_anim.isChecked());
				break;
			case R.id.bottom_top:
				sl_bottom.closeTopMenu(cb_anim.isChecked());
				break;
			case R.id.bottom_bottom:
				sl_bottom.closeBottomMenu(cb_anim.isChecked());
				break;
		}
	}

	
	@Override
	public void onStateChanged(View v, int old_state, int new_state) {
	}
	
	@Override
	public void onOffsetChanged(View v, float offsetX, float offsetY, int state) {		
	}
	
	class ButtonAdapter extends BaseAdapter{

		int count;
		String text;
		
		public ButtonAdapter(int count, String text){
			this.count = count;
			this.text = text;
		}
		
		@Override
		public int getCount() {
			return count;
		}

		@Override
		public Object getItem(int position) {
			return text;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if(v == null)
				v = LayoutInflater.from(MainActivity.this).inflate(R.layout.row_menu, null);
			
			((TextView)v).setText(text);
			return v;
		}
		
	}
}
