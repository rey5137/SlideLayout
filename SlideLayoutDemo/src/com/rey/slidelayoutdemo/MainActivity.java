package com.rey.slidelayoutdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.rey.slidelayout.SlideLayout;

public class MainActivity extends Activity implements SlideLayout.OnStateChangedListener {

	SlideLayout sl_top;
	SlideLayout sl_bottom;
	SlideLayout sl_cur;
	
	CheckBox cb_top;
	CheckBox cb_anim;
	Button bt_left;
	Button bt_right;
	Button bt_top;
	Button bt_bottom;
	
	
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		sl_top = (SlideLayout)findViewById(R.id.main_sl_top);
		sl_bottom = (SlideLayout)findViewById(R.id.main_sl_bottom);
		cb_top = (CheckBox)findViewById(R.id.main_cb_top);
		cb_anim = (CheckBox)findViewById(R.id.main_cb_anim);
		bt_left = (Button)findViewById(R.id.main_bt_left);
		bt_right = (Button)findViewById(R.id.main_bt_right);
		bt_top = (Button)findViewById(R.id.main_bt_top);
		bt_bottom = (Button)findViewById(R.id.main_bt_bottom);
		
		sl_top.setOnStateChangedListener(this);
		sl_cur = sl_top;
		sl_bottom.setVisibility(View.GONE);
		
		cb_top.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					sl_bottom.setOnStateChangedListener(null);
					sl_bottom.closeAllMenu(false);
					sl_bottom.setVisibility(View.GONE);
					
					sl_top.setOnStateChangedListener(MainActivity.this);
					sl_top.setVisibility(View.VISIBLE);
					sl_cur = sl_top;
				}
				else{					
					sl_top.setOnStateChangedListener(null);
					sl_top.closeAllMenu(false);
					sl_top.setVisibility(View.GONE);
					
					sl_bottom.setOnStateChangedListener(MainActivity.this);
					sl_bottom.setVisibility(View.VISIBLE);
					sl_cur = sl_bottom;
				}
				
				
			}
		});
		
		bt_left.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {				
				boolean showContent = sl_cur.isState(SlideLayout.ACTION_SHOW, SlideLayout.TARGET_CONTENT);
				
				if(showContent)
					sl_cur.openLeftMenu(cb_anim.isChecked());
				else
					sl_cur.closeLeftMenu(cb_anim.isChecked());
				
				setButton(sl_cur.getState());
			}
		});
		
		bt_right.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {				
				boolean showContent = sl_cur.isState(SlideLayout.ACTION_SHOW, SlideLayout.TARGET_CONTENT);
				
				if(showContent)
					sl_cur.openRightMenu(cb_anim.isChecked());
				else
					sl_cur.closeRightMenu(cb_anim.isChecked());
				
				setButton(sl_cur.getState());
			}
		});
		
		bt_top.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {				
				boolean showContent = sl_cur.isState(SlideLayout.ACTION_SHOW, SlideLayout.TARGET_CONTENT);
				
				if(showContent)
					sl_cur.openTopMenu(cb_anim.isChecked());
				else
					sl_cur.closeTopMenu(cb_anim.isChecked());
				
				setButton(sl_cur.getState());
			}
		});
		
		bt_bottom.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {				
				boolean showContent = sl_cur.isState(SlideLayout.ACTION_SHOW, SlideLayout.TARGET_CONTENT);
				
				if(showContent)
					sl_cur.openBottomMenu(cb_anim.isChecked());
				else
					sl_cur.closeBottomMenu(cb_anim.isChecked());

				setButton(sl_cur.getState());
			}
		});
	}

	private void setButton(int state){
		int action = SlideLayout.getStateAction(state);
		int direction = SlideLayout.getStateTarget(state);
		
		if(action == SlideLayout.ACTION_SHOW && direction == SlideLayout.TARGET_CONTENT){
			bt_left.setEnabled(true);
			bt_right.setEnabled(true);
			bt_top.setEnabled(true);
			bt_bottom.setEnabled(true);
			bt_left.setText("Open left");
			bt_right.setText("Open right");
			bt_top.setText("Open top");
			bt_bottom.setText("Open bottom");
		}
		else{
			bt_left.setEnabled(direction == SlideLayout.TARGET_LEFT);
			bt_left.setText((action == SlideLayout.ACTION_SHOW && bt_left.isEnabled()) ? "Close left" : "Open left");
			
			bt_right.setEnabled(direction == SlideLayout.TARGET_RIGHT);
			bt_right.setText((action == SlideLayout.ACTION_SHOW && bt_right.isEnabled()) ? "Close right" : "Open right");
			
			bt_top.setEnabled(direction == SlideLayout.TARGET_TOP);
			bt_top.setText((action == SlideLayout.ACTION_SHOW && bt_top.isEnabled()) ? "Close top" : "Open top");
			
			bt_bottom.setEnabled(direction == SlideLayout.TARGET_BOTTOM);
			bt_bottom.setText((action == SlideLayout.ACTION_SHOW && bt_bottom.isEnabled()) ? "Close bottom" : "Open bottom");
		}
	}
	
	@Override
	public void onStateChanged(View v, int old_state, int new_state) {
		setButton(new_state);
	}
	
	@Override
	public void onOffsetChanged(View v, float offsetX, float offsetY, int state) {		
	}
}
