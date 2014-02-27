package com.rey.slidelayout;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.widget.FrameLayout;

public class SlideLayout extends FrameLayout {
		
	private int action;
	private int target;
	
	public static final int ACTION_SHOW = 0x00;
	public static final int ACTION_OPEN = 0x01;	
	public static final int ACTION_CLOSE = 0x02;
	public static final int ACTION_DRAG = 0x04;
	
	public static final int TARGET_CONTENT = 0x00;
	public static final int TARGET_LEFT = 0x01;
	public static final int TARGET_RIGHT = 0x02;
	public static final int TARGET_TOP = 0x04;
	public static final int TARGET_BOTTOM = 0x08;
	
	private static final int ACTION_MASK = 0x0F;
	private static final int TARGET_MASK = 0xF0;
	
	private int viewWidth = -1;
	private int viewHeight = -1;
	
	private int leftMenuChild = -1;
	private MenuStyle leftMenuStyle;
	
	private int rightMenuChild = -1;
	private MenuStyle rightMenuStyle;
	
	private int topMenuChild = -1;
	private MenuStyle topMenuStyle;
	
	private int bottomMenuChild = -1;
	private MenuStyle bottomMenuStyle;
	
	private int leftShadowChild = -1;
	private int rightShadowChild = -1;
	private int topShadowChild = -1;
	private int bottomShadowChild = -1;
	
	private int contentChild = -1;	
	private int offsetX = 0;
	private int offsetY = 0;
	private boolean dragEnable = true;	
	private Interpolator interpolator = new SmoothInterpolator();
		
	private float downX = -1;
	private float downY = -1;
	private float prevX;
	private float prevY;
	private float disX;
	private float disY;
	private boolean startDrag = false;
	
	public interface OnStateChangedListener{
		public void onStateChanged(View v, int old_state, int new_state);
		
		public void onOffsetChanged(View v, float offsetX, float offsetY, int state);
	}
	
	private WeakReference<OnStateChangedListener> listener_state;
		
	enum OP{
		START_DRAG_LEFT_FROM_CONTENT, 
		START_DRAG_RIGHT_FROM_CONTENT, 
		START_DRAG_TOP_FROM_CONTENT, 
		START_DRAG_BOTTOM_FROM_CONTENT, 
		START_DRAG_LEFT_FROM_MENU, 
		START_DRAG_RIGHT_FROM_MENU, 
		START_DRAG_TOP_FROM_MENU, 
		START_DRAG_BOTTOM_FROM_MENU, 
		DRAG_LEFT_END, 
		DRAG_RIGHT_END, 
		DRAG_TOP_END, 
		DRAG_BOTTOM_END, 
		FLING_LEFT, 
		FLING_RIGHT, 
		FLING_TOP, 
		FLING_BOTTOM, 
		OPEN_LEFT,  
		OPEN_RIGHT, 
		OPEN_TOP, 
		OPEN_BOTTOM,
		CLOSE_LEFT, 
		CLOSE_RIGHT,
		CLOSE_TOP,
		CLOSE_BOTTOM
	};
		
	private GestureDetector gestureDetector;
	private GestureDetector.OnGestureListener listener_gesture = new GestureDetector.OnGestureListener() {
		
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			return SlideLayout.this.onSingleTapUp(e);
		}
		
		@Override
		public void onShowPress(MotionEvent e) {
		}
		
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			return SlideLayout.this.onScroll(e1, e2, distanceX, distanceY);			
		}
		
		@Override
		public void onLongPress(MotionEvent e) {			
		}
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			return SlideLayout.this.onFling(e1, e2, velocityX, velocityY);
		}
		
		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}
	};
	
	public SlideLayout(Context context) {
		this(context, null);
	}

	public SlideLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SlideLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		int menuStyleId = 0;
		int leftMenuStyleId = 0;
		int rightMenuStyleId = 0;
		int topMenuStyleId = 0;
		int bottomMenuStyleId = 0;
		
		if(attrs != null){
			TypedArray a = context.obtainStyledAttributes(attrs,  R.styleable.SlideLayoutStyle, defStyle, R.style.SlideLayoutStyleDefault);
			 
			for (int i = 0, count = a.getIndexCount(); i < count; i++){
			    int attr = a.getIndex(i);
			    switch (attr){
				    case R.styleable.SlideLayoutStyle_dragEnable:
			        	dragEnable = a.getBoolean(attr, true);
			        	break;
			    	case R.styleable.SlideLayoutStyle_contentChild:
			    		contentChild = a.getInt(attr, -1);
			    		break;   
			    	case R.styleable.SlideLayoutStyle_leftMenuChild:
			        	leftMenuChild = a.getInt(attr, -1);
			            break;
			        case R.styleable.SlideLayoutStyle_rightMenuChild:
			        	rightMenuChild = a.getInt(attr, -1);
			            break; 
			        case R.styleable.SlideLayoutStyle_topMenuChild:
			        	topMenuChild = a.getInt(attr, -1);
			            break; 
			        case R.styleable.SlideLayoutStyle_bottomMenuChild:
			        	bottomMenuChild = a.getInt(attr, -1);
			            break; 
			        case R.styleable.SlideLayoutStyle_menuStyle:
			        	menuStyleId = a.getResourceId(attr, 0);
			            break;
			        case R.styleable.SlideLayoutStyle_leftMenuStyle:
			        	leftMenuStyleId = a.getResourceId(attr, 0);
			            break;
			        case R.styleable.SlideLayoutStyle_rightMenuStyle:
			        	rightMenuStyleId = a.getResourceId(attr, 0);
			            break;
			        case R.styleable.SlideLayoutStyle_topMenuStyle:
			        	topMenuStyleId = a.getResourceId(attr, 0);
			            break;
			        case R.styleable.SlideLayoutStyle_bottomMenuStyle:
			        	bottomMenuStyleId = a.getResourceId(attr, 0);
			            break;
			    }
			}
			a.recycle();	
		}
		
		if(leftMenuChild >= 0)
			leftMenuStyle = new MenuStyle(context, leftMenuStyleId > 0 ? leftMenuStyleId : menuStyleId);
				
		if(rightMenuChild >= 0)
			rightMenuStyle = new MenuStyle(context, rightMenuStyleId > 0 ? rightMenuStyleId : menuStyleId);
		
		if(topMenuChild >= 0)
			topMenuStyle = new MenuStyle(context, topMenuStyleId > 0 ? topMenuStyleId : menuStyleId);
		
		if(bottomMenuChild >= 0)
			bottomMenuStyle = new MenuStyle(context, bottomMenuStyleId > 0 ? bottomMenuStyleId : menuStyleId);
		
		gestureDetector = new GestureDetector(context, listener_gesture);
		
		measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		viewWidth = getMeasuredWidth();
		viewHeight = getMeasuredHeight();
		setLeftMenuView(viewWidth, viewHeight);
		setRightMenuView(viewWidth, viewHeight);
		setTopMenuView(viewWidth, viewHeight);
		setBottomMenuView(viewWidth, viewHeight);
		setContentView(viewWidth, viewHeight);
		setShadow(viewWidth, viewHeight);   
	}
	
	protected View getLeftMenuView(){		
		return getChildAt(leftMenuChild);
	}
	
	protected View getRightMenuView(){		
		return getChildAt(rightMenuChild);
	}
	
	protected View getTopMenuView(){		
		return getChildAt(topMenuChild);
	}
	
	protected View getBottomMenuView(){		
		return getChildAt(bottomMenuChild);
	}
	
	protected View getContentView(){		
		return getChildAt(contentChild);
	}
		
	protected View getRightShadowView(){		
		return getChildAt(rightShadowChild);
	}
	
	protected View getLeftShadowView(){		
		return getChildAt(leftShadowChild);
	}
	
	protected View getTopShadowView(){		
		return getChildAt(topShadowChild);
	}
	
	protected View getBottomShadowView(){		
		return getChildAt(bottomShadowChild);
	}
			
	protected void setLeftMenuView(int viewWidth, int viewHeight){
		View menu = getLeftMenuView();
		if(menu == null)
			return;
	    
		if(leftMenuStyle.menuBorderPercent >= 0f)
			leftMenuStyle.menuBorder = (int)(viewWidth * leftMenuStyle.menuBorderPercent);
		if(leftMenuStyle.menuOverDragBorderPercent >= 0f)
			leftMenuStyle.menuOverDragBorder = (int)(viewWidth * leftMenuStyle.menuOverDragBorderPercent);	
		
		leftMenuStyle.size = viewWidth - leftMenuStyle.menuBorder;
		if(leftMenuStyle.closeEdgePercent >= 0f)
			leftMenuStyle.closeEdge = (int)(leftMenuStyle.size * leftMenuStyle.closeEdgePercent);
		
	    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(leftMenuStyle.size, FrameLayout.LayoutParams.MATCH_PARENT);
	    layoutParams.setMargins(0, 0, 0, 0);
        menu.setLayoutParams(layoutParams);
        menu.setVisibility(offsetX <= 0 ? View.GONE : View.VISIBLE);	
	}
	
	protected void setRightMenuView(int viewWidth, int viewHeight){
		View menu = getRightMenuView();
		if(menu == null)
			return;
	    
		if(rightMenuStyle.menuBorderPercent >= 0f)
			rightMenuStyle.menuBorder = (int)(viewWidth * rightMenuStyle.menuBorderPercent);
		if(rightMenuStyle.menuOverDragBorderPercent >= 0f)
			rightMenuStyle.menuOverDragBorder = (int)(viewWidth * rightMenuStyle.menuOverDragBorderPercent);	
		
		rightMenuStyle.size = viewWidth - rightMenuStyle.menuBorder;
		if(rightMenuStyle.closeEdgePercent >= 0f)
			rightMenuStyle.closeEdge = (int)(rightMenuStyle.size * rightMenuStyle.closeEdgePercent);
		
	    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(rightMenuStyle.size, FrameLayout.LayoutParams.MATCH_PARENT);
	    layoutParams.setMargins(0, 0, 0, 0);
		menu.setLayoutParams(layoutParams);
		menu.setVisibility(offsetX >= 0 ? View.GONE : View.VISIBLE);
	}
	
	protected void setTopMenuView(int viewWidth, int viewHeight){
		View menu = getTopMenuView();
		if(menu == null)
			return;
	    
		if(topMenuStyle.menuBorderPercent >= 0f)
			topMenuStyle.menuBorder = (int)(viewWidth * topMenuStyle.menuBorderPercent);
		if(topMenuStyle.menuOverDragBorderPercent >= 0f)
			topMenuStyle.menuOverDragBorder = (int)(viewWidth * topMenuStyle.menuOverDragBorderPercent);
		
		topMenuStyle.size = viewHeight - topMenuStyle.menuBorder;
		if(topMenuStyle.closeEdgePercent >= 0f)
			topMenuStyle.closeEdge = (int)(topMenuStyle.size * topMenuStyle.closeEdgePercent);
		
	    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, topMenuStyle.size);
	    layoutParams.setMargins(0, 0, 0, 0);
		menu.setLayoutParams(layoutParams);
		menu.setVisibility(offsetY <= 0 ? View.GONE : View.VISIBLE);
	}
	
	protected void setBottomMenuView(int viewWidth, int viewHeight){
		View menu = getBottomMenuView();
		if(menu == null)
			return;
	    
		if(bottomMenuStyle.menuBorderPercent >= 0f)
			bottomMenuStyle.menuBorder = (int)(viewWidth * bottomMenuStyle.menuBorderPercent);
		if(bottomMenuStyle.menuOverDragBorderPercent >= 0f)
			bottomMenuStyle.menuOverDragBorder = (int)(viewWidth * bottomMenuStyle.menuOverDragBorderPercent);
		
		bottomMenuStyle.size = viewHeight - bottomMenuStyle.menuBorder;
		if(bottomMenuStyle.closeEdgePercent >= 0f)
			bottomMenuStyle.closeEdge = (int)(bottomMenuStyle.size * bottomMenuStyle.closeEdgePercent);
		
	    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, bottomMenuStyle.size);
	    layoutParams.setMargins(0, 0, 0, 0);
		menu.setLayoutParams(layoutParams);
		menu.setVisibility(offsetY >= 0 ? View.GONE : View.VISIBLE);
	}
	
	protected void setContentView(int viewWidth, int viewHeight){
		View content = getContentView();
		if(content == null)
			return;
	    
	    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
	    layoutParams.setMargins(0, 0, 0, 0);
		content.setLayoutParams(layoutParams);
	}
	
	protected void setShadow(int viewWidth, int viewHeight){
		int left = offsetX + getPaddingLeft();
		int top = offsetY + getPaddingTop();
		
		if(getLeftShadowView() != null){
			removeViewInLayout(getLeftShadowView());
			leftShadowChild = -1;
		}
		
		if(getRightShadowView() != null){
			removeViewInLayout(getRightShadowView());
			rightShadowChild = -1;
		}
		
		if(getTopShadowView() != null){
			removeViewInLayout(getTopShadowView());
			topShadowChild = -1;
		}
		
		if(getBottomShadowView() != null){
			removeViewInLayout(getBottomShadowView());
			bottomShadowChild = -1;
		}
		
		if(getLeftMenuView() != null && leftMenuStyle.menuShadow > 0){
			View v = new View(getContext());
			v.setBackgroundResource(leftMenuChild > contentChild ? R.drawable.sm_rightshadow : R.drawable.sm_leftshadow);	
			
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(leftMenuStyle.menuShadow, FrameLayout.LayoutParams.MATCH_PARENT);
			layoutParams.setMargins(0, 0, 0, 0);					
			addViewInLayout(v, -1, layoutParams);
			
			leftShadowChild = getChildCount() - 1;
			v.setVisibility(left <= 0 ? View.GONE : View.VISIBLE);
		}
		
		if(getRightMenuView() != null && rightMenuStyle.menuShadow > 0){
			View v = new View(getContext());
			v.setBackgroundResource(rightMenuChild > contentChild ? R.drawable.sm_leftshadow : R.drawable.sm_rightshadow);	
			
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(rightMenuStyle.menuShadow, FrameLayout.LayoutParams.MATCH_PARENT);
			layoutParams.setMargins(0, 0, 0, 0);					
			addViewInLayout(v, -1, layoutParams);
			
			rightShadowChild = getChildCount() - 1;
			v.setVisibility(left >= 0 ? View.GONE : View.VISIBLE);
		}
		
		if(getTopMenuView() != null && topMenuStyle.menuShadow > 0){
			View v = new View(getContext());
			v.setBackgroundResource(topMenuChild > contentChild ? R.drawable.sm_bottomshadow : R.drawable.sm_topshadow);	
			
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, topMenuStyle.menuShadow);
			layoutParams.setMargins(0, 0, 0, 0);					
			addViewInLayout(v, -1, layoutParams);
			
			topShadowChild = getChildCount() - 1;
			v.setVisibility(top <= 0 ? View.GONE : View.VISIBLE);
		}
		
		if(getBottomMenuView() != null && bottomMenuStyle.menuShadow > 0){
			View v = new View(getContext());
			v.setBackgroundResource(bottomMenuChild > contentChild ? R.drawable.sm_topshadow :R.drawable.sm_bottomshadow);	
			
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, bottomMenuStyle.menuShadow);
			layoutParams.setMargins(0, 0, 0, 0);					
			addViewInLayout(v, -1, layoutParams);
			
			bottomShadowChild = getChildCount() - 1;
			v.setVisibility(top >= 0 ? View.GONE : View.VISIBLE);
		}
	}
		
	public boolean isDragEnable(){
		return dragEnable;
	}
	
	public void setDragEnable(boolean enable){
		dragEnable = enable;
	}
	
	public void setOnStateChangedListener(OnStateChangedListener listener){
		if(listener == null)
			listener_state = null;
		
		listener_state = new WeakReference<OnStateChangedListener>(listener);
	}
	
	public boolean dispatchTouchEvent(MotionEvent event){			
		if(action == ACTION_SHOW){
			switch (target) {
				case TARGET_CONTENT:
					return dispatchTouchEventStateShowContent(event);
				case TARGET_LEFT:
					return dispatchTouchEventStateShowLeftMenu(event);
				case TARGET_RIGHT:
					return dispatchTouchEventStateShowRightMenu(event);
				case TARGET_TOP:
					return dispatchTouchEventStateShowTopMenu(event);
				case TARGET_BOTTOM:
					return dispatchTouchEventStateShowBottomMenu(event);
			}
		}
		else if(action == ACTION_DRAG){
			switch (target) {
				case TARGET_LEFT:
					return dispatchTouchEventStateDragLeftMenu(event);
				case TARGET_RIGHT:
					return dispatchTouchEventStateDragRightMenu(event);
				case TARGET_TOP:
					return dispatchTouchEventStateDragTopMenu(event);
				case TARGET_BOTTOM:
					return dispatchTouchEventStateDragBottomMenu(event);
			}
		}
		
		return true;		
	}
	
	private boolean dispatchTouchEventStateShowContent(MotionEvent event){
		if(!dragEnable)
			return super.dispatchTouchEvent(event);
		
		if(event.getAction() ==  MotionEvent.ACTION_DOWN){
			if((leftMenuStyle != null && event.getX() < leftMenuStyle.dragEdge) 
					|| (rightMenuStyle != null && event.getX() > viewWidth - rightMenuStyle.dragEdge)
					|| (topMenuStyle != null && event.getY() < topMenuStyle.dragEdge)
					|| (bottomMenuStyle != null && event.getY() > viewHeight - bottomMenuStyle.dragEdge)){
				downX = event.getX();
				downY = event.getY();
				prevX = downX;
				prevY = downY;
				disX = 0f;
				disY = 0f;
				super.dispatchTouchEvent(event);
				return true;
			}
		}
		else if(event.getAction() ==  MotionEvent.ACTION_UP){
			downX = -1;		
			downY = -1;
		}
		else if(event.getAction() == MotionEvent.ACTION_MOVE && downX > 0 && downY > 0){
			if(getLeftMenuView() != null && downX < leftMenuStyle.dragEdge){
				if(event.getX() >= prevX){
					disX += event.getX() - prevX;
					prevX = event.getX();
					prevY = event.getY();
					
					if(disX > leftMenuStyle.touchSlop){		
						offsetX = Math.min(leftMenuStyle.size, Math.max(0, (int)(offsetX + disX)));
						offsetViewX(offsetX);									
						float alpha = (float)offsetX / (float)leftMenuStyle.size;
						if(leftMenuChild > contentChild)
							changeAlpha(getContentView(), 1f - alpha, TARGET_LEFT);						
						else						
							changeAlpha(getLeftMenuView(), alpha, TARGET_LEFT);						
												
						MotionEvent cancelEvent = MotionEvent.obtain(event);
	                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
	                    setState(offsetX < leftMenuStyle.size ? ACTION_DRAG : ACTION_SHOW, TARGET_LEFT, OP.START_DRAG_LEFT_FROM_CONTENT);
						boolean result = super.dispatchTouchEvent(cancelEvent);
						cancelEvent.recycle();
						
						dispatchOffsetChangedEvent(alpha, 0f);
						
						startDrag = true;						
						return result;								
					}
				}
				else
					downX = -1;
			}
			else if(getRightMenuView() != null && downX > viewWidth - rightMenuStyle.dragEdge){
				if(event.getX() <= prevX){
					disX += prevX - event.getX();
					prevX = event.getX();
					prevY = event.getY();
					
					if(disX > rightMenuStyle.touchSlop){								
						offsetX = Math.max(-rightMenuStyle.size, Math.min(0, (int)(offsetX - disX)));
						offsetViewX(offsetX);
						float alpha = (float)-offsetX / (float)rightMenuStyle.size;
						if(rightMenuChild > contentChild)
							changeAlpha(getContentView(), 1f - alpha, TARGET_RIGHT);
						else
							changeAlpha(getRightMenuView(), alpha, TARGET_RIGHT);
						
						MotionEvent cancelEvent = MotionEvent.obtain(event);
	                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));		
	                    setState(offsetX > -rightMenuStyle.size ? ACTION_DRAG : ACTION_SHOW, TARGET_RIGHT, OP.START_DRAG_RIGHT_FROM_CONTENT);
						boolean result = super.dispatchTouchEvent(cancelEvent);
						cancelEvent.recycle();
						
						dispatchOffsetChangedEvent(alpha, 0f);
						
						startDrag = true;
						return result;								
					}
				}
				else
					downX = -1;
			}
			else if(getTopMenuView() != null && downY < topMenuStyle.dragEdge){
				if(event.getY() >= prevY){
					disY += event.getY() - prevY;
					prevY = event.getY();
					prevX = event.getX();
					
					if(disY > topMenuStyle.touchSlop){
						offsetY = Math.min(topMenuStyle.size, Math.max(0, (int)(offsetY + disY)));
						offsetViewY(offsetY);		
						float alpha = (float)offsetY / (float)topMenuStyle.size;
						if(topMenuChild > contentChild)
							changeAlpha(getContentView(), 1f - alpha, TARGET_TOP);
						else
							changeAlpha(getTopMenuView(), alpha, TARGET_TOP);
						
						MotionEvent cancelEvent = MotionEvent.obtain(event);
	                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));	
	                    setState(offsetY < topMenuStyle.size ? ACTION_DRAG : ACTION_SHOW, TARGET_TOP, OP.START_DRAG_TOP_FROM_CONTENT);
						boolean result = super.dispatchTouchEvent(cancelEvent);
						cancelEvent.recycle();
						
						dispatchOffsetChangedEvent(0f, alpha);
						
						startDrag = true;
						return result;
					}
				}
				else
					downY = -1;
			}
			else if(getBottomMenuView() != null && downY > viewHeight - bottomMenuStyle.dragEdge){
				if(event.getY() <= prevY){
					disY += prevY - event.getY();
					prevX = event.getX();
					prevY = event.getY();
					
					if(disY > bottomMenuStyle.touchSlop){
						offsetY = Math.max(-bottomMenuStyle.size, Math.min(0, (int)(offsetY - disY)));
						offsetViewY(offsetY);
						float alpha = (float)-offsetY / (float)bottomMenuStyle.size;
						if(bottomMenuChild > contentChild)
							changeAlpha(getContentView(), 1f - alpha, TARGET_BOTTOM);
						else
							changeAlpha(getBottomMenuView(), alpha, TARGET_BOTTOM);
						
						MotionEvent cancelEvent = MotionEvent.obtain(event);
	                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));				                    
						setState(offsetY > -bottomMenuStyle.size ? ACTION_DRAG : ACTION_SHOW, TARGET_BOTTOM, OP.START_DRAG_BOTTOM_FROM_CONTENT);
						boolean result = super.dispatchTouchEvent(cancelEvent);
						cancelEvent.recycle();
						
						dispatchOffsetChangedEvent(0f, alpha);
						
						startDrag = true;
						return result;								
					}
				}
				else
					downY = -1;
			}
		}		
			
		return super.dispatchTouchEvent(event);
	}
	
	private boolean dispatchTouchEventStateShowLeftMenu(MotionEvent event){
		if(leftMenuStyle.menuBorder == 0){
			if(event.getAction() ==  MotionEvent.ACTION_DOWN){
				if(event.getX() > viewWidth - leftMenuStyle.dragEdge){
					downX = event.getX();
					downY = event.getY();
					prevX = downX;
					prevY = downY;
					disX = 0f;
					disY = 0f;
				}
			}
			else if(event.getAction() ==  MotionEvent.ACTION_UP){
				downX = -1;		
				downY = -1;
			}
			else if(event.getAction() == MotionEvent.ACTION_MOVE && downX > 0 && downY > 0){
				if(event.getX() <= prevX){
					disX += prevX - event.getX();
					prevX = event.getX();
					prevY = event.getY();
					
					if(disX > leftMenuStyle.touchSlop){								
						offsetX = Math.min(leftMenuStyle.size, Math.max(0, (int)(event.getX())));
						offsetViewX(offsetX);		
						float alpha = (float)offsetX / (float)leftMenuStyle.size;
						if(leftMenuChild > contentChild)
							changeAlpha(getContentView(), 1f - alpha, TARGET_LEFT);
						else
							changeAlpha(getLeftMenuView(), alpha, TARGET_LEFT);
						
						MotionEvent cancelEvent = MotionEvent.obtain(event);
	                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));				                    
	                    setState(offsetX < leftMenuStyle.size ? ACTION_DRAG : ACTION_SHOW, TARGET_LEFT, OP.START_DRAG_LEFT_FROM_CONTENT);
						boolean result = getLeftMenuView().dispatchTouchEvent(cancelEvent) || super.dispatchTouchEvent(cancelEvent);
						cancelEvent.recycle();
						
						dispatchOffsetChangedEvent(alpha, 0f);												
						return result;							
					}
				}
				else
					downX = -1;
			}
		}
		
		if(!getLeftMenuView().dispatchTouchEvent(event)){
			if(!dragEnable)
				return super.dispatchTouchEvent(event);
			else if(!gestureDetector.onTouchEvent(event)){
				return super.dispatchTouchEvent(event);
			}
		}
		
		return true;
	}
	
	private boolean dispatchTouchEventStateDragLeftMenu(MotionEvent event){
		if(!gestureDetector.onTouchEvent(event) && (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)){
			
			if(offsetX < leftMenuStyle.closeEdge)
				closeLeftMenu(true);					
			else
				openLeftMenu(true);
		}
		
		return true;
	}
	
	private boolean dispatchTouchEventStateShowRightMenu(MotionEvent event){
		if(rightMenuStyle.menuBorder == 0){
			if(event.getAction() ==  MotionEvent.ACTION_DOWN){
				if(event.getX() < rightMenuStyle.dragEdge){
					downX = event.getX();
					downY = event.getY();
					prevX = downX;
					prevY = downY;
					disX = 0f;
					disY = 0f;
				}
			}
			else if(event.getAction() ==  MotionEvent.ACTION_UP){
				downX = -1;		
				downY = -1;
			}
			else if(event.getAction() == MotionEvent.ACTION_MOVE && downX > 0 && downY > 0){
				if(event.getX() >= prevX){
					disX += event.getX() - prevX;
					prevX = event.getX();
					prevY = event.getY();
					
					if(disX > rightMenuStyle.touchSlop){
						offsetX = Math.max(-rightMenuStyle.size, Math.min(0, (int)(event.getX() - viewWidth)));
						offsetViewX(offsetX);
						float alpha = (float)-offsetX / (float)rightMenuStyle.size;
						if(rightMenuChild > contentChild)
							changeAlpha(getContentView(), 1f - alpha, TARGET_RIGHT);
						else
							changeAlpha(getRightMenuView(), alpha, TARGET_RIGHT);
						
						MotionEvent cancelEvent = MotionEvent.obtain(event);
	                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));				                    
	                    setState(offsetX > -rightMenuStyle.size ? ACTION_DRAG : ACTION_SHOW, TARGET_RIGHT, OP.START_DRAG_RIGHT_FROM_CONTENT);
						boolean result = getRightMenuView().dispatchTouchEvent(cancelEvent) || super.dispatchTouchEvent(cancelEvent);
						cancelEvent.recycle();
						
						dispatchOffsetChangedEvent(alpha, 0f);
						
						return result;		
					}
				}
				else
					downX = -1;
			}
		}
						
		event.offsetLocation(-rightMenuStyle.menuBorder, 0);
		if(!getRightMenuView().dispatchTouchEvent(event)){
			event.offsetLocation(rightMenuStyle.menuBorder, 0);
			
			if(!dragEnable)
				return super.dispatchTouchEvent(event);
			else if(!gestureDetector.onTouchEvent(event))
				return super.dispatchTouchEvent(event);
		}
		
		return true;
	}
	
	private boolean dispatchTouchEventStateDragRightMenu(MotionEvent event){
		if(!gestureDetector.onTouchEvent(event) && (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)){
			
			if(offsetX > -rightMenuStyle.closeEdge)
				closeRightMenu(true);
			else
				openRightMenu(true);
		}
		
		return true;
	}
	
	private boolean dispatchTouchEventStateShowTopMenu(MotionEvent event){
		if(topMenuStyle.menuBorder == 0){
			if(event.getAction() ==  MotionEvent.ACTION_DOWN){
				if(event.getY() > viewHeight - topMenuStyle.dragEdge){
					downX = event.getX();
					downY = event.getY();
					prevX = downX;
					prevY = downY;
					disX = 0f;
					disY = 0f;
				}
			}
			else if(event.getAction() ==  MotionEvent.ACTION_UP){
				downX = -1;		
				downY = -1;
			}
			else if(event.getAction() == MotionEvent.ACTION_MOVE && downX > 0 && downY > 0){
				if(event.getY() <= prevY){
					disY += prevY - event.getY();
					prevX = event.getX();
					prevY = event.getY();
					
					if(disY > topMenuStyle.touchSlop){
						offsetY = Math.min(topMenuStyle.size, Math.max(0, (int)(event.getY())));
						offsetViewY(offsetY);		
						float alpha = (float)offsetY / (float)topMenuStyle.size;
						if(topMenuChild > contentChild)
							changeAlpha(getContentView(), 1f - alpha, TARGET_TOP);
						else
							changeAlpha(getTopMenuView(), alpha, TARGET_TOP);
						
						MotionEvent cancelEvent = MotionEvent.obtain(event);
	                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));				                    
	                    setState(offsetY < topMenuStyle.size ? ACTION_DRAG : ACTION_SHOW, TARGET_TOP, OP.START_DRAG_TOP_FROM_CONTENT);
						boolean result = getTopMenuView().dispatchTouchEvent(cancelEvent) || super.dispatchTouchEvent(cancelEvent);
						cancelEvent.recycle();
						
						dispatchOffsetChangedEvent(0f, alpha);
						
						return result;							
					}
				}
				else
					downY = -1;
			}
		}
		
		if(!getTopMenuView().dispatchTouchEvent(event)){
			if(!dragEnable)
				return super.dispatchTouchEvent(event);
			else if(!gestureDetector.onTouchEvent(event))
				return super.dispatchTouchEvent(event);
		}
		
		return true;
	}
	
	private boolean dispatchTouchEventStateDragTopMenu(MotionEvent event){
		if(!gestureDetector.onTouchEvent(event) && (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)){
			
			if(offsetY < topMenuStyle.closeEdge)
				closeTopMenu(true);
			else
				openTopMenu(true);
		}
		
		return true;
	}
	
	private boolean dispatchTouchEventStateShowBottomMenu(MotionEvent event){
		if(bottomMenuStyle.menuBorder == 0){
			if(event.getAction() ==  MotionEvent.ACTION_DOWN){
				if(event.getY() < bottomMenuStyle.dragEdge){
					downX = event.getX();
					downY = event.getY();
					prevX = downX;
					prevY = downY;
					disX = 0f;
					disY = 0f;
				}
			}
			else if(event.getAction() ==  MotionEvent.ACTION_UP){
				downX = -1;		
				downY = -1;
			}
			else if(event.getAction() == MotionEvent.ACTION_MOVE && downX > 0 && downY > 0){
				if(event.getY() >= prevY){
					disY += event.getY() - prevY;
					prevY = event.getY();
					prevX = event.getX();
					
					if(disY > bottomMenuStyle.touchSlop){
						offsetY = Math.max(-bottomMenuStyle.size, Math.min(0, (int)(event.getY() - viewHeight)));
						offsetViewY(offsetY);
						float alpha = (float)-offsetY / (float)bottomMenuStyle.size;
						if(bottomMenuChild > contentChild)
							changeAlpha(getContentView(), 1f - alpha, TARGET_BOTTOM);
						else
							changeAlpha(getBottomMenuView(), alpha, TARGET_BOTTOM);
						
						MotionEvent cancelEvent = MotionEvent.obtain(event);
	                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));				                    
	                    setState(offsetY > -bottomMenuStyle.size ? ACTION_DRAG : ACTION_SHOW, TARGET_BOTTOM, OP.START_DRAG_BOTTOM_FROM_CONTENT);
						boolean result = getBottomMenuView().dispatchTouchEvent(cancelEvent) || super.dispatchTouchEvent(cancelEvent);
						cancelEvent.recycle();
						
						dispatchOffsetChangedEvent(0f, alpha);
						
						return result;	
					}
				}
				else
					downY = -1;
			}
		}
		
		event.offsetLocation(-bottomMenuStyle.menuBorder, 0);
		if(!getBottomMenuView().dispatchTouchEvent(event)){
			event.offsetLocation(-bottomMenuStyle.menuBorder, 0);

			if(!dragEnable)
				return super.dispatchTouchEvent(event);
			else if(!gestureDetector.onTouchEvent(event))
				return super.dispatchTouchEvent(event);
		}
		return true;
	}
	
	private boolean dispatchTouchEventStateDragBottomMenu(MotionEvent event){
		if(!gestureDetector.onTouchEvent(event) && (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)){
			
			if(offsetY > -bottomMenuStyle.closeEdge)
				closeBottomMenu(true);
			else
				openBottomMenu(true);
		}
		
		return true;
	}
	
	protected boolean onSingleTapUp(MotionEvent e) {
		if(action != ACTION_SHOW)
			return false;
		
		switch (target) {
			case TARGET_LEFT:
				if(e.getX() > leftMenuStyle.size){
					closeLeftMenu(true);
					return true;
				}
				break;
			case TARGET_RIGHT:
				if(e.getX() < viewWidth - rightMenuStyle.size){
					closeRightMenu(true);
					return true;
				}
				break;	
			case TARGET_TOP:
				if(e.getY() > topMenuStyle.size){
					closeTopMenu(true);
					return true;
				}
				break;	
			case TARGET_BOTTOM:
				if(e.getY() < viewHeight - bottomMenuStyle.size){
					closeBottomMenu(true);
					return true;
				}
				break;
		}
		
		return false;
	}
	
	protected boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		if(action == ACTION_SHOW){
			switch (target) {
				case TARGET_LEFT:				
					if(e1 != null && e1.getX() > leftMenuStyle.size && distanceX > 0){
						offsetX =  Math.min(leftMenuStyle.size, Math.max(0, offsetX - (int)distanceX));
						offsetViewX(offsetX);
						float alpha = (float)offsetX / (float)leftMenuStyle.size;
						if(leftMenuChild > contentChild)
							changeAlpha(getContentView(), 1f - alpha, TARGET_LEFT);
						else
							changeAlpha(getLeftMenuView(), alpha, TARGET_LEFT);
						
						MotionEvent cancelEvent = MotionEvent.obtain(e2);
	                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (e2.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
						super.dispatchTouchEvent(cancelEvent);
						cancelEvent.recycle();
						
						if(offsetX > 0)
							setState(ACTION_DRAG, TARGET_LEFT, OP.START_DRAG_LEFT_FROM_MENU);
						else
							setState(ACTION_SHOW, TARGET_CONTENT, OP.START_DRAG_LEFT_FROM_MENU);
						
						dispatchOffsetChangedEvent(alpha, 0f);					
						return true;
					}	
					break;
				case TARGET_RIGHT:				
					if(e1 != null && e1.getX() < viewWidth - rightMenuStyle.size && distanceX < 0){					
						offsetX =  Math.max(-rightMenuStyle.size, Math.min(0, offsetX - (int)distanceX));
						offsetViewX(offsetX);
						float alpha = (float)-offsetX / (float)rightMenuStyle.size;
						if(rightMenuChild > contentChild)
							changeAlpha(getContentView(), 1f - alpha, TARGET_RIGHT);
						else
							changeAlpha(getRightMenuView(), alpha, TARGET_RIGHT);
						
						MotionEvent cancelEvent = MotionEvent.obtain(e2);
	                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (e2.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
						super.dispatchTouchEvent(cancelEvent);
						cancelEvent.recycle();
						
						if(offsetX < 0)
							setState(ACTION_DRAG, TARGET_RIGHT, OP.START_DRAG_RIGHT_FROM_MENU);
						else
							setState(ACTION_SHOW, TARGET_CONTENT, OP.START_DRAG_RIGHT_FROM_MENU);
						dispatchOffsetChangedEvent(alpha, 0f);	
						return true;
					}	
					break;	
				case TARGET_TOP:				
					if(e1 != null && e1.getY() > topMenuStyle.size && distanceY > 0){					
						offsetY =  Math.min(topMenuStyle.size, Math.max(0, offsetY - (int)distanceY));
						offsetViewY(offsetY);
						float alpha = (float)offsetY / (float)topMenuStyle.size;
						if(topMenuChild > contentChild)
							changeAlpha(getContentView(), 1f - alpha, TARGET_TOP);
						else
							changeAlpha(getTopMenuView(), alpha, TARGET_TOP);
						
						MotionEvent cancelEvent = MotionEvent.obtain(e2);
	                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (e2.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
						super.dispatchTouchEvent(cancelEvent);
						cancelEvent.recycle();
						
						if(offsetY > 0)
							setState(ACTION_DRAG, TARGET_TOP, OP.START_DRAG_TOP_FROM_MENU);
						else
							setState(ACTION_SHOW, TARGET_CONTENT, OP.START_DRAG_TOP_FROM_MENU);						
						dispatchOffsetChangedEvent(0f, alpha);	
						return true;
					}	
					break;
				case TARGET_BOTTOM:				
					if(e1 != null && e1.getY() < viewHeight - bottomMenuStyle.size && distanceY < 0){					
						offsetY =  Math.max(-bottomMenuStyle.size, Math.min(0, offsetY - (int)distanceY));
						offsetViewY(offsetY);
						float alpha = (float)-offsetY / (float)bottomMenuStyle.size;
						if(bottomMenuChild > contentChild)
							changeAlpha(getContentView(), 1f - alpha, TARGET_BOTTOM);
						else
							changeAlpha(getBottomMenuView(), alpha, TARGET_BOTTOM);
						
						MotionEvent cancelEvent = MotionEvent.obtain(e2);
	                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (e2.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
						super.dispatchTouchEvent(cancelEvent);
						cancelEvent.recycle();
						
						if(offsetY < 0)
							setState(ACTION_DRAG, TARGET_BOTTOM, OP.START_DRAG_BOTTOM_FROM_MENU);
						else
							setState(ACTION_SHOW, TARGET_CONTENT, OP.START_DRAG_BOTTOM_FROM_MENU);	
						dispatchOffsetChangedEvent(0f, alpha);	
						return true;
					}	
					break;
			}
		}
		else if(action == ACTION_DRAG){
			switch (target) {
				case TARGET_LEFT:
					if(startDrag){
						startDrag = false;
						return true;
					}
					offsetX = Math.min(leftMenuStyle.overDrag ? viewWidth - leftMenuStyle.menuOverDragBorder : leftMenuStyle.size, Math.max(0, offsetX - (int)distanceX));
					offsetViewX(offsetX);
					float alpha = (float)offsetX / (float)leftMenuStyle.size;
					if(leftMenuChild > contentChild)
						changeAlpha(getContentView(), 1f - alpha, TARGET_LEFT);					
					else
						changeAlpha(getLeftMenuView(), alpha, TARGET_LEFT);		
					dispatchOffsetChangedEvent(alpha, 0f);	
					return true;
				case TARGET_RIGHT:
					if(startDrag){
						startDrag = false;
						return true;
					}				
					offsetX = Math.max(rightMenuStyle.overDrag ? rightMenuStyle.menuOverDragBorder - viewWidth : -rightMenuStyle.size, Math.min(0, offsetX - (int)distanceX));
					offsetViewX(offsetX);
					alpha = (float)-offsetX / (float)rightMenuStyle.size;
					if(rightMenuChild > contentChild)
						changeAlpha(getContentView(), 1f - alpha, TARGET_RIGHT);
					else
						changeAlpha(getRightMenuView(), alpha, TARGET_RIGHT);	
					dispatchOffsetChangedEvent(alpha, 0f);	
					return true;	
				case TARGET_TOP:
					if(startDrag){
						startDrag = false;
						return true;
					}	
					offsetY = Math.min(topMenuStyle.overDrag ? viewHeight - topMenuStyle.menuOverDragBorder : topMenuStyle.size, Math.max(0, offsetY - (int)distanceY));
					offsetViewY(offsetY);
					alpha = (float)offsetY / (float)topMenuStyle.size;
					if(topMenuChild > contentChild)
						changeAlpha(getContentView(), 1f - alpha, TARGET_TOP);
					else
						changeAlpha(getTopMenuView(), alpha, TARGET_TOP);	
					dispatchOffsetChangedEvent(0f, alpha);	
					return true;
				case TARGET_BOTTOM:
					if(startDrag){
						startDrag = false;
						return true;
					}				
					offsetY = Math.max(bottomMenuStyle.overDrag ? bottomMenuStyle.menuOverDragBorder - viewHeight : -bottomMenuStyle.size, Math.min(0, offsetY - (int)distanceY));
					offsetViewY(offsetY);
					alpha = (float)-offsetY / (float)bottomMenuStyle.size;
					if(bottomMenuChild > contentChild)
						changeAlpha(getContentView(), 1f - alpha, TARGET_BOTTOM);
					else
						changeAlpha(getBottomMenuView(), alpha, TARGET_BOTTOM);		
					dispatchOffsetChangedEvent(0f, alpha);	
					return true;
			}
		}		
		return false;
	}
	
	protected boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if(action == ACTION_SHOW || action == ACTION_DRAG){
			switch (target) {
				case TARGET_LEFT:
					if(velocityX > leftMenuStyle.velocitySlop){
						if(offsetX != leftMenuStyle.size)
							openLeftMenu(true);
						else
							setState(ACTION_SHOW, TARGET_LEFT, OP.FLING_LEFT);	
						return true;
					}
					else if(velocityX < -leftMenuStyle.velocitySlop){
						if(offsetX > 0)
							closeLeftMenu(true);
						else
							setState(ACTION_SHOW, TARGET_CONTENT, OP.FLING_LEFT);
						return true;
					}
					break;
				case TARGET_RIGHT:
					if(velocityX < -rightMenuStyle.velocitySlop){
						if(offsetX != -rightMenuStyle.size)
							openRightMenu(true);
						else
							setState(ACTION_SHOW, TARGET_RIGHT, OP.FLING_RIGHT);	
						return true;
					}
					else if(velocityX > rightMenuStyle.velocitySlop){
						if(offsetX < 0)
							closeRightMenu(true);
						else
							setState(ACTION_SHOW, TARGET_CONTENT, OP.FLING_RIGHT);	
						return true;
					}
					break;	
				case TARGET_TOP:
					if(velocityY > topMenuStyle.velocitySlop){
						if(offsetY != topMenuStyle.size)
							openTopMenu(true);
						else
							setState(ACTION_SHOW, TARGET_TOP, OP.FLING_TOP);	
						return true;
					}
					else if(velocityY < -topMenuStyle.velocitySlop){
						if(offsetY > 0)
							closeTopMenu(true);
						else
							setState(ACTION_SHOW, TARGET_CONTENT, OP.FLING_TOP);
						return true;
					}
					break;
				case TARGET_BOTTOM:
					if(velocityY < -bottomMenuStyle.velocitySlop){
						if(offsetY != -bottomMenuStyle.size)
							openBottomMenu(true);
						else
							setState(ACTION_SHOW, TARGET_BOTTOM, OP.FLING_BOTTOM);	
						return true;
					}
					else if(velocityY > bottomMenuStyle.velocitySlop){
						if(offsetY < 0)
							closeBottomMenu(true);
						else
							setState(ACTION_SHOW, TARGET_CONTENT, OP.FLING_BOTTOM);	
						return true;
					}
					break;	
			}
		}		
		return false;
	}
	
	public void closeAllMenu(boolean animation){
		if(isState(ACTION_SHOW, TARGET_CONTENT))
			return;
		
		switch (target) {
			case TARGET_LEFT:
				closeLeftMenu(animation);
				break;
			case TARGET_RIGHT:
				closeRightMenu(animation);
				break;
			case TARGET_TOP:
				closeTopMenu(animation);
				break;
			case TARGET_BOTTOM:
				closeBottomMenu(animation);
				break;
		}
	}
	
	public void openLeftMenu(boolean animation){		
		if(getLeftMenuView() == null || (!isState(ACTION_SHOW, TARGET_CONTENT) && !isState(ACTION_DRAG, TARGET_LEFT)))
			return;
		
		long duration = (long)Math.abs(leftMenuStyle.animDuration * ((float)(leftMenuStyle.size - offsetX) / (float)leftMenuStyle.size));		
		getLeftMenuView().clearAnimation();
		
		if(animation && duration > 0){
			setState(ACTION_OPEN, TARGET_LEFT, OP.OPEN_LEFT);	
			SlideLeftAnimation anim = new SlideLeftAnimation(true);
			anim.setDuration(duration);
			anim.setInterpolator(interpolator);
			anim.setAnimationListener(new Animation.AnimationListener() {			
				@Override
				public void onAnimationStart(Animation animation) {}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					setState(ACTION_SHOW, TARGET_LEFT, OP.OPEN_LEFT);	
				}
			});
			getLeftMenuView().startAnimation(anim);
		}
		else{	
			setState(ACTION_SHOW, TARGET_LEFT, OP.OPEN_LEFT);	
			offsetX = leftMenuStyle.size;
			offsetViewX(offsetX);
			if(leftMenuChild > contentChild)
				changeAlpha(getContentView(), 0f, TARGET_LEFT);
			else
				changeAlpha(getLeftMenuView(), 1f, TARGET_LEFT);
		}		
	}
	
	public void closeLeftMenu(boolean animation){
		if(getLeftMenuView() == null || (!isState(ACTION_SHOW, TARGET_LEFT) && !isState(ACTION_DRAG, TARGET_LEFT)))
			return;
		
		long duration = (long)(leftMenuStyle.animDuration * ((float)offsetX / (float)leftMenuStyle.size));				
		getLeftMenuView().clearAnimation();
		
		if(animation && duration > 0){
			setState(ACTION_CLOSE, TARGET_LEFT, OP.CLOSE_LEFT);
			SlideLeftAnimation anim = new SlideLeftAnimation(false);
			anim.setDuration(duration);
			anim.setInterpolator(interpolator);
			anim.setAnimationListener(new Animation.AnimationListener() {			
				@Override
				public void onAnimationStart(Animation animation) {}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					setState(ACTION_SHOW, TARGET_CONTENT, OP.CLOSE_LEFT);
				}
			});
			getLeftMenuView().startAnimation(anim);
		}
		else{		
			setState(ACTION_SHOW, TARGET_CONTENT, OP.CLOSE_LEFT);
			offsetX = 0;
			offsetViewX(offsetX);	
			if(leftMenuChild > contentChild)
				changeAlpha(getContentView(), 1f, TARGET_LEFT);
		}	
	}
	
	public void openRightMenu(boolean animation){
		if(getRightMenuView() == null || (!isState(ACTION_SHOW, TARGET_CONTENT) && !isState(ACTION_DRAG, TARGET_RIGHT)))
			return;
				
		long duration = (long)Math.abs(rightMenuStyle.animDuration * ((float)(offsetX + rightMenuStyle.size) / (float)rightMenuStyle.size));		
		getRightMenuView().clearAnimation();
		
		if(animation && duration > 0){
			setState(ACTION_OPEN, TARGET_RIGHT, OP.OPEN_RIGHT);	
			SlideRightAnimation anim = new SlideRightAnimation(true);
			anim.setDuration(duration);
			anim.setInterpolator(interpolator);
			anim.setAnimationListener(new Animation.AnimationListener() {			
				@Override
				public void onAnimationStart(Animation animation) {}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					setState(ACTION_SHOW, TARGET_RIGHT, OP.OPEN_RIGHT);	
				}
			});
			getRightMenuView().startAnimation(anim);
		}
		else{		
			setState(ACTION_SHOW, TARGET_RIGHT, OP.OPEN_RIGHT);	
			offsetX = -rightMenuStyle.size;
			offsetViewX(offsetX);
			if(rightMenuChild > contentChild)
				changeAlpha(getContentView(), 0f, TARGET_RIGHT);
			else
				changeAlpha(getRightMenuView(), 1f, TARGET_RIGHT);
		}
	}
			
	public void closeRightMenu(boolean animation){
		if(getRightMenuView() == null || (!isState(ACTION_SHOW, TARGET_RIGHT) && !isState(ACTION_DRAG, TARGET_RIGHT)))
			return;
		
		long duration = (long)(rightMenuStyle.animDuration * ((float)-offsetX/ (float)rightMenuStyle.size));		
		getRightMenuView().clearAnimation();
		
		if(animation && duration > 0){
			setState(ACTION_CLOSE, TARGET_RIGHT, OP.CLOSE_RIGHT);
			SlideRightAnimation anim = new SlideRightAnimation(false);
			anim.setDuration(duration);
			anim.setInterpolator(interpolator);
			anim.setAnimationListener(new Animation.AnimationListener() {			
				@Override
				public void onAnimationStart(Animation animation) {}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					setState(ACTION_SHOW, TARGET_CONTENT, OP.CLOSE_RIGHT);
				}
			});
			getRightMenuView().startAnimation(anim);
		}
		else{
			setState(ACTION_SHOW, TARGET_CONTENT, OP.CLOSE_RIGHT);
			offsetX = 0;
			offsetViewX(offsetX);		
			if(rightMenuChild > contentChild)
				changeAlpha(getContentView(), 1f, TARGET_RIGHT);
		}
	}
	
	public void openTopMenu(boolean animation){		
		if(getTopMenuView() == null || (!isState(ACTION_SHOW, TARGET_CONTENT) && !isState(ACTION_DRAG, TARGET_TOP)))
			return;
		
		long duration = (long)Math.abs(topMenuStyle.animDuration * ((float)(topMenuStyle.size - offsetY) / (float)topMenuStyle.size));		
		getTopMenuView().clearAnimation();
		
		if(animation && duration > 0){
			setState(ACTION_OPEN, TARGET_TOP, OP.OPEN_TOP);	
			SlideTopAnimation anim = new SlideTopAnimation(true);
			anim.setDuration(duration);
			anim.setInterpolator(interpolator);
			anim.setAnimationListener(new Animation.AnimationListener() {			
				@Override
				public void onAnimationStart(Animation animation) {}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					setState(ACTION_SHOW, TARGET_TOP, OP.OPEN_TOP);	
				}
			});
			getTopMenuView().startAnimation(anim);
		}
		else{	
			setState(ACTION_SHOW, TARGET_TOP, OP.OPEN_TOP);	
			offsetY = topMenuStyle.size;
			offsetViewY(offsetY);
			if(topMenuChild > contentChild)
				changeAlpha(getContentView(), 0f, TARGET_TOP);
			else
				changeAlpha(getTopMenuView(), 1f, TARGET_TOP);
		}
	}
	
	public void closeTopMenu(boolean animation){
		if(getTopMenuView() == null || (!isState(ACTION_SHOW, TARGET_TOP) && !isState(ACTION_DRAG, TARGET_TOP)))
			return;
		
		long duration = (long)(topMenuStyle.animDuration * ((float)offsetY / (float)topMenuStyle.size));		
		getTopMenuView().clearAnimation();
		
		if(animation && duration > 0){	
			setState(ACTION_CLOSE, TARGET_TOP, OP.CLOSE_TOP);
			SlideTopAnimation anim = new SlideTopAnimation(false);
			anim.setDuration(duration);
			anim.setInterpolator(interpolator);
			anim.setAnimationListener(new Animation.AnimationListener() {			
				@Override
				public void onAnimationStart(Animation animation) {}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					setState(ACTION_SHOW, TARGET_CONTENT, OP.CLOSE_TOP);
				}
			});
			getTopMenuView().startAnimation(anim);
		}
		else{	
			setState(ACTION_SHOW, TARGET_CONTENT, OP.CLOSE_TOP);
			offsetY = 0;
			offsetViewY(offsetY);
			if(topMenuChild > contentChild)
				changeAlpha(getContentView(), 1f, TARGET_TOP);
		}
	}
	
	public void openBottomMenu(boolean animation){
		if(getBottomMenuView() == null || (!isState(ACTION_SHOW, TARGET_CONTENT) && !isState(ACTION_DRAG, TARGET_BOTTOM)))
			return;
		
		long duration = (long)Math.abs(bottomMenuStyle.animDuration * ((float)(offsetY + bottomMenuStyle.size) / (float)bottomMenuStyle.size));		
		getBottomMenuView().clearAnimation();
		
		if(animation && duration > 0){	
			setState(ACTION_OPEN, TARGET_BOTTOM, OP.OPEN_BOTTOM);	
			SlideBottomAnimation anim = new SlideBottomAnimation(true);
			anim.setDuration(duration);
			anim.setInterpolator(interpolator);
			anim.setAnimationListener(new Animation.AnimationListener() {			
				@Override
				public void onAnimationStart(Animation animation) {}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					setState(ACTION_SHOW, TARGET_BOTTOM, OP.OPEN_BOTTOM);
				}
			});
			getBottomMenuView().startAnimation(anim);
		}
		else{	
			setState(ACTION_SHOW, TARGET_BOTTOM, OP.OPEN_BOTTOM);
			offsetY = -bottomMenuStyle.size;
			offsetViewY(offsetY);
			if(bottomMenuChild > contentChild)
				changeAlpha(getContentView(), 0f, TARGET_BOTTOM);
			else
				changeAlpha(getBottomMenuView(), 1f, TARGET_BOTTOM);			
		}
	}
			
	public void closeBottomMenu(boolean animation){
		if(getBottomMenuView() == null || (!isState(ACTION_SHOW, TARGET_BOTTOM) && !isState(ACTION_DRAG, TARGET_BOTTOM)))
			return;
		
		long duration = (long)(bottomMenuStyle.animDuration * ((float)-offsetY/ (float)bottomMenuStyle.size));		
		getBottomMenuView().clearAnimation();
		
		if(animation && duration > 0){
			setState(ACTION_CLOSE, TARGET_BOTTOM, OP.CLOSE_BOTTOM);
			SlideBottomAnimation anim = new SlideBottomAnimation(false);
			anim.setDuration(duration);
			anim.setInterpolator(interpolator);
			anim.setAnimationListener(new Animation.AnimationListener() {			
				@Override
				public void onAnimationStart(Animation animation) {}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					setState(ACTION_SHOW, TARGET_CONTENT, OP.CLOSE_BOTTOM);
				}
			});
			getBottomMenuView().startAnimation(anim);
		}
		else{	
			setState(ACTION_SHOW, TARGET_CONTENT, OP.CLOSE_BOTTOM);
			offsetY = 0;
			offsetViewY(offsetY);
			if(bottomMenuChild > contentChild)
				changeAlpha(getContentView(), 1f, TARGET_BOTTOM);
		}
	}
		
	public static int getStateAction(int state){
		return state & ACTION_MASK;
	}
	
	public static int getStateTarget(int state){
		return (state & TARGET_MASK) >> 4;
	}
	
	public static int getState(int action, int target){
		action = action & ACTION_MASK;
		target = target & ACTION_MASK;
		
		return (target << 4) | action;
	}
	
	public synchronized int getState(){
		return getState(action, target);
	}
	
	public synchronized boolean isState(int action, int target){
		return this.action == action && this.target == target;
	}
	
	protected synchronized void setState(int action, int target, OP op){
		int prev_target = this.target;
		
		if(listener_state != null && listener_state.get() != null)
			listener_state.get().onStateChanged(this, getState(this.action, this.target), getState(action, target));
		
		this.action = action;
		this.target = target;
		downX = -1;
		downY = -1;			
		
		if(action == ACTION_DRAG){
			if(getParent() != null)
				getParent().requestDisallowInterceptTouchEvent(true);
		}
		
		if(action == ACTION_SHOW){
			if(target != TARGET_CONTENT)
				startDrag = false;
			else{
				switch (prev_target) {
					case TARGET_LEFT:
						changeVisibility(getLeftMenuView(), View.GONE);
						changeVisibility(getLeftShadowView(), View.GONE);
						break;
					case TARGET_RIGHT:
						changeVisibility(getRightMenuView(), View.GONE);
						changeVisibility(getRightShadowView(), View.GONE);
						break;
					case TARGET_TOP:
						changeVisibility(getTopMenuView(), View.GONE);
						changeVisibility(getTopShadowView(), View.GONE);
						break;
					case TARGET_BOTTOM:
						changeVisibility(getBottomMenuView(), View.GONE);
						changeVisibility(getBottomShadowView(), View.GONE);
						break;
				}
			}				
		}			
	}
	
	protected void dispatchOffsetChangedEvent(float offsetX, float offsetY){
		if(listener_state != null && listener_state.get() != null)
			listener_state.get().onOffsetChanged(this, offsetX, offsetY, getState(action, target));
	}
			
	protected void offsetViewX(int offsetX){
		int left_content = offsetX;
		View content = getContentView();
		View menu;
		View shadow;
		
		if(target == TARGET_LEFT){			
			menu = getLeftMenuView();
			shadow = getLeftShadowView();
			
			if(leftMenuChild > contentChild){
				int left_menu = Math.min(0, left_content - leftMenuStyle.size);
				
				offsetLeftAndRight(content, (int)(left_content * leftMenuStyle.slideRatio - content.getLeft()));
				offsetLeftAndRight(menu, left_menu - menu.getLeft());				
				changeVisibility(menu, left_content <= 0 ? View.GONE : View.VISIBLE);
				
				if(shadow != null){
					offsetLeftAndRight(shadow, left_menu + leftMenuStyle.size - shadow.getLeft());
					changeVisibility(shadow, left_content <= 0 ? View.GONE : View.VISIBLE);
				}
			}
			else{			
				offsetLeftAndRight(content, left_content - content.getLeft());						
				offsetLeftAndRight(menu, (int)((left_content - leftMenuStyle.size) * leftMenuStyle.slideRatio - menu.getLeft()));				
				changeVisibility(menu, left_content <= 0 ? View.GONE : View.VISIBLE);	
				
				if(shadow != null){
					offsetLeftAndRight(shadow, left_content - leftMenuStyle.menuShadow- shadow.getLeft());
					changeVisibility(shadow, left_content <= 0 ? View.GONE : View.VISIBLE);
				}
			}			
		}			
		else if(target == TARGET_RIGHT){
			menu = getRightMenuView();
			shadow = getRightShadowView();
			
			if(rightMenuChild > contentChild){
				int left_menu = Math.max(rightMenuStyle.menuBorder, left_content + viewWidth);
				
				offsetLeftAndRight(content, (int)(left_content * rightMenuStyle.slideRatio - content.getLeft()));
				offsetLeftAndRight(menu, left_menu - menu.getLeft());
				changeVisibility(menu, left_content >= 0 ? View.GONE : View.VISIBLE);
				
				if(shadow != null){
					offsetLeftAndRight(shadow, left_menu - rightMenuStyle.menuShadow - shadow.getLeft());
					changeVisibility(shadow, left_content >= 0 ? View.GONE : View.VISIBLE);
				}
			}
			else{				
				offsetLeftAndRight(content, left_content - content.getLeft());				
				offsetLeftAndRight(menu, (int)((left_content + rightMenuStyle.size) * rightMenuStyle.slideRatio + rightMenuStyle.menuBorder - menu.getLeft()));								
				changeVisibility(menu, left_content >= 0 ? View.GONE : View.VISIBLE);
				
				if(shadow != null){
					offsetLeftAndRight(shadow, left_content + viewWidth - shadow.getLeft());
					changeVisibility(shadow, left_content >= 0 ? View.GONE : View.VISIBLE);
				}
			}			
		}
		
		invalidate();
	}
	
	protected void offsetViewY(int offsetY){
		int top_content = offsetY;
		View content = getContentView();
		View menu;		
		View shadow;		
							
		if(target == TARGET_TOP){
			menu = getTopMenuView();
			shadow = getTopShadowView();
			
			if(topMenuChild > contentChild){
				int top_menu = Math.min(0, top_content - topMenuStyle.size);
				
				offsetTopAndBottom(content, (int)(top_content * topMenuStyle.slideRatio - content.getTop()));
				offsetTopAndBottom(menu, top_menu - menu.getTop());				
				changeVisibility(menu, top_content <= 0 ? View.GONE : View.VISIBLE);
				
				if(shadow != null){
					offsetTopAndBottom(shadow, top_menu + topMenuStyle.size - shadow.getTop());
					changeVisibility(shadow, top_content <= 0 ? View.GONE : View.VISIBLE);
				}
			}
			else{						
				offsetTopAndBottom(content, top_content - content.getTop());
				offsetTopAndBottom(menu, (int)((top_content - topMenuStyle.size) * topMenuStyle.slideRatio - menu.getTop()));				
				changeVisibility(menu, top_content <= 0 ? View.GONE : View.VISIBLE);		
							
				if(shadow != null){
					offsetTopAndBottom(shadow, top_content - topMenuStyle.menuShadow - shadow.getTop());
					changeVisibility(shadow, top_content <= 0 ? View.GONE : View.VISIBLE);
				}
			}			
		}
		else if(target == TARGET_BOTTOM){
			menu = getBottomMenuView();		
			shadow = getBottomShadowView();
			
			if(bottomMenuChild > contentChild){
				int top_menu = Math.max(bottomMenuStyle.menuBorder, top_content + viewHeight);
				
				offsetTopAndBottom(content, (int)(top_content * bottomMenuStyle.slideRatio - content.getTop()));
				offsetTopAndBottom(menu, top_menu - menu.getTop());
				changeVisibility(menu, top_content >= 0 ? View.GONE : View.VISIBLE);
				
				if(shadow != null){
					offsetTopAndBottom(shadow, top_menu - bottomMenuStyle.menuShadow - shadow.getTop());
					changeVisibility(shadow, top_content >= 0 ? View.GONE : View.VISIBLE);
				}				
			}
			else{
				offsetTopAndBottom(content, top_content - content.getTop());				
				offsetTopAndBottom(menu, (int)((top_content + bottomMenuStyle.size) * bottomMenuStyle.slideRatio + bottomMenuStyle.menuBorder - menu.getTop()));								
				changeVisibility(menu, top_content >= 0 ? View.GONE : View.VISIBLE);
				
				if(shadow != null){
					offsetTopAndBottom(shadow, top_content + viewHeight - shadow.getTop());
					changeVisibility(shadow, top_content >= 0 ? View.GONE : View.VISIBLE);
				}
			}			
		}
				
		invalidate();
	}
		
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    	bottom -= top + getPaddingBottom();
    	right -= left + getPaddingRight();
    	
    	if(viewWidth != right || viewHeight != bottom){
    		viewWidth = right;
    		viewHeight = bottom;
    		setLeftMenuView(viewWidth, viewHeight);
    		setRightMenuView(viewWidth, viewHeight);
    		setTopMenuView(viewWidth, viewHeight);
    		setBottomMenuView(viewWidth, viewHeight);
    		setContentView(viewWidth, viewHeight);
    		setShadow(viewWidth, viewHeight);   
    		
    		// if menu already opened, then offset view correctly
    		if(action == ACTION_SHOW){
    			switch (target) {
					case TARGET_LEFT:
						offsetX = leftMenuStyle.size;
						offsetViewX(offsetX);
						break;
					case TARGET_RIGHT:
						offsetX = rightMenuStyle.size;
						offsetViewX(offsetX);
						break;
					case TARGET_TOP:
						offsetY = topMenuStyle.size;
						offsetViewY(offsetY);
						break;
					case TARGET_BOTTOM:
						offsetY = bottomMenuStyle.size;
						offsetViewY(offsetY);
						break;
    			}
    		}
    	}
    	    	
    	final int count = getChildCount();
    	
    	for (int i = 0; i < count; i++) {
    		View child = getChildAt(i);
    		if(child.getVisibility() == View.GONE)
    			continue;    		
    		
    		if(i == leftMenuChild){
    			int leftChild = (leftMenuChild > contentChild) ? Math.min(0, offsetX - leftMenuStyle.size) : (int)((offsetX - leftMenuStyle.size) * leftMenuStyle.slideRatio);    			    			    			
    			child.layout(leftChild, offsetY, leftChild + leftMenuStyle.size, offsetY + viewHeight);
    		}
    		else if(i == rightMenuChild){
    			int leftChild = (rightMenuChild > contentChild) ? Math.max(rightMenuStyle.menuBorder, offsetX + viewWidth) : (int)((offsetX + rightMenuStyle.size) * rightMenuStyle.slideRatio + rightMenuStyle.menuBorder);    			
    			child.layout(leftChild, offsetY, leftChild + rightMenuStyle.size, offsetY + viewHeight);
    		}
    		else if(i == topMenuChild){
    			int topChild = (topMenuChild > contentChild) ? Math.min(0, offsetY - topMenuStyle.size) : (int)((offsetY - topMenuStyle.size) * topMenuStyle.slideRatio);    			    			
    			child.layout(offsetX, topChild, offsetX + viewWidth, topChild + topMenuStyle.size);
    		}
    		else if(i == bottomMenuChild){
    			int topChild = (bottomMenuChild > contentChild) ? Math.max(bottomMenuStyle.menuBorder, offsetY + viewHeight) : (int)((offsetY + bottomMenuStyle.size) * bottomMenuStyle.slideRatio + bottomMenuStyle.menuBorder);
    			child.layout(offsetX, topChild, offsetX + viewWidth, topChild + bottomMenuStyle.size);
    		}
    		else if(i == contentChild){
    			int x = offsetX;
    			int y = offsetY;
    			
    			if(target == TARGET_LEFT){
    				if(leftMenuChild > contentChild)
    					x = (int)(offsetX * leftMenuStyle.slideRatio);    				
    			}
    			else if(target == TARGET_RIGHT){
    				if(rightMenuChild > contentChild)
    					x = (int)(offsetX * rightMenuStyle.slideRatio);       				
    			}
    			else if(target == TARGET_TOP){
    				if(topMenuChild > contentChild)
    					y = (int)(offsetY * topMenuStyle.slideRatio);     
    			}
    			else if(target == TARGET_BOTTOM){
    				if(bottomMenuChild > contentChild)
    					y = (int)(offsetY * bottomMenuStyle.slideRatio);   
    			}
    			
    			child.layout(x, y, x + viewWidth, y + viewHeight);
    		}
    		else if(i == leftShadowChild){
    			int leftChild = leftMenuChild > contentChild ? Math.min(leftMenuStyle.size, offsetX) : (offsetX - leftMenuStyle.menuShadow);
    			child.layout(leftChild, offsetY, leftChild + leftMenuStyle.menuShadow, offsetY + viewHeight); 
    		}
    		else if(i == rightShadowChild){
    			int leftChild = rightMenuChild > contentChild ? (Math.max(rightMenuStyle.menuBorder, offsetX + viewWidth) - rightMenuStyle.menuShadow) : (offsetX + viewWidth);
    			child.layout(leftChild, offsetY, leftChild + rightMenuStyle.menuShadow, offsetY + viewHeight); 
    		}  
    		else if(i == topShadowChild){
    			int topChild = topMenuChild > contentChild ? Math.min(topMenuStyle.size, offsetY) : (offsetY - topMenuStyle.menuShadow);
    			child.layout(offsetX, topChild, offsetX + viewWidth, topChild + topMenuStyle.menuShadow);
    		}
    		else if(i == bottomShadowChild){
    			int topChild = bottomMenuChild > contentChild ? (Math.max(bottomMenuStyle.menuBorder, offsetY + viewHeight) - bottomMenuStyle.menuShadow) : (offsetY + viewHeight);
    			child.layout(offsetX, topChild, offsetX + viewWidth, topChild + bottomMenuStyle.menuShadow); 
    		}
    	}    
    }
    
    protected void offsetLeftAndRight(View v, int offset){
    	if(v == null)
    		return;
    	
		v.offsetLeftAndRight(offset);
	}
    
    protected void offsetTopAndBottom(View v, int offset){
    	if(v == null)
    		return;
    	
		v.offsetTopAndBottom(offset);
	}
    
    protected void changeVisibility(View v, int visibility){
    	if(v == null)
    		return;
    	
    	if(visibility == View.GONE)
    		v.clearAnimation();
    	
		v.setVisibility(visibility);
	}
    
    protected void changeAlpha(View v, float value, int target){
    	if(v == null)
    		return;
    	
    	float minAlpha = 1f;
    	switch (target) {
			case TARGET_LEFT:
				minAlpha = leftMenuStyle.minAlpha;
				break;
			case TARGET_RIGHT:
				minAlpha = rightMenuStyle.minAlpha;
				break;
			case TARGET_TOP:
				minAlpha = topMenuStyle.minAlpha;
				break;
			case TARGET_BOTTOM:
				minAlpha = bottomMenuStyle.minAlpha;
				break;
		}
    	ChangeAlphaAnimation anim = new ChangeAlphaAnimation(Math.max(0f, Math.min(1f, value)) * (1f - minAlpha) + minAlpha);
		v.startAnimation(anim);
	}
    
    private class SlideLeftAnimation extends Animation {
		
		private int distance;
		private int xstart;
		private boolean isOpen;
				
		public SlideLeftAnimation(boolean isOpen){
			this.isOpen = isOpen;
		}
		
		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {				
			if(interpolatedTime == 0f){
				if(isOpen){		
					distance = leftMenuStyle.size - offsetX;	
					xstart = offsetX;
				}
				else{					
					distance = offsetX;
					xstart = offsetX;
				}
			}
			else {
				if(isOpen){
					float value = getInterpolator().getInterpolation(interpolatedTime);
					offsetX = distance > 0 ? Math.min(leftMenuStyle.size, (int)(xstart + distance * value)) : Math.max(leftMenuStyle.size, (int)(xstart + distance * value));
				}
				else{
					float value = getInterpolator().getInterpolation(interpolatedTime);
					offsetX = Math.max(0, (int)(xstart - distance * value));
				}
				offsetViewX(offsetX);
			}
			
			if(leftMenuChild > contentChild)
				changeAlpha(getContentView(), 1f - (float)offsetX / (float)leftMenuStyle.size, TARGET_LEFT);
			else
				t.setAlpha(Math.min(1f, (float)offsetX / (float)leftMenuStyle.size) * (1f - leftMenuStyle.minAlpha) + leftMenuStyle.minAlpha);				
			
			if(isOpen){
				if(offsetX == leftMenuStyle.size)
					this.cancel();
			}
			else{
				if(offsetX == 0)
					this.cancel();
			}
		}
	}    

    private class SlideRightAnimation extends Animation {
		
		private int distance;
		private int xstart;
		private boolean isOpen;
				
		public SlideRightAnimation(boolean isOpen){
			this.isOpen = isOpen;
		}
		
		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {				
			if(interpolatedTime == 0f){
				if(isOpen){		
					distance = offsetX + rightMenuStyle.size;	
					xstart = offsetX;
				}
				else{					
					distance = -offsetX;
					xstart = offsetX;
				}
			}
			else {
				if(isOpen){
					float value = getInterpolator().getInterpolation(interpolatedTime);
					offsetX = distance > 0 ? Math.max(-rightMenuStyle.size, (int)(xstart - distance * value)) : Math.min(-rightMenuStyle.size, (int)(xstart - distance * value));
				}
				else{
					float value = getInterpolator().getInterpolation(interpolatedTime);
					offsetX = Math.min(0, (int)(xstart + distance * value));
				}
				offsetViewX(offsetX);
			}
			
			if(rightMenuChild > contentChild)
				changeAlpha(getContentView(), 1f - (float)-offsetX / (float)rightMenuStyle.size, TARGET_RIGHT);
			else
				t.setAlpha(Math.min(1f, (float)-offsetX / (float)rightMenuStyle.size) * (1f - rightMenuStyle.minAlpha) + rightMenuStyle.minAlpha);
			
			if(isOpen){
				if(offsetX == -rightMenuStyle.size)
					this.cancel();
			}
			else{
				if(offsetX == 0)
					this.cancel();
			}
		}
	}
    
    private class SlideTopAnimation extends Animation {
		
		private int distance;
		private int ystart;
		private boolean isOpen;
				
		public SlideTopAnimation(boolean isOpen){
			this.isOpen = isOpen;
		}
		
		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {				
			if(interpolatedTime == 0f){
				if(isOpen){		
					distance = topMenuStyle.size - offsetY;	
					ystart = offsetY;
				}
				else{					
					distance = offsetY;
					ystart = offsetY;
				}
			}
			else {
				if(isOpen){
					float value = getInterpolator().getInterpolation(interpolatedTime);
					offsetY = distance > 0 ? Math.min(topMenuStyle.size, (int)(ystart + distance * value)) : Math.max(topMenuStyle.size, (int)(ystart + distance * value));
				}
				else{
					float value = getInterpolator().getInterpolation(interpolatedTime);
					offsetY = Math.max(0, (int)(ystart - distance * value));
				}
				offsetViewY(offsetY);
			}
			
			if(topMenuChild > contentChild)
				changeAlpha(getContentView(), 1f - (float)offsetY / (float)topMenuStyle.size, TARGET_TOP);
			else
				t.setAlpha(Math.min(1f, (float)offsetY / (float)topMenuStyle.size) * (1f - topMenuStyle.minAlpha) + topMenuStyle.minAlpha);
			
			if(isOpen){
				if(offsetY == topMenuStyle.size)
					this.cancel();
			}
			else{
				if(offsetY == 0)
					this.cancel();
			}
		}
	} 
    
    private class SlideBottomAnimation extends Animation {
		
		private int distance;
		private int ystart;
		private boolean isOpen;
				
		public SlideBottomAnimation(boolean isOpen){
			this.isOpen = isOpen;
		}
		
		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {				
			if(interpolatedTime == 0f){
				if(isOpen){		
					distance = offsetY + bottomMenuStyle.size;	
					ystart = offsetY;
				}
				else{					
					distance = -offsetY;
					ystart = offsetY;
				}
			}
			else {
				if(isOpen){
					float value = getInterpolator().getInterpolation(interpolatedTime);
					offsetY = distance > 0 ? Math.max(-bottomMenuStyle.size, (int)(ystart - distance * value)) : Math.min(-bottomMenuStyle.size, (int)(ystart - distance * value));
				}
				else{
					float value = getInterpolator().getInterpolation(interpolatedTime);
					offsetY = Math.min(0, (int)(ystart + distance * value));
				}
				offsetViewY(offsetY);
			}
			
			if(bottomMenuChild > contentChild)
				changeAlpha(getContentView(), 1f - (float)-offsetY / (float)bottomMenuStyle.size, TARGET_BOTTOM);
			else
				t.setAlpha(Math.min(1f, (float)-offsetY / (float)bottomMenuStyle.size) * (1f - bottomMenuStyle.minAlpha) + bottomMenuStyle.minAlpha);
			
			if(isOpen){
				if(offsetY == -bottomMenuStyle.size)
					this.cancel();
			}
			else{
				if(offsetY == 0)
					this.cancel();
			}
		}
	}
    
	private class ChangeAlphaAnimation extends Animation{
		
		private float alpha;
		
		public ChangeAlphaAnimation(float alpha){
			setFillAfter(true);
			setDuration(0);
			setAlpha(alpha);
		}
				
		public void setAlpha(float alpha){
			this.alpha = alpha;
		}
		
		@Override
	    protected void applyTransformation(float interpolatedTime, Transformation t) {
	        t.setAlpha(alpha);
	    }
	}
	
	private class SmoothInterpolator implements Interpolator{

		@Override
		public float getInterpolation(float input) {
			return (float)Math.pow(input - 1, 3) + 1;
		}
		
	}
	
	private class MenuStyle{
		boolean overDrag = false;
		
		int menuBorder;
		float menuBorderPercent = -1f;
		
		int menuOverDragBorder;
		float menuOverDragBorderPercent = -1f;
						
		int menuShadow = 10;
		
		int dragEdge = 30;		
		
		int touchSlop = 16;
		
		float minAlpha = 0.3f;
		
		float velocitySlop = 500f;
		
		int closeEdge;
		float closeEdgePercent = -1f;
		
		int animDuration = 1000;
		
		int size;
		
		float slideRatio = 0.5f;
		
		public MenuStyle(Context context, int resID){
			TypedArray a = context.obtainStyledAttributes(resID, R.styleable.SlideMenuStyle);
			 
			for (int i = 0, count = a.getIndexCount(); i < count; i++){
			    int attr = a.getIndex(i);
			    switch (attr){
			    	case R.styleable.SlideMenuStyle_overDrag:
			    		overDrag = a.getBoolean(attr, false);
			    		break;
			        case R.styleable.SlideMenuStyle_menuBorder:
			        	TypedValue value = a.peekValue(attr);
			        	if(value.type == TypedValue.TYPE_DIMENSION)
			        		menuBorder = a.getDimensionPixelSize(attr, 50);
			        	else
			        		menuBorderPercent = Math.max(0f, Math.min(1f, a.getFloat(attr, 0f)));			        	
			            break;
			        case R.styleable.SlideMenuStyle_menuOverDragBorder:
			        	value = a.peekValue(attr);
			        	if(value.type == TypedValue.TYPE_DIMENSION)
			        		menuOverDragBorder = a.getDimensionPixelSize(attr, 50);
			        	else
			        		menuOverDragBorderPercent = Math.max(0f, Math.min(1f, a.getFloat(attr, 0f)));			        	
			            break;
			        case R.styleable.SlideMenuStyle_slideRatio:
			        	slideRatio = a.getFloat(attr, 0.5f);
			            break;	
			        case R.styleable.SlideMenuStyle_menuShadow:
			        	menuShadow = a.getDimensionPixelSize(attr, 10);
			        	break;			        
			        case R.styleable.SlideMenuStyle_dragEdge:
			        	value = a.peekValue(attr);
			        	if(value.type == TypedValue.TYPE_DIMENSION)
			        		dragEdge = a.getDimensionPixelSize(attr, 30);
			        	else{
			        		dragEdge = a.getInt(attr, -1);
			        		if(dragEdge == -1)
			        			dragEdge = Integer.MAX_VALUE;
			        	}
			            break;
			        case R.styleable.SlideMenuStyle_touchSlop:
			        	touchSlop = a.getDimensionPixelSize(attr, 30);
			            break;    
			        case R.styleable.SlideMenuStyle_minAlpha:
			        	minAlpha = Math.max(0f, Math.min(1f, a.getFloat(attr, 0.3f)));
			            break;  
			        case R.styleable.SlideMenuStyle_velocitySlop:
			        	velocitySlop = Math.max(500f, a.getFloat(attr, 500f));
			            break; 
			        case R.styleable.SlideMenuStyle_closeEdge:
			        	value = a.peekValue(attr);
			        	if(value.type == TypedValue.TYPE_DIMENSION)
			        		closeEdge = a.getDimensionPixelSize(attr, 50);
			        	else
			        		closeEdgePercent = Math.max(0f, Math.min(1f, a.getFloat(attr, 0.75f)));
			            break; 
			        case R.styleable.SlideMenuStyle_animDuration:
			        	animDuration = Math.max(0, a.getInt(attr, 1000));
			            break; 
			    }
			}
			a.recycle();	
		}
	}
}