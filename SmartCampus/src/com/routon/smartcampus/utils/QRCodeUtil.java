package com.routon.smartcampus.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Hashtable;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Base64;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * 二维码生成工具类
 */
public class QRCodeUtil {
    /**
     * 生成二维码Bitmap
     *
     * @param content   内容
     * @param widthPix  图片宽度
     * @param heightPix 图片高度
     * @param logoBm    二维码中心的Logo图标（可以为null）
     * @param filePath  用于存储二维码图片的文件路径
     * @return 生成二维码及保存文件是否成功
     */
    public static Bitmap createQRImage(String content, int widthPix, int heightPix, Bitmap logoBm, String filePath) {
        try {
            if (content == null || "".equals(content)) {
                return null;
            }
            
            Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
			content = new String(content.getBytes("UTF-8"),"ISO-8859-1");
			hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);  
			// 图像数据转换，使用了矩阵转换
			BitMatrix bitMatrix = new QRCodeWriter().encode(content,
					BarcodeFormat.QR_CODE, widthPix, heightPix, hints);
			int[] pixels = new int[widthPix * heightPix];
			// 下面这里按照二维码的算法，逐个生成二维码的图片，
			// 两个for循环是图片横列扫描的结果

            // 下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (int y = 0; y < heightPix; y++) {
                for (int x = 0; x < widthPix; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * widthPix + x] = 0xff000000;
                    } else {
                        pixels[y * widthPix + x] = 0xffffffff;
                    }
                }
            }
 
            // 生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.RGB_565);
            bitmap.setPixels(pixels, 0, widthPix, 0, 0, widthPix, heightPix);
 
            if (logoBm != null) {
                bitmap = addLogo(bitmap, logoBm);
            }
            
            if( bitmap != null ){
            	File f = new File(filePath);
            	if( f.exists() ){
            		f.delete();
            	}
            	bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(f));
            	//必须使用compress方法将bitmap保存到文件中再进行读取。直接返回的bitmap是没有任何压缩的，内存消耗巨大！
            	return bitmap;
            }
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
 
        return null;
    }
    
    public static Bitmap createQRTextImage(String imagecontent,String textContent, int widthPix, int heightPix,int textSize,String filePath){
    	Log.d("qrcodeutil","createQRTextImage content:"+imagecontent);
    	Bitmap textBitmap = createTextImage(widthPix/3,heightPix/3,textSize,textContent);
    	return createQRImage(imagecontent,widthPix,heightPix,textBitmap,filePath);
    }
    
    /**
	 * 创建指定大小的包含文字的图片，背景为透明
	 * @param width      图片宽度
	 * @param height     图片高度
	 * @param txtSize    文字字号
	 * @param innerTxt   内容文字
	 * @return
	 */
	public static Bitmap createTextImage(int width,int height,int txtSize,String innerTxt) {
		//若使背景为透明，必须设置为Bitmap.Config.ARGB_4444
		Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		
		Canvas canvas = new Canvas(bm);
		
		Paint paint = new Paint();
		paint.setColor(Color.rgb(16, 164, 183));
		RectF r = new RectF();
		r.left = 0;
		r.right = width;
		r.top = 0 ;
		r.bottom = height;
		canvas.drawRoundRect(r, width/6, height/6,paint);
//		paint.setColor(Color.WHITE);
        
//		Rect bounds = new Rect();  	
		    
//		paint.setTextSize(txtSize);
//		paint.setTypeface(Typeface.DEFAULT_BOLD);
//		
//		paint.setTextAlign(Align.CENTER);  	  
//		paint.getTextBounds(innerTxt, 0, innerTxt.length(), bounds); 
//		
//		//计算得出文字的绘制起始x、y坐标
//		 int posX = width/2;  
//		 int posY = (height + bounds.height())/2;   
		
//		canvas.drawText(innerTxt, posX, posY, paint);
		
		TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(txtSize);	  
        textPaint.setTextAlign(Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        StaticLayout sl= new StaticLayout(innerTxt, textPaint, bm.getWidth()-8, StaticLayout.Alignment.ALIGN_NORMAL, 1.5f, 0.0f, false);
        canvas.translate(width/2, (height-sl.getHeight())/2);
        sl.draw(canvas);
	
		
		return bm;
	}
 
    /**
     * 在二维码中间添加Logo图案
     */
    private static Bitmap addLogo(Bitmap src, Bitmap logo) {
        if (src == null) {
            return null;
        }
 
        if (logo == null) {
            return src;
        }
 
        //获取图片的宽高
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();
 
        if (srcWidth == 0 || srcHeight == 0) {
            return null;
        }
 
        if (logoWidth == 0 || logoHeight == 0) {
            return src;
        }
 
        //logo大小为二维码整体大小的1/5
        float scaleFactor = srcWidth * 1.0f / 3 / logoWidth;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.RGB_565);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);
 
            canvas.save(Canvas.ALL_SAVE_FLAG);
            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }
 
        return bitmap;
    }
    
    /**
     * 解析二维码图片
     * @param path
     * @return
     */
    public static Result scanningImage(String bitmapPath) {        
     // DecodeHintType 和EncodeHintType  
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();  
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8"); // 设置二维码内容的编码  
        BitmapFactory.Options options = new BitmapFactory.Options();  
        options.inJustDecodeBounds = true; // 先获取原大小  
        Bitmap bitmap = BitmapFactory.decodeFile(bitmapPath, options);  
       
        options.inJustDecodeBounds = false; // 获取新的大小  
  
        int sampleSize = (int) (options.outHeight / (float) 400);  
  
        if (sampleSize <= 0)  
            sampleSize = 1;  
        options.inSampleSize = sampleSize;  
        bitmap = BitmapFactory.decodeFile(bitmapPath, options);  
        if( bitmap == null ){
        	return null;
        }
        
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        final int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);

        //将图片转换成二进制图片  
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));  
        //初始化解析对象  
        QRCodeReader reader = new QRCodeReader();  
        //开始解析  
        Result result = null;  
        try {  
            result = reader.decode(binaryBitmap, hints);  
        } catch (Exception e) {  
            // TODO: handle exception  
        	e.printStackTrace();
        }  
          
        return result; 
    }
    
    private static final String DES_KEY = "Routon17";
    
    public static String Bytes2HexString(byte[] b) { 
        String ret = ""; 
        for (int i = 0; i < b.length; i++) { 
            String hex = Integer.toHexString(b[i] & 0xFF); 
            if (hex.length() == 1) { 
                hex = '0' + hex; 
            } 
            ret += hex.toUpperCase(); 
        } 
        return ret; 
    } 
	
	public static String[] decodeQR(String encodeData,Context mContext) {
		if( encodeData == null || encodeData.isEmpty() == true ){
			return null;
		}
        byte[] encodeByte;
		try {
			encodeByte = Base64.decode(encodeData, Base64.DEFAULT);
		} catch (IllegalArgumentException e2) {
			Log.e("base64", "base64解码错误");
			e2.printStackTrace();
			return null;
		}
		Log.d("QRCodeUtil","decodeQR encodeByte:"+Bytes2HexString(encodeByte)+",encodeData:"+encodeData);
        IvParameterSpec zeroIv = new IvParameterSpec(DES_KEY.getBytes());
        SecretKeySpec key = new SecretKeySpec(DES_KEY.getBytes(), "DES");
        Cipher cipher=null;
        byte decryptedData[]=null;
		try {
			cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Log.e("QRCodeUtil", "没有此种解密算法");
			return null;
		} catch (NoSuchPaddingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}  
        try {
			cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("QRCodeUtil", "解密密匙错误");
			return null;
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("QRCodeUtil", "错误的解密参数");
			return null;
		}
		try {
			decryptedData = cipher.doFinal(encodeByte);
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("QRCodeUtil", "需解密的字符串非法");
			return null;
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}  
		Log.d("QRCodeUtil","decodeQR decryptedData:"+Bytes2HexString(decryptedData));
        String tempString = new String(decryptedData);
        Log.e("deData",tempString);
        if( tempString == null || tempString.isEmpty() == true ){
        	return null;
        }
        return tempString.split(",");
    }
 
}