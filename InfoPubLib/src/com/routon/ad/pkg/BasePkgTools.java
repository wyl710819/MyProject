package com.routon.ad.pkg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.routon.inforelease.util.TimeUtils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public abstract class BasePkgTools {
	protected int mNextStep = 0;
	protected int mProgress;

	public static boolean mkdir(String dir_path) {
		File dir = new File(dir_path);
		return dir.mkdirs();
	}
	
	public static boolean mkdir(String parent, String dir_name) {
		return mkdir(new File(parent), dir_name);
	}
	
	public static boolean mkdir(File parent, String dir_name) {
		File dir = new File(parent, dir_name);
		return dir.mkdirs();
	}
	
	public static boolean mkdir(File dir) {
		return dir.mkdirs();
	}
	
	public static boolean deleteFile(File file) {
		if (file.isFile() && file.exists()) {
			file.delete();
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean deleteDir(File dirFile) {
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		boolean flag = true; // 删除文件夹下的所有文件(包括子目录)
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			// 删除子文件
			if (files[i].isFile()) {
				flag = deleteFile(files[i]);
			}
			// 删除子目录
			else {
				flag = deleteDir(files[i]);
			}
		}

		if (!flag)
			return false;
		
		// 删除当前目录
		if (dirFile.delete()) {
			return true;
		}
		
		return false;
	}
	
	public static void saveXml(Document doc, File file) {
		if (doc != null) {
			TransformerFactory transFactory = TransformerFactory.newInstance();// 取得TransformerFactory实例
			Transformer transformer;
			try {
				transformer = transFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); // 设置输出采用的编码方式
				transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // 是否自动添加额外的空白
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no"); // 是否忽略XML声明
				Source source = new DOMSource(doc);
				
				
				PrintWriter pw = new PrintWriter(new FileOutputStream(file));
				StreamResult result = new StreamResult(pw);
				transformer.transform(source, result);
				
				pw.close();
			} catch (TransformerConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static String getCurrentTime() {
		DateFormat df = new SimpleDateFormat(TimeUtils.FORMAT_yyyyMMddHHmmss);
		String name = df.format(new Date());

		return name;
	}
	
	protected static void setAttribute(Element elm, String name, String value) {
		if (name != null && value != null && elm != null)
			elm.setAttribute(name, value);
	}
	
	public interface OnPackListener {
		void onProgress(int progress);
		void onFinished(int errcode);
	}
	
	private OnPackListener mOnPackListener;
	
	public void setOnPackListener(OnPackListener listener) {
		mOnPackListener = listener;
	}
	
	protected void notifyProgress(int progress) {
		mProgress = progress;
		if (mOnPackListener != null) {
			mOnPackListener.onProgress(mProgress);
		}
	}
	
	protected void notifyFinished(int errcode) {
		if (mOnPackListener != null) {
			mOnPackListener.onFinished(errcode);
		}
	}

	class ZipRunnable implements Runnable {
		private int mProgress;
		private String mSrcDirPath;
		private String mDestPath;

		public ZipRunnable(int progress, String srcDir, String destPath) {
			mProgress = progress;
			mSrcDirPath = srcDir;
			mDestPath = destPath;
		}

		@Override
		public void run() {
			try {
				zip(mSrcDirPath, mDestPath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				notifyFinished(1);
			}
		}
		
		private void zip(String srcDirPath, String destPath) throws IOException {
	        File zipFile = new File(destPath);  
	        InputStream input = null;
	        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));        
	        
	        // list all file recursive
	        List<File> list = new ArrayList<File>();
	        File srcDir = new File(srcDirPath);
	        list.add(srcDir); // add eclass_dir to list
	        for (int i = 0; i < list.size(); i++) {
	        	File file = list.get(i);
	        	
	            if (file.isDirectory()) {
	            	File[] f = file.listFiles();
	            	for (int j = 0; j < f.length; j++) {
	            		Log.v("Pkg", "add: " + f[j].getName());
	            		list.add(f[j]);
	            	}
	            }
	        }
	        
	        mProgress++;
			notifyProgressAsync(mProgress);
	        
	        String eclass_dir_path = srcDir.getPath();
	        int start = eclass_dir_path.length() + 1;
	        
	        byte[] buffer = new byte[4096];
	        
	        list.remove(0); // remove eclass_dir from list
	        
	        int left = 100 - mProgress;
	        float progress_base = left;
	        if (list.size() > 0) {
	        	 progress_base = (float) left / list.size();
	        }
	        for (int i = 0; i < list.size(); i++) {
	        	File f = list.get(i);
	        	if (f.isDirectory()) {
	        		ZipEntry ze = new ZipEntry(f.getPath().substring(start) + File.separator);
	        		zipOut.putNextEntry(ze);
//	        		zipOut.closeEntry();
	        	} else {
	                input = new FileInputStream(f);
	                zipOut.putNextEntry(new ZipEntry(f.getPath().substring(start)));
	                
	                while (true) {
	                	int read = input.read(buffer);
	                	if (read <= 0)
	                		break;
	                	
	                	zipOut.write(buffer, 0, read);
	                }
	                
	                input.close();
//	                zipOut.closeEntry();
	        	}
	        	
	        	mProgress += progress_base;
	    		notifyProgressAsync(mProgress);
	        }
	        zipOut.close();
	        
	        notifyFinishedAsync(0);
	        Log.v("BasePkgTools", "zip finish");
		}
	};
	
	protected void notifyProgressAsync(int progress) {
		Message msg = Message.obtain(mHandler, MSG_UPDATE_PROGRESS, progress, 0);
		mHandler.sendMessage(msg);
	}
	
	protected void notifyFinishedAsync(int errcode) {
		Message msg = Message.obtain(mHandler, MSG_UPDATE_PROGRESS, errcode, 1);
		if (errcode == 0)
			mHandler.sendMessageDelayed(msg, 500);
		else
			mHandler.sendMessage(msg);
	}

	// msg id为0有用，请从1开始使用
	private static final int MSG_DO_NEXT_STEP = 1;
	private static final int MSG_UPDATE_PROGRESS = 3;
	
	protected static final int MSG_CUSTOM_BEGIN = 10;  // 继承类处理的消息定义在此基础上增加
	
	protected Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_DO_NEXT_STEP:
				doNextStep();
				break;
				
			case MSG_UPDATE_PROGRESS: {
				if (msg.arg2 != 0) {
					notifyFinished(msg.arg1);
				} else {
					int progress = msg.arg1;
					notifyProgress(progress);
				}
				break;
			}
				
			default:
				onHandleMessage(msg);
				break;
		}
		}
	};

	protected void onHandleMessage(Message msg) {
		
	}
	
	protected abstract void doNextStep();
	
	protected void postDoNextStep() {
		mNextStep++;
		mHandler.sendEmptyMessage(MSG_DO_NEXT_STEP);
	}
	
	protected void postNextStep() {
		mHandler.sendEmptyMessage(MSG_DO_NEXT_STEP);
	}
	
	/*
	 * 在dir目录下查找匹配regularExpression规则的第一个文件，未找到则返回默认文件名
	 */
	public static String firstFileName(File dir, String regularExpression, String defaultName) {
		String[] files = dir.list();
		if (files != null) {
			for (String name : files) {
				if (name.matches(regularExpression)) {
					return name;
				}
			}
		}
		
		return defaultName;
	}

}
