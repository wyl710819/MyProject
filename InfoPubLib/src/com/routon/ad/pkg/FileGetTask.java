package com.routon.ad.pkg;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

public class FileGetTask extends HttpGetTask {
	private static final String TAG = "FileGetTask";

	private String mDestFilePath;

	private FileOutputStream mFOS;

	public FileGetTask(String url, String path) {
		super(url);

		mDestFilePath = path;
		Log.v(TAG, "path: " + path);
	}

	public FileGetTask(String url, String path, Object context) {
		super(url, context);

		mDestFilePath = path;
		Log.v(TAG, "path: " + path);
	}

	public String getFilePath() {
		return mDestFilePath;
	}

	@Override
	public void run() {
		if (mUrl.startsWith("file://")) {
			notifyTaskStarted();
			boolean ret = downloadImageFromLocal(mUrl.replaceFirst("file://", ""), mDestFilePath);
			notifyTaskFinished(ret ? ERR_NONE : ERR_TRANSFER);
		} else {
			super.run();
		}
	}
	
	private boolean downloadImageFromLocal(String srcPath, String destPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath,newOpts);//此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        Log.v(TAG, "w: " + w + " h: " + h);
        float ww = 1920;
        float hh = 1080;
        int dw = 1920;
        int dh = 1080;
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int resampleSize = 1;//be=1表示不缩放
        if (w > 1920 || h > 1080) {//如果宽度大的话根据宽度固定大小缩放
            float sw = newOpts.outWidth / ww;
            float sh = newOpts.outHeight / hh;
            if (sw > sh) {
            	dh = (int) (newOpts.outHeight / sw);
            	resampleSize = (int) sw;
            } else {
            	dw = (int) (newOpts.outWidth / sh);
            	resampleSize = (int) sh;
            }
        }else{
        	return copyFileToDestPath(srcPath, destPath);
        }
        Log.v(TAG, "resampleSize: " + resampleSize);
        Log.v(TAG, "dw: " + dw + " dh: " + dh);
        
        String filename = new File(srcPath).getName();
        String out_file_path = destPath;
        
        newOpts.inSampleSize = resampleSize;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        Log.v(TAG, "w1: " + newOpts.outWidth + " h1: " + newOpts.outHeight);
        w = newOpts.outWidth;
        h = newOpts.outHeight;
        dw = 1920;
        dh = 1080;
        if (w > 1920 || h > 1080) {//如果宽度大的话根据宽度固定大小缩放
            float sw = newOpts.outWidth / ww;
            float sh = newOpts.outHeight / hh;
            Log.v(TAG, "sw: " + sw + " sh: " + sh);
            if (sw > sh) {
            	dw = 1920;
            	dh = (int) (newOpts.outHeight / sw);
            } else {
            	dw = (int) (newOpts.outWidth / sh);
            	dh = 1080;
            }
        }
        Log.v(TAG, "dw: " + dw + " dh: " + dh);
        Bitmap new_bitmap = zoomBitmap(bitmap, dw, dh);
        bitmap.recycle();
        File file2 = new File(out_file_path);  
        try {  
        	FileOutputStream out = new FileOutputStream(file2);
	        CompressFormat format;
	        if(filename.endsWith("png")||filename.endsWith("PNG")){
	        	 format = CompressFormat.PNG;
	        }else{
	        	 format = CompressFormat.JPEG;
	        }
	        if(new_bitmap.compress(format, 100, out)){  
	            out.flush();  
	            out.close();  
	        }  
	    } catch (Exception e) {  
	    	 // TODO: handle exception  
	    	e.printStackTrace();
	    } finally {
	    	new_bitmap.recycle();
	    }
        
        return true;
    }
	
	private boolean copyFileToDestPath(String srcPath, String destPath) {
		try {
			copyFile(srcPath, destPath);

			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidht = ((float) w / width);
		float scaleHeight = ((float) h / height);
		matrix.postScale(scaleWidht, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		return newbmp;
	}
	
	/**
	 * function: String 复制文件
	 * 
	 * @param sourceFile
	 *            源文件字符串
	 * @param targetFile
	 *            目标文件字符串
	 * @return None
	 */
	public static void copyFile(String sourceFile, String targetFile) throws IOException {
		BufferedInputStream inBuff = null;
		BufferedOutputStream outBuff = null;
		File tf = new File(targetFile);
		File fdir = new File(tf.getParent());
		if (!fdir.exists()) {
			fdir.mkdirs();
		}
		File sf = new File(sourceFile);
		try {
			// 新建文件输入流并对它进行缓冲
			inBuff = new BufferedInputStream(new FileInputStream(sf));

			// 新建文件输出流并对它进行缓冲
			outBuff = new BufferedOutputStream(new FileOutputStream(tf));

			// 缓冲数组
			byte[] b = new byte[1024 * 5];
			int len;
			while ((len = inBuff.read(b)) != -1) {
				outBuff.write(b, 0, len);
			}
			// 刷新此缓冲的输出流
			outBuff.flush();
		} finally {
			// 关闭流
			if (inBuff != null)
				inBuff.close();
			if (outBuff != null)
				outBuff.close();
		}
	}

	@Override
	protected void onBeginTransfer() {
		File file = new File(mDestFilePath);
		try {
			mFOS = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDataTransfer(byte[] buffer, int read_size) {
		if (mFOS != null) {
			try {
				mFOS.write(buffer, 0, read_size);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onEndTransfer(int code) {
		if (mFOS != null) {
			try {
				mFOS.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
