package com.routon.inforelease.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import com.routon.ad.element.StringUtils;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class ImageUtils {
	
	public interface loadCallBack{
		void loadCb(File file,String portrait);
	}
	
	public static final int MAX_CLASS_PICTURE_NUM = 9;
	    
	 /**
	  * 判断sd卡是否存在
	  */
    public static boolean isHasSdcard() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }
    
    public static String getSmallImageUrl(Context context,String imagePath){
//    	DisplayMetrics  dm = new DisplayMetrics();    
//		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm); 
		int screenWidth = 512;//dm.widthPixels;;
		int lastIndexOfDot = imagePath.lastIndexOf("."); 
		int tailLength = imagePath.length() - lastIndexOfDot;
		String smallImageUrl = new String();
		smallImageUrl += imagePath.substring(0, lastIndexOfDot);
		smallImageUrl += "_" + screenWidth + "x" + screenWidth;
		smallImageUrl += imagePath.substring(imagePath.length()-tailLength, imagePath.length());
		return smallImageUrl;
    }
    
    public static String getPreviewImageUrl(Context context,String imagePath){
//    	DisplayMetrics  dm = new DisplayMetrics();    
//		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm); 
		int screenWidth = 1024;//dm.widthPixels;;
		int lastIndexOfDot = imagePath.lastIndexOf("."); 
		int tailLength = imagePath.length() - lastIndexOfDot;
		String smallImageUrl = new String();
		smallImageUrl += imagePath.substring(0, lastIndexOfDot);
		smallImageUrl += "_" + screenWidth + "x" + screenWidth;
		smallImageUrl += imagePath.substring(imagePath.length()-tailLength, imagePath.length());
		return smallImageUrl;
    }

	public static Bitmap loadBitmap(String path, int maxwidth, int maxheight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);

		// 调用上面定义的方法计算inSampleSize值
		options.inSampleSize = calculateInSampleSize(options, maxwidth, maxheight);
		// 使用获取到的inSampleSize值再次解析图片
		options.inJustDecodeBounds = false;
		bitmap = BitmapFactory.decodeFile(path, options);
		return bitmap;
	}
	
	public static Bitmap getBitmapFromView(View v) {
	    Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_4444);
	    Canvas c = new Canvas(b);
	    v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
	    // Draw background
	    Drawable bgDrawable = v.getBackground();
	    if (bgDrawable != null)
	        bgDrawable.draw(c);
	    else
	        c.drawColor(Color.TRANSPARENT);
	    // Draw view to canvas
	    v.draw(c);
	    return b;
	}
	
	/**
	 * 计算inSampleSize，用于压缩图片
	 * 
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	private static int calculateInSampleSize(BitmapFactory.Options options,	int reqWidth, int reqHeight) {
		// 源图片的宽度
		int width = options.outWidth;
		int height = options.outHeight;
		int inSampleSize = 1;

		 if (height > reqHeight || width > reqWidth) {
            //计算图片高度和我们需要高度的最接近比例值
            final int heightRatio = (int) Math.ceil((float) height / (float) reqHeight);
            //宽度比例值
            final int widthRatio = (int) Math.ceil((float) width / (float) reqWidth);
            //取比例值中的较大值作为inSampleSize
            inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
        }
		 
		//用于压缩图片的值一般为２的次方，如果不是，读取图片时会四舍五入，结果还是可能超出reqWidth或者reqHeight
		int roundedSize;
	    if (inSampleSize <= 8) {
	        roundedSize = 1;
	        while (roundedSize < inSampleSize) {
	            roundedSize <<= 1;
	       }
	    } else {
	        roundedSize = (inSampleSize + 7) / 8 * 8;
	    }
	    return roundedSize;
	}
	
	  public static void copyFile(String oldPath, String newPath) {   
		  if( newPath.equals(oldPath) ){
			  return;
		  }
	       try {   
	           int bytesum = 0;   
	           int byteread = 0;   
	           File oldfile = new File(oldPath);   
	           
	           if (oldfile.exists()) { //文件存在时   
	        	   File newfile = new File(newPath);
	        	   newfile.delete();
	               InputStream inStream = new FileInputStream(oldPath); //读入原文件   
	               FileOutputStream fs = new FileOutputStream(newPath);   
	               byte[] buffer = new byte[1444];   
	               int length;   
	               while ( (byteread = inStream.read(buffer)) != -1) {   
	                   bytesum += byteread; //字节数 文件大小   
	                   System.out.println(bytesum);   
	                   fs.write(buffer, 0, byteread);   
	               }   
	               inStream.close();   
	           }   
	       }   
	       catch (Exception e) {   
	           System.out.println("复制单个文件操作出错");   
	           e.printStackTrace();   
	  
	       }   
	  
	   }   
	
	//下载头像图片
	public static void downloadAndSaveProfilePhoto(Context context,final String imageUrl,final String portait,String lastUpdateTime,
			final loadCallBack cb){
        if( imageUrl != null && portait != null && portait.trim().isEmpty() == false ){
        	//save as sid_20171207160235.jpg 
        	final File dcimFile = ImageUtils.getProfilePhoto(context,portait,lastUpdateTime);
        	//文件存在，无需下载
        	if( dcimFile.exists() == true ){
        		if( cb != null ){
        			cb.loadCb(dcimFile,portait);
        		}
        		return;
        	}
        	//when download,also save ad sid.jpg
        	//登录界面无法知道lastUpdateTime，直接读取sid.jpg
        	final File portraitFile = ImageUtils.getProfilePhoto(context,portait,null);
          
        	Log.d("download","downloadAndSaveProfilePhoto url:"+imageUrl+",dcimFile:"+dcimFile.getAbsolutePath());
        	
        	final WeakReference<Context> srContext = new WeakReference<Context>(context);

            Thread thread=new Thread(new Runnable()  
            {  
                @Override  
                public void run()  
                {  
                	  // 从网络上获取图片
                    URL url;
					try {
						url = new URL(imageUrl);
						HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		                    conn.setConnectTimeout(5000);
		                    conn.setRequestMethod("GET");
		                    conn.setDoInput(true);
		                    if (conn.getResponseCode() == 200) {

		                        InputStream is = conn.getInputStream();
		                        FileOutputStream fos = new FileOutputStream(dcimFile);
		                        byte[] buffer = new byte[1024];
		                        int len = 0;
		                        while ((len = is.read(buffer)) != -1) {
		                            fos.write(buffer, 0, len);
		                        }
		                        is.close();
		                        fos.close();
		                        copyFile(dcimFile.getAbsolutePath(),portraitFile.getAbsolutePath());
		                        Context context = srContext.get();
		                        if( context != null ){
			                        ((Activity) context).runOnUiThread(new Runnable() {
			                            @Override
			                            public void run() {
			                            	 if( cb != null ){            		                
			 		                			cb.loadCb(dcimFile,portait);
			 		                		}
			                            }
			                        });
		                        }
		                       
		                    }
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                   
                }  
            });  
            thread.start();  
        }
	}
	
	//下载头像图片
	public static void downloadAndSaveProfilePhoto(final Context context,final String imageUrl,String portait,String lastUpdateTime){
		downloadAndSaveProfilePhoto(context,imageUrl,portait,lastUpdateTime,null);
	}
	
	public static String getImageFormatName(String studentId,String imageLastUpdateTime){
		if( imageLastUpdateTime == null ){
			return studentId+".jpg";
		}
		return studentId+"_"+imageLastUpdateTime+".jpg";
	}
	
	public static File getProfilePhoto(Context context,String portrait,String imageLastUpdateTime){
		if( portrait == null || portrait.isEmpty() == true ){
			return null;
		}
//		Log.d("ImageUtils","getProfilePhoto name:"+name);
		String time = null;
		if( imageLastUpdateTime != null && imageLastUpdateTime.isEmpty() == false ){
			time = TimeUtils.getTime(imageLastUpdateTime, "yyyyMMddHHmmss");
		}
    	String filename = getImageFormatName(portrait, time);
		File file = new File(context.getExternalCacheDir(),filename);
		return file;
	}

	public static Bitmap decodeThumbFile(String path) {
		path = path.replaceFirst("file://", "");
		int index = path.lastIndexOf('_');
		if (index > 0) {
			int jndex = path.lastIndexOf('.');
			if (jndex > 0 && jndex > index) {
				String p = path.substring(0, index) + path.substring(jndex);
				//Log.v(TAG, "p: " + p);
				String wxh = path.substring(index + 1, jndex);
				String[] split = wxh.split("x");
				int w = 100;
				int h = 100;
				if (split.length == 2) {
					w = StringUtils.toInteger(split[0], 100);
					h = StringUtils.toInteger(split[1], 100);
				}
				return loadBitmap(p, w, h);
			}
		}
		return BitmapFactory.decodeFile(path);
	}
	
	public static void saveBitmap(Bitmap bitmap,Uri uri) {
		if( bitmap == null || uri == null ){
			return;
		}
        File file = new File(uri.getPath());
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	public static String getPath(Context context,Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            //Log.e("hxy", "uri auth: " + uri.getAuthority());
            if (isExternalStorageDocument(uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] split = docId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            } else if (isMedia(uri)) {
                String[] proj = {MediaStore.Images.Media.DATA};
                Cursor actualimagecursor = ((Activity) context).managedQuery(uri, proj, null, null, null);
                int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                actualimagecursor.moveToFirst();
                return actualimagecursor.getString(actual_image_column_index);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri,
                                       String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isMedia(Uri uri) {
        return "media".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

}
