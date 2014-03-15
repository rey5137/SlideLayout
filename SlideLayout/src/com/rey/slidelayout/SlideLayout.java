package com.rey.slidelayout;

import java.lang.ref.WeakReference;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.widget.FrameLayout;

public class SlideLayout extends FrameLayout {
		
	private int mAction;
	private int mTarget;
	
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
	
	private int mViewWidth = -1;
	private int mViewHeight = -1;
	
	private int mLeftMenuChild = -1;
	private MenuStyle mLeftMenuStyle;
	
	private int mRightMenuChild = -1;
	private MenuStyle mRightMenuStyle;
	
	private int mTopMenuChild = -1;
	private MenuStyle mTopMenuStyle;
	
	private int mBottomMenuChild = -1;
	private MenuStyle mBottomMenuStyle;
	
	private int mOverlayChild = -1;
	private int mLeftShadowChild = -1;
	private int mRightShadowChild = -1;
	private int mTopShadowChild = -1;
	private int mBottomShadowChild = -1;
	
	private int mContentChild = -1;	
	private int mTotalChild = 0;
	private int mOffsetX = 0;
	private int mOffsetY = 0;
	private boolean mDragEnable = true;	
	private Interpolator mInterpolator;
		
	private float mDownX = -1;
	private float mDownY = -1;
	private float mPrevX;
	private float mPrevY;
	private float mDisX;
	private float mDisY;
	private boolean mStartDrag = false;
	
	public interface OnStateChangedListener{
		public void onStateChanged(View v, int old_state, int new_state);
		
		public void onOffsetChanged(View v, float offsetX, float offsetY, int state);
	}
	
	private WeakReference<OnStateChangedListener> mStateListener;
		
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
		
	private GestureDetector mGestureDetector;
	private GestureDetector.OnGestureListener mGestureListener = new GestureDetector.OnGestureListener() {
		
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
		
		TypedArray a = null;
		if(attrs != null)
			a = context.obtainStyledAttributes(attrs, R.styleable.SlideLayoutStyle, defStyle, R.style.SlideLayoutStyleDefault);		
		else
			a = context.obtainStyledAttributes(defStyle > 0 ? defStyle : R.style.SlideLayoutStyleDefault, R.styleable.SlideLayoutStyle);
		
		if(a != null){			 
			for (int i = 0, count = a.getIndexCount(); i < count; i++){
			    int attr = a.getIndex(i);
			    switch (attr){
				    case R.styleable.SlideLayoutStyle_sl_dragEnable:
			        	mDragEnable = a.getBoolean(attr, true);
			        	break;
			    	case R.styleable.SlideLayoutStyle_sl_contentChild:
			    		mContentChild = a.getInt(attr, -1);
			    		break;   
			    	case R.styleable.SlideLayoutStyle_sl_leftMenuChild:
			        	mLeftMenuChild = a.getInt(attr, -1);
			            break;
			        case R.styleable.SlideLayoutStyle_sl_rightMenuChild:
			        	mRightMenuChild = a.getInt(attr, -1);
			            break; 
			        case R.styleable.SlideLayoutStyle_sl_topMenuChild:
			        	mTopMenuChild = a.getInt(attr, -1);
			            break; 
			        case R.styleable.SlideLayoutStyle_sl_bottomMenuChild:
			        	mBottomMenuChild = a.getInt(attr, -1);
			            break; 
			        case R.styleable.SlideLayoutStyle_sl_menuStyle:
			        	menuStyleId = a.getResourceId(attr, 0);
			            break;
			        case R.styleable.SlideLayoutStyle_sl_leftMenuStyle:
			        	leftMenuStyleId = a.getResourceId(attr, 0);
			            break;
			        case R.styleable.SlideLayoutStyle_sl_rightMenuStyle:
			        	rightMenuStyleId = a.getResourceId(attr, 0);
			            break;
			        case R.styleable.SlideLayoutStyle_sl_topMenuStyle:
			        	topMenuStyleId = a.getResourceId(attr, 0);
			            break;
			        case R.styleable.SlideLayoutStyle_sl_bottomMenuStyle:
			        	bottomMenuStyleId = a.getResourceId(attr, 0);
			            break;
			    }
			}
			a.recycle();	
		}
		
		mTotalChild = 0;
		if(mContentChild >= 0)
			mTotalChild++;
		
		if(mLeftMenuChild >= 0){
			mLeftMenuStyle = new MenuStyle(context, leftMenuStyleId > 0 ? leftMenuStyleId : menuStyleId);
			mTotalChild += 2;
		}
				
		if(mRightMenuChild >= 0){
			mRightMenuStyle = new MenuStyle(context, rightMenuStyleId > 0 ? rightMenuStyleId : menuStyleId);
			mTotalChild += 2;
		}
		
		if(mTopMenuChild >= 0){
			mTopMenuStyle = new MenuStyle(context, topMenuStyleId > 0 ? topMenuStyleId : menuStyleId);
			mTotalChild += 2;
		}
		
		if(mBottomMenuChild >= 0){
			mBottomMenuStyle = new MenuStyle(context, bottomMenuStyleId > 0 ? bottomMenuStyleId : menuStyleId);
			mTotalChild += 2;
		}
		
		if(mTotalChild > 1)
			mTotalChild++;
				
		mGestureDetector = new GestureDetector(context, mGestureListener);
	}
	
	@Override
	public void addView(View child, int width, int height) {
		addView(child);		
	}

	@Override
	public void addView(View child, int index) {
		addView(child);
	}
	
	@Override
	public void addView(View child, ViewGroup.LayoutParams params) {
		addView(child, -1, params);
	}
	
	@Override
	public void addView(View child, int index, ViewGroup.LayoutParams params) {
		if(params == null)
			params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		
		if(getChildCount() == mTotalChild / 2 - 1){
			super.addView(child, index, params);
			addShadowView();
		}
		else
			super.addView(child, index, params);		
	}

	@Override
	public void addView(View child) {
		addView(child, null);
	}
	
	/**
	 * Attach this to an entire Activity
	 * @param activity the activity is attached
	 * @param attachToWindow true: attach this to decorView (include title bar), false: attach this to contentView
	 */
	public void attachToActivity(Activity activity, boolean attachToWindow) {
		TypedArray a = activity.getTheme().obtainStyledAttributes(new int[] {android.R.attr.windowBackground});
		int background = a.getResourceId(0, 0);
		a.recycle();

		if(attachToWindow){
			ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
			ViewGroup decorChild = (ViewGroup) decor.getChildAt(0);
			decorChild.setBackgroundResource(background);
			decor.removeView(decorChild);
			decor.addView(this, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));		
			this.addView(decorChild);
		}
		else{
			ViewGroup contentParent = (ViewGroup)activity.findViewById(android.R.id.content);
			View content = contentParent.getChildAt(0);
			contentParent.removeView(content);
			contentParent.addView(this, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
			this.addView(content);
			if (content.getBackground() == null)
				content.setBackgroundResource(background);
		}
	}
	
	@SuppressLint("NewApi")
	private void addShadowView(){
		int count = getChildCount();
		
		View v = new View(getContext());
		v.setBackgroundColor(0xFF000000);
		if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.HONEYCOMB)
			v.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		
		super.addView(v, -1, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
		
		mOverlayChild = count;
		count++;
		
		if(getLeftMenuView() != null && mLeftMenuStyle.mMenuShadow > 0){
			v = new View(getContext());
			v.setBackgroundResource(mLeftMenuChild > mContentChild ? R.drawable.sm_rightshadow : R.drawable.sm_leftshadow);
			super.addView(v, -1, new FrameLayout.LayoutParams(mLeftMenuStyle.mMenuShadow, FrameLayout.LayoutParams.MATCH_PARENT));
			
			mLeftShadowChild = count;
			count++;
		}
		
		if(getRightMenuView() != null && mRightMenuStyle.mMenuShadow > 0){
			v = new View(getContext());
			v.setBackgroundResource(mRightMenuChild > mContentChild ? R.drawable.sm_leftshadow : R.drawable.sm_rightshadow);	
			super.addView(v, -1, new FrameLayout.LayoutParams(mRightMenuStyle.mMenuShadow, FrameLayout.LayoutParams.MATCH_PARENT));
			
			mRightShadowChild = count;
			count++;
		}
		
		if(getTopMenuView() != null && mTopMenuStyle.mMenuShadow > 0){
			v = new View(getContext());
			v.setBackgroundResource(mTopMenuChild > mContentChild ? R.drawable.sm_bottomshadow : R.drawable.sm_topshadow);				
			super.addView(v, -1, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, mTopMenuStyle.mMenuShadow));
			
			mTopShadowChild = count;
			count++;
		}
		
		if(getBottomMenuView() != null && mBottomMenuStyle.mMenuShadow > 0){
			v = new View(getContext());
			v.setBackgroundResource(mBottomMenuChild > mContentChild ? R.drawable.sm_topshadow :R.drawable.sm_bottomshadow);
			super.addView(v, -1, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, mBottomMenuStyle.mMenuShadow));
			
			mBottomShadowChild = count;
			count++;
		}
	}
	
	public View getLeftMenuView(){		
		return getChildAt(mLeftMenuChild);
	}
	
	public View getRightMenuView(){		
		return getChildAt(mRightMenuChild);
	}
	
	public View getTopMenuView(){		
		return getChildAt(mTopMenuChild);
	}
	
	public View getBottomMenuView(){		
		return getChildAt(mBottomMenuChild);
	}
	
	public View getContentView(){		
		return getChildAt(mContentChild);
	}
		
	protected View getRightShadowView(){		
		return getChildAt(mRightShadowChild);
	}
	
	protected View getLeftShadowView(){		
		return getChildAt(mLeftShadowChild);
	}
	
	protected View getTopShadowView(){		
		return getChildAt(mTopShadowChild);
	}
	
	protected View getBottomShadowView(){		
		return getChildAt(mBottomShadowChild);
	}
	
	protected View getOverlayView(){		
		return getChildAt(mOverlayChild);
	}
			
	protected void setLeftMenuView(int viewWidth, int viewHeight){
		View menu = getLeftMenuView();
		if(menu == null)
			return;
	    
		if(mLeftMenuStyle.mMenuBorderPercent >= 0f)
			mLeftMenuStyle.mMenuBorder = (int)(viewWidth * mLeftMenuStyle.mMenuBorderPercent);
		if(mLeftMenuStyle.mMenuOverDragBorderPercent >= 0f)
			mLeftMenuStyle.mMenuOverDragBorder = (int)(viewWidth * mLeftMenuStyle.mMenuOverDragBorderPercent);	
		
		mLeftMenuStyle.mSize = viewWidth - mLeftMenuStyle.mMenuBorder;
		if(mLeftMenuStyle.mCloseEdgePercent >= 0f)
			mLeftMenuStyle.mCloseEdge = (int)(mLeftMenuStyle.mSize * mLeftMenuStyle.mCloseEdgePercent);
		
        menu.setLayoutParams(new FrameLayout.LayoutParams(mLeftMenuStyle.mSize, FrameLayout.LayoutParams.MATCH_PARENT));
        menu.setVisibility(mOffsetX <= 0 ? View.GONE : View.VISIBLE);	
	}
	
	protected void setRightMenuView(int viewWidth, int viewHeight){
		View menu = getRightMenuView();
		if(menu == null)
			return;
	    
		if(mRightMenuStyle.mMenuBorderPercent >= 0f)
			mRightMenuStyle.mMenuBorder = (int)(viewWidth * mRightMenuStyle.mMenuBorderPercent);
		if(mRightMenuStyle.mMenuOverDragBorderPercent >= 0f)
			mRightMenuStyle.mMenuOverDragBorder = (int)(viewWidth * mRightMenuStyle.mMenuOverDragBorderPercent);	
		
		mRightMenuStyle.mSize = viewWidth - mRightMenuStyle.mMenuBorder;
		if(mRightMenuStyle.mCloseEdgePercent >= 0f)
			mRightMenuStyle.mCloseEdge = (int)(mRightMenuStyle.mSize * mRightMenuStyle.mCloseEdgePercent);
		
		menu.setLayoutParams(new FrameLayout.LayoutParams(mRightMenuStyle.mSize, FrameLayout.LayoutParams.MATCH_PARENT));
		menu.setVisibility(mOffsetX >= 0 ? View.GONE : View.VISIBLE);
	}
	
	protected void setTopMenuView(int viewWidth, int viewHeight){
		View menu = getTopMenuView();
		if(menu == null)
			return;
	    
		if(mTopMenuStyle.mMenuBorderPercent >= 0f)
			mTopMenuStyle.mMenuBorder = (int)(viewWidth * mTopMenuStyle.mMenuBorderPercent);
		if(mTopMenuStyle.mMenuOverDragBorderPercent >= 0f)
			mTopMenuStyle.mMenuOverDragBorder = (int)(viewWidth * mTopMenuStyle.mMenuOverDragBorderPercent);
		
		mTopMenuStyle.mSize = viewHeight - mTopMenuStyle.mMenuBorder;
		if(mTopMenuStyle.mCloseEdgePercent >= 0f)
			mTopMenuStyle.mCloseEdge = (int)(mTopMenuStyle.mSize * mTopMenuStyle.mCloseEdgePercent);
		
		menu.setLayoutParams( new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, mTopMenuStyle.mSize));
		menu.setVisibility(mOffsetY <= 0 ? View.GONE : View.VISIBLE);
	}
	
	protected void setBottomMenuView(int viewWidth, int viewHeight){
		View menu = getBottomMenuView();
		if(menu == null)
			return;
	    
		if(mBottomMenuStyle.mMenuBorderPercent >= 0f)
			mBottomMenuStyle.mMenuBorder = (int)(viewWidth * mBottomMenuStyle.mMenuBorderPercent);
		if(mBottomMenuStyle.mMenuOverDragBorderPercent >= 0f)
			mBottomMenuStyle.mMenuOverDragBorder = (int)(viewWidth * mBottomMenuStyle.mMenuOverDragBorderPercent);
		
		mBottomMenuStyle.mSize = viewHeight - mBottomMenuStyle.mMenuBorder;
		if(mBottomMenuStyle.mCloseEdgePercent >= 0f)
			mBottomMenuStyle.mCloseEdge = (int)(mBottomMenuStyle.mSize * mBottomMenuStyle.mCloseEdgePercent);
		
		menu.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, mBottomMenuStyle.mSize));
		menu.setVisibility(mOffsetY >= 0 ? View.GONE : View.VISIBLE);
	}
	
	protected void setContentView(int viewWidth, int viewHeight){
		View content = getContentView();
		if(content == null)
			return;
	    
		content.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
	}
	
	protected void setShadow(int viewWidth, int viewHeight){
		setVisibility(getOverlayView(), View.GONE);
		setVisibility(getLeftShadowView(), mOffsetX <= 0 ? View.GONE : View.VISIBLE);
		setVisibility(getRightShadowView(), mOffsetX >= 0 ? View.GONE : View.VISIBLE);
		setVisibility(getTopShadowView(), mOffsetY <= 0 ? View.GONE : View.VISIBLE);
		setVisibility(getBottomShadowView(), mOffsetY >= 0 ? View.GONE : View.VISIBLE);
	}
		
	/**
	 * Check if SlideMenu can be dragged or not
	 */
	public boolean isDragEnable(){
		return mDragEnable;
	}
	
	/**
	 * Set SlideMenu can be dragged or not
	 */
	public void setDragEnable(boolean enable){
		mDragEnable = enable;
	}
		
	public void setOnStateChangedListener(OnStateChangedListener listener){
		if(listener == null)
			mStateListener = null;
		
		mStateListener = new WeakReference<OnStateChangedListener>(listener);
	}
	
	public boolean dispatchTouchEvent(MotionEvent event){			
		if(mAction == ACTION_SHOW){
			switch (mTarget) {
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
		else if(mAction == ACTION_DRAG){
			switch (mTarget) {
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
		if(!mDragEnable)
			return super.dispatchTouchEvent(event);
		
		if(event.getAction() ==  MotionEvent.ACTION_DOWN){
			if((mLeftMenuStyle != null && event.getX() < mLeftMenuStyle.mDragEdge) 
					|| (mRightMenuStyle != null && event.getX() > mViewWidth - mRightMenuStyle.mDragEdge)
					|| (mTopMenuStyle != null && event.getY() < mTopMenuStyle.mDragEdge)
					|| (mBottomMenuStyle != null && event.getY() > mViewHeight - mBottomMenuStyle.mDragEdge)){
				mDownX = event.getX();
				mDownY = event.getY();
				mPrevX = mDownX;
				mPrevY = mDownY;
				mDisX = 0f;
				mDisY = 0f;
				super.dispatchTouchEvent(event);
				return true;
			}
		}
		else if(event.getAction() ==  MotionEvent.ACTION_UP){
			mDownX = -1;		
			mDownY = -1;
		}
		else if(event.getAction() == MotionEvent.ACTION_MOVE && mDownX > 0 && mDownY > 0){
			if(getLeftMenuView() != null && mDownX < mLeftMenuStyle.mDragEdge){
				if(event.getX() >= mPrevX){
					mDisX += event.getX() - mPrevX;
					mPrevX = event.getX();
					mPrevY = event.getY();
					
					if(mDisX > mLeftMenuStyle.mTouchSlop){		
						mOffsetX = Math.min(mLeftMenuStyle.mSize, Math.max(0, (int)(mOffsetX + mDisX)));
						offsetViewX(mOffsetX);										
										
						setState(mOffsetX < mLeftMenuStyle.mSize ? ACTION_DRAG : ACTION_SHOW, TARGET_LEFT, OP.START_DRAG_LEFT_FROM_CONTENT);
						boolean result = cancelMotionEvent(event, null);
												
						dispatchOffsetChangedEvent((float)mOffsetX / (float)mLeftMenuStyle.mSize, 0f);
						
						mStartDrag = true;						
						return result;								
					}
				}
				else
					mDownX = -1;
			}
			else if(getRightMenuView() != null && mDownX > mViewWidth - mRightMenuStyle.mDragEdge){
				if(event.getX() <= mPrevX){
					mDisX += mPrevX - event.getX();
					mPrevX = event.getX();
					mPrevY = event.getY();
					
					if(mDisX > mRightMenuStyle.mTouchSlop){								
						mOffsetX = Math.max(-mRightMenuStyle.mSize, Math.min(0, (int)(mOffsetX - mDisX)));
						offsetViewX(mOffsetX);
						
						setState(mOffsetX > -mRightMenuStyle.mSize ? ACTION_DRAG : ACTION_SHOW, TARGET_RIGHT, OP.START_DRAG_RIGHT_FROM_CONTENT);
						boolean result = cancelMotionEvent(event, null);
												
						dispatchOffsetChangedEvent((float)-mOffsetX / (float)mRightMenuStyle.mSize, 0f);
						
						mStartDrag = true;
						return result;								
					}
				}
				else
					mDownX = -1;
			}
			else if(getTopMenuView() != null && mDownY < mTopMenuStyle.mDragEdge){
				if(event.getY() >= mPrevY){
					mDisY += event.getY() - mPrevY;
					mPrevY = event.getY();
					mPrevX = event.getX();
					
					if(mDisY > mTopMenuStyle.mTouchSlop){
						mOffsetY = Math.min(mTopMenuStyle.mSize, Math.max(0, (int)(mOffsetY + mDisY)));
						offsetViewY(mOffsetY);		
						
						setState(mOffsetY < mTopMenuStyle.mSize ? ACTION_DRAG : ACTION_SHOW, TARGET_TOP, OP.START_DRAG_TOP_FROM_CONTENT);
						boolean result = cancelMotionEvent(event, null);
												
						dispatchOffsetChangedEvent(0f, (float)mOffsetY / (float)mTopMenuStyle.mSize);
						
						mStartDrag = true;
						return result;
					}
				}
				else
					mDownY = -1;
			}
			else if(getBottomMenuView() != null && mDownY > mViewHeight - mBottomMenuStyle.mDragEdge){
				if(event.getY() <= mPrevY){
					mDisY += mPrevY - event.getY();
					mPrevX = event.getX();
					mPrevY = event.getY();
					
					if(mDisY > mBottomMenuStyle.mTouchSlop){
						mOffsetY = Math.max(-mBottomMenuStyle.mSize, Math.min(0, (int)(mOffsetY - mDisY)));
						offsetViewY(mOffsetY);
						
						setState(mOffsetY > -mBottomMenuStyle.mSize ? ACTION_DRAG : ACTION_SHOW, TARGET_BOTTOM, OP.START_DRAG_BOTTOM_FROM_CONTENT);
						boolean result = cancelMotionEvent(event, null);
												
						dispatchOffsetChangedEvent(0f, (float)-mOffsetY / (float)mBottomMenuStyle.mSize);
						
						mStartDrag = true;
						return result;								
					}
				}
				else
					mDownY = -1;
			}
		}		
			
		return super.dispatchTouchEvent(event);
	}
	
	private boolean dispatchTouchEventStateShowLeftMenu(MotionEvent event){
		if(mLeftMenuStyle.mMenuBorder == 0){
			if(event.getAction() ==  MotionEvent.ACTION_DOWN){
				if(event.getX() > mViewWidth - mLeftMenuStyle.mDragEdge){
					mDownX = event.getX();
					mDownY = event.getY();
					mPrevX = mDownX;
					mPrevY = mDownY;
					mDisX = 0f;
					mDisY = 0f;
				}
			}
			else if(event.getAction() ==  MotionEvent.ACTION_UP){
				mDownX = -1;		
				mDownY = -1;
			}
			else if(event.getAction() == MotionEvent.ACTION_MOVE && mDownX > 0 && mDownY > 0){
				if(event.getX() <= mPrevX){
					mDisX += mPrevX - event.getX();
					mPrevX = event.getX();
					mPrevY = event.getY();
					
					if(mDisX > mLeftMenuStyle.mTouchSlop){								
						mOffsetX = Math.min(mLeftMenuStyle.mSize, Math.max(0, (int)(event.getX())));
						offsetViewX(mOffsetX);		
						
						setState(mOffsetX < mLeftMenuStyle.mSize ? ACTION_DRAG : ACTION_SHOW, TARGET_LEFT, OP.START_DRAG_LEFT_FROM_CONTENT);
						boolean result = cancelMotionEvent(event, getLeftMenuView());
												
						dispatchOffsetChangedEvent((float)mOffsetX / (float)mLeftMenuStyle.mSize, 0f);												
						return result;							
					}
				}
				else
					mDownX = -1;
			}
		}
		
		if(!mDragEnable || !mGestureDetector.onTouchEvent(event)){
			if(!getLeftMenuView().dispatchTouchEvent(event))
				return super.dispatchTouchEvent(event);
		}		
		else
			getLeftMenuView().dispatchTouchEvent(event);	
		
		return true; 
	}
	
	private boolean dispatchTouchEventStateDragLeftMenu(MotionEvent event){
		if(!mGestureDetector.onTouchEvent(event) && (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)){
			
			if(mOffsetX < mLeftMenuStyle.mCloseEdge)
				closeLeftMenu(true);					
			else
				openLeftMenu(true);
		}
		
		return true;
	}
	
	private boolean dispatchTouchEventStateShowRightMenu(MotionEvent event){
		if(mRightMenuStyle.mMenuBorder == 0){
			if(event.getAction() ==  MotionEvent.ACTION_DOWN){
				if(event.getX() < mRightMenuStyle.mDragEdge){
					mDownX = event.getX();
					mDownY = event.getY();
					mPrevX = mDownX;
					mPrevY = mDownY;
					mDisX = 0f;
					mDisY = 0f;
				}
			}
			else if(event.getAction() ==  MotionEvent.ACTION_UP){
				mDownX = -1;		
				mDownY = -1;
			}
			else if(event.getAction() == MotionEvent.ACTION_MOVE && mDownX > 0 && mDownY > 0){
				if(event.getX() >= mPrevX){
					mDisX += event.getX() - mPrevX;
					mPrevX = event.getX();
					mPrevY = event.getY();
					
					if(mDisX > mRightMenuStyle.mTouchSlop){
						mOffsetX = Math.max(-mRightMenuStyle.mSize, Math.min(0, (int)(event.getX() - mViewWidth)));
						offsetViewX(mOffsetX);
						
						setState(mOffsetX > -mRightMenuStyle.mSize ? ACTION_DRAG : ACTION_SHOW, TARGET_RIGHT, OP.START_DRAG_RIGHT_FROM_CONTENT);
						boolean result = cancelMotionEvent(event, getRightMenuView());
												
						dispatchOffsetChangedEvent((float)-mOffsetX / (float)mRightMenuStyle.mSize, 0f);
						
						return result;		
					}
				}
				else
					mDownX = -1;
			}
		}
		
		if(!mDragEnable || !mGestureDetector.onTouchEvent(event)){
			event.offsetLocation(-mRightMenuStyle.mMenuBorder, 0);
			
			if(!getRightMenuView().dispatchTouchEvent(event)){
				event.offsetLocation(mRightMenuStyle.mMenuBorder, 0);
				return super.dispatchTouchEvent(event);
			}
		}		
		else{
			event.offsetLocation(-mRightMenuStyle.mMenuBorder, 0);
			getRightMenuView().dispatchTouchEvent(event);
			event.offsetLocation(mRightMenuStyle.mMenuBorder, 0);
		}
		
		return true; 		
	}
	
	private boolean dispatchTouchEventStateDragRightMenu(MotionEvent event){
		if(!mGestureDetector.onTouchEvent(event) && (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)){
			
			if(mOffsetX > -mRightMenuStyle.mCloseEdge)
				closeRightMenu(true);
			else
				openRightMenu(true);
		}
		
		return true;
	}
	
	private boolean dispatchTouchEventStateShowTopMenu(MotionEvent event){
		if(mTopMenuStyle.mMenuBorder == 0){
			if(event.getAction() ==  MotionEvent.ACTION_DOWN){
				if(event.getY() > mViewHeight - mTopMenuStyle.mDragEdge){
					mDownX = event.getX();
					mDownY = event.getY();
					mPrevX = mDownX;
					mPrevY = mDownY;
					mDisX = 0f;
					mDisY = 0f;
				}
			}
			else if(event.getAction() ==  MotionEvent.ACTION_UP){
				mDownX = -1;		
				mDownY = -1;
			}
			else if(event.getAction() == MotionEvent.ACTION_MOVE && mDownX > 0 && mDownY > 0){
				if(event.getY() <= mPrevY){
					mDisY += mPrevY - event.getY();
					mPrevX = event.getX();
					mPrevY = event.getY();
					
					if(mDisY > mTopMenuStyle.mTouchSlop){
						mOffsetY = Math.min(mTopMenuStyle.mSize, Math.max(0, (int)(event.getY())));
						offsetViewY(mOffsetY);		
						
						setState(mOffsetY < mTopMenuStyle.mSize ? ACTION_DRAG : ACTION_SHOW, TARGET_TOP, OP.START_DRAG_TOP_FROM_CONTENT);
						boolean result = cancelMotionEvent(event, getTopMenuView());
												
						dispatchOffsetChangedEvent(0f, (float)mOffsetY / (float)mTopMenuStyle.mSize);
						
						return result;							
					}
				}
				else
					mDownY = -1;
			}
		}
		
		if(!mDragEnable || !mGestureDetector.onTouchEvent(event)){
			if(!getTopMenuView().dispatchTouchEvent(event))
				return super.dispatchTouchEvent(event);
		}		
		else
			getTopMenuView().dispatchTouchEvent(event);	
		
		return true; 
		
//		if(!getTopMenuView().dispatchTouchEvent(event)){
//			if(!dragEnable)
//				return super.dispatchTouchEvent(event);
//			else if(!gestureDetector.onTouchEvent(event))
//				return super.dispatchTouchEvent(event);
//		}
//		
//		return true;
	}
	
	private boolean dispatchTouchEventStateDragTopMenu(MotionEvent event){
		if(!mGestureDetector.onTouchEvent(event) && (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)){
			
			if(mOffsetY < mTopMenuStyle.mCloseEdge)
				closeTopMenu(true);
			else
				openTopMenu(true);
		}
		
		return true;
	}
	
	private boolean dispatchTouchEventStateShowBottomMenu(MotionEvent event){
		if(mBottomMenuStyle.mMenuBorder == 0){
			if(event.getAction() ==  MotionEvent.ACTION_DOWN){
				if(event.getY() < mBottomMenuStyle.mDragEdge){
					mDownX = event.getX();
					mDownY = event.getY();
					mPrevX = mDownX;
					mPrevY = mDownY;
					mDisX = 0f;
					mDisY = 0f;
				}
			}
			else if(event.getAction() ==  MotionEvent.ACTION_UP){
				mDownX = -1;		
				mDownY = -1;
			}
			else if(event.getAction() == MotionEvent.ACTION_MOVE && mDownX > 0 && mDownY > 0){
				if(event.getY() >= mPrevY){
					mDisY += event.getY() - mPrevY;
					mPrevY = event.getY();
					mPrevX = event.getX();
					
					if(mDisY > mBottomMenuStyle.mTouchSlop){
						mOffsetY = Math.max(-mBottomMenuStyle.mSize, Math.min(0, (int)(event.getY() - mViewHeight)));
						offsetViewY(mOffsetY);
						
						setState(mOffsetY > -mBottomMenuStyle.mSize ? ACTION_DRAG : ACTION_SHOW, TARGET_BOTTOM, OP.START_DRAG_BOTTOM_FROM_CONTENT);
						boolean result = cancelMotionEvent(event, getBottomMenuView());
												
						dispatchOffsetChangedEvent(0f, (float)-mOffsetY / (float)mBottomMenuStyle.mSize);
						
						return result;	
					}
				}
				else
					mDownY = -1;
			}
		}
		
		if(!mDragEnable || !mGestureDetector.onTouchEvent(event)){
			event.offsetLocation(0, -mBottomMenuStyle.mMenuBorder);
			
			if(!getBottomMenuView().dispatchTouchEvent(event)){
				event.offsetLocation(0, mBottomMenuStyle.mMenuBorder);
				return super.dispatchTouchEvent(event);
			}
		}		
		else{
			event.offsetLocation(0, -mBottomMenuStyle.mMenuBorder);
			getBottomMenuView().dispatchTouchEvent(event);
			event.offsetLocation(0, mBottomMenuStyle.mMenuBorder);
		}
		
		return true; 
	}
	
	private boolean dispatchTouchEventStateDragBottomMenu(MotionEvent event){
		if(!mGestureDetector.onTouchEvent(event) && (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)){
			
			if(mOffsetY > -mBottomMenuStyle.mCloseEdge)
				closeBottomMenu(true);
			else
				openBottomMenu(true);
		}
		
		return true;
	}
	
	protected boolean onSingleTapUp(MotionEvent e) {		
		if(mAction != ACTION_SHOW)
			return false;
		
		switch (mTarget) {
			case TARGET_LEFT:
				if(e.getX() > mLeftMenuStyle.mSize){
					closeLeftMenu(true);
					return true;
				}
				break;
			case TARGET_RIGHT:
				
				if(e.getX() < mViewWidth - mRightMenuStyle.mSize){
					closeRightMenu(true);
					return true;
				}
				break;	
			case TARGET_TOP:
				if(e.getY() > mTopMenuStyle.mSize){
					closeTopMenu(true);
					return true;
				}
				break;	
			case TARGET_BOTTOM:
				if(e.getY() < mViewHeight - mBottomMenuStyle.mSize){
					closeBottomMenu(true);
					return true;
				}
				break;
		}
		
		return false;
	}
	
	protected boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		if(mAction == ACTION_SHOW){
			switch (mTarget) {
				case TARGET_LEFT:				
					if(e1 != null && e1.getX() > mLeftMenuStyle.mSize && distanceX > 0){
						mOffsetX =  Math.min(mLeftMenuStyle.mSize, Math.max(0, mOffsetX - (int)distanceX));
						offsetViewX(mOffsetX);
						
						cancelMotionEvent(e2, null);
												
						if(mOffsetX > 0)
							setState(ACTION_DRAG, TARGET_LEFT, OP.START_DRAG_LEFT_FROM_MENU);
						else
							setState(ACTION_SHOW, TARGET_CONTENT, OP.START_DRAG_LEFT_FROM_MENU);
						
						dispatchOffsetChangedEvent((float)mOffsetX / (float)mLeftMenuStyle.mSize, 0f);					
						return true;
					}	
					break;
				case TARGET_RIGHT:				
					if(e1 != null && e1.getX() < mViewWidth - mRightMenuStyle.mSize && distanceX < 0){					
						mOffsetX =  Math.max(-mRightMenuStyle.mSize, Math.min(0, mOffsetX - (int)distanceX));
						offsetViewX(mOffsetX);
						
						cancelMotionEvent(e2, null);
												
						if(mOffsetX < 0)
							setState(ACTION_DRAG, TARGET_RIGHT, OP.START_DRAG_RIGHT_FROM_MENU);
						else
							setState(ACTION_SHOW, TARGET_CONTENT, OP.START_DRAG_RIGHT_FROM_MENU);
						dispatchOffsetChangedEvent((float)-mOffsetX / (float)mRightMenuStyle.mSize, 0f);	
						return true;
					}	
					break;	
				case TARGET_TOP:				
					if(e1 != null && e1.getY() > mTopMenuStyle.mSize && distanceY > 0){					
						mOffsetY =  Math.min(mTopMenuStyle.mSize, Math.max(0, mOffsetY - (int)distanceY));
						offsetViewY(mOffsetY);
						
						cancelMotionEvent(e2, null);
												
						if(mOffsetY > 0)
							setState(ACTION_DRAG, TARGET_TOP, OP.START_DRAG_TOP_FROM_MENU);
						else
							setState(ACTION_SHOW, TARGET_CONTENT, OP.START_DRAG_TOP_FROM_MENU);						
						dispatchOffsetChangedEvent(0f, (float)mOffsetY / (float)mTopMenuStyle.mSize);	
						return true;
					}	
					break;
				case TARGET_BOTTOM:				
					if(e1 != null && e1.getY() < mViewHeight - mBottomMenuStyle.mSize && distanceY < 0){					
						mOffsetY =  Math.max(-mBottomMenuStyle.mSize, Math.min(0, mOffsetY - (int)distanceY));
						offsetViewY(mOffsetY);					
						
						cancelMotionEvent(e2, null);
												
						if(mOffsetY < 0)
							setState(ACTION_DRAG, TARGET_BOTTOM, OP.START_DRAG_BOTTOM_FROM_MENU);
						else
							setState(ACTION_SHOW, TARGET_CONTENT, OP.START_DRAG_BOTTOM_FROM_MENU);	
						dispatchOffsetChangedEvent(0f, (float)-mOffsetY / (float)mBottomMenuStyle.mSize);	
						return true;
					}	
					break;
			}
		}
		else if(mAction == ACTION_DRAG){
			switch (mTarget) {
				case TARGET_LEFT:
					if(mStartDrag){
						mStartDrag = false;
						return true;
					}
					mOffsetX = Math.min(mLeftMenuStyle.mOverDrag ? mViewWidth - mLeftMenuStyle.mMenuOverDragBorder : mLeftMenuStyle.mSize, Math.max(0, mOffsetX - (int)distanceX));
					offsetViewX(mOffsetX);				
					dispatchOffsetChangedEvent((float)mOffsetX / (float)mLeftMenuStyle.mSize, 0f);	
					return true;
				case TARGET_RIGHT:
					if(mStartDrag){
						mStartDrag = false;
						return true;
					}				
					mOffsetX = Math.max(mRightMenuStyle.mOverDrag ? mRightMenuStyle.mMenuOverDragBorder - mViewWidth : -mRightMenuStyle.mSize, Math.min(0, mOffsetX - (int)distanceX));
					offsetViewX(mOffsetX);
					dispatchOffsetChangedEvent((float)-mOffsetX / (float)mRightMenuStyle.mSize, 0f);	
					return true;	
				case TARGET_TOP:
					if(mStartDrag){
						mStartDrag = false;
						return true;
					}	
					mOffsetY = Math.min(mTopMenuStyle.mOverDrag ? mViewHeight - mTopMenuStyle.mMenuOverDragBorder : mTopMenuStyle.mSize, Math.max(0, mOffsetY - (int)distanceY));
					offsetViewY(mOffsetY);
					dispatchOffsetChangedEvent(0f, (float)mOffsetY / (float)mTopMenuStyle.mSize);	
					return true;
				case TARGET_BOTTOM:
					if(mStartDrag){
						mStartDrag = false;
						return true;
					}				
					mOffsetY = Math.max(mBottomMenuStyle.mOverDrag ? mBottomMenuStyle.mMenuOverDragBorder - mViewHeight : -mBottomMenuStyle.mSize, Math.min(0, mOffsetY - (int)distanceY));
					offsetViewY(mOffsetY);
					dispatchOffsetChangedEvent(0f, (float)-mOffsetY / (float)mBottomMenuStyle.mSize);	
					return true;
			}
		}		
		return false;
	}
	
	protected boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if(mAction == ACTION_SHOW || mAction == ACTION_DRAG){
			switch (mTarget) {
				case TARGET_LEFT:
					if(velocityX > mLeftMenuStyle.mVelocitySlop){
						if(mOffsetX != mLeftMenuStyle.mSize)
							openLeftMenu(true);
						else
							setState(ACTION_SHOW, TARGET_LEFT, OP.FLING_LEFT);	
						return true;
					}
					else if(velocityX < -mLeftMenuStyle.mVelocitySlop){
						if(mOffsetX > 0)
							closeLeftMenu(true);
						else
							setState(ACTION_SHOW, TARGET_CONTENT, OP.FLING_LEFT);
						return true;
					}
					break;
				case TARGET_RIGHT:					
					if(velocityX < -mRightMenuStyle.mVelocitySlop){
						if(mOffsetX != -mRightMenuStyle.mSize)
							openRightMenu(true);
						else
							setState(ACTION_SHOW, TARGET_RIGHT, OP.FLING_RIGHT);	
						return true;
					}
					else if(velocityX > mRightMenuStyle.mVelocitySlop){
						if(mOffsetX < 0)
							closeRightMenu(true);
						else
							setState(ACTION_SHOW, TARGET_CONTENT, OP.FLING_RIGHT);	
						return true;
					}
					break;	
				case TARGET_TOP:
					if(velocityY > mTopMenuStyle.mVelocitySlop){
						if(mOffsetY != mTopMenuStyle.mSize)
							openTopMenu(true);
						else
							setState(ACTION_SHOW, TARGET_TOP, OP.FLING_TOP);	
						return true;
					}
					else if(velocityY < -mTopMenuStyle.mVelocitySlop){
						if(mOffsetY > 0)
							closeTopMenu(true);
						else
							setState(ACTION_SHOW, TARGET_CONTENT, OP.FLING_TOP);
						return true;
					}
					break;
				case TARGET_BOTTOM:
					if(velocityY < -mBottomMenuStyle.mVelocitySlop){
						if(mOffsetY != -mBottomMenuStyle.mSize)
							openBottomMenu(true);
						else
							setState(ACTION_SHOW, TARGET_BOTTOM, OP.FLING_BOTTOM);	
						return true;
					}
					else if(velocityY > mBottomMenuStyle.mVelocitySlop){
						if(mOffsetY < 0)
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
	
	/**
	 * Close any menu if it is opened
	 * @param animation true: show closing animation, false: close immediately
	 */
	public void closeAllMenu(boolean animation){
		if(isState(ACTION_SHOW, TARGET_CONTENT))
			return;
		
		switch (mTarget) {
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
		
		long duration = (long)Math.abs(mLeftMenuStyle.mAnimDuration * ((float)(mLeftMenuStyle.mSize - mOffsetX) / (float)mLeftMenuStyle.mSize));		
		getLeftMenuView().clearAnimation();
		
		if(animation && duration > 0){
			setState(ACTION_OPEN, TARGET_LEFT, OP.OPEN_LEFT);	
			SlideAnimation anim = new SlideAnimation(true);
			anim.setDuration(duration);
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
			this.startAnimation(anim);
		}
		else{	
			setState(ACTION_SHOW, TARGET_LEFT, OP.OPEN_LEFT);	
			mOffsetX = mLeftMenuStyle.mSize;
			offsetViewX(mOffsetX);
		}		
	}
	
	public void closeLeftMenu(boolean animation){
		if(getLeftMenuView() == null || (!isState(ACTION_SHOW, TARGET_LEFT) && !isState(ACTION_DRAG, TARGET_LEFT)))
			return;
		
		long duration = (long)(mLeftMenuStyle.mAnimDuration * ((float)mOffsetX / (float)mLeftMenuStyle.mSize));				
		getLeftMenuView().clearAnimation();
		
		if(animation && duration > 0){
			setState(ACTION_CLOSE, TARGET_LEFT, OP.CLOSE_LEFT);
			SlideAnimation anim = new SlideAnimation(false);
			anim.setDuration(duration);
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
			this.startAnimation(anim);
		}
		else{		
			setState(ACTION_SHOW, TARGET_CONTENT, OP.CLOSE_LEFT);
			mOffsetX = 0;
			offsetViewX(mOffsetX);	
		}	
	}
	
	public void openRightMenu(boolean animation){
		if(getRightMenuView() == null || (!isState(ACTION_SHOW, TARGET_CONTENT) && !isState(ACTION_DRAG, TARGET_RIGHT)))
			return;
				
		long duration = (long)Math.abs(mRightMenuStyle.mAnimDuration * ((float)(mOffsetX + mRightMenuStyle.mSize) / (float)mRightMenuStyle.mSize));		
		getRightMenuView().clearAnimation();
		
		if(animation && duration > 0){
			setState(ACTION_OPEN, TARGET_RIGHT, OP.OPEN_RIGHT);	
			SlideAnimation anim = new SlideAnimation(true);
			anim.setDuration(duration);
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
			this.startAnimation(anim);
		}
		else{		
			setState(ACTION_SHOW, TARGET_RIGHT, OP.OPEN_RIGHT);	
			mOffsetX = -mRightMenuStyle.mSize;
			offsetViewX(mOffsetX);
		}
	}
			
	public void closeRightMenu(boolean animation){
		if(getRightMenuView() == null || (!isState(ACTION_SHOW, TARGET_RIGHT) && !isState(ACTION_DRAG, TARGET_RIGHT)))
			return;
		
		long duration = (long)(mRightMenuStyle.mAnimDuration * ((float)-mOffsetX/ (float)mRightMenuStyle.mSize));		
		getRightMenuView().clearAnimation();
		
		if(animation && duration > 0){
			setState(ACTION_CLOSE, TARGET_RIGHT, OP.CLOSE_RIGHT);
			SlideAnimation anim = new SlideAnimation(false);
			anim.setDuration(duration);
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
			this.startAnimation(anim);
		}
		else{
			setState(ACTION_SHOW, TARGET_CONTENT, OP.CLOSE_RIGHT);
			mOffsetX = 0;
			offsetViewX(mOffsetX);		
		}
	}
	
	public void openTopMenu(boolean animation){		
		if(getTopMenuView() == null || (!isState(ACTION_SHOW, TARGET_CONTENT) && !isState(ACTION_DRAG, TARGET_TOP)))
			return;
		
		long duration = (long)Math.abs(mTopMenuStyle.mAnimDuration * ((float)(mTopMenuStyle.mSize - mOffsetY) / (float)mTopMenuStyle.mSize));		
		getTopMenuView().clearAnimation();
		
		if(animation && duration > 0){
			setState(ACTION_OPEN, TARGET_TOP, OP.OPEN_TOP);	
			SlideAnimation anim = new SlideAnimation(true);
			anim.setDuration(duration);
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
			this.startAnimation(anim);
		}
		else{	
			setState(ACTION_SHOW, TARGET_TOP, OP.OPEN_TOP);	
			mOffsetY = mTopMenuStyle.mSize;
			offsetViewY(mOffsetY);
		}
	}
	
	public void closeTopMenu(boolean animation){
		if(getTopMenuView() == null || (!isState(ACTION_SHOW, TARGET_TOP) && !isState(ACTION_DRAG, TARGET_TOP)))
			return;
		
		long duration = (long)(mTopMenuStyle.mAnimDuration * ((float)mOffsetY / (float)mTopMenuStyle.mSize));		
		getTopMenuView().clearAnimation();
		
		if(animation && duration > 0){	
			setState(ACTION_CLOSE, TARGET_TOP, OP.CLOSE_TOP);
			SlideAnimation anim = new SlideAnimation(false);
			anim.setDuration(duration);
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
			this.startAnimation(anim);
		}
		else{	
			setState(ACTION_SHOW, TARGET_CONTENT, OP.CLOSE_TOP);
			mOffsetY = 0;
			offsetViewY(mOffsetY);
		}
	}
	
	public void openBottomMenu(boolean animation){
		if(getBottomMenuView() == null || (!isState(ACTION_SHOW, TARGET_CONTENT) && !isState(ACTION_DRAG, TARGET_BOTTOM)))
			return;
		
		long duration = (long)Math.abs(mBottomMenuStyle.mAnimDuration * ((float)(mOffsetY + mBottomMenuStyle.mSize) / (float)mBottomMenuStyle.mSize));		
		getBottomMenuView().clearAnimation();
		
		if(animation && duration > 0){	
			setState(ACTION_OPEN, TARGET_BOTTOM, OP.OPEN_BOTTOM);	
			SlideAnimation anim = new SlideAnimation(true);
			anim.setDuration(duration);
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
			this.startAnimation(anim);
		}
		else{	
			setState(ACTION_SHOW, TARGET_BOTTOM, OP.OPEN_BOTTOM);
			mOffsetY = -mBottomMenuStyle.mSize;
			offsetViewY(mOffsetY);
		}
	}
			
	public void closeBottomMenu(boolean animation){
		if(getBottomMenuView() == null || (!isState(ACTION_SHOW, TARGET_BOTTOM) && !isState(ACTION_DRAG, TARGET_BOTTOM)))
			return;
		
		long duration = (long)(mBottomMenuStyle.mAnimDuration * ((float)-mOffsetY/ (float)mBottomMenuStyle.mSize));		
		getBottomMenuView().clearAnimation();
		
		if(animation && duration > 0){
			setState(ACTION_CLOSE, TARGET_BOTTOM, OP.CLOSE_BOTTOM);
			SlideAnimation anim = new SlideAnimation(false);
			anim.setDuration(duration);
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
			this.startAnimation(anim);
		}
		else{	
			setState(ACTION_SHOW, TARGET_CONTENT, OP.CLOSE_BOTTOM);
			mOffsetY = 0;
			offsetViewY(mOffsetY);
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
		return getState(mAction, mTarget);
	}
	
	public synchronized boolean isState(int action, int target){
		return this.mAction == action && this.mTarget == target;
	}
	
	private synchronized void setState(int action, int target, OP op){
		int prev_action = this.mAction;
		int prev_target = this.mTarget;
		
		this.mAction = action;
		this.mTarget = target;
		
		if(mStateListener != null && mStateListener.get() != null)
			mStateListener.get().onStateChanged(this, getState(prev_action, prev_target), getState(action, target));		
		
		mDownX = -1;
		mDownY = -1;			
		
		if(action == ACTION_DRAG){
			if(getParent() != null)
				getParent().requestDisallowInterceptTouchEvent(true);
		}
		
		if(action == ACTION_SHOW){
			if(target != TARGET_CONTENT)
				mStartDrag = false;
			else{
				switch (prev_target) {
					case TARGET_LEFT:
						setVisibility(getLeftMenuView(), View.GONE);
						setVisibility(getLeftShadowView(), View.GONE);
						break;
					case TARGET_RIGHT:
						setVisibility(getRightMenuView(), View.GONE);
						setVisibility(getRightShadowView(), View.GONE);
						break;
					case TARGET_TOP:
						setVisibility(getTopMenuView(), View.GONE);
						setVisibility(getTopShadowView(), View.GONE);
						break;
					case TARGET_BOTTOM:
						setVisibility(getBottomMenuView(), View.GONE);
						setVisibility(getBottomShadowView(), View.GONE);
						break;
				}
			}				
		}			
	}
	
	private void dispatchOffsetChangedEvent(float offsetX, float offsetY){
		if(mStateListener != null && mStateListener.get() != null)
			mStateListener.get().onOffsetChanged(this, offsetX, offsetY, getState(mAction, mTarget));
	}
			
	private void offsetViewX(int offsetX){
		View content = getContentView();
		View menu;
		View shadow;
		View overlay = getOverlayView();
		
		if(mTarget == TARGET_LEFT){			
			menu = getLeftMenuView();
			shadow = getLeftShadowView();
			
			if(mLeftMenuChild > mContentChild){
				int left_menu = Math.min(0, offsetX - mLeftMenuStyle.mSize) + getPaddingLeft();
				int left_content = (int)(offsetX * mLeftMenuStyle.mSlideRatio) + getPaddingLeft();
				
				offsetLeftAndRight(content, left_content - content.getLeft());			
				offsetLeftAndRight(menu, left_menu - menu.getLeft());	
				offsetLeftAndRight(overlay, left_menu + mLeftMenuStyle.mSize - overlay.getLeft());
				offsetLeftAndRight(shadow, left_menu + mLeftMenuStyle.mSize - shadow.getLeft());		
				
				setDim(1f - (float)offsetX / (float)mLeftMenuStyle.mSize, mLeftMenuStyle.mMaxDim);
			}
			else{			
				int left_menu = (int)((offsetX - mLeftMenuStyle.mSize) * mLeftMenuStyle.mSlideRatio) + getPaddingLeft();
				int left_content = offsetX + getPaddingLeft();
				
				offsetLeftAndRight(content, left_content - content.getLeft());						
				offsetLeftAndRight(menu, left_menu - menu.getLeft());
				offsetLeftAndRight(overlay, left_content - mViewWidth - overlay.getLeft());
				offsetLeftAndRight(shadow, left_content - mLeftMenuStyle.mMenuShadow- shadow.getLeft());
				
				setDim((float)offsetX / (float)mLeftMenuStyle.mSize, mLeftMenuStyle.mMaxDim);
			}		
			
			if(offsetX > 0){
				setVisibility(overlay, View.VISIBLE);
				setVisibility(menu, View.VISIBLE);
				setVisibility(shadow, View.VISIBLE);
			}
			else{
				setVisibility(overlay, View.GONE);
				setVisibility(menu, View.GONE);
				setVisibility(shadow, View.GONE);
			}
		}			
		else if(mTarget == TARGET_RIGHT){
			menu = getRightMenuView();
			shadow = getRightShadowView();
			
			if(mRightMenuChild > mContentChild){
				int left_menu = Math.max(mRightMenuStyle.mMenuBorder, offsetX + mViewWidth) + getPaddingLeft();
				int left_content = (int)(offsetX * mRightMenuStyle.mSlideRatio) + getPaddingLeft();
				
				offsetLeftAndRight(content, left_content - content.getLeft());
				offsetLeftAndRight(menu, left_menu - menu.getLeft());
				offsetLeftAndRight(overlay, left_menu - mViewWidth - overlay.getLeft());
				offsetLeftAndRight(shadow, left_menu - mRightMenuStyle.mMenuShadow - shadow.getLeft());
				
				setDim(1f - (float)-offsetX / (float)mRightMenuStyle.mSize, mRightMenuStyle.mMaxDim);
			}
			else{	
				int left_menu = (int)((offsetX + mRightMenuStyle.mSize) * mRightMenuStyle.mSlideRatio) + mRightMenuStyle.mMenuBorder + getPaddingLeft();
				int left_content = offsetX + getPaddingLeft();
				
				offsetLeftAndRight(content, left_content - content.getLeft());				
				offsetLeftAndRight(menu, left_menu - menu.getLeft());
				offsetLeftAndRight(overlay, left_content + mViewWidth - overlay.getLeft());
				offsetLeftAndRight(shadow, left_content + mViewWidth - shadow.getLeft());
				
				setDim((float)-offsetX / (float)mRightMenuStyle.mSize, mRightMenuStyle.mMaxDim);
			}	
			
			if(offsetX < 0){
				setVisibility(overlay, View.VISIBLE);
				setVisibility(menu, View.VISIBLE);
				setVisibility(shadow, View.VISIBLE);
			}
			else{
				setVisibility(overlay, View.GONE);
				setVisibility(menu, View.GONE);
				setVisibility(shadow, View.GONE);
			}
		}
		
		invalidate();
	}
	
	private void offsetViewY(int offsetY){
		View content = getContentView();
		View menu;		
		View shadow;	
		View overlay = getOverlayView();
							
		if(mTarget == TARGET_TOP){
			menu = getTopMenuView();
			shadow = getTopShadowView();
			
			if(mTopMenuChild > mContentChild){
				int top_menu = Math.min(0, offsetY - mTopMenuStyle.mSize) + getPaddingTop();
				int top_content = (int)(offsetY * mTopMenuStyle.mSlideRatio) + getPaddingTop();
				
				offsetTopAndBottom(content, top_content - content.getTop());
				offsetTopAndBottom(menu, top_menu - menu.getTop());		
				offsetTopAndBottom(overlay, top_menu + mTopMenuStyle.mSize - overlay.getTop());
				offsetTopAndBottom(shadow, top_menu + mTopMenuStyle.mSize - shadow.getTop());
				
				setDim(1f - (float)offsetY / (float)mTopMenuStyle.mSize, mTopMenuStyle.mMaxDim);
			}
			else{		
				int top_menu = (int)((offsetY - mTopMenuStyle.mSize) * mTopMenuStyle.mSlideRatio) + getPaddingTop();
				int top_content = offsetY + getPaddingTop();
				
				offsetTopAndBottom(content, top_content - content.getTop());
				offsetTopAndBottom(menu, top_menu - menu.getTop());		
				offsetTopAndBottom(overlay, top_content - mViewHeight - overlay.getTop());
				offsetTopAndBottom(shadow, top_content - mTopMenuStyle.mMenuShadow - shadow.getTop());
				
				setDim((float)offsetY / (float)mTopMenuStyle.mSize, mTopMenuStyle.mMaxDim);
			}
			
			if(offsetY > 0){
				setVisibility(overlay, View.VISIBLE);
				setVisibility(menu, View.VISIBLE);
				setVisibility(shadow, View.VISIBLE);
			}
			else{
				setVisibility(overlay, View.GONE);
				setVisibility(menu, View.GONE);
				setVisibility(shadow, View.GONE);
			}
		}
		else if(mTarget == TARGET_BOTTOM){
			menu = getBottomMenuView();		
			shadow = getBottomShadowView();
			
			if(mBottomMenuChild > mContentChild){
				int top_menu = Math.max(mBottomMenuStyle.mMenuBorder, offsetY + mViewHeight) + getPaddingTop();
				int top_content = (int)(offsetY * mBottomMenuStyle.mSlideRatio) + getPaddingTop();
				
				offsetTopAndBottom(content, top_content - content.getTop());
				offsetTopAndBottom(menu, top_menu - menu.getTop());
				offsetTopAndBottom(overlay, top_menu - mViewHeight - overlay.getTop());	
				offsetTopAndBottom(shadow, top_menu - mBottomMenuStyle.mMenuShadow - shadow.getTop());		
				
				setDim(1f - (float)-offsetY / (float)mBottomMenuStyle.mSize, mBottomMenuStyle.mMaxDim);
			}
			else{
				int top_menu = (int)((offsetY + mBottomMenuStyle.mSize) * mBottomMenuStyle.mSlideRatio + mBottomMenuStyle.mMenuBorder) + getPaddingTop();
				int top_content = offsetY + getPaddingTop();
				
				offsetTopAndBottom(content, top_content - content.getTop());				
				offsetTopAndBottom(menu, top_menu - menu.getTop());
				offsetTopAndBottom(overlay, top_content + mViewHeight - overlay.getTop());	
				offsetTopAndBottom(shadow, top_content + mViewHeight - shadow.getTop());
				
				setDim((float)-offsetY / (float)mBottomMenuStyle.mSize, mBottomMenuStyle.mMaxDim);
			}
			
			if(offsetY < 0){
				setVisibility(overlay, View.VISIBLE);
				setVisibility(menu, View.VISIBLE);
				setVisibility(shadow, View.VISIBLE);
			}
			else{
				setVisibility(overlay, View.GONE);
				setVisibility(menu, View.GONE);
				setVisibility(shadow, View.GONE);
			}
		}
				
		invalidate();
	}
		
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {    	
    	right -= left + getPaddingRight();
    	bottom -= top + getPaddingBottom();
    	left = getPaddingLeft();
    	top = getPaddingTop();   	
    	
    	if(mViewWidth != right - left || mViewHeight != bottom - top){
    		mViewWidth = right - left;
    		mViewHeight = bottom - top;
    		setLeftMenuView(mViewWidth, mViewHeight);
    		setRightMenuView(mViewWidth, mViewHeight);
    		setTopMenuView(mViewWidth, mViewHeight);
    		setBottomMenuView(mViewWidth, mViewHeight);
    		setContentView(mViewWidth, mViewHeight);
    		setShadow(mViewWidth, mViewHeight);   
    		
    		// if menu already opened, then offset view correctly
    		if(mAction == ACTION_SHOW){
    			switch (mTarget) {
					case TARGET_LEFT:
						mOffsetX = mLeftMenuStyle.mSize;
						offsetViewX(mOffsetX);
						break;
					case TARGET_RIGHT:
						mOffsetX = mRightMenuStyle.mSize;
						offsetViewX(mOffsetX);
						break;
					case TARGET_TOP:
						mOffsetY = mTopMenuStyle.mSize;
						offsetViewY(mOffsetY);
						break;
					case TARGET_BOTTOM:
						mOffsetY = mBottomMenuStyle.mSize;
						offsetViewY(mOffsetY);
						break;
    			}
    		}
    	}
    	    
    	View menu;
    	View shadow;
		View content = getContentView();		
		View overlay = getOverlayView();
		
    	switch (mTarget) {
			case TARGET_LEFT:
				menu = getLeftMenuView();				
				shadow = getLeftShadowView();
								
				if(mLeftMenuChild > mContentChild){
					int right_menu = left + Math.min(mLeftMenuStyle.mSize, mOffsetX);
					int left_content = left + (int)(mOffsetX * mLeftMenuStyle.mSlideRatio); 
					
					layout(menu, right_menu - mLeftMenuStyle.mSize, top, right_menu, bottom);					
					layout(content, left_content, top, left_content + mViewWidth, bottom);					
					layout(shadow, right_menu, top, right_menu + mLeftMenuStyle.mMenuShadow, bottom); 
	    			layout(overlay, right_menu, top, right_menu + mViewWidth, bottom);	
				}
				else{
					int left_menu = left + (int)((mOffsetX - mLeftMenuStyle.mSize) * mLeftMenuStyle.mSlideRatio);
					int left_content = left + mOffsetX;
					
					layout(menu, left_menu, top, left_menu + mLeftMenuStyle.mSize, bottom);
					layout(content, left_content, top, left_content + mViewWidth, bottom);	    			
					layout(shadow, left_content - mLeftMenuStyle.mMenuShadow, top, left_content, bottom); 
	    			layout(overlay, left_content - mViewWidth, top, left_content, bottom);
				}
				break;			
			case TARGET_RIGHT:
				menu = getRightMenuView();				
				shadow = getRightShadowView();
				if(mRightMenuChild > mContentChild){
					int left_menu = left + Math.max(mRightMenuStyle.mMenuBorder, mOffsetX + mViewWidth); 
					int left_content = left + (int)(mOffsetX * mRightMenuStyle.mSlideRatio); 
					
	    			layout(menu, left_menu, top, left_menu + mRightMenuStyle.mSize, bottom);
	    			layout(content, left_content, top, left_content + mViewWidth, bottom);	    			
	    			layout(shadow, left_menu - mRightMenuStyle.mMenuShadow, top, left_menu, bottom); 
	    			layout(overlay, left_menu - mViewWidth, top, left_menu, bottom);
				}
				else{
					int left_menu = left + (int)((mOffsetX + mRightMenuStyle.mSize) * mRightMenuStyle.mSlideRatio + mRightMenuStyle.mMenuBorder);
					int right_content = left + mOffsetX + mViewWidth;
					
					layout(menu, left_menu, top, left_menu + mRightMenuStyle.mSize, bottom);
					layout(content, right_content - mViewWidth, top, right_content, bottom);
					layout(shadow, right_content, top, right_content + mRightMenuStyle.mMenuShadow, bottom); 
					layout(overlay, right_content, top, right_content + mViewWidth, bottom);
				}
				break;
			case TARGET_TOP:
				menu = getTopMenuView();				
				shadow = getTopShadowView();
				if(mTopMenuChild > mContentChild){
					int bottom_menu = top + Math.min(mTopMenuStyle.mSize, mOffsetY);    
					int top_content = top + (int)(mOffsetY * mTopMenuStyle.mSlideRatio);   
					
	    			layout(menu, left, bottom_menu - mTopMenuStyle.mSize, right, bottom_menu);
	    			layout(content, left, top_content, right, top_content + mViewHeight);	    			
	    			layout(shadow, left, bottom_menu, right, bottom_menu + mTopMenuStyle.mMenuShadow);
	    			layout(overlay, left, bottom_menu, right, bottom_menu + mViewHeight);
				}
				else{
					int top_menu = top + (int)((mOffsetY - mTopMenuStyle.mSize) * mTopMenuStyle.mSlideRatio);
					int top_content = top + mOffsetY;
					
					layout(menu, left, top_menu, right, top_menu + mTopMenuStyle.mSize);					
					layout(content, left, top_content, right, top_content + mViewHeight);					
					layout(shadow, left, top_content - mTopMenuStyle.mMenuShadow, right, top_content);
					layout(overlay, left, top_content - mViewHeight, right, top_content);
				}
				break;
			case TARGET_BOTTOM:
				menu = getBottomMenuView();				
				shadow = getBottomShadowView();
				if(mBottomMenuChild > mContentChild){
					int top_menu = top + Math.max(mBottomMenuStyle.mMenuBorder, mOffsetY + mViewHeight);
					int top_content = top + (int)(mOffsetY * mBottomMenuStyle.mSlideRatio);
					
	    			layout(menu, left, top_menu, right, top_menu + mBottomMenuStyle.mSize);
	    			layout(content, left, top_content, right, top_content + mViewHeight);	    			
	    			layout(shadow, left, top_menu - mBottomMenuStyle.mMenuShadow, right, top_menu); 
	    			layout(overlay, left, top_menu - mViewHeight, right, top_menu); 
				}
				else{
					int top_menu = top + (int)((mOffsetY + mBottomMenuStyle.mSize) * mBottomMenuStyle.mSlideRatio + mBottomMenuStyle.mMenuBorder);
					int bottom_content = top + mOffsetY + mViewHeight;
					
					layout(menu, left, top_menu, right, top_menu + mBottomMenuStyle.mSize);
					layout(content, left, bottom_content - mViewHeight, right, bottom_content);
					layout(shadow, left, bottom_content, right, bottom_content + mBottomMenuStyle.mMenuShadow); 
					layout(overlay, left, bottom_content, right, bottom_content + mViewHeight); 
				}
				break;
			case TARGET_CONTENT:				
				layout(content, left, top, right, bottom);
				layout(overlay, 0, 0, 0, 0);
				break;
				
		}    	
    }
    
    private void offsetLeftAndRight(View v, int offset){
    	if(v == null)
    		return;
    	
		v.offsetLeftAndRight(offset);
	}
    
    private void offsetTopAndBottom(View v, int offset){
    	if(v == null)
    		return;
    	
		v.offsetTopAndBottom(offset);
	}
    
    private void layout(View v, int l, int t, int r, int b){
    	if(v == null || v.getVisibility() == View.GONE)
    		return;
    	
    	v.layout(l, t, r, b);
    }
    
    private void setVisibility(View v, int visibility){
    	if(v == null)
    		return;
    	
    	if(visibility == View.GONE)
    		v.clearAnimation();
    	
		v.setVisibility(visibility);
	}
        
    private boolean cancelMotionEvent(MotionEvent event, View mDispatchView){
    	MotionEvent cancelEvent = MotionEvent.obtain(event);
        cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
        
        boolean result;
        if(mDispatchView != null)
        	result = mDispatchView.dispatchTouchEvent(cancelEvent) || super.dispatchTouchEvent(cancelEvent);
        else
        	result = super.dispatchTouchEvent(cancelEvent);
        
		cancelEvent.recycle();
		
		return result;
    }
    
    /**
     * 
     * @param progress 1f: no Dim, 0f: maximum Dim
     * @param maxDim maximum Dim value
     */
    private void setDim(float progress, float maxDim){
    	View v = getOverlayView();
    	if(v == null)
    		return;
    	
    	v.clearAnimation();
    	
    	ChangeAlphaAnimation anim = new ChangeAlphaAnimation((1f - Math.max(0f, Math.min(1f,  progress))) * maxDim);
		v.startAnimation(anim);
		
		v.setVisibility(progress == 1f ? View.GONE : View.VISIBLE);
	}
    
    private class SlideAnimation extends Animation {
		
		private int distance;
		private int start;
		private boolean isOpen;
				
		public SlideAnimation(boolean isOpen){
			this.isOpen = isOpen;
			switch (mTarget) {
				case TARGET_LEFT:
					distance = isOpen ? mLeftMenuStyle.mSize - mOffsetX : mOffsetX;
					start = mOffsetX;
					setInterpolator(mLeftMenuStyle.getInterpolator());
					break;
				case TARGET_RIGHT:
					distance = isOpen ? mRightMenuStyle.mSize + mOffsetX :  -mOffsetX;
					start = mOffsetX;
					setInterpolator(mRightMenuStyle.getInterpolator());
					break;
				case TARGET_TOP:
					distance = isOpen ? mTopMenuStyle.mSize - mOffsetY : mOffsetY;
					start = mOffsetY;
					setInterpolator(mTopMenuStyle.getInterpolator());
					break;
				case TARGET_BOTTOM:
					distance = isOpen ? mBottomMenuStyle.mSize + mOffsetY : -mOffsetY;
					start = mOffsetY;
					setInterpolator(mBottomMenuStyle.getInterpolator());
					break;
			}
		}
		
		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {	
			float value = getInterpolator().getInterpolation(interpolatedTime);
			switch (mTarget) {
				case TARGET_LEFT:
					if(isOpen)
						mOffsetX = distance > 0 ? Math.min(mLeftMenuStyle.mSize, (int)(start + distance * value)) : Math.max(mLeftMenuStyle.mSize, (int)(start + distance * value));						
					else
						mOffsetX = Math.max(0, (int)(start - distance * value));						
					offsetViewX(mOffsetX);
					
					if(isOpen){
						if(mOffsetX == mLeftMenuStyle.mSize)
							this.cancel();
					}
					else{
						if(mOffsetX == 0)
							this.cancel();
					}
					break;
				case TARGET_RIGHT:
					if(isOpen)
						mOffsetX = distance > 0 ? Math.max(-mRightMenuStyle.mSize, (int)(start - distance * value)) : Math.min(-mRightMenuStyle.mSize, (int)(start - distance * value));						
					else
						mOffsetX = Math.min(0, (int)(start + distance * value));
					offsetViewX(mOffsetX);
					
					if(isOpen){
						if(mOffsetX == -mRightMenuStyle.mSize)
							this.cancel();
					}
					else{
						if(mOffsetX == 0)
							this.cancel();
					}
					break;
				case TARGET_TOP:
					if(isOpen)
						mOffsetY = distance > 0 ? Math.min(mTopMenuStyle.mSize, (int)(start + distance * value)) : Math.max(mTopMenuStyle.mSize, (int)(start + distance * value));						
					else
						mOffsetY = Math.max(0, (int)(start - distance * value));
					offsetViewY(mOffsetY);
					
					if(isOpen){
						if(mOffsetY == mTopMenuStyle.mSize)
							this.cancel();
					}
					else{
						if(mOffsetY == 0)
							this.cancel();
					}
					break;
				case TARGET_BOTTOM:
					if(isOpen)
						mOffsetY = distance > 0 ? Math.max(-mBottomMenuStyle.mSize, (int)(start - distance * value)) : Math.min(-mBottomMenuStyle.mSize, (int)(start - distance * value));						
					else
						mOffsetY = Math.min(0, (int)(start + distance * value));
					offsetViewY(mOffsetY);
					
					if(isOpen){
						if(mOffsetY == -mBottomMenuStyle.mSize)
							this.cancel();
					}
					else{
						if(mOffsetY == 0)
							this.cancel();
					}
					break;
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
		boolean mOverDrag = false;
		
		int mMenuBorder;
		float mMenuBorderPercent = -1f;
		
		int mMenuOverDragBorder;
		float mMenuOverDragBorderPercent = -1f;
						
		int mMenuShadow = 10;	
		int mDragEdge = 30;			
		int mTouchSlop = 16;		
		float mMaxDim = 0.5f;		
		float mVelocitySlop = 500f;
		
		int mCloseEdge;
		float mCloseEdgePercent = -1f;
		
		int mAnimDuration = 1000;
		int mInterpolatorId = 0;
		
		int mSize;
		
		float mSlideRatio = 0.5f;
		
		public MenuStyle(Context context, int resID){
			TypedArray a = context.obtainStyledAttributes(resID, R.styleable.SlideMenuStyle);
			 
			for (int i = 0, count = a.getIndexCount(); i < count; i++){
			    int attr = a.getIndex(i);
			    switch (attr){
			    	case R.styleable.SlideMenuStyle_sm_overDrag:
			    		mOverDrag = a.getBoolean(attr, false);
			    		break;
			        case R.styleable.SlideMenuStyle_sm_menuBorder:
			        	TypedValue value = a.peekValue(attr);
			        	if(value.type == TypedValue.TYPE_DIMENSION)
			        		mMenuBorder = a.getDimensionPixelSize(attr, 50);
			        	else
			        		mMenuBorderPercent = Math.max(0f, Math.min(1f, a.getFloat(attr, 0f)));			        	
			            break;
			        case R.styleable.SlideMenuStyle_sm_menuOverDragBorder:
			        	value = a.peekValue(attr);
			        	if(value.type == TypedValue.TYPE_DIMENSION)
			        		mMenuOverDragBorder = a.getDimensionPixelSize(attr, 50);
			        	else
			        		mMenuOverDragBorderPercent = Math.max(0f, Math.min(1f, a.getFloat(attr, 0f)));			        	
			            break;
			        case R.styleable.SlideMenuStyle_sm_slideRatio:
			        	mSlideRatio = a.getFloat(attr, 0.5f);
			            break;	
			        case R.styleable.SlideMenuStyle_sm_menuShadow:
			        	mMenuShadow = a.getDimensionPixelSize(attr, 10);
			        	break;			        
			        case R.styleable.SlideMenuStyle_sm_dragEdge:
			        	value = a.peekValue(attr);
			        	if(value.type == TypedValue.TYPE_DIMENSION)
			        		mDragEdge = a.getDimensionPixelSize(attr, 30);
			        	else{
			        		mDragEdge = a.getInt(attr, -1);
			        		if(mDragEdge == -1)
			        			mDragEdge = Integer.MAX_VALUE;
			        	}
			            break;
			        case R.styleable.SlideMenuStyle_sm_touchSlop:
			        	mTouchSlop = a.getDimensionPixelSize(attr, 30);
			            break;   
			        case R.styleable.SlideMenuStyle_sm_maxDim:
			        	mMaxDim = Math.max(0f, Math.min(1f, a.getFloat(attr, 0f)));
			            break;  
			        case R.styleable.SlideMenuStyle_sm_velocitySlop:
			        	mVelocitySlop = Math.max(500f, a.getFloat(attr, 500f));
			            break; 
			        case R.styleable.SlideMenuStyle_sm_closeEdge:
			        	value = a.peekValue(attr);
			        	if(value.type == TypedValue.TYPE_DIMENSION)
			        		mCloseEdge = a.getDimensionPixelSize(attr, 50);
			        	else
			        		mCloseEdgePercent = Math.max(0f, Math.min(1f, a.getFloat(attr, 0.75f)));
			            break; 
			        case R.styleable.SlideMenuStyle_sm_animDuration:
			        	mAnimDuration = Math.max(0, a.getInt(attr, 1000));
			            break; 
			        case R.styleable.SlideMenuStyle_sm_animInterpolator:
			        	mInterpolatorId = a.getResourceId(attr, 0);
			            break; 
			    }
			}
			a.recycle();	
		}
		
		public Interpolator getInterpolator(){
			if(mInterpolatorId == 0){
				if(mInterpolator == null)
					mInterpolator = new SmoothInterpolator();
			
				return mInterpolator;
			}
			else
				return AnimationUtils.loadInterpolator(getContext(), mInterpolatorId);			
		}
	}
}