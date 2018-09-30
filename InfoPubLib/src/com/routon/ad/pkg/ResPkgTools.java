package com.routon.ad.pkg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.routon.ad.element.ResPkg;
import com.routon.ad.element.ResPkgFile;
import com.routon.ad.element.StringUtils;
import com.routon.inforelease.json.ClassInfoListdatasBean;
import com.routon.inforelease.json.ClassInfoListfilesBean;

public class ResPkgTools {
	private static final String TAG = "EClassPkgTools";
	
	File mCacheDir;
	File mResPkgDir;
	
	private TaskExecutor mExecutor;
	
	private List<String> mImageList = new ArrayList<String>();
	
	public ResPkgTools(File cache_dir) {
		mExecutor = new TaskExecutor();
		
		mCacheDir = cache_dir;
		File respkg_dir = new File(cache_dir, "respkg");
		mResPkgDir = respkg_dir;
	}
	
	private int mNextStep = 0;
	private int mProgress;
	
	private List<ClassInfoListdatasBean> mClassDataList;
		
	public void startMakePkg(List<ClassInfoListdatasBean> dataList) {
		mNextStep = 0;
		mClassDataList = dataList;

		mImageList.clear();
		mProgress = 0;

		deleteDir(mResPkgDir);
		mResPkgDir.mkdir();
		createDir(mResPkgDir);
		
		notifyProgress(1);
		
		mNextStep = 1;
		doNextStep();
	}
	
	private float mImageProgressBase = 1.0f;
	
	private void doNextStep() {
		switch (mNextStep) {
		case 1: {
			List<ResPkg> list = new ArrayList<ResPkg>();
			int id = 100;
			for (ClassInfoListdatasBean data : mClassDataList) {
				ResPkg resPkg = new ResPkg();
				resPkg.id = id;
				resPkg.title = data.title;
				
				resPkg.version = Long.toString(System.currentTimeMillis());
				resPkg.file_url = "ftp://xxx/respkg/" + resPkg.id + ".xml";
				
				int fileId = id;
				for (ClassInfoListfilesBean bean : data.files) {
					ResPkgFile file = new ResPkgFile();
					file.id = fileId;
					file.duration = StringUtils.toInteger(data.duration, 30);
					file.url = bean.content;
					file.content = data.desc;
					
					fileId++;
					
					mImageList.add(file.url);
					
					resPkg.files.add(file);
				}
				if (data.files != null && data.files.size() > 0) {
					resPkg.url = data.files.get(0).content;
				}

				id += 100;
				list.add(resPkg);
			}
			writeResPkg(list, mResPkgDir);
			
			notifyProgress(mProgress + 1);
			
			mNextStep++;
			doNextStep();
		}
			break;
			
		case 2:				
				int left = (100 - mProgress) / 2;
				mImageProgressBase = left;
				if (mImageList.size() > 0) {
					mImageProgressBase = (float) left / mImageList.size();
				} else {
					notifyProgress(mProgress + left);
				}

				mNextStep++;
				doNextStep();
			break;
			
		case 3: {
			if (mImageList.size() > 0) {
				String url = mImageList.get(0);
				String name = StringUtils.getFileName(url);
				File pic_dir = mResPkgDir;
				String path = new File(pic_dir, name).getPath();
				FileGetTask task = new FileGetTask(url, path, url);
				task.setOnHttpGetTaskListener(mOnImageGetTaskListener);
				mExecutor.execute(task);
			} else {
				mNextStep++;
				doNextStep();
			}
		}
			break;
			
		case 4:
			Log.v(TAG, "start zip");
			mExecutor.execute(new ZipRunnable(mProgress));
			break;
			
		default:		
			break;
		}
	}
		
	private static final int MSG_GET_IMAGE = 2;
	private static final int MSG_UPDATE_PROGRESS = 3;
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
			{
				switch (msg.arg1) {
					
				case MSG_GET_IMAGE:
					onGetImage((String) msg.obj);
					break;
				}
			}
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
		}
		}
	};
	
	private void onGetImage(String url) {
		Log.v(TAG, "onGetImage: " + url);
		
		mImageList.remove(0);
		
		Log.v(TAG, "img list left size: " + mImageList.size());
		
		notifyProgress((int) (mProgress + mImageProgressBase));
		
		doNextStep();
	}
	
//	private void doIfDownloadFinished() {
//		if (mImageLoaded && mTextLoaded && (mImageList.size() == 0)) {
//			Log.v(TAG, "start zip");
//			mExecutor.execute(new ZipRunnable(mProgress));
//		} 
//	}

	public static void createDir(File respkg_dir) {
		//mkdir(respkg_dir, "imgs");
	}
	
	public static boolean mkdir(File parent, String dir_name) {
		File dir = new File(parent, dir_name);
		return dir.mkdir();
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
	
	private static void writeResPkg(List<ResPkg> list, File respkg_dir) {
		File path = new File(respkg_dir, "respkg.xml");
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element root = doc.createElement("root");
			doc.appendChild(root);
			Element pkgs_elm = doc.createElement("pkgs");
			root.appendChild(pkgs_elm);

			for (ResPkg t : list) {
				Element pkg_elm = doc.createElement("pkg");
				pkg_elm.setAttribute("id", Integer.toString(t.id));
				pkg_elm.setAttribute("version", t.version);
				pkg_elm.setAttribute("url", t.file_url);				
				pkgs_elm.appendChild(pkg_elm);
				
				String name = StringUtils.getFileName(t.file_url);
				File resPkgFile = new File(respkg_dir, name);
				Document resPkgDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				Element resPkgRoot = resPkgDoc.createElement("root");
				resPkgDoc.appendChild(resPkgRoot);
				Element e = t.toElement(resPkgDoc);
				resPkgRoot.appendChild(e);
				saveXml(resPkgDoc, resPkgFile);
			}
		
			saveXml(doc, path);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void saveXml(Document doc, File file) {
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
	
	private class ZipRunnable implements Runnable {
		private int mProgress;

		public ZipRunnable(int progress) {
			mProgress = progress;
		}

		@Override
		public void run() {
			try {
				zip(mResPkgDir, mCacheDir);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				notifyFinished(1);
			}
		}
		
		private void notifyProgress(int progress) {
			Message msg = Message.obtain(mHandler, MSG_UPDATE_PROGRESS, progress, 0);
			mHandler.sendMessage(msg);
		}
		
		private void notifyFinished(int errcode) {
			Message msg = Message.obtain(mHandler, MSG_UPDATE_PROGRESS, errcode, 1);
			if (errcode == 0)
				mHandler.sendMessageDelayed(msg, 500);
			else
				mHandler.sendMessage(msg);
		}
		
		private void zip(File respkg_dir, File cache_dir) throws IOException {
	        File zipFile = new File(cache_dir, "respkg.zip");  
	        InputStream input = null;
	        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));        
	        
	        // list all file recursive
	        List<File> list = new ArrayList<File>();
	        list.add(respkg_dir); // add respkg_dir to list
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
			notifyProgress(mProgress);
	        
	        String respkg_dir_path = respkg_dir.getPath();
	        int start = respkg_dir_path.length() + 1;
	        
	        byte[] buffer = new byte[4096];
	        
	        list.remove(0); // remove respkg_dir from list
	        
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
	    		notifyProgress(mProgress);
	        }
	        zipOut.close();
	        
	        notifyFinished(0);
	        Log.v(TAG, "zip finish");
		}
	};
	
	
	private HttpGetTask.OnHttpGetTaskListener mOnImageGetTaskListener = new HttpGetTask.OnHttpGetTaskListener() {
		
		@Override
		public void onTaskStarted(HttpGetTask task) {
			
		}
		
		@Override
		public void onTaskFinished(HttpGetTask task, int code) {
			Message msg = mHandler.obtainMessage(0, MSG_GET_IMAGE, 0, task.getContext());
			mHandler.sendMessage(msg);
		}
	};
	
	public void cancel() {
		
	}
	
	public interface OnPackListener {
		void onProgress(int progress);
		void onFinished(int errcode);
	}
	
	private OnPackListener mOnPackListener;
	
	public void setOnPackListener(OnPackListener listener) {
		mOnPackListener = listener;
	}
	
	private void notifyProgress(int progress) {
		mProgress = progress;
		if (mOnPackListener != null) {
			mOnPackListener.onProgress(mProgress);
		}
	}
	
	private void notifyFinished(int errcode) {
		if (mOnPackListener != null) {
			mOnPackListener.onFinished(errcode);
		}
	}
}
