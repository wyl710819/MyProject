package com.routon.smartcampus.flower;

import java.util.ArrayList;

import com.routon.edurelease.R;
import com.routon.smartcampus.view.HorizontalListView;
import com.routon.smartcampus.view.RemarkImgListviewAdapter;
import com.routon.smartcampus.view.RemarkImgListviewAdapter.MyDeleteClickListener;
import com.routon.widgets.Toast;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class BadgeEditPopWin extends PopupWindow {
	public View view;
	private Context mContext;
	public ArrayList<String> imgList;
	private RemarkImgListviewAdapter remarkImgListviewAdapter;
	private TextView imgCountView;
	private HorizontalListView listView;
	private EditText editText;
	private TextView titleEditView;
	private ImageView addImgView;
	private TextView awardView;
	private Button addBtn;
	private Button delBtn;
	private TextView bonusPointsTextView;
	private BadgeInfo mBean;
	private boolean isDeleteRes;

	public BadgeEditPopWin(Context context, BadgeInfo bean, PopOnClickListener listener, String btnStr,
			ArrayList<String> savemages, String saveRemarkString) {
		this.mContext = context;
		this.mBean = bean;
		
        if (mBean.bonuspoint>=0&& mBean.prop==1) {
        	mBean.prop=0;
        }else if(mBean.bonuspoint<0){
        	mBean.prop=1;
		}
		init(listener, mBean, btnStr);

		if (bean.badgeRemark != null) {
			editText.setText(bean.badgeRemark);
			editText.setSelection(bean.badgeRemark.length());
		} else {
			editText.setHint("请输入评语内容");
		}

		
		
		if (bean.badgeTitle != null) {
			titleEditView.setText(bean.badgeTitle);
//			titleEditView.setSelection(bean.badgeTitle.length());
		} else {
//			titleEditView.setHint("请输入评语标题");
		}

		if (savemages != null && savemages.size() > 0) {
			imgList.clear();
			imgList.addAll(savemages);
			imgCountView.setText(imgList.size() - 2 + "/9");
			remarkImgListviewAdapter.notifyDataSetChanged();
		}

		if (saveRemarkString != null) {
			mBean.badgeRemark = saveRemarkString;
			editText.setText(saveRemarkString);
			editText.setSelection(saveRemarkString.length());
		}

	}

	private void init(final PopOnClickListener popOnClickListener, BadgeInfo bean, String btnStr) {
		this.view = LayoutInflater.from(mContext).inflate(R.layout.badge_remark_popwin_layout, null);

		titleEditView = (TextView) view.findViewById(R.id.remark_title_view);
		editText = (EditText) view.findViewById(R.id.remark_edit_view);
		awardView = (TextView) view.findViewById(R.id.remark_award_text);
		addImgView = (ImageView) view.findViewById(R.id.remark_edit_img);
		imgCountView = (TextView) view.findViewById(R.id.remark_img_count_text);
		listView = (HorizontalListView) view.findViewById(R.id.remark_img_listview);

		addBtn = (Button) view.findViewById(R.id.add_btn);
		delBtn = (Button) view.findViewById(R.id.del_btn);
		bonusPointsTextView = (TextView) view.findViewById(R.id.bonuspoint_text);
		bonusPointsTextView.setText("" + bean.bonuspoint);

		addBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				if (mBean.prop==0) {//正面
					if (mBean.bonuspoint>=10) {
						return;
					}
				}else {//负面
					if (mBean.bonuspoint>=0) {
						return;
					}
				}
				
				mBean.bonuspoint++;
				bonusPointsTextView.setText("" + mBean.bonuspoint);
			}
		});

		delBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				if (mBean.prop==0) {//正面
					if (mBean.bonuspoint<=0) {
						return;
					}
				}else {//负面
					if (mBean.bonuspoint<=-10) {
						return;
					}
				}
				
				mBean.bonuspoint--;
				bonusPointsTextView.setText("" + mBean.bonuspoint);
			}
		});

		awardView.setText(btnStr);

		imgList = new ArrayList<String>();
		imgList.add("null");
		imgCountView.setText(imgList.size() - 1 + "/9");
		remarkImgListviewAdapter = new RemarkImgListviewAdapter(mContext, imgList);

		listView.setAdapter(remarkImgListviewAdapter);

		remarkImgListviewAdapter.setDeleteClickListener(new MyDeleteClickListener() {

			@Override
			public void deleteClick(View v, int position) {
				isDeleteRes=true;
				imgList.remove(position);
				imgCountView.setText(imgList.size() - 1 + "/9");
				remarkImgListviewAdapter.notifyDataSetChanged();
			}
		});

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				if(isDeleteRes){
					isDeleteRes=false;
					return;
				}
				
				
				if (position == imgList.size() - 1) {// 添加图片
					if (imgList.size() < 10) {
						popOnClickListener.lastItemtemClick();
					} else {
						Toast.makeText(mContext, "最多只能添加９张图片！", Toast.LENGTH_SHORT).show();
					}

				} else {
					popOnClickListener.itemClick(position);
				}
			}
		});

		addImgView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				popOnClickListener.saveRemark(v);

			}
		});

		awardView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				popOnClickListener.awardClick();

			}
		});

		titleEditView.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				mBean.badgeTitle = String.valueOf(s);
			}
		});
		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				mBean.badgeRemark = String.valueOf(s);
			}
		});

		this.setOutsideTouchable(true);
		this.setContentView(this.view);
		this.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
		this.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);
		this.setFocusable(true);
		ColorDrawable dw = new ColorDrawable(0xb0000000);
		this.setBackgroundDrawable(dw);
		this.setAnimationStyle(R.style.take_photo_anim);
	}

	public void addImgList(ArrayList<String> imgs) {
		imgList.addAll(0, imgs);
		imgCountView.setText(imgList.size() - 1 + "/9");
		remarkImgListviewAdapter.notifyDataSetChanged();

	}

	public ArrayList<String> getRemarkImages() {
		return imgList;
	}

	public BadgeInfo getRemarkData() {
		return mBean;
	}

	public void setBonusPoints(int bonusPoints) {
		this.mBean.bonuspoint = bonusPoints;
	}

	public void updateImgList(ArrayList<String> imgs) {
		imgList.clear();
		imgList.addAll(imgs);
		imgCountView.setText(imgList.size() - 1 + "/9");
		remarkImgListviewAdapter.notifyDataSetChanged();

	}

}
