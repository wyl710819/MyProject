package com.routon.smartcampus.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.routon.edurelease.R;
import com.routon.smartcampus.flower.Badge;
import com.routon.smartcampus.flower.BadgeInfo;
import com.routon.smartcampus.flower.OftenBadgeBean;
import com.routon.smartcampus.utils.FlowerUtil;

/**
 * This utility class can add a horizontal popup-menu easily
 * <p>
 * 该工具类可以很方便的为View、ListView/GridView绑定长按弹出横向气泡菜单
 */
public class PopupList {

    public static final int DEFAULT_NORMAL_TEXT_COLOR = Color.WHITE;
    public static final int DEFAULT_PRESSED_TEXT_COLOR = Color.WHITE;
    public static final float DEFAULT_TEXT_SIZE_DP = 14;
    public static final float DEFAULT_TEXT_PADDING_LEFT_DP = 20.0f;
    public static final float DEFAULT_TEXT_PADDING_TOP_DP = 8.0f;
    public static final float DEFAULT_TEXT_PADDING_RIGHT_DP = 20.0f;
    public static final float DEFAULT_TEXT_PADDING_BOTTOM_DP = 8.0f;
    public static final int DEFAULT_NORMAL_BACKGROUND_COLOR = 0xCC000000;
    public static final int DEFAULT_PRESSED_BACKGROUND_COLOR = 0xE7777777;
    public static final int DEFAULT_BACKGROUND_RADIUS_DP = 8;
    public static final int DEFAULT_DIVIDER_COLOR = 0x9AFFFFFF;
    public static final float DEFAULT_DIVIDER_WIDTH_DP = 0.5f;
    public static final float DEFAULT_DIVIDER_HEIGHT_DP = 20.0f;

    private Context mContext;
    private PopupWindow mPopupWindow;
    private View mAnchorView;
    private View mAdapterView;
    private View mContextView;
    private View mIndicatorView;
    private List<String> mPopupItemList;
    private PopupListListener mPopupListListener;
    private PopupWindow.OnDismissListener mPopDismissListener;
    private int mContextPosition;
    private float mRawX;
    private float mRawY;
    private StateListDrawable mLeftItemBackground;
    private StateListDrawable mRightItemBackground;
    private StateListDrawable mCornerItemBackground;
    private ColorStateList mTextColorStateList;
    private GradientDrawable mCornerBackground;
    private int mIndicatorWidth;
    private int mIndicatorHeight;
    private int mPopupWindowWidth;
    private int mPopupWindowHeight;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mNormalTextColor;
    private int mPressedTextColor;
    private float mTextSize;
    private int mTextPaddingLeft;
    private int mTextPaddingTop;
    private int mTextPaddingRight;
    private int mTextPaddingBottom;
    private int mNormalBackgroundColor;
    private int mPressedBackgroundColor;
    private int mBackgroundCornerRadius;
    private int mDividerColor;
    private int mDividerWidth;
    private int mDividerHeight;
	private ArrayList<BadgeInfo> mImgList;
	List<BadgeInfo> badges=null;
	private boolean mIsShowViewTop;
	private LayoutInflater mInflater;
	private LinearLayout popupListContainer;
	private boolean isMore=true;
	private boolean isStaffUserAgent;
	private LinearLayout contentView;
    public PopupList(Context context) {
        this.mContext = context;
        this.mNormalTextColor = DEFAULT_NORMAL_TEXT_COLOR;
        this.mPressedTextColor = DEFAULT_PRESSED_TEXT_COLOR;
        this.mTextSize = dp2px(DEFAULT_TEXT_SIZE_DP);
        this.mTextPaddingLeft = dp2px(DEFAULT_TEXT_PADDING_LEFT_DP);
        this.mTextPaddingTop = dp2px(DEFAULT_TEXT_PADDING_TOP_DP);
        this.mTextPaddingRight = dp2px(DEFAULT_TEXT_PADDING_RIGHT_DP);
        this.mTextPaddingBottom = dp2px(DEFAULT_TEXT_PADDING_BOTTOM_DP);
        this.mNormalBackgroundColor = DEFAULT_NORMAL_BACKGROUND_COLOR;
        this.mPressedBackgroundColor = DEFAULT_PRESSED_BACKGROUND_COLOR;
        this.mBackgroundCornerRadius = dp2px(DEFAULT_BACKGROUND_RADIUS_DP);
        this.mDividerColor = DEFAULT_DIVIDER_COLOR;
        this.mDividerWidth = dp2px(DEFAULT_DIVIDER_WIDTH_DP);
        this.mDividerHeight = dp2px(DEFAULT_DIVIDER_HEIGHT_DP);
        this.mIndicatorView = getDefaultIndicatorView(mContext);
        if (mScreenWidth == 0) {
            mScreenWidth = getScreenWidth();
        }
        if (mScreenHeight == 0) {
            mScreenHeight = getScreenHeight();
        }
        
        mInflater = LayoutInflater.from(context);
        
        refreshBackgroundOrRadiusStateList();
        refreshTextColorStateList(mPressedTextColor, mNormalTextColor);
    }

    /**
     * Popup a window when anchorView is clicked and held.
     * That method will call {@link View#setOnTouchListener(View.OnTouchListener)} and
     * {@link View#setOnLongClickListener(View.OnLongClickListener)}(or
     * {@link AbsListView#setOnItemLongClickListener(AdapterView.OnItemLongClickListener)}
     * if anchorView is a instance of AbsListView), so you can only use
     * {@link PopupList#showPopupListWindow(View, int, float, float, List, PopupListListener)}
     * if you called those method before.
     *
     * @param anchorView        the view on which to pin the popup window
     * @param popupItemList     the list of the popup menu
     * @param popupListListener the Listener
     */
    public void bind(View anchorView, List<String> popupItemList, PopupListListener popupListListener) {
        this.mAnchorView = anchorView;
        this.mPopupItemList = popupItemList;
        this.mPopupListListener = popupListListener;
        this.mPopupWindow = null;
        mAnchorView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mRawX = event.getRawX();
                mRawY = event.getRawY();
                return false;
            }
        });
        if (mAnchorView instanceof AbsListView) {
            ((AbsListView) mAnchorView).setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    if (mPopupListListener != null
                            && !mPopupListListener.showPopupList(parent, view, position)) {
                        return false;
                    }
                    mAdapterView = parent;
                    mContextView = view;
                    mContextPosition = position;
                    showPopupListWindow();
                    return true;
                }
            });
        } else {
            mAnchorView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mPopupListListener != null
                            && !mPopupListListener.showPopupList(v, v, 0)) {
                        return false;
                    }
                    mContextView = v;
                    mContextPosition = 0;
                    showPopupListWindow();
                    return true;
                }
            });
        }
    }

    /**
     * show a popup window in a bubble style.
     *
     * @param anchorView        the view on which to pin the popup window
     * @param contextPosition   context position
     * @param rawX              the original raw X coordinate
     * @param rawY              the original raw Y coordinate
     * @param popupItemList     the list of the popup menu
     * @param popupListListener the Listener
     */
    public void showPopupListWindow(View anchorView, int contextPosition, float rawX, float rawY, List<String> popupItemList, PopupListListener popupListListener) {
        this.mAnchorView = anchorView;
        this.mPopupItemList = popupItemList;
        this.mPopupListListener = popupListListener;
        this.mPopupWindow = null;
        this.mRawX = rawX;
        this.mRawY = rawY;
        mContextView = anchorView;
        mContextPosition = contextPosition;
        if (mPopupListListener != null
                && !mPopupListListener.showPopupList(mContextView, mContextView, contextPosition)) {
            return;
        }
        showPopupListWindow();
    }
    
    
    public void showPopupListWindow(View anchorView, int contextPosition, float rawX, float rawY, ArrayList<BadgeInfo> imgList,List<String> popupItemList, boolean isShowViewTop,PopupListListener popupListListener) {
        this.mAnchorView = anchorView;
        this.mImgList = imgList;
        this.mIsShowViewTop=isShowViewTop;
        this.mPopupItemList = popupItemList;
        this.mPopupListListener = popupListListener;
        this.mPopupWindow = null;
        this.mRawX = rawX;
        this.mRawY = rawY;
        mContextView = anchorView;
        mContextPosition = contextPosition;
        if (mPopupListListener != null
                && !mPopupListListener.showPopupList(mContextView, mContextView, contextPosition)) {
            return;
        }
        showPopupListWindow();
    }
    
    public void showPopupListWindowAtCenter(View anchorView, int contextPosition, List<String> popupItemList, PopupListListener popupListListener) {

        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);
		
		int x = anchorView.getWidth()/2;
		int y = location[1] + anchorView.getHeight()/2;
		showPopupListWindow(anchorView,contextPosition,x,y,popupItemList,popupListListener);
    }
    
    public void setOnDismissListener(PopupWindow.OnDismissListener listener){
    	mPopDismissListener = listener;
    }

    @SuppressLint("NewApi")
	private void showPopupListWindow() {
        if (mContext instanceof Activity && ((Activity) mContext).isFinishing()) {
            return;
        }
        if (mPopupWindow == null || mPopupListListener instanceof AdapterPopupListListener) {
            contentView = new LinearLayout(mContext);
            contentView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            contentView.setOrientation(LinearLayout.VERTICAL);
            popupListContainer = new LinearLayout(mContext);
            LinearLayout.LayoutParams popupParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            popupParams.gravity = Gravity.CENTER;
            popupListContainer.setLayoutParams(popupParams);
            popupListContainer.setOrientation(LinearLayout.HORIZONTAL);
            popupListContainer.setBackgroundDrawable(mCornerBackground);
            
            
            List<LinearLayout> layoutList=null;
            int badgeLayoutCount=0;
            if (mImgList!=null && mImgList.size()>0) {
            	layoutList=new ArrayList<LinearLayout>();
            	badgeLayoutCount=mImgList==null ? 0 : (mImgList.size()/6)+(mImgList.size()%6==0 ? 0 : 1);
            	
            	for (int i = 0; i < badgeLayoutCount; i++) {
                	LinearLayout layout = new LinearLayout(mContext);
                    LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.gravity = Gravity.CENTER;
                    layout.setLayoutParams(layoutParams);
                    layout.setOrientation(LinearLayout.HORIZONTAL);
                    layoutList.add(layout);
    			}
			}
            
            
           
            
            
//            LinearLayout horizontalListViewLayout = new LinearLayout(mContext);
//            LinearLayout.LayoutParams horizontalParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//            horizontalParams.gravity = Gravity.CENTER;
//            horizontalListViewLayout.setLayoutParams(horizontalParams);
//            horizontalListViewLayout.setOrientation(LinearLayout.HORIZONTAL);
//            LinearLayout horizontalListViewLayout2 = null;
//            
//            if (mImgList.size()>6) {
//            	horizontalListViewLayout2 = new LinearLayout(mContext);
//                horizontalListViewLayout2.setLayoutParams(horizontalParams);
//                horizontalListViewLayout2.setOrientation(LinearLayout.HORIZONTAL);
//			}
            
            if (layoutList!=null && mIsShowViewTop) {
            	for (int i = layoutList.size()-1; i >=0; i--) {
            		contentView.addView(layoutList.get(i));
				}
            	
			}
            contentView.addView(popupListContainer);
           
            if (mIndicatorView != null) {
                LinearLayout.LayoutParams layoutParams;
                if (mIndicatorView.getLayoutParams() == null) {
                    layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                } else {
                    layoutParams = (LinearLayout.LayoutParams) mIndicatorView.getLayoutParams();
                }
                layoutParams.gravity = Gravity.CENTER;
                
                int tagT = 0;
                if (mImgList.size()<5) {
                	tagT=40;
				}else if(mImgList.size()==5){
					tagT=10;
				}else if(mImgList.size()==6){
					tagT=-20;
				}else if(mImgList.size()>6&&mImgList.size()<=8){
					tagT=40;
				}else if(mImgList.size()>8&&mImgList.size()<=10){
					tagT=10;
				}else if(mImgList.size()>10){
					tagT=-20;
				}
                
                if (mContextPosition%3==0) {
                	layoutParams.rightMargin=dp2px(tagT-10);
				}else if (mContextPosition%3==1) {
				}else if (mContextPosition%3==2) {
					layoutParams.leftMargin=dp2px(tagT);
				}
                
                mIndicatorView.setLayoutParams(layoutParams);
                ViewParent viewParent = mIndicatorView.getParent();
                if (viewParent instanceof ViewGroup) {
                    ((ViewGroup) viewParent).removeView(mIndicatorView);
                }
                contentView.addView(mIndicatorView);
            }
            
            if (layoutList!=null && !mIsShowViewTop) {
            	
            	for (int i = 0; i < layoutList.size(); i++) {
            		contentView.addView(layoutList.get(i));
				}
            	
			}
            
            if (mImgList!=null && mImgList.size()>0) {
            	LinearLayout.LayoutParams imgParams;
            	if(getScreenHeight()>1280){
            		imgParams = new LinearLayout.LayoutParams(dp2px(45), dp2px(45));
            	}else{
            		imgParams = new LinearLayout.LayoutParams(dp2px(80), dp2px(80));
            	}
            	
            	
            	if (mIsShowViewTop) {
            		imgParams.setMargins(dp2px(3), 0, dp2px(3), dp2px(13));
				}else {
					imgParams.setMargins(dp2px(3), dp2px(13), dp2px(3), 0);
				}
            	
            	List<ArrayList<BadgeInfo>> badgeList = null;
            	if (mImgList.size()>6) {
            		badgeList=new ArrayList<ArrayList<BadgeInfo>>();
//            		int badgeCount=(mImgList.size()/badgeLayoutCount)+(mImgList.size()-(mImgList.size()/badgeLayoutCount*badgeLayoutCount));
            		int badgeCount=mImgList.size()/badgeLayoutCount+(mImgList.size()%badgeLayoutCount==0 ? 0 : 1);
//            		int badgeCountEnd=mImgList.size()%badgeLayoutCount;
            		for (int i = 0; i < badgeLayoutCount; i++) {
            			if (i==0) {
            				ArrayList<BadgeInfo> badgeArrays=new ArrayList<BadgeInfo>(mImgList.subList(0, badgeCount));
            				badgeList.add(badgeArrays);
						}else if (i==badgeLayoutCount-1) {
							ArrayList<BadgeInfo> badgeArrays=new ArrayList<BadgeInfo>(mImgList.subList(badgeCount*i, mImgList.size()));
            				badgeList.add(badgeArrays);
							
						}else{
							ArrayList<BadgeInfo> badgeArrays=new ArrayList<BadgeInfo>(mImgList.subList(badgeCount*i, badgeCount*(i+1)));
            				badgeList.add(badgeArrays);
						}
            			
					}
//            		badges=(List<Badge>) mImgList.subList(0, 6);
//            		badges2=(List<Badge>) mImgList.subList(6, mImgList.size());
				}else {
					badges=mImgList;
				}
            	
            	
            	if (mImgList.size()>6 && badgeList!=null) {
            		for (int i = 0; i < badgeLayoutCount; i++) {
            			final ArrayList<BadgeInfo> bList=badgeList.get(i);
            			for (int j = 0; j < bList.size(); j++) {
//                			ImageView imageView = new ImageView(mContext);
//                    		imageView.setLayoutParams(imgParams);
                    		
                    		View view=mInflater.inflate(R.layout.popup_layout, null);
                			ImageView imageView =(ImageView) view.findViewById(R.id.image_view);
                			TextView textView=(TextView) view.findViewById(R.id.text_view);
                			
                    		
                    		OftenBadgeBean flower = (OftenBadgeBean) bList.get(j);
                    		textView.setText(flower.badgeTitle);
                    		FlowerUtil.loadFlower(mContext, imageView, flower.badgeTitle, flower.imgUrl);
                    		final int finalJ = j;
                       	 imageView.setClickable(true);
                       	 imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (mPopupListListener != null) {
                                        mPopupListListener.onImgListClick(mContextView, mContextPosition, finalJ,bList.get(finalJ));
                                        hidePopupListWindow();
                                    }
                                }
                            });
                       	layoutList.get(i).addView(view);
        				}
            			
					}
            		
            		
            		
            	}else {
            		if (badges!=null) {
                		for (int i = 0; i < badges.size(); i++) {
//                			ImageView imageView = new ImageView(mContext);
//                    		imageView.setLayoutParams(imgParams);
                			
                			View view=mInflater.inflate(R.layout.popup_layout, null);
                			ImageView imageView =(ImageView) view.findViewById(R.id.image_view);
                			TextView textView=(TextView) view.findViewById(R.id.text_view);
                			
                			OftenBadgeBean flower = (OftenBadgeBean) badges.get(i);
                			textView.setText(flower.badgeTitle);
                    		FlowerUtil.loadFlower(mContext, imageView, flower.badgeTitle, flower.imgUrl);
                    		final int finalJ = i;
                       	 imageView.setClickable(true);
                       	 imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (mPopupListListener != null) {
                                        mPopupListListener.onImgListClick(mContextView, mContextPosition, finalJ,badges.get(finalJ));
                                        hidePopupListWindow();
                                    }
                                }
                            });
                       	layoutList.get(0).addView(view);
        				}
    				}
				}
            	
            	
            	
            	/*if (badges2!=null) {
            		for (int i = 0; i < badges2.size(); i++) {
            			ImageView imageView = new ImageView(mContext);
                		imageView.setLayoutParams(imgParams);
                		Badge flower = badges2.get(i);
                		FlowerUtil.loadFlower(mContext, imageView, flower.name, flower.imgUrl);
                		final int finalJ = i;
                   	 imageView.setClickable(true);
                   	 imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mPopupListListener != null) {
                                    mPopupListListener.onImgListClick(mContextView, mContextPosition, finalJ,badges2.get(finalJ));
                                    hidePopupListWindow();
                                }
                            }
                        });
                   	 horizontalListViewLayout2.addView(imageView);
    				}
				}*/
            	
            	
            	 
			}
            
            
            
            for (int i = 0; i < mPopupItemList.size(); i++) {
                TextView textView = new TextView(mContext);
                textView.setTextColor(mTextColorStateList);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
                textView.setPadding(mTextPaddingLeft, mTextPaddingTop, mTextPaddingRight, mTextPaddingBottom);
                textView.setClickable(true);
                final int finalI = i;
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mPopupListListener != null) {
                            mPopupListListener.onPopupListClick(mContextView, mContextPosition, finalI);
                            
                            if (finalI==mPopupItemList.size()-1) {
                            	if (isMore) {
                            		setMenuItem();
								}else {
									hidePopupListWindow();
								}
                            	
							}else {
								hidePopupListWindow();
							}
                            
                        }
                    }
                });
                if (mPopupListListener instanceof AdapterPopupListListener) {
                    AdapterPopupListListener adapterPopupListListener = (AdapterPopupListListener) mPopupListListener;
                    textView.setText(adapterPopupListListener.formatText(mAdapterView, mContextView, mContextPosition, i, mPopupItemList.get(i)));
                } else {
                    textView.setText(mPopupItemList.get(i));
                }
                if (mPopupItemList.size() > 1 && i == 0) {
                    textView.setBackgroundDrawable(mLeftItemBackground);
                } else if (mPopupItemList.size() > 1 && i == mPopupItemList.size() - 1) {
                    textView.setBackgroundDrawable(mRightItemBackground);
                } else if (mPopupItemList.size() == 1) {
                    textView.setBackgroundDrawable(mCornerItemBackground);
                } else {
                    textView.setBackgroundDrawable(getCenterItemBackground());
                }
                popupListContainer.addView(textView);
                if (mPopupItemList.size() > 1 && i != mPopupItemList.size() - 1) {
                    View divider = new View(mContext);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(mDividerWidth, mDividerHeight);
                    layoutParams.gravity = Gravity.CENTER;
                    divider.setLayoutParams(layoutParams);
                    divider.setBackgroundColor(mDividerColor);
                    popupListContainer.addView(divider);
                }
            }
            if (mPopupWindowWidth == 0) {
                mPopupWindowWidth = getViewWidth(contentView);
            }
            if (mIndicatorView != null && mIndicatorWidth == 0) {
                if (mIndicatorView.getLayoutParams().width > 0) {
                    mIndicatorWidth = mIndicatorView.getLayoutParams().width;
                } else {
                    mIndicatorWidth = getViewWidth(mIndicatorView);
                }
            }
            if (mIndicatorView != null && mIndicatorHeight == 0) {
                if (mIndicatorView.getLayoutParams().height > 0) {
                    mIndicatorHeight = mIndicatorView.getLayoutParams().height;
                } else {
                    mIndicatorHeight = getViewHeight(mIndicatorView);
                }
            }
            if (mPopupWindowHeight == 0) {
            	if (layoutList!=null && layoutList.size()>0) {
                    mPopupWindowHeight = getViewHeight(popupListContainer) + mIndicatorHeight+getViewHeight(layoutList.get(0))*layoutList.size();
				}else {
					mPopupWindowHeight = getViewHeight(popupListContainer) + mIndicatorHeight;
				}
            }
            
            int tagWindowWidth = 0;
           
            
            if (mImgList.size()>0) {
            	if (mPopupWindowWidth>=dp2px(280)) {
                	tagWindowWidth=mPopupWindowWidth+dp2px(30);
    			}else if (mPopupWindowWidth<dp2px(280) && mPopupWindowWidth>=dp2px(215)) {
    				tagWindowWidth=mPopupWindowWidth+dp2px(80);
    			}else if (mPopupWindowWidth<dp2px(215)) {
    				tagWindowWidth=mPopupWindowWidth+dp2px(100);
    			}
			}else {
				tagWindowWidth=mPopupWindowWidth+dp2px(100);
			}
            
            mPopupWindow = new PopupWindow(contentView, tagWindowWidth, mPopupWindowHeight, true);
            mPopupWindow.setTouchable(true);
            if( mPopDismissListener != null ){
            	mPopupWindow.setOnDismissListener(mPopDismissListener);
            }
            mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        }
        if (mIndicatorView != null) {
            float marginLeftScreenEdge = mRawX;
            float marginRightScreenEdge = mScreenWidth - mRawX;
            if (marginLeftScreenEdge < mPopupWindowWidth / 2f) {
                // in case of the draw of indicator out of corner's bounds
                if (marginLeftScreenEdge < mIndicatorWidth / 2f + mBackgroundCornerRadius) {
                    mIndicatorView.setTranslationX(mIndicatorWidth / 2f + mBackgroundCornerRadius - mPopupWindowWidth / 2f);
                } else {
                    mIndicatorView.setTranslationX(marginLeftScreenEdge - mPopupWindowWidth / 2f);
                }
            } else if (marginRightScreenEdge < mPopupWindowWidth / 2f) {
                if (marginRightScreenEdge < mIndicatorWidth / 2f + mBackgroundCornerRadius) {
                    mIndicatorView.setTranslationX(mPopupWindowWidth / 2f - mIndicatorWidth / 2f - mBackgroundCornerRadius);
                } else {
                    mIndicatorView.setTranslationX(mPopupWindowWidth / 2f - marginRightScreenEdge);
                }
            } else {
                mIndicatorView.setTranslationX(0);
            }
        }
        if (!mPopupWindow.isShowing()) {
        	if (mImgList!=null && mImgList.size()>0) {
        		mPopupWindow.showAtLocation(mAnchorView, Gravity.CENTER,
                        (int) mRawX - mScreenWidth / 2,
                        (int) mRawY - mScreenHeight / 2 - mPopupWindowHeight + mIndicatorHeight);
			}else {
				mPopupWindow.showAtLocation(mAnchorView, Gravity.CENTER,
                        (int) mRawX - mScreenWidth / 2,
                        (int) mRawY - mScreenHeight / 2 - mPopupWindowHeight*2);
			}
            
        }
    }

    private void refreshBackgroundOrRadiusStateList() {
        // left
        GradientDrawable leftItemPressedDrawable = new GradientDrawable();
        leftItemPressedDrawable.setColor(mPressedBackgroundColor);
        leftItemPressedDrawable.setCornerRadii(new float[]{
                mBackgroundCornerRadius, mBackgroundCornerRadius,
                0, 0,
                0, 0,
                mBackgroundCornerRadius, mBackgroundCornerRadius});
        GradientDrawable leftItemNormalDrawable = new GradientDrawable();
        leftItemNormalDrawable.setColor(Color.TRANSPARENT);
        leftItemNormalDrawable.setCornerRadii(new float[]{
                mBackgroundCornerRadius, mBackgroundCornerRadius,
                0, 0,
                0, 0,
                mBackgroundCornerRadius, mBackgroundCornerRadius});
        mLeftItemBackground = new StateListDrawable();
        mLeftItemBackground.addState(new int[]{android.R.attr.state_pressed}, leftItemPressedDrawable);
        mLeftItemBackground.addState(new int[]{}, leftItemNormalDrawable);
        // right
        GradientDrawable rightItemPressedDrawable = new GradientDrawable();
        rightItemPressedDrawable.setColor(mPressedBackgroundColor);
        rightItemPressedDrawable.setCornerRadii(new float[]{
                0, 0,
                mBackgroundCornerRadius, mBackgroundCornerRadius,
                mBackgroundCornerRadius, mBackgroundCornerRadius,
                0, 0});
        GradientDrawable rightItemNormalDrawable = new GradientDrawable();
        rightItemNormalDrawable.setColor(Color.TRANSPARENT);
        rightItemNormalDrawable.setCornerRadii(new float[]{
                0, 0,
                mBackgroundCornerRadius, mBackgroundCornerRadius,
                mBackgroundCornerRadius, mBackgroundCornerRadius,
                0, 0});
        mRightItemBackground = new StateListDrawable();
        mRightItemBackground.addState(new int[]{android.R.attr.state_pressed}, rightItemPressedDrawable);
        mRightItemBackground.addState(new int[]{}, rightItemNormalDrawable);
        // corner
        GradientDrawable cornerItemPressedDrawable = new GradientDrawable();
        cornerItemPressedDrawable.setColor(mPressedBackgroundColor);
        cornerItemPressedDrawable.setCornerRadius(mBackgroundCornerRadius);
        GradientDrawable cornerItemNormalDrawable = new GradientDrawable();
        cornerItemNormalDrawable.setColor(Color.TRANSPARENT);
        cornerItemNormalDrawable.setCornerRadius(mBackgroundCornerRadius);
        mCornerItemBackground = new StateListDrawable();
        mCornerItemBackground.addState(new int[]{android.R.attr.state_pressed}, cornerItemPressedDrawable);
        mCornerItemBackground.addState(new int[]{}, cornerItemNormalDrawable);
        mCornerBackground = new GradientDrawable();
        mCornerBackground.setColor(mNormalBackgroundColor);
        mCornerBackground.setCornerRadius(mBackgroundCornerRadius);
    }

    private StateListDrawable getCenterItemBackground() {
        StateListDrawable centerItemBackground = new StateListDrawable();
        GradientDrawable centerItemPressedDrawable = new GradientDrawable();
        centerItemPressedDrawable.setColor(mPressedBackgroundColor);
        GradientDrawable centerItemNormalDrawable = new GradientDrawable();
        centerItemNormalDrawable.setColor(Color.TRANSPARENT);
        centerItemBackground.addState(new int[]{android.R.attr.state_pressed}, centerItemPressedDrawable);
        centerItemBackground.addState(new int[]{}, centerItemNormalDrawable);
        return centerItemBackground;
    }

    private void refreshTextColorStateList(int pressedTextColor, int normalTextColor) {
        int[][] states = new int[2][];
        states[0] = new int[]{android.R.attr.state_pressed};
        states[1] = new int[]{};
        int[] colors = new int[]{pressedTextColor, normalTextColor};
        mTextColorStateList = new ColorStateList(states, colors);
    }

    public void hidePopupListWindow() {
        if (mContext instanceof Activity && ((Activity) mContext).isFinishing()) {
            return;
        }
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }

    public View getIndicatorView() {
        return mIndicatorView;
    }

    public View getDefaultIndicatorView(Context context) {
        return getTriangleIndicatorView(context, dp2px(16), dp2px(8), DEFAULT_NORMAL_BACKGROUND_COLOR);
    }

    public View getTriangleIndicatorView(Context context, final float widthPixel, final float heightPixel,
                                         final int color) {
        ImageView indicator = new ImageView(context);
        Drawable drawable = new Drawable() {
            @Override
            public void draw(Canvas canvas) {
                Path path = new Path();
                Paint paint = new Paint();
                paint.setColor(color);
                paint.setStyle(Paint.Style.FILL);
                path.moveTo(0f, 0f);
                path.lineTo(widthPixel, 0f);
                path.lineTo(widthPixel / 2, heightPixel);
                path.close();
                canvas.drawPath(path, paint);
            }

            @Override
            public void setAlpha(int alpha) {

            }

            @Override
            public void setColorFilter(ColorFilter colorFilter) {

            }

            @Override
            public int getOpacity() {
                return PixelFormat.TRANSLUCENT;
            }

            @Override
            public int getIntrinsicWidth() {
                return (int) widthPixel;
            }

            @Override
            public int getIntrinsicHeight() {
                return (int) heightPixel;
            }
        };
        indicator.setImageDrawable(drawable);
        return indicator;
    }

    public void setIndicatorView(View indicatorView) {
        this.mIndicatorView = indicatorView;
    }

    public void setIndicatorSize(int widthPixel, int heightPixel) {
        this.mIndicatorWidth = widthPixel;
        this.mIndicatorHeight = heightPixel;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(mIndicatorWidth, mIndicatorHeight);
        layoutParams.gravity = Gravity.CENTER;
        if (mIndicatorView != null) {
            mIndicatorView.setLayoutParams(layoutParams);
        }
    }

    public int getNormalTextColor() {
        return mNormalTextColor;
    }

    public void setNormalTextColor(int normalTextColor) {
        this.mNormalTextColor = normalTextColor;
        refreshTextColorStateList(mPressedTextColor, mNormalTextColor);
    }

    public int getPressedTextColor() {
        return mPressedTextColor;
    }

    public void setPressedTextColor(int pressedTextColor) {
        this.mPressedTextColor = pressedTextColor;
        refreshTextColorStateList(mPressedTextColor, mNormalTextColor);
    }

    public float getTextSize() {
        return mTextSize;
    }

    public void setTextSize(float textSizePixel) {
        this.mTextSize = textSizePixel;
    }

    public int getTextPaddingLeft() {
        return mTextPaddingLeft;
    }

    public void setTextPaddingLeft(int textPaddingLeft) {
        this.mTextPaddingLeft = textPaddingLeft;
    }

    public int getTextPaddingTop() {
        return mTextPaddingTop;
    }

    public void setTextPaddingTop(int textPaddingTop) {
        this.mTextPaddingTop = textPaddingTop;
    }

    public int getTextPaddingRight() {
        return mTextPaddingRight;
    }

    public void setTextPaddingRight(int textPaddingRight) {
        this.mTextPaddingRight = textPaddingRight;
    }

    public int getTextPaddingBottom() {
        return mTextPaddingBottom;
    }

    public void setTextPaddingBottom(int textPaddingBottom) {
        this.mTextPaddingBottom = textPaddingBottom;
    }

    /**
     * @param left   the left padding in pixels
     * @param top    the top padding in pixels
     * @param right  the right padding in pixels
     * @param bottom the bottom padding in pixels
     */
    public void setTextPadding(int left, int top, int right, int bottom) {
        this.mTextPaddingLeft = left;
        this.mTextPaddingTop = top;
        this.mTextPaddingRight = right;
        this.mTextPaddingBottom = bottom;
    }

    public int getNormalBackgroundColor() {
        return mNormalBackgroundColor;
    }

    public void setNormalBackgroundColor(int normalBackgroundColor) {
        this.mNormalBackgroundColor = normalBackgroundColor;
        refreshBackgroundOrRadiusStateList();
    }

    public int getPressedBackgroundColor() {
        return mPressedBackgroundColor;
    }

    public void setPressedBackgroundColor(int pressedBackgroundColor) {
        this.mPressedBackgroundColor = pressedBackgroundColor;
        refreshBackgroundOrRadiusStateList();
    }

    public int getBackgroundCornerRadius() {
        return mBackgroundCornerRadius;
    }

    public void setBackgroundCornerRadius(int backgroundCornerRadiusPixel) {
        this.mBackgroundCornerRadius = backgroundCornerRadiusPixel;
        refreshBackgroundOrRadiusStateList();
    }

    public int getDividerColor() {
        return mDividerColor;
    }

    public void setDividerColor(int dividerColor) {
        this.mDividerColor = dividerColor;
    }

    public int getDividerWidth() {
        return mDividerWidth;
    }

    public void setDividerWidth(int dividerWidthPixel) {
        this.mDividerWidth = dividerWidthPixel;
    }

    public int getDividerHeight() {
        return mDividerHeight;
    }

    public void setDividerHeight(int dividerHeightPixel) {
        this.mDividerHeight = dividerHeightPixel;
    }

    public Resources getResources() {
        if (mContext == null) {
            return Resources.getSystem();
        } else {
            return mContext.getResources();
        }
    }

    private int getScreenWidth() {
        WindowManager wm = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    private int getScreenHeight() {
        WindowManager wm = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    private int getViewWidth(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        return view.getMeasuredWidth();
    }

    private int getViewHeight(View view) {
    	if (view==null) {
    		return 0;
		}
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        return view.getMeasuredHeight();
    }

    public int dp2px(float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                value, getResources().getDisplayMetrics());
    }

    public int sp2px(float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                value, getResources().getDisplayMetrics());
    }

    public interface PopupListListener {

        /**
         * Whether the PopupList should be bound to the special view
         *
         * @param adapterView     The context view(The AbsListView where the click happened or normal view).
         * @param contextView     The view within the AbsListView that was clicked or normal view
         * @param contextPosition The position of the view in the list
         * @return true if the view should bind the PopupList, false otherwise
         */
        boolean showPopupList(View adapterView, View contextView, int contextPosition);

        /**
         * The callback to be invoked with an item in this PopupList has
         * been clicked
         *
         * @param contextView     The context view(The AbsListView where the click happened or normal view).
         * @param contextPosition The position of the view in the list
         * @param position        The position of the view in the PopupList
         */
        void onPopupListClick(View contextView, int contextPosition, int position);
        
        void onImgListClick(View contextView, int contextPosition, int position,BadgeInfo mBadge);
    }

    public interface AdapterPopupListListener extends PopupListListener {
        String formatText(View adapterView, View contextView, int contextPosition, int position, String text);
    }

	public void setMenuItem() {
		isMore = false;
		TextView tView=(TextView)popupListContainer.getChildAt(6);
		tView.setBackgroundDrawable(getCenterItemBackground());
		if (isStaffUserAgent) {
			tView.setText("取消代理");
		}else {
			tView.setText("授权代理");
		}
		 View divider = new View(mContext);
         LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(mDividerWidth, mDividerHeight);
         layoutParams.gravity = Gravity.CENTER;
         divider.setLayoutParams(layoutParams);
         divider.setBackgroundColor(mDividerColor);
         popupListContainer.addView(divider);
         
         TextView tView2 = new TextView(mContext);
         tView2.setTextColor(mTextColorStateList);
         tView2.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
         tView2.setPadding(mTextPaddingLeft, mTextPaddingTop, mTextPaddingRight, mTextPaddingBottom);
         tView2.setClickable(true);
         tView2.setText("换照片");
         LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
         tView2.setLayoutParams(textParams);
         tView2.setBackgroundDrawable(mRightItemBackground);
         tView2.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (mPopupListListener != null) {
                     mPopupListListener.onPopupListClick(mContextView, mContextPosition, 4);
                     
							hidePopupListWindow();
                     
                 }
             }
         });
         popupListContainer.addView(tView2);
         
	}

	public void setStaffUserAgent(boolean isStaffUserAgent) {
		// TODO Auto-generated method stub
		this.isStaffUserAgent=isStaffUserAgent;
	}
    

}
