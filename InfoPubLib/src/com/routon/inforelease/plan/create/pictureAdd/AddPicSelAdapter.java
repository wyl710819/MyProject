package com.routon.inforelease.plan.create.pictureAdd;

import java.util.LinkedList;
import java.util.List;

import com.routon.inforelease.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import com.routon.widgets.Toast;


public class AddPicSelAdapter extends CommonAdapter<String>
{

	/**
	 * 用户选择的图片，存储为图片的完整路径
	 */
	public static List<String> mSelectedImage = new LinkedList<String>();

	/**
	 * 文件夹路径
	 */
	private String mDirPath;
	private int maxLength = 12;

	private OnCamerClicked onCamerClickedlistener = null;
	private OnImageClicked onImageClickedListener = null;

	protected boolean mIsCaptureImg=false;
	
	public AddPicSelAdapter(Context context, List<String> mDatas, int itemLayoutId,
			String dirPath, boolean isCaptureImg)
	{
		super(context, mDatas, itemLayoutId);
		this.mDirPath = dirPath;
		this.mIsCaptureImg=isCaptureImg;
	}

	@Override
	public void convert(final PictureSelViewHolder helper, final String item, final int position)
	{
		//设置no_pic
		helper.setImageResource(R.id.item_image, R.drawable.default_pic);
		
		//设置no_selected
		helper.setImageResourceVisibility(R.id.item_select, View.VISIBLE);
		helper.setImageResource(R.id.item_select,
						R.drawable.checkbox_normal);
		//设置图片
		if(mDirPath != null){
			helper.setImageByUrl(R.id.item_image, mDirPath + "/" + item);
		}else{
			if(item.equals("camera")){
				helper.setImageResourceVisibility(R.id.item_select, View.GONE);
				helper.setImageResource(R.id.item_image, R.drawable.camera);
			}else{
				helper.setImageByUrl(R.id.item_image, item);
			}
		}
		
		final ImageView mImageView = helper.getView(R.id.item_image);
		final ImageView mSelect = helper.getView(R.id.item_select);
		
		
		mImageView.setColorFilter(null);
		mImageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.i("imageViewClicked", "------position-"+position+"   path:"+mImageView.getTag());
				if(item.equals("camera")){
					if(onCamerClickedlistener != null)
						onCamerClickedlistener.onCamerClicked();
					return;
				}
				
				if(onImageClickedListener != null){
					String imagePath = null;
					if(mDirPath == null){
						imagePath = item;
					}else{
						imagePath = mDirPath + "/" + item;
					}
					onImageClickedListener.onImageClicked(imagePath);
				}
			
			}
		});
		
		//设置mSelect的点击事件
		mSelect.setOnClickListener(new OnClickListener()
		{
			//选择，则将图片变暗，反之则反之
			@Override
			public void onClick(View v)
			{			
				String imagePath = null;
				if(mDirPath == null){
					imagePath = item;
				}else{
					imagePath = mDirPath + "/" + item;
				}
				// 已经选择过该图片
				if (mSelectedImage.contains(imagePath))
				{
					mSelectedImage.remove(imagePath);
					mSelect.setImageResource(R.drawable.checkbox_normal);
					mImageView.setColorFilter(null);
				} else// 未选择该图片
				{
					if(mSelectedImage.size() < maxLength){
						mSelectedImage.add(imagePath);
						mSelect.setImageResource(R.drawable.checkbox_pressed);
						mImageView.setColorFilter(Color.parseColor("#77000000"));
					}else{
						Toast.makeText(mContext, "一次最多只能上传"+maxLength+"张图片,已超出范围!",Toast.LENGTH_LONG).show();
					}
				}
				

			}
		});
		
		/**
		 * 已经选择过的图片，显示出选择过的效果
		 */
		String currentImagePath = null;
		if(mDirPath == null){
			currentImagePath = item;
		}else{
			currentImagePath = mDirPath + "/" + item;
		}
		// 已经选择过该图片
		if (mSelectedImage.contains(currentImagePath)){
			mSelect.setImageResource(R.drawable.checkbox_pressed);
			mImageView.setColorFilter(Color.parseColor("#77000000"));
		}
		if (mIsCaptureImg&&position==1) {
			mSelectedImage.add(currentImagePath);
			mSelect.setImageResource(R.drawable.checkbox_pressed);
			mImageView.setColorFilter(Color.parseColor("#77000000"));
			mIsCaptureImg=false;
		}
	}
	
	public void setOnCamerClickedlistener(OnCamerClicked onCamerClickedlistener) {
		
		this.onCamerClickedlistener = onCamerClickedlistener;
	}

	public void setOnImageClickedListener(OnImageClicked onImageClickedListener) {
		this.onImageClickedListener = onImageClickedListener;
	}


	public interface OnCamerClicked{
		public void onCamerClicked();
	}
	
	public interface OnImageClicked{
		public void onImageClicked(String imagPath);
	}
}
