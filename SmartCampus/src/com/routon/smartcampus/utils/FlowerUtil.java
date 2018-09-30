package com.routon.smartcampus.utils;

import com.routon.edurelease.R;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.widget.ImageView;

public class FlowerUtil {

//	public Bitmap bitmap2Gray(Bitmap bmSrc) {  
//        // 得到图片的长和宽  
//        int width = bmSrc.getWidth();  
//        int height = bmSrc.getHeight();  
//        // 创建目标灰度图像  
//        Bitmap bmpGray = null;  
//        bmpGray = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);  
//        // 创建画布  
//        Canvas c = new Canvas(bmpGray);  
//        Paint paint = new Paint();  
//        ColorMatrix cm = new ColorMatrix();  
//        cm.setSaturation(0);  
//        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);  
//        paint.setColorFilter(f);  
//        c.drawBitmap(bmSrc, 0, 0, paint);  
//        return bmpGray;  
//    }  
//	// 创建带字母的标记图片  
//	public static Bitmap createTextFlowerBitmap(Context context,Bitmap imgMarker,String text) {  
//		if( text == null ){
//			return imgMarker;
//		}	
//		  
//		float density  = 3;  
//		Log.d("FlowerUtil","createTextFlowerBitmap text:"+text);
//		
//		int width = imgMarker.getWidth();
//		int height = imgMarker.getHeight();
//		Bitmap imgTemp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);  
//	    Canvas canvas = new Canvas(imgTemp);  
//	    Paint paint = new Paint(); // 建立画笔  
//	    paint.setDither(true);  
//	    paint.setFilterBitmap(true);  
//	    Rect src = new Rect(0, 0, width, height);  
//	    Rect dst = new Rect(0, 0, width, height);  
//	    canvas.drawBitmap(imgMarker, src, dst, paint);  
//	
////	    Path path = new Path();
////	    path.addRect(10, 400, 400, 460, Direction.CW);
////	    RectF oval = new RectF(13, 74, 148, 140);
//	  //  path.addOval(oval, Path.Direction.CW);//椭圆路径
////	    path.addArc(oval, 210,120);//椭圆路径
////	    paint.setStyle(Paint.Style.STROKE);  
//	   
////	    canvas.drawPath(path, paint);  
////	    paint.setColor(Color.RED);  
//	    
//	    Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG  
//	            | Paint.DEV_KERN_TEXT_FLAG);  
//	    textPaint.setTextSize(46);  
//	    Typeface font = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
//	    textPaint.setTypeface(font); // 采用默认的宽度  
//	    textPaint.setColor(Color.WHITE);  
//	    
//	    textPaint.setTextAlign(Paint.Align.CENTER);
//	     
//	    textPaint.setStyle(Paint.Style.FILL);
//	    textPaint.setShadowLayer(1f*density, 1.0f*density, 1.0f*density, Color.BLACK);
//	    canvas.drawText(text, 136,244, textPaint);
//	
//	    canvas.save(Canvas.ALL_SAVE_FLAG);  
//	    canvas.restore();  
//	   // imgTemp = bitmap2Gray(imgTemp);
//	    return imgTemp;  
//
//	}  
	
//	public static void loadFlower(final Context context,final ImageView view,final String name){
//		File cacheDir = context.getCacheDir();
//	
//		String dirName = StringUtil.stringToMD5(name);
//		File flowerDir = new File(cacheDir,dirName);
//		if( flowerDir.exists() == false ){
//			flowerDir.mkdir();
//		}
//		String fileName = dirName+".png";
//		final File textFlowerFile = new File(flowerDir,fileName);
//		//test
////		textFlowerFile.delete();
//		if( textFlowerFile.exists() == true ){//缓存文件存在
//			Log.d("flowerUtil","exits text flower file");
//			Bitmap bitmap = BitmapFactory.decodeFile(textFlowerFile.getAbsolutePath());
//			view.setBackground(new BitmapDrawable(context.getResources(),bitmap));
//		}else{
//			Bitmap defaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.flower);
//			
//			Bitmap newBitmap = FlowerUtil.createTextFlowerBitmap(context, defaultBitmap, name);
//			view.setBackground(new BitmapDrawable(context.getResources(),FlowerUtil.createTextFlowerBitmap(context, defaultBitmap, name)));		
//			try {
//				FileOutputStream out;
//				out = new FileOutputStream(textFlowerFile);
//				newBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);  
//		        out.flush();  
//		        out.close(); 
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}         
//			
//		}
//	}
	
//	public static void cleanFlowers(Context context){
//		File cacheDir = context.getCacheDir();
//		if( cacheDir == null ) return;
//		File parentDir = new File(cacheDir,"flowers");
//		FileUtil.deleteDirWithFile(parentDir);
//	}
	
	public static void loadFlower(final Context context,final ImageView view,final String name,String imgUrl){
		if ( imgUrl!=null && view!=null && imgUrl.trim().length() > 0 ) {
			Picasso.with(context).load(imgUrl).error(R.drawable.flower)
			.placeholder(R.drawable.flower).into(view);
		}
		
		
		
		
		
//		Log.d("flowerUtil","loadFlower imgUrl:"+imgUrl+",name:"+name);
//		File cacheDir = context.getCacheDir();
//		String dirName = StringUtil.stringToMD5(imgUrl);
//		File parentDir = new File(cacheDir,"flowers");
//		if( parentDir.exists() == false ){
//			parentDir.mkdir();
//		}
//		File flowerDir = new File(parentDir,dirName);
//		if( flowerDir.exists() == false ){
//			flowerDir.mkdir();
//		}
//		String fileName = StringUtil.stringToMD5(name)+".png";
//		final File textFlowerFile = new File(flowerDir,fileName);
//		//test
////		textFlowerFile.delete();
//		if( textFlowerFile.exists() == true ){//缓存文件存在
//			Log.d("flowerUtil","exits text flower file");
//			Bitmap bitmap = BitmapFactory.decodeFile(textFlowerFile.getAbsolutePath());
//			view.setBackground(new BitmapDrawable(context.getResources(),bitmap));
//		}else{
//			Bitmap defaultBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.flower);
//			view.setBackground(new BitmapDrawable(context.getResources(),FlowerUtil.createTextFlowerBitmap(context, defaultBitmap, name)));
//			ImageListener imageListener = new ImageListener(){
//	
//				@Override
//				public void onErrorResponse(VolleyError error) {
//					// TODO Auto-generated method stub
//					
//				}
//	
//				@Override
//				public void onResponse(ImageContainer response, boolean isImmediate) {
//					// TODO Auto-generated method stub
//					Bitmap bitmap = response.getBitmap();
//					if( bitmap != null ){
//						Bitmap newBitmap = FlowerUtil.createTextFlowerBitmap(context, bitmap, name);
//						view.setBackground(new BitmapDrawable(context.getResources(),newBitmap));
//						view.setVisibility(View.VISIBLE);
//						if( textFlowerFile.exists() == false ){
//							//保存文件到本地
//							try {  
//						        FileOutputStream out = new FileOutputStream(textFlowerFile);  
//						        newBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);  
//						        out.flush();  
//						        out.close();  
//						    } catch (Exception e) {  
//						        e.printStackTrace();  
//						    }  
//						}
//					}
//				}
//				
//			};
//			InfoReleaseApplication.mImageLoader.get(imgUrl, imageListener);
//		}
	}
}
