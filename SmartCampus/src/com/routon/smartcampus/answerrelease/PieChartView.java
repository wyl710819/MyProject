package com.routon.smartcampus.answerrelease;

import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

public class PieChartView extends View {

	private Paint mPaint;// 画笔，在此处定义，避免多次创建
	private Paint mPaintText;
	private Double[] inputData;// 输入数据，用于确定占比
	private String[] answerItem;

	private Random random;// 产生随机值
	private RectF rect;// 饼状图的外接矩形
	int position;
	private int screenHeight;
	private DisplayMetrics dm;
	private float density;

	/*
	 * public PieChartView(Context context,Double[]inputData1,int position1){
	 * super(context);
	 * 
	 * 
	 * this.position=position1; if(inputData1.length>0){ this.inputData = new
	 * Double[inputData1.length]; for(int i=0;i<inputData1.length;i++){
	 * //Log.i("xxx",inputData1[i]+"---"); sumTotal=inputData1[i]+sumTotal; }
	 * for(int j=0;j<inputData1.length;j++){ this.inputData[j]=(double)
	 * Math.round(inputData1[j]/sumTotal*100*10)/10; }
	 * //System.arraycopy(inputData1, 0, this.inputData, 0, inputData1.length);
	 * 
	 * 
	 * }
	 * 
	 * }
	 */
	public PieChartView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		initData();
	}

	public PieChartView(Context context) {
		this(context, null);
		initData();
	}

	private void initData() {
		// TODO Auto-generated method stub
		dm = new DisplayMetrics();
		((Activity) getContext()).getWindowManager().getDefaultDisplay()
				.getMetrics(dm);
		screenHeight = dm.heightPixels;
		density = dm.density;

	}

	public PieChartView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mPaint = new Paint();
		mPaintText = new Paint();
		random = new Random();
		rect = new RectF();

	}

	/**
	 * 对外接口，获取输入数据,并将输入的数据转化位百分比数字
	 */

	double sumTotal;
	double answerTotal;

	public Boolean setInputData(List<OptionContentBean> optionList,
			int position1) {

		sumTotal = 0;
		answerTotal = 0;
		answerItem = new String[optionList.size()];
		this.position = position1;
		if (optionList.size() > 0) {
			this.inputData = new Double[optionList.size()];
			for (int i = 0; i < optionList.size(); i++) {
				// Log.i("xxx",inputData1[i]+"---");
				sumTotal = optionList.get(i).selCount + sumTotal;
				answerItem[i] = optionList.get(i).optionName;
				if (i == optionList.size() - 2) {
					answerTotal = sumTotal;
				}
			}
			for (int j = 0; j < optionList.size(); j++) {
				this.inputData[j] = (double) Math
						.round(optionList.get(j).selCount / sumTotal * 100 * 10) / 10;
			}
			// System.arraycopy(inputData1, 0, this.inputData, 0,
			// inputData1.length);
			invalidate();
			return true;
		} else {
			return false;
		}

	}

	@Override
	protected void onDraw(Canvas canvas) {

		int width = getMeasuredWidth();
		int height = getMeasuredHeight();

		// 保证输入数据的有效性
		if (inputData == null) {

			return;
		}
		if ((inputData.length == 0)) {

			return;
		}

		// 将数据转换为角度
		int num = inputData.length;
		float[] angles = new float[num];
		// int[] colors=new int[num];
		Double sum = 0.0;
		for (int i = 0; i < num; i++) {
			sum += inputData[i];
		}
		if (sum == 0) {

			return;
		}
		float angleSum = 0;
		for (int i = 0; i < num; i++) {
			angles[i] = (float) (inputData[i] * 360 / sum);
			angleSum += angles[i];

		}

		Paint paintTitle = new Paint();
		paintTitle.setTextSize(sp2px(19));
		paintTitle.setColor(Color.RED);
		paintTitle.setAntiAlias(true);
		paintTitle.setShadowLayer(1, 3, 3, Color.BLACK);

		int colors[] = { Color.parseColor("#CD2626"),
				Color.parseColor("#9400D3"), Color.parseColor("#C67171"),
				Color.parseColor("#B8860B"), Color.parseColor("#EE00EE"),
				Color.parseColor("#CD6889"), Color.GREEN,
				Color.parseColor("#B8860B"), Color.parseColor("#EE7621"),
				Color.parseColor("#9932CC"), Color.parseColor("#A0522D"),
				Color.parseColor("#CD2626"), Color.parseColor("#8A2BE2"),
				Color.parseColor("#6B8E23"), Color.parseColor("#EE00EE"),
				Color.parseColor("#CD2626"), Color.parseColor("#9400D3"),
				Color.parseColor("#C67171"), Color.parseColor("#B8860B"),
				Color.parseColor("#EE00EE"), Color.parseColor("#CD6889"),
				Color.GREEN, Color.parseColor("#B8860B"),
				Color.parseColor("#EE7621"), Color.parseColor("#9932CC"),
				Color.parseColor("#A0522D"), Color.parseColor("#CD2626"),
				Color.parseColor("#8A2BE2"), Color.parseColor("#6B8E23"),
				Color.parseColor("#EE00EE"), Color.parseColor("#CD2626"),
				Color.parseColor("#8A2BE2"), Color.parseColor("#6B8E23"),
				Color.parseColor("#EE00EE"), Color.parseColor("#CD2626"),
				Color.parseColor("#9400D3"), Color.parseColor("#C67171"),
				Color.parseColor("#B8860B"), Color.parseColor("#EE00EE"),
				Color.parseColor("#CD6889"), Color.GREEN,
				Color.parseColor("#B8860B"), Color.parseColor("#EE7621") };

		// mPaint.setColor(mFrontColor);

		// 画出饼状图
		float startAngle = 0;
		float sweepAngle = 0;
		// 饼状图外接矩形
		rect.set(0, 0, width, height);

		float averAngle;

		boolean isAgreeExist = false;
		boolean isDisagreeExist = false;
		for (int i = 0; i < num; i++) {

			// 判断是否是投票，是投票则显示图例，否则不显示
			if (answerItem[0].equals("Y") || answerItem[0].equals("N")) {

				Paint paintPercent = new Paint();
				paintPercent.setColor(Color.BLACK);
				paintPercent.setTextSize(sp2px(13));
				paintPercent.setAntiAlias(true);

				Paint paintResult = new Paint();
				paintResult.setTextSize(sp2px(13));
				paintResult.setColor(Color.WHITE);
				paintResult.setAntiAlias(true);

				//适配1280平板
				if(screenHeight<=1280){
					if (answerItem[i].equals("Y")) {

//						isAgreeExist = true;
//						mPaint.setColor(Color.parseColor("#3CB371"));
//						canvas.drawRect(50, 700, 150, 800, mPaint);
//						canvas.drawText("同意", 753, 43, paintResult);
//						canvas.drawText(":" + inputData[i].toString() + "%", 835,
//								43, paintPercent);
					} 
				}
				if (answerItem[i].equals("Y")) {

					isAgreeExist = true;
					mPaint.setColor(Color.parseColor("#3CB371"));
					canvas.drawRect(750, 0, 835, 50, mPaint);
					canvas.drawText("同意", 753, 43, paintResult);
					canvas.drawText(":" + inputData[i].toString() + "%", 835,
							43, paintPercent);
				} else if (answerItem[i].equals("N")) {

					isDisagreeExist = true;
					mPaint.setColor(Color.RED);
					if (isAgreeExist) {
						canvas.drawRect(750, 50, 835, 100, mPaint);
						canvas.drawText("反对", 753, 93, paintResult);
						canvas.drawText(":" + inputData[i].toString() + "%",
								835, 93, paintPercent);
					} else {

						canvas.drawRect(750, 0, 835, 50, mPaint);
						canvas.drawText("反对", 753, 43, paintResult);
						canvas.drawText(":" + inputData[i].toString() + "%",
								835, 43, paintPercent);
					}

				} else {
					mPaint.setColor(Color.parseColor("#191970"));
					if (isAgreeExist && isDisagreeExist) {
						canvas.drawRect(750, 100, 835, 150, mPaint);
						canvas.drawText("弃权", 753, 143, paintResult);
						canvas.drawText(":" + inputData[i].toString() + "%",
								835, 143, paintPercent);
					} else if ((isAgreeExist && !isDisagreeExist)
							|| (!isAgreeExist && isDisagreeExist)) {
						canvas.drawRect(750, 50, 835, 100, mPaint);
						canvas.drawText("弃权", 753, 93, paintResult);
						canvas.drawText(":" + inputData[i].toString() + "%",
								835, 93, paintPercent);
					} else {
						canvas.drawRect(750, 0, 835, 50, mPaint);
						canvas.drawText("弃权", 753, 43, paintResult);
						canvas.drawText(":" + inputData[i].toString() + "%",
								835, 43, paintPercent);
					}
				}
			}

			startAngle += sweepAngle;
			sweepAngle = angles[i];

			// float aver=(2*startAngle+sweepAngle)/2;
			averAngle = (float) (((startAngle * 2 + sweepAngle) / 2) / 360 * (2 * Math.PI));

			// 设置圆弧画笔
			mPaint.setColor(colors[i]);
			mPaint.setAntiAlias(true);
			// 正确答案圆弧外接矩形
			RectF rectCorrect;
			RectF rectArc;
			// 判断点击item，根据点击获取position然后将点击的item放大处理
			if (position == -1) {
				rectCorrect = new RectF(0, 0, dp2px(300), dp2px(300));
				if (answerItem[0].equals("Y") || answerItem[0].equals("N")) {
					if(screenHeight>1280){
						rectArc = new RectF(10, 30, 890, 910);
						if (answerItem[i].equals("Y")) {
							mPaint.setColor(Color.parseColor("#3CB371"));
							canvas.drawArc(rectArc, startAngle, sweepAngle, true,
									mPaint);
						} else if (answerItem[i].equals("N")) {
							mPaint.setColor(Color.RED);
							canvas.drawArc(rectArc, startAngle, sweepAngle, true,
									mPaint);
						} else {
							mPaint.setColor(Color.parseColor("#191970"));
							canvas.drawArc(rectArc, startAngle, sweepAngle, true,
									mPaint);
						}
					}else{
						rectArc = new RectF(10, 30, (int) (dp2px(297) * 2.5 / density),
								(int) (dp2px(297) * 2.5 / density));
						if (answerItem[i].equals("Y")) {
							mPaint.setColor(Color.parseColor("#3CB371"));
							canvas.drawArc(rectArc, startAngle, sweepAngle, true,
									mPaint);
						} else if (answerItem[i].equals("N")) {
							mPaint.setColor(Color.RED);
							canvas.drawArc(rectArc, startAngle, sweepAngle, true,
									mPaint);
						} else {
							mPaint.setColor(Color.parseColor("#191970"));
							canvas.drawArc(rectArc, startAngle, sweepAngle, true,
									mPaint);
						}
					}
					

				} else {
					if (screenHeight > 1280) {
						rectArc = new RectF(10, 10, dp2px(297), dp2px(297));
					} else {
						rectArc = new RectF(10, 10,
								(int) (dp2px(297) * 2.5 / density),
								(int) (dp2px(297) * 2.5 / density));
					}

					canvas.drawArc(rectArc, startAngle, sweepAngle, true,
							mPaint);
				}
			}
		

			// 画内圆环
			mPaint.setColor(Color.GRAY);
			// Rect rectInCircle=new Rect(60,60,180,180);
			mPaint.setAntiAlias(true);
			canvas.drawCircle((float)(dp2px(150)*2.5), (float)(dp2px(150)*2.5),(float) (dp2px(75)*2.5), mPaint);
			// 画圆环中的文字
			mPaint.setColor(Color.RED);
			mPaint.setTextSize(sp2px(17));
			String textInCircle;
			// 根据选项内容，显示不同的标题
			if ((int) answerTotal == 0) {
				textInCircle = "未收到数据";
				if(screenHeight<=1280){
					canvas.drawText(textInCircle, (float)(dp2px(100)*3), (float)(dp2px(157)*2.5), mPaint);
				}else{
					canvas.drawText(textInCircle, dp2px(100), dp2px(157), mPaint);
				}
			} else if ((int) answerTotal > 0) {
				if (answerItem[0].equals("Y") || answerItem[0].equals("N")) {
					// canvas.drawText("投票结果百分比图", 490, 30, paintTitle);
					textInCircle = "共收到" + (int) answerTotal + "份投票";
				} else {
					// canvas.drawText("选项百分比图", 490, 30, paintTitle);
					textInCircle = "共收到" + (int) answerTotal + "份答案";
				}
				if(screenHeight<=1280){
					canvas.drawText(textInCircle, (float)(dp2px(90)*3), (float)(dp2px(153)*2.5), mPaint);
				}
				if(screenHeight>1280){
					canvas.drawText(textInCircle, dp2px(90), dp2px(153), mPaint);
				}
				
			}

			// 画百分比数字

			// 当圆弧比例小于20度时,规划文字路径

		}
		startAngle = 0;
		sweepAngle = 0;
		float averAngle2;

		for (int j = 0; j < num; j++) {
			Rect rect3 = new Rect();
			mPaintText.getTextBounds(inputData[j].toString(), 0, inputData[j]
					.toString().length(), rect3);
			startAngle += sweepAngle;
			sweepAngle = angles[j];

			float aver1 = (2 * startAngle + sweepAngle) / 2;
			averAngle2 = (float) (((startAngle * 2 + sweepAngle) / 2) / 360 * (2 * Math.PI));
			// Log.i("xxx",startAngle+"---"+sweepAngle+"--"+aver1);
			float x = getMeasuredWidth() / 2
					+ (float) (rect.width() / 2 * Math.cos(averAngle2) * 3 / 4);
			float y = getMeasuredHeight()
					/ 2
					+ (float) (rect.height() / 2 * Math.sin(averAngle2) * 3 / 4);
			//
			if (screenHeight > 1280) {
				if (sweepAngle < 20) {
					float aver = (2 * startAngle + sweepAngle) / 2;
					mPaintText.setColor(Color.WHITE);
					mPaintText.setAntiAlias(true);
					// mPaint.setShadowLayer(5, 3, 3, Color.BLACK);
					mPaintText.setTextSize(sp2px(13));
					mPaintText.setShadowLayer(1, 3, 3, Color.BLACK);

					Path linePathCW = new Path();// 顺时针路径
					// 根据不同象限规划不同的路径
					if ((aver > 0 && aver <= 90)) {
						linePathCW
								.moveTo(445 + (float) (450 * Math
										.cos(averAngle2) * 0.55),
										465 + (float) (450 * Math
												.sin(averAngle2) * 0.55));

						linePathCW.lineTo(
								450 + (float) (450 * Math.cos(averAngle2)),
								450 + (float) (450 * Math.sin(averAngle2)));

						// Log.i("坐标:","X"+120+(float)
						// (120*Math.cos(averAngle2))+"--"+120+(float)(120*Math.sin(averAngle2)));
						if (answerItem[0].equals("Y")
								|| answerItem[0].equals("N")) {
							canvas.drawTextOnPath("", linePathCW, 0, 0,
									mPaintText);

						} else {
							canvas.drawTextOnPath(answerItem[j] + ":"
									+ inputData[j].toString() + "%",
									linePathCW, 0, 0, mPaintText);
						}

					} else if ((aver > 90 && aver <= 180)) {
						linePathCW
								.moveTo(453 + (float) (450 * Math
										.cos(averAngle2) * 0.88),
										453 + (float) (450 * Math
												.sin(averAngle2) * 0.88));

						linePathCW
								.lineTo(450 + (float) (450 * Math
										.cos(averAngle2) * 1 / 2),
										450 + (float) (450 * Math
												.sin(averAngle2) * 1 / 2));

						if (answerItem[0].equals("Y")
								|| answerItem[0].equals("N")) {
							canvas.drawTextOnPath("", linePathCW, 0, 0,
									mPaintText);

						} else {
							canvas.drawTextOnPath(answerItem[j] + ":"
									+ inputData[j].toString() + "%",
									linePathCW, 0, 0, mPaintText);
						}
					} else if (aver > 180 && aver <= 270) {
						linePathCW
								.moveTo(447 + (float) (450 * Math
										.cos(averAngle2) * 0.88),
										453 + (float) (450 * Math
												.sin(averAngle2) * 0.88));

						linePathCW.lineTo(450 + (float) (450 * Math.abs(Math
								.cos(averAngle2)) * 1 / 2),
								450 + (float) (450 * Math.abs(Math
										.sin(averAngle2)) * 1 / 2));
						if (answerItem[0].equals("Y")
								|| answerItem[0].equals("N")) {
							canvas.drawTextOnPath("", linePathCW, 0, 0,
									mPaintText);

						} else {
							canvas.drawTextOnPath(answerItem[j] + ":"
									+ inputData[j].toString() + "%",
									linePathCW, 0, 0, mPaintText);
						}
						// canvas.drawTextOnPath(inputData[j].toString()+"%",
						// linePathCW, 0, 0, mPaint);
					} else if (aver > 270 && aver <= 360) {
						if (j != inputData.length - 1) {
							linePathCW
									.moveTo(455 + (float) (450 * Math
											.cos(averAngle2) * 3 / 5),
											450 + (float) (450 * Math
													.sin(averAngle2) * 3 / 5));

							linePathCW.lineTo(
									450 + (float) (450 * Math.cos(averAngle2)),
									450 + (float) (450 * Math.sin(averAngle2)));
							if (answerItem[0].equals("Y")
									|| answerItem[0].equals("N")) {
								canvas.drawTextOnPath("", linePathCW, 0, 0,
										mPaintText);

							} else {
								if (answerItem[0].equals("Y")
										|| answerItem[0].equals("N")) {
									canvas.drawTextOnPath("", linePathCW, 0, 0,
											mPaintText);

								} else {
									canvas.drawTextOnPath(answerItem[j] + ":"
											+ inputData[j].toString() + "%",
											linePathCW, 0, 0, mPaintText);
								}
							}
						} else {
							if (inputData[j] != 0.0) {
								linePathCW.moveTo(450 + (float) (450 * Math
										.cos(averAngle2) * 1 / 2),
										450 + (float) (450 * Math
												.sin(averAngle2) * 1 / 2));

								linePathCW.lineTo(450 + (float) (450 * Math
										.cos(averAngle2)),
										450 + (float) (450 * Math
												.sin(averAngle2)));
								if (answerItem[0].equals("Y")
										|| answerItem[0].equals("N")) {
									canvas.drawTextOnPath("", linePathCW, 0, 0,
											mPaintText);

								} else {
									canvas.drawTextOnPath(answerItem[j] + ":"
											+ inputData[j].toString() + "%",
											linePathCW, 0, 0, mPaintText);
								}
							} else {
								return;
							}

						}

					}

				} else {
					mPaintText.setColor(Color.WHITE);
					mPaintText.setTextSize(sp2px(13));
					mPaintText.setAntiAlias(true);
					mPaintText.setShadowLayer(1, 3, 3, Color.BLACK);
					if (j < inputData.length - 1) {
						if (answerItem[0].equals("Y")
								|| answerItem[0].equals("N")) {
							canvas.drawText("", x * 5 / 6 - rect3.width() * 2
									/ 3, y + rect3.height() / 3, mPaintText);

						} else {
							canvas.drawText(
									answerItem[j] + ":"
											+ inputData[j].toString() + "%", x
											* 5 / 6 - rect3.width() * 2 / 3, y
											+ rect3.height() / 3, mPaintText);
						}

					} else {
						Log.d("xxx", inputData[j] + "----");
						if (inputData[j] != 0.0) {
							if (answerItem[0].equals("Y")
									|| answerItem[0].equals("N")) {
								canvas.drawText("", x * 5 / 6 - rect3.width()
										* 4 / 5, y + rect3.height() / 3,
										mPaintText);
							} else {
								canvas.drawText(
										"未选" + ":" + inputData[j].toString()
												+ "%",
										x * 5 / 6 - rect3.width() * 4 / 5, y
												+ rect3.height() / 3,
										mPaintText);
							}

						} else
							return;

					}
				}
			}

			// 适配平板
			if (screenHeight <=1280) {
				if (sweepAngle < 20) {
					float aver = (2 * startAngle + sweepAngle) / 2;
					mPaintText.setColor(Color.WHITE);
					mPaintText.setAntiAlias(true);
					// mPaint.setShadowLayer(5, 3, 3, Color.BLACK);
					mPaintText.setTextSize(sp2px(13));
					mPaintText.setShadowLayer(1, 3, 3, Color.BLACK);

					Path linePathCW = new Path();// 顺时针路径
					// 根据不同象限规划不同的路径
					if ((aver > 0 && aver <= 90)) {
						linePathCW
								.moveTo((float) (dp2px(148)*2.5 + (float) (dp2px(150) * Math
										.cos(averAngle2) * 0.55*2.5)),
										(float) (dp2px(155)*2.5 + (float) (dp2px(150) * Math
												.sin(averAngle2) * 0.55*2.5)));

						linePathCW.lineTo(
								(float)(dp2px(150)*2.5) + (float) (dp2px(150) * Math.cos(averAngle2)*2.5),
								(float)(dp2px(150)*2.5) + (float) (dp2px(150) * Math.sin(averAngle2)*2.5));

						// Log.i("坐标:","X"+120+(float)
						// (120*Math.cos(averAngle2))+"--"+120+(float)(120*Math.sin(averAngle2)));
						if (answerItem[0].equals("Y")
								|| answerItem[0].equals("N")) {
							canvas.drawTextOnPath("", linePathCW, 0, 0,
									mPaintText);

						} else {
							canvas.drawTextOnPath(answerItem[j] + ":"
									+ inputData[j].toString() + "%",
									linePathCW, 0, 0, mPaintText);
						}

					} else if ((aver > 90 && aver <= 180)) {
						linePathCW
								.moveTo((float)(dp2px(151)*2.5) + (float) (dp2px(150) * Math
										.cos(averAngle2) * 0.88*2.5),
										(float)(dp2px(151)*2.5) + (float) (dp2px(150) * Math
												.sin(averAngle2) * 0.88*2.5));

						linePathCW
								.lineTo((float)(dp2px(150)*2.5) + (float) (dp2px(150) * Math
										.cos(averAngle2) * 1 / 2*2.5),
										(float)(dp2px(150)*2.5) + (float) (dp2px(150) * Math
												.sin(averAngle2) * 1 / 2*2.5));

						if (answerItem[0].equals("Y")
								|| answerItem[0].equals("N")) {
							canvas.drawTextOnPath("", linePathCW, 0, 0,
									mPaintText);

						} else {
							canvas.drawTextOnPath(answerItem[j] + ":"
									+ inputData[j].toString() + "%",
									linePathCW, 0, 0, mPaintText);
						}
					} else if (aver > 180 && aver <= 270) {
						linePathCW
								.moveTo((float)(dp2px(149)*2.5) + (float) (dp2px(150) * Math
										.cos(averAngle2) * 0.88*2.5),
										(float)(dp2px(151)*2.5) + (float) (dp2px(150) * Math
												.sin(averAngle2) * 0.88*2.5));

						linePathCW.lineTo((float)(dp2px(150)*2.5) + (float) (dp2px(150) * Math.abs(Math
								.cos(averAngle2)) * 1 / 2*2.5),
								(float)(dp2px(150)*2.5) + (float) (dp2px(150) * Math.abs(Math
										.sin(averAngle2)) * 1 / 2*2.5));
						if (answerItem[0].equals("Y")
								|| answerItem[0].equals("N")) {
							canvas.drawTextOnPath("", linePathCW, 0, 0,
									mPaintText);

						} else {
							canvas.drawTextOnPath(answerItem[j] + ":"
									+ inputData[j].toString() + "%",
									linePathCW, 0, 0, mPaintText);
						}
						// canvas.drawTextOnPath(inputData[j].toString()+"%",
						// linePathCW, 0, 0, mPaint);
					} else if (aver > 270 && aver <= 360) {
						if (j != inputData.length - 1) {
							linePathCW
									.moveTo((float)(dp2px(151)*2.5) + (float) (dp2px(150) * Math
											.cos(averAngle2) * 3 / 5*2.5),
											(float)(dp2px(150)*2.5) + (float) (dp2px(150) * Math
													.sin(averAngle2) * 3 / 5*2.5));

							linePathCW.lineTo(
									((float)(dp2px(150)*2.5) + (float) (dp2px(150) * Math.cos(averAngle2)*2.5)),
											(float)(dp2px(150)*2.5) + (float) (dp2px(150) * Math.sin(averAngle2)*2.5));
							if (answerItem[0].equals("Y")
									|| answerItem[0].equals("N")) {
								canvas.drawTextOnPath("", linePathCW, 0, 0,
										mPaintText);

							} else {
								if (answerItem[0].equals("Y")
										|| answerItem[0].equals("N")) {
									canvas.drawTextOnPath("", linePathCW, 0, 0,
											mPaintText);

								} else {
									canvas.drawTextOnPath(answerItem[j] + ":"
											+ inputData[j].toString() + "%",
											linePathCW, 0, 0, mPaintText);
								}
							}
						} else {
							if (inputData[j] != 0.0) {
								linePathCW.moveTo((float)(dp2px(150)*2.5) + (float) (dp2px(150) * Math
										.cos(averAngle2) * 1 / 2*2.5),
										(float)(dp2px(150)*2.5) + (float) (dp2px(150) * Math
												.sin(averAngle2) * 1 / 2*2.5));

								linePathCW.lineTo((float)(dp2px(150)*2.5) + (float) (dp2px(150) * Math
										.cos(averAngle2)*2.5),
										(float)(dp2px(150)*2.5) + (float) (dp2px(150) * Math
												.sin(averAngle2)*2.5));
								if (answerItem[0].equals("Y")
										|| answerItem[0].equals("N")) {
									canvas.drawTextOnPath("", linePathCW, 0, 0,
											mPaintText);

								} else {
									canvas.drawTextOnPath(answerItem[j] + ":"
											+ inputData[j].toString() + "%",
											linePathCW, 0, 0, mPaintText);
								}
							} else {
								return;
							}

						}

					}

				} else {
					mPaintText.setColor(Color.WHITE);
					mPaintText.setTextSize(sp2px(13));
					mPaintText.setAntiAlias(true);
					mPaintText.setShadowLayer(1, 3, 3, Color.BLACK);
					if (j < inputData.length - 1) {
						if (answerItem[0].equals("Y")
								|| answerItem[0].equals("N")) {
							canvas.drawText("", x * 5 / 6 - rect3.width() * 2
									/ 3, y + rect3.height() / 3, mPaintText);

						} else {
							canvas.drawText(
									answerItem[j] + ":"
											+ inputData[j].toString() + "%", x
											* 5 / 6 - rect3.width() * 2 / 3, y
											+ rect3.height() / 3, mPaintText);
						}

					} else {
						Log.d("xxx", inputData[j] + "----");
						if (inputData[j] != 0.0) {
							if (answerItem[0].equals("Y")
									|| answerItem[0].equals("N")) {
								canvas.drawText("", x * 5 / 6 - rect3.width()
										* 4 / 5, y + rect3.height() / 3,
										mPaintText);
							} else {
								canvas.drawText(
										"未选" + ":" + inputData[j].toString()
												+ "%",
										x * 5 / 6 - rect3.width() * 4 / 5, y
												+ rect3.height() / 3,
										mPaintText);
							}

						} else
							return;

					}
				}
			}
		}

	}

	private int dp2px(double value) {
		float v = getContext().getResources().getDisplayMetrics().density;
		return (int) (v * value + 0.5f);
	}

	private int sp2px(int value) {
		float v = getContext().getResources().getDisplayMetrics().scaledDensity;
		return (int) (v * value + 0.5f);
	}
}