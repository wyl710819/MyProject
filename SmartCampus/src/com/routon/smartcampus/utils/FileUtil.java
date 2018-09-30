package com.routon.smartcampus.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.os.Environment;
import android.util.Log;

public class FileUtil {
    
	public static String getSDPath(){  
		File sdDir = null;  
		boolean sdCardExist = Environment.getExternalStorageState()  
		.equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在  
//		Log.d("FileUtil","getSDPath sdCardExist:"+sdCardExist);
		if (sdCardExist)//为true的话，内置sd卡存在
		{  
			sdDir = Environment.getExternalStorageDirectory();//获取跟目录  
//			Log.d("FileUtil","getSDPath sdDir:"+sdDir);
			return sdDir.toString();  
		}else{//判断外置SD卡是否挂载：
			String extSdcardPath = System.getenv("SECONDARY_STORAGE");
//			Log.d("FileUtil","getSDPath extSdcardPath:"+extSdcardPath);
			return extSdcardPath;	
		}	
	}
	
	/** 
     * 获取外置SD卡路径 
     * @return  应该就一条记录或空 
     */  
    public static List<String> getExtSDCardPath()  
    {  
        List<String> lResult = new ArrayList<String>();  
        try {  
            Runtime rt = Runtime.getRuntime();  
            Process proc = rt.exec("mount");  
            InputStream is = proc.getInputStream();  
            InputStreamReader isr = new InputStreamReader(is);  
            BufferedReader br = new BufferedReader(isr);  
            String line;  
            while ((line = br.readLine()) != null) {  
                if (line.contains("extSdCard"))  
                {  
                    String [] arr = line.split(" ");  
                    String path = arr[1];  
                    File file = new File(path);  
                    if (file.isDirectory())  
                    {  
                        lResult.add(path);  
                    }  
                }  
            }  
            isr.close();  
        } catch (Exception e) {  
        }  
        return lResult;  
    }  
	
	//下面这个函数用于将字节数组换成成16进制的字符串

   public static String byteArrayToHex(byte[] b) {
        String hs = "";   
        String stmp = "";   
        for (int n = 0; n < b.length; n++) {   
            stmp = (Integer.toHexString(b[n] & 0XFF));   
            if (stmp.length() == 1) {   
                hs = hs + "0" + stmp;   
            } else {   
                hs = hs + stmp;   
            }   
            if (n < b.length - 1) {   
                hs = hs + "";   
            }   
        }   
        // return hs.toUpperCase();   
        return hs;

	      // 首先初始化一个字符数组，用来存放每个16进制字符

	      /*char[] hexDigits = {'0','1','2','3','4','5','6','7','8','9', 'A','B','C','D','E','F' };

	 

	      // new一个字符数组，这个就是用来组成结果字符串的（解释一下：一个byte是八位二进制，也就是2位十六进制字符（2的8次方等于16的2次方））

	      char[] resultCharArray =new char[byteArray.length * 2];

	      // 遍历字节数组，通过位运算（位运算效率高），转换成字符放到字符数组中去

	      int index = 0;

	      for (byte b : byteArray) {

	         resultCharArray[index++] = hexDigits[b>>> 4 & 0xf];

	         resultCharArray[index++] = hexDigits[b& 0xf];

	      }

	      // 字符数组组合成字符串返回

	      return new String(resultCharArray);*/

	}
	
	public static String fileMD5(String inputFile) throws IOException {
	      // 缓冲区大小（这个可以抽出一个参数）
	      int bufferSize = 256 * 1024;
	      FileInputStream fileInputStream = null;
	      DigestInputStream digestInputStream = null;
	      try {
	         // 拿到一个MD5转换器（同样，这里可以换成SHA1）
	         MessageDigest messageDigest =MessageDigest.getInstance("MD5");
	         // 使用DigestInputStream
	         fileInputStream = new FileInputStream(inputFile);
	         digestInputStream = new DigestInputStream(fileInputStream,messageDigest);
	         // read的过程中进行MD5处理，直到读完文件
	         byte[] buffer =new byte[bufferSize];
	         while (digestInputStream.read(buffer) > 0);
	         // 获取最终的MessageDigest
	         messageDigest= digestInputStream.getMessageDigest();
	         // 拿到结果，也是字节数组，包含16个元素
	         byte[] resultByteArray = messageDigest.digest();
	         // 同样，把字节数组转换成字符串
	         return byteArrayToHex(resultByteArray);
	      } catch (NoSuchAlgorithmException e) {
	         return null;
	      } finally {
	         try {
	            digestInputStream.close();
	         } catch (Exception e) {
	         }
	         try {
	            fileInputStream.close();
	         } catch (Exception e) {
	         }
	      }
	   }
	
	public static String getImageFormatName(String studentId,String imageLastUpdateTime){
//		Calendar calendar = TimeUtils.getFormatCalendar(imageLastUpdateTime, TimeUtils.FORMAT_yyyy_MM_dd_HH_mm_ss);
//		if( calendar == null ) return null;
//		SimpleDateFormat sdf = new SimpleDateFormat(TimeUtils.FORMAT_yyyyMMddHHmmss);
		return studentId+"_"+imageLastUpdateTime+".jpg";
	}
	
	 /**   
     * DeCompress the ZIP to the path   
     * @param zipFileString  name of ZIP   
     * @param outPathString   path to be unZIP  
     * @throws Exception   
     */    
    public static void UnZipFolder(String zipFileString, String outPathString) throws Exception {    
        ZipInputStream inZip = new ZipInputStream(new FileInputStream(zipFileString));    
        ZipEntry zipEntry;    
        String szName = "";    
        while ((zipEntry = inZip.getNextEntry()) != null) {    
            szName = zipEntry.getName();    
            if (zipEntry.isDirectory()) {    
                // get the folder name of the widget    
                szName = szName.substring(0, szName.length() - 1);    
                File folder = new File(outPathString + File.separator + szName);    
                folder.mkdirs();    
            } else {    
            
                File file = new File(outPathString + File.separator + szName);    
                file.createNewFile();    
                // get the output stream of the file    
                FileOutputStream out = new FileOutputStream(file);    
                int len;    
                byte[] buffer = new byte[1024];    
                // read (len) bytes into buffer    
                while ((len = inZip.read(buffer)) != -1) {    
                    // write (len) byte from buffer at the position 0    
                    out.write(buffer, 0, len);    
                    out.flush();    
                }    
                out.close();    
            }    
        }   
        inZip.close();    
    }  

	 /**
     * 根据byte数组生成文件
     * 
     * @param bytes
     *            生成文件用到的byte数组
     */
    public static void createFileWithByte(byte[] bytes,String savePath) {
        // TODO Auto-generated method stub
        /**
         * 创建File对象，其中包含文件所在的目录以及文件的命名
         */
        File file = new File(savePath);
        File parentFile = file.getParentFile();
        if( parentFile.exists() == false ){
        	parentFile.mkdirs();
        }
        file = new File(savePath);
        // 创建FileOutputStream对象
        FileOutputStream outputStream = null;
        // 创建BufferedOutputStream对象
        BufferedOutputStream bufferedOutputStream = null;
        try {
            // 如果文件存在则删除
            if (file.exists()) {
                file.delete();
            }
            // 在文件系统中根据路径创建一个新的空文件
            file.createNewFile();
            // 获取FileOutputStream对象
            outputStream = new FileOutputStream(file);
            // 获取BufferedOutputStream对象
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            // 往文件所在的缓冲输出流中写byte数据
            bufferedOutputStream.write(bytes);
            // 刷出缓冲输出流，该步很关键，要是不执行flush()方法，那么文件的内容是空的。
            bufferedOutputStream.flush();
        } catch (Exception e) {
            // 打印异常信息
            e.printStackTrace();
        } finally {
            // 关闭创建的流对象
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 获取指定文件大小(单位：字节)
     *
     * @param file
     * @return
     * @throws Exception
     */
    public static long getFileSize(File file) throws Exception {
        if (file == null) {
            return 0;
        }
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
        }
        return size;
    }
    
    public static List<String> getImagesFromPath(String filePath) {  
        // 图片列表  
        List<String> imagePathList = new ArrayList<String>();  
 
        // 得到该路径文件夹下所有的文件  
        File fileAll = new File(filePath);  
        if( fileAll == null ){
        	return imagePathList;
        }
        File[] files = fileAll.listFiles();  
        // 将所有的文件存入ArrayList中,并过滤所有图片格式的文件  
        for (int i = 0; i < files.length; i++) {  
            File file = files[i];  
            if( file.isDirectory() == true ){//图片
            	imagePathList.addAll(getImagesFromPath(file.getAbsolutePath()));
            }else if (checkIsImageFile(file.getPath())) {  
                imagePathList.add(file.getPath());  
            }
        }  
        // 返回得到的图片列表  
        return imagePathList;  
    }  
  
    /** 
     * 检查扩展名，得到图片格式的文件 
     * @param fName  文件名 
     * @return 
     */  
    public static boolean checkIsImageFile(String fName) {  
        boolean isImageFile = false;  
        // 获取扩展名  
        String FileEnd = fName.substring(fName.lastIndexOf(".") + 1,  
                fName.length()).toLowerCase();  
        if (FileEnd.equals("jpg") || FileEnd.equals("png") || FileEnd.equals("gif")  
                || FileEnd.equals("jpeg")|| FileEnd.equals("bmp") ) {  
            isImageFile = true;  
        } else {  
            isImageFile = false;  
        }  
        return isImageFile;  
    }  
    
   //删除文件夹和文件夹里面的文件
    public static void deleteDir(final String pPath) {
        File dir = new File(pPath);
        deleteDirWithFile(dir);
    }

    public static void deleteDirWithFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
            	deleteDirWithFile(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }
}
