package com.routon.smartcampus.schoolcompare;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import com.routon.edurelease.R;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.FrameLayout.LayoutParams;

public class ClassSubGradeAdapter extends BaseAdapter{
	
	private Context context;
	private int ratingMode;
	private int selectId;
	private List<ClassCompareBean> classCompareBeanList;
	private boolean isCompareFinish;
	
	public ClassSubGradeAdapter(Context context, int ratingMode,int selectId, List<ClassCompareBean> classCompareBeanList,
			boolean isCompareFinish){
		this.context = context;
		this.ratingMode = ratingMode;
		this.selectId = selectId;
		this.classCompareBeanList = classCompareBeanList;
		this.isCompareFinish = isCompareFinish;
	}

	@Override
	public int getCount() {
		if(ratingMode == 2){
			return classCompareBeanList.size();
		}else {
			return classCompareBeanList.get(0).subprojectBeanList.size();
		}
	}

	@Override
	public Object getItem(int arg0) {
		if(ratingMode == 2){
			return classCompareBeanList.get(arg0);
		}else {
			return classCompareBeanList.get(0).subprojectBeanList.get(arg0);
		}
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if(convertView == null){
			convertView = LayoutInflater.from(context).inflate(R.layout.item_commit_sub_grade, parent, false);
			viewHolder = new ViewHolder(convertView);
			convertView.setTag(viewHolder);
		}else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		if(isCompareFinish){
			viewHolder.btnDecrease.setVisibility(View.GONE);
			viewHolder.btnIncrease.setVisibility(View.GONE);
		}else {
			viewHolder.btnDecrease.setVisibility(View.VISIBLE);
			viewHolder.btnIncrease.setVisibility(View.VISIBLE);
		}
 		if(ratingMode == 1){
 			final TextView tvTitle = viewHolder.tvTitle;
 			final TextView tvGrade = viewHolder.tvGrade;
 			final Button btnDecrease = viewHolder.btnDecrease;
 			final Button btnIncrease = viewHolder.btnIncrease;
 	 		final FrameLayout llMain = viewHolder.llMain;
			ClassCompareBean classCompareBean = null;
			for(int i=0;i<classCompareBeanList.size();i++){
				ClassCompareBean bean = classCompareBeanList.get(i);
				if(bean.groupId == selectId){
					classCompareBean = bean;
				}
			}
			final SubprojectBean subprojectBean = classCompareBean.subprojectBeanList.get(position);
			viewHolder.tvTitle.setText(subprojectBean.name);
			if(isCompareFinish){
				if(subprojectBean.score != -9999){
					double value = subprojectBean.score - subprojectBean.itemAvg;
					/*BigDecimal bg = new BigDecimal(value);
				    double currage =bg.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();*/
				    DecimalFormat format = new DecimalFormat("0.00");
					viewHolder.tvPerGrade.setText(format.format(value));
					if(value > 0){
						viewHolder.tvPerGrade.setTextColor(Color.GREEN);
					}else {
						viewHolder.tvPerGrade.setTextColor(Color.RED);
					}
				}else {
					viewHolder.tvPerGrade.setText("0.00");
					viewHolder.tvPerGrade.setTextColor(Color.parseColor("#999999"));
				}
			}else {
				viewHolder.tvPerGrade.setTextColor(Color.parseColor("#999999"));
				viewHolder.tvPerGrade.setText("±"+String.valueOf(subprojectBean.operateStep));
			}
			if(subprojectBean.isPermit == false){
				if(isCompareFinish){
					if(subprojectBean.score == -9999){
						viewHolder.tvGrade.setText("-");
					}else {
						viewHolder.tvGrade.setText(String.valueOf(subprojectBean.score));
					}
				}else {
					viewHolder.tvGrade.setText("-");
				}
				viewHolder.btnDecrease.setVisibility(View.INVISIBLE);
				viewHolder.btnIncrease.setVisibility(View.INVISIBLE);
			}
			else {
				if(subprojectBean.score == -9999){
					viewHolder.tvGrade.setText("-");
				}else {
					viewHolder.tvGrade.setText(String.valueOf(subprojectBean.score));
				}
				if (isCompareFinish) {
					viewHolder.btnDecrease.setVisibility(View.INVISIBLE);
					viewHolder.btnIncrease.setVisibility(View.INVISIBLE);
				} else {
					viewHolder.btnDecrease.setVisibility(View.VISIBLE);
					viewHolder.btnIncrease.setVisibility(View.VISIBLE);
					viewHolder.btnDecrease.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View arg0) {
							if (subprojectBean.score- subprojectBean.operateStep < subprojectBean.minScore) {
								return;
							}
							TextView tvInDecreaseView = new TextView(context);
							tvInDecreaseView.setText("-"+ subprojectBean.operateStep);
							tvInDecreaseView.setTextColor(Color.RED);
							tvInDecreaseView.setTextSize(21);
							FrameLayout.LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,
									Gravity.CENTER_VERTICAL| Gravity.LEFT);
							layoutParams.leftMargin = tvGrade.getLeft();
							llMain.addView(tvInDecreaseView,layoutParams);
							startDecreaseAnim(tvInDecreaseView,btnDecrease, tvGrade,subprojectBean);
						}
					});
					viewHolder.btnIncrease.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View arg0) {
							if (subprojectBean.score+ subprojectBean.operateStep > subprojectBean.maxScore) {
								return;
							}
							TextView tvInCreaseView = new TextView(context);
							tvInCreaseView.setText("+"+ subprojectBean.operateStep);
							tvInCreaseView.setTextColor(Color.GREEN);
							tvInCreaseView.setTextSize(14);
							FrameLayout.LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,
									Gravity.CENTER_VERTICAL| Gravity.RIGHT);
							layoutParams.rightMargin = (int) dp2px(context, 80);
							llMain.addView(tvInCreaseView, layoutParams);
							startIncreaseAnim(tvInCreaseView,btnDecrease, tvGrade,subprojectBean);
						}
					});
				}
			}
		}else {
			final TextView tvTitle = viewHolder.tvTitle;
			final TextView tvGrade = viewHolder.tvGrade;
			final Button btnDecrease = viewHolder.btnDecrease;
			final Button btnIncrease = viewHolder.btnIncrease;
	 		final FrameLayout llMain = viewHolder.llMain;
			ClassCompareBean classCompareBean = classCompareBeanList.get(position);
			List<SubprojectBean> subprojectBeans = classCompareBean.subprojectBeanList;
			int index=0;
			for(int i=0;i<subprojectBeans.size();i++){
				SubprojectBean subprojectBean = subprojectBeans.get(i);
				if(subprojectBean.id == selectId){
					index = i;
				}
			}
			final SubprojectBean subprojectBean = subprojectBeans.get(index);
			viewHolder.tvTitle.setText(classCompareBean.groupName);
			if(isCompareFinish){
				if(subprojectBean.score != -9999){
					double value = subprojectBean.score - subprojectBean.itemAvg;
					/*BigDecimal bg = new BigDecimal(value);
				    double currage =bg.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();*/
				    DecimalFormat format = new DecimalFormat("0.00");
					viewHolder.tvPerGrade.setText(format.format(value));
					if(value > 0){
						viewHolder.tvPerGrade.setTextColor(Color.GREEN);
					}else {
						viewHolder.tvPerGrade.setTextColor(Color.RED);
					}
				}else {
					viewHolder.tvPerGrade.setText("0.00");
					viewHolder.tvPerGrade.setTextColor(Color.parseColor("#999999"));
				}
			}else {
				viewHolder.tvPerGrade.setTextColor(Color.parseColor("#999999"));
				viewHolder.tvPerGrade.setText("±"+String.valueOf(subprojectBean.operateStep));
			}
			if(subprojectBean.isPermit == false){
				if(isCompareFinish){
					if(subprojectBean.score == -9999){
						viewHolder.tvGrade.setText("-");
					}else {
						viewHolder.tvGrade.setText(String.valueOf(subprojectBean.score));
					}
				}else {
					viewHolder.tvGrade.setText("-");
				}
				viewHolder.btnDecrease.setVisibility(View.INVISIBLE);
				viewHolder.btnIncrease.setVisibility(View.INVISIBLE);
			}else {
				viewHolder.tvGrade.setText(String.valueOf(subprojectBean.score));
				if(isCompareFinish){
					viewHolder.btnDecrease.setVisibility(View.INVISIBLE);
					viewHolder.btnIncrease.setVisibility(View.INVISIBLE);
				}else {
					viewHolder.btnDecrease.setVisibility(View.VISIBLE);
					viewHolder.btnIncrease.setVisibility(View.VISIBLE);
					viewHolder.btnDecrease.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View arg0) {
							if(subprojectBean.score-subprojectBean.operateStep<subprojectBean.minScore){
								return;
							}
							TextView tvInDecreaseView = new TextView(context);
							tvInDecreaseView.setText("-"+subprojectBean.operateStep);
							tvInDecreaseView.setTextColor(Color.RED);
							tvInDecreaseView.setTextSize(21);
							FrameLayout.LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, 
									LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL|Gravity.LEFT);
							layoutParams.leftMargin = tvGrade.getLeft();
							llMain.addView(tvInDecreaseView, layoutParams);
							startDecreaseAnim(tvInDecreaseView, btnDecrease, tvGrade, subprojectBean);
						}
					});
					viewHolder.btnIncrease.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View arg0) {
							if(subprojectBean.score+subprojectBean.operateStep>subprojectBean.maxScore){
								return;
							}
							TextView tvInCreaseView = new TextView(context);
							tvInCreaseView.setText("+"+subprojectBean.operateStep);
							tvInCreaseView.setTextColor(Color.GREEN);
							tvInCreaseView.setTextSize(14);
							FrameLayout.LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, 
									LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL|Gravity.RIGHT);
							layoutParams.rightMargin = (int) dp2px(context, 80);
							llMain.addView(tvInCreaseView, layoutParams);
							startIncreaseAnim(tvInCreaseView, btnDecrease, tvGrade, subprojectBean);
						}
					});
				}
			}
		}
		return convertView;
	}
	
	
    public void startIncreaseAnim(final TextView tvView, Button btnDecrease, final TextView tvGrade, final SubprojectBean subprojectBean){
    	ObjectAnimator scaleAnimator = ObjectAnimator.ofFloat(tvView, "textSize", 14,21);
    	ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(tvView, "alpha", 0.1f,1f);
    	ObjectAnimator xAnimator = ObjectAnimator.ofFloat(tvView, "translationX", 0, 
    	btnDecrease.getLeft()-tvGrade.getLeft()+(tvGrade.getWidth()-tvView.getWidth())/3*2);
    	ObjectAnimator yAnimator = ObjectAnimator.ofFloat(tvView, "translationY", 
    			0,-66,-100,-66,0);
    	final AnimatorSet animatorSet = new AnimatorSet();
    	animatorSet.setDuration(1000);
    	animatorSet.playTogether(scaleAnimator, alphaAnimator, xAnimator, yAnimator);
    	animatorSet.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator arg0) {
			}
			
			@Override
			public void onAnimationRepeat(Animator arg0) {
			}
			
			@Override
			public void onAnimationEnd(Animator arg0) {
				tvView.setVisibility(View.INVISIBLE);
				subprojectBean.score = (subprojectBean.score+subprojectBean.operateStep)>subprojectBean.maxScore?
						subprojectBean.score:subprojectBean.score+subprojectBean.operateStep;
				BigDecimal bg = new BigDecimal(subprojectBean.score);
				double f1 = bg.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
				subprojectBean.score = f1;
				tvGrade.setText(String.valueOf(f1));
				tvGrade.setTextColor(Color.GREEN);
				tvGrade.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						tvGrade.setTextColor(Color.BLACK);
					}
				}, 300);
			}
			
			@Override
			public void onAnimationCancel(Animator arg0) {
			}
		});
    	animatorSet.start();
    }
    
    public void startDecreaseAnim(final TextView tvView, Button btnDecrease, final TextView tvGrade,final SubprojectBean subprojectBean){
    	ObjectAnimator scaleAnimator = ObjectAnimator.ofFloat(tvView, "textSize", 21,14);
    	ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(tvView, "alpha", 1f,0.1f);
    	ObjectAnimator xAnimator = ObjectAnimator.ofFloat(tvView, "translationX", 0, 
    	btnDecrease.getLeft()-tvGrade.getLeft()+(tvGrade.getWidth()-tvView.getWidth())/2);
    	ObjectAnimator yAnimator = ObjectAnimator.ofFloat(tvView, "translationY", 
    			0,-66,-100,-66,0);
    	final AnimatorSet animatorSet = new AnimatorSet();
    	animatorSet.setDuration(1000);
    	animatorSet.playTogether(scaleAnimator, alphaAnimator, xAnimator, yAnimator);
    	animatorSet.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator arg0) {
			}
			
			@Override
			public void onAnimationRepeat(Animator arg0) {
			}
			
			@Override
			public void onAnimationEnd(Animator arg0) {
				tvView.setVisibility(View.INVISIBLE);
				subprojectBean.score = (subprojectBean.score-subprojectBean.operateStep)<subprojectBean.minScore?
						subprojectBean.score:subprojectBean.score-subprojectBean.operateStep;
				BigDecimal bg = new BigDecimal(subprojectBean.score);
				double f1 = bg.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
				subprojectBean.score = f1;
				tvGrade.setText(String.valueOf(f1));
				tvGrade.setTextColor(Color.RED);
				tvGrade.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						tvGrade.setTextColor(Color.BLACK);
					}
				}, 300);
			}
			
			@Override
			public void onAnimationCancel(Animator arg0) {
			}
		});
    	animatorSet.start();
    }
    
    public static float sp2px(Context context, int sp){
    	float density = context.getResources().getDisplayMetrics().scaledDensity;
    	return density*sp;
    	
    }
    
    public static float dp2px(Context context, int dp){
    	float density = context.getResources().getDisplayMetrics().density;
    	return density*dp;
    }

	class ViewHolder{
		TextView tvTitle;
		TextView tvGrade;
		TextView tvPerGrade;
		Button btnDecrease;
		Button btnIncrease;
		FrameLayout llMain;
		public ViewHolder(View view){
			tvTitle = (TextView)view.findViewById(R.id.tv_sub_grade_title);
			tvGrade = (TextView)view.findViewById(R.id.tv_sub_grade_grade);
			tvPerGrade = (TextView)view.findViewById(R.id.tv_sub_grade_per_grade);
			btnDecrease = (Button)view.findViewById(R.id.btn_decrease_grade);
			btnIncrease = (Button)view.findViewById(R.id.btn_increase_grade);
			llMain = (FrameLayout)view.findViewById(R.id.ll_item_commit_sub_grade);
		}
	}
}
